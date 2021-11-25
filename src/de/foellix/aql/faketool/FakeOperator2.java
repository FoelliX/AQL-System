package de.foellix.aql.faketool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

public class FakeOperator2 {
	public static void fakeOperation() {
		final File result = new File(FakeTool.RESULTS_DIRECTORY, "operator2_result.txt");

		try (BufferedWriter writer = new BufferedWriter(new FileWriter(result))) {
			writer.write("getIMEI() // SOURCE");
			writer.write("sendTextMessage(..) // SINK");
			writer.close();

			System.out.println("Applying TOFD operator...\nOutput: " + result.getAbsolutePath());
		} catch (final FileAlreadyExistsException e) {
			// Ignore
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}