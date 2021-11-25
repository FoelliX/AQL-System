package de.foellix.aql.datastructure.query;

import java.io.Serializable;

public class StringOrQuestionPair implements Serializable {
	private static final long serialVersionUID = -8630338834390778141L;

	private IStringOrQuestion key;
	private IStringOrQuestion value;

	public StringOrQuestionPair() {
	}

	public StringOrQuestionPair(IStringOrQuestion key, IStringOrQuestion value) {
		this.key = key;
		this.value = value;
	}

	public IStringOrQuestion getKey() {
		return this.key;
	}

	public IStringOrQuestion getValue() {
		return this.value;
	}

	public void setKey(IStringOrQuestion key) {
		this.key = key;
	}

	public void setValue(IStringOrQuestion value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return this.key + " = " + this.value;
	}
}