package de.foellix.aql.datastructure.query;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.storage.Storage;

public class DefaultQuestion extends Question {
	private static final long serialVersionUID = 476906681815651573L;

	private int subjectOfInterest;

	private QuestionReference in;
	private QuestionReference from;
	private QuestionReference to;

	private List<IStringOrQuestion> features;
	private List<IStringOrQuestion> uses;
	private Stack<StringOrQuestionPair> withs;

	public DefaultQuestion() {
	}

	public DefaultQuestion copy() {
		final DefaultQuestion copy = new DefaultQuestion();
		super.copy(copy);
		copy.setSubjectOfInterest(this.subjectOfInterest);
		copy.setIn(this.in);
		copy.setFrom(this.from);
		copy.setTo(this.to);
		copy.setFeatures(this.features);
		copy.setUses(this.uses);
		copy.setWiths(this.withs);
		return copy;
	}

	public int getSubjectOfInterest() {
		return this.subjectOfInterest;
	}

	public QuestionReference getIn() {
		if (this.in == null && this.from != null) {
			return this.from;
		} else {
			return this.in;
		}
	}

	public QuestionReference getFrom() {
		return this.from;
	}

	public QuestionReference getTo() {
		return this.to;
	}

	public List<IStringOrQuestion> getFeatures() {
		return this.features;
	}

	public List<IStringOrQuestion> getUses() {
		return this.uses;
	}

	public Stack<StringOrQuestionPair> getWiths() {
		return this.withs;
	}

	public void setSubjectOfInterest(String subjectOfInterest) {
		this.subjectOfInterest = Helper.soiToType(subjectOfInterest);
	}

	public void setSubjectOfInterest(int subjectOfInterest) {
		this.subjectOfInterest = subjectOfInterest;
	}

	public void setIn(QuestionReference in) {
		this.in = in;
	}

	public void setFrom(QuestionReference from) {
		this.from = from;
	}

	public void setTo(QuestionReference to) {
		this.to = to;
	}

	public void setFeatures(List<IStringOrQuestion> features) {
		this.features = features;
	}

	public void setUses(List<IStringOrQuestion> uses) {
		this.uses = uses;
	}

	public void setWiths(Stack<StringOrQuestionPair> withs) {
		this.withs = withs;
	}

	@Override
	public String toString(int level) {
		final StringBuilder sb = new StringBuilder(
				getIndent(level) + (this.withBrackets ? "{ " : "") + Helper.typeToSoi(this.subjectOfInterest));
		if (this.in != null) {
			sb.append(" IN ");
			sb.append(this.in);
		} else {
			if (this.from != null) {
				sb.append(" FROM ");
				sb.append(this.from);
			}
			if (this.to != null) {
				sb.append(" TO ");
				sb.append(this.to);
			}
		}
		if (this.features != null && !this.features.isEmpty()) {
			sb.append(" FEATURING ");
			for (final Iterator<IStringOrQuestion> i = this.features.iterator(); i.hasNext();) {
				final IStringOrQuestion next = i.next();
				sb.append(next);
				if (i.hasNext()) {
					sb.append(", ");
				}
			}
		}
		if (this.uses != null && !this.uses.isEmpty()) {
			sb.append(" USES ");
			for (final Iterator<IStringOrQuestion> i = this.uses.iterator(); i.hasNext();) {
				final IStringOrQuestion next = i.next();
				sb.append(next);
				if (i.hasNext()) {
					sb.append(", ");
				}
			}
		}
		if (this.withs != null && !this.withs.isEmpty()) {
			sb.append(" WITH ");
			for (final Iterator<StringOrQuestionPair> i = this.withs.iterator(); i.hasNext();) {
				final StringOrQuestionPair next = i.next();
				sb.append(next);
				if (i.hasNext()) {
					sb.append(", ");
				}
			}
		}
		sb.append((sb.charAt(sb.length() - 1) == '\n' || sb.charAt(sb.length() - 1) == '\t' ? "" : " ")
				+ this.endingSymbol + (this.withBrackets ? " }" : ""));
		return sb.toString();
	}

	@Override
	public Set<Question> getChildren(boolean recursively) {
		final Set<Question> children = new HashSet<>();

		if (this.features != null && !this.features.isEmpty()) {
			for (final IStringOrQuestion feature : this.features) {
				if (feature instanceof Question) {
					final Question featureQuestion = (Question) feature;
					children.add(featureQuestion);
					if (recursively) {
						children.addAll(featureQuestion.getChildren(true));
					}
				}
			}
		}
		if (this.uses != null && !this.uses.isEmpty()) {
			for (final IStringOrQuestion use : this.uses) {
				if (use instanceof Question) {
					final Question useQuestion = (Question) use;
					children.add(useQuestion);
					if (recursively) {
						children.addAll(useQuestion.getChildren(true));
					}
				}
			}
		}
		if (this.withs != null && !this.withs.isEmpty()) {
			for (final StringOrQuestionPair with : this.withs) {
				if (with.getKey() instanceof Question) {
					final Question withQuestion = (Question) with.getKey();
					children.add(withQuestion);
					if (recursively) {
						children.addAll(withQuestion.getChildren(true));
					}
				}
				if (with.getValue() instanceof Question) {
					final Question withQuestion = (Question) with.getValue();
					children.add(withQuestion);
					if (recursively) {
						children.addAll(withQuestion.getChildren(true));
					}
				}
			}
		}
		if (this.in != null) {
			children.addAll(this.in.getChildren(recursively));
		}
		if (this.from != null) {
			children.addAll(this.from.getChildren(recursively));
		}
		if (this.to != null) {
			children.addAll(this.to.getChildren(recursively));
		}

		return children;
	}

	@Override
	public boolean replaceChild(Question childToReplace, IStringOrQuestion replacement) {
		// features
		if (this.features != null) {
			if (this.features.remove(childToReplace)) {
				this.features.add(replacement);
				return true;
			}
		}

		// uses
		if (this.uses != null) {
			if (this.uses.remove(childToReplace)) {
				this.uses.add(replacement);
				return true;
			}
		}

		// withs
		if (this.withs != null && !this.withs.isEmpty()) {
			for (final StringOrQuestionPair with : this.withs) {
				if (with.getKey() == childToReplace) {
					with.setKey(replacement);
					return true;
				}
				if (with.getValue() == childToReplace) {
					with.setValue(replacement);
					return true;
				}
			}
		}

		// in
		if (this.in != null) {
			if (this.in.getApp() == childToReplace) {
				this.in.setApp(replacement);
				return true;
			}
			if (this.in.getClassname() == childToReplace) {
				this.in.setClassname(replacement);
				return true;
			}
			if (this.in.getMethod() == childToReplace) {
				this.in.setMethod(replacement);
				return true;
			}
			if (this.in.getStatement() == childToReplace) {
				this.in.setStatement(replacement);
				return true;
			}
		}

		// from
		if (this.from != null) {
			if (this.from.getApp() == childToReplace) {
				this.from.setApp(replacement);
				return true;
			}
			if (this.from.getClassname() == childToReplace) {
				this.from.setClassname(replacement);
				return true;
			}
			if (this.from.getMethod() == childToReplace) {
				this.from.setMethod(replacement);
				return true;
			}
			if (this.from.getStatement() == childToReplace) {
				this.from.setStatement(replacement);
				return true;
			}
		}

		// to
		if (this.to != null) {
			if (this.to.getApp() == childToReplace) {
				this.to.setApp(replacement);
				return true;
			}
			if (this.to.getClassname() == childToReplace) {
				this.to.setClassname(replacement);
				return true;
			}
			if (this.to.getMethod() == childToReplace) {
				this.to.setMethod(replacement);
				return true;
			}
			if (this.to.getStatement() == childToReplace) {
				this.to.setStatement(replacement);
				return true;
			}
		}

		return false;
	}

	@Override
	public Collection<QuestionReference> getAllReferences(boolean recursively) {
		final Set<QuestionReference> references = new LinkedHashSet<>();

		if (this.in != null) {
			references.add(this.in);
		}
		if (this.from != null) {
			references.add(this.from);
		}
		if (this.to != null) {
			references.add(this.to);
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
		// Features
		if (this.features != null) {
			for (final IStringOrQuestion feature : this.features) {
				if (feature instanceof Question) {
					if (!feature.isComplete(takeUnansweredIntoAccount)
							|| Storage.getInstance().getData().getTaskFromQuestionTaskMap((Question) feature) == null
							|| !Storage.getInstance().getData().getTaskFromQuestionTaskMap((Question) feature)
									.getTaskAnswer().isAnswered()) {
						return false;
					}
				}
			}
		}

		return super.isComplete(takeUnansweredIntoAccount);
	}
}