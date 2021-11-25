package de.foellix.aql.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.helper.HashHelper;
import de.foellix.aql.helper.Helper;

public class ConverterTestHelper {
	public static String PLACEHOLDER = "PLACEHOLDER";

	public static void assertForConverters(String expected, String actual) {
		final boolean matches = Pattern.matches(ConverterTestHelper.makeUpExpected(expected),
				ConverterTestHelper.makeUpActual(actual));
		if (!matches) {
			final String expectedReplacedApps = ConverterTestHelper
					.makeUpExpected(expected.replaceAll("App\\('[^']*'\\)", "App('Y')"));
			final String actualReplacedApps = ConverterTestHelper
					.makeUpActual(actual.replaceAll("App\\('[^']*'\\)", "App('Y')"));
			if (expectedReplacedApps.equals(actualReplacedApps)) {
				System.out.println("With replaced apps the strings are equal!");
				System.out.println("Expected: '" + ConverterTestHelper.makeUpExpected(expected) + "'\n but was: '"
						+ ConverterTestHelper.makeUpActual(actual) + "'");
			} else {
				System.out.println("Expected: '" + expectedReplacedApps + "'\n but was: '" + actualReplacedApps + "'");
			}
		}
		assertTrue(matches);
	}

	private static String makeUpExpected(String expected) {
		return expected.replaceAll("\\p{Punct}", "X").replace(PLACEHOLDER, ".*");
	}

	private static String makeUpActual(String actual) {
		return actual.replaceAll("\\p{Punct}", "X").replace("\n", " ");
	}

	public static void assertForConvertersByHash(String expected, String apkName, Answer answer) {
		assertEquals(expected, HashHelper.sha256Hash(
				Helper.toString(answer).replaceAll("App\\('.*'\\)", "App('/some/path/" + apkName + ".apk')")));
	}
}
