package de.foellix.aql.faketool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.foellix.aql.Log;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.helper.CLIHelper;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.helper.HashHelper;
import de.foellix.aql.system.AQLSystem;
import de.foellix.aql.system.BackupAndReset;

@Tag("requiresBuild")
@Tag("systemIsSetup")
public class FakeToolNewFeaturesTest {
	private static final File RESULT_DIRECTORY = new File("examples/faketool/results");

	private static AQLSystem aqlSystem;
	private static String startApp, endApp;

	@BeforeAll
	public static void setup() {
		Log.setLogLevel(Log.DEBUG_DETAILED);
		Log.setShorten(true);

		// Apps
		startApp = new File("examples/faketool/InterAppStart1.apk").getAbsolutePath();
		endApp = new File("examples/faketool/InterAppEnd1.apk").getAbsolutePath();

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
			final String query = "Sources IN App('" + startApp + "') ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(1, ((Answer) answer).getSources().getSource().size());
		assertNull(((Answer) answer).getSinks());
	}

	@Test
	public void test02() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "UNIFY [ Sources IN App('" + startApp + "') ?, Sinks IN App('" + startApp + "') ? ] ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(1, ((Answer) answer).getSources().getSource().size());
		assertEquals(1, ((Answer) answer).getSinks().getSink().size());
	}

	@Test
	public void test03() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "SourcesAndSinks IN App('" + startApp + "') ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(1, ((Answer) answer).getSources().getSource().size());
		assertEquals(1, ((Answer) answer).getSinks().getSink().size());
	}

	@Test
	public void test04() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "Flows IN App({ Slice FROM App('" + startApp + "') TO App('" + endApp + "') ! }) ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(2, ((Answer) answer).getFlows().getFlow().size());
		assertEquals(FakeTool.InterAppStart1Hash,
				HashHelper.getHash(
						((Answer) answer).getFlows().getFlow().get(0).getReference().get(0).getApp().getHashes(),
						HashHelper.HASH_TYPE_SHA256));
	}

	@Test
	public void test05() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "Flows IN App({ Slice FROM App('" + startApp + "') TO App('" + endApp
					+ "') ! }) WITH '%SourcesAndSinks%' = 'SorucesAndSinks.txt' ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(2, ((Answer) answer).getFlows().getFlow().size());
		assertEquals(FakeTool.InterAppStart1Hash,
				HashHelper.getHash(
						((Answer) answer).getFlows().getFlow().get(0).getReference().get(0).getApp().getHashes(),
						HashHelper.HASH_TYPE_SHA256));
	}

	@Test
	public void test06() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "FILTER [ UNIFY [ Flows IN App('" + startApp + "') ?, Sources IN App('" + startApp
					+ "') ? ] ? | 'converted' = { Arguments IN App('" + startApp + "') . } ] ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(2, ((Answer) answer).getFlows().getFlow().size());
		assertNull(((Answer) answer).getSources());
		assertNull(((Answer) answer).getSinks());
	}

	@Test
	public void test07() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "Flows IN App('" + startApp + "') WITH 'SourcesAndSinks' = 'SourcesAndSinks.txt' ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(2, ((Answer) answer).getFlows().getFlow().size());
	}

	@Test
	public void test08() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "Flows IN App('" + startApp
					+ "') WITH 'SourcesAndSinks' = { TOFD [ SourcesAndSinks IN App('" + startApp + "') ? ] ! } ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(2, ((Answer) answer).getFlows().getFlow().size());
	}

	@Test
	public void test09() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "Flows IN App({ Slice FROM App('" + startApp + "') TO App('" + endApp
					+ "') ! }) WITH 'SourcesAndSinks' = { TOFD [ SourcesAndSinks IN App('" + startApp + "') ? ] ! } ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(2, ((Answer) answer).getFlows().getFlow().size());
	}
}