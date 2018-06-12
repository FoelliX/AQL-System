package de.foellix.aql.system.task;

import de.foellix.aql.config.Tool;

public class TaskInfo {
	private Tool tool;
	private int pid;

	TaskInfo(final Tool tool) {
		this.tool = tool;
		this.pid = -1;
	}

	public int getMemoryUsage() {
		return this.tool.getMemoryPerInstance();
	}

	public void setMemoryUsage(int value) {
		this.tool.setMemoryPerInstance(value);
	}

	public Tool getTool() {
		return this.tool;
	}

	public void setTool(Tool tool) {
		this.tool = tool;
	}

	public int getPID() {
		return this.pid;
	}

	public void setPID(int value) {
		this.pid = value;
	}
}
