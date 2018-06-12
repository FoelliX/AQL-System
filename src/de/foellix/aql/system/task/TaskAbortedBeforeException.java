package de.foellix.aql.system.task;

public class TaskAbortedBeforeException extends Exception {
	private static final long serialVersionUID = -5534042424241300815L;

	public TaskAbortedBeforeException() {
		super();
	}

	public TaskAbortedBeforeException(String s) {
		super(s);
	}
}
