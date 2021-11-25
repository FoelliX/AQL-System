package de.foellix.aql.system.task;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ToolTaskInfo extends PreprocessorTaskInfo {
	private static final long serialVersionUID = -8864585012299334936L;

	public static final String STATEMENT_IN = "%STATEMENT_IN%";
	public static final String STATEMENT_FROM = "%STATEMENT_FROM%";
	public static final String STATEMENT_TO = "%STATEMENT_TO%";

	public static final String LINENUMBER_IN = "%LINENUMBER_IN%";
	public static final String LINENUMBER_FROM = "%LINENUMBER_FROM%";
	public static final String LINENUMBER_TO = "%LINENUMBER_TO%";

	public static final String METHOD_IN = "%METHOD_IN%";
	public static final String METHOD_FROM = "%METHOD_FROM%";
	public static final String METHOD_TO = "%METHOD_TO%";

	public static final String CLASS_IN = "%CLASS_IN%";
	public static final String CLASS_FROM = "%CLASS_FROM%";
	public static final String CLASS_TO = "%CLASS_TO%";

	public static final String APP_APK_IN = "%APP_APK_IN%";
	public static final String APP_APK_FROM = "%APP_APK_FROM%";
	public static final String APP_APK_TO = "%APP_APK_TO%";

	public static final String APP_APK_IN_FILENAME = "%APP_APK_IN_FILENAME%";
	public static final String APP_APK_FROM_FILENAME = "%APP_APK_FROM_FILENAME%";
	public static final String APP_APK_TO_FILENAME = "%APP_APK_TO_FILENAME%";

	public static final String APP_APK_IN_NAME = "%APP_APK_IN_NAME%";
	public static final String APP_APK_FROM_NAME = "%APP_APK_FROM_NAME%";
	public static final String APP_APK_TO_NAME = "%APP_APK_TO_NAME%";

	public static final String APP_APK_IN_PACKAGE = "%APP_APK_IN_PACKAGE%";
	public static final String APP_APK_FROM_PACKAGE = "%APP_APK_FROM_PACKAGE%";
	public static final String APP_APK_TO_PACKAGE = "%APP_APK_TO_PACKAGE%";

	public ToolTaskInfo() {
		super();
	}

	@Override
	public void setData(String variableName, String value) {
		switch (variableName) {
		case APP_APK:
			super.setData(APP_APK_IN, value);
			break;
		case APP_APK_FILENAME:
			super.setData(APP_APK_IN_FILENAME, value);
			break;
		case APP_APK_NAME:
			super.setData(APP_APK_IN_NAME, value);
			break;
		case APP_APK_PACKAGE:
			super.setData(APP_APK_IN_PACKAGE, value);
			break;
		}
		super.setData(variableName, value);
		if (variableName.equals(APP_APK_IN) || variableName.equals(APP_APK_FROM) || variableName.equals(APP_APK_TO)) {
			super.setData(APP_APK, getData(variableName));
		} else if (variableName.equals(APP_APK_IN_FILENAME) || variableName.equals(APP_APK_FROM_FILENAME)
				|| variableName.equals(APP_APK_TO_FILENAME)) {
			super.setData(APP_APK_FILENAME, getData(variableName));
		} else if (variableName.equals(APP_APK_IN_NAME) || variableName.equals(APP_APK_FROM_NAME)
				|| variableName.equals(APP_APK_TO_NAME)) {
			super.setData(APP_APK_NAME, getData(variableName));
		} else if (variableName.equals(APP_APK_IN_PACKAGE) || variableName.equals(APP_APK_FROM_PACKAGE)
				|| variableName.equals(APP_APK_TO_PACKAGE)) {
			super.setData(APP_APK_PACKAGE, getData(variableName));
		}
	}

	@Override
	public String getData(String variableName) {
		final String data = super.getData(variableName);
		if (data == null) {
			switch (variableName) {
			case APP_APK:
				if (super.getData(APP_APK_IN) != null && !super.getData(APP_APK_IN).isEmpty()) {
					return super.getData(APP_APK_IN);
				} else if (super.getData(APP_APK_FROM) != null && !super.getData(APP_APK_FROM).isEmpty()) {
					return super.getData(APP_APK_FROM);
				} else if (super.getData(APP_APK_TO) != null && !super.getData(APP_APK_TO).isEmpty()) {
					return super.getData(APP_APK_TO);
				}
			case APP_APK_FILENAME:
				if (super.getData(APP_APK_IN_FILENAME) != null && !super.getData(APP_APK_IN_FILENAME).isEmpty()) {
					return super.getData(APP_APK_IN_FILENAME);
				} else if (super.getData(APP_APK_FROM_FILENAME) != null
						&& !super.getData(APP_APK_FROM_FILENAME).isEmpty()) {
					return super.getData(APP_APK_FROM_FILENAME);
				} else if (super.getData(APP_APK_TO_FILENAME) != null
						&& !super.getData(APP_APK_TO_FILENAME).isEmpty()) {
					return super.getData(APP_APK_TO_FILENAME);
				}
			case APP_APK_NAME:
				if (super.getData(APP_APK_IN_NAME) != null && !super.getData(APP_APK_IN_NAME).isEmpty()) {
					return super.getData(APP_APK_IN_NAME);
				} else if (super.getData(APP_APK_FROM_NAME) != null && !super.getData(APP_APK_FROM_NAME).isEmpty()) {
					return super.getData(APP_APK_FROM_NAME);
				} else if (super.getData(APP_APK_TO_NAME) != null && !super.getData(APP_APK_TO_NAME).isEmpty()) {
					return super.getData(APP_APK_TO_NAME);
				}
			case APP_APK_PACKAGE:
				if (super.getData(APP_APK_IN_PACKAGE) != null && !super.getData(APP_APK_IN_PACKAGE).isEmpty()) {
					return super.getData(APP_APK_IN_PACKAGE);
				} else if (super.getData(APP_APK_FROM_PACKAGE) != null
						&& !super.getData(APP_APK_FROM_PACKAGE).isEmpty()) {
					return super.getData(APP_APK_FROM_PACKAGE);
				} else if (super.getData(APP_APK_TO_PACKAGE) != null && !super.getData(APP_APK_TO_PACKAGE).isEmpty()) {
					return super.getData(APP_APK_TO_PACKAGE);
				}
			}
		}
		return data;
	}

	@Override
	public Set<String> getAllVariableNames() {
		final Set<String> variableNames = new HashSet<>();
		variableNames.addAll(Arrays.asList(new String[] { STATEMENT_IN, STATEMENT_FROM, STATEMENT_TO, LINENUMBER_IN,
				LINENUMBER_FROM, LINENUMBER_TO, METHOD_IN, METHOD_FROM, METHOD_TO, CLASS_IN, CLASS_FROM, CLASS_TO,
				APP_APK_IN, APP_APK_FROM, APP_APK_TO, APP_APK_IN_FILENAME, APP_APK_FROM_FILENAME, APP_APK_TO_FILENAME,
				APP_APK_IN_NAME, APP_APK_FROM_NAME, APP_APK_TO_NAME, APP_APK_IN_PACKAGE, APP_APK_FROM_PACKAGE,
				APP_APK_TO_PACKAGE }));
		variableNames.addAll(super.getAllVariableNames());
		return variableNames;
	}

	@Override
	public Set<String> getAllFileVariableNames() {
		final Set<String> specificVariableNames = new HashSet<>();
		specificVariableNames.addAll(Arrays.asList(new String[] { APP_APK_IN, APP_APK_FROM, APP_APK_TO }));
		specificVariableNames.addAll(super.getAllFileVariableNames());
		return specificVariableNames;
	}
}
