package de.foellix.aql.system.exceptions;

public class FeedbackAnswerException extends CancelExecutionException {
	private static final long serialVersionUID = 5983237120013481226L;

	public FeedbackAnswerException() {
		super();
	}

	public FeedbackAnswerException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public FeedbackAnswerException(String message, Throwable cause) {
		super(message, cause);
	}

	public FeedbackAnswerException(String message) {
		super(message);
	}

	public FeedbackAnswerException(Throwable cause) {
		super(cause);
	}
}