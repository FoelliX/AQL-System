package de.foellix.aql.tests;

import de.foellix.aql.Log;
import de.foellix.aql.system.System;

public class SystemTest4 {
	public static void main(final String[] args) {
		Log.setLogLevel(Log.DEBUG);

		final System aqlSystem = new System();
		final String query = "Flows FROM App('SIMApp.apk') TO App('SMSApp.apk') ?";
		// final String query = "CONNECT [ Flows IN App('SIMApp.apk') ?,
		// Flows IN App('SMSApp.apk') ?, IntentSinks IN App('SIMApp.apk')
		// ?, IntentSources IN App('SMSApp.apk') ? ]";
		aqlSystem.query(query);
	}
}
