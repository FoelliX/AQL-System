package de.foellix.aql.tests;

import de.foellix.aql.Log;
import de.foellix.aql.system.System;

public class SystemTest1 {
	public static void main(final String[] args) {
		Log.setLogLevel(Log.DEBUG);

		final System aqlSystem = new System();
		final String query = "Flows IN App('SIMApp.apk') ?";
		aqlSystem.query(query);
	}
}
