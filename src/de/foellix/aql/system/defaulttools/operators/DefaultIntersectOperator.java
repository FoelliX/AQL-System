package de.foellix.aql.system.defaulttools.operators;

import java.io.File;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.Intent;
import de.foellix.aql.datastructure.Intentfilter;
import de.foellix.aql.datastructure.Intentfilters;
import de.foellix.aql.datastructure.Intents;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsinks;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.Intentsources;
import de.foellix.aql.datastructure.Permission;
import de.foellix.aql.datastructure.Permissions;
import de.foellix.aql.datastructure.Sink;
import de.foellix.aql.datastructure.Sinks;
import de.foellix.aql.datastructure.Source;
import de.foellix.aql.datastructure.Sources;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.EqualsHelper;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.system.task.OperatorTask;

public class DefaultIntersectOperator extends DefaultOperator {
	public DefaultIntersectOperator() {
		super();
	}

	@Override
	public File applyOperator(OperatorTask task) {
		final File answerFile = FileHelper.getTempFile(FileHelper.FILE_ENDING_XML);
		AnswerHandler.createXML(applyOperatorInner(task), answerFile);
		return answerFile;
	}

	public Answer applyOperatorInner(OperatorTask task) {
		Answer temp = null;
		for (final Answer childAnswer : task.getAvailableAnswersOfChildren()) {
			if (temp == null) {
				temp = childAnswer;
			} else {
				temp = intersect(temp, childAnswer);
			}
		}
		return temp;
	}

	public Answer intersect(final Answer answer, final Answer intersectWithAnswer) {
		final Answer intersection = new Answer();

		// Permissions
		intersection.setPermissions(new Permissions());
		if ((answer.getPermissions() != null && !answer.getPermissions().getPermission().isEmpty())
				&& (intersectWithAnswer.getPermissions() != null
						&& !intersectWithAnswer.getPermissions().getPermission().isEmpty())) {
			for (final Permission candidate : answer.getPermissions().getPermission()) {
				for (final Permission needle : intersectWithAnswer.getPermissions().getPermission()) {
					if (EqualsHelper.equals(needle, candidate, equalsOptions)) {
						intersection.getPermissions().getPermission().add(candidate);
						break;
					}
				}
			}
		}
		if (intersection.getPermissions().getPermission().isEmpty()) {
			intersection.setPermissions(null);
		}

		// Intents
		intersection.setIntents(new Intents());
		if ((answer.getIntents() != null && !answer.getIntents().getIntent().isEmpty())
				&& (intersectWithAnswer.getIntents() != null
						&& !intersectWithAnswer.getIntents().getIntent().isEmpty())) {
			for (final Intent candidate : answer.getIntents().getIntent()) {
				for (final Intent needle : intersectWithAnswer.getIntents().getIntent()) {
					if (EqualsHelper.equals(needle, candidate, equalsOptions)) {
						intersection.getIntents().getIntent().add(candidate);
						break;
					}
				}
			}
		}
		if (intersection.getIntents().getIntent().isEmpty()) {
			intersection.setIntents(null);
		}

		// Intent-Filters
		intersection.setIntentfilters(new Intentfilters());
		if ((answer.getIntentfilters() != null && !answer.getIntentfilters().getIntentfilter().isEmpty())
				&& (intersectWithAnswer.getIntentfilters() != null
						&& !intersectWithAnswer.getIntentfilters().getIntentfilter().isEmpty())) {
			for (final Intentfilter candidate : answer.getIntentfilters().getIntentfilter()) {
				for (final Intentfilter needle : intersectWithAnswer.getIntentfilters().getIntentfilter()) {
					if (EqualsHelper.equals(needle, candidate, equalsOptions)) {
						intersection.getIntentfilters().getIntentfilter().add(candidate);
						break;
					}
				}
			}
		}
		if (intersection.getIntentfilters().getIntentfilter().isEmpty()) {
			intersection.setIntentfilters(null);
		}

		// Sources
		intersection.setSources(new Sources());
		if ((answer.getSources() != null && !answer.getSources().getSource().isEmpty())
				&& (intersectWithAnswer.getSources() != null
						&& !intersectWithAnswer.getSources().getSource().isEmpty())) {
			for (final Source candidate : answer.getSources().getSource()) {
				for (final Source needle : intersectWithAnswer.getSources().getSource()) {
					if (EqualsHelper.equals(needle, candidate, equalsOptions)) {
						intersection.getSources().getSource().add(candidate);
						break;
					}
				}
			}
		}
		if (intersection.getSources().getSource().isEmpty()) {
			intersection.setSources(null);
		}

		// Sinks
		intersection.setSinks(new Sinks());
		if ((answer.getSinks() != null && !answer.getSinks().getSink().isEmpty())
				&& (intersectWithAnswer.getSinks() != null && !intersectWithAnswer.getSinks().getSink().isEmpty())) {
			for (final Sink candidate : answer.getSinks().getSink()) {
				for (final Sink needle : intersectWithAnswer.getSinks().getSink()) {
					if (EqualsHelper.equals(needle, candidate, equalsOptions)) {
						intersection.getSinks().getSink().add(candidate);
						break;
					}
				}
			}
		}
		if (intersection.getSinks().getSink().isEmpty()) {
			intersection.setSinks(null);
		}

		// Intent-sinks
		intersection.setIntentsinks(new Intentsinks());
		if ((answer.getIntentsinks() != null && !answer.getIntentsinks().getIntentsink().isEmpty())
				&& (intersectWithAnswer.getIntentsinks() != null
						&& !intersectWithAnswer.getIntentsinks().getIntentsink().isEmpty())) {
			for (final Intentsink candidate : answer.getIntentsinks().getIntentsink()) {
				for (final Intentsink needle : intersectWithAnswer.getIntentsinks().getIntentsink()) {
					if (EqualsHelper.equals(needle, candidate, equalsOptions)) {
						intersection.getIntentsinks().getIntentsink().add(candidate);
						break;
					}
				}
			}
		}
		if (intersection.getIntentsinks().getIntentsink().isEmpty()) {
			intersection.setIntentsinks(null);
		}

		// Intent-sources
		intersection.setIntentsources(new Intentsources());
		if ((answer.getIntentsources() != null && !answer.getIntentsources().getIntentsource().isEmpty())
				&& (intersectWithAnswer.getIntentsources() != null
						&& !intersectWithAnswer.getIntentsources().getIntentsource().isEmpty())) {
			for (final Intentsource candidate : answer.getIntentsources().getIntentsource()) {
				for (final Intentsource needle : intersectWithAnswer.getIntentsources().getIntentsource()) {
					if (EqualsHelper.equals(needle, candidate, equalsOptions)) {
						intersection.getIntentsources().getIntentsource().add(candidate);
						break;
					}
				}
			}
		}
		if (intersection.getIntentsources().getIntentsource().isEmpty()) {
			intersection.setIntentsources(null);
		}

		// Flows
		intersection.setFlows(new Flows());
		if ((answer.getFlows() != null && !answer.getFlows().getFlow().isEmpty())
				&& (intersectWithAnswer.getFlows() != null && !intersectWithAnswer.getFlows().getFlow().isEmpty())) {
			for (final Flow candidate : answer.getFlows().getFlow()) {
				for (final Flow needle : intersectWithAnswer.getFlows().getFlow()) {
					if (EqualsHelper.equals(needle, candidate, equalsOptions)) {
						intersection.getFlows().getFlow().add(candidate);
						break;
					}
				}
			}
		}
		if (intersection.getFlows().getFlow().isEmpty()) {
			intersection.setFlows(null);
		}

		return intersection;
	}
}