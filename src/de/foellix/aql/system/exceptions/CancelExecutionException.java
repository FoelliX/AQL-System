package de.foellix.aql.system.exceptions;

public class CancelExecutionException extends Exception {
	private static final long serialVersionUID = -8842935191854501412L;

	public CancelExecutionException() {
		super();
	}

	public CancelExecutionException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public CancelExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public CancelExecutionException(String message) {
		super(message);
	}

	public CancelExecutionException(Throwable cause) {
		super(cause);
	}
}