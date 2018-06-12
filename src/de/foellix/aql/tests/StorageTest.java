package de.foellix.aql.tests;

import de.foellix.aql.datastructure.handler.ParseException;
import de.foellix.aql.system.Storage;

public class StorageTest {
	public static void main(final String[] args) throws ParseException {
		System.out.println(Storage.getInstance().getData().toString());
	}
}
