package de.foellix.aql.transformations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.foellix.aql.Log;
import de.foellix.aql.SimSmsTest;
import de.foellix.aql.datastructure.handler.QueryHandler;
import de.foellix.aql.datastructure.query.Query;
import de.foellix.aql.helper.CLIHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.AQLSystem;
import de.foellix.aql.system.exceptions.CancelExecutionException;

class QuestionTransformerTest extends SimSmsTest {
	@BeforeAll
	public static void setup() {
		CLIHelper.evaluateConfig("examples/no_config.xml");
		CLIHelper.evaluateRules("examples/rules.xml");
	}

	@AfterAll
	public static void reset() {
		CLIHelper.evaluateConfig("config.xml");
		CLIHelper.evaluateRules("examples/no_rules.xml");
	}

	@Test
	public void test01() {
		final String query = "Flows FROM Statement('X')->App('" + SIM_APP + "' | 'KEYWORD') TO App('" + SMS_APP
				+ "') FEATURING 'EFG', 'ABC', 'IAC' WITH 'Sas' = 'SaS.txt' ?";
		final String expected = "Flows IN App('" + SIM_APP
				+ "' | 'KEYWORD') FEATURING 'EFG', 'IAC' WITH 'Sas' = 'SaS.txt' ?";
		test(query, expected, true);
	}

	@Test
	public void test02() {
		final String query = "Flows FROM Statement('$r3.<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>(\\\"+49111111111\\\", null, $r2, null, null)')->Method('<de.foellix.sinkapp.SinkMainActivity: void sink()>')->Class('de.foellix.sinkapp.SinkMainActivity')->App('"
				+ SIM_APP
				+ "') TO Statement('r1 = virtualinvoke r2.<android.content.Intent: java.lang.String getStringExtra(java.lang.String)>(\\\"Secret\\\")')->Method('<de.foellix.sinkapp.SinkMainActivity: void sink2()>')->Class('de.foellix.sinkapp.SinkMainActivity2')->App('"
				+ SMS_APP + "') ?";
		final String expected = "Flows IN App('" + SMS_APP + "') ?";
		test(query, expected, true);
	}

	@Test
	public void test03() {
		final String query = "Flows FROM App('" + SIM_APP + "' | 'KEYWORD') TO App('" + SMS_APP + "') ?";
		final String expected = "Flows IN App('" + SMS_APP + "') ?";
		test(query, expected, true);
	}

	@Test
	public void test04() {
		final String query = "Flows FROM App('" + SIM_APP + "') TO App('" + SMS_APP + "') FEATURING 'COMBINER' ?";
		final String expected = "Flows IN App('" + SIM_APP + ", " + SMS_APP + "' | 'COMBINE') FEATURING 'COMBINER' ?";
		test(query, expected, true);
	}

	@Test
	public void test05() {
		final String query = "Flows FROM App('" + SIM_APP + "') TO App('" + SMS_APP + "') FEATURING 'SIMPLE' ?";
		final String expected = "Flows IN App('" + SIM_APP + ", " + SMS_APP + "' | 'COMBINE') ?";
		test(query, expected, true);
	}

	@Test
	public void test06() {
		final String query = "Flows FROM App('" + SIM_APP + "') TO App('" + SMS_APP + "') FEATURING 'SIMPLE', 'TEST' ?";
		final String expected = "CONNECT [ Flows IN App('" + SIM_APP + "') FEATURING 'TEST' ?, Flows FROM App('"
				+ SIM_APP + "') TO App('" + SMS_APP + "') FEATURING 'SIMPLE', 'TEST' ?, CONNECT [ Flows IN App('"
				+ SIM_APP + "') FEATURING 'TEST' ?, Flows IN App('" + SIM_APP
				+ "') FEATURING 'SIMPLE', 'TEST' ?, Flows IN App('" + SIM_APP
				+ "') FEATURING 'SIMPLE', 'TEST' ? ] ? ] ?";
		test(query, expected, false);
	}

	@Test
	public void test07() {
		final String query = "Flows IN App('" + SIM_APP + "') FEATURING 'SIMPLE', 'VARIABLES' ?";
		final String expected = "CONNECT [ Flows IN App(Slice FROM Statement('getSimSerialNumber')->App('" + SIM_APP
				+ "' !) TO Statement('wtf')->App('" + SIM_APP
				+ "' !) FEATURING 'icc' WITH 'INPUT_EDGES' = { FILTER [ IntentSinks IN App('" + SIM_APP
				+ "' !) ? ] ? } !) FEATURING 'OLD' ?, IntentSources IN App(Slice FROM Statement('getSimSerialNumber')->App('"
				+ SIM_APP + "' !) TO Statement('wtf')->App('" + SIM_APP
				+ "' !) FEATURING 'icc' WITH 'INPUT_EDGES' = { FILTER [ IntentSinks IN App('" + SIM_APP
				+ "' !) ? ] ? } !) ? ] ?";
		test(query, expected, false);
	}

	@Test
	public void test08() {
		final String query = "Flows FROM Statement('$r.test1')->Method('$r.test2')->Class('$r.test3')->App('" + SIM_APP
				+ "') TO Statement('$r.test4')->Method('$r.test5')->Class('$r.test6')->App('" + SIM_APP
				+ "') FEATURING 'SIMPLE', 'FULLREFS' ?";
		final String expected = "Flows IN App({ Slice FROM Statement('$r.test1')->Method('$r.test2')->Class('$r.test3')->App('"
				+ SIM_APP + "') TO Statement('$r.test4')->Method('$r.test5')->Class('$r.test6')->App('" + SIM_APP
				+ "') ! }) ?";
		test(query, expected, false);
	}

	private static void test(String query, String expected, boolean testWithSystem) {
		testWithoutSystem(query, expected);
		if (testWithSystem) {
			testWithSystem(query, expected);
		}
		Log.emptyLine();
	}

	private static void testWithoutSystem(String query, String expected) {
		final boolean noException = true;

		final Query queryObj = QueryHandler.parseQuery(query);
		final String before = Helper.replaceAllWhiteSpaceChars(queryObj.toString(), true);
		QueryTransformer.transform(queryObj, queryObj.getQuestions().iterator().next());
		final String after = Helper.replaceAllWhiteSpaceChars(queryObj.toString(), true);

		Log.msg("Test1 ->   Before: " + before, Log.NORMAL);
		Log.msg("Test1 ->    After: " + after, Log.NORMAL);
		Log.msg("Test1 -> Expected: " + expected, Log.NORMAL);

		assertTrue(noException);
		assertEquals(expected, after);
	}

	private static void testWithSystem(String query, String expected) {
		boolean noException = true;

		final AQLSystem aqlSystem = new AQLSystem();
		final Query queryObj = QueryHandler.parseQuery(query);
		final String before = Helper.replaceAllWhiteSpaceChars(queryObj.toString(), true);
		try {
			aqlSystem.getTaskCreator().query(queryObj);
		} catch (final CancelExecutionException e) {
			Log.error("Could not create task!");
			if (!e.getMessage().contains("No appropriate preprocessor")
					&& !e.getMessage().contains("No appropriate tool")) {
				noException = false;
			}
		}
		final String after = Helper.replaceAllWhiteSpaceChars(queryObj.toString(), true);

		Log.msg("Test2 ->   Before: " + before, Log.NORMAL);
		Log.msg("Test2 ->    After: " + after, Log.NORMAL);
		Log.msg("Test2 -> Expected: " + expected, Log.NORMAL);

		assertTrue(noException);
		assertEquals(expected, after);
	}
}