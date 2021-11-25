package de.foellix.aql.system.defaulttools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Permissions;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.helper.CLIHelper;
import de.foellix.aql.helper.ManifestHelper;
import de.foellix.aql.helper.ManifestInfo;
import de.foellix.aql.system.AQLSystem;
import de.foellix.aql.system.BackupAndReset;
import de.foellix.aql.system.defaulttools.analysistools.DefaultPermissionFinder;
import de.foellix.aql.system.task.TaskAnswer;
import de.foellix.aql.system.task.ToolTask;
import de.foellix.aql.system.task.ToolTaskInfo;

public class DefaultPermissionFinderTest {
	private static final File DIRECTLEAK_APK = new File("examples/scenarios/DirectLeak1.apk");

	@BeforeAll
	public static void setup() {
		CLIHelper.evaluateConfig("examples/no_config.xml");
	}

	@AfterAll
	public static void reset() {
		CLIHelper.evaluateConfig("config.xml");
	}

	@Test
	public void testPermissionsFinder() {
		final ManifestInfo manifestInfo = ManifestHelper.getInstance().getManifest(DIRECTLEAK_APK);
		final Permissions permissions = manifestInfo.getPermissions();
		assertEquals(permissions.getPermission().get(0).getName(), "android.permission.SEND_SMS");
		assertEquals(permissions.getPermission().get(1).getName(), "android.permission.READ_PHONE_STATE");
	}

	@Test
	public void testRAW() {
		boolean noException = true;

		try {
			final DefaultPermissionFinder dpf = new DefaultPermissionFinder();
			final ToolTaskInfo taskinfo = new ToolTaskInfo();
			taskinfo.setData(ToolTaskInfo.APP_APK, DIRECTLEAK_APK.getAbsolutePath());
			final ToolTask task = new ToolTask(null, taskinfo, dpf);
			task.setTaskAnswer(new TaskAnswer(task, TaskAnswer.ANSWER_TYPE_AQL, Question.QUESTION_TYPE_PERMISSIONS));
			final File resultFile = dpf.applyAnalysisTool(task);
			final Permissions permissions = AnswerHandler.parseXML(resultFile).getPermissions();
			assertEquals(permissions.getPermission().get(0).getName(), "android.permission.SEND_SMS");
			assertEquals(permissions.getPermission().get(1).getName(), "android.permission.READ_PHONE_STATE");
		} catch (final Exception e) {
			noException = false;
		}

		assertTrue(noException);
	}

	@Test
	public void testQuery01() {
		BackupAndReset.reset();
		final AQLSystem system = new AQLSystem();
		final Permissions permissions = ((Answer) system.queryAndWait("Permissions IN App('"
				+ DIRECTLEAK_APK.getAbsolutePath() + "') USING '" + DefaultTool.DEFAULT_TOOL_USES + "' ?").iterator()
				.next()).getPermissions();
		assertEquals(permissions.getPermission().get(0).getName(), "android.permission.SEND_SMS");
		assertEquals(permissions.getPermission().get(1).getName(), "android.permission.READ_PHONE_STATE");
	}

	// Deactivated since it requires PANDA or similar tool to be installed
	// @Test
	// public void testQuery02() {
	// BackupAndReset.reset();
	// final AQLSystem system = new AQLSystem();
	// final Permissions permissions = ((Answer) system
	// .queryAndWait("Permissions IN App('" + DIRECTLEAK_APK.getAbsolutePath() + "') ?").iterator().next())
	// .getPermissions();
	// assertEquals(permissions.getPermission().get(0).getName(), "android.permission.SEND_SMS");
	// assertEquals(permissions.getPermission().get(1).getName(), "android.permission.WRITE_SMS");
	// }
}