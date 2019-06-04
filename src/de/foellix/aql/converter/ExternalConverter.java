package de.foellix.aql.converter;

import java.io.File;

import de.foellix.aql.Log;
import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.datastructure.handler.ParseException;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.ProcessWrapper;
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
			Log.msg("Executing third-party converter: " + this.converter.getName() + " ("
					+ Helper.replaceVariables(this.converter.getExecute().getRun(), this.taskInfo, this.resultFile)
					+ ")", Log.NORMAL);

			final String[] runCmd = Helper
					.replaceVariables(this.converter.getExecute().getRun(), this.taskInfo, this.resultFile).split(" ");
			final String path = Helper.replaceVariables(this.taskInfo.getTool().getPath(), this.taskInfo,
					this.resultFile);
			final Process process = new ProcessBuilder(runCmd).directory(new File(path)).start();

			final ProcessWrapper processWrapper = new ProcessWrapper(process);
			if (processWrapper.waitFor() == 0) {
				// Successful
				final File result = Helper
						.findFileWithAsterisk(new File(Helper.replaceVariables(this.converter.getExecute().getResult(),
								this.taskInfo, this.taskInfo.getQuestion())));
				Helper.waitForResult("Result file was not generated. " + this.converter.getName()
						+ " third-party converter may have not finished properly.", result);

				this.answer = AnswerHandler.parseXML(result);
				Log.msg("Third-party converter finished successfully: " + this.converter.getName() + " ("
						+ Helper.replaceVariables(this.converter.getExecute().getRun(), this.taskInfo, this.resultFile)
						+ ")", Log.NORMAL);
			} else {
				// Failed
				throw new ParseException("Process finished unsuccessfully.");
			}
		} catch (final Exception e) {
			Log.error("Third-party converter:\n" + Helper.toString(this.converter)
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
							+ Helper.toString(this.converter) + "\n(" + e.getMessage() + ")");
					return new Answer();
				}
			}
		}
		return this.answer;
	}

	public Tool getConverter() {
		return this.converter;
	}
}