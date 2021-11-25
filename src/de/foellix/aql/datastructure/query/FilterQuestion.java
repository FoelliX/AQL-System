package de.foellix.aql.datastructure.query;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import de.foellix.aql.system.defaulttools.operators.DefaultOperator;

public class FilterQuestion extends OperatorQuestion {
	private static final long serialVersionUID = -6492486171076759598L;

	private static String OPERATOR = DefaultOperator.OPERATOR_FILTER;

	private Question question;

	private StringOrQuestionPair filterPair;
	private QuestionReference filterReference;
	private String filterSubjectOfInterest;

	public FilterQuestion() {
		super(OPERATOR);
	}

	public Question getQuestion() {
		return this.question;
	}

	public StringOrQuestionPair getFilterPair() {
		return this.filterPair;
	}

	public QuestionReference getFilterReference() {
		return this.filterReference;
	}

	public String getFilterSubjectOfInterest() {
		return this.filterSubjectOfInterest;
	}

	public void setQuestion(Question question) {
		this.question = question;
	}

	public void setFilterPair(StringOrQuestionPair filterPair) {
		this.filterPair = filterPair;
	}

	public void setFilterReference(QuestionReference filterReference) {
		this.filterReference = filterReference;
	}

	public void setFilterSubjectOfInterest(String filterSubjectOfInterest) {
		this.filterSubjectOfInterest = filterSubjectOfInterest;
	}

	@Override
	public String toString(int level) {
		final StringBuilder sb = new StringBuilder(getIndent(level) + (this.withBrackets ? "{ " : "") + "FILTER [\n");
		sb.append(this.question.toString(level + 1));
		if (this.filterPair != null) {
			sb.append(" | " + this.filterPair);
		}
		if (this.filterReference != null) {
			sb.append(" | " + this.filterReference);
		}
		if (this.filterSubjectOfInterest != null) {
			sb.append(" | " + this.filterSubjectOfInterest);
		}
		sb.append("\n" + getIndent(level) + "] " + this.endingSymbol + (this.withBrackets ? " }" : ""));
		return sb.toString();
	}

	@Override
	public Collection<Question> getChildren(boolean recursively) {
		return getChildren(recursively, false);
	}

	public Collection<Question> getChildren(boolean recursively, boolean noFilterChilds) {
		final Set<Question> children = new HashSet<>();

		if (this.question != null) {
			children.add(this.question);
			if (recursively) {
				children.addAll(this.question.getChildren(true));
			}
		}

		if (!noFilterChilds) {
			// key-value filter
			if (this.filterPair != null) {
				if (this.filterPair.getKey() instanceof Question) {
					children.add((Question) this.filterPair.getKey());
					if (recursively) {
						children.addAll(((Question) this.filterPair.getKey()).getChildren(true));
					}
				}
				if (this.filterPair.getValue() instanceof Question) {
					children.add((Question) this.filterPair.getValue());
					if (recursively) {
						children.addAll(((Question) this.filterPair.getValue()).getChildren(true));
					}
				}
			}

			// reference filter
			if (this.filterReference != null) {
				children.addAll(this.filterReference.getChildren(recursively));
			}
		}

		return children;
	}

	@Override
	public boolean replaceChild(Question childToReplace, IStringOrQuestion replacement) {
		boolean replaced = false;
		if (replacement instanceof Question) {
			if (this.question == childToReplace) {
				this.question = (Question) replacement;
				replaced = true;
			}
			if (this.filterPair != null) {
				if (this.filterPair.getKey() == childToReplace) {
					this.filterPair.setKey(replacement);
					replaced = true;
				}
				if (this.filterPair.getValue() == childToReplace) {
					this.filterPair.setValue(replacement);
					replaced = true;
				}
			}
		}
		return replaced;
	}

	@Override
	public Collection<QuestionReference> getAllReferences(boolean recursively) {
		final Set<QuestionReference> references = new HashSet<>();

		if (this.filterReference != null) {
			references.add(this.filterReference);
		}

		if (recursively) {
			for (final Question child : getChildren(true)) {
				references.addAll(child.getAllReferences(true));
			}
		}

		return references;
	}

	@Override
	public boolean isComplete(boolean takeUnansweredIntoAccount) {
		// Key & Value questions
		if (takeUnansweredIntoAccount) {
			if (this.filterPair != null && (!this.filterPair.getKey().isComplete(takeUnansweredIntoAccount)
					|| !this.filterPair.getValue().isComplete(takeUnansweredIntoAccount))) {
				return false;
			}
		}

		// Reference questions
		if (takeUnansweredIntoAccount) {
			if (this.filterReference != null && !this.filterReference.isComplete(takeUnansweredIntoAccount)) {
				return false;
			}
		}

		return super.isComplete(takeUnansweredIntoAccount);
	}
}