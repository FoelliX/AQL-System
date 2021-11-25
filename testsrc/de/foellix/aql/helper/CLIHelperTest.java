package de.foellix.aql.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

public class CLIHelperTest {
	@Test
	void cutFromFirstToLastTest() {
		assertEquals("test$test", "test$test");
		assertEquals("test\\$test", CLIHelper.escapeChars("test$test"));
		assertNotEquals("test$test", CLIHelper.escapeChars("test$test"));
		assertNotEquals("test$test", CLIHelper.escapeChars("test$test"));
	}
}