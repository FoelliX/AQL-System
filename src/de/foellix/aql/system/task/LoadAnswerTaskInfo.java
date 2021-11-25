package de.foellix.aql.system.task;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class LoadAnswerTaskInfo extends TaskInfo {
	private static final long serialVersionUID = -2846869606666291763L;

	private static final Set<String> NO_VARS = new HashSet<>();

	private File fileToLoad;

	public LoadAnswerTaskInfo() {
		super();
	}

	public File getFileToLoad() {
		return this.fileToLoad;
	}

	public void setFileToLoad(File fileToLoad) {
		this.fileToLoad = fileToLoad;
	}

	@Override
	public Set<String> getAllVariableNames() {
		return NO_VARS;
	}

	@Override
	public Set<String> getAllFileVariableNames() {
		return NO_VARS;
	}
}