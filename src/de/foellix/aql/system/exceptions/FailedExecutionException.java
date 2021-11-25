package de.foellix.aql.system.exceptions;

public class FailedExecutionException extends CancelExecutionException {
	private static final long serialVersionUID = -8842935191854501412L;

	public FailedExecutionException() {
		super();
	}

	public FailedExecutionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FailedExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public FailedExecutionException(String message) {
		super(message);
	}

	public FailedExecutionException(Throwable cause) {
		super(cause);
	}
}