package de.foellix.aql.faketool;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Returns a fake-sliced app
 *
 * @author FPauck
 *
 */
public class FakeAnalysisTool4 {
	public static void fakeSlice(String input) {
		try {
			if (input.equals(FakeTool.InterAppStart1Hash) || input.equals(FakeTool.InterAppStart1_preprocessedHash)
					|| input.equals(FakeTool.InterAppEnd1Hash)
					|| input.equals(FakeTool.InterAppEnd1_preprocessedHash)) {
				final File origin;
				final File result;
				if (input.equals(FakeTool.InterAppStart1Hash)
						|| input.equals(FakeTool.InterAppStart1_preprocessedHash)) {
					origin = new File(FakeTool.APK_DIRECTORY, "InterAppStart1.apk");
					result = new File(FakeTool.RESULTS_DIRECTORY, FakeTool.apkName + "_sliced.apk");
				} else {
					origin = new File(FakeTool.APK_DIRECTORY, "InterAppStart1.apk");
					result = new File(FakeTool.RESULTS_DIRECTORY, FakeTool.apkName + "_sliced.apk");
				}
				Files.copy(origin.toPath(), result.toPath(), StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Slicing...\nOutput: " + result.getAbsolutePath());
			} else {
				System.out.println("Nothing to slice! (Input: " + input + ")");
			}
		} catch (final FileAlreadyExistsException e) {
			// Ignore
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}
