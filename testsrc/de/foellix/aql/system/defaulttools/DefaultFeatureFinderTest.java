package de.foellix.aql.system.defaulttools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.foellix.aql.helper.SootHelper;
import de.foellix.aql.helper.tools.FeatureFinder;
import de.foellix.aql.system.AQLSystem;
import de.foellix.aql.system.BackupAndReset;
import de.foellix.aql.system.defaulttools.analysistools.DefaultFeatureFinder;
import de.foellix.aql.system.task.TaskAnswer;
import de.foellix.aql.system.task.ToolTask;
import de.foellix.aql.system.task.ToolTaskInfo;

public class DefaultFeatureFinderTest {
	private static final File DIRECTLEAK_APK = new File("examples/scenarios/DirectLeak1.apk");
	private static final File SIM_APP_APK = new File("examples/scenarios/simsms/SIMApp.apk");

	@Test
	public void testFeatureFinder01() {
		if (new File("config.xml").exists()) {
			final FeatureFinder ff = new FeatureFinder();

			// Part 1
			List<String> features = ff.getFeatures(DIRECTLEAK_APK);
			assertEquals("[]", features.toString());

			// Part 2
			SootHelper.setExcludes(new String[] {});
			features = ff.getFeatures(DIRECTLEAK_APK);
			SootHelper.resetExcludes();
			assertEquals("[GUI, IAC, ICC, Reflection]", features.toString());

			// Part 3
			features = ff.getFeatures(DIRECTLEAK_APK);
			assertEquals("[]", features.toString());
		}
	}

	@Test
	public void testFeatureFinder02() {
		if (new File("config.xml").exists()) {
			final FeatureFinder ff = new FeatureFinder();
			final List<String> features = ff.getFeatures(SIM_APP_APK);
			assertEquals("[IAC, ICC]", features.toString());
		}
	}

	@Test
	public void testRAW() {
		if (new File("config.xml").exists()) {
			boolean noException = true;

			try {
				final DefaultFeatureFinder dff = new DefaultFeatureFinder();
				final ToolTaskInfo taskinfo = new ToolTaskInfo();
				taskinfo.setData(ToolTaskInfo.APP_APK, SIM_APP_APK.getAbsolutePath());
				final ToolTask task = new ToolTask(null, taskinfo, dff);
				task.setTaskAnswer(new TaskAnswer(task, TaskAnswer.ANSWER_TYPE_RAW));
				final File resultFile = dff.applyAnalysisTool(task);
				final String result = Files.readAllLines(resultFile.toPath()).iterator().next();
				assertEquals("IAC, ICC", result);
			} catch (final Exception e) {
				noException = false;
			}

			assertTrue(noException);
		}
	}

	@Test
	public void testQuery() {
		if (new File("config.xml").exists()) {
			BackupAndReset.reset();
			final AQLSystem system = new AQLSystem();
			final String rawAnswer = (String) system.queryAndWait("Arguments IN App('" + SIM_APP_APK.getAbsolutePath()
					+ "') USING '" + DefaultTool.DEFAULT_TOOL_USES + "' .").iterator().next();
			assertEquals("IAC, ICC", rawAnswer);
		}
	}
}