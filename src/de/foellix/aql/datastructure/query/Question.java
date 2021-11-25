package de.foellix.aql.datastructure.query;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.HashHelper;
import de.foellix.aql.system.storage.Storage;
import de.foellix.aql.system.task.TaskAnswer;

public abstract class Question implements IStringOrQuestion, Serializable {
	public static final int QUESTION_TYPE_UNKNOWN = -1;
	public static final int QUESTION_TYPE_FLOWS = 0;
	public static final int QUESTION_TYPE_PERMISSIONS = 1;
	public static final int QUESTION_TYPE_INTENTS = 2;
	public static final int QUESTION_TYPE_INTENTFILTER = 3;
	public static final int QUESTION_TYPE_INTENTSOURCES = 4;
	public static final int QUESTION_TYPE_INTENTSINKS = 5;
	public static final int QUESTION_TYPE_SLICE = 6;
	public static final int QUESTION_TYPE_SOURCES = 7;
	public static final int QUESTION_TYPE_SINKS = 8;
	public static final int QUESTION_TYPE_ARGUMENTS = 9;

	public static final char ENDING_SYMBOL_AQL = '?';
	public static final char ENDING_SYMBOL_FILE = '!';
	public static final char ENDING_SYMBOL_RAW = '.';

	private static final long serialVersionUID = -5772254649214549993L;

	// Empty sets
	protected static final Set<Question> CHILDREN_EMPTY = new LinkedHashSet<>();
	protected static final Set<QuestionReference> REFERENCES_EMPTY = new LinkedHashSet<>();

	private Question parent;
	protected char endingSymbol;
	protected boolean withBrackets;

	public Question() {
		this.parent = null;
		this.endingSymbol = '*';
		this.withBrackets = false;
	}

	public void copy(Question copy) {
		copy.setParent(this.getParent());
		copy.setEndingSymbol(this.getEndingSymbol());
		copy.setWithBrackets(this.isWithBrackets());
	}

	@Override
	public boolean isComplete(boolean takeUnansweredIntoAccount) {
		if (takeUnansweredIntoAccount) {
			// File or String queries
			if (Storage.getInstance().getData().getTaskFromQuestionTaskMap(this) == null
					|| !Storage.getInstance().getData().getTaskFromQuestionTaskMap(this).getTaskAnswer().isAnswered()) {
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

	public char getEndingSymbol() {
		return this.endingSymbol;
	}

	public boolean isWithBrackets() {
		return this.withBrackets;
	}

	public void setEndingSymbol(char endingSymbol) {
		this.endingSymbol = endingSymbol;
	}

	public void setWithBrackets(boolean withBrackets) {
		this.withBrackets = withBrackets;
	}

	public Question getParent() {
		return this.parent;
	}

	public void setParent(Question parent) {
		this.parent = parent;
	}

	protected String getIndent(int level) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < level; i++) {
			sb.append("\t");
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toStringInAnswer() {
		if (this.isComplete(true)) {
			final TaskAnswer ta = Storage.getInstance().getData().getTaskFromQuestionTaskMap(this).getTaskAnswer();
			if (ta.getType() == TaskAnswer.ANSWER_TYPE_RAW) {
				return (String) ta.getAnswer();
			} else if (ta.getType() == TaskAnswer.ANSWER_TYPE_FILE) {
				return ((File) ta.getAnswer()).getAbsolutePath();
			} else {
				Log.error("Cannot use answer as String considering following question:\n" + this.toString());
			}
		} else {
			Log.msg("Answer not available, yet. Considering question:\n" + this.toString(), Log.DEBUG_DETAILED);
		}
		return null;
	}

	@Override
	public String toString() {
		return toString(0);
	}

	public Collection<Question> getLeafs() {
		final Set<Question> leafs = new LinkedHashSet<>();
		for (final Question child : getChildren(true)) {
			if (child.getChildren(false).isEmpty()) {
				leafs.add(child);
			}
		}
		return leafs;
	}

	public abstract String toString(int level);

	public Collection<Question> getChildren() {
		return getChildren(false);
	}

	public abstract Collection<Question> getChildren(boolean recursively);

	public abstract boolean replaceChild(Question childToReplace, IStringOrQuestion replacement);

	public Collection<QuestionReference> getAllReferences() {
		return getAllReferences(false);
	}

	public abstract Collection<QuestionReference> getAllReferences(boolean recursively);

	public Collection<App> getAllApps() {
		return getAllApps(false);
	}

	public Collection<App> getAllApps(boolean recursively) {
		return getAllApps(recursively, true);
	}

	public Collection<App> getAllApps(boolean recursively, boolean mergeByHash) {
		final Set<App> temp = new LinkedHashSet<>();
		final Set<String> checkSet = new HashSet<>();
		for (final QuestionReference qRef : getAllReferences(recursively)) {
			final Reference ref = qRef.toReference();
			if (ref != null && ref.getApp() != null) {
				final String hash = HashHelper.getHash(ref.getApp().getHashes(), HashHelper.HASH_TYPE_SHA256);
				if (!mergeByHash || !checkSet.contains(hash)) {
					temp.add(ref.getApp());
					checkSet.add(hash);
				}
			}
		}
		return temp;
	}
}