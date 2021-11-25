package de.foellix.aql.system.exceptions;

public class TimeoutExecutionException extends CancelExecutionException {
	private static final long serialVersionUID = 5983237120013481226L;

	public TimeoutExecutionException() {
		super();
	}

	public TimeoutExecutionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TimeoutExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public TimeoutExecutionException(String message) {
		super(message);
	}

	public TimeoutExecutionException(Throwable cause) {
		super(cause);
	}
}