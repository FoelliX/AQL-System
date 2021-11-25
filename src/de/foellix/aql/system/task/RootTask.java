package de.foellix.aql.system.task;

import de.foellix.aql.system.TaskCreator;

public class RootTask extends Task {
	private static final long serialVersionUID = 1750929015569927552L;

	public RootTask(TaskCreator taskCreator) {
		super(taskCreator, null, null);
		setTaskAnswer(new RootTaskAnswer(this));
	}

	@Override
	public void refreshVariables(Task child) {
	}

	@Override
	public String getStatus() {
		final StringBuilder sb = new StringBuilder();
		for (final Task child : getChildren()) {
			if (!sb.isEmpty()) {
				sb.append(" / ");
			}
			sb.append(child.getStatus());
		}
		int failedTasks = 0;
		for (final Task child : getChildren(true)) {
			if (child.getStatus().equals(Task.STATUS_TEXT_FAILED)) {
				failedTasks++;
			}
		}
		if (failedTasks > 0) {
			sb.append(" (Tasks failed: " + failedTasks + ")");
		}
		return sb.toString();
	}
}