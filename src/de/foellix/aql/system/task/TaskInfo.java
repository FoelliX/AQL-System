package de.foellix.aql.system.task;

import de.foellix.aql.config.Tool;
import de.foellix.aql.system.DefaultOperator;

public class TaskInfo {
	private Tool tool;
	private int pid;

	TaskInfo(final Tool tool) {
		this.tool = tool;
		this.pid = -1;
	}

	public int getMemoryUsage() {
		if (this.tool.getExecute() != null && !this.tool.isExternal()
				&& this.tool.getExecute().getMemoryPerInstance() != null) {
			return this.tool.getExecute().getMemoryPerInstance();
		} else {
			return 0;
		}
	}

	public void setMemoryUsage(int value) {
		if (!(this.tool instanceof DefaultOperator)) {
			this.tool.getExecute().setMemoryPerInstance(value);
		}
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
