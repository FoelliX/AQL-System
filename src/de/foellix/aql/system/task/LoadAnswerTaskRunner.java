package de.foellix.aql.system.task;

import java.io.File;
import java.util.List;

import de.foellix.aql.system.TaskScheduler;
import de.foellix.aql.system.exceptions.CancelExecutionException;
import de.foellix.aql.system.exceptions.FailedExecutionException;

public class LoadAnswerTaskRunner extends TaskRunner {
	public LoadAnswerTaskRunner(TaskScheduler parent, Task task) {
		super(parent, task);
	}

	@Override
	protected boolean runSpecificRunner() throws CancelExecutionException {
		final File fileToLoad = ((LoadAnswerTaskInfo) this.task.getTaskInfo()).getFileToLoad();
		if (fileToLoad.exists()) {
			this.task.getTaskAnswer().setAnswerFile(fileToLoad, true);
			return true;
		} else {
			throw new FailedExecutionException(
					"Could not load file: " + fileToLoad.getAbsolutePath() + " (does not exist)");
		}
	}

	@Override
	protected List<File> getExternalFiles() {
		// not required - never external
		return null;
	}

	@Override
	protected String getExternalQuery() {
		// not required - never external
		return null;
	}
}
