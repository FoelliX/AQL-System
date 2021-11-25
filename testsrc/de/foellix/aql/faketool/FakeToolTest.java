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
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.CLIHelper;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.helper.HashHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.AQLSystem;
import de.foellix.aql.system.BackupAndReset;

@Tag("requiresBuild")
@Tag("systemIsSetup")
public class FakeToolTest {
	private static final File RESULT_DIRECTORY = new File("examples/faketool/results");

	private static AQLSystem aqlSystem;
	private static String startApp, endApp;

	@BeforeAll
	public static void setup() {
		Log.setLogLevel(Log.DEBUG_DETAILED);
		// Log.setShorten(true);

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
		Object answer1 = null;
		Object answer2 = null;
		boolean noException = true;
		try {
			final String query = "Flows IN App('" + startApp + "') ?";
			answer1 = aqlSystem.queryAndWait(query).iterator().next();

			// Test loading the answer
			System.out.println();
			answer2 = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(2, ((Answer) answer1).getFlows().getFlow().size());
		assertEquals(2, ((Answer) answer2).getFlows().getFlow().size());
	}

	@Test
	public void test02() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "Flows IN App('" + endApp + "') ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(2, ((Answer) answer).getFlows().getFlow().size());
	}

	@Test
	public void test03() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "IntentSinks IN App('" + startApp + "') ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(1, ((Answer) answer).getIntentsinks().getIntentsink().size());
	}

	@Test
	public void test04() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "IntentSources IN App('" + endApp + "') ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(1, ((Answer) answer).getIntentsources().getIntentsource().size());
	}

	@Test
	public void test05() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "Flows IN App('" + startApp + "' | 'FAKE') ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();

		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(2, ((Answer) answer).getFlows().getFlow().size());
		for (final Flow flow : ((Answer) answer).getFlows().getFlow()) {
			for (final Reference ref : flow.getReference()) {
				assertEquals(HashHelper.getHash(ref.getApp().getHashes(), HashHelper.HASH_TYPE_SHA256),
						FakeTool.InterAppStart1_preprocessedHash);
			}
		}
	}

	@Test
	public void test06() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "Flows IN App('" + endApp + "' | 'FAKE') ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();

		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(2, ((Answer) answer).getFlows().getFlow().size());
		for (final Flow flow : ((Answer) answer).getFlows().getFlow()) {
			for (final Reference ref : flow.getReference()) {
				assertEquals(HashHelper.getHash(ref.getApp().getHashes(), HashHelper.HASH_TYPE_SHA256),
						FakeTool.InterAppEnd1_preprocessedHash);
			}
		}
	}

	@Test
	public void test07() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "CONNECT [ IntentSinks IN App('" + startApp + "') ?, IntentSources IN App('" + endApp
					+ "') ? ] ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(1, ((Answer) answer).getIntentsinks().getIntentsink().size());
		assertEquals(1, ((Answer) answer).getIntentsources().getIntentsource().size());

		// Check flow
		assertEquals(1, ((Answer) answer).getFlows().getFlow().size());
		final Flow flow = ((Answer) answer).getFlows().getFlow().get(0);
		assertTrue(HashHelper.getHash(Helper.getFrom(flow).getApp().getHashes(), HashHelper.HASH_TYPE_SHA256)
				.equals(FakeTool.InterAppStart1Hash));
		assertTrue(HashHelper.getHash(Helper.getTo(flow).getApp().getHashes(), HashHelper.HASH_TYPE_SHA256)
				.equals(FakeTool.InterAppEnd1Hash));
	}

	@Test
	public void test08() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "UNIFY [ Flows IN App('" + startApp + "') ?, Flows IN App('" + endApp + "') ? ] ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(4, ((Answer) answer).getFlows().getFlow().size());
	}

	@Test
	public void test09() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "UNIFY [ Flows IN App('" + startApp + "') ?, Flows IN App('" + endApp
					+ "') ?, MATCH [ IntentSinks IN App('" + startApp + "') ?, IntentSources IN App('" + endApp
					+ "') ? ] ? ] ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(5, ((Answer) answer).getFlows().getFlow().size());
		assertEquals(1, ((Answer) answer).getIntentsinks().getIntentsink().size());
		assertEquals(1, ((Answer) answer).getIntentsources().getIntentsource().size());
	}

	@Test
	public void test10() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "CONNECT [ Flows IN App('" + startApp + "') ?, Flows IN App('" + endApp
					+ "') ?, MATCH [ IntentSinks IN App('" + startApp + "') ?, IntentSources IN App('" + endApp
					+ "') ? ] ? ] ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(6, ((Answer) answer).getFlows().getFlow().size());
		assertEquals(1, ((Answer) answer).getIntentsinks().getIntentsink().size());
		assertEquals(1, ((Answer) answer).getIntentsources().getIntentsource().size());
	}

	@Test
	public void test11() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "MATCH [ IntentSinks IN App('" + startApp + "') ?, IntentSources IN App('" + endApp
					+ "') ? ] ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(1, ((Answer) answer).getFlows().getFlow().size());
		assertEquals(1, ((Answer) answer).getIntentsinks().getIntentsink().size());
		assertEquals(1, ((Answer) answer).getIntentsources().getIntentsource().size());
	}

	@Test
	public void test12() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "FILTER [ Flows IN App('" + startApp + "') ? ] ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(2, ((Answer) answer).getFlows().getFlow().size());
	}

	@Test
	public void test13() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "FILTER [ FILTER [ FILTER [ FILTER [ FILTER [ Flows IN App('" + startApp
					+ "') ? ] ? ] ? ] ? ] ? ] ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(2, ((Answer) answer).getFlows().getFlow().size());
	}

	@Test
	public void test14() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "FILTER [ UNIFY [ Flows IN App('" + startApp + "') ?, IntentSources IN App('" + endApp
					+ "') ? ] ? ] ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(2, ((Answer) answer).getFlows().getFlow().size());
		assertTrue(((Answer) answer).getIntentsources() == null
				|| ((Answer) answer).getIntentsources().getIntentsource() == null
				|| ((Answer) answer).getIntentsources().getIntentsource().isEmpty());
		assertTrue(
				((Answer) answer).getIntentsinks() == null || ((Answer) answer).getIntentsinks().getIntentsink() == null
						|| ((Answer) answer).getIntentsinks().getIntentsink().isEmpty());
	}

	@Test
	public void test15() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "FILTER [ UNIFY [ Flows IN App('" + startApp + "') ?, IntentSinks IN App('" + startApp
					+ "') ? ] ? | Flows ] ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(1, ((Answer) answer).getIntentsinks().getIntentsink().size());
		assertTrue(((Answer) answer).getFlows() == null || ((Answer) answer).getFlows().getFlow() == null
				|| ((Answer) answer).getFlows().getFlow().isEmpty());
	}

	@Test
	public void test16() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "FILTER [ Flows IN App('" + startApp
					+ "') ? | Method('<de.foellix.aql.aqlbench.api19.interappstart1.MainActivity: void source()>')->App('"
					+ startApp + "') ] ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(1, ((Answer) answer).getFlows().getFlow().size());
	}

	@Test
	public void test17() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "FILTER [ Flows IN App('" + startApp + "') ? | App('" + endApp + "') ] ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertNull(((Answer) answer).getFlows());
	}

	@Test
	public void test18() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "FILTER [ UNIFY [ Flows IN App('" + startApp + "') ?, IntentSinks IN App('" + startApp
					+ "') ? ] ? | 'converted'='true' ] ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(2, ((Answer) answer).getFlows().getFlow().size());
		assertNull(((Answer) answer).getIntentsinks());
	}

	@Test
	public void test19() {
		Object answer = null;
		boolean noException = true;
		try {
			final String query = "FILTER [ UNIFY [ Flows IN App('" + startApp + "') ?, IntentSinks IN App('" + startApp
					+ "') ? ] ? | Method('<de.foellix.aql.aqlbench.api19.interappstart1.MainActivity: void sink(android.content.Intent)>')->App('"
					+ startApp + "') ] ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(1, ((Answer) answer).getFlows().getFlow().size());
		assertNull(((Answer) answer).getIntentsinks());
	}
}