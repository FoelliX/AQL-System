package de.foellix.aql.system;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.foellix.aql.Log;
import de.foellix.aql.SimSmsTest;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.helper.EqualsHelper;
import de.foellix.aql.helper.Helper;

@Tag("systemIsSetup")
public class SystemTest extends SimSmsTest {
	@BeforeAll
	public static void setup() {
		Log.setLogLevel(Log.DEBUG_DETAILED);
		Log.setShorten(true);
		BackupAndReset.reset();
	}

	@Test
	public void test01() {
		boolean noException = true;
		try {
			final AQLSystem aqlSystem = new AQLSystem();
			final String query = "Flows IN App('" + SIM_APP + "') ?";
			aqlSystem.queryAndWait(query);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	public void test02() {
		boolean noException = true;
		try {
			final AQLSystem aqlSystem = new AQLSystem();
			final String query1 = "Flows IN App('" + SIM_APP + "') ? Flows IN App('" + SMS_APP + "') ?";
			final String query2 = "UNIFY[Flows IN App('" + SIM_APP + "') ?, Flows IN App('" + SMS_APP
					+ "') ?] ? UNIFY[Flows IN App('" + SIM_APP + "') ?, Flows IN App('" + SMS_APP + "') ?] ?";

			aqlSystem.queryAndWait(query1);
			aqlSystem.queryAndWait(query2);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	public void test03() {
		boolean noException = true;
		try {
			final AQLSystem aqlSystem = new AQLSystem();
			final String query1 = "IntentSinks IN App('" + SIM_APP + "') ? IntentSources IN App('" + SMS_APP + "') ?";
			final String query2 = "CONNECT[IntentSinks IN App('" + SIM_APP + "') ?, IntentSources IN App('" + SMS_APP
					+ "') ?] ?";

			aqlSystem.queryAndWait(query1);
			aqlSystem.queryAndWait(query2);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	public void test04() {
		boolean noException = true;
		try {
			final AQLSystem aqlSystem = new AQLSystem(new Options().setRules(new File("examples/no_rules.xml")));
			final String query = "Flows FROM App('" + SIM_APP + "') TO App('" + SMS_APP + "') ?";

			aqlSystem.queryAndWait(query);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	public void test05() {
		boolean noException = true;
		try {
			final AQLSystem aqlSystem = new AQLSystem();
			final String query = "FILTER [UNIFY [Flows FROM App('" + SIM_APP + "') TO App('" + SMS_APP
					+ "') ?, UNIFY [Permissions IN App('" + SIM_APP + "') ?, Permissions IN App('" + SMS_APP
					+ "') ?] ?] ?] ?";

			aqlSystem.queryAndWait(query);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	public void test06() {
		final String directory = "examples/scenarios/simsms";
		Object a1 = null;
		Object a2 = null;

		boolean noException = true;
		try {
			final AQLSystem aqlSystem = new AQLSystem(new Options().setRules(new File("examples/no_rules.xml")));
			final String query1 = "UNIFY [Flows IN App('" + new File(directory).getAbsolutePath() + "/*.apk') ?] ?";
			final String query2 = "UNIFY [Flows IN App('" + directory + "/*.apk') ?] ?";
			final String query3 = "UNIFY [Flows FROM App('" + directory + "/*.apk') ?] ?";
			final String query4 = "UNIFY [Flows TO App('" + directory + "/*.apk') ?] ?";
			final String query5 = "Flows IN App('" + directory + "/*.apk') ?";
			final String query6 = "UNIFY [Flows FROM App('" + directory + "/*.apk') TO App('" + directory
					+ "/*.apk')?] ?";

			a1 = aqlSystem.queryAndWait(query1).iterator().next();
			a2 = aqlSystem.queryAndWait(query2).iterator().next();
			aqlSystem.queryAndWait(query3);
			aqlSystem.queryAndWait(query4);
			aqlSystem.queryAndWait(query5);
			aqlSystem.queryAndWait(query6);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertTrue(EqualsHelper.equals((Answer) a1, (Answer) a2));
	}

	@Test
	public void test07() {
		final String directory = "examples/scenarios/simsms";

		boolean noException = true;
		try {
			final AQLSystem aqlSystem = new AQLSystem(new Options().setRules(new File("examples/default_rules.xml")));
			final String query1 = "UNIFY [Flows IN App('" + directory + "') ?] ?";
			final String query2 = "UNIFY [Flows FROM App('" + directory + "') TO App('" + directory + "')?] ?";

			aqlSystem.queryAndWait(query1).iterator().next();
			aqlSystem.queryAndWait(query2).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	public void test08() {
		final File answersFolder = new File("answers");
		Object a1 = null, a2 = null, a3 = null;

		boolean noException = true;
		try {
			final AQLSystem aqlSystem = new AQLSystem();

			final String query1 = "Permissions IN App('" + SIM_APP + "') ?";
			a1 = aqlSystem.queryAndWait(query1).iterator().next();

			final String query2 = "'" + Helper.lastFileModified(answersFolder).getAbsolutePath() + "' ?";
			a2 = aqlSystem.queryAndWait(query2).iterator().next();

			final String query3 = "UNIFY [Permissions IN App('" + SIM_APP + "') ?, '"
					+ Helper.lastFileModified(answersFolder).getAbsolutePath() + "' ?] ?";
			a3 = aqlSystem.queryAndWait(query3).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertTrue(EqualsHelper.equals((Answer) a1, (Answer) a2));
		assertTrue(EqualsHelper.equals((Answer) a2, (Answer) a3));
		assertTrue(EqualsHelper.equals((Answer) a1, (Answer) a3));
	}

	@Test
	public void test09() {
		boolean noException = true;
		try {
			final AQLSystem aqlSystem = new AQLSystem();
			final String query = "FILTER [Flows IN App('" + SIM_APP + "') ? | 'complete'='true'] ?";
			aqlSystem.queryAndWait(query);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	public void test10() {
		boolean noException = true;
		try {
			final AQLSystem aqlSystem = new AQLSystem();
			final String query = "Flows IN App('" + SIM_APP + "' | 'TEST') ?";
			aqlSystem.queryAndWait(query);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	public void test11() {
		boolean noException = true;
		try {
			final AQLSystem aqlSystem = new AQLSystem();
			final String query = "FILTER [Flows FROM App('" + SIM_APP + "') TO App('" + SMS_APP
					+ "') ? | 'complete' = 'true' ] ?";
			aqlSystem.queryAndWait(query);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	public void test12() {
		boolean noException = true;
		try {
			final AQLSystem aqlSystem = new AQLSystem();
			final Tool fd = ConfigHandler.getInstance().getToolByName("FlowDroid");
			if (fd != null) {
				final int backupMem = fd.getExecute().getMemoryPerInstance();
				fd.getExecute().setMemoryPerInstance(9999);
				final String query = "UNIFY [ Flows IN App('" + SIM_APP + "') ?, Flows IN App('" + SMS_APP + "') ? ] ?";
				aqlSystem.queryAndWait(query);
				fd.getExecute().setMemoryPerInstance(backupMem);
			}
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	public void test13() {
		boolean noException = true;
		try {
			final AQLSystem aqlSystem = new AQLSystem();
			final String q1 = "FILTER [ Flows IN App('" + SIM_APP + "') ? | App('" + SIM_APP + "') ] ?";
			final String q2 = "FILTER [ Flows IN App('" + SIM_APP + "') ? | App('" + SMS_APP + "') ] ?";
			final String q3 = "FILTER [ Flows IN App('" + SIM_APP + "') ? | Method('not existent')->App('" + SIM_APP
					+ "') ] ?";
			final Answer a1 = (Answer) aqlSystem.queryAndWait(q1).iterator().next();
			final Answer a2 = (Answer) aqlSystem.queryAndWait(q2).iterator().next();
			final Answer a3 = (Answer) aqlSystem.queryAndWait(q3).iterator().next();
			assertTrue(a1.getFlows().getFlow().size() >= 1);
			assertTrue(a2.getFlows() == null);
			assertTrue(a3.getFlows() == null);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	public void test14() {
		boolean noException = true;
		try {
			final AQLSystem aqlSystem = new AQLSystem();
			final String query = "FILTER [ Flows IN App('" + SIM_APP + "') ? | App('" + SIM_APP + "' | 'TEST') ] ?";
			aqlSystem.queryAndWait(query);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}
}