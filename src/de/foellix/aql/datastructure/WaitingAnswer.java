package de.foellix.aql.datastructure;

import de.foellix.aql.config.Tool;

public class WaitingAnswer extends Answer {
	private static final long serialVersionUID = -8679610156366962484L;

	private boolean hasBeenExecuted;

	private final Tool operator;
	private final Answer[] answers;

	private Answer answer;

	public WaitingAnswer(Tool operator, Answer[] answers) {
		this.hasBeenExecuted = false;

		this.operator = operator;
		this.answers = answers;
	}

	public boolean hasBeenExecuted() {
		return this.hasBeenExecuted;
	}

	public Tool getOperator() {
		return this.operator;
	}

	public Answer[] getAnswers() {
		return this.answers;
	}

	public Answer getAnswer() {
		return this.answer;
	}

	public void setAnswer(Answer answer) {
		this.answer = answer;
		this.hasBeenExecuted = true;
	}

	public void setHasBeenExecuted(boolean hasBeenExecuted) {
		this.hasBeenExecuted = hasBeenExecuted;
	}
}
