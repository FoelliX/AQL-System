package de.foellix.aql.system.defaulttools.analysistools;

import java.io.File;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Permissions;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.ManifestHelper;
import de.foellix.aql.helper.ManifestInfo;
import de.foellix.aql.system.task.ToolTask;
import de.foellix.aql.system.task.ToolTaskInfo;

public class DefaultPermissionFinder extends DefaultAnalysisTool {
	@Override
	public File applyAnalysisTool(ToolTask task) {
		// Find Permissions
		Permissions permissions = null;
		final String apk = task.getTaskInfo().getData(ToolTaskInfo.APP_APK);
		if (apk != null) {
			final File apkFile = new File(apk);
			if (apkFile.exists()) {
				final ManifestInfo manifestInfo = ManifestHelper.getInstance().getManifest(apkFile);
				permissions = manifestInfo.getPermissions();
			}
		} else {
			Log.warning("Cannot run PermissionFinder - no APK specified!");
		}

		// Create Answer
		final File answerFile = task.getTaskAnswer().getAnswerFile();
		final Answer answer = new Answer();
		if (permissions != null) {
			answer.setPermissions(permissions);
		}
		AnswerHandler.createXML(answer, answerFile);
		return answerFile;
	}
}