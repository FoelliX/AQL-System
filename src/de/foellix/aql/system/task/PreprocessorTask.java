package de.foellix.aql.system.task;

import java.io.File;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.Storage;

public class PreprocessorTask {
	Task parent;

	private final PreprocessorTaskInfo taskinfo;

	private App preprocessedApp;

	PreprocessorTask(Task parent) {
		this.parent = parent;
		this.taskinfo = (PreprocessorTaskInfo) parent.getTaskinfo();
	}

	public void execute() throws Exception {
		// Preprocessor
		init();
		if (this.preprocessedApp != null) {
			this.parent.getParent().preprocessingFinished(this.taskinfo, this.preprocessedApp);

			this.parent.successPart2(false);
		} else {
			Log.msg("Executing Preprocessor: " + this.taskinfo.getTool().getName() + " ("
					+ Helper.replaceVariables(this.taskinfo.getTool().getRun(), this.taskinfo, this.taskinfo.getApp())
					+ ")", Log.IMPORTANT);

			final String[] runCmd = this.taskinfo.getTool().getRun().split(" ");
			for (int i = 0; i < runCmd.length; i++) {
				runCmd[i] = Helper.replaceVariables(runCmd[i], this.taskinfo, this.taskinfo.getApp());
			}
			final String path = Helper.replaceVariables(this.taskinfo.getTool().getPath(), this.taskinfo,
					this.taskinfo.getApp());

			// TODO: Improve this solution (ApkCombiner)
			this.parent.setupProcess(new ProcessBuilder(runCmd).directory(new File(path)).start(), true);

			if (this.parent.getProcess().waitFor() == 0) {
				// Successful
				this.parent.successPart1(this.taskinfo.getTool().getName() + " successfully executed in %TIME%s. ("
						+ Helper.replaceVariables(this.taskinfo.getTool().getRun(), this.taskinfo,
								this.taskinfo.getApp())
						+ ")");

				final File result = Helper.findFileWithAsterisk(new File(Helper
						.replaceVariables(this.taskinfo.getTool().getResult(), this.taskinfo, this.taskinfo.getApp())));
				Helper.waitForResult("Preprocessed app was not generated. " + this.taskinfo.getTool().getName()
						+ " may have not finished properly.", result);

				finish(result);

				this.parent.successPart2(true);
			} else {
				// Failed
				this.parent.failed(this.taskinfo.getTool().getName() + " execution failed after %TIME%s! (" + Helper
						.replaceVariables(this.taskinfo.getTool().getRun(), this.taskinfo, this.taskinfo.getApp())
						+ ")");
			}
		}
	}

	void abort(Exception err) {
		try {
			if (!this.parent.isExecuted()) {
				Log.error(this.taskinfo.getTool().getName() + " execution aborted "
						+ (err instanceof TaskAbortedBeforeException ? "before " : "") + "after "
						+ this.parent.getTime() + "s! (" + Helper.replaceVariables(this.taskinfo.getTool().getRun(),
								this.taskinfo, this.taskinfo.getApp())
						+ ")");
			} else {
				err.printStackTrace();
				Log.error(this.taskinfo.getTool().getName() + "'s result unavailable after " + this.parent.getTime()
						+ "s! (" + Helper.replaceVariables(this.taskinfo.getTool().getRun(), this.taskinfo,
								this.taskinfo.getApp())
						+ ")");
			}
		} catch (final NullPointerException e) {
			if (!this.parent.isExecuted()) {
				Log.msg("Execution aborted!", Log.IMPORTANT);
			} else {
				Log.msg("Result unavailable!", Log.IMPORTANT);
			}
		}
	}

	private void init() {
		this.preprocessedApp = Storage.getInstance().load(this.taskinfo.getTool(), this.taskinfo.getApp());
	}

	private void finish(File result) {
		this.preprocessedApp = Helper.createApp(result);
		Storage.getInstance().store(this.taskinfo.getTool(), this.taskinfo.getApp(), this.preprocessedApp);
		this.parent.getParent().preprocessingFinished(this.taskinfo, this.preprocessedApp);

	}
}
