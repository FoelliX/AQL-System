package de.foellix.aql.system.task;

import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.Question;
import de.foellix.aql.datastructure.WaitingAnswer;

public class OperatorTaskInfo extends TaskInfo {
	private final Question question;
	private final WaitingAnswer answer;

	public OperatorTaskInfo(Question question, final Tool operator, final WaitingAnswer answer) {
		super(operator);
		this.question = question;
		this.answer = answer;
	}

	public WaitingAnswer getWaitingAnswer() {
		return this.answer;
	}

	public Question getQuestion() {
		return this.question;
	}
}
