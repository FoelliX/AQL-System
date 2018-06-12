package de.foellix.aql.tests;

import de.foellix.aql.Log;
import de.foellix.aql.system.System;

public class SystemTest2 {
	public static void main(final String[] args) {
		Log.setLogLevel(Log.DEBUG_DETAILED);

		final System aqlSystem = new System();
		final String query1 = "Flows IN App('SIMApp.apk') ? Flows IN App('SMSApp.apk') ?";
		final String query2 = "UNIFY[Flows IN App('SIMApp.apk') ?, Flows IN App('SMSApp.apk') ?] UNIFY[Flows IN App('SIMApp.apk') ?, Flows IN App('SMSApp.apk') ?]";

		aqlSystem.query(query1);
		aqlSystem.query(query2);
	}
}
