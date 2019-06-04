package de.foellix.aql.faketool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.foellix.aql.Log;
import de.foellix.aql.Properties;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.system.BackupAndReset;
import de.foellix.aql.system.System;

@Tag("requiresBuild")
@Tag("systemIsSetup")
public class FakeToolTest1 {
	private static System aqlSystem;

	@BeforeAll
	public static void setup() {
		Log.setLogLevel(Log.NORMAL);
		Log.setShorten(true);

		// Setup config
		final String version = Properties.info().VERSION;
		File fakeToolJar = new File("target/build/");
		fakeToolJar = new File(fakeToolJar, "AQL-System-" + version + ".jar");

		ConfigHandler.getInstance().setConfig(new File("examples/faketool/config_faketool.xml"));

		// Update FakeTool jar
		for (final Tool tool : ConfigHandler.getInstance().getConfig().getTools().getTool()) {
			tool.getExecute().setRun(tool.getExecute().getRun().replaceFirst("%FAKETOOL%", fakeToolJar.getName()));
		}
		for (final Tool tool : ConfigHandler.getInstance().getConfig().getPreprocessors().getTool()) {
			tool.getExecute().setRun(tool.getExecute().getRun().replaceFirst("%FAKETOOL%", fakeToolJar.getName()));
		}
		for (final Tool tool : ConfigHandler.getInstance().getConfig().getOperators().getTool()) {
			tool.getExecute().setRun(tool.getExecute().getRun().replaceFirst("%FAKETOOL%", fakeToolJar.getName()));
		}
		for (final Tool tool : ConfigHandler.getInstance().getConfig().getConverters().getTool()) {
			tool.getExecute().setRun(tool.getExecute().getRun().replaceFirst("%FAKETOOL%", fakeToolJar.getName()));
		}
		Log.msg(ConfigHandler.getInstance().toString(), Log.DEBUG_DETAILED);

		aqlSystem = new System();
	}

	@BeforeEach
	public void reset() {
		BackupAndReset.reset();
	}

	@Test
	public void test1() {
		Answer answer = null;
		boolean noException = true;
		try {
			final String query = "Flows IN App('examples/faketool/InterAppStart1.apk') ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(2, answer.getFlows().getFlow().size());
	}

	@Test
	public void test2() {
		Answer answer = null;
		boolean noException = true;
		try {
			final String query = "Flows IN App('examples/faketool/InterAppEnd1.apk') ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(2, answer.getFlows().getFlow().size());
	}

	@Test
	public void test3() {
		Answer answer = null;
		boolean noException = true;
		try {
			final String query = "IntentSinks IN App('examples/faketool/InterAppStart1.apk') ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(1, answer.getIntentsinks().getIntentsink().size());
	}

	@Test
	public void test4() {
		Answer answer = null;
		boolean noException = true;
		try {
			final String query = "IntentSources IN App('examples/faketool/InterAppEnd1.apk') ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(1, answer.getIntentsources().getIntentsource().size());
	}

	@Test
	public void test5() {
		Answer answer = null;
		boolean noException = true;
		try {
			final String query = "CONNECT [ IntentSinks IN App('examples/faketool/InterAppStart1.apk') ?, IntentSources IN App('examples/faketool/InterAppEnd1.apk') ? ]";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(1, answer.getFlows().getFlow().size());
		assertEquals(1, answer.getIntentsinks().getIntentsink().size());
		assertEquals(1, answer.getIntentsources().getIntentsource().size());
	}

	@Test
	public void test6() {
		Answer answer = null;
		boolean noException = true;
		try {
			final String query = "Flows IN App('examples/faketool/InterAppStart1.apk' | 'FAKE') ?";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(2, answer.getFlows().getFlow().size());
	}

	@Test
	public void test7() {
		Answer answer = null;
		boolean noException = true;
		try {
			final String query = "UNIFY [ Flows IN App('examples/faketool/InterAppStart1.apk') ?, Flows IN App('examples/faketool/InterAppEnd1.apk') ? ]";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(4, answer.getFlows().getFlow().size());
	}

	@Test
	public void test8() {
		Answer answer = null;
		boolean noException = true;
		try {
			final String query = "UNIFY [ Flows IN App('examples/faketool/InterAppStart1.apk') ?, Flows IN App('examples/faketool/InterAppEnd1.apk') ?, MATCH [ IntentSinks IN App('examples/faketool/InterAppStart1.apk') ?, IntentSources IN App('examples/faketool/InterAppEnd1.apk') ? ]]";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(5, answer.getFlows().getFlow().size());
		assertEquals(1, answer.getIntentsinks().getIntentsink().size());
		assertEquals(1, answer.getIntentsources().getIntentsource().size());
	}

	@Test
	public void test9() {
		Answer answer = null;
		boolean noException = true;
		try {
			final String query = "CONNECT [ Flows IN App('examples/faketool/InterAppStart1.apk') ?, Flows IN App('examples/faketool/InterAppEnd1.apk') ?, MATCH [ IntentSinks IN App('examples/faketool/InterAppStart1.apk') ?, IntentSources IN App('examples/faketool/InterAppEnd1.apk') ? ]]";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(6, answer.getFlows().getFlow().size());
		assertEquals(1, answer.getIntentsinks().getIntentsink().size());
		assertEquals(1, answer.getIntentsources().getIntentsource().size());
	}

	@Test
	public void test10() {
		Answer answer = null;
		boolean noException = true;
		try {
			final String query = "MATCH [ IntentSinks IN App('examples/faketool/InterAppStart1.apk') ?, IntentSources IN App('examples/faketool/InterAppEnd1.apk') ? ]";
			answer = aqlSystem.queryAndWait(query).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertEquals(1, answer.getFlows().getFlow().size());
		assertEquals(1, answer.getIntentsinks().getIntentsink().size());
		assertEquals(1, answer.getIntentsources().getIntentsource().size());
	}
}
