package de.foellix.aql.system.task;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PreprocessorTaskInfo extends TaskInfo {
	private static final long serialVersionUID = -6385058634711829658L;

	public static final String APP_APK = "%APP_APK%";
	public static final String APP_APK_FILENAME = "%APP_APK_FILENAME%";
	public static final String APP_APK_NAME = "%APP_APK_NAME%";
	public static final String APP_APK_PACKAGE = "%APP_APK_PACKAGE%";

	public PreprocessorTaskInfo() {
		super();
	}

	@Override
	public Set<String> getAllVariableNames() {
		final Set<String> variableNames = new HashSet<>();
		variableNames.addAll(Arrays.asList(new String[] { APP_APK, APP_APK_FILENAME, APP_APK_NAME, APP_APK_PACKAGE }));
		return variableNames;
	}

	@Override
	public Set<String> getAllFileVariableNames() {
		final Set<String> specificVariableNames = new HashSet<>();
		specificVariableNames.add(APP_APK);
		return specificVariableNames;
	}
}
