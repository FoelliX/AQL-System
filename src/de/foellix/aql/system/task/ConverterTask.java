package de.foellix.aql.system.task;

import de.foellix.aql.config.Tool;
import de.foellix.aql.system.TaskCreator;

public class ConverterTask extends Task {
	private static final long serialVersionUID = -6422103546283429985L;

	public ConverterTask(TaskCreator taskCreator, ConverterTaskInfo taskInfo, Tool tool) {
		super(taskCreator, taskInfo, tool);
	}

	@Override
	public String getHashableString() {
		if (getChildren().size() == 1) {
			final Task child = getChildren().iterator().next();
			if (child instanceof ToolTask) {
				return child.getHashableString();
			}
		}
		return super.getHashableString();
	}

	@Override
	public void refreshVariables(Task child) {
		this.taskInfo.setData(ConverterTaskInfo.RESULT_FILE, child.getTaskAnswer().getAnswerFile().getAbsolutePath());
		for (final String variable : child.getTaskInfo().getAllVariableNames()) {
			if ((child.getTaskInfo().getData(variable) != null) && (this.taskInfo.getData(variable) == null
					|| !this.taskInfo.getData(variable).equals(child.getTaskInfo().getData(variable)))) {
				this.taskInfo.setData(variable, child.getTaskInfo().getData(variable));
			}
		}
	}
}