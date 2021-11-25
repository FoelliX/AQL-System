package de.foellix.aql.system.defaulttools.operators;

import java.io.File;
import java.util.Collection;

import de.foellix.aql.Log;
import de.foellix.aql.Properties;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Attribute;
import de.foellix.aql.datastructure.Attributes;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.datastructure.query.FilterQuestion;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.helper.EqualsHelper;
import de.foellix.aql.helper.EqualsOptions;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.helper.KeywordsAndConstantsHelper;
import de.foellix.aql.system.storage.Storage;
import de.foellix.aql.system.task.FilterOperatorTaskInfo;
import de.foellix.aql.system.task.OperatorTask;
import de.foellix.aql.system.task.OperatorTaskInfo;
import de.foellix.aql.system.task.TaskAnswer;

public class DefaultFilterOperator extends DefaultOperator {
	public DefaultFilterOperator() {
		super();
		this.execute.setRun(this.getClass().getSimpleName() + " (" + Properties.info().VERSION + ") "
				+ OperatorTaskInfo.ANSWERS + ", " + OperatorTaskInfo.ANSWERSHASH + ", "
				+ OperatorTaskInfo.ANSWERSHASH_MD5 + ", " + OperatorTaskInfo.ANSWERSHASH_SHA1 + ", "
				+ OperatorTaskInfo.ANSWERSHASH_SHA256 + ", " + FilterOperatorTaskInfo.KEY + ", "
				+ FilterOperatorTaskInfo.VALUE + ", " + FilterOperatorTaskInfo.SUBJECT_OF_INTEREST + ", "
				+ FilterOperatorTaskInfo.REFERENCE_STATEMENT + ", " + FilterOperatorTaskInfo.REFERENCE_LINENUMBER + ", "
				+ FilterOperatorTaskInfo.REFERENCE_METHOD + ", " + FilterOperatorTaskInfo.REFERENCE_CLASS + ", "
				+ FilterOperatorTaskInfo.REFERENCE_APK);
	}

	@Override
	public File applyOperator(OperatorTask task) {
		final File answerFile = FileHelper.getTempFile(FileHelper.FILE_ENDING_XML);
		AnswerHandler.createXML(applyOperatorInner(task), answerFile);
		return answerFile;
	}

	public Answer applyOperatorInner(OperatorTask task) {
		// Get answer
		final Collection<Question> relatedQuestions = ((FilterQuestion) Storage.getInstance().getData()
				.getQuestionFromQuestionTaskMap(task)).getChildren(false, true);
		Answer temp = null;
		if (relatedQuestions.size() == 1) {
			final TaskAnswer tempTA = Storage.getInstance().getData()
					.getTaskFromQuestionTaskMap(relatedQuestions.iterator().next()).getTaskAnswer();
			if (tempTA.getType() == TaskAnswer.ANSWER_TYPE_AQL) {
				temp = (Answer) tempTA.getAnswer();
			} else if (tempTA.getType() == TaskAnswer.ANSWER_TYPE_FILE) {
				temp = AnswerHandler.castToAnswer(tempTA.getAnswerFile());
			}
		}
		if (temp == null) {
			Log.error("Could not apply FILTER operator! (Can only be applied on a single AQL-Answer.)");
			return null;
		}

		// Filter
		boolean keyValue = false;
		if ((task.getTaskInfo().getData(FilterOperatorTaskInfo.KEY) != null
				&& !task.getTaskInfo().getData(FilterOperatorTaskInfo.KEY).isEmpty())
				&& (task.getTaskInfo().getData(FilterOperatorTaskInfo.VALUE) != null
						&& !task.getTaskInfo().getData(FilterOperatorTaskInfo.VALUE).isEmpty())) {
			keyValue = true;
		}
		boolean reference = false;
		if ((task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_STATEMENT) != null
				&& !task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_STATEMENT).isEmpty())
				|| (task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_METHOD) != null
						&& !task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_METHOD).isEmpty())
				|| (task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_CLASS) != null
						&& !task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_CLASS).isEmpty())
				|| (task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_APK) != null
						&& !task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_APK).isEmpty())) {
			reference = true;
		}
		final boolean soi = task.getTaskInfo().getData(FilterOperatorTaskInfo.SUBJECT_OF_INTEREST) != null
				&& !task.getTaskInfo().getData(FilterOperatorTaskInfo.SUBJECT_OF_INTEREST).isEmpty();

		int soiType = Question.QUESTION_TYPE_UNKNOWN;
		if (soi) {
			soiType = Helper.soiToType(task.getTaskInfo().getData(FilterOperatorTaskInfo.SUBJECT_OF_INTEREST));
		}
		if (!keyValue && !reference) {
			if (soi) {
				return filterOutSOI(temp, soiType);
			} else {
				return filter(temp);
			}
		} else {
			if (keyValue) {
				return filterByKeyValue(temp, task.getTaskInfo().getData(FilterOperatorTaskInfo.KEY),
						task.getTaskInfo().getData(FilterOperatorTaskInfo.VALUE), soiType);
			}
			if (reference) {
				final Reference ref = new Reference();
				if (task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_STATEMENT) != null
						&& !task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_STATEMENT).isEmpty()) {
					if (task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_LINENUMBER) != null
							&& !task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_LINENUMBER).isEmpty()) {
						ref.setStatement(Helper.createStatement(
								task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_STATEMENT),
								Integer.parseInt(
										task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_LINENUMBER))));
					} else {
						ref.setStatement(Helper.createStatement(
								task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_STATEMENT)));
					}
				}
				if (task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_METHOD) != null
						&& !task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_METHOD).isEmpty()) {
					ref.setMethod(task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_METHOD));
				}
				if (task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_CLASS) != null
						&& !task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_CLASS).isEmpty()) {
					ref.setClassname(task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_CLASS));
				}
				if (task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_APK) != null
						&& !task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_APK).isEmpty()) {
					ref.setApp(Helper.createApp(task.getTaskInfo().getData(FilterOperatorTaskInfo.REFERENCE_APK)));
				}

				return filterByReference(temp, ref, soiType);
			}
		}

		return null;
	}

	/**
	 * Filters the given answer. Only keeps elements that refer to references a flow-element refers to.
	 *
	 * @param answer
	 *            The answer to be filtered
	 * @return The filtered answer
	 */
	public Answer filter(Answer answer) {
		// Permissions
		if (answer.getPermissions() != null) {
			for (int i = 0; i < answer.getPermissions().getPermission().size(); i++) {
				final Reference ref1 = answer.getPermissions().getPermission().get(i).getReference();

				if (foundInFlows(answer.getFlows(), ref1)) {
					answer.getPermissions().getPermission().remove(i);
					i--;
				}
			}
			if (answer.getPermissions().getPermission().size() == 0) {
				answer.setPermissions(null);
			}
		}

		// Intent-sinks
		if (answer.getIntentsinks() != null) {
			for (int i = 0; i < answer.getIntentsinks().getIntentsink().size(); i++) {
				final Reference ref1 = answer.getIntentsinks().getIntentsink().get(i).getReference();

				if (foundInFlows(answer.getFlows(), ref1)) {
					answer.getIntentsinks().getIntentsink().remove(i);
					i--;
				}
			}
			if (answer.getIntentsinks().getIntentsink().size() == 0) {
				answer.setIntentsinks(null);
			}
		}

		// Intent-sources
		if (answer.getIntentsources() != null) {
			for (int i = 0; i < answer.getIntentsources().getIntentsource().size(); i++) {
				final Reference ref1 = answer.getIntentsources().getIntentsource().get(i).getReference();

				if (foundInFlows(answer.getFlows(), ref1)) {
					answer.getIntentsources().getIntentsource().remove(i);
					i--;
				}
			}
			if (answer.getIntentsources().getIntentsource().size() == 0) {
				answer.setIntentsources(null);
			}
		}

		// Flows (set flag only)
		if (answer.getFlows() != null) {
			for (final Flow path1 : answer.getFlows().getFlow()) {
				if (path1.getAttributes() != null) {
					Attribute removeAttr = null;
					for (final Attribute attr : path1.getAttributes().getAttribute()) {
						if (attr.getName().equals("complete") && attr.getValue().equals("true")) {
							removeAttr = attr;
							break;
						}
					}
					if (removeAttr != null) {
						path1.getAttributes().getAttribute().remove(removeAttr);
					}
				}
			}

			for (final Flow path1 : answer.getFlows().getFlow()) {
				boolean complete = true;

				Reference path1From = null;
				Reference path1To = null;
				for (final Reference refPath1 : path1.getReference()) {
					if (refPath1.getType().equals(KeywordsAndConstantsHelper.REFERENCE_TYPE_FROM)) {
						path1From = refPath1;
					} else if (refPath1.getType().equals(KeywordsAndConstantsHelper.REFERENCE_TYPE_TO)) {
						path1To = refPath1;
					}
				}

				for (final Flow path2 : answer.getFlows().getFlow()) {
					if (path1 != path2) {
						Reference path2From = null;
						Reference path2To = null;
						for (final Reference refPath2 : path2.getReference()) {
							if (refPath2.getType().equals(KeywordsAndConstantsHelper.REFERENCE_TYPE_FROM)) {
								path2From = refPath2;
							} else if (refPath2.getType().equals(KeywordsAndConstantsHelper.REFERENCE_TYPE_TO)) {
								path2To = refPath2;
							}
						}

						if (EqualsHelper.equals(path1From, path2To) || EqualsHelper.equals(path1To, path2From)) {
							complete = false;
						}

					}
				}
				if (complete) {
					boolean add = true;
					if (path1.getAttributes() == null) {
						path1.setAttributes(new Attributes());
					} else {
						for (final Attribute attr : path1.getAttributes().getAttribute()) {
							if (attr.getName().equals("complete") && attr.getValue().equals("true")) {
								add = false;
								break;
							}
						}
					}

					if (add) {
						final Attribute completeAttr = new Attribute();
						completeAttr.setName("complete");
						completeAttr.setValue("true");
						path1.getAttributes().getAttribute().add(completeAttr);
					}
				}
			}
		}

		// Remove redundant items
		answer = Helper.removeRedundant(answer, equalsOptions);

		return answer;
	}

	/**
	 * Returns true if the given reference appears as a reference in the given flows.
	 *
	 * @param flows
	 *            The flows to be checked
	 * @param reference
	 *            The reference looked for
	 * @return true if reference was found in flows
	 */
	private boolean foundInFlows(Flows flows, Reference reference) {
		if (flows == null) {
			return true;
		}
		for (final Flow path : flows.getFlow()) {
			for (final Reference compareToReference : path.getReference()) {
				if (EqualsHelper.equals(reference, compareToReference)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Filters out or removes the given subjects of interest from the answer.
	 *
	 * @param answer
	 *            The answer to be filtered
	 * @param subjectOfInterest
	 *            The subject of interest to be filtered out (or be removed)
	 * @return The filtered answer
	 */
	public Answer filterOutSOI(Answer answer, int subjectOfInterest) {
		// Permissions
		if (subjectOfInterest == Question.QUESTION_TYPE_PERMISSIONS) {
			answer.setPermissions(null);
		}

		// Intents
		if (subjectOfInterest == Question.QUESTION_TYPE_INTENTS) {
			answer.setIntents(null);
		}

		// Intent-Filters
		if (subjectOfInterest == Question.QUESTION_TYPE_INTENTFILTER) {
			answer.setIntentfilters(null);
		}

		// Sources
		if (subjectOfInterest == Question.QUESTION_TYPE_SOURCES) {
			answer.setSources(null);
		}

		// Sinks
		if (subjectOfInterest == Question.QUESTION_TYPE_SINKS) {
			answer.setSinks(null);
		}

		// Intent-Sinks
		if (subjectOfInterest == Question.QUESTION_TYPE_INTENTSINKS) {
			answer.setIntentsinks(null);
		}

		// Intent-Sources
		if (subjectOfInterest == Question.QUESTION_TYPE_INTENTSOURCES) {
			answer.setIntentsources(null);
		}

		// Flows
		if (subjectOfInterest == Question.QUESTION_TYPE_FLOWS) {
			answer.setFlows(null);
		}

		return answer;
	}

	/**
	 * Filters out or removes all subjects of interest except the given one.
	 *
	 * @param answer
	 *            The answer to be filtered
	 * @param subjectOfInterest
	 *            The subject of interest to be kept
	 * @return The filtered answer
	 */
	public Answer filterToSOI(Answer answer, int subjectOfInterest) {
		// Permissions
		if (subjectOfInterest != Question.QUESTION_TYPE_PERMISSIONS) {
			answer.setPermissions(null);
		}

		// Intents
		if (subjectOfInterest != Question.QUESTION_TYPE_INTENTS) {
			answer.setIntents(null);
		}

		// Intent-Filters
		if (subjectOfInterest != Question.QUESTION_TYPE_INTENTFILTER) {
			answer.setIntentfilters(null);
		}

		// Sources
		if (subjectOfInterest != Question.QUESTION_TYPE_SOURCES) {
			answer.setSources(null);
		}

		// Sinks
		if (subjectOfInterest != Question.QUESTION_TYPE_SINKS) {
			answer.setSinks(null);
		}

		// Intent-Sinks
		if (subjectOfInterest != Question.QUESTION_TYPE_INTENTSINKS) {
			answer.setIntentsinks(null);
		}

		// Intent-Sources
		if (subjectOfInterest != Question.QUESTION_TYPE_INTENTSOURCES) {
			answer.setIntentsources(null);
		}

		// Flows
		if (subjectOfInterest != Question.QUESTION_TYPE_FLOWS) {
			answer.setFlows(null);
		}

		return answer;
	}

	/**
	 * Filter all elements. Only keep those where the key-value-pair is assigned as attribute.
	 *
	 * @param answer
	 *            The answer to be filtered
	 * @param key
	 *            The key to check
	 * @param value
	 *            The key's value
	 * @return The filtered answer
	 */
	public Answer filterByKeyValue(Answer answer, String key, String value) {
		return filterByKeyValue(answer, key, value, Question.QUESTION_TYPE_UNKNOWN);
	}

	/**
	 * Filter only the given subject of interest (subjectOfInterest) of the given answer. All other elements remain untouched. Only keep those where the key-value-pair is assigned as attribute.
	 *
	 * @param answer
	 *            The answer to be filtered
	 * @param key
	 *            The key to check
	 * @param value
	 *            The key's value
	 * @return The filtered answer
	 */
	public Answer filterByKeyValue(Answer answer, String key, String value, int subjectOfInterest) {
		// Permissions
		if ((subjectOfInterest == Question.QUESTION_TYPE_UNKNOWN
				|| subjectOfInterest == Question.QUESTION_TYPE_PERMISSIONS) && answer.getPermissions() != null) {
			for (int i = 0; i < answer.getPermissions().getPermission().size(); i++) {
				boolean keep = false;
				if (answer.getPermissions().getPermission().get(i).getAttributes() != null) {
					for (final Attribute attr : answer.getPermissions().getPermission().get(i).getAttributes()
							.getAttribute()) {
						if (attr.getName().equals(key) && attr.getValue().equals(value)) {
							keep = true;
							break;
						}
					}
				}

				if (!keep) {
					answer.getPermissions().getPermission().remove(i);
					i--;
				}
			}
			if (answer.getPermissions().getPermission().size() == 0) {
				answer.setPermissions(null);
			}
		}

		// Intents
		if ((subjectOfInterest == Question.QUESTION_TYPE_UNKNOWN || subjectOfInterest == Question.QUESTION_TYPE_INTENTS)
				&& answer.getIntents() != null) {
			for (int i = 0; i < answer.getIntents().getIntent().size(); i++) {
				boolean keep = false;
				if (answer.getIntents().getIntent().get(i).getAttributes() != null) {
					for (final Attribute attr : answer.getIntents().getIntent().get(i).getAttributes().getAttribute()) {
						if (attr.getName().equals(key) && attr.getValue().equals(value)) {
							keep = true;
							break;
						}
					}
				}

				if (!keep) {
					answer.getIntents().getIntent().remove(i);
					i--;
				}
			}
			if (answer.getIntents().getIntent().size() == 0) {
				answer.setIntents(null);
			}
		}

		// Intent-Filters
		if ((subjectOfInterest == Question.QUESTION_TYPE_UNKNOWN
				|| subjectOfInterest == Question.QUESTION_TYPE_INTENTFILTER) && answer.getIntentfilters() != null) {
			for (int i = 0; i < answer.getIntentfilters().getIntentfilter().size(); i++) {
				boolean keep = false;
				if (answer.getIntentfilters().getIntentfilter().get(i).getAttributes() != null) {
					for (final Attribute attr : answer.getIntentfilters().getIntentfilter().get(i).getAttributes()
							.getAttribute()) {
						if (attr.getName().equals(key) && attr.getValue().equals(value)) {
							keep = true;
							break;
						}
					}
				}

				if (!keep) {
					answer.getIntentfilters().getIntentfilter().remove(i);
					i--;
				}
			}
			if (answer.getIntentfilters().getIntentfilter().size() == 0) {
				answer.setIntentfilters(null);
			}
		}

		// Sources
		if ((subjectOfInterest == Question.QUESTION_TYPE_UNKNOWN || subjectOfInterest == Question.QUESTION_TYPE_SOURCES)
				&& answer.getSources() != null) {
			for (int i = 0; i < answer.getSources().getSource().size(); i++) {
				boolean keep = false;
				if (answer.getSources().getSource().get(i).getAttributes() != null) {
					for (final Attribute attr : answer.getSources().getSource().get(i).getAttributes().getAttribute()) {
						if (attr.getName().equals(key) && attr.getValue().equals(value)) {
							keep = true;
							break;
						}
					}
				}

				if (!keep) {
					answer.getSources().getSource().remove(i);
					i--;
				}
			}
			if (answer.getSources().getSource().size() == 0) {
				answer.setSources(null);
			}
		}

		// Sinks
		if ((subjectOfInterest == Question.QUESTION_TYPE_UNKNOWN || subjectOfInterest == Question.QUESTION_TYPE_SOURCES)
				&& answer.getSinks() != null) {
			for (int i = 0; i < answer.getSinks().getSink().size(); i++) {
				boolean keep = false;
				if (answer.getSinks().getSink().get(i).getAttributes() != null) {
					for (final Attribute attr : answer.getSinks().getSink().get(i).getAttributes().getAttribute()) {
						if (attr.getName().equals(key) && attr.getValue().equals(value)) {
							keep = true;
							break;
						}
					}
				}

				if (!keep) {
					answer.getSinks().getSink().remove(i);
					i--;
				}
			}
			if (answer.getSinks().getSink().size() == 0) {
				answer.setSinks(null);
			}
		}

		// Intent-Sinks
		if ((subjectOfInterest == Question.QUESTION_TYPE_UNKNOWN
				|| subjectOfInterest == Question.QUESTION_TYPE_INTENTSINKS) && answer.getIntentsinks() != null) {
			for (int i = 0; i < answer.getIntentsinks().getIntentsink().size(); i++) {
				boolean keep = false;
				if (answer.getIntentsinks().getIntentsink().get(i).getAttributes() != null) {
					for (final Attribute attr : answer.getIntentsinks().getIntentsink().get(i).getAttributes()
							.getAttribute()) {
						if (attr.getName().equals(key) && attr.getValue().equals(value)) {
							keep = true;
							break;
						}
					}
				}

				if (!keep) {
					answer.getIntentsinks().getIntentsink().remove(i);
					i--;
				}
			}
			if (answer.getIntentsinks().getIntentsink().size() == 0) {
				answer.setIntentsinks(null);
			}
		}

		// Intent-Sources
		if ((subjectOfInterest == Question.QUESTION_TYPE_UNKNOWN
				|| subjectOfInterest == Question.QUESTION_TYPE_INTENTSOURCES) && answer.getIntentsources() != null) {
			for (int i = 0; i < answer.getIntentsources().getIntentsource().size(); i++) {
				boolean keep = false;
				if (answer.getIntentsources().getIntentsource().get(i).getAttributes() != null) {
					for (final Attribute attr : answer.getIntentsources().getIntentsource().get(i).getAttributes()
							.getAttribute()) {
						if (attr.getName().equals(key) && attr.getValue().equals(value)) {
							keep = true;
							break;
						}
					}
				}

				if (!keep) {
					answer.getIntentsources().getIntentsource().remove(i);
					i--;
				}
			}
			if (answer.getIntentsources().getIntentsource().size() == 0) {
				answer.setIntentsources(null);
			}
		}

		// Flows
		if ((subjectOfInterest == Question.QUESTION_TYPE_UNKNOWN || subjectOfInterest == Question.QUESTION_TYPE_FLOWS)
				&& answer.getFlows() != null) {
			for (int i = 0; i < answer.getFlows().getFlow().size(); i++) {
				boolean keep = false;
				if (answer.getFlows().getFlow().get(i).getAttributes() != null) {
					for (final Attribute attr : answer.getFlows().getFlow().get(i).getAttributes().getAttribute()) {
						if (attr.getName().equals(key) && attr.getValue().equals(value)) {
							keep = true;
							break;
						}
					}
				}

				if (!keep) {
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

	/**
	 * Filters the given answer. Only keeps elements that refer to the provided reference.
	 *
	 * @param answer
	 *            The answer to be filtered
	 * @param reference
	 *            The reference to filter for
	 * @return The filtered answer
	 */
	public Answer filterByReference(Answer answer, Reference reference) {
		return filterByReference(answer, reference, Question.QUESTION_TYPE_UNKNOWN);
	}

	/**
	 * Filter only the given subject of interest (subjectOfInterest) of the given answer. All other elements remain untouched. Only keeps elements that refer to the provided reference.
	 *
	 * @param answer
	 *            The answer to be filtered
	 * @param reference
	 *            The reference to filter for
	 * @param subjectOfInterest
	 *            The subject of interest to be filtered
	 * @return The filtered answer
	 */
	public Answer filterByReference(Answer answer, Reference reference, int subjectOfInterest) {
		final EqualsOptions localOptions = equalsOptions.copy();
		localOptions.setOption(EqualsOptions.NULL_ALLOWED_ON_LEFT_HAND_SIDE, true);
		localOptions.setOption(EqualsOptions.PRECISELY_REFERENCE, false);

		// Permissions
		if ((subjectOfInterest == Question.QUESTION_TYPE_UNKNOWN
				|| subjectOfInterest == Question.QUESTION_TYPE_PERMISSIONS) && answer.getPermissions() != null) {
			for (int i = 0; i < answer.getPermissions().getPermission().size(); i++) {
				if (!EqualsHelper.equals(reference, answer.getPermissions().getPermission().get(i).getReference(),
						localOptions)) {
					answer.getPermissions().getPermission().remove(i);
					i--;
				}
			}
			if (answer.getPermissions().getPermission().size() == 0) {
				answer.setPermissions(null);
			}
		}

		// Sources
		if ((subjectOfInterest == Question.QUESTION_TYPE_UNKNOWN || subjectOfInterest == Question.QUESTION_TYPE_SOURCES)
				&& answer.getSources() != null) {
			for (int i = 0; i < answer.getSources().getSource().size(); i++) {
				if (!EqualsHelper.equals(reference, answer.getSources().getSource().get(i).getReference(),
						localOptions)) {
					answer.getSources().getSource().remove(i);
					i--;
				}
			}
			if (answer.getSources().getSource().size() == 0) {
				answer.setSources(null);
			}
		}

		// Sinks
		if ((subjectOfInterest == Question.QUESTION_TYPE_UNKNOWN || subjectOfInterest == Question.QUESTION_TYPE_SINKS)
				&& answer.getSinks() != null) {
			for (int i = 0; i < answer.getSinks().getSink().size(); i++) {
				if (!EqualsHelper.equals(reference, answer.getSinks().getSink().get(i).getReference(), localOptions)) {
					answer.getSinks().getSink().remove(i);
					i--;
				}
			}
			if (answer.getSinks().getSink().size() == 0) {
				answer.setSinks(null);
			}
		}

		// Intent-sinks
		if ((subjectOfInterest == Question.QUESTION_TYPE_UNKNOWN
				|| subjectOfInterest == Question.QUESTION_TYPE_INTENTSINKS) && answer.getIntentsinks() != null) {
			for (int i = 0; i < answer.getIntentsinks().getIntentsink().size(); i++) {
				if (!EqualsHelper.equals(reference, answer.getIntentsinks().getIntentsink().get(i).getReference(),
						localOptions)) {
					answer.getIntentsinks().getIntentsink().remove(i);
					i--;
				}
			}
			if (answer.getIntentsinks().getIntentsink().size() == 0) {
				answer.setIntentsinks(null);
			}
		}

		// Intent-sources
		if ((subjectOfInterest == Question.QUESTION_TYPE_UNKNOWN
				|| subjectOfInterest == Question.QUESTION_TYPE_INTENTSOURCES) && answer.getIntentsources() != null) {
			for (int i = 0; i < answer.getIntentsources().getIntentsource().size(); i++) {
				if (!EqualsHelper.equals(reference, answer.getIntentsources().getIntentsource().get(i).getReference(),
						localOptions)) {
					answer.getIntentsources().getIntentsource().remove(i);
					i--;
				}
			}
			if (answer.getIntentsources().getIntentsource().size() == 0) {
				answer.setIntentsources(null);
			}
		}

		// Flows
		if ((subjectOfInterest == Question.QUESTION_TYPE_UNKNOWN || subjectOfInterest == Question.QUESTION_TYPE_FLOWS)
				&& answer.getFlows() != null) {
			for (int i = 0; i < answer.getFlows().getFlow().size(); i++) {
				boolean remove = true;
				for (final Reference temp : answer.getFlows().getFlow().get(i).getReference()) {
					if (EqualsHelper.equals(reference, temp, localOptions)) {
						remove = false;
						break;
					}
				}
				if (remove) {
					answer.getFlows().getFlow().remove(i);
					i--;
				}
			}
			if (answer.getFlows().getFlow().size() == 0) {
				answer.setFlows(null);
			}
		}

		// Remove redundant items
		answer = Helper.removeRedundant(answer, equalsOptions);

		return answer;
	}
}