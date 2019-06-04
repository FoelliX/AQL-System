package de.foellix.aql.system.task;

import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.helper.Helper;

public class TaskMemory {
	private static TaskMemory instance = new TaskMemory();

	private final List<String> abortedTasks;

	private TaskMemory() {
		this.abortedTasks = new ArrayList<>();
	}

	public static TaskMemory getInstance() {
		return instance;
	}

	public boolean contains(Task task) {
		return this.abortedTasks.contains(taskToString(task));
	}

	public void aborted(Task task) {
		String entry;
		try {
			entry = taskToString(task);
		} catch (final Exception e) {
			return;
		}
		this.abortedTasks.add(entry);
	}

	private String taskToString(Task task) {
		final TaskInfo taskinfo = task.getTaskinfo();
		if (taskinfo instanceof ToolTaskInfo) {
			final ToolTaskInfo t = (ToolTaskInfo) taskinfo;
			return Helper.getExecuteCommand(t) + "; " + task.getParent().getScheduler().getTimeout();
		} else if (taskinfo instanceof PreprocessorTaskInfo) {
			final PreprocessorTaskInfo t = (PreprocessorTaskInfo) taskinfo;
			return Helper.getExecuteCommand(t) + "; " + task.getParent().getScheduler().getTimeout();
		}
		return null;
	}
}
