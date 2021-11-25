package de.foellix.aql.system.defaulttools.analysistools;

import java.io.File;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Sinks;
import de.foellix.aql.datastructure.Sources;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.tools.SourceSinkFinder;
import de.foellix.aql.system.task.ToolTask;
import de.foellix.aql.system.task.ToolTaskInfo;

public class DefaultSourceSinkFinder extends DefaultSootAnalysisTool {
	@Override
	public File applyAnalysisTool(ToolTask task) {
		// Find Sources and Sinks
		Sources sources = null;
		Sinks sinks = null;
		final String apk = task.getTaskInfo().getData(ToolTaskInfo.APP_APK);
		if (apk != null) {
			final File apkFile = new File(apk);
			if (apkFile.exists()) {
				final SourceSinkFinder ssf;
				if (task.getTaskInfo().getAllSetVariableNames().contains("%SourcesAndSinks%")) {
					ssf = new SourceSinkFinder(apkFile, new File(task.getTaskInfo().getData("%SourcesAndSinks%")));
				} else {
					ssf = new SourceSinkFinder(apkFile);
				}
				sources = ssf.getSources();
				sinks = ssf.getSinks();
			}
		} else {
			Log.warning("Cannot run IntentInformationFinder - no APK specified!");
		}

		// Create Answer
		final File answerFile = task.getTaskAnswer().getAnswerFile();
		final Answer answer = new Answer();
		if (sources != null) {
			answer.setSources(sources);
		}
		if (sinks != null) {
			answer.setSinks(sinks);
		}
		AnswerHandler.createXML(answer, answerFile);
		return answerFile;
	}
}