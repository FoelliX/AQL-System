package de.foellix.aql.tests;

import de.foellix.aql.Log;
import de.foellix.aql.system.System;

public class SystemTest10 {
	public static void main(final String[] args) {
		Log.setLogLevel(Log.DEBUG_DETAILED);

		final System aqlSystem = new System();
		final String query = "FILTER [Flows FROM App('SIMApp.apk') TO App('SMSApp.apk') ? | 'complete' = 'true' ]";
		aqlSystem.query(query);
	}
}
