package de.foellix.aql.system.task;

import java.io.File;

import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.query.FilterQuestion;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.helper.AppInfo;
import de.foellix.aql.system.TaskCreator;
import de.foellix.aql.system.storage.Storage;

public class FilterOperatorTask extends OperatorTask {
	private static final long serialVersionUID = 7627075773642008834L;

	public FilterOperatorTask(TaskCreator taskCreator, FilterOperatorTaskInfo taskInfo, Tool tool) {
		super(taskCreator, taskInfo, tool);
	}

	@Override
	public void refreshVariables(Task child) {
		super.refreshVariables(child);

		// Replace key-value variables
		if (child.getTaskAnswer().getType() == TaskAnswer.ANSWER_TYPE_RAW) {
			final Question loadedFilterQuestion = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(this);
			final Question loadedKeyOrValueQuestion = Storage.getInstance().getData()
					.getQuestionFromQuestionTaskMap(child);
			if (loadedFilterQuestion instanceof FilterQuestion) {
				if (((FilterQuestion) loadedFilterQuestion).getFilterPair().getKey() == loadedKeyOrValueQuestion) {
					this.taskInfo.setData(FilterOperatorTaskInfo.KEY, (String) child.getTaskAnswer().getAnswer());
				} else if (((FilterQuestion) loadedFilterQuestion).getFilterPair()
						.getValue() == loadedKeyOrValueQuestion) {
					this.taskInfo.setData(FilterOperatorTaskInfo.VALUE, (String) child.getTaskAnswer().getAnswer());
				}
			}
		}

		// Replace variables in filter reference (analog to implementation in ToolTask) ...
		// by filter-reference-preprocessor
		if (child instanceof PreprocessorTask) {
			final Question loadedQuestion = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(this);
			if (loadedQuestion instanceof FilterQuestion) {
				final FilterQuestion question = (FilterQuestion) loadedQuestion;

				if (child instanceof PreprocessorTask) {
					if (question.getFilterReference() != null
							&& question.getFilterReference().getExecutedPreprocessorKeywords() != null
							&& !question.getFilterReference().getExecutedPreprocessorKeywords().isEmpty()) {
						for (final String keyword : question.getFilterReference().getExecutedPreprocessorKeywords()) {
							final PreprocessorTask loadedPreprocessorTask = Storage.getInstance().getData()
									.getPreprocessorTask(question.getFilterReference(), keyword);
							if (child == loadedPreprocessorTask) {
								final File apkFile = (File) child.getTaskAnswer().getAnswer();
								final AppInfo appInfo = new AppInfo(apkFile);
								this.taskInfo.setData(FilterOperatorTaskInfo.REFERENCE_APK,
										appInfo.getApkFile().getAbsolutePath());
								this.taskInfo.setData(FilterOperatorTaskInfo.REFERENCE_APK_FILENAME,
										appInfo.getFilename());
								this.taskInfo.setData(FilterOperatorTaskInfo.REFERENCE_APK_NAME, appInfo.getAppName());
								this.taskInfo.setData(FilterOperatorTaskInfo.REFERENCE_APK_PACKAGE,
										appInfo.getPkgName());
							}
						}
					}
				}
			}
		}

		// by app
		if (child.getTaskAnswer().getType() == TaskAnswer.ANSWER_TYPE_FILE) {
			final Question loadedQuestion = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(this, true);
			final Question loadedFileQuestion = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(child);
			if (loadedQuestion instanceof FilterQuestion) {
				if (((FilterQuestion) loadedQuestion).getFilterReference() != null
						&& ((FilterQuestion) loadedQuestion).getFilterReference().getApp() == loadedFileQuestion) {
					final File apkFile = (File) child.getTaskAnswer().getAnswer();
					final AppInfo appInfo = new AppInfo(apkFile);
					this.taskInfo.setData(FilterOperatorTaskInfo.REFERENCE_APK, appInfo.getApkFile().getAbsolutePath());
					this.taskInfo.setData(FilterOperatorTaskInfo.REFERENCE_APK_FILENAME, appInfo.getFilename());
					this.taskInfo.setData(FilterOperatorTaskInfo.REFERENCE_APK_NAME, appInfo.getAppName());
					this.taskInfo.setData(FilterOperatorTaskInfo.REFERENCE_APK_PACKAGE, appInfo.getPkgName());
				}
			}
		}

		// by statement, method, class
		if (child.getTaskAnswer().getType() == TaskAnswer.ANSWER_TYPE_RAW) {
			final Question loadedQuestion = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(this, true);
			final Question loadedFileQuestion = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(child);
			if (loadedQuestion instanceof FilterQuestion) {
				if (((FilterQuestion) loadedQuestion).getFilterReference() != null) {
					if (((FilterQuestion) loadedQuestion).getFilterReference().getStatement() instanceof Question) {
						final Question needle = (Question) ((FilterQuestion) loadedQuestion).getFilterReference()
								.getStatement();
						if (needle == loadedFileQuestion) {
							this.taskInfo.setData(ToolTaskInfo.STATEMENT_IN,
									child.getTaskAnswer().getAnswerForQuery(false));
						}
					}
					if (((FilterQuestion) loadedQuestion).getFilterReference().getMethod() instanceof Question) {
						final Question needle = (Question) ((FilterQuestion) loadedQuestion).getFilterReference()
								.getMethod();
						if (needle == loadedFileQuestion) {
							this.taskInfo.setData(ToolTaskInfo.METHOD_IN,
									child.getTaskAnswer().getAnswerForQuery(false));
						}
					}
					if (((FilterQuestion) loadedQuestion).getFilterReference().getClassname() instanceof Question) {
						final Question needle = (Question) ((FilterQuestion) loadedQuestion).getFilterReference()
								.getClassname();
						if (needle == loadedFileQuestion) {
							this.taskInfo.setData(ToolTaskInfo.CLASS_IN,
									child.getTaskAnswer().getAnswerForQuery(false));
						}
					}
				}
			}
		}
	}
}