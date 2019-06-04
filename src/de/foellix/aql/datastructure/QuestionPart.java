package de.foellix.aql.datastructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.foellix.aql.helper.EqualsHelper;
import de.foellix.aql.helper.Helper;

public class QuestionPart implements IQuestionNode, Serializable {
	private static final long serialVersionUID = -3835466478917897792L;

	private int mode;
	private List<Reference> references;
	private final Map<Reference, List<String>> preprocessorMap;
	private final List<String> features, uses;

	public QuestionPart() {
		this.mode = 0;
		this.references = new ArrayList<>();
		this.preprocessorMap = new HashMap<>();
		this.features = new ArrayList<>();
		this.uses = new ArrayList<>();
	}

	@Override
	public String toString() {
		return toString(0);
	}

	@Override
	public String toString(final int level) {
		String indent = "";
		for (int i = 0; i < level; i++) {
			indent += "\t";
		}

		final StringBuilder sb = new StringBuilder();

		sb.append(indent + Helper.typeToSoi(this.mode));
		if (this.references.size() == 2) {
			sb.append(" FROM ");
		} else {
			sb.append(" IN ");
		}
		for (int i = 0; i < this.references.size(); i++) {
			if (i == 0) {
				sb.append(Helper.toString(this.references.get(i)));
			} else {
				sb.append(" TO " + Helper.toString(this.references.get(i)));
			}
			if (this.preprocessorMap.get(this.references.get(i)) != null) {
				sb.setLength(sb.length() - 1);
				for (final String keyword : this.preprocessorMap.get(this.references.get(i))) {
					sb.append(" | " + keyword);
				}
				sb.append(")");
			}
		}
		if (this.features != null && !this.features.isEmpty()) {
			sb.append(" FEATURING ");
			for (int o = 0; o < this.features.size(); o++) {
				sb.append("'" + this.features.get(o) + "'");
				if (o != this.features.size() - 1) {
					sb.append(", ");
				}
			}
		}
		if (this.uses != null && !this.uses.isEmpty()) {
			sb.append(" USES ");
			for (int o = 0; o < this.uses.size(); o++) {
				sb.append("'" + this.uses.get(o) + "'");
				if (o != this.uses.size() - 1) {
					sb.append(", ");
				}
			}
		}
		sb.append(" ?");

		return sb.toString();
	}

	@Override
	public String toRAW(boolean external) {
		final StringBuilder sb = new StringBuilder();
		if (external) {
			sb.append(String.valueOf(this.mode));
		}

		// References
		for (final Reference reference : this.references) {
			sb.append(Helper.toRAW(reference));
		}

		// Features
		if (this.features != null) {
			final List<String> sortedFeatures = new ArrayList<>(this.features);
			sortedFeatures.sort(String::compareToIgnoreCase);
			for (final String feature : sortedFeatures) {
				sb.append(feature);
			}
		}

		// Uses
		if (this.uses != null) {
			final List<String> sortedUses = new ArrayList<>(this.uses);
			sortedUses.sort(String::compareToIgnoreCase);
			for (final String use : sortedUses) {
				sb.append(use);
			}
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

	@Override
	public List<Reference> getAllReferences() {
		return this.references;
	}

	@Override
	public List<App> getAllApps(boolean equalsOnObjectLevel) {
		final List<App> apps = new ArrayList<>();
		for (final Reference reference : this.references) {
			boolean add = true;
			for (final App check : apps) {
				if ((equalsOnObjectLevel && reference.getApp().equals(check))
						|| (!equalsOnObjectLevel && EqualsHelper.equals(reference.getApp(), check))) {
					add = false;
					break;
				}
			}
			if (add) {
				apps.add(reference.getApp());
			}
		}
		return apps;
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

	public void setMode(final int mode) {
		this.mode = mode;
	}

	public void setReferences(final List<Reference> references) {
		this.references = references;
	}

	public List<String> getFeatures() {
		return this.features;
	}

	public List<String> getUses() {
		return this.uses;
	}
}
