package de.foellix.aql.system.defaulttools.analysistools;

import java.io.File;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Intentfilters;
import de.foellix.aql.datastructure.Intents;
import de.foellix.aql.datastructure.Intentsinks;
import de.foellix.aql.datastructure.Intentsources;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.tools.IntentInformationFinder;
import de.foellix.aql.system.task.ToolTask;
import de.foellix.aql.system.task.ToolTaskInfo;

public class DefaultIntentInformationFinder extends DefaultSootAnalysisTool {
	@Override
	public File applyAnalysisTool(ToolTask task) {
		// Find Intent information
		Intents intents = null;
		Intentfilters intentfilters = null;
		Intentsinks intentsinks = null;
		Intentsources intentsources = null;
		final String apk = task.getTaskInfo().getData(ToolTaskInfo.APP_APK);
		if (apk != null) {
			final File apkFile = new File(apk);
			if (apkFile.exists()) {
				final IntentInformationFinder iif = new IntentInformationFinder(apkFile);
				intents = iif.getIntents();
				intentfilters = iif.getIntentfilters();
				intentsinks = iif.getIntentsinks();
				intentsources = iif.getIntentsources();
			}
		} else {
			Log.warning("Cannot run IntentInformationFinder - no APK specified!");
		}

		// Create Answer
		final File answerFile = task.getTaskAnswer().getAnswerFile();
		final Answer answer = new Answer();
		if (intents != null) {
			answer.setIntents(intents);
		}
		if (intentfilters != null) {
			answer.setIntentfilters(intentfilters);
		}
		if (intentsinks != null) {
			answer.setIntentsinks(intentsinks);
		}
		if (intentsources != null) {
			answer.setIntentsources(intentsources);
		}
		AnswerHandler.createXML(answer, answerFile);
		return answerFile;
	}
}