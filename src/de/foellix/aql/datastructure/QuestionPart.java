package de.foellix.aql.datastructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.foellix.aql.helper.Helper;

public class QuestionPart implements IQuestionNode, Serializable {
	private static final long serialVersionUID = -3835466478917897792L;

	private int mode;
	private List<Reference> references;
	private final Map<Reference, List<String>> preprocessorMap;
	private final List<String> features;

	public QuestionPart() {
		this.mode = 0;
		this.references = new ArrayList<>();
		this.preprocessorMap = new HashMap<>();
		this.features = new ArrayList<>();
	}

	@Override
	public String toString() {
		return toString(0);
	}

	@Override
	public String toString(final int level) {
		final StringBuilder sb = new StringBuilder();

		String indent = "";
		for (int i = 0; i < level; i++) {
			indent += "\t";
		}

		sb.append(indent + "QUESTION (\n" + indent + "\tMode: " + Helper.modeToString(this.mode) + "\n" + indent
				+ "\tReferences:\n");
		for (int i = 0; i < this.references.size(); i++) {
			sb.append(indent + "\t" + i + ".\n" + indent + "\t" + Helper.toString(this.references.get(i), level + 1));
			if (this.preprocessorMap.get(this.references.get(i)) != null) {
				boolean first = true;
				for (final String keyword : this.preprocessorMap.get(this.references.get(i))) {
					if (first) {
						sb.append(" <- " + keyword);
						first = false;
					} else {
						sb.append(", " + keyword);
					}
				}
			}
			sb.append("\n");
		}
		sb.append(indent + ")");
		if (!this.features.isEmpty()) {
			sb.append(" FEATURES: ");
			for (int o = 0; o < this.features.size(); o++) {
				sb.append(this.features.get(o));
				if (o != this.features.size() - 1) {
					sb.append(", ");
				}
			}
		}
		sb.append("\n");

		return sb.toString();
	}

	@Override
	public String toRAW() {
		final StringBuilder sb = new StringBuilder();
		for (final Reference reference : this.references) {
			sb.append(Helper.toRAW(reference));
		}
		return sb.toString();
	}

	@Override
	public List<IQuestionNode> getChildren() {
		return null;
	}

	@Override
	public List<QuestionPart> getAllQuestionParts() {
		final List<QuestionPart> temp = new ArrayList<>();
		temp.add(this);
		return temp;
	}

	@Override
	public List<PreviousQuestion> getAllPreviousQuestions() {
		return null;
	}

	// Add
	public void addReference(final Reference reference) {
		this.references.add(reference);
	}

	public void addPreprocessor(final Reference reference, final List<String> preprocessors) {
		this.preprocessorMap.put(reference, preprocessors);
	}

	// Remove
	public void removePreprocessor(final Reference reference, String preprocessorKeyword) {
		if (this.preprocessorMap.get(reference).size() == 1 || preprocessorKeyword == null) {
			this.preprocessorMap.remove(reference);
		} else {
			preprocessorKeyword = preprocessorKeyword.replaceAll("'", "");
			String remove = null;
			for (final String keyword : this.preprocessorMap.get(reference)) {
				if (keyword.replaceAll("'", "").equals(preprocessorKeyword)) {
					remove = keyword;
					break;
				}
			}
			if (remove != null) {
				this.preprocessorMap.get(reference).remove(remove);
			}
		}
	}

	// Custom Getter
	public List<String> getPreprocessor(final Reference reference) {
		return this.preprocessorMap.get(reference);
	}

	// Generated Getters & Setters
	public int getMode() {
		return this.mode;
	}

	public List<Reference> getReferences() {
		return this.references;
	}

	public void setMode(final int mode) {
		this.mode = mode;
	}

	public void setReferences(final List<Reference> references) {
		this.references = references;
	}

	public List<String> getFeatures() {
		return this.features;
	}
}
