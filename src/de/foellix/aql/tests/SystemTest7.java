package de.foellix.aql.tests;

import java.io.File;
import java.io.FileFilter;

import de.foellix.aql.Log;
import de.foellix.aql.system.System;

public class SystemTest7 {
	public static void main(final String[] args) {
		Log.setLogLevel(Log.DEBUG);

		final System aqlSystem = new System();

		final String query1 = "Permissions IN App('SIMApp.apk') ?";
		aqlSystem.query(query1);

		final String query2 = "'" + lastFileModified().getAbsolutePath() + "' !";
		aqlSystem.query(query2);

		final String query3 = "UNIFY [Permissions IN App('SMSApp.apk') ?, '" + lastFileModified().getAbsolutePath()
				+ "' !]";
		aqlSystem.query(query3);
	}

	private static File lastFileModified() {
		final File fl = new File("answers/");
		final File[] files = fl.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File file) {
				return file.isFile();
			}
		});
		long lastMod = Long.MIN_VALUE;
		File choice = null;
		for (final File file : files) {
			if (file.lastModified() > lastMod) {
				choice = file;
				lastMod = file.lastModified();
			}
		}
		return choice;
	}
}
