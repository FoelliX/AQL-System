package de.foellix.aql.system.task;

import java.io.File;

import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.query.DefaultQuestion;
import de.foellix.aql.datastructure.query.LoadingQuestion;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.datastructure.query.StringOrQuestionPair;
import de.foellix.aql.helper.AppInfo;
import de.foellix.aql.system.TaskCreator;
import de.foellix.aql.system.storage.Storage;

public class ToolTask extends Task {
	private static final long serialVersionUID = -5789871100141384864L;

	public ToolTask(TaskCreator taskCreator, ToolTaskInfo taskInfo, Tool tool) {
		super(taskCreator, taskInfo, tool);
	}

	@Override
	public void refreshVariables(Task child) {
		// by preprocessor
		if (child instanceof PreprocessorTask) {
			final Question loadedQuestion = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(this, true);
			if (loadedQuestion instanceof DefaultQuestion) {
				final DefaultQuestion question = (DefaultQuestion) loadedQuestion;

				if (child instanceof PreprocessorTask) {
					// IN
					if (question.getIn() != null && question.getIn().getExecutedPreprocessorKeywords() != null
							&& !question.getIn().getExecutedPreprocessorKeywords().isEmpty()) {
						for (final String keyword : question.getIn().getExecutedPreprocessorKeywords()) {
							final PreprocessorTask loadedPreprocessorTask = Storage.getInstance().getData()
									.getPreprocessorTask(question.getIn(), keyword);
							if (child == loadedPreprocessorTask) {
								final File apkFile = (File) child.getTaskAnswer().getAnswer();
								final AppInfo appInfo = new AppInfo(apkFile);
								this.taskInfo.setData(ToolTaskInfo.APP_APK_IN, appInfo.getApkFile().getAbsolutePath());
								this.taskInfo.setData(ToolTaskInfo.APP_APK_IN_FILENAME, appInfo.getFilename());
								this.taskInfo.setData(ToolTaskInfo.APP_APK_IN_NAME, appInfo.getAppName());
								this.taskInfo.setData(ToolTaskInfo.APP_APK_IN_PACKAGE, appInfo.getPkgName());
							}
						}
					}

					// FROM
					if (question.getFrom() != null && question.getFrom().getExecutedPreprocessorKeywords() != null
							&& !question.getFrom().getExecutedPreprocessorKeywords().isEmpty()) {
						for (final String keyword : question.getFrom().getExecutedPreprocessorKeywords()) {
							final PreprocessorTask loadedPreprocessorTask = Storage.getInstance().getData()
									.getPreprocessorTask(question.getFrom(), keyword);
							if (child == loadedPreprocessorTask) {
								final File apkFile = (File) child.getTaskAnswer().getAnswer();
								final AppInfo appInfo = new AppInfo(apkFile);
								this.taskInfo.setData(ToolTaskInfo.APP_APK_FROM,
										appInfo.getApkFile().getAbsolutePath());
								this.taskInfo.setData(ToolTaskInfo.APP_APK_FROM_FILENAME, appInfo.getFilename());
								this.taskInfo.setData(ToolTaskInfo.APP_APK_FROM_NAME, appInfo.getAppName());
								this.taskInfo.setData(ToolTaskInfo.APP_APK_FROM_PACKAGE, appInfo.getPkgName());
							}
						}
					}

					// TO
					if (question.getTo() != null && question.getTo().getExecutedPreprocessorKeywords() != null
							&& !question.getTo().getExecutedPreprocessorKeywords().isEmpty()) {
						for (final String keyword : question.getTo().getExecutedPreprocessorKeywords()) {
							final PreprocessorTask loadedPreprocessorTask = Storage.getInstance().getData()
									.getPreprocessorTask(question.getTo(), keyword);
							if (child == loadedPreprocessorTask) {
								final File apkFile = (File) child.getTaskAnswer().getAnswer();
								final AppInfo appInfo = new AppInfo(apkFile);
								this.taskInfo.setData(ToolTaskInfo.APP_APK_TO, appInfo.getApkFile().getAbsolutePath());
								this.taskInfo.setData(ToolTaskInfo.APP_APK_TO_FILENAME, appInfo.getFilename());
								this.taskInfo.setData(ToolTaskInfo.APP_APK_TO_NAME, appInfo.getAppName());
								this.taskInfo.setData(ToolTaskInfo.APP_APK_TO_PACKAGE, appInfo.getPkgName());
							}
						}
					}
				}
			}
		}

		// by app or with-file
		if (child.getTaskAnswer().getType() == TaskAnswer.ANSWER_TYPE_FILE) {
			final Question loadedQuestion = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(this, true);
			final Question loadedFileQuestion = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(child);
			if (loadedQuestion instanceof DefaultQuestion) {
				if (((DefaultQuestion) loadedQuestion).getIn() != null
						&& ((DefaultQuestion) loadedQuestion).getIn().getApp() == loadedFileQuestion) {
					// IN
					final File apkFile = (File) child.getTaskAnswer().getAnswer();
					final AppInfo appInfo = new AppInfo(apkFile);
					this.taskInfo.setData(ToolTaskInfo.APP_APK_IN, appInfo.getApkFile().getAbsolutePath());
					this.taskInfo.setData(ToolTaskInfo.APP_APK_IN_FILENAME, appInfo.getFilename());
					this.taskInfo.setData(ToolTaskInfo.APP_APK_IN_NAME, appInfo.getAppName());
					this.taskInfo.setData(ToolTaskInfo.APP_APK_IN_PACKAGE, appInfo.getPkgName());
				} else if (((DefaultQuestion) loadedQuestion).getFrom() != null
						&& ((DefaultQuestion) loadedQuestion).getFrom().getApp() == loadedFileQuestion) {
					// FROM
					final File apkFile = (File) child.getTaskAnswer().getAnswer();
					final AppInfo appInfo = new AppInfo(apkFile);
					this.taskInfo.setData(ToolTaskInfo.APP_APK_FROM, appInfo.getApkFile().getAbsolutePath());
					this.taskInfo.setData(ToolTaskInfo.APP_APK_FROM_FILENAME, appInfo.getFilename());
					this.taskInfo.setData(ToolTaskInfo.APP_APK_FROM_NAME, appInfo.getAppName());
					this.taskInfo.setData(ToolTaskInfo.APP_APK_FROM_PACKAGE, appInfo.getPkgName());
				} else if (((DefaultQuestion) loadedQuestion).getTo() != null
						&& ((DefaultQuestion) loadedQuestion).getTo().getApp() == loadedFileQuestion) {
					// TO
					final File apkFile = (File) child.getTaskAnswer().getAnswer();
					final AppInfo appInfo = new AppInfo(apkFile);
					this.taskInfo.setData(ToolTaskInfo.APP_APK_TO, appInfo.getApkFile().getAbsolutePath());
					this.taskInfo.setData(ToolTaskInfo.APP_APK_TO_FILENAME, appInfo.getFilename());
					this.taskInfo.setData(ToolTaskInfo.APP_APK_TO_NAME, appInfo.getAppName());
					this.taskInfo.setData(ToolTaskInfo.APP_APK_TO_PACKAGE, appInfo.getPkgName());
				} else {
					// WITH
					refreshWith(child, loadedQuestion, loadedFileQuestion);
				}
			}
		}

		// by AQL with-file
		if (child.getTaskAnswer().getType() == TaskAnswer.ANSWER_TYPE_AQL) {
			final Question loadedQuestion = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(this, true);
			final Question loadedFileQuestion = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(child);
			if (loadedQuestion instanceof DefaultQuestion) {
				// WITH
				refreshWith(child, loadedQuestion, loadedFileQuestion);
			}
		}

		// by statement, method, class, with-key-or-value string
		if (child.getTaskAnswer().getType() == TaskAnswer.ANSWER_TYPE_RAW) {
			final Question loadedQuestion = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(this, true);
			final Question loadedFileQuestion = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(child);
			if (loadedQuestion instanceof DefaultQuestion) {
				// WITH
				refreshWith(child, loadedQuestion, loadedFileQuestion);

				// IN
				if (((DefaultQuestion) loadedQuestion).getIn() != null) {
					if (((DefaultQuestion) loadedQuestion).getIn().getStatement() instanceof Question) {
						final Question needle = (Question) ((DefaultQuestion) loadedQuestion).getIn().getStatement();
						if (needle == loadedFileQuestion) {
							this.taskInfo.setData(ToolTaskInfo.STATEMENT_IN,
									child.getTaskAnswer().getAnswerForQuery(false));
						}
					}
					if (((DefaultQuestion) loadedQuestion).getIn().getMethod() instanceof Question) {
						final Question needle = (Question) ((DefaultQuestion) loadedQuestion).getIn().getMethod();
						if (needle == loadedFileQuestion) {
							this.taskInfo.setData(ToolTaskInfo.METHOD_IN,
									child.getTaskAnswer().getAnswerForQuery(false));
						}
					}
					if (((DefaultQuestion) loadedQuestion).getIn().getClassname() instanceof Question) {
						final Question needle = (Question) ((DefaultQuestion) loadedQuestion).getIn().getClassname();
						if (needle == loadedFileQuestion) {
							this.taskInfo.setData(ToolTaskInfo.CLASS_IN,
									child.getTaskAnswer().getAnswerForQuery(false));
						}
					}
				}

				// FROM
				if (((DefaultQuestion) loadedQuestion).getFrom() != null) {
					if (((DefaultQuestion) loadedQuestion).getFrom().getStatement() instanceof Question) {
						final Question needle = (Question) ((DefaultQuestion) loadedQuestion).getFrom().getStatement();
						if (needle == loadedFileQuestion) {
							this.taskInfo.setData(ToolTaskInfo.STATEMENT_FROM,
									child.getTaskAnswer().getAnswerForQuery(false));
						}
					}
					if (((DefaultQuestion) loadedQuestion).getFrom().getMethod() instanceof Question) {
						final Question needle = (Question) ((DefaultQuestion) loadedQuestion).getFrom().getMethod();
						if (needle == loadedFileQuestion) {
							this.taskInfo.setData(ToolTaskInfo.METHOD_FROM,
									child.getTaskAnswer().getAnswerForQuery(false));
						}
					}
					if (((DefaultQuestion) loadedQuestion).getFrom().getClassname() instanceof Question) {
						final Question needle = (Question) ((DefaultQuestion) loadedQuestion).getFrom().getClassname();
						if (needle == loadedFileQuestion) {
							this.taskInfo.setData(ToolTaskInfo.CLASS_FROM,
									child.getTaskAnswer().getAnswerForQuery(false));
						}
					}
				}

				// TO
				if (((DefaultQuestion) loadedQuestion).getTo() != null) {
					if (((DefaultQuestion) loadedQuestion).getTo().getStatement() instanceof Question) {
						final Question needle = (Question) ((DefaultQuestion) loadedQuestion).getTo().getStatement();
						if (needle == loadedFileQuestion) {
							this.taskInfo.setData(ToolTaskInfo.STATEMENT_TO,
									child.getTaskAnswer().getAnswerForQuery(false));
						}
					}
					if (((DefaultQuestion) loadedQuestion).getTo().getMethod() instanceof Question) {
						final Question needle = (Question) ((DefaultQuestion) loadedQuestion).getTo().getMethod();
						if (needle == loadedFileQuestion) {
							this.taskInfo.setData(ToolTaskInfo.METHOD_TO,
									child.getTaskAnswer().getAnswerForQuery(false));
						}
					}
					if (((DefaultQuestion) loadedQuestion).getTo().getClassname() instanceof Question) {
						final Question needle = (Question) ((DefaultQuestion) loadedQuestion).getTo().getClassname();
						if (needle == loadedFileQuestion) {
							this.taskInfo.setData(ToolTaskInfo.CLASS_TO,
									child.getTaskAnswer().getAnswerForQuery(false));
						}
					}
				}
			}
		}

		// by app loading question
		if (child.getTaskAnswer().getType() == TaskAnswer.ANSWER_TYPE_FILE) {
			final Question loadedQuestion = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(this, true);
			final Question loadedFileQuestion = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(child);
			if (loadedQuestion instanceof DefaultQuestion && loadedFileQuestion instanceof LoadingQuestion) {
				// WITH
				refreshWith(child, loadedQuestion, loadedFileQuestion);

				// IN
				if (((DefaultQuestion) loadedQuestion).getIn() != null) {
					if (((DefaultQuestion) loadedQuestion).getIn().getApp() instanceof LoadingQuestion) {
						final Question needle = (Question) ((DefaultQuestion) loadedQuestion).getIn().getApp();
						if (needle == loadedFileQuestion) {
							final File apkFile = (File) child.getTaskAnswer().getAnswer();
							final AppInfo appInfo = new AppInfo(apkFile);
							this.taskInfo.setData(ToolTaskInfo.APP_APK_IN, appInfo.getApkFile().getAbsolutePath());
							this.taskInfo.setData(ToolTaskInfo.APP_APK_IN_FILENAME, appInfo.getFilename());
							this.taskInfo.setData(ToolTaskInfo.APP_APK_IN_NAME, appInfo.getAppName());
							this.taskInfo.setData(ToolTaskInfo.APP_APK_IN_PACKAGE, appInfo.getPkgName());
						}
					}
				}

				// FROM
				if (((DefaultQuestion) loadedQuestion).getFrom() != null) {
					if (((DefaultQuestion) loadedQuestion).getFrom().getApp() instanceof LoadingQuestion) {
						final Question needle = (Question) ((DefaultQuestion) loadedQuestion).getFrom().getApp();
						if (needle == loadedFileQuestion) {
							final File apkFile = (File) child.getTaskAnswer().getAnswer();
							final AppInfo appInfo = new AppInfo(apkFile);
							this.taskInfo.setData(ToolTaskInfo.APP_APK_FROM, appInfo.getApkFile().getAbsolutePath());
							this.taskInfo.setData(ToolTaskInfo.APP_APK_FROM_FILENAME, appInfo.getFilename());
							this.taskInfo.setData(ToolTaskInfo.APP_APK_FROM_NAME, appInfo.getAppName());
							this.taskInfo.setData(ToolTaskInfo.APP_APK_FROM_PACKAGE, appInfo.getPkgName());
						}
					}
				}

				// TO
				if (((DefaultQuestion) loadedQuestion).getTo() != null) {
					if (((DefaultQuestion) loadedQuestion).getTo().getApp() instanceof LoadingQuestion) {
						final Question needle = (Question) ((DefaultQuestion) loadedQuestion).getTo().getApp();
						if (needle == loadedFileQuestion) {
							final File apkFile = (File) child.getTaskAnswer().getAnswer();
							final AppInfo appInfo = new AppInfo(apkFile);
							this.taskInfo.setData(ToolTaskInfo.APP_APK_TO, appInfo.getApkFile().getAbsolutePath());
							this.taskInfo.setData(ToolTaskInfo.APP_APK_TO_FILENAME, appInfo.getFilename());
							this.taskInfo.setData(ToolTaskInfo.APP_APK_TO_NAME, appInfo.getAppName());
							this.taskInfo.setData(ToolTaskInfo.APP_APK_TO_PACKAGE, appInfo.getPkgName());
						}
					}
				}
			}
		}
	}

	private void refreshWith(Task child, Question loadedQuestion, Question loadedFileQuestion) {
		if (((DefaultQuestion) loadedQuestion).getWiths() != null) {
			final String answerFileString = child.getTaskAnswer().getAnswerForQuery(false);
			for (final StringOrQuestionPair pair : ((DefaultQuestion) loadedQuestion).getWiths()) {
				if (pair.getValue() == loadedFileQuestion && pair.getKey().isComplete(true)) {
					this.taskInfo.setData(TaskInfo.VARIABLE_SYMBOL_FILE + pair.getKey().toStringInAnswer(false)
							+ TaskInfo.VARIABLE_SYMBOL_FILE, answerFileString);
					break;
				} else if (pair.getKey() == loadedFileQuestion && pair.getValue().isComplete(true)) {
					this.taskInfo.setData(
							TaskInfo.VARIABLE_SYMBOL_FILE + answerFileString + TaskInfo.VARIABLE_SYMBOL_FILE,
							pair.getValue().toStringInAnswer(false));
					break;
				}
			}
		}
	}
}