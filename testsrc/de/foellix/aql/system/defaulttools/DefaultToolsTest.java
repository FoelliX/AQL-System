package de.foellix.aql.system.defaulttools;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.helper.SootHelper;

public class DefaultToolsTest {
	private static final File DIRECTLEAK_APK = new File("examples/scenarios/DirectLeak1.apk");

	@Test
	@Tag("externalDownload")
	public void testAndroidJarDownload() {
		if (SootHelper.ANDROID_JAR_FORCED_FILE.exists()) {
			SootHelper.ANDROID_JAR_FORCED_FILE.delete();
		}
		final String temp = ConfigHandler.getInstance().getConfig().getAndroidPlatforms();
		ConfigHandler.getInstance().getConfig().setAndroidPlatforms("");
		assertTrue(SootHelper.getScene(DIRECTLEAK_APK).getApplicationClasses().toString()
				.contains("de.ecspride.MainActivity"));
		ConfigHandler.getInstance().getConfig().setAndroidPlatforms(temp);
		SootHelper.ANDROID_JAR_FORCED_FILE.delete();
	}
}