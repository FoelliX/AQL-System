package de.foellix.aql.tests;

import de.foellix.aql.Log;
import de.foellix.aql.ui.cli.CommandLineInterface;

public class GUITest {
	public static void main(final String[] args) {
		Log.setLogLevel(Log.DEBUG);

		final String query = "FILTER [UNIFY [Flows FROM App('SIMApp.apk') TO App('SMSApp.apk') ?, UNIFY [Permissions IN App('SIMApp.apk') ?, Permissions IN App('SMSApp.apk') ?]]]";

		CommandLineInterface.main(new String[] { "-gui", "-q", query });
	}
}
