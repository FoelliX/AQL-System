package de.foellix.aql.faketool;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.util.Collection;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.foellix.aql.Log;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.config.Tool;
import de.foellix.aql.helper.CLIHelper;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.system.AQLSystem;
import de.foellix.aql.system.BackupAndReset;
import de.foellix.aql.system.Options;

@Tag("requiresBuild")
@Tag("systemIsSetup")
public class TimeoutAndFailsTest {
	private static final File RESULT_DIRECTORY = new File("examples/faketool/results");

	private static AQLSystem aqlSystem;
	private static String startApp;

	@BeforeAll
	public static void setup() {
		Log.setLogLevel(Log.DEBUG_DETAILED);
		Log.setShorten(true);

		// Apps
		startApp = new File("examples/faketool/InterAppStart1.apk").getAbsolutePath();

		// Setup config
		CLIHelper.evaluateConfig("examples/faketool/config_faketool.xml");

		// Update FakeTool jar
		for (final Tool tool : ConfigHandler.getInstance().getConfig().getTools().getTool()) {
			tool.getExecute().setRun(FakeToolHelper.replaceFakeToolVariable(tool.getExecute().getRun()));
		}
		for (final Tool tool : ConfigHandler.getInstance().getConfig().getPreprocessors().getTool()) {
			tool.getExecute().setRun(FakeToolHelper.replaceFakeToolVariable(tool.getExecute().getRun()));
		}
		for (final Tool tool : ConfigHandler.getInstance().getConfig().getOperators().getTool()) {
			tool.getExecute().setRun(FakeToolHelper.replaceFakeToolVariable(tool.getExecute().getRun()));
		}
		for (final Tool tool : ConfigHandler.getInstance().getConfig().getConverters().getTool()) {
			tool.getExecute().setRun(FakeToolHelper.replaceFakeToolVariable(tool.getExecute().getRun()));
		}
		Log.msg(ConfigHandler.getInstance().toString(), Log.DEBUG_DETAILED);

		aqlSystem = new AQLSystem();
	}

	@BeforeEach
	public void reset() {
		// Reset results
		try {
			if (!(FileHelper.deleteDir(RESULT_DIRECTORY))) {
				throw new FileAlreadyExistsException(RESULT_DIRECTORY.getAbsolutePath() + " could not be deleted!");
			}
			RESULT_DIRECTORY.mkdir();
		} catch (final Exception e) {
			Log.error("Could not reset FakeTool's default result directory!" + Log.getExceptionAppendix(e));
		}

		// Reset Storage
		BackupAndReset.reset();
	}

	@AfterEach
	public void blank() {
		System.out.println();
	}

	@Test
	public void test01() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "Flows IN App('" + startApp + "') USES 'FakeTool7' ?";
			final Collection<Object> answers = aqlSystem.queryAndWait(query);
			if (answers != null) {
				answer = answers.iterator().next();
			}
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertNull(answer);
	}

	@Test
	public void test02() {
		aqlSystem.getOptions().setTimeoutMode(Options.TIMEOUT_MODE_OVERRIDE);
		aqlSystem.getOptions().setTimeout(CLIHelper.evaluateTimeout("5s"));

		Object answer = null;
		boolean noException = true;
		long time = -1;
		try {
			final String query = "Flows IN App('" + startApp + "') USES 'FakeTool6' ?";
			time = System.currentTimeMillis();
			final Collection<Object> answers = aqlSystem.queryAndWait(query);
			time = System.currentTimeMillis() - time;
			if (answers != null) {
				answer = answers.iterator().next();
			}
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertNull(answer);
		assertTrue(time > 4999 && time < 9999);
	}

	@Test
	public void test03() {
		aqlSystem.getOptions().setTimeoutMode(Options.TIMEOUT_MODE_MAX);
		aqlSystem.getOptions().setTimeout(CLIHelper.evaluateTimeout("5s"));

		Object answer = null;
		boolean noException = true;
		long time = -1;
		try {
			final String query = "Flows IN App('" + startApp + "') USES 'FakeTool6' ?";
			time = System.currentTimeMillis();
			final Collection<Object> answers = aqlSystem.queryAndWait(query);
			time = System.currentTimeMillis() - time;
			if (answers != null) {
				answer = answers.iterator().next();
			}
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertNull(answer);
		assertTrue(time > 9999 && time < 14999);
	}

	@Test
	public void test04() {
		aqlSystem.getOptions().setTimeoutMode(Options.TIMEOUT_MODE_MIN);
		aqlSystem.getOptions().setTimeout(CLIHelper.evaluateTimeout("5s"));

		Object answer = null;
		boolean noException = true;
		long time = -1;
		try {
			final String query = "Flows IN App('" + startApp + "') USES 'FakeTool6' ?";
			time = System.currentTimeMillis();
			final Collection<Object> answers = aqlSystem.queryAndWait(query);
			time = System.currentTimeMillis() - time;
			if (answers != null) {
				answer = answers.iterator().next();
			}
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertNull(answer);
		assertTrue(time > 4999 && time < 9999);
	}
}
