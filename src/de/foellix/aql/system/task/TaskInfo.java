package de.foellix.aql.system.task;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.foellix.aql.Log;

public abstract class TaskInfo implements Serializable {
	private static final long serialVersionUID = 5522324864052949068L;

	public static final String VARIABLE_SYMBOL_FILE = "%";
	public static final String VARIABLE_SYMBOL_OTHER = "$";

	public static final String ANDROID_PLATFORMS = "%ANDROID_PLATFORMS%";
	public static final String ANDROID_BUILDTOOLS = "%ANDROID_BUILDTOOLS%";
	public static final String MEMORY = "%MEMORY%";
	public static final String PID = "%PID%";
	public static final String DATE = "%DATE%";

	private final Map<String, String> data;

	public TaskInfo() {
		this.data = new HashMap<>();
	}

	public String replaceVariables(String input) {
		String output = new String(input);
		for (final String variable : this.data.keySet()) {
			output = output.replace(variable, this.data.get(variable));
		}
		if (output.contains(VARIABLE_SYMBOL_FILE) || output.contains(VARIABLE_SYMBOL_OTHER)) {
			Log.msg("Probably not all variables could be replaced with actual values.\n\tInput: " + input
					+ "\n\tOutput: " + output, Log.DEBUG);
		}
		return output;
	}

	public void setData(String variableName, String value) {
		// Set or replace value
		if (this.data.containsKey(variableName)) {
			if (!this.data.get(variableName).equals(value)) {
				Log.msg("Replacing variable (" + variableName + ") value (\"" + this.data.get(variableName)
						+ "\") by: \"" + value + "\"", Log.DEBUG_DETAILED);
				this.data.replace(variableName, value);
			}
		} else {
			this.data.put(variableName, value);
			Log.msg("Setting variable (" + variableName + ") value to: \"" + value + "\"", Log.DEBUG_DETAILED);
		}
	}

	public String getData(String variableName) {
		return this.data.get(variableName);
	}

	public Set<String> getAllSetVariableNames() {
		return new HashSet<>(this.data.keySet());
	}

	public abstract Set<String> getAllVariableNames();

	public abstract Set<String> getAllFileVariableNames();

	@Override
	public String toString() {
		return toString(false);
	}

	public Object toStringNoPID() {
		return toString(true);
	}

	private String toString(boolean skipPID) {
		final StringBuilder sb = new StringBuilder("Variables [");
		for (final String var : this.data.keySet()) {
			if (skipPID && var.equals(TaskInfo.PID)) {
				continue;
			}
			sb.append("\n\t" + var + " = " + this.data.get(var));
		}
		sb.append("\n]");
		return sb.toString();
	}
}