package de.foellix.aql.system;

public class ProgressHandler {
	private final AQLSystem parent;

	private String status;
	private int running;
	private int done;
	private int max;

	ProgressHandler(AQLSystem parent) {
		this.parent = parent;

		this.status = "Ready";
		this.running = 0;
		this.done = 0;
		this.max = 0;
	}

	public boolean hasListener() {
		return !this.parent.getProgressListener().isEmpty();
	}

	public void progress(String status, int running, int done, int max) {
		// Refresh
		if (status != null) {
			this.status = status;
		}
		if (running > -1) {
			this.running = running;
		}
		if (done > -1) {
			this.done = done;
		}
		if (max > -1) {
			this.max = max;
		}

		// Call listener
		for (final IProgressChanged listener : this.parent.getProgressListener()) {
			listener.onProgressChanged(this.status, this.running, this.done, this.max);
		}
	}
}
