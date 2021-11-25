package de.foellix.aql.system;

import de.foellix.aql.Log;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.defaulttools.DefaultTool;
import de.foellix.aql.system.task.TaskRunner;

public class TaskTimer extends Thread {
	private final TaskRunner runner;
	private final long timeoutInSeconds;
	private boolean stop;
	private boolean timedOut;

	public TaskTimer(final TaskRunner runner, final long timeoutInSeconds) {
		super();

		this.runner = runner;
		this.timeoutInSeconds = timeoutInSeconds;
		this.stop = false;
		this.timedOut = false;
	}

	@Override
	public void run() {
		final long start = java.lang.System.currentTimeMillis();

		while (start + this.timeoutInSeconds * 1000 > java.lang.System.currentTimeMillis()) {
			try {
				// Wait
				Thread.sleep(500);
				if (this.stop) {
					if (!(this.runner.getTask().getTool() instanceof DefaultTool)) {
						Log.msg("Timeout-Timer stopped normally.", Log.DEBUG_DETAILED);
					}
					return;
				}
			} catch (final InterruptedException e) {
				Log.msg("Timeout-Timer stopped: " + e.getMessage(), Log.DEBUG_DETAILED);
				return;
			}
			if (this.runner.getTask().getTaskAnswer().isAnswered()) {
				// Stop
				return;
			}
		}

		// Expired
		this.timedOut = true;
		Log.warning("Timeout-Timer expired for " + Helper.getQualifiedName(this.runner.getTask().getTool()));
		// RunOnTimeOut
		this.runner.runOnAbort();
		this.runner.interrupt();
	}

	@Override
	public void interrupt() {
		this.stop = true;
	}

	public boolean isTimedOut() {
		return this.timedOut;
	}
}
