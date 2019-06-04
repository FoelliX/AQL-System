package de.foellix.aql.faketool;

import java.io.File;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;

public class FakeTool {
	public static void main(String[] args) {
		java.lang.System.out.println("* FakeTool started *\n\nInput:\n\tApks directory: " + args[0]
				+ "\n\tResults directory:" + args[1] + "\n\tFunction choice:" + args[2] + "\n\tInput file:" + args[3]);

		final File apkDirectory = new File(args[0]);
		final File resultDirectory = new File(args[1]);

		if (args[2].equals("-tool1")) {
			final Answer answer = FakeAnalysisTool1.fakeAnalysis(args[3], apkDirectory);
			if (args[3].contains("Start1.apk")) {
				AnswerHandler.createXML(answer, new File(resultDirectory, "InterAppStart1_flows_result.xml"));
				AnswerHandler.createXML(answer, new File(resultDirectory, "InterAppStart1_preprocessed_flows_result.xml"));
			} else if (args[3].contains("End1.apk")) {
				AnswerHandler.createXML(answer, new File(resultDirectory, "InterAppEnd1_flows_result.xml"));
				AnswerHandler.createXML(answer, new File(resultDirectory, "InterAppEnd1_preprocessed_flows_result.xml"));
			}
		} else if (args[2].equals("-tool2")) {
			final Answer answer = FakeAnalysisTool2.fakeAnalysis(args[3], apkDirectory);
			if (args[3].contains("Start1.apk")) {
				AnswerHandler.createXML(answer, new File(resultDirectory, "InterAppStart1_intents_result.xml"));
			} else if (args[3].contains("End1.apk")) {
				AnswerHandler.createXML(answer, new File(resultDirectory, "InterAppEnd1_intents_result.xml"));
			}
		} else if (args[2].equals("-preprocessor")) {
			FakePreprocessor.fakePreprocess(args[3], resultDirectory);
		} else if (args[2].equals("-operator")) {
			final Answer answer = FakeOperator.fakeOperation(args[3], apkDirectory);
			AnswerHandler.createXML(answer, new File(resultDirectory, "operator_result.xml"));
		} else if (args[2].equals("-converter")) {
			final Answer answer = FakeConverter.fakeConversion(args[3]);
			AnswerHandler.createXML(answer, new File(resultDirectory, "converter_result.xml"));
		}

		java.lang.System.out.println("* FakeTool finished *");
	}
}