package de.foellix.aql.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import de.foellix.aql.Log;
import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.datastructure.handler.ParseException;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.ToolTaskInfo;

public class ExternalConverter extends Thread implements IConverter {
	private final Tool converter;
	private File resultFile;
	private ToolTaskInfo taskInfo;

	private Answer answer;

	public ExternalConverter(Tool converter) {
		this.converter = converter;
	}

	@Override
	public void run() {
		try {
			Log.msg("Executing external Converter: " + this.converter.getName() + " ("
					+ Helper.replaceVariables(this.converter.getRun(), this.taskInfo, this.resultFile) + ")",
					Log.NORMAL);

			final String[] runCmd = Helper.replaceVariables(this.converter.getRun(), this.taskInfo, this.resultFile)
					.split(" ");
			final String path = Helper.replaceVariables(this.taskInfo.getTool().getPath(), this.taskInfo,
					this.resultFile);
			final Process process = new ProcessBuilder(runCmd).directory(new File(path)).start();

			if (Log.logIt(Log.DEBUG_DETAILED)) {
				String line;
				final BufferedReader input1 = new BufferedReader(new InputStreamReader(process.getInputStream()));
				while ((line = input1.readLine()) != null) {
					Log.msg("Process (output): " + line, Log.DEBUG_DETAILED);
				}
				input1.close();

				final BufferedReader input2 = new BufferedReader(new InputStreamReader(process.getErrorStream()));
				while ((line = input2.readLine()) != null) {
					Log.msg("Process (error): " + line, Log.DEBUG_DETAILED);
				}
				input2.close();
			} else {
				process.getErrorStream().close();
				process.getOutputStream().close();
			}

			if (process.waitFor() == 0) {
				// Successful
				final File result = Helper.findFileWithAsterisk(new File(Helper
						.replaceVariables(this.converter.getResult(), this.taskInfo, this.taskInfo.getQuestion())));
				Helper.waitForResult("Result file was not generated. " + this.converter.getName()
						+ " converter may have not finished properly.", result);

				this.answer = AnswerHandler.parseXML(result);
				Log.msg("External converter finished successfully: " + this.converter.getName() + " ("
						+ Helper.replaceVariables(this.converter.getRun(), this.taskInfo, this.resultFile) + ")",
						Log.NORMAL);
			} else {
				// Failed
				throw new ParseException("Process finished unsuccessfully.");
			}
		} catch (final Exception e) {
			Log.error("External converter:\n" + Helper.toString(this.converter)
					+ "\nexecution failed! Replying empty answer! (" + e.getMessage() + ")");
			this.answer = new Answer();
		}
	}

	@Override
	public Answer parse(File resultFile, ToolTaskInfo taskInfo) throws Exception {
		this.resultFile = resultFile;
		this.taskInfo = taskInfo;
		this.answer = null;
		this.start();
		while (this.answer == null) {
			try {
				Thread.sleep(250);
			} catch (final InterruptedException e) {
				if (this.answer == null) {
					Log.error("Error occured while waiting for converter process to end: "
							+ Helper.toString(this.converter) + " (" + e.getMessage() + ")");
					return new Answer();
				}
			}
		}
		return this.answer;
	}
}