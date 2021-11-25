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

public class DefaultMinusOperator extends DefaultOperator {
	public DefaultMinusOperator() {
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
				temp = minus(temp, childAnswer);
			}
		}
		return temp;
	}

	public Answer minus(final Answer answer, final Answer minusAnswer) {
		final Answer setminus = new Answer();

		// Permissions
		if (answer.getPermissions() != null && !answer.getPermissions().getPermission().isEmpty()) {
			setminus.setPermissions(new Permissions());
			setminus.getPermissions().getPermission().addAll(answer.getPermissions().getPermission());
			if (minusAnswer.getPermissions() != null && !minusAnswer.getPermissions().getPermission().isEmpty()) {
				for (final Permission candidate : answer.getPermissions().getPermission()) {
					for (final Permission needle : minusAnswer.getPermissions().getPermission()) {
						if (EqualsHelper.equals(needle, candidate, equalsOptions)) {
							setminus.getPermissions().getPermission().remove(candidate);
							break;
						}
					}
				}
			}
		}

		// Intents
		if (answer.getIntents() != null && !answer.getIntents().getIntent().isEmpty()) {
			setminus.setIntents(new Intents());
			setminus.getIntents().getIntent().addAll(answer.getIntents().getIntent());
			if (minusAnswer.getIntents() != null && !minusAnswer.getIntents().getIntent().isEmpty()) {
				for (final Intent candidate : answer.getIntents().getIntent()) {
					for (final Intent needle : minusAnswer.getIntents().getIntent()) {
						if (EqualsHelper.equals(needle, candidate, equalsOptions)) {
							setminus.getIntents().getIntent().remove(candidate);
							break;
						}
					}
				}
			}
		}

		// Intent-Filters
		if (answer.getIntentfilters() != null && !answer.getIntentfilters().getIntentfilter().isEmpty()) {
			setminus.setIntentfilters(new Intentfilters());
			setminus.getIntentfilters().getIntentfilter().addAll(answer.getIntentfilters().getIntentfilter());
			if (minusAnswer.getIntentfilters() != null && !minusAnswer.getIntentfilters().getIntentfilter().isEmpty()) {
				for (final Intentfilter candidate : answer.getIntentfilters().getIntentfilter()) {
					for (final Intentfilter needle : minusAnswer.getIntentfilters().getIntentfilter()) {
						if (EqualsHelper.equals(needle, candidate, equalsOptions)) {
							setminus.getIntentfilters().getIntentfilter().remove(candidate);
							break;
						}
					}
				}
			}
		}

		// Sources
		if (answer.getSources() != null && !answer.getSources().getSource().isEmpty()) {
			setminus.setSources(new Sources());
			setminus.getSources().getSource().addAll(answer.getSources().getSource());
			if (minusAnswer.getSources() != null && !minusAnswer.getSources().getSource().isEmpty()) {
				for (final Source candidate : answer.getSources().getSource()) {
					for (final Source needle : minusAnswer.getSources().getSource()) {
						if (EqualsHelper.equals(needle, candidate, equalsOptions)) {
							setminus.getSources().getSource().remove(candidate);
							break;
						}
					}
				}
			}
		}

		// Sinks
		if (answer.getSinks() != null && !answer.getSinks().getSink().isEmpty()) {
			setminus.setSinks(new Sinks());
			setminus.getSinks().getSink().addAll(answer.getSinks().getSink());
			if (minusAnswer.getSinks() != null && !minusAnswer.getSinks().getSink().isEmpty()) {
				for (final Sink candidate : answer.getSinks().getSink()) {
					for (final Sink needle : minusAnswer.getSinks().getSink()) {
						if (EqualsHelper.equals(needle, candidate, equalsOptions)) {
							setminus.getSinks().getSink().remove(candidate);
							break;
						}
					}
				}
			}
		}

		// Intent-sinks
		if (answer.getIntentsinks() != null && !answer.getIntentsinks().getIntentsink().isEmpty()) {
			setminus.setIntentsinks(new Intentsinks());
			setminus.getIntentsinks().getIntentsink().addAll(answer.getIntentsinks().getIntentsink());
			if (minusAnswer.getIntentsinks() != null && !minusAnswer.getIntentsinks().getIntentsink().isEmpty()) {
				for (final Intentsink candidate : answer.getIntentsinks().getIntentsink()) {
					for (final Intentsink needle : minusAnswer.getIntentsinks().getIntentsink()) {
						if (EqualsHelper.equals(needle, candidate, equalsOptions)) {
							setminus.getIntentsinks().getIntentsink().remove(candidate);
							break;
						}
					}
				}
			}
		}

		// Intent-sources
		if (answer.getIntentsources() != null && !answer.getIntentsources().getIntentsource().isEmpty()) {
			setminus.setIntentsources(new Intentsources());
			setminus.getIntentsources().getIntentsource().addAll(answer.getIntentsources().getIntentsource());
			if (minusAnswer.getIntentsources() != null && !minusAnswer.getIntentsources().getIntentsource().isEmpty()) {
				for (final Intentsource candidate : answer.getIntentsources().getIntentsource()) {
					for (final Intentsource needle : minusAnswer.getIntentsources().getIntentsource()) {
						if (EqualsHelper.equals(needle, candidate, equalsOptions)) {
							setminus.getIntentsources().getIntentsource().remove(candidate);
							break;
						}
					}
				}
			}
		}

		// Flows
		if (answer.getFlows() != null && !answer.getFlows().getFlow().isEmpty()) {
			setminus.setFlows(new Flows());
			setminus.getFlows().getFlow().addAll(answer.getFlows().getFlow());
			if (minusAnswer.getFlows() != null && !minusAnswer.getFlows().getFlow().isEmpty()) {
				for (final Flow candidate : answer.getFlows().getFlow()) {
					for (final Flow needle : minusAnswer.getFlows().getFlow()) {
						if (EqualsHelper.equals(needle, candidate, equalsOptions)) {
							setminus.getFlows().getFlow().remove(candidate);
							break;
						}
					}
				}
			}
		}

		return setminus;
	}
}