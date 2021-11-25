package de.foellix.aql.faketool;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class FakePreprocessor {
	public static void fakePreprocess(String input) {
		try {
			if (input.equals(FakeTool.InterAppStart1Hash) || input.equals(FakeTool.InterAppStart1_preprocessedHash)
					|| input.equals(FakeTool.InterAppEnd1Hash)
					|| input.equals(FakeTool.InterAppEnd1_preprocessedHash)) {
				final File origin;
				final File result;
				if (input.equals(FakeTool.InterAppStart1Hash)
						|| input.equals(FakeTool.InterAppStart1_preprocessedHash)) {
					origin = new File(FakeTool.APK_DIRECTORY, "InterAppStart1_preprocessed.apk");
					result = new File(FakeTool.RESULTS_DIRECTORY, FakeTool.apkName + "_preprocessed.apk");
				} else {
					origin = new File(FakeTool.APK_DIRECTORY, "InterAppEnd1_preprocessed.apk");
					result = new File(FakeTool.RESULTS_DIRECTORY, FakeTool.apkName + "_preprocessed.apk");
				}
				Files.copy(origin.toPath(), result.toPath(), StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Preprocessing...\nOutput: " + result.getAbsolutePath());
			} else {
				System.out.println("Nothing to preprocess!");
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
