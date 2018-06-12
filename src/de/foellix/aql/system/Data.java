package de.foellix.aql.system;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.foellix.aql.datastructure.IQuestionNode;
import de.foellix.aql.helper.BiMap;

public class Data implements Serializable {
	private static final long serialVersionUID = 867876111213719900L;

	private final BiMap<String, List<IQuestionNode>> data;
	private final Map<String, String> mayFitHashes;
	private final Map<String, String> mayFitTool;

	Data() {
		this.data = new BiMap<>();
		this.mayFitHashes = new HashMap<>();
		this.mayFitTool = new HashMap<>();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();

		for (final String hash : this.data.keySet()) {
			sb.append("--- " + hash + " ---\n");
			final int i = 1;
			for (final IQuestionNode question : this.data.get(hash)) {
				sb.append(i + ":\n" + question.toString() + "\n");
			}
		}

		return sb.toString();
	}

	// Generated Getters
	public BiMap<String, List<IQuestionNode>> getData() {
		return this.data;
	}

	public Map<String, String> getMayFitHashes() {
		return this.mayFitHashes;
	}

	public Map<String, String> getMayFitTool() {
		return this.mayFitTool;
	}
}
