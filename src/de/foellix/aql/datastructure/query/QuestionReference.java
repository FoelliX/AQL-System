package de.foellix.aql.datastructure.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.storage.Storage;

public class QuestionReference implements IStringOrQuestion, Serializable {
	private static final long serialVersionUID = 4404315370639941792L;

	private IStringOrQuestion statement;
	private IStringOrQuestion method;
	private IStringOrQuestion classname;
	private IStringOrQuestion app;
	private int lineNumber;
	private Reference reference; // Only stores toReference result
	private List<IStringOrQuestion> preprocessorKeywords;
	private List<String> executedPreprocessorKeywords;

	public QuestionReference() {
		this.lineNumber = -1;
		this.preprocessorKeywords = new ArrayList<>();
		this.executedPreprocessorKeywords = new ArrayList<>();
	}

	public QuestionReference copy() {
		final QuestionReference copiedReference = new QuestionReference();
		copiedReference.setStatement(this.getStatement());
		copiedReference.setMethod(this.getMethod());
		copiedReference.setClassname(this.getClassname());
		return copiedReference;
	}

	public Collection<Question> getChildren(boolean recursively) {
		final Collection<Question> children = new ArrayList<>();
		if (this.statement instanceof Question) {
			children.add((Question) this.statement);
			if (recursively) {
				children.addAll(((Question) this.statement).getChildren(true));
			}
		}
		if (this.method instanceof Question) {
			children.add((Question) this.method);
			if (recursively) {
				children.addAll(((Question) this.method).getChildren(true));
			}
		}
		if (this.classname instanceof Question) {
			children.add((Question) this.classname);
			if (recursively) {
				children.addAll(((Question) this.classname).getChildren(true));
			}
		}
		if (this.app instanceof Question) {
			children.add((Question) this.app);
			if (recursively) {
				children.addAll(((Question) this.app).getChildren(true));
			}
		}
		if (this.preprocessorKeywords != null && !this.preprocessorKeywords.isEmpty()) {
			for (final IStringOrQuestion preprocessor : this.preprocessorKeywords) {
				if (preprocessor instanceof Question) {
					final Question preprocessorQuestion = (Question) preprocessor;
					children.add(preprocessorQuestion);
					if (recursively) {
						children.addAll(preprocessorQuestion.getChildren(true));
					}
				}
			}
		}
		return children;
	}

	@Override
	public boolean isComplete(boolean takeUnansweredIntoAccount) {
		// Preprocessors
		if (!this.preprocessorKeywords.isEmpty()) {
			for (final IStringOrQuestion keyword : this.preprocessorKeywords) {
				if (!keyword.isComplete(takeUnansweredIntoAccount)
						|| !this.executedPreprocessorKeywords.contains(keyword.toStringInAnswer(false))) {
					return false;
				}
			}
		}

		// Reference questions
		if (this.statement instanceof Question) {
			if (Storage.getInstance().getData().getTaskFromQuestionTaskMap((Question) this.statement) == null
					|| !Storage.getInstance().getData().getTaskFromQuestionTaskMap((Question) this.statement)
							.getTaskAnswer().isAnswered()) {
				return false;
			}
		}
		if (this.method instanceof Question) {
			if (Storage.getInstance().getData().getTaskFromQuestionTaskMap((Question) this.method) == null
					|| !Storage.getInstance().getData().getTaskFromQuestionTaskMap((Question) this.method)
							.getTaskAnswer().isAnswered()) {
				return false;
			}
		}
		if (this.classname instanceof Question) {
			if (Storage.getInstance().getData().getTaskFromQuestionTaskMap((Question) this.classname) == null
					|| !Storage.getInstance().getData().getTaskFromQuestionTaskMap((Question) this.classname)
							.getTaskAnswer().isAnswered()) {
				return false;
			}
		}
		if (this.app instanceof Question) {
			if (Storage.getInstance().getData().getTaskFromQuestionTaskMap((Question) this.app) == null
					|| !Storage.getInstance().getData().getTaskFromQuestionTaskMap((Question) this.app).getTaskAnswer()
							.isAnswered()) {
				return false;
			}
		}

		// Children
		if (this.getChildren(false).isEmpty()) {
			return true;
		} else {
			for (final Question child : getChildren(false)) {
				if (!child.isComplete(takeUnansweredIntoAccount)) {
					return false;
				}
			}
			return true;
		}
	}

	public Reference toReference() {
		if (!isComplete(true)) {
			return null;
		} else if (this.reference == null) {
			this.reference = new Reference();

			// Statement
			if (this.statement != null) {
				this.reference.setStatement(Helper.createStatement(this.statement.toStringInAnswer(false)));
				if (this.lineNumber > 0) {
					this.reference.getStatement().setLinenumber(this.lineNumber);
				}
			}

			// Method
			if (this.method != null) {
				this.reference.setMethod(this.method.toStringInAnswer(false));
			}

			// Class
			if (this.classname != null) {
				this.reference.setClassname(this.classname.toStringInAnswer(false));
			}

			// App
			if (this.app != null) {
				String app = new String(this.app.toStringInAnswer(false));
				if (app.equals("''") || app.equals("'/'") || app.equals("'\'") || app.equals("'*'")) {
					app = "'.'";
				} else if (app.endsWith("*'")) {
					app = app.substring(0, app.length() - 2) + "'";
				}
				this.reference.setApp(Helper.createApp(app));
			}
		}
		return this.reference;
	}

	@Override
	public String toStringInAnswer() {
		if (isComplete(true)) {
			return toString();
		} else {
			Log.msg("Answer not available, yet. Considering question:\n" + this.toString(), Log.DEBUG_DETAILED);
		}
		return null;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		if (this.statement != null) {
			sb.append("Statement(" + this.statement + (this.lineNumber > 0 ? "," + this.lineNumber : "") + ")->");
		}
		if (this.method != null) {
			sb.append("Method(" + this.method + ")->");
		}
		if (this.classname != null) {
			sb.append("Class(" + this.classname + ")->");
		}
		sb.append("App(" + this.app);
		if (this.preprocessorKeywords != null && !this.preprocessorKeywords.isEmpty()) {
			for (final IStringOrQuestion preprocessorKeyword : this.preprocessorKeywords) {
				sb.append(" | " + preprocessorKeyword);
			}
		}
		sb.append(")");
		return sb.toString();
	}

	public IStringOrQuestion getStatement() {
		return this.statement;
	}

	public IStringOrQuestion getMethod() {
		return this.method;
	}

	public IStringOrQuestion getClassname() {
		return this.classname;
	}

	public IStringOrQuestion getApp() {
		return this.app;
	}

	public int getLineNumber() {
		return this.lineNumber;
	}

	public List<IStringOrQuestion> getPreprocessorKeywords() {
		return this.preprocessorKeywords;
	}

	public List<String> getExecutedPreprocessorKeywords() {
		return this.executedPreprocessorKeywords;
	}

	public void setStatement(IStringOrQuestion statement) {
		this.statement = statement;
	}

	public void setMethod(IStringOrQuestion method) {
		this.method = method;
	}

	public void setClassname(IStringOrQuestion classname) {
		this.classname = classname;
	}

	public void setApp(IStringOrQuestion app) {
		this.app = app;
	}

	public void setLineNumber(int lineNumber) {
		this.lineNumber = lineNumber;
	}
}