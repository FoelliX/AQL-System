package de.foellix.aql.system.defaulttools;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.google.common.io.Files;

import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.helper.tools.APKSigner;
import de.foellix.aql.system.AQLSystem;
import de.foellix.aql.system.BackupAndReset;
import de.foellix.aql.system.defaulttools.operators.DefaultSignOperator;
import de.foellix.aql.system.task.OperatorTask;
import de.foellix.aql.system.task.OperatorTaskInfo;

public class DefaultSignOperatorTest {
	private static final File SIGN_TEST_APK = new File("data/temp/signTest.apk");
	private static final File DIRECTLEAK_APK = new File("examples/scenarios/DirectLeak1.apk");

	@Test
	public void testGetAndroidPlatformTools() {
		// Test 1
		System.out.println(ConfigHandler.getInstance().getAndroidBuildTools());

		// Test 2
		final String temp1 = ConfigHandler.getInstance().getConfig().getAndroidBuildTools();
		ConfigHandler.getInstance().getConfig().setAndroidBuildTools("/not/available");
		System.out.println(ConfigHandler.getInstance().getAndroidBuildTools());

		// Test 3
		final String temp2 = ConfigHandler.getInstance().getConfig().getAndroidPlatforms();
		ConfigHandler.getInstance().getConfig().setAndroidPlatforms("/not/available");
		System.out.println(ConfigHandler.getInstance().getAndroidBuildTools());

		// Reset
		ConfigHandler.getInstance().getConfig().setAndroidBuildTools(temp1);
		ConfigHandler.getInstance().getConfig().setAndroidPlatforms(temp2);
	}

	@Test
	public void testAPKSigner() {
		boolean noExceptions = true;

		if (new File("config.xml").exists()) {
			try {
				if (SIGN_TEST_APK.exists()) {
					SIGN_TEST_APK.delete();
				}
				Files.copy(DIRECTLEAK_APK, SIGN_TEST_APK);
				final APKSigner signer = new APKSigner(ConfigHandler.getInstance().getAndroidBuildTools());
				assertTrue(signer.sign(SIGN_TEST_APK));
				SIGN_TEST_APK.delete();
			} catch (final IOException e) {
				noExceptions = false;
			}
		}

		assertTrue(noExceptions);
	}

	@Test
	public void testRAW() {
		boolean noException = true;

		try {
			final DefaultSignOperator dso = new DefaultSignOperator();
			final OperatorTaskInfo taskinfo = new OperatorTaskInfo();
			taskinfo.setData(OperatorTaskInfo.ANSWERS, DIRECTLEAK_APK.getAbsolutePath());
			final OperatorTask task = new OperatorTask(null, taskinfo, dso);
			dso.applyOperator(task);
		} catch (final Exception e) {
			noException = false;
		}

		assertTrue(noException);
	}

	@Test
	public void testQuery() {
		boolean noException = true;

		try {
			BackupAndReset.reset();
			final AQLSystem system = new AQLSystem();
			final File apk = (File) system.queryAndWait("SIGN [ '" + DIRECTLEAK_APK.getAbsolutePath() + "' ! ] !")
					.iterator().next();
			assertTrue(apk.exists());
		} catch (final Exception e) {
			noException = false;
		}

		assertTrue(noException);
	}
}