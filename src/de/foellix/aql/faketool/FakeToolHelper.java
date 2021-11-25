package de.foellix.aql.faketool;

import java.io.File;

import de.foellix.aql.Properties;

public class FakeToolHelper {
	public static String FAKETOOL_VARIABLE = "%FAKETOOL%";

	private static File fakeToolJar = null;

	public static String replaceFakeToolVariable(String input) {
		if (fakeToolJar == null) {
			final String version = Properties.info().VERSION;
			fakeToolJar = new File("target/build/");
			fakeToolJar = new File(fakeToolJar, "AQL-System-" + version + ".jar");
		}

		return input.replace(FAKETOOL_VARIABLE, fakeToolJar.getName());
	}
}
