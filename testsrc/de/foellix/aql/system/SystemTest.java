package de.foellix.aql.system;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.helper.EqualsHelper;
import de.foellix.aql.helper.Helper;

@Tag("systemIsSetup")
public class SystemTest {
	@BeforeAll
	public static void setup() {
		Log.setLogLevel(Log.DEBUG);
		Log.setShorten(true);
	}

	@Test
	public void test1() {
		boolean noException = true;
		try {
			final System aqlSystem = new System();
			final String query = "Flows IN App('SIMApp.apk') ?";
			aqlSystem.queryAndWait(query);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	public void test2() {
		boolean noException = true;
		try {
			final System aqlSystem = new System();
			final String query1 = "Flows IN App('SIMApp.apk') ? Flows IN App('SMSApp.apk') ?";
			final String query2 = "UNIFY[Flows IN App('SIMApp.apk') ?, Flows IN App('SMSApp.apk') ?] UNIFY[Flows IN App('SIMApp.apk') ?, Flows IN App('SMSApp.apk') ?]";

			aqlSystem.queryAndWait(query1);
			aqlSystem.queryAndWait(query2);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	public void test3() {
		boolean noException = true;
		try {
			final System aqlSystem = new System();
			final String query1 = "IntentSinks IN App('SIMApp.apk') ? IntentSources IN App('SMSApp.apk') ?";
			final String query2 = "CONNECT[IntentSinks IN App('SIMApp.apk') ?, IntentSources IN App('SMSApp.apk') ?]";

			aqlSystem.queryAndWait(query1);
			aqlSystem.queryAndWait(query2);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	public void test4() {
		boolean noException = true;
		try {
			final System aqlSystem = new System();
			final String query = "Flows FROM App('SIMApp.apk') TO App('SMSApp.apk') ?";

			aqlSystem.queryAndWait(query);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	public void test5() {
		boolean noException = true;
		try {
			final System aqlSystem = new System();
			final String query = "FILTER [UNIFY [Flows FROM App('SIMApp.apk') TO App('SMSApp.apk') ?, UNIFY [Permissions IN App('SIMApp.apk') ?, Permissions IN App('SMSApp.apk') ?]]]";

			aqlSystem.queryAndWait(query);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	public void test6() {
		final File currentDirectory = new File("examples/simsms");
		Answer a1 = null, a2 = null;

		boolean noException = true;
		try {
			final System aqlSystem = new System();
			final String query1 = "UNIFY [Flows IN App('" + currentDirectory.getAbsolutePath() + "') ?]";
			final String query2 = "UNIFY [Flows IN App('') ?]";
			// final String query3 = "UNIFY [Flows FROM App('') TO App('')?]";

			a1 = aqlSystem.queryAndWait(query1).iterator().next();
			a2 = aqlSystem.queryAndWait(query2).iterator().next();
			// aqlSystem.queryAndWait(query3);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertTrue(EqualsHelper.equals(a1, a2));
	}

	@Test
	public void test7() {
		final File answersFolder = new File("answers");
		Answer a1 = null, a2 = null, a3 = null;

		boolean noException = true;
		try {
			final System aqlSystem = new System();

			final String query1 = "Permissions IN App('SIMApp.apk') ?";
			a1 = aqlSystem.queryAndWait(query1).iterator().next();

			final String query2 = "'" + Helper.lastFileModified(answersFolder).getAbsolutePath() + "' !";
			a2 = aqlSystem.queryAndWait(query2).iterator().next();

			final String query3 = "UNIFY [Permissions IN App('SIMApp.apk') ?, '"
					+ Helper.lastFileModified(answersFolder).getAbsolutePath() + "' !]";
			a3 = aqlSystem.queryAndWait(query3).iterator().next();
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
		assertTrue(EqualsHelper.equals(a1, a2));
		assertTrue(EqualsHelper.equals(a2, a3));
		assertTrue(EqualsHelper.equals(a1, a3));
	}

	@Test
	public void test8() {
		boolean noException = true;
		try {
			final System aqlSystem = new System();
			final String query = "FILTER [Flows IN App('SIMApp.apk') ? | 'complete'='true']";
			aqlSystem.queryAndWait(query);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}

	@Test
	public void test9() {
		boolean noException = true;
		try {
			final System aqlSystem = new System();
			final String query = "Flows IN App('SIMApp.apk' | 'TEST') ?";
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
			final System aqlSystem = new System();
			final String query = "FILTER [Flows FROM App('SIMApp.apk') TO App('SMSApp.apk') ? | 'complete' = 'true' ]";
			aqlSystem.queryAndWait(query);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}
}
