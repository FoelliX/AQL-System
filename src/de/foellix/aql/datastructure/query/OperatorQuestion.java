package de.foellix.aql.datastructure.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class OperatorQuestion extends Question {
	private static final long serialVersionUID = 2138086911624865494L;

	private String operator;

	private List<Question> questions;

	public OperatorQuestion(String operator) {
		this.operator = operator;
		this.questions = new ArrayList<>();
	}

	public String getOperator() {
		return this.operator;
	}

	public List<Question> getQuestions() {
		return this.questions;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}

	@Override
	public String toString(int level) {
		final StringBuilder sb = new StringBuilder(
				getIndent(level) + (this.withBrackets ? "{ " : "") + this.operator + " [\n");
		if (this.questions != null && !this.questions.isEmpty()) {
			for (final Iterator<Question> i = this.questions.iterator(); i.hasNext();) {
				sb.append(i.next().toString(level + 1));
				if (i.hasNext()) {
					sb.append(",\n");
				}
			}
		}
		sb.append("\n" + getIndent(level) + "] " + this.endingSymbol + (this.withBrackets ? " }" : ""));
		return sb.toString();
	}

	@Override
	public Collection<Question> getChildren(boolean recursively) {
		final Set<Question> children = new LinkedHashSet<>();

		for (final Question question : this.questions) {
			children.add(question);
			if (recursively) {
				children.addAll(question.getChildren(true));
			}
		}

		return children;
	}

	@Override
	public boolean replaceChild(Question childToReplace, IStringOrQuestion replacement) {
		if (replacement instanceof Question) {
			if (this.questions.remove(childToReplace)) {
				this.questions.add((Question) replacement);
				return true;
			}
		}
		return false;
	}

	@Override
	public Collection<QuestionReference> getAllReferences(boolean recursively) {
		final Set<QuestionReference> references = new LinkedHashSet<>();

		for (final Question child : getChildren(recursively)) {
			references.addAll(child.getAllReferences(recursively));
		}

		return references;
	}

	@Override
	public boolean isComplete(boolean takeUnansweredIntoAccount) {
		return super.isComplete(takeUnansweredIntoAccount);
	}
}