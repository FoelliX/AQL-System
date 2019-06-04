package de.foellix.aql.system.task;

import java.io.File;

import de.foellix.aql.Log;
import de.foellix.aql.helper.Helper;

public class ExtraTask extends Thread {
	private final TaskInfo taskinfo;
	private final TaskStatus mode;
	private boolean done;

	public ExtraTask(TaskInfo taskinfo, TaskStatus mode) {
		this.taskinfo = taskinfo;
		this.mode = mode;
	}

	@Override
	public void run() {
		String runCmdTemp = null;
		try {
			if (this.mode == TaskStatus.STATUS_ENTRY) {
				runCmdTemp = this.taskinfo.getTool().getRunOnEntry();
			} else if (this.mode == TaskStatus.STATUS_ABORT) {
				runCmdTemp = this.taskinfo.getTool().getRunOnAbort();
			} else if (this.mode == TaskStatus.STATUS_FAIL) {
				runCmdTemp = this.taskinfo.getTool().getRunOnFail();
			} else if (this.mode == TaskStatus.STATUS_SUCCESS) {
				runCmdTemp = this.taskinfo.getTool().getRunOnSuccess();
			} else {
				runCmdTemp = this.taskinfo.getTool().getRunOnExit();
			}
			runCmdTemp = Helper.replaceVariables(runCmdTemp, this.taskinfo);
			final String[] runCmd = runCmdTemp.split(" ");

			final String path = Helper.replaceVariables(this.taskinfo.getTool().getPath(), this.taskinfo);
			final Process process = new ProcessBuilder(runCmd).directory(new File(path)).start();

			final ProcessWrapper processWrapper = new ProcessWrapper(process);
			processWrapper.waitFor();

			this.done = true;
		} catch (final Exception e) {
			if (runCmdTemp != null) {
				Log.warning("Run on event did not execute properly: " + runCmdTemp + " (" + e.getMessage() + ")");
			} else {
				Log.warning("Run on event did not execute properly! (" + e.getMessage() + ")");
				if (Log.logIt(Log.DEBUG_DETAILED)) {
					e.printStackTrace();
				}
			}
			this.done = true;
		}
	}

	public void runAndWait() {
		this.done = false;
		if ((this.mode == TaskStatus.STATUS_EXIT && this.taskinfo.getTool().getRunOnExit() != null
				&& !this.taskinfo.getTool().getRunOnExit().equals(""))
				|| (this.mode == TaskStatus.STATUS_ENTRY && this.taskinfo.getTool().getRunOnEntry() != null
						&& !this.taskinfo.getTool().getRunOnEntry().equals(""))
				|| (this.mode == TaskStatus.STATUS_ABORT && this.taskinfo.getTool().getRunOnAbort() != null
						&& !this.taskinfo.getTool().getRunOnAbort().equals(""))
				|| (this.mode == TaskStatus.STATUS_FAIL && this.taskinfo.getTool().getRunOnFail() != null
						&& !this.taskinfo.getTool().getRunOnFail().equals(""))
				|| (this.mode == TaskStatus.STATUS_SUCCESS && this.taskinfo.getTool().getRunOnSuccess() != null
						&& !this.taskinfo.getTool().getRunOnSuccess().equals(""))) {
			this.start();
			while (!this.done) {
				try {
					Thread.sleep(250);
				} catch (final InterruptedException e) {
					if (!this.done) {
						Log.error("Error occured while waiting for process to end: "
								+ Helper.toString(this.taskinfo.getTool()) + " (" + e.getMessage() + ")");
					}
				}
			}
		}
	}

	public TaskInfo getTaskinfo() {
		return this.taskinfo;
	}
}
