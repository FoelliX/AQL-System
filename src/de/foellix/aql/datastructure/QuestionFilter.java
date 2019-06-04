package de.foellix.aql.datastructure;

import de.foellix.aql.helper.Helper;

public class QuestionFilter extends Question {
	private static final long serialVersionUID = -2855045660219419728L;

	String name, value;
	int soi;

	public QuestionFilter(final String operator) {
		super(operator);

		this.name = null;
		this.value = null;

		this.soi = KeywordsAndConstants.QUESTION_TYPE_UNKNOWN;
	}

	@Override
	public String toString(final int level) {
		String indent = "";
		for (int i = 0; i < level; i++) {
			indent += "\t";
		}

		final StringBuilder sb = new StringBuilder();

		sb.append(indent + getOperator() + " [\n");
		sb.append(getChildren().get(0).toString(level + 1));

		if (this.name != null && !this.name.replaceAll(" ", "").equals("")) {
			sb.append("\n" + indent + "\t| " + this.name + " = ");
			if (this.value != null && !this.value.replaceAll(" ", "").equals("")) {
				sb.append(this.value);
			}
		}
		if (this.soi != KeywordsAndConstants.QUESTION_TYPE_UNKNOWN) {
			sb.append("\n" + indent + "\t| " + Helper.typeToSoi(this.soi));
		}

		sb.append("\n" + indent + "]");

		return sb.toString();
	}

	// Generated Getters & Setters
	public String getName() {
		return this.name;
	}

	public String getValue() {
		return this.value;
	}

	public int getSoi() {
		return this.soi;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public void setSoi(final int soi) {
		this.soi = soi;
	}
}