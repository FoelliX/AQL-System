package de.foellix.aql.ui.cli;

import java.io.File;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.system.IAnswerAvailable;
import de.foellix.aql.system.task.Task;

public class AnswerToConsole implements IAnswerAvailable {
	@Override
	public void answerAvailable(Object answer, int status) {
		if (status >= Task.STATUS_EXECUTION_UNKNOWN) {
			if (answer instanceof Answer) {
				final Answer castedAnswer = (Answer) answer;

				int flows;
				int sources;
				int sinks;
				int intentfilters;
				int intents;
				int intentsinks;
				int intentsources;
				int permissions;

				try {
					flows = castedAnswer.getFlows().getFlow().size();
				} catch (final Exception e) {
					flows = 0;
				}
				try {
					sources = castedAnswer.getSources().getSource().size();
				} catch (final Exception e) {
					sources = 0;
				}
				try {
					sinks = castedAnswer.getSinks().getSink().size();
				} catch (final Exception e) {
					sinks = 0;
				}
				try {
					intentfilters = castedAnswer.getIntentfilters().getIntentfilter().size();
				} catch (final Exception e) {
					intentfilters = 0;
				}
				try {
					intents = castedAnswer.getIntents().getIntent().size();
				} catch (final Exception e) {
					intents = 0;
				}
				try {
					intentsinks = castedAnswer.getIntentsinks().getIntentsink().size();
				} catch (final Exception e) {
					intentsinks = 0;
				}
				try {
					intentsources = castedAnswer.getIntentsources().getIntentsource().size();
				} catch (final Exception e) {
					intentsources = 0;
				}
				try {
					permissions = castedAnswer.getPermissions().getPermission().size();
				} catch (final Exception e) {
					permissions = 0;
				}

				if ((flows + sources + sinks + intentfilters + intents + intentsinks + intentsources + permissions == 0)
						|| (!Log.getShorten() && (flows + sources + sinks + intentfilters + intents + intentsinks
								+ intentsources + permissions < 100))) {
					Log.msg("\n\n\n" + AnswerHandler.createXMLString(castedAnswer) + "\n\n\n", Log.NORMAL);
				} else {
					Log.msg("\nAQL-Answer:\n<answer>\n\t...\nShortened for the sake of clarity (Just showing numbers)\n\tFlows:"
							+ flows + "\n\tSources:" + sources + "\n\tSinks:" + sinks + "\n\tIntentFilters:"
							+ intentfilters + "\n\tIntents:" + intents + "\n\tIntentSinks:" + intentsinks
							+ "\n\tIntentSources:" + intentsources + "\n\tPermissions:" + permissions
							+ "\n\t...\n</answer>\n", Log.NORMAL);
				}
			} else if (answer instanceof File) {
				final File castedAnswer = (File) answer;
				Log.msg("\nFile-Answer: " + castedAnswer.getAbsolutePath() + "\n", Log.NORMAL);
			} else if (answer instanceof String) {
				final String castedAnswer = (String) answer;
				Log.msg("\nRaw-Answer: " + castedAnswer + "\n", Log.NORMAL);
			}
		}
	}
}
