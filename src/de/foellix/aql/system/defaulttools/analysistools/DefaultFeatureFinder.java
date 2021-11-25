package de.foellix.aql.system.defaulttools.analysistools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.helper.tools.FeatureFinder;
import de.foellix.aql.system.task.ToolTask;
import de.foellix.aql.system.task.ToolTaskInfo;

public class DefaultFeatureFinder extends DefaultSootAnalysisTool {
	@Override
	public File applyAnalysisTool(ToolTask task) {
		// Find features
		List<String> features = new ArrayList<>();
		final String apk = task.getTaskInfo().getData(ToolTaskInfo.APP_APK);
		if (apk != null) {
			final File apkFile = new File(apk);
			if (apkFile.exists()) {
				final FeatureFinder fd = new FeatureFinder();
				features = fd.getFeatures(apkFile);
			}
		} else {
			Log.warning("Cannot run FeatureFinder - no APK specified!");
		}

		// Write to file
		final File answerFile = task.getTaskAnswer().getAnswerFile();
		try {
			if (features.isEmpty()) {
				answerFile.createNewFile();
			} else {
				try (final FileWriter fw = new FileWriter(answerFile)) {
					boolean first = true;
					for (final String feature : features) {
						if (!first) {
							fw.append(", ");
						} else {
							first = false;
						}
						fw.append(feature);
					}
				}
			}
		} catch (final IOException e) {
			Log.warning("Could not create answer file: " + answerFile.getAbsolutePath() + Log.getExceptionAppendix(e));
		}
		return answerFile;
	}
}