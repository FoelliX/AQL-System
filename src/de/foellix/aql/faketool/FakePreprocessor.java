package de.foellix.aql.faketool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FakePreprocessor {
	public static void fakePreprocess(String input, File resultsDirectory) {
		try {
			if (input.contains("Start1.apk") || input.contains("End1.apk")) {
				final File origin = new File(input);
				File result;
				if (input.contains("Start1.apk")) {
					result = new File(resultsDirectory, "InterAppStart1_preprocessed.apk");
				} else {
					result = new File(resultsDirectory, "InterAppEnd1_preprocessed.apk");
				}
				Files.copy(origin.toPath(), result.toPath());
				System.out.println("Preprocessing...\nInput: " + input + "\nOutput: " + result.getAbsolutePath());
			} else {
				System.out.println("Nothing top preprocess! (Input: " + input + ")");
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
