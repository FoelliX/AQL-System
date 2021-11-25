package de.foellix.aql.system.defaulttools.analysistools;

import java.io.File;

import de.foellix.aql.system.task.ToolTask;

public abstract class DefaultSootAnalysisTool extends DefaultAnalysisTool {
	public DefaultSootAnalysisTool() {
		super();
		this.execute.setInstances(1);
	}

	@Override
	public abstract File applyAnalysisTool(ToolTask task);
}