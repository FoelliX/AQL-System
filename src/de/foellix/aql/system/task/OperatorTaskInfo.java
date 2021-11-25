package de.foellix.aql.system.task;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OperatorTaskInfo extends TaskInfo {
	private static final long serialVersionUID = 9196192391015453116L;

	public static final String ANSWERS = "%ANSWERS%";
	public static final String ANSWERSHASH = "%ANSWERSHASH%";
	public static final String ANSWERSHASH_MD5 = "%ANSWERSHASH_MD5%";
	public static final String ANSWERSHASH_SHA1 = "%ANSWERSHASH_SHA-1%";
	public static final String ANSWERSHASH_SHA256 = "%ANSWERSHASH_SHA-256%";

	public OperatorTaskInfo() {
		super();
	}

	@Override
	public Set<String> getAllVariableNames() {
		final Set<String> variableNames = new HashSet<>();
		variableNames.addAll(Arrays
				.asList(new String[] { ANSWERS, ANSWERSHASH, ANSWERSHASH_MD5, ANSWERSHASH_SHA1, ANSWERSHASH_SHA256 }));
		return variableNames;
	}

	@Override
	public Set<String> getAllFileVariableNames() {
		final Set<String> specificVariableNames = new HashSet<>();
		specificVariableNames.add(ANSWERS);
		return specificVariableNames;
	}
}
