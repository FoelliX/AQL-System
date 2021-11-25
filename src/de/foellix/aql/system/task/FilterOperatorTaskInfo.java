package de.foellix.aql.system.task;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class FilterOperatorTaskInfo extends OperatorTaskInfo {
	private static final long serialVersionUID = 1493683290715967935L;

	public static final String KEY = "%KEY%";
	public static final String VALUE = "%VALUE%";
	public static final String SUBJECT_OF_INTEREST = "%SOI%";
	public static final String REFERENCE_STATEMENT = "%FILTER_STATEMENT%";
	public static final String REFERENCE_LINENUMBER = "%FILTER_LINENUMBER%";
	public static final String REFERENCE_METHOD = "%FILTER_METHOD%";
	public static final String REFERENCE_CLASS = "%FILTER_CLASS%";
	public static final String REFERENCE_APK = "%FILTER_APK%";
	public static final String REFERENCE_APK_FILENAME = "%FILTER_APK_FILENAME%";
	public static final String REFERENCE_APK_NAME = "%FILTER_APK_NAME%";
	public static final String REFERENCE_APK_PACKAGE = "%FILTER_APK_PACKAGE%";

	public FilterOperatorTaskInfo() {
		super();
	}

	@Override
	public Set<String> getAllVariableNames() {
		final Set<String> variableNames = new HashSet<>();
		variableNames.addAll(Arrays.asList(new String[] { KEY, VALUE, SUBJECT_OF_INTEREST, REFERENCE_STATEMENT,
				REFERENCE_LINENUMBER, REFERENCE_METHOD, REFERENCE_CLASS, REFERENCE_APK, REFERENCE_APK_FILENAME,
				REFERENCE_APK_NAME, REFERENCE_APK_PACKAGE }));
		variableNames.addAll(super.getAllVariableNames());
		return variableNames;
	}

	@Override
	public Set<String> getAllFileVariableNames() {
		final Set<String> specificVariableNames = new HashSet<>();
		specificVariableNames.add(REFERENCE_APK);
		specificVariableNames.addAll(super.getAllFileVariableNames());
		return specificVariableNames;
	}
}
