package de.foellix.aql.system.task.gui;

import java.io.Serializable;
import java.util.Set;

import de.foellix.aql.system.task.Task;

public class TaskTreeSnapshot implements Serializable {
	private static final long serialVersionUID = -4813081809199561700L;

	private Task rootTask;
	private Set<Task> runningTasks;

	public TaskTreeSnapshot(Task rootTask, Set<Task> runningTasks) {
		this.rootTask = rootTask;
		this.runningTasks = runningTasks;
	}

	public Task getRootTask() {
		return this.rootTask;
	}

	public Set<Task> getRunningTasks() {
		return this.runningTasks;
	}

	public void setRootTask(Task rootTask) {
		this.rootTask = rootTask;
	}

	public void setRunningTasks(Set<Task> runningTasks) {
		this.runningTasks = runningTasks;
	}
}