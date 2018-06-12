package de.foellix.aql.tests;

import de.foellix.aql.Log;
import de.foellix.aql.system.System;

public class SystemTest3 {
	public static void main(final String[] args) {
		Log.setLogLevel(Log.DEBUG);

		final System aqlSystem = new System();
		final String query1 = "IntentSinks IN App('SIMApp.apk') ? IntentSources IN App('SMSApp.apk') ?";
		final String query2 = "CONNECT[IntentSinks IN App('SIMApp.apk') ?, IntentSources IN App('SMSApp.apk') ?]";

		aqlSystem.query(query1);
		aqlSystem.query(query2);
	}
}
