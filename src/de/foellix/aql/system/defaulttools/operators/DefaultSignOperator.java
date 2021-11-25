package de.foellix.aql.system.defaulttools.operators;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

import de.foellix.aql.Log;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.helper.tools.APKSigner;
import de.foellix.aql.system.task.OperatorTask;
import de.foellix.aql.system.task.OperatorTaskInfo;

public class DefaultSignOperator extends DefaultOperator {
	@Override
	public File applyOperator(OperatorTask task) {
		final File apk = new File(task.getTaskInfo().getData(OperatorTaskInfo.ANSWERS));
		if (apk.exists()) {
			final File signedApk = FileHelper.getTempFile(FileHelper.FILE_ENDING_APK);
			try {
				Files.copy(apk, signedApk);
				final File androidPlatformTools = ConfigHandler.getInstance().getAndroidBuildTools();
				if (androidPlatformTools.exists()) {
					final APKSigner signer = new APKSigner(androidPlatformTools);
					signer.sign(signedApk);
				} else {
					Log.warning("Could not find Android platform tools in \"" + androidPlatformTools.getAbsolutePath()
							+ "\". Continuing with unsigned APK!");
				}
				return signedApk;
			} catch (final IOException e) {
				Log.warning("Could not copy APK to be signed from \"" + apk.getAbsolutePath() + "\" to \"" + signedApk
						+ "\"." + Log.getExceptionAppendix(e));
			}
		} else {
			Log.warning("APK to be signed could not be found (" + apk.getAbsolutePath() + ")!");
		}
		return null;
	}
}