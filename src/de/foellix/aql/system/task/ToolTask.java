package de.foellix.aql.system.task;

import java.io.File;

import de.foellix.aql.Log;
import de.foellix.aql.converter.ConverterRegistry;
import de.foellix.aql.converter.IConverter;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.Storage;

public class ToolTask {
	Task parent;

	private final ToolTaskInfo taskinfo;

	private Answer answer;

	ToolTask(Task parent) {
		this.parent = parent;
		this.taskinfo = (ToolTaskInfo) parent.getTaskinfo();
	}

	public void execute() throws Exception {
		// Tool
		init();
		if (this.answer != null) {
			this.parent.getParent().localAnswerAvailable(this.taskinfo.getQuestion(), this.answer);

			this.parent.successPart2(false);
		} else {
			Log.msg("Executing Tool: " + this.taskinfo.getTool().getName() + " (" + Helper.replaceVariables(
					this.taskinfo.getTool().getRun(), this.taskinfo, this.taskinfo.getQuestion()) + ")", Log.IMPORTANT);

			final String[] runCmd = this.taskinfo.getTool().getRun().split(" ");
			for (int i = 0; i < runCmd.length; i++) {
				runCmd[i] = Helper.replaceVariables(runCmd[i], this.taskinfo, this.taskinfo.getQuestion());
			}
			final String path = Helper.replaceVariables(this.taskinfo.getTool().getPath(), this.taskinfo,
					this.taskinfo.getQuestion());

			this.parent.setupProcess(new ProcessBuilder(runCmd).directory(new File(path)).start(), false);

			if (this.parent.getProcess().waitFor() == 0) {
				// Successful
				this.parent.successPart1(this.taskinfo.getTool().getName() + " successfully executed in %TIME%s. ("
						+ Helper.replaceVariables(this.taskinfo.getTool().getRun(), this.taskinfo,
								this.taskinfo.getQuestion())
						+ ")");

				final File result = Helper
						.findFileWithAsterisk(new File(Helper.replaceVariables(this.taskinfo.getTool().getResult(),
								this.taskinfo, this.taskinfo.getQuestion())));
				Helper.waitForResult("Result file was not generated. " + this.taskinfo.getTool().getName()
						+ " may have not finished properly.", result);

				finish(result);

				this.parent.successPart2(true);
			} else {
				// Failed
				this.parent.failed(this.taskinfo.getTool().getName() + " execution failed after %TIME%s! (" + Helper
						.replaceVariables(this.taskinfo.getTool().getRun(), this.taskinfo, this.taskinfo.getQuestion())
						+ ")");
			}
		}
	}

	void abort(Exception err) {
		try {
			if (!this.parent.isExecuted()) {
				Log.msg(this.taskinfo.getTool().getName()
						+ " execution aborted " + (err instanceof TaskAbortedBeforeException ? "before " : "")
						+ "after " + this.parent.getTime() + "s! (" + Helper.replaceVariables(
								this.taskinfo.getTool().getRun(), this.taskinfo, this.taskinfo.getQuestion())
						+ ")", Log.IMPORTANT);
			} else {
				Log.msg(this.taskinfo.getTool().getName() + "'s result conversion failed after " + this.parent.getTime()
						+ "s! (" + Helper.replaceVariables(this.taskinfo.getTool().getRun(), this.taskinfo,
								this.taskinfo.getQuestion())
						+ ")", Log.IMPORTANT);
			}
		} catch (final NullPointerException e) {
			if (!this.parent.isExecuted()) {
				Log.msg("Execution aborted!", Log.IMPORTANT);
			} else {
				Log.msg("Result conversion failed!", Log.IMPORTANT);
			}
		}
	}

	private void init() {
		this.answer = Storage.getInstance().load(this.taskinfo.getTool(), this.taskinfo.getQuestion(),
				this.parent.getParent().getScheduler().isAlwaysPreferLoading());
	}

	private void finish(File result) throws Exception {
		final IConverter converter = ConverterRegistry.getInstance().getConverter(this.taskinfo.getTool());
		this.answer = converter.parse(result, this.taskinfo);
		Storage.getInstance().store(this.taskinfo.getTool(), this.taskinfo.getQuestion(), this.answer);
		this.parent.getParent().localAnswerAvailable(this.taskinfo.getQuestion(), this.answer);
	}
}
