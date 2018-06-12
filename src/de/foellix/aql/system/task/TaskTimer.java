package de.foellix.aql.system.task;

import de.foellix.aql.Log;

public class TaskTimer extends Thread {
	private final Task parent;
	private final long timeout;

	public TaskTimer(final Task parent, final long timeout) {
		super();

		this.parent = parent;
		this.timeout = timeout;
	}

	@Override
	public void run() {
		final long start = java.lang.System.currentTimeMillis();

		try {
			while (start + this.timeout * 1000 > java.lang.System.currentTimeMillis()) {
				Thread.sleep(500);
				if (this.parent.isDone()) {
					return;
				}
			}
			this.parent.interrupt();
		} catch (final InterruptedException e) {
			Log.msg("Timer stopped: " + e.getMessage(), Log.DEBUG_DETAILED);
		}
	}
}
