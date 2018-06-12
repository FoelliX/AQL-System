package de.foellix.aql.system;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.IQuestionNode;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.Question;
import de.foellix.aql.datastructure.QuestionPart;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.HashHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.PreprocessorTaskInfo;
import de.foellix.aql.system.task.ToolTaskInfo;

public class QueryProcessor {
	private final System parent;

	private List<PreprocessorTaskInfo> preprocessTasks;

	QueryProcessor(System parent) {
		this.parent = parent;
	}

	void preprocess() {
		Log.msg("***** Initial Query *****\n" + this.parent.getCurrentQuery().toString(), Log.DEBUG);

		int size = 0;
		this.preprocessTasks = new ArrayList<>();

		if (this.parent.getCurrentQuery().getAllQuestionParts() != null) {
			// Run Preprocessors
			for (final QuestionPart questionPart : this.parent.getCurrentQuery().getAllQuestionParts()) {
				for (final Reference reference : questionPart.getReferences()) {
					if (addPreprocessorTask(questionPart, reference) != null) {
						size += questionPart.getPreprocessor(reference).size();
					}
				}
			}

			// Execute preprocessors
			this.parent.getScheduler().setWaiting(size);
			this.parent.setMax(this.parent.getScheduler().getWaiting());
			this.parent.progress("Step 1 of 3: Preprocessing");
			if (this.parent.getScheduler().getWaiting() > 0) {
				for (final PreprocessorTaskInfo task : this.preprocessTasks) {
					this.parent.getScheduler().schedulePreprocessor(task);
				}
				this.parent.getScheduler().runSchedule();
			} else {
				ask(this.parent.getCurrentQuery());
			}
		} else {
			return;
		}
	}

	PreprocessorTaskInfo addPreprocessorTask(QuestionPart questionPart, Reference reference) {
		if (questionPart.getPreprocessor(reference) != null && !questionPart.getPreprocessor(reference).isEmpty()) {
			final String keyword = questionPart.getPreprocessor(reference).get(0);
			final Tool preprocessor = ToolSelector.getInstance().selectPreprocessor(questionPart, keyword);

			if (preprocessor != null) {
				boolean running = false;
				for (final PreprocessorTaskInfo task : this.preprocessTasks) {
					if (task.getTool() != null
							&& task.getHash().equals(HashHelper.createHash(preprocessor, reference.getApp()))) {
						running = true;
					}
				}
				if (!running) {
					final PreprocessorTaskInfo newTaskInfo = new PreprocessorTaskInfo(preprocessor, reference.getApp(),
							questionPart, keyword);
					this.preprocessTasks.add(newTaskInfo);
					return newTaskInfo;
				}
			} else {
				Log.error("No applicable preprocessor found for keyword: " + keyword);
				return null;
			}
		}
		return null;
	}

	void ask(final IQuestionNode question) {
		Log.msg("***** Preprocessed Query *****\n" + question.toString(), Log.DEBUG);

		this.parent.setTempStorage(new HashMap<>());

		if (question.getAllQuestionParts() != null) {
			// All tools available?
			boolean fixpoint = false;
			while (!fixpoint) {
				fixpoint = true;
				for (final QuestionPart questionPart : question.getAllQuestionParts()) {
					final Tool tool = ToolSelector.getInstance().selectTool(questionPart);
					if (tool == null) {
						Log.warning("No applicable tool could be found. Trying to transform the question.");
						final IQuestionNode transformedQuestion = transformQuestion(questionPart);
						if (transformedQuestion == null) {
							if (Storage.getInstance().load(null, questionPart, true) != null) {
								Log.warning(
										"No applicable tool could be found. However, there is a previously computed answer available.");
							} else {
								Log.error("No applicable tool could be found.");
								return;
							}
						} else {
							if (question == questionPart) {
								this.parent.setCurrentQuery(transformedQuestion);
								ask(transformedQuestion);
								return;
							} else {
								Helper.replaceQuestionPart(question, questionPart, transformedQuestion);
							}
							fixpoint = false;
							break;
						}
					} else {
						if (questionPart.getReferences().size() == 1) {
							final File folder = new File(questionPart.getReferences().get(0).getApp().getFile());
							if (folder.isDirectory()) {
								final List<File> apks = new ArrayList<>();
								for (final File file : folder.listFiles()) {
									if (file.getAbsolutePath().endsWith(".apk")) {
										apks.add(file);
									}
								}
								if (apks.size() >= 1) {
									final Question expandedQuestion = new Question(
											KeywordsAndConstants.OPERATOR_COLLECTION);
									for (final File apk : apks) {
										final QuestionPart newPart = Helper.copy(questionPart);
										newPart.getReferences().get(0).setApp(Helper.createApp(apk.getAbsolutePath()));
										expandedQuestion.addChild(newPart);
									}
									if (question == questionPart) {
										this.parent.setCurrentQuery(expandedQuestion);
										ask(expandedQuestion);
										return;
									} else {
										Helper.replaceQuestionPart(question, questionPart, expandedQuestion);
									}
									fixpoint = false;
									break;
								}
							}
						} else if (questionPart.getReferences().size() == 2) {
							final File folder1 = new File(questionPart.getReferences().get(0).getApp().getFile());
							final File folder2 = new File(questionPart.getReferences().get(0).getApp().getFile());
							final List<File> apks1 = new ArrayList<>();
							final List<File> apks2 = new ArrayList<>();
							if (folder1.isDirectory()) {
								for (final File file : folder1.listFiles()) {
									if (file.getAbsolutePath().endsWith(".apk")) {
										apks1.add(file);
									}
								}
							} else {
								apks1.add(folder1);
							}
							if (folder2.isDirectory()) {
								for (final File file : folder2.listFiles()) {
									if (file.getAbsolutePath().endsWith(".apk")) {
										apks2.add(file);
									}
								}
							} else {
								apks2.add(folder2);
							}

							if ((apks1.size() > 1) || (apks2.size() > 1)) {
								final Question expandedQuestion = new Question(
										KeywordsAndConstants.OPERATOR_COLLECTION);
								for (final File apk1 : apks1) {
									for (final File apk2 : apks2) {
										final QuestionPart newPart = Helper.copy(questionPart);
										newPart.getReferences().get(0).setApp(Helper.createApp(apk1.getAbsolutePath()));
										newPart.getReferences().get(1).setApp(Helper.createApp(apk2.getAbsolutePath()));
										expandedQuestion.addChild(newPart);
									}
								}
								if (question == questionPart) {
									this.parent.setCurrentQuery(expandedQuestion);
									ask(expandedQuestion);
									return;
								} else {
									Helper.replaceQuestionPart(question, questionPart, expandedQuestion);
								}
								fixpoint = false;
								break;
							}
						}
					}
				}
			}

			Log.msg("***** Final Query *****\n" + question.toString(), Log.DEBUG);

			// Execute tools
			this.parent.getScheduler().setWaiting(question.getAllQuestionParts().size());
			this.parent.setMax(this.parent.getScheduler().getWaiting());
			if (this.parent.getMax() != 0) {
				this.parent.progress("Step 2 of 3: Analyzing");
				for (final QuestionPart questionPart : question.getAllQuestionParts()) {
					final ToolTaskInfo taskInfo = new ToolTaskInfo(ToolSelector.getInstance().selectTool(questionPart),
							questionPart);
					this.parent.getScheduler().scheduleTool(taskInfo);
				}
				this.parent.getScheduler().runSchedule();
			} else {
				// Nothing to execute - Just loading answers
				this.parent.localAnswerAvailable(null, null);
			}
		} else {
			return;
		}
	}

	private IQuestionNode transformQuestion(final QuestionPart question) {
		// Match pattern
		if (question.getMode() == KeywordsAndConstants.QUESTION_TYPE_FLOWS) {
			if (ToolSelector.getInstance().detectInterApp(question)) {
				Reference from, to;

				if (question.getReferences().get(0).getType().equals(KeywordsAndConstants.REFERENCE_TYPE_FROM)) {
					from = question.getReferences().get(0);
					to = question.getReferences().get(1);
				} else {
					from = question.getReferences().get(1);
					to = question.getReferences().get(0);
				}

				// IntraAppParts
				final QuestionPart q1 = new QuestionPart();
				q1.setMode(KeywordsAndConstants.QUESTION_TYPE_FLOWS);
				q1.addReference(from);

				final QuestionPart q2 = new QuestionPart();
				q2.setMode(KeywordsAndConstants.QUESTION_TYPE_FLOWS);
				q2.addReference(to);

				// Intent-Sources & -SinksParts
				final QuestionPart q3 = new QuestionPart();
				q3.setMode(KeywordsAndConstants.QUESTION_TYPE_INTENTSINKS);
				q3.addReference(from);

				final QuestionPart q4 = new QuestionPart();
				q4.setMode(KeywordsAndConstants.QUESTION_TYPE_INTENTSOURCES);
				q4.addReference(to);

				// Connect
				final Question connectNode = new Question(KeywordsAndConstants.OPERATOR_CONNECT);
				connectNode.addChild(q1);
				connectNode.addChild(q2);
				connectNode.addChild(q3);
				connectNode.addChild(q4);

				// Filter
				final Question newQuestion = new Question(KeywordsAndConstants.OPERATOR_FILTER);
				newQuestion.addChild(connectNode);

				return newQuestion;
			}
		} else {
			// TODO: Add other useful transforms
		}

		return null;
	}
}
