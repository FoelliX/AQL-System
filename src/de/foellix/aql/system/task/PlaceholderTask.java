package de.foellix.aql.system.task;

import de.foellix.aql.system.TaskCreator;

public class PlaceholderTask extends Task {
	private static final long serialVersionUID = -7012685255505155122L;

	private final PlaceholderTaskAnswer taskAnswer;

	public PlaceholderTask(TaskCreator taskCreator) {
		super(taskCreator, null, null);
		this.taskAnswer = new PlaceholderTaskAnswer(this);
	}

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public TaskAnswer getTaskAnswer() {
		return this.taskAnswer;
	}

	@Override
	public void refreshVariables(Task child) {
	}
}
