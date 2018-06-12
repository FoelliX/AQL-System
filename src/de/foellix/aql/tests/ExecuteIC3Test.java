package de.foellix.aql.tests;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ExecuteIC3Test {
	public static void main(final String[] args) {
		System.out.println("Process-Start: " + System.currentTimeMillis() + "\n");

		final String runString = "/home/foellix/masterarbeit/run/run.sh -d apps/sen1/SIMApp.apk";

		try {
			final Process p = Runtime.getRuntime().exec(runString);

			p.waitFor();

			String line;
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			input.close();

			input = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((line = input.readLine()) != null) {
				System.err.println(line);
			}
			input.close();
		} catch (final Exception err) {
			err.printStackTrace();
		}

		System.out.println("\nProcess-End: " + System.currentTimeMillis());
	}
}