package de.foellix.aql.ui.cli;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.system.IAnswerAvailable;

public class AnswerToConsole implements IAnswerAvailable {
	@Override
	public void answerAvailable(Answer answer, int status) {
		int flows;
		int intentfilters;
		int intents;
		int intentsinks;
		int intentsources;
		int permissions;

		try {
			flows = answer.getFlows().getFlow().size();
		} catch (final Exception e) {
			flows = 0;
		}
		try {
			intentfilters = answer.getIntentfilters().getIntentfilter().size();
		} catch (final Exception e) {
			intentfilters = 0;
		}
		try {
			intents = answer.getIntents().getIntent().size();
		} catch (final Exception e) {
			intents = 0;
		}
		try {
			intentsinks = answer.getIntentsinks().getIntentsink().size();
		} catch (final Exception e) {
			intentsinks = 0;
		}
		try {
			intentsources = answer.getIntentsources().getIntentsource().size();
		} catch (final Exception e) {
			intentsources = 0;
		}
		try {
			permissions = answer.getPermissions().getPermission().size();
		} catch (final Exception e) {
			permissions = 0;
		}

		if ((flows + intentfilters + intents + intentsinks + intentsources + permissions == 0) || (!Log.getShorten()
				&& (flows + intentfilters + intents + intentsinks + intentsources + permissions < 100))) {
			Log.msg("\n\n\n" + AnswerHandler.createXMLString(answer) + "\n\n\n", Log.NORMAL);
		} else {
			Log.msg("\n<answer>\n\t...\nShortened for the sake of clarity (Just showing numbers)\n\tFlows:" + flows
					+ "\n\tIntentFilters:" + intentfilters + "\n\tIntents:" + intents + "\n\tIntentSinks:" + intentsinks
					+ "\n\tIntentSources:" + intentsources + "\n\tPermissions:" + permissions + "\n\t...\n</answer>\n",
					Log.NORMAL);
		}
	}
}
