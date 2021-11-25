package de.foellix.aql.system.defaulttools.operators;

import java.io.File;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.Intentfilters;
import de.foellix.aql.datastructure.Intents;
import de.foellix.aql.datastructure.Intentsinks;
import de.foellix.aql.datastructure.Intentsources;
import de.foellix.aql.datastructure.Permissions;
import de.foellix.aql.datastructure.Sinks;
import de.foellix.aql.datastructure.Sources;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.OperatorTask;

public class DefaultUnifyOperator extends DefaultOperator {
	public DefaultUnifyOperator() {
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
				temp = unify(temp, childAnswer);
			}
		}
		return temp;
	}

	public Answer unify(final Answer answer, final Answer unifyWithAnswer) {
		Answer returnAnswer = new Answer();

		// Permissions
		if (answer.getPermissions() != null || unifyWithAnswer.getPermissions() != null) {
			final Permissions permissions = new Permissions();
			if (answer.getPermissions() != null) {
				permissions.getPermission().addAll(answer.getPermissions().getPermission());
			}
			if (unifyWithAnswer.getPermissions() != null) {
				permissions.getPermission().addAll(unifyWithAnswer.getPermissions().getPermission());
			}
			returnAnswer.setPermissions(permissions);
		}

		// Intents
		if (answer.getIntents() != null || unifyWithAnswer.getIntents() != null) {
			final Intents intents = new Intents();
			if (answer.getIntents() != null) {
				intents.getIntent().addAll(answer.getIntents().getIntent());
			}
			if (unifyWithAnswer.getIntents() != null) {
				intents.getIntent().addAll(unifyWithAnswer.getIntents().getIntent());
			}
			returnAnswer.setIntents(intents);
		}

		// Intent-filters
		if (answer.getIntentfilters() != null || unifyWithAnswer.getIntentfilters() != null) {
			final Intentfilters intentfilters = new Intentfilters();
			if (answer.getIntentfilters() != null) {
				intentfilters.getIntentfilter().addAll(answer.getIntentfilters().getIntentfilter());
			}
			if (unifyWithAnswer.getIntentfilters() != null) {
				intentfilters.getIntentfilter().addAll(unifyWithAnswer.getIntentfilters().getIntentfilter());
			}
			returnAnswer.setIntentfilters(intentfilters);
		}

		// Sources
		if (answer.getSources() != null || unifyWithAnswer.getSources() != null) {
			final Sources sources = new Sources();
			if (answer.getSources() != null) {
				sources.getSource().addAll(answer.getSources().getSource());
			}
			if (unifyWithAnswer.getSources() != null) {
				sources.getSource().addAll(unifyWithAnswer.getSources().getSource());
			}
			returnAnswer.setSources(sources);
		}

		// Sinks
		if (answer.getSinks() != null || unifyWithAnswer.getSinks() != null) {
			final Sinks sinks = new Sinks();
			if (answer.getSinks() != null) {
				sinks.getSink().addAll(answer.getSinks().getSink());
			}
			if (unifyWithAnswer.getSinks() != null) {
				sinks.getSink().addAll(unifyWithAnswer.getSinks().getSink());
			}
			returnAnswer.setSinks(sinks);
		}

		// Intent-sinks
		if (answer.getIntentsinks() != null || unifyWithAnswer.getIntentsinks() != null) {
			final Intentsinks intentsinks = new Intentsinks();
			if (answer.getIntentsinks() != null) {
				intentsinks.getIntentsink().addAll(answer.getIntentsinks().getIntentsink());
			}
			if (unifyWithAnswer.getIntentsinks() != null) {
				intentsinks.getIntentsink().addAll(unifyWithAnswer.getIntentsinks().getIntentsink());
			}
			returnAnswer.setIntentsinks(intentsinks);
		}

		// Intent-sources
		if (answer.getIntentsources() != null || unifyWithAnswer.getIntentsources() != null) {
			final Intentsources intentsources = new Intentsources();
			if (answer.getIntentsources() != null) {
				intentsources.getIntentsource().addAll(answer.getIntentsources().getIntentsource());
			}
			if (unifyWithAnswer.getIntentsources() != null) {
				intentsources.getIntentsource().addAll(unifyWithAnswer.getIntentsources().getIntentsource());
			}
			returnAnswer.setIntentsources(intentsources);
		}

		// Flow
		if (answer.getFlows() != null || unifyWithAnswer.getFlows() != null) {
			final Flows paths = new Flows();
			if (answer.getFlows() != null) {
				paths.getFlow().addAll(answer.getFlows().getFlow());
			}
			if (unifyWithAnswer.getFlows() != null) {
				paths.getFlow().addAll(unifyWithAnswer.getFlows().getFlow());
			}
			returnAnswer.setFlows(paths);
		}

		// Remove redundant items
		returnAnswer = Helper.removeRedundant(returnAnswer, equalsOptions);

		return returnAnswer;
	}
}