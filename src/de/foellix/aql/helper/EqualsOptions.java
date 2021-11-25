package de.foellix.aql.helper;

import java.util.HashMap;
import java.util.Map;

public class EqualsOptions {
	public static final EqualsOptions DEFAULT = new EqualsOptions();

	public static final int NULL_ALLOWED_ON_LEFT_HAND_SIDE = 0;
	public static final int PRECISELY_REFERENCE = 1;
	public static final int PRECISELY_TARGET = 2;
	public static final int IGNORE_APP = 3;
	public static final int GENERATE_HASH_IF_NOT_AVAILABLE = 4;
	public static final int CONSIDER_LINENUMBER = 5;
	public static final int DENY_NULL_LINENUMBER = 6;

	private Map<Integer, Boolean> mapping;

	public EqualsOptions() {
		this.mapping = new HashMap<>();
	}

	public boolean getOption(int option) {
		if (this.mapping.containsKey(option)) {
			return this.mapping.get(option);
		} else {
			// Per default all options are set to false
			return false;
		}
	}

	public EqualsOptions setOption(int option, boolean value) {
		if (this == DEFAULT) {
			final EqualsOptions newEO = new EqualsOptions();
			newEO.setOption(option, value);
			return newEO;
		} else {
			if (this.mapping.containsKey(option)) {
				this.mapping.replace(option, value);
			} else {
				this.mapping.put(option, value);
			}
			return this;
		}
	}

	public EqualsOptions copy() {
		final EqualsOptions outputOptions = new EqualsOptions();
		for (int i = 0; i <= 4; i++) {
			outputOptions.setOption(i, this.getOption(i));
		}
		return outputOptions;
	}
}