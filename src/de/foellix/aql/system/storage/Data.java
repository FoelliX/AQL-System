package de.foellix.aql.system.storage;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.query.IStringOrQuestion;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.datastructure.query.QuestionReference;
import de.foellix.aql.helper.BiMap;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.ConverterTask;
import de.foellix.aql.system.task.PreprocessorTask;
import de.foellix.aql.system.task.Task;
import de.foellix.aql.system.task.TaskAnswer;

public class Data implements Serializable {
	private static final long serialVersionUID = 8715218638063357419L;

	private final Map<String, TaskAnswer> runAnswerMap;
	private final BiMap<Question, Task> questionTaskMap;
	private final Map<QuestionReference, Map<String, PreprocessorTask>> questionReferenceTaskMap;
	private long answerFileCounter;

	public Data() {
		this.runAnswerMap = new HashMap<>();
		this.questionTaskMap = new BiMap<>();
		this.questionReferenceTaskMap = new HashMap<>();
		this.answerFileCounter = 0;
	}

	public void store(Task task) {
		final String hashString = task.getHashableString();
		if (hashString != null) {
			this.runAnswerMap.put(hashString, task.getTaskAnswer());
			Log.msg("Storing answered task: " + task.getTaskAnswer().getAnswerFile().getAbsolutePath() + " ["
					+ hashString + "]", Log.DEBUG_DETAILED);
		} else {
			Log.msg("Task cannot be stored. No hash string available.", Log.NORMAL);
		}

		Storage.getInstance().saveData();
	}

	public TaskAnswer load(Task task) {
		final String hashString = task.getHashableString();
		if (hashString != null) {
			return this.runAnswerMap.get(hashString);
		}
		Log.msg("Task cannot be loaded. No hash string available.", Log.NORMAL);
		return null;
	}

	public Map<String, TaskAnswer> getRunAnswerMap() {
		return this.runAnswerMap;
	}

	public void putIntoQuestionTaskMap(Question question, Task task) {
		this.questionTaskMap.put(question, task);
	}

	public Question getQuestionFromQuestionTaskMap(Task task) {
		return getQuestionFromQuestionTaskMap(task, false);
	}

	public Question getQuestionFromQuestionTaskMap(Task task, boolean takeConverterIntoAccount) {
		if (takeConverterIntoAccount) {
			final Task converterTask = Helper.getConverterParent(task);
			if (converterTask != null && converterTask.getChildren().size() == 1) {
				return this.questionTaskMap.getKey(converterTask);
			}
		}
		return this.questionTaskMap.getKey(task);
	}

	public void removeFromQuestionTaskMap(Question question) {
		this.questionTaskMap.remove(question);
	}

	public void removeFromQuestionTaskMap(Task task) {
		final Question question = this.questionTaskMap.getKey(task);
		if (question != null) {
			this.questionTaskMap.remove(question);
		}
	}

	public Task getTaskFromQuestionTaskMap(Question question) {
		return getTaskFromQuestionTaskMap(question, false);
	}

	public Task getTaskFromQuestionTaskMap(Question question, boolean ignoreConverterTasks) {
		if (ignoreConverterTasks) {
			if (this.questionTaskMap.get(question).getChildren().size() == 1) {
				final Task convereterTask = this.questionTaskMap.get(question);
				if (convereterTask instanceof ConverterTask) {
					return convereterTask.getChildren().iterator().next();
				} else {
					return convereterTask;
				}
			}
		}
		return this.questionTaskMap.get(question);
	}

	public synchronized File getNewAnswerFile() {
		this.answerFileCounter++;
		final String filename = Helper.addZeroDigits(this.answerFileCounter);
		return new File(Storage.getInstance().getStorageDirectory(), filename);
	}

	public PreprocessorTask getPreprocessorTask(QuestionReference ref, IStringOrQuestion keyword) {
		return getPreprocessorTask(ref, keyword.toStringInAnswer());
	}

	public PreprocessorTask getPreprocessorTask(QuestionReference ref, String keyword) {
		if (!this.questionReferenceTaskMap.containsKey(ref)) {
			return null;
		} else {
			return this.questionReferenceTaskMap.get(ref).get(keyword);
		}
	}

	public void addPreprocessorTask(QuestionReference ref, IStringOrQuestion keyword,
			PreprocessorTask preprocessorTask) {
		if (!this.questionReferenceTaskMap.containsKey(ref)) {
			this.questionReferenceTaskMap.put(ref, new HashMap<>());
		}
		this.questionReferenceTaskMap.get(ref).put(keyword.toStringInAnswer(false), preprocessorTask);
	}

	public void wipeCache() {
		this.questionTaskMap.clear();
		this.questionReferenceTaskMap.clear();
	}
}