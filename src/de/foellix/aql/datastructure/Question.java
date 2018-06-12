package de.foellix.aql.datastructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Question implements IQuestionNode, Serializable {
	private static final long serialVersionUID = 4453131349077342896L;

	private String operator;

	private final List<IQuestionNode> children;

	public Question(final String operator) {
		this.operator = operator;
		this.children = new ArrayList<>();
	}

	public String getOperator() {
		return this.operator;
	}

	public void setOperator(final String operator) {
		this.operator = operator;
	}

	public void addChild(final IQuestionNode child) {
		this.children.add(child);
	}

	@Override
	public List<IQuestionNode> getChildren() {
		return this.children;
	}

	@Override
	public List<QuestionPart> getAllQuestionParts() {
		final List<QuestionPart> list = new ArrayList<>();
		for (final IQuestionNode node : this.children) {
			if (node instanceof QuestionPart) {
				list.add((QuestionPart) node);
			} else if (node instanceof Question) {
				list.addAll(((Question) node).getAllQuestionParts());
			}
		}
		return list;
	}

	@Override
	public List<PreviousQuestion> getAllPreviousQuestions() {
		final List<PreviousQuestion> list = new ArrayList<>();
		for (final IQuestionNode node : this.children) {
			if (node instanceof PreviousQuestion) {
				list.add((PreviousQuestion) node);
			} else if (node instanceof Question) {
				list.addAll(((Question) node).getAllPreviousQuestions());
			}
		}
		return list;
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

		sb.append(indent + this.operator + " [\n");

		for (final IQuestionNode node : this.getChildren()) {
			sb.append(node.toString(level + 1));
		}

		sb.append(indent + "]\n");

		return sb.toString();
	}

	@Override
	public String toRAW() {
		final StringBuilder sb = new StringBuilder();
		for (final IQuestionNode child : this.getChildren()) {
			sb.append(child.toRAW());
		}
		return sb.toString();
	}
}