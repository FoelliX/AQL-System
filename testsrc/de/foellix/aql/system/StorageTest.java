package de.foellix.aql.system;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class StorageTest {
	@Test
	public void test() {
		boolean noException = true;
		try {
			java.lang.System.out.println(Storage.getInstance().getData().toString());
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}
}
