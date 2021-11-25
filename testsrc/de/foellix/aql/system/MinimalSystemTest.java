package de.foellix.aql.system;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.foellix.aql.Log;
import de.foellix.aql.SimSmsTest;

public class MinimalSystemTest extends SimSmsTest {
	@BeforeAll
	public static void setup() {
		Log.setLogLevel(Log.DEBUG_DETAILED);
	}

	@Test
	public void test01() {
		assertTrue(test("Flows IN App('" + SIM_APP + "') ?"));
	}

	@Test
	public void test02() {
		assertTrue(test("Flows IN App('" + SIM_APP + "') ?"));
	}

	@Test
	public void test03() {
		assertTrue(test("Flows FROM App('" + SIM_APP + "') TO App('" + SMS_APP + "') ?"));
	}

	private boolean test(String query) {
		try {
			final AQLSystem aqlSystem = new AQLSystem();
			aqlSystem.queryAndWait(query);
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}
