package de.foellix.aql.helper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.foellix.aql.Log;

public class AsteriskMap extends HashMap<String, Set<String>> {
	private static final long serialVersionUID = -3795387749726764473L;

	@Override
	public Set<String> get(Object statement) {
		final Set<String> value = super.get(statement);
		if (value != null) {
			return value;
		} else if (statement instanceof String) {
			final String statementStr = "<" + Helper.cutFromFirstToLast((String) statement, "<", ">") + ">";
			for (final String key : this.keySet()) {
				final String originalPattern = key;
				String pattern = originalPattern.replaceAll("\\(", "\\\\(").replaceAll("\\)", "\\\\)");
				pattern = pattern.replaceAll("\\[", "\\\\[").replaceAll("\\]", "\\\\]");
				pattern = pattern.replace("?", ".?").replace("*", ".*");
				if (statementStr.matches(pattern)) {
					return super.get(originalPattern);
				}
			}
		}
		return null;
	}

	public void load(File file) {
		// Read file
		try {
			final Set<String> all = new HashSet<>();
			final List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
			for (String line : lines) {
				if (!line.startsWith("%") && line.contains(" -> ")) {
					if (!line.contains("> -> ")) {
						line = line.replace(" -> ", " -### ");
						line = line.substring(0, line.lastIndexOf('>') + 1)
								+ line.substring(line.lastIndexOf(" -### "));
						line = line.replace(" -### ", " -> ");
					}

					final String[] parts = line.split(" -> ");
					final String stm = parts[0];
					final String features = parts[1];
					for (final String feature : features.replace(" ", "").split(",")) {
						all.add(feature);
						if (this.get(stm) == null) {
							this.put(stm, new HashSet<>());
						}
						this.get(stm).add(feature);
					}
				}
			}
			Log.msg("Loaded " + this.keySet().size() + " statements for " + all.size() + " distinct categories from \""
					+ file.getAbsolutePath() + "\".", Log.DEBUG_DETAILED);
		} catch (final IOException e) {
			Log.warning("Could not load features file: " + file.getAbsolutePath() + Log.getExceptionAppendix(e));
		}
	}
}