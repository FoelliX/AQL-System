package de.foellix.aql.system.defaulttools.analysistools;

import java.io.File;

import de.foellix.aql.Properties;
import de.foellix.aql.system.defaulttools.DefaultTool;
import de.foellix.aql.system.task.ToolTask;
import de.foellix.aql.system.task.ToolTaskInfo;

public abstract class DefaultAnalysisTool extends DefaultTool {
	public DefaultAnalysisTool() {
		super();
		this.execute.setRun(this.getClass().getSimpleName() + " (" + Properties.info().VERSION + ") "
				+ ToolTaskInfo.APP_APK_FROM + ", " + ToolTaskInfo.APP_APK_FROM_FILENAME + ", "
				+ ToolTaskInfo.APP_APK_FROM_NAME + ", " + ToolTaskInfo.APP_APK_FROM_PACKAGE + ", "
				+ ToolTaskInfo.APP_APK_IN + ", " + ToolTaskInfo.APP_APK_IN_FILENAME + ", "
				+ ToolTaskInfo.APP_APK_IN_NAME + ", " + ToolTaskInfo.APP_APK_IN_PACKAGE + ", " + ToolTaskInfo.APP_APK_TO
				+ ", " + ToolTaskInfo.APP_APK_TO_FILENAME + ", " + ToolTaskInfo.APP_APK_TO_NAME + ", "
				+ ToolTaskInfo.APP_APK_TO_PACKAGE + ", " + ToolTaskInfo.CLASS_FROM + ", " + ToolTaskInfo.CLASS_IN + ", "
				+ ToolTaskInfo.CLASS_TO + ", " + ToolTaskInfo.LINENUMBER_FROM + ", " + ToolTaskInfo.LINENUMBER_IN + ", "
				+ ToolTaskInfo.LINENUMBER_TO + ", " + ToolTaskInfo.METHOD_FROM + ", " + ToolTaskInfo.METHOD_IN + ", "
				+ ToolTaskInfo.METHOD_TO + ", " + ToolTaskInfo.STATEMENT_FROM + ", " + ToolTaskInfo.STATEMENT_IN + ", "
				+ ToolTaskInfo.STATEMENT_TO + ", " + ToolTaskInfo.APP_APK + ", " + ToolTaskInfo.APP_APK_FILENAME + ", "
				+ ToolTaskInfo.APP_APK_NAME + ", " + ToolTaskInfo.APP_APK_PACKAGE);
	}

	public abstract File applyAnalysisTool(ToolTask task);
}