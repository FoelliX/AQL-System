package de.foellix.aql.faketool;

import java.io.File;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.helper.HashHelper;
import de.foellix.aql.helper.Helper;

public class FakeTool {
	protected static File APK_DIRECTORY = new File("examples/faketool");
	protected static File RESULTS_DIRECTORY = new File("examples/faketool/results");

	protected static final String InterAppStart1Hash = HashHelper
			.sha256Hash(new File(APK_DIRECTORY, "InterAppStart1.apk"));
	protected static final String InterAppStart1_preprocessedHash = HashHelper
			.sha256Hash(new File(APK_DIRECTORY, "InterAppStart1_preprocessed.apk"));
	protected static final String InterAppEnd1Hash = HashHelper.sha256Hash(new File(APK_DIRECTORY, "InterAppEnd1.apk"));
	protected static final String InterAppEnd1_preprocessedHash = HashHelper
			.sha256Hash(new File(APK_DIRECTORY, "InterAppEnd1_preprocessed.apk"));

	protected static String apkName;

	public static void main(String[] args) {
		if (args.length <= 0) {
			System.out.println("No launch parameters provided! Exiting...");
			System.exit(1);
		} else if (args[0].equals("-tool6")) {
			// Timeout tool
			try {
				int counter = 0;
				while (true) {
					System.out.println("Infinite loop execution: " + ++counter);
					Thread.sleep(3000);
				}
			} catch (final InterruptedException e) {
				// do nothing
			}
		} else if (args[0].equals("-tool7")) {
			// Fail tool
			try {
				System.out.println("3 seconds to exit FakeTool's execution with failed flag (1).");
				Thread.sleep(3000);
				System.out.println("Exiting...");
			} catch (final InterruptedException e) {
				// do nothing
			}
			System.exit(1);
		} else {
			// Execute other FakeTools
			final File inputApkFile = new File(args[1]);
			if (inputApkFile.getName().lastIndexOf(FileHelper.FILE_ENDING_APK) > 0) {
				apkName = inputApkFile.getName().substring(0,
						inputApkFile.getName().lastIndexOf(FileHelper.FILE_ENDING_APK));
			}
			String hash = null;
			if (inputApkFile.getName().endsWith(FileHelper.FILE_ENDING_APK)) {
				final File apkFile = new File(inputApkFile.getAbsolutePath());
				if (apkFile.exists()) {
					hash = HashHelper.sha256Hash(apkFile);
				}
			}

			System.out.println("* FakeTool started *\n\nInput:\n\tApks directory: " + APK_DIRECTORY.getAbsolutePath()
					+ "\n\tResults directory: " + RESULTS_DIRECTORY.getAbsolutePath() + "\n\tFunction choice: "
					+ args[0].substring(1) + "\n\tInput file: " + inputApkFile.getAbsolutePath());

			if (args[0].equals("-tool1")) {
				final Answer answer = FakeAnalysisTool1.fakeAnalysis(inputApkFile, hash);
				saveAnswer(answer, new File(RESULTS_DIRECTORY, apkName + "_flows_result.xml"));
			} else if (args[0].equals("-tool2")) {
				final Answer answer = FakeAnalysisTool2.fakeAnalysis(inputApkFile, hash);
				saveAnswer(answer, new File(RESULTS_DIRECTORY, apkName + "_intents_result.xml"));
			} else if (args[0].equals("-tool3")) {
				final Answer answer = FakeAnalysisTool3.fakeAnalysis(hash);
				saveAnswer(answer, new File(RESULTS_DIRECTORY, apkName + "_sources_and_sinks_result.xml"));
			} else if (args[0].equals("-tool4")) {
				FakeAnalysisTool4.fakeSlice(hash);
			} else if (args[0].equals("-tool5")) {
				FakeAnalysisTool5.fakeArguments(hash);
			} else if (args[0].equals("-preprocessor")) {
				FakePreprocessor.fakePreprocess(hash);
			} else if (args[0].equals("-operator1")) {
				final Answer answer = FakeOperator1.fakeOperation(args[1]);
				saveAnswer(answer, new File(RESULTS_DIRECTORY, "operator1_result.xml"));
			} else if (args[0].equals("-operator2")) {
				FakeOperator2.fakeOperation();
			} else if (args[0].equals("-converter")) {
				final Answer answer = FakeConverter.fakeConversion(inputApkFile);
				saveAnswer(answer, new File(RESULTS_DIRECTORY, "converter_result.xml"));
			}

			System.out.println("\n* FakeTool finished *");
			System.exit(0);
		}
	}

	private static void saveAnswer(Answer answer, File answerFile) {
		final boolean isEmpty = Helper.isEmpty(answer);
		if (answer != null || !isEmpty) {
			System.out.println("Storing answer object in: " + answerFile.getAbsolutePath());
		} else if (isEmpty) {
			System.out.println("Storing empty answer object in: " + answerFile.getAbsolutePath());
		} else {
			System.out.println("Faketool execution failed! Answer is " + (answer == null ? "null" : "invalid") + "!");
			System.exit(1);
		}
		AnswerHandler.createXML(answer, answerFile);
	}
}