package de.foellix.aql.system.task;

import java.util.HashSet;
import java.util.Set;

public class ConverterTaskInfo extends ToolTaskInfo {
	private static final long serialVersionUID = 5600160695815905274L;

	public static final String RESULT_FILE = "%RESULT_FILE%";

	public ConverterTaskInfo() {
		super();
	}

	@Override
	public Set<String> getAllVariableNames() {
		final Set<String> variableNames = new HashSet<>();
		variableNames.add(RESULT_FILE);
		variableNames.addAll(super.getAllVariableNames());
		return variableNames;
	}

	@Override
	public Set<String> getAllFileVariableNames() {
		final Set<String> specificVariableNames = new HashSet<>();
		specificVariableNames.add(RESULT_FILE);
		specificVariableNames.addAll(super.getAllFileVariableNames());
		return specificVariableNames;
	}
}
