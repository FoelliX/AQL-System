package de.foellix.aql.system;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.query.DefaultQuestion;
import de.foellix.aql.datastructure.query.FilterQuestion;
import de.foellix.aql.datastructure.query.IStringOrQuestion;
import de.foellix.aql.datastructure.query.LoadingQuestion;
import de.foellix.aql.datastructure.query.OperatorQuestion;
import de.foellix.aql.datastructure.query.Query;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.datastructure.query.QuestionReference;
import de.foellix.aql.datastructure.query.StringOrQuestionPair;
import de.foellix.aql.helper.AppInfo;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.defaulttools.operators.DefaultOperator;
import de.foellix.aql.system.exceptions.CancelExecutionException;
import de.foellix.aql.system.storage.Storage;
import de.foellix.aql.system.task.ConverterTask;
import de.foellix.aql.system.task.ConverterTaskInfo;
import de.foellix.aql.system.task.FilterOperatorTask;
import de.foellix.aql.system.task.FilterOperatorTaskInfo;
import de.foellix.aql.system.task.LoadAnswerTask;
import de.foellix.aql.system.task.LoadAnswerTaskInfo;
import de.foellix.aql.system.task.OperatorTask;
import de.foellix.aql.system.task.OperatorTaskInfo;
import de.foellix.aql.system.task.PlaceholderTask;
import de.foellix.aql.system.task.PreprocessorTask;
import de.foellix.aql.system.task.PreprocessorTaskInfo;
import de.foellix.aql.system.task.RootTask;
import de.foellix.aql.system.task.Task;
import de.foellix.aql.system.task.TaskAnswer;
import de.foellix.aql.system.task.TaskInfo;
import de.foellix.aql.system.task.ToolTask;
import de.foellix.aql.system.task.ToolTaskInfo;
import de.foellix.aql.transformations.QueryTransformer;

public class TaskCreator {
	private final AQLSystem parent;

	private Query currentQuery;
	private Task currentTask;

	public TaskCreator(AQLSystem aqlSystem) {
		this.parent = aqlSystem;
	}

	public Task query(Query query) throws CancelExecutionException {
		return query(query, true);
	}

	public Task query(Query query, boolean initialQuery) throws CancelExecutionException {
		this.currentQuery = query;
		this.currentTask = new RootTask(this);
		if (initialQuery) {
			// Resolve directory references
			query.setQuestions(DirectoryResolver.resolveDirectoryReference(query.getQuestions()));
			String resolved = query.toString();
			Log.msg("Resolved:" + (resolved.contains("\n") ? '\n' : ' ') + resolved, Log.DEBUG);

			// Apply transformers
			boolean changed = false;
			for (final Question question : query.getQuestions()) {
				if (QueryTransformer.transform(query, question)) {
					changed = true;
				}
			}
			if (changed) {
				resolved = Helper.replaceCustomVariables(query.toString(), this.parent.getGlobalVariables());
				Log.msg("Transformed:" + (resolved.contains("\n") ? '\n' : ' ') + resolved, Log.DEBUG);
			}
		}
		for (final Question question : query.getQuestions()) {
			this.currentTask.addChild(getTask(question));
		}
		if (!initialQuery) {
			this.parent.getTaskScheduler().changed(this.currentTask);
		}
		return this.currentTask;
	}

	private Task getTask(Question question) throws CancelExecutionException {
		// Get or create task
		Task task = Storage.getInstance().getData().getTaskFromQuestionTaskMap(question);
		if (task == null || task instanceof PlaceholderTask) {
			if (question.isComplete(false)) {
				Task newTask = null;
				if (question instanceof DefaultQuestion) {
					newTask = getTask((DefaultQuestion) question);
				} else if (question instanceof FilterQuestion) {
					newTask = getTask((FilterQuestion) question);
				} else if (question instanceof OperatorQuestion) {
					newTask = getTask((OperatorQuestion) question);
				} else if (question instanceof LoadingQuestion) {
					newTask = getTask((LoadingQuestion) question);
				} else {
					Log.error("Unknown question type: " + question.getClass().getSimpleName());
				}
				if (task instanceof PlaceholderTask) {
					task.replaceByTask(newTask);
				}
				task = newTask;
			} else if (task == null) {
				task = new PlaceholderTask(this);
			}
			appendPreprocessors(task, question);
			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);
		}

		// Continue with children
		for (final Question child : question.getChildren(false)) {
			if (task instanceof ConverterTask) {
				if (task.getChildren().size() == 1) {
					task.getChildren().iterator().next().addChild(getTask(child));
				} else {
					Log.warning("Invalid converter task with multiple children detected.");
					task.addChild(getTask(child));
				}
			} else {
				task.addChild(getTask(child));
			}
		}

		return task;
	}

	private Task getTask(DefaultQuestion question) throws CancelExecutionException {
		// 1. Tool
		final Tool tool = ToolSelector.selectAnalysisTool(question);
		if (tool == null) {
			final String errorMsg = "No appropriate tool could be found to answer the following question: " + question;
			this.parent.queryCanceled(Task.STATUS_NO_APPROPRIATE_TOOL, errorMsg);
			throw new CancelExecutionException(errorMsg);
		}

		// 2. TaskInfo
		final ToolTaskInfo taskInfo = new ToolTaskInfo();
		setAlwaysAttributes(taskInfo, tool);

		if (question.getIn() != null) {
			if (question.getIn().getApp() != null && question.getIn().getApp().isComplete(true)
					&& question.getIn().isComplete(true)) {
				final File apkFile = new File(question.getIn().getApp().toStringInAnswer(false));
				if (!apkFile.exists()) {
					final String errorMsg = apkFile.getAbsolutePath() + " does not exist but was used in: " + question;
					this.parent.queryCanceled(Task.STATUS_APK_NOT_FOUND, errorMsg);
					throw new CancelExecutionException(errorMsg);
				}
				final AppInfo appInfo = new AppInfo(apkFile);
				taskInfo.setData(ToolTaskInfo.APP_APK_IN, appInfo.getApkFile().getAbsolutePath());
				taskInfo.setData(ToolTaskInfo.APP_APK_IN_FILENAME, appInfo.getFilename());
				taskInfo.setData(ToolTaskInfo.APP_APK_IN_NAME, appInfo.getAppName());
				taskInfo.setData(ToolTaskInfo.APP_APK_IN_PACKAGE, appInfo.getPkgName());
			}
			if (question.getIn().getStatement() != null && question.getIn().getStatement().isComplete(true)) {
				taskInfo.setData(ToolTaskInfo.STATEMENT_IN, question.getIn().getStatement().toStringInAnswer(false));
			}
			taskInfo.setData(ToolTaskInfo.LINENUMBER_IN, String.valueOf(question.getIn().getLineNumber()).toString());
			if (question.getIn().getMethod() != null && question.getIn().getMethod().isComplete(true)) {
				taskInfo.setData(ToolTaskInfo.METHOD_IN, question.getIn().getMethod().toStringInAnswer(false));
			}
			if (question.getIn().getClassname() != null && question.getIn().getClassname().isComplete(true)) {
				taskInfo.setData(ToolTaskInfo.CLASS_IN, question.getIn().getClassname().toStringInAnswer(false));
			}
		}
		if (question.getFrom() != null) {
			if (question.getFrom().getApp() != null && question.getFrom().getApp().isComplete(true)
					&& question.getFrom().isComplete(true)) {
				final File apkFile = new File(question.getFrom().getApp().toStringInAnswer(false));
				final AppInfo appInfo = new AppInfo(apkFile);
				taskInfo.setData(ToolTaskInfo.APP_APK_FROM, appInfo.getApkFile().getAbsolutePath());
				taskInfo.setData(ToolTaskInfo.APP_APK_FROM_FILENAME, appInfo.getFilename());
				taskInfo.setData(ToolTaskInfo.APP_APK_FROM_NAME, appInfo.getAppName());
				taskInfo.setData(ToolTaskInfo.APP_APK_FROM_PACKAGE, appInfo.getPkgName());
			}
			if (question.getFrom().getStatement() != null && question.getIn().getStatement().isComplete(true)) {
				taskInfo.setData(ToolTaskInfo.STATEMENT_FROM,
						question.getFrom().getStatement().toStringInAnswer(false));
			}
			taskInfo.setData(ToolTaskInfo.LINENUMBER_FROM,
					String.valueOf(question.getFrom().getLineNumber()).toString());
			if (question.getFrom().getMethod() != null && question.getIn().getMethod().isComplete(true)) {
				taskInfo.setData(ToolTaskInfo.METHOD_FROM, question.getFrom().getMethod().toStringInAnswer(false));
			}
			if (question.getFrom().getClassname() != null && question.getIn().getClassname().isComplete(true)) {
				taskInfo.setData(ToolTaskInfo.CLASS_FROM, question.getFrom().getClassname().toStringInAnswer(false));
			}
		}
		if (question.getTo() != null) {
			if (question.getTo().getApp() != null && question.getTo().getApp().isComplete(true)
					&& question.getTo().isComplete(true)) {
				final File apkFile = new File(question.getTo().getApp().toStringInAnswer(false));
				final AppInfo appInfo = new AppInfo(apkFile);
				taskInfo.setData(ToolTaskInfo.APP_APK_TO, appInfo.getApkFile().getAbsolutePath());
				taskInfo.setData(ToolTaskInfo.APP_APK_TO_FILENAME, appInfo.getFilename());
				taskInfo.setData(ToolTaskInfo.APP_APK_TO_NAME, appInfo.getAppName());
				taskInfo.setData(ToolTaskInfo.APP_APK_TO_PACKAGE, appInfo.getPkgName());
			}
			if (question.getTo().getStatement() != null && question.getIn().getStatement().isComplete(true)) {
				taskInfo.setData(ToolTaskInfo.STATEMENT_TO, question.getTo().getStatement().toStringInAnswer(false));
			}
			taskInfo.setData(ToolTaskInfo.LINENUMBER_TO, String.valueOf(question.getTo().getLineNumber()).toString());
			if (question.getTo().getMethod() != null && question.getIn().getMethod().isComplete(true)) {
				taskInfo.setData(ToolTaskInfo.METHOD_TO, question.getTo().getMethod().toStringInAnswer(false));
			}
			if (question.getTo().getClassname() != null && question.getIn().getClassname().isComplete(true)) {
				taskInfo.setData(ToolTaskInfo.CLASS_TO, question.getTo().getClassname().toStringInAnswer(false));
			}
		}
		if (question.getWiths() != null) {
			for (final StringOrQuestionPair pair : question.getWiths()) {
				if (pair.getKey().isComplete(true) && pair.getValue().isComplete(true)) {
					taskInfo.setData(TaskInfo.VARIABLE_SYMBOL_FILE + pair.getKey().toStringInAnswer(false)
							+ TaskInfo.VARIABLE_SYMBOL_FILE, pair.getValue().toStringInAnswer(false));
				}
			}
		}

		// 3. Task
		final ToolTask task = new ToolTask(this, taskInfo, tool);

		// 4. TaskAnswer
		final TaskAnswer taskAnswer = new TaskAnswer(task, getAnswerType(question), question.getSubjectOfInterest());
		task.setTaskAnswer(taskAnswer);

		// 5. Converter
		if (!tool.isExternal()) {
			// 1. Converter
			final Tool converter = ToolSelector.selectConverter(tool);

			if (converter != null) {
				// 2. Converter: TaskInfo
				final ConverterTaskInfo converterTaskInfo = new ConverterTaskInfo();
				for (final String variable : taskInfo.getAllVariableNames()) {
					if (taskInfo.getData(variable) != null && !taskInfo.getData(variable).isEmpty()) {
						converterTaskInfo.setData(variable, taskInfo.getData(variable));
					}
				}

				// 3. Converter: Task
				final ConverterTask converterTask = new ConverterTask(this, converterTaskInfo, converter);

				// 4. Converter: TaskAnswer
				final TaskAnswer converterTaskAnswer = new TaskAnswer(converterTask, TaskAnswer.ANSWER_TYPE_AQL,
						question.getSubjectOfInterest());
				converterTask.setTaskAnswer(converterTaskAnswer);

				// Append converter task
				converterTask.addChild(task);

				// Return converter task instead
				return converterTask;
			}
		}

		return task;
	}

	private Task getTask(OperatorQuestion question) throws CancelExecutionException {
		// 1. Tool
		final Tool tool = ToolSelector.selectOperator(question);
		if (tool == null) {
			final String errorMsg = "No appropriate operator could be found to answer the following question:\n"
					+ question;
			this.parent.queryCanceled(Task.STATUS_NO_APPROPRIATE_TOOL, errorMsg);
			throw new CancelExecutionException(errorMsg);
		}

		// 2. TaskInfo
		final OperatorTaskInfo taskInfo = new OperatorTaskInfo();
		setAlwaysAttributes(taskInfo, tool);

		// 3. Task
		final OperatorTask task = new OperatorTask(this, taskInfo, tool);

		// 4. TaskAnswer
		final TaskAnswer taskAnswer = new TaskAnswer(task, getAnswerType(question));
		task.setTaskAnswer(taskAnswer);

		return task;
	}

	private Task getTask(FilterQuestion question) throws CancelExecutionException {
		// 1. Tool
		final Tool tool = ToolSelector.selectOperator(question);
		if (tool == null) {
			final String errorMsg = "No appropriate filter-operator could be found to answer the following question:\n"
					+ question;
			this.parent.queryCanceled(Task.STATUS_NO_APPROPRIATE_TOOL, errorMsg);
			throw new CancelExecutionException(errorMsg);
		}

		// 2. TaskInfo
		final FilterOperatorTaskInfo taskInfo = new FilterOperatorTaskInfo();
		setAlwaysAttributes(taskInfo, tool);

		// 2.1. Special filter attributes
		if (question.getFilterSubjectOfInterest() != null) {
			taskInfo.setData(FilterOperatorTaskInfo.SUBJECT_OF_INTEREST, question.getFilterSubjectOfInterest());
		}
		if (question.getFilterPair() != null) {
			if (question.getFilterPair().getKey() != null && question.getFilterPair().getKey().isComplete(true)) {
				taskInfo.setData(FilterOperatorTaskInfo.KEY, question.getFilterPair().getKey().toStringInAnswer(false));
			}
			if (question.getFilterPair().getValue() != null && question.getFilterPair().getValue().isComplete(true)) {
				taskInfo.setData(FilterOperatorTaskInfo.VALUE,
						question.getFilterPair().getValue().toStringInAnswer(false));
			}
		}
		if (question.getFilterReference() != null) {
			if (question.getFilterReference().getStatement() != null
					&& question.getFilterReference().getStatement().isComplete(true)) {
				taskInfo.setData(FilterOperatorTaskInfo.REFERENCE_STATEMENT,
						question.getFilterReference().getStatement().toStringInAnswer(false));
				taskInfo.setData(FilterOperatorTaskInfo.REFERENCE_LINENUMBER,
						String.valueOf(question.getFilterReference().getLineNumber()).toString());
			}
			if (question.getFilterReference().getMethod() != null
					&& question.getFilterReference().getMethod().isComplete(true)) {
				taskInfo.setData(FilterOperatorTaskInfo.REFERENCE_METHOD,
						question.getFilterReference().getMethod().toStringInAnswer(false));
			}
			if (question.getFilterReference().getClassname() != null
					&& question.getFilterReference().getClassname().isComplete(true)) {
				taskInfo.setData(FilterOperatorTaskInfo.REFERENCE_CLASS,
						question.getFilterReference().getClassname().toStringInAnswer(false));
			}
			if (question.getFilterReference().getApp() != null
					&& question.getFilterReference().getApp().isComplete(true)) {
				taskInfo.setData(FilterOperatorTaskInfo.REFERENCE_APK,
						question.getFilterReference().getApp().toStringInAnswer(false));
			}
		}

		// 3. Task
		final OperatorTask task = new FilterOperatorTask(this, taskInfo, tool);

		// 4. TaskAnswer
		final TaskAnswer taskAnswer = new TaskAnswer(task, getAnswerType(question));
		task.setTaskAnswer(taskAnswer);

		return task;
	}

	private Task getTask(LoadingQuestion question) {
		// 1. Tool
		final Tool tool = new LoadAnswerTool();

		// 2. TaskInfo
		final LoadAnswerTaskInfo taskInfo = new LoadAnswerTaskInfo();
		taskInfo.setFileToLoad(question.getFile());

		// 3. Task
		final LoadAnswerTask task = new LoadAnswerTask(this, taskInfo, tool);

		// 4. TaskAnswer
		final TaskAnswer taskAnswer = new TaskAnswer(task, getAnswerType(question));
		task.setTaskAnswer(taskAnswer);

		return task;
	}

	private void appendPreprocessors(Task task, Question question) throws CancelExecutionException {
		// Append to tool instead of converter
		if (task instanceof ConverterTask) {
			if (task.getChildren().size() == 1) {
				task = task.getChildren().iterator().next();
			} else {
				Log.warning("Cannot append preprocessor to converter task with multiple children.");
			}
		}
		if (task instanceof ToolTask) {
			// Append Preprocessor
			for (final QuestionReference ref : question.getAllReferences()) {
				for (final IStringOrQuestion keyword : ref.getPreprocessorKeywords()) {
					PreprocessorTask preprocessorTask = Storage.getInstance().getData().getPreprocessorTask(ref,
							keyword);
					if (preprocessorTask == null) {
						// 1. Preprocessor
						final Tool preprocessor = ToolSelector.selectPreprocessor(keyword.toStringInAnswer(false));

						if (preprocessor != null) {
							// 2. Preprocessor: TaskInfo
							final PreprocessorTaskInfo preprocessorTaskInfo = new PreprocessorTaskInfo();
							setAlwaysAttributes(preprocessorTaskInfo, preprocessor);
							for (final String variableName : task.getTaskInfo().getAllVariableNames()) {
								if (task.getTaskInfo().getData(variableName) != null) {
									preprocessorTaskInfo.setData(Helper.toVariableName(variableName),
											task.getTaskInfo().getData(variableName));
								}
							}
							String apkFileString = ref.getApp().toStringInAnswer(false);
							final File apkFile = new File(apkFileString);
							if (apkFile.exists()) {
								// Single APK File
								final AppInfo appInfo = new AppInfo(apkFile);
								preprocessorTaskInfo.setData(PreprocessorTaskInfo.APP_APK,
										appInfo.getApkFile().getAbsolutePath());
								preprocessorTaskInfo.setData(PreprocessorTaskInfo.APP_APK_FILENAME,
										appInfo.getFilename());
								preprocessorTaskInfo.setData(PreprocessorTaskInfo.APP_APK_NAME, appInfo.getAppName());
								preprocessorTaskInfo.setData(PreprocessorTaskInfo.APP_APK_PACKAGE,
										appInfo.getPkgName());
							} else if (apkFileString.contains(",")) {
								// Multiple APK Files
								apkFileString = Helper.replaceDoubleSpaces(apkFileString).replace(", ", ",");
								final List<AppInfo> appInfos = new ArrayList<>();
								for (final String oneApkFileString : apkFileString.split(",")) {
									final File oneApkFile = new File(oneApkFileString);
									if (oneApkFile.exists()) {
										appInfos.add(new AppInfo(oneApkFile));
									}
								}
								final StringBuilder appApk = new StringBuilder();
								final StringBuilder appApkFilename = new StringBuilder();
								final StringBuilder appApkName = new StringBuilder();
								final StringBuilder appApkPackage = new StringBuilder();
								for (final AppInfo appInfo : appInfos) {
									// Combine APK files separated by comma
									if (!appApk.isEmpty()) {
										appApk.append(", ");
									}
									appApk.append(appInfo.getApkFile().getAbsolutePath());

									// Combine APK filenames separated by underscore
									if (!appApkFilename.isEmpty()) {
										appApkFilename.append("_");
									}
									appApkFilename.append(appInfo.getFilename());

									// Combine APK names separated by underscore
									if (!appApkName.isEmpty()) {
										appApkName.append("_");
									}
									appApkName.append(appInfo.getAppName());

									// Combine APK packages separated by underscore
									if (!appApkPackage.isEmpty()) {
										appApkPackage.append("_ ");
									}
									appApkPackage.append(appInfo.getPkgName());
								}
								preprocessorTaskInfo.setData(PreprocessorTaskInfo.APP_APK, appApk.toString());
								preprocessorTaskInfo.setData(PreprocessorTaskInfo.APP_APK_FILENAME,
										appApkFilename.toString());
								preprocessorTaskInfo.setData(PreprocessorTaskInfo.APP_APK_NAME, appApkName.toString());
								preprocessorTaskInfo.setData(PreprocessorTaskInfo.APP_APK_PACKAGE,
										appApkPackage.toString());
							}

							// 3. Preprocessor: Task
							preprocessorTask = new PreprocessorTask(this, preprocessorTaskInfo, preprocessor, ref);

							// 4. Preprocessor: TaskAnswer
							final TaskAnswer preprocessorTaskAnswer = new TaskAnswer(preprocessorTask,
									TaskAnswer.ANSWER_TYPE_FILE);
							preprocessorTask.setTaskAnswer(preprocessorTaskAnswer);

							// Append new preprocessor task
							task.addChild(preprocessorTask);

							// Store preprocessor task
							Storage.getInstance().getData().addPreprocessorTask(ref, keyword, preprocessorTask);
						} else {
							final String errorMsg = "No appropriate preprocessor (" + keyword
									+ ") could be found to answer the following question: " + question;
							this.parent.queryCanceled(Task.STATUS_NO_APPROPRIATE_TOOL, errorMsg);
							throw new CancelExecutionException(errorMsg);
						}
					} else {
						// Append loaded preprocessor task
						task.addChild(preprocessorTask);
					}
				}
			}
		}
	}

	public void checkTransformations(Task task) {
		// Replace RAW information
		this.currentQuery.replaceAvailableRAW();

		// Check transformations
		if (Storage.getInstance().getData().getQuestionFromQuestionTaskMap(task) != null && QueryTransformer
				.transform(this.currentQuery, Storage.getInstance().getData().getQuestionFromQuestionTaskMap(task))) {
			try {
				this.currentTask = query(this.currentQuery, false);
			} catch (final CancelExecutionException e) {
				Log.warning("Could not apply transformation rule. Continuing without." + Log.getExceptionAppendix(e));
			}
		}
	}

	private void setAlwaysAttributes(TaskInfo taskInfo, Tool tool) {
		if (!(tool instanceof DefaultOperator)) {
			if (!tool.isExternal()) {
				taskInfo.setData(ToolTaskInfo.MEMORY, Integer.toString(tool.getExecute().getMemoryPerInstance()));
			}
			if (ConfigHandler.getInstance().getConfig() != null) {
				taskInfo.setData(ToolTaskInfo.ANDROID_PLATFORMS,
						ConfigHandler.getInstance().getConfig().getAndroidPlatforms());
				taskInfo.setData(ToolTaskInfo.ANDROID_BUILDTOOLS,
						ConfigHandler.getInstance().getConfig().getAndroidBuildTools());
			}
			taskInfo.setData(ToolTaskInfo.DATE, Helper.getDate());
		}
	}

	private int getAnswerType(Question question) {
		switch (question.getEndingSymbol()) {
			case Question.ENDING_SYMBOL_FILE:
				return TaskAnswer.ANSWER_TYPE_FILE;
			case Question.ENDING_SYMBOL_RAW:
				return TaskAnswer.ANSWER_TYPE_RAW;
			default:
				return TaskAnswer.ANSWER_TYPE_AQL;
		}
	}

	public AQLSystem getParent() {
		return this.parent;
	}

	public void tryPlaceholderReplacement(Task task) {
		for (final Task child : task.getChildren()) {
			if (!child.isReady()) {
				return;
			}
		}
		try {
			if (task instanceof PlaceholderTask) {
				getTask(Storage.getInstance().getData().getQuestionFromQuestionTaskMap(task));
			}
			for (final Task parent : new HashSet<>(task.getParents())) {
				tryPlaceholderReplacement(parent);
			}
		} catch (final CancelExecutionException e) {
			Log.error("While trying to replace placeholders." + Log.getExceptionAppendix(e));
		}
	}
}