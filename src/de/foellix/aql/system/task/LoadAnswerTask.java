package de.foellix.aql.system.task;

import java.io.File;

import de.foellix.aql.config.Tool;
import de.foellix.aql.system.TaskCreator;

public class LoadAnswerTask extends Task {
	private static final long serialVersionUID = 529065064338570645L;

	private static final File LOCAL_DIRECTORY = new File(".");

	public LoadAnswerTask(TaskCreator taskCreator, LoadAnswerTaskInfo taskInfo, Tool tool) {
		super(taskCreator, taskInfo, tool);
	}

	@Override
	public File getRunInPath(boolean useHashes) {
		return LOCAL_DIRECTORY;
	}

	@Override
	public String getRunCommand(boolean useHashes, boolean reportMissingVariables) {
		return "Load from file: " + this.getTaskAnswer().getAnswerFile().getAbsolutePath();
	}

	@Override
	public void refreshVariables(Task child) {
	}
}