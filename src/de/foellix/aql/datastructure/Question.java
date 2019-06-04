package de.foellix.aql.datastructure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.helper.EqualsHelper;

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
	public List<Reference> getAllReferences() {
		final List<Reference> references = new ArrayList<>();
		for (final IQuestionNode child : this.children) {
			for (final Reference reference : child.getAllReferences()) {
				boolean add = true;
				for (final Reference check : references) {
					if (EqualsHelper.equals(reference, check)) {
						add = false;
						break;
					}
				}
				if (add) {
					references.add(reference);
				}
			}
		}
		return references;
	}

	@Override
	public List<App> getAllApps(boolean equalsOnObjectLevel) {
		final List<App> apps = new ArrayList<>();
		for (final IQuestionNode child : this.children) {
			for (final App app : child.getAllApps(equalsOnObjectLevel)) {
				boolean add = true;
				for (final App check : apps) {
					if ((equalsOnObjectLevel && app.equals(check))
							|| (!equalsOnObjectLevel && EqualsHelper.equals(app, check))) {
						add = false;
						break;
					}
				}
				if (add) {
					apps.add(app);
				}
			}
		}
		return apps;
	}

	@Override
	public String toString() {
		return toString(0);
	}

	@Override
	public String toString(int level) {
		String indent = "";
		for (int i = 0; i < level; i++) {
			indent += "\t";
		}

		final StringBuilder sb = new StringBuilder();
		if (!this.operator.equals(KeywordsAndConstants.OPERATOR_COLLECTION)) {
			sb.append(indent + this.operator + " [\n");
		} else {
			level--;
		}

		boolean first = true;
		for (final IQuestionNode child : this.children) {
			if (first) {
				first = false;
				sb.append(child.toString(level + 1));
			} else {
				if (this.operator.equals(KeywordsAndConstants.OPERATOR_COLLECTION)) {
					sb.append("\n" + child.toString(level + 1));
				} else {
					sb.append(",\n" + child.toString(level + 1));
				}
			}
		}

		if (!this.operator.equals(KeywordsAndConstants.OPERATOR_COLLECTION)) {
			sb.append("\n" + indent + "]");
		}

		return sb.toString();
	}

	@Override
	public String toRAW(boolean external) {
		final StringBuilder sb = new StringBuilder();
		for (final IQuestionNode child : this.getChildren()) {
			sb.append(child.toRAW(external));
		}
		return sb.toString();
	}

}