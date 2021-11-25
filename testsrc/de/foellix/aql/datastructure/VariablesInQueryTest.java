package de.foellix.aql.datastructure;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.foellix.aql.Log;
import de.foellix.aql.SimSmsTest;
import de.foellix.aql.datastructure.handler.QueryHandler;

class VariablesInQueryTest extends SimSmsTest {
	private static final String VARIABLE_PREFIX_FOR_TESTING = "var";

	private static final String SIMApp = new File("examples/scenarios/simsms/SIMApp.apk").getAbsolutePath();
	private static final String SMSApp = new File("examples/scenarios/simsms/SMSApp.apk").getAbsolutePath();

	private static int id;

	@BeforeAll
	public static void setup() {
		Log.setLogLevel(Log.DEBUG);
		Log.setPrefixEnabled(false);

		id = 0;
	}

	@Test
	public void test01() {
		final String query = "Flows IN App('" + SIM_APP + "') ?\nFlows IN App('" + SMS_APP + "') ?";
		assertTrue(test(query, "Flows IN App('" + SIMApp + "') ?\nFlows IN App('" + SMSApp + "') ?"));
	}

	@Test
	public void test02() {
		final String query = VARIABLE_PREFIX_FOR_TESTING + "1 = Flows IN App('" + SIM_APP + "') ?\nFlows IN App('"
				+ SMS_APP + "') ?";
		assertTrue(test(query, "Flows IN App('" + SMSApp + "') ?"));
	}

	@Test
	public void test03() {
		final String query = VARIABLE_PREFIX_FOR_TESTING + "1 = Flows IN App('" + SIM_APP + "') ?\n$"
				+ VARIABLE_PREFIX_FOR_TESTING + "1";
		assertTrue(test(query, "Flows IN App('" + SIMApp + "') ?"));
	}

	@Test
	public void test04() {
		final String query = VARIABLE_PREFIX_FOR_TESTING + "1 = Flows IN App('" + SIM_APP + "') ?\nUNIFY [ $"
				+ VARIABLE_PREFIX_FOR_TESTING + "1, Flows IN App('" + SMS_APP + "') ? ] ?";
		assertTrue(
				test(query, "UNIFY [\n\tFlows IN App('" + SIMApp + "') ?,\n\tFlows IN App('" + SMSApp + "') ?\n] ?"));
	}

	@Test
	public void test05() {
		final String query = VARIABLE_PREFIX_FOR_TESTING + "1 = Flows IN App('" + SIM_APP + "') ?\nUNIFY [ $"
				+ VARIABLE_PREFIX_FOR_TESTING + "1, Flows IN App('" + SMS_APP + "') ?, $" + VARIABLE_PREFIX_FOR_TESTING
				+ "1 ] ?";
		assertTrue(test(query, "UNIFY [\n\tFlows IN App('" + SIMApp + "') ?,\n\tFlows IN App('" + SMSApp
				+ "') ?,\n\tFlows IN App('" + SIMApp + "') ?\n] ?"));
	}

	@Test
	public void test06() {
		final String query = "$" + VARIABLE_PREFIX_FOR_TESTING + "1 " + VARIABLE_PREFIX_FOR_TESTING
				+ "1 = Flows IN App('" + SIM_APP + "') ?";
		assertTrue(test(query, ""));
	}

	@Test
	public void test07() {
		final String query = "f = Flows IN App('" + SIM_APP + "') ?\n$f";
		assertTrue(test(query, "Flows IN App('" + SIMApp + "') ?"));
	}

	@Test
	public void test08() {
		final String query = "f1 = Flows IN App('" + SIM_APP + "') ?\nf2 = Flows IN App('" + SMS_APP + "') ?\n$f1";
		assertTrue(test(query, "Flows IN App('" + SIMApp + "') ?"));
	}

	@Test
	public void test09() {
		final String query = "f = Flows IN App('" + SIM_APP + "') ?\nff = Flows IN App('" + SMS_APP + "') ?\n$f";
		assertTrue(test(query, "Flows IN App('" + SIMApp + "') ?"));
	}

	@Test
	public void test10() {
		final String query = "f = Flows IN App('" + SIM_APP + "') ?\nff = Flows IN App('" + SMS_APP + "') ?\n$ff";
		assertTrue(test(query, "Flows IN App('" + SMSApp + "') ?"));
	}

	@Test
	public void test11() {
		final String query = "ff = Flows IN App('" + SIM_APP + "') ?\nf = Flows IN App('" + SMS_APP + "') ?\n$ff";
		assertTrue(test(query, "Flows IN App('" + SIMApp + "') ?"));
	}

	@Test
	public void test12() {
		final String query = VARIABLE_PREFIX_FOR_TESTING + "1 = Slice FROM App('" + SIM_APP + "') !\nFlows IN App($"
				+ VARIABLE_PREFIX_FOR_TESTING + "1) ?";
		assertTrue(test(query, "Flows IN App(Slice FROM App('" + SIMApp + "') !) ?"));
	}

	@Test
	public void test13() {
		final String query = VARIABLE_PREFIX_FOR_TESTING + "1 = Slice FROM App('" + SIM_APP + "') !\nFlows IN App({ $"
				+ VARIABLE_PREFIX_FOR_TESTING + "1 }) ?";
		assertTrue(test(query, "Flows IN App({ Slice FROM App('" + SIMApp + "') ! }) ?"));
	}

	@Test
	public void test14() {
		final String query = VARIABLE_PREFIX_FOR_TESTING + "1 = { Slice FROM App('" + SIM_APP + "') ! }\nFlows IN App($"
				+ VARIABLE_PREFIX_FOR_TESTING + "1) ?";
		assertTrue(test(query, "Flows IN App({ Slice FROM App('" + SIMApp + "') ! }) ?"));
	}

	@Test
	public void test15() {
		final String query = VARIABLE_PREFIX_FOR_TESTING + "1 = { Slice FROM App('" + SIM_APP
				+ "') ! }\nFlows IN App({ $" + VARIABLE_PREFIX_FOR_TESTING + "1 }) ?";
		assertTrue(test(query, "Flows IN App({ Slice FROM App('" + SIMApp + "') ! }) ?"));
	}

	private static boolean test(String query, String expected) {
		id++;
		final String temp = QueryHandler.parseQuery(query).toString();
		if (!temp.equals(expected)) {
			Log.msg("*** Test (" + id + ") failed! ***\nquery:\n'" + query + "'\n\nexpected:\n'" + expected
					+ "'\n\nactual:\n'" + temp + "'\n", Log.DEBUG);
			return false;
		} else {
			Log.msg("*** Test (" + id + ") passed! ***\nactual:\n'" + temp + "'\n", Log.DEBUG);
			return true;
		}
	}
}