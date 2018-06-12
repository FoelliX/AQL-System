package de.foellix.aql.tests;

import de.foellix.aql.Log;
import de.foellix.aql.system.System;

public class SystemTest6 {
	public static void main(final String[] args) {
		Log.setLogLevel(Log.DEBUG);

		final System aqlSystem = new System();
		final String query1 = "UNIFY [Flows IN App('E:/uni/masterarbeit/workspace/AQL') ?]";
		final String query2 = "UNIFY [Flows IN App('') ?]";
		final String query3 = "UNIFY [Flows FROM App('') TO App('')?]";
		aqlSystem.query(query1);
		aqlSystem.query(query2);
		aqlSystem.query(query3);
	}
}
