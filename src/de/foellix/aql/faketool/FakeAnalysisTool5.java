package de.foellix.aql.faketool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;

/**
 * Returns the argument 'true'
 *
 * @author FPauck
 *
 */
public class FakeAnalysisTool5 {
	public static void fakeArguments(String input) {
		if (input.equals(FakeTool.InterAppStart1Hash) || input.equals(FakeTool.InterAppStart1_preprocessedHash)
				|| input.equals(FakeTool.InterAppEnd1Hash) || input.equals(FakeTool.InterAppEnd1_preprocessedHash)) {
			File result;
			if (input.equals(FakeTool.InterAppStart1Hash) || input.equals(FakeTool.InterAppStart1_preprocessedHash)) {
				result = new File(FakeTool.RESULTS_DIRECTORY, FakeTool.apkName + "_arguments.txt");
			} else {
				result = new File(FakeTool.RESULTS_DIRECTORY, FakeTool.apkName + "_arguments.txt");
			}
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(result))) {
				writer.write("true");
				writer.close();

				System.out.println("Computing Arguments...\nInput: " + input + "\nOutput: " + result.getAbsolutePath());
			} catch (final FileAlreadyExistsException e) {
				// Ignore
			} catch (final IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("No arguments for this input! (Input: " + input + ")");
		}
	}
}
