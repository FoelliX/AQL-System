package de.foellix.aql.tests;

import de.foellix.aql.Log;
import de.foellix.aql.system.System;

public class SystemTest5 {
	public static void main(final String[] args) {
		Log.setLogLevel(Log.DEBUG_DETAILED);

		final System aqlSystem = new System();
		final String query = "FILTER [UNIFY [Flows FROM App('SIMApp.apk') TO App('SMSApp.apk') ?, UNIFY [Permissions IN App('SIMApp.apk') ?, Permissions IN App('SMSApp.apk') ?]]]";
		aqlSystem.query(query);
	}
}
