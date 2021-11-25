package de.foellix.aql.datastructure.query;

import java.io.Serializable;

public class QuestionString implements IStringOrQuestion, Serializable {
	private static final long serialVersionUID = -6639474813193228789L;

	private String string;

	public QuestionString(String string) {
		super();
		this.string = string;
	}

	public String getString() {
		return this.string;
	}

	public void setString(String string) {
		this.string = string;
	}

	@Override
	public String toStringInAnswer(boolean withQuotes) {
		if (withQuotes) {
			return toString();
		} else {
			return toString().substring(1, toString().length() - 1);
		}
	}

	@Override
	public String toStringInAnswer() {
		return toString();
	}

	@Override
	public String toString() {
		return "'" + this.string + "'";
	}

	@Override
	public boolean isComplete(boolean takeUnansweredIntoAccount) {
		return true;
	}
}