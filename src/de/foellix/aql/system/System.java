package de.foellix.aql.system;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.IQuestionNode;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.PreviousQuestion;
import de.foellix.aql.datastructure.Question;
import de.foellix.aql.datastructure.QuestionPart;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.WaitingAnswer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.datastructure.handler.ParseException;
import de.foellix.aql.datastructure.handler.QuestionHandler;
import de.foellix.aql.datastructure.handler.QuestionParser;
import de.foellix.aql.helper.EqualsHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.PreprocessorTaskInfo;
import de.foellix.aql.system.task.Task;
import de.foellix.aql.ui.cli.AnswerToConsole;

public class System {
	private boolean storeAnswers = true;

	private final QueryProcessor queryProcessor;
	private final Scheduler scheduler;
	private final OperatorManager operatorManager;

	private final List<IAnswerAvailable> answerReceivers;
	private final List<IProgressChanged> progressListener;

	private String currentInitialQuery;
	private Collection<Answer> waitForAnswers;
	private IQuestionNode currentQuery;
	private Map<QuestionPart, Answer> tempStorage;
	private String step;
	private int max;

	public System() {
		this.max = 0;

		this.answerReceivers = new ArrayList<>();
		this.progressListener = new ArrayList<>();

		if (Log.logIt(Log.NORMAL)) {
			this.answerReceivers.add(new AnswerToConsole());
		}

		this.queryProcessor = new QueryProcessor(this);
		this.scheduler = new Scheduler(this);
		this.operatorManager = new OperatorManager(this);
	}

	public void query(final String query) {
		Log.reset();
		this.currentInitialQuery = query.replaceAll("\n", "").replaceAll("\t", "").replaceAll("\r", "");
		Log.msg("Input: " + this.currentInitialQuery, Log.IMPORTANT);

		try {
			final InputStream input = new ByteArrayInputStream(query.getBytes());
			final QuestionParser parser = new QuestionParser(input);
			parser.query();
			final QuestionHandler questionHandler = parser.getQuestionHandler();
			this.currentQuery = questionHandler.getCollection();

			if (this.currentQuery.getChildren() != null && this.currentQuery.getChildren().size() == 1) {
				this.currentQuery = this.currentQuery.getChildren().get(0);
			}

			this.queryProcessor.preprocess();
		} catch (final ParseException e) {
			Log.error("The query is not valid.");
			e.printStackTrace();
			return;
		}
	}

	public Collection<Answer> queryAndWait(final String query) {
		this.waitForAnswers = null;
		new Thread(() -> {
			query(query);
		}).start();
		while (this.waitForAnswers == null) {
			try {
				Thread.sleep(250);
			} catch (final InterruptedException e) {
				return null;
			}
		}
		return this.waitForAnswers;
	}

	// FIXME:
	public void preprocessingFinished(final PreprocessorTaskInfo taskInfo, final App storedPreprocessedVersion) {
		// Replace with preprocessed versions
		final QuestionPart questionPart = taskInfo.getQuestion();
		for (final Reference reference : questionPart.getReferences()) {
			if (EqualsHelper.equals(reference.getApp(), taskInfo.getApp())) {
				if (storedPreprocessedVersion != null) {
					reference.setApp(storedPreprocessedVersion);
				}

				questionPart.removePreprocessor(reference, taskInfo.getKeyword());

				try {
					// Add next preprocessor task to scheduler
					final PreprocessorTaskInfo followUpTask = this.queryProcessor.addPreprocessorTask(questionPart,
							reference);
					if (followUpTask != null) {
						this.scheduler.schedulePreprocessor(followUpTask);
					}
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}

		this.scheduler.decreaseWaiting();
		progress();
		if (this.scheduler.getWaiting() <= 0) {
			this.queryProcessor.ask(this.currentQuery);
		}
	}

	public void localAnswerAvailable(final QuestionPart questionPart, Answer answer) {
		if (questionPart != null && answer == null) {
			Log.msg("Aborted: " + this.currentInitialQuery, Log.IMPORTANT);

			this.scheduler.setWaiting(0);
			this.scheduler.getRunningInstances().clear();
			this.scheduler.getSchedule().clear();
			for (final Task task : this.scheduler.getCurrentlyRunningThreads()) {
				task.interrupt();
			}
			this.scheduler.getCurrentlyRunningThreads().clear();
			progress("Aborted");

			for (final IAnswerAvailable receiver : this.answerReceivers) {
				receiver.answerAvailable(new Answer(), KeywordsAndConstants.ANSWER_STATUS_FAILED);
			}
			return;
		}

		if (questionPart != null && answer != null) {
			// Filter by subject of interest
			answer = filterBySOI(questionPart, answer);

			// Filter by References
			answer = filterByRef(questionPart, answer);

			// Intermediate Result Output
			Log.msg("**** Answer available ****\nQuestion:\n" + questionPart.toString() + "\nAnswer:\n"
					+ Helper.toString(answer), Log.DEBUG_DETAILED);
			this.tempStorage.put(questionPart, answer);
		}

		this.scheduler.decreaseWaiting();
		progress();
		if (this.scheduler.getWaiting() <= 0) {
			this.operatorManager.apply();
		}
	}

	public void operatorExecuted(WaitingAnswer waitingAnswer, Answer answer) {
		if (waitingAnswer != null) {
			waitingAnswer.setAnswer(answer);
		}

		this.scheduler.decreaseWaiting();
		progress();
		if (this.scheduler.getWaiting() <= 0) {
			final Collection<Answer> collection = this.operatorManager.getAnswerCollection();

			Log.msg("****** Answer Collection ******\n", Log.DEBUG_DETAILED);
			for (Answer collectionPart : collection) {
				if (collectionPart instanceof WaitingAnswer) {
					collectionPart = ((WaitingAnswer) collectionPart).getAnswer();
				}

				if (this.storeAnswers) {
					final File xmlFile = Helper.makeUnique(new File("answers/answer_" + Helper.getDate() + "-0.xml"));
					AnswerHandler.createXML(collectionPart, xmlFile);
				}
				Log.msg("***** Answer *****\n" + Helper.toString(collectionPart), Log.DEBUG);

				for (final IAnswerAvailable receiver : this.answerReceivers) {
					receiver.answerAvailable(collectionPart, KeywordsAndConstants.ANSWER_STATUS_SUCCESSFUL);
				}
			}

			this.waitForAnswers = collection;

			Log.msg("Finished: " + this.currentInitialQuery, Log.IMPORTANT);
		}
	}

	private Answer filterBySOI(final QuestionPart questionPart, final Answer answer) {
		final int[] sois = { KeywordsAndConstants.QUESTION_TYPE_FLOWS, KeywordsAndConstants.QUESTION_TYPE_INTENTFILTER,
				KeywordsAndConstants.QUESTION_TYPE_INTENTS, KeywordsAndConstants.QUESTION_TYPE_INTENTSINKS,
				KeywordsAndConstants.QUESTION_TYPE_INTENTSOURCES, KeywordsAndConstants.QUESTION_TYPE_PERMISSIONS };
		for (final int soi : sois) {
			if (questionPart.getMode() != soi) {
				DefaultOperator.filter3(answer, soi);
			}
		}
		return answer;
	}

	private Answer filterByRef(final QuestionPart questionPart, final Answer answer) {
		// Permissions
		if (answer.getPermissions() != null) {
			for (int i = 0; i < answer.getPermissions().getPermission().size(); i++) {
				final Reference ref = answer.getPermissions().getPermission().get(i).getReference();

				if (!EqualsHelper.equals(questionPart.getReferences().get(0), ref, true)) {
					answer.getPermissions().getPermission().remove(i);
					i--;
				}
			}
			if (answer.getPermissions().getPermission().size() == 0) {
				answer.setPermissions(null);
			}
		}

		// Intents
		if (answer.getIntents() != null) {
			for (int i = 0; i < answer.getIntents().getIntent().size(); i++) {
				final Reference ref = answer.getIntents().getIntent().get(i).getReference();

				if (!EqualsHelper.equals(questionPart.getReferences().get(0), ref, true)) {
					answer.getIntents().getIntent().remove(i);
					i--;
				}
			}
			if (answer.getIntents().getIntent().size() == 0) {
				answer.setIntents(null);
			}
		}

		// Intent-Filters
		if (answer.getIntentfilters() != null) {
			for (int i = 0; i < answer.getIntentfilters().getIntentfilter().size(); i++) {
				final Reference ref = answer.getIntentfilters().getIntentfilter().get(i).getReference();

				if (!EqualsHelper.equals(questionPart.getReferences().get(0), ref, true)) {
					answer.getIntentfilters().getIntentfilter().remove(i);
					i--;
				}
			}
			if (answer.getIntentfilters().getIntentfilter().size() == 0) {
				answer.setIntentfilters(null);
			}
		}

		// Intent-Sinks
		if (answer.getIntentsinks() != null) {
			for (int i = 0; i < answer.getIntentsinks().getIntentsink().size(); i++) {
				final Reference ref = answer.getIntentsinks().getIntentsink().get(i).getReference();

				if (!EqualsHelper.equals(questionPart.getReferences().get(0), ref, true)) {
					answer.getIntentsinks().getIntentsink().remove(i);
					i--;
				}
			}
			if (answer.getIntentsinks().getIntentsink().size() == 0) {
				answer.setIntentsinks(null);
			}
		}

		// Intent-Sources
		if (answer.getIntentsources() != null) {
			for (int i = 0; i < answer.getIntentsources().getIntentsource().size(); i++) {
				final Reference ref = answer.getIntentsources().getIntentsource().get(i).getReference();

				if (!EqualsHelper.equals(questionPart.getReferences().get(0), ref, true)) {
					answer.getIntentsources().getIntentsource().remove(i);
					i--;
				}
			}
			if (answer.getIntentsources().getIntentsource().size() == 0) {
				answer.setIntentsources(null);
			}
		}

		// Flows
		if (answer.getFlows() != null) {
			for (int i = 0; i < answer.getFlows().getFlow().size(); i++) {
				boolean keepFrom = false;
				boolean keepTo = questionPart.getReferences().size() < 2;

				Reference fromRef = null;
				Reference toRef = null;
				for (final Reference ref : answer.getFlows().getFlow().get(i).getReference()) {
					// From
					if (ref.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_FROM)) {
						fromRef = ref;
						if (EqualsHelper.equals(questionPart.getReferences().get(0), fromRef, true)) {
							keepFrom = true;
						}
					}

					// To
					if (!keepTo) {
						if (ref.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_TO)) {
							toRef = ref;
							if (EqualsHelper.equals(questionPart.getReferences().get(1), toRef, true)) {
								keepTo = true;
							}
						}
					}
				}

				// Check for transitive connection
				if (fromRef != null && toRef != null) {
					if (!keepFrom && keepTo) {
						for (final Flow flow : answer.getFlows().getFlow()) {
							boolean keepFrom2 = false;
							boolean keepTo2 = false;

							for (final Reference ref : flow.getReference()) {
								if (ref.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_FROM)) {
									if (EqualsHelper.equals(questionPart.getReferences().get(0), ref, true)) {
										keepFrom2 = true;
									}
								} else if (ref.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_TO)) {
									if (EqualsHelper.equals(ref, fromRef)) {
										keepTo2 = true;
									}
								}
							}

							if (keepFrom2 && keepTo2) {
								keepFrom = true;
								break;
							}
						}
					} else if (keepFrom && !keepTo) {
						for (final Flow flow : answer.getFlows().getFlow()) {
							boolean keepFrom2 = false;
							boolean keepTo2 = false;

							for (final Reference ref : flow.getReference()) {
								if (ref.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_FROM)) {
									if (EqualsHelper.equals(ref, toRef)) {
										keepFrom2 = true;
									}
								} else if (ref.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_TO)) {
									if (EqualsHelper.equals(questionPart.getReferences().get(1), ref, true)) {
										keepTo2 = true;
									}
								}
							}

							if (keepFrom2 && keepTo2) {
								keepTo = true;
								break;
							}
						}
					}
				}

				if (!keepFrom || !keepTo) {
					answer.getFlows().getFlow().remove(i);
					i--;
				}
			}
			if (answer.getFlows().getFlow().size() == 0) {
				answer.setFlows(null);
			}
		}

		return answer;
	}

	List<Answer> buildCompleteAnswer(final IQuestionNode question) {
		final List<Answer> answerCollection = new ArrayList<>();
		if (question instanceof Question
				&& ((Question) question).getOperator().equals(KeywordsAndConstants.OPERATOR_COLLECTION)) {
			for (final IQuestionNode child : question.getChildren()) {
				answerCollection.add(buildCompleteAnswerNotCollection(child));
			}
		} else {
			answerCollection.add(buildCompleteAnswerNotCollection(question));
		}
		return answerCollection;
	}

	Answer buildCompleteAnswerNotCollection(final IQuestionNode question) {
		if (question instanceof Question) {
			return this.operatorManager.applyOperator((Question) question);
		} else if (question instanceof QuestionPart) {
			return this.tempStorage.get(question);
		} else {
			final Answer a = AnswerHandler.parseXML(new File(((PreviousQuestion) question).getFile()));
			if (a != null) {
				return a;
			} else {
				return new Answer();
			}
		}
	}

	void progress() {
		progress(this.step);
	}

	void progress(final String step) {
		this.step = step;

		int inProgress = 0;
		if (this.scheduler.getWaiting() != 0) {
			for (final Integer active : this.scheduler.getRunningInstances().values()) {
				inProgress += active.intValue();
			}
		}

		for (final IProgressChanged listener : this.progressListener) {
			listener.onProgressChanged(step, inProgress, this.max - this.scheduler.getWaiting(), this.max);
		}
	}

	public void cancel() {
		localAnswerAvailable(new QuestionPart(), null);
	}

	// Getters & Setters
	public QueryProcessor getQueryProcessor() {
		return this.queryProcessor;
	}

	public Scheduler getScheduler() {
		return this.scheduler;
	}

	public OperatorManager getOperatorManager() {
		return this.operatorManager;
	}

	public List<IAnswerAvailable> getAnswerReceivers() {
		return this.answerReceivers;
	}

	public List<IProgressChanged> getProgressListener() {
		return this.progressListener;
	}

	public IQuestionNode getCurrentQuery() {
		return this.currentQuery;
	}

	public Map<QuestionPart, Answer> getTempStorage() {
		return this.tempStorage;
	}

	public int getMax() {
		return this.max;
	}

	public void setCurrentQuery(IQuestionNode currentQuery) {
		this.currentQuery = currentQuery;
	}

	public void setStoreAnswers(boolean storeAnswers) {
		this.storeAnswers = storeAnswers;
	}

	public void setTempStorage(Map<QuestionPart, Answer> tempStorage) {
		this.tempStorage = tempStorage;
	}

	public void setMax(int max) {
		this.max = max;
	}
}