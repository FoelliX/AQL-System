package de.foellix.aql.datastructure.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import de.foellix.aql.datastructure.App;
import de.foellix.aql.system.storage.Storage;
import de.foellix.aql.system.task.Task;

public class Query {
	private List<Question> questions;
	private Stack<Question> questionStack; // null when query parsing is finished
	private Map<String, Question> variableMap; // null when query parsing is finished

	public Query() {
		this.questions = new ArrayList<>();
		this.questionStack = new Stack<>();
		this.variableMap = new HashMap<>();
	}

	public List<Question> getQuestions() {
		return this.questions;
	}

	public Stack<Question> getQuestionStack() {
		return this.questionStack;
	}

	public Collection<Question> getAllQuestions() {
		final Collection<Question> questions = new HashSet<>();
		for (final Question question : this.questions) {
			questions.addAll(question.getChildren(true));
		}
		questions.addAll(this.questions);
		return questions;
	}

	public Collection<QuestionReference> getAllReferences() {
		final Collection<QuestionReference> references = new HashSet<>();
		for (final Question question : this.questions) {
			references.addAll(question.getAllReferences());
		}
		return references;
	}

	public Collection<App> getAllApps() {
		final Collection<App> apps = new HashSet<>();
		for (final Question question : this.questions) {
			apps.addAll(question.getAllApps());
		}
		return apps;
	}

	public void setQuestions(List<Question> questions) {
		this.questions = questions;
	}

	public void setQuestionStack(Stack<Question> questionStack) {
		this.questionStack = questionStack;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		for (final Iterator<Question> i = this.questions.iterator(); i.hasNext();) {
			sb.append(i.next());
			if (i.hasNext()) {
				sb.append("\n");
			}
		}
		return sb.toString();
	}

	public Map<String, Question> getVariableMap() {
		return this.variableMap;
	}

	public void destroy() {
		this.questionStack = null;
		this.variableMap = null;
	}

	public void replaceAvailableRAW() {
		final Collection<Question> questions = this.getAllQuestions();
		for (final Question question : questions) {
			if (question instanceof DefaultQuestion && question.getEndingSymbol() == Question.ENDING_SYMBOL_RAW
					&& question.isComplete(true)) {
				// Remember tasks
				final List<Task> rememberParentTasks = new ArrayList<>();
				Question parent = question;
				while (parent != null) {
					rememberParentTasks.add(Storage.getInstance().getData().getTaskFromQuestionTaskMap(parent));
					parent = parent.getParent();
				}

				// Replace RAW
				final QuestionString raw = new QuestionString(Storage.getInstance().getData()
						.getTaskFromQuestionTaskMap(question).getTaskAnswer().getRawContent());
				if (question.getParent() != null) {
					question.getParent().replaceChild(question, raw);
				}

				// Update Storage
				parent = question;
				while (parent != null) {
					Storage.getInstance().getData().putIntoQuestionTaskMap(parent, rememberParentTasks.get(0));
					rememberParentTasks.remove(0);
					parent = parent.getParent();
				}
			}
		}
	}
}