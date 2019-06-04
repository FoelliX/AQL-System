package de.foellix.aql.system.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.foellix.aql.config.Tool;

public class TaskHooks {
	private final Map<Tool, List<ITaskHook>> hooks;

	public TaskHooks() {
		this.hooks = new HashMap<>();
	}

	public Map<Tool, List<ITaskHook>> getHooks() {
		return this.hooks;
	}
}
