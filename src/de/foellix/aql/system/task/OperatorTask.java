package de.foellix.aql.system.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.DefaultOperator;
import de.foellix.aql.system.Storage;

public class OperatorTask {
	private static final File tempDirectory = new File("data/temp/");
	private static int num = 0;

	Task parent;

	private final OperatorTaskInfo taskinfo;
	private final List<File> tempAnswerFiles;

	private Answer answer;

	OperatorTask(Task parent) {
		this.parent = parent;
		this.taskinfo = (OperatorTaskInfo) parent.getTaskinfo();
		this.tempAnswerFiles = new ArrayList<>();
	}

	public void execute() throws Exception {
		// Operator
		if (this.taskinfo.getTool() instanceof DefaultOperator) {
			this.answer = DefaultOperator.applyOperator(this.taskinfo);
			this.parent.getParent().operatorExecuted(this.taskinfo.getWaitingAnswer(), this.answer);

			this.parent.successPart2(false);
		} else {
			init();

			Log.msg("Executing Operator: " + this.taskinfo.getTool().getName() + " ("
					+ Helper.replaceVariables(this.taskinfo.getTool().getRun(), this.taskinfo, this.tempAnswerFiles)
					+ ")", Log.IMPORTANT);

			final String[] runCmd = this.taskinfo.getTool().getRun().split(" ");
			for (int i = 0; i < runCmd.length; i++) {
				runCmd[i] = Helper.replaceVariables(runCmd[i], this.taskinfo, this.tempAnswerFiles);
			}
			final String path = Helper.replaceVariables(this.taskinfo.getTool().getPath(), this.taskinfo,
					this.tempAnswerFiles);

			this.parent.setupProcess(new ProcessBuilder(runCmd).directory(new File(path)).start(), false);

			if (this.parent.getProcess().waitFor() == 0) {
				// Successful
				this.parent.successPart1(this.taskinfo.getTool().getName() + " successfully executed in %TIME%s. ("
						+ Helper.replaceVariables(this.taskinfo.getTool().getRun(), this.taskinfo, this.tempAnswerFiles)
						+ ")");

				final File result = Helper.findFileWithAsterisk(new File(Helper
						.replaceVariables(this.taskinfo.getTool().getResult(), this.taskinfo, this.tempAnswerFiles)));
				Helper.waitForResult("Result file was not generated. " + this.taskinfo.getTool().getName()
						+ " may have not finished properly.", result);

				finish(result);

				this.parent.successPart2(true);
			} else {
				// Failed
				this.parent.failed(this.taskinfo.getTool().getName() + " execution failed after %TIME%s! ("
						+ Helper.replaceVariables(this.taskinfo.getTool().getRun(), this.taskinfo, this.tempAnswerFiles)
						+ ")");
			}
		}
	}

	void abort(Exception err) {
		deleteTempFiles();
		try {
			if (!this.parent.isExecuted()) {
				Log.msg(this.taskinfo.getTool().getName() + " execution aborted "
						+ (err instanceof TaskAbortedBeforeException ? "before " : "") + "after "
						+ this.parent.getTime() + "s! ("
						+ Helper.replaceVariables(this.taskinfo.getTool().getRun(), this.taskinfo, this.tempAnswerFiles)
						+ ")", Log.IMPORTANT);
			} else {
				Log.msg(this.taskinfo.getTool().getName() + "'s result parsing failed after " + this.parent.getTime()
						+ "s! ("
						+ Helper.replaceVariables(this.taskinfo.getTool().getRun(), this.taskinfo, this.tempAnswerFiles)
						+ ")", Log.IMPORTANT);
			}
		} catch (final NullPointerException e) {
			if (!this.parent.isExecuted()) {
				Log.msg("Execution aborted!", Log.IMPORTANT);
			} else {
				Log.msg("Result parsing failed!", Log.IMPORTANT);
			}
		}
	}

	private void init() {
		this.answer = null;
		tempDirectory.mkdirs();
		for (final Answer temp : this.taskinfo.getWaitingAnswer().getAnswers()) {
			File uniqueFile;
			do {
				uniqueFile = new File(tempDirectory, "temp_" + num++ + ".xml");
			} while (uniqueFile.exists());
			AnswerHandler.createXML(temp, uniqueFile);
			this.tempAnswerFiles.add(uniqueFile);
		}
	}

	private void finish(File result) {
		this.answer = AnswerHandler.parseXML(result);
		Storage.getInstance().store(this.taskinfo.getTool(), this.taskinfo.getQuestion(), this.answer);
		this.parent.getParent().operatorExecuted(this.taskinfo.getWaitingAnswer(), this.answer);
		deleteTempFiles();
	}

	private void deleteTempFiles() {
		for (final File tempFile : this.tempAnswerFiles) {
			if (tempFile.exists()) {
				tempFile.delete();
			}
		}
	}
}
