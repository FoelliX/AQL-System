package de.foellix.aql.system;

import de.foellix.aql.system.task.Task;

public interface ITaskHook {
	public void execute(Task task);
}