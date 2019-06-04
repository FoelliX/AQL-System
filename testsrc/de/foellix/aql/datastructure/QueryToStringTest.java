package de.foellix.aql.datastructure;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.handler.QueryHandler;

class QueryToStringTest {
	@BeforeAll
	public static void setup() {
		Log.setLogLevel(Log.DEBUG);
		Log.setPrefixEnabled(false);
	}

	@Test
	public void test1() {
		final String query = "Flows IN App('SIMApp.apk') ?";
		assertTrue(test(query));
	}

	@Test
	public void test2() {
		final String query = "Permissions IN App('SIMApp.apk') ?";
		assertTrue(test(query));
	}

	@Test
	public void test3() {
		final String query = "Flows IN App('SIMApp.apk' | 'TEST') ?";
		assertTrue(test(query));
	}

	@Test
	public void test4() {
		final String query = "Flows IN App('SIMApp.apk' | 'TEST' | 'TEST2') ?";
		assertTrue(test(query));
	}

	@Test
	public void test5() {
		final String query = "Flows IN App('SIMApp.apk') ?\nPermissions IN App('SIMApp.apk') ?";
		assertTrue(test(query));
	}

	@Test
	public void test6() {
		final String query = "Flows FROM App('SIMApp.apk') TO App('SMSApp.apk') ?";
		assertTrue(test(query));
	}

	@Test
	public void test7() {
		final String query = "FILTER [\n\tFlows IN App('SIMApp.apk') ?\n]";
		assertTrue(test(query));
	}

	@Test
	public void test8() {
		final String query = "FILTER [\n\tFlows IN App('SIMApp.apk') ?\n\t| 'complete' = 'true'\n]";
		assertTrue(test(query));
	}

	@Test
	public void test9() {
		final String query = "FILTER [\n\tFlows IN App('SIMApp.apk') ?\n\t| '' = 'true'\n]";
		assertTrue(test(query));
	}

	@Test
	public void test10() {
		final String query = "FILTER [\n\tFlows IN App('SIMApp.apk') ?\n\t| 'complete' = ''\n]";
		assertTrue(test(query));
	}

	@Test
	public void test11() {
		final String query = "FILTER [\n\tFlows IN App('SIMApp.apk') ?\n\t| Flows\n]";
		assertTrue(test(query));
	}

	@Test
	public void test12() {
		final String query = "FILTER [\n\tFlows IN App('SIMApp.apk') ?\n\t| 'complete' = 'true'\n\t| Flows\n]";
		assertTrue(test(query));
	}

	@Test
	public void test13() {
		final String query = "CONNECT [\n\tFlows IN App('SIMApp.apk') ?,\n\tFlows IN App('SMSApp.apk') ?\n]";
		assertTrue(test(query));
	}

	@Test
	public void test14() {
		final String query = "CONNECT [\n\tFILTER [\n\t\tFlows IN App('SIMApp.apk') ?\n\t],\n\tFlows IN App('SMSApp.apk') ?\n]";
		assertTrue(test(query));
	}

	@Test
	public void test15() {
		final String query = "'test.xml' !";
		assertTrue(test(query));
	}

	@Test
	public void test16() {
		final String query = "FILTER [\n\t'test.xml' !\n]";
		assertTrue(test(query));
	}

	private static boolean test(String query) {
		final String temp = QueryHandler.parseQuery(query).toString();
		if (!temp.equals(query)) {
			Log.msg("Test failed:\n'" + query + "'\n'" + temp + "'", Log.DEBUG);
			return false;
		} else {
			Log.msg("Test passed!", Log.DEBUG);
			return true;
		}
	}
}