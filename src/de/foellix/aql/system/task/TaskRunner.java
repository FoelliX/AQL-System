package de.foellix.aql.system.task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import com.google.common.io.Files;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.helper.CLIHelper;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.ITaskHook;
import de.foellix.aql.system.Options;
import de.foellix.aql.system.ProcessWrapper;
import de.foellix.aql.system.TaskScheduler;
import de.foellix.aql.system.TaskTimer;
import de.foellix.aql.system.UnirestHandler;
import de.foellix.aql.system.defaulttools.DefaultTool;
import de.foellix.aql.system.exceptions.CancelExecutionException;
import de.foellix.aql.system.exceptions.FailedExecutionException;
import de.foellix.aql.system.exceptions.FeedbackAnswerException;
import de.foellix.aql.system.exceptions.TimeoutExecutionException;
import de.foellix.aql.system.storage.Storage;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.MultipartBody;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

public abstract class TaskRunner extends Thread {
	protected TaskScheduler parent;

	protected Task task;
	protected ProcessWrapper processWrapper;

	private TaskTimer timeoutTimer;

	protected File externalAnswerFile;

	public TaskRunner(TaskScheduler parent, Task task) {
		super();
		this.parent = parent;
		this.task = task;
		this.timeoutTimer = null;
	}

	@Override
	public void run() {
		boolean success = false;
		boolean canceled = false;
		boolean timedOut = false;
		String feedback = null;

		// Execute Hooks (before)
		if (this.parent.getParent().getTaskHooksBefore().getHooks().get(this.task.getTool()) != null
				&& !this.parent.getParent().getTaskHooksBefore().getHooks().get(this.task.getTool()).isEmpty()) {
			Log.msg("Running hooks before executing tool: " + Helper.getQualifiedName(this.task.getTool()),
					Log.DEBUG_DETAILED);
			executeTaskHooks(true);
		}

		// RunOnEntry
		if (this.task.getRunOnEntry() != null) {
			Log.msg("Running on entry event for the tool: " + Helper.getQualifiedName(this.task.getTool()),
					Log.DEBUG_DETAILED);
			if (!runExtraTask(this.task.getRunOnEntry())) {
				Log.warning("Execution of extra task on entry seemingly failed: " + this.task.getRunOnEntry());
			}
		}

		// Start Timeout Timer
		long timeout = this.parent.getParent().getOptions().getTimeout();
		if (this.task.getTool().getTimeout() != null) {
			final long toolTimeout = CLIHelper.evaluateTimeout(this.task.getTool().getTimeout());
			if (this.parent.getParent().getOptions().getTimeoutMode() == Options.TIMEOUT_MODE_MAX) {
				timeout = Math.max(timeout, toolTimeout);
			} else if (this.parent.getParent().getOptions().getTimeoutMode() == Options.TIMEOUT_MODE_MIN) {
				timeout = Math.min(timeout, toolTimeout);
			}
		}
		if (timeout > 0) {
			this.timeoutTimer = new TaskTimer(this, timeout);
			this.timeoutTimer.start();
		}

		// Run tool
		try {
			runTool();

			// RunOnSuccess
			success = true;
			if (this.task.getRunOnSuccess() != null) {
				Log.msg("Running on success event for the tool: " + Helper.getQualifiedName(this.task.getTool()),
						Log.DEBUG_DETAILED);
				if (!runExtraTask(this.task.getRunOnSuccess())) {
					Log.warning("Execution of extra task on success seemingly failed: " + this.task.getRunOnSuccess());
				}
			}
		} catch (final FailedExecutionException e) {
			Log.error(Helper.getQualifiedName(this.task.getTool()) + " execution in \""
					+ this.task.getRunInPath().getAbsolutePath() + "\" failed:\n" + this.task.getRunCommand()
					+ Log.getExceptionAppendix(e, true));

			// RunOnFail
			if (this.task.getRunOnFail() != null) {
				Log.msg("Running on fail event for the tool: " + Helper.getQualifiedName(this.task.getTool()),
						Log.DEBUG_DETAILED);
				if (!runExtraTask(this.task.getRunOnFail())) {
					Log.warning("Execution of extra task on fail seemingly failed: " + this.task.getRunOnFail());
				}
			}
		} catch (final TimeoutExecutionException e) {
			try {
				Log.error(Helper.getQualifiedName(this.task.getTool()) + " execution in \""
						+ this.task.getRunInPath().getAbsolutePath() + "\" timed out:\n" + this.task.getRunCommand()
						+ Log.getExceptionAppendix(e, true));
			} catch (final Exception newE) {
				Log.error("Tool execution timed out!" + Log.getExceptionAppendix(e));
			}

			// RunOnAbort was RunOnTimeout already

			// Cancel execution
			timedOut = true;
		} catch (final FeedbackAnswerException e) {
			try {
				Log.error(Helper.getQualifiedName(this.task.getTool()) + " execution in \""
						+ this.task.getRunInPath().getAbsolutePath() + "\" canceled:\n" + this.task.getRunCommand()
						+ Log.getExceptionAppendix(e, true));
			} catch (final Exception newE) {
				Log.error("Tool execution gave feedback answer!" + Log.getExceptionAppendix(e));
			}

			// RunOnAbort
			runOnAbort();

			// Cancel execution
			feedback = e.getMessage();
		} catch (final CancelExecutionException e) {
			try {
				Log.error(Helper.getQualifiedName(this.task.getTool()) + " execution in \""
						+ this.task.getRunInPath().getAbsolutePath() + "\" canceled:\n" + this.task.getRunCommand()
						+ Log.getExceptionAppendix(e, true));
			} catch (final Exception newE) {
				Log.error("Tool execution canceled!" + Log.getExceptionAppendix(e));
			}

			// RunOnAbort
			runOnAbort();

			// Cancel execution
			canceled = true;
		} finally {
			if (isTimeoutTimerAlive()) {
				this.timeoutTimer.interrupt();
			}
		}

		// RunOnExit
		if (this.task.getRunOnExit() != null) {
			Log.msg("Running on exit event for the tool: " + Helper.getQualifiedName(this.task.getTool()),
					Log.DEBUG_DETAILED);
			if (!runExtraTask(this.task.getRunOnExit())) {
				Log.warning("Execution of extra task on exit seemingly failed: " + this.task.getRunOnExit());
			}
		}

		// Execute Hooks (after)
		if (this.parent.getParent().getTaskHooksAfter().getHooks().get(this.task.getTool()) != null
				&& !this.parent.getParent().getTaskHooksAfter().getHooks().get(this.task.getTool()).isEmpty()) {
			Log.msg("Running hooks after executing tool: " + Helper.getQualifiedName(this.task.getTool()),
					Log.DEBUG_DETAILED);
			executeTaskHooks(false);
		}

		// Forward result
		if (timedOut) {
			this.parent.taskFinished(this.task, Task.STATUS_EXECUTION_TIMEOUT);
		} else if (feedback != null) {
			this.parent.taskFinished(this.task, feedback);
		} else if (canceled) {
			this.parent.taskFinished(this.task, Task.STATUS_EXECUTION_INTERRUPTED);
		} else if (success) {
			this.parent.taskFinished(this.task, Task.STATUS_EXECUTION_SUCCESSFUL);
		} else {
			this.parent.taskFinished(this.task, Task.STATUS_EXECUTION_FAILED);
		}
	}

	public void runOnAbort() {
		// RunOnAbort (or RunOnTimeOut)
		if (this.task.getRunOnAbort() != null) {
			Log.msg("Running on abort event for the tool: " + Helper.getQualifiedName(this.task.getTool()),
					Log.DEBUG_DETAILED);
			if (!runExtraTask(this.task.getRunOnAbort())) {
				Log.warning("Execution of extra task on abort seemingly failed: " + this.task.getRunOnAbort());
			}
		}
	}

	private void runTool() throws CancelExecutionException {
		final long time = System.currentTimeMillis();

		// Check storage
		final TaskAnswer taskAnswer = Storage.getInstance().getData().load(this.task);
		if (taskAnswer != null) {
			this.task.getTaskAnswer().setAnswerFile(taskAnswer.getAnswerFile(), true);
			Question loadedQuestion = null;

			// Load from converter parent
			if (this.task instanceof ToolTask) {
				for (final Task parent : this.task.getParents()) {
					if (parent instanceof ConverterTask) {
						loadedQuestion = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(parent);
						if (loadedQuestion != null) {
							break;
						}
					}
				}
			}

			if (this.task instanceof PreprocessorTask) {
				((PreprocessorTask) this.task).updateQuestionReference();
				Log.msg("Loaded from storage: Preprocessor result for App('"
						+ this.task.getTaskInfo().getData(PreprocessorTaskInfo.APP_APK) + "' | '"
						+ this.task.getTool().getQuestions() + "')", Log.IMPORTANT);
			} else {
				// Nothing found? Load for task
				if (loadedQuestion == null) {
					loadedQuestion = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(this.task);
				}
				final String loadedQuestionString = loadedQuestion.toString();
				Log.msg("Loaded from storage:" + (loadedQuestionString.contains("\n") ? "\n" : " ")
						+ loadedQuestionString, Log.IMPORTANT);
			}
			return;
		}

		Log.msg("Starting execution of "
				+ (this.task.getTool() instanceof DefaultTool ? "default "
						: (this.task.getTool().isExternal() ? "external " : "internal "))
				+ Helper.getQualifiedName(this.task.getTool()) + "."
				+ (Log.logIt(Log.DEBUG, true)
						? " [" + this.task.getRunInPath().getAbsolutePath() + ": " + this.task.getRunCommand() + "]"
						: ""),
				Log.IMPORTANT);

		// Run tool
		try {
			runSpecificRunner();
		} catch (final CancelExecutionException e) {
			throw e;
		}

		Log.msg("Finished execution of "
				+ (this.task.getTool() instanceof DefaultTool ? "default "
						: (this.task.getTool().isExternal() ? "external " : "internal "))
				+ Helper.getQualifiedName(this.task.getTool()) + " after "
				+ (Math.round((System.currentTimeMillis() - time) / 10) / 100f) + " seconds."
				+ (Log.logIt(Log.DEBUG)
						? " [" + this.task.getRunInPath().getAbsolutePath() + ": " + this.task.getRunCommand() + "]"
						: ""),
				Log.IMPORTANT);
	}

	protected abstract boolean runSpecificRunner() throws CancelExecutionException;

	private boolean runExtraTask(String cmd) {
		try {
			final Process process = new ProcessBuilder(Helper.getRunCommandAsArray(cmd))
					.directory(this.task.getRunInPath()).start();
			this.processWrapper = new ProcessWrapper(process);
			return (this.processWrapper.waitFor() == 0);
		} catch (final IOException e) {
			Log.warning("Extra task could not be executed: " + cmd + Log.getExceptionAppendix(e));

			// Successfully not executed
			return true;
		}
	}

	private void executeTaskHooks(boolean before) {
		final List<ITaskHook> hooks;
		if (before) {
			hooks = this.parent.getParent().getTaskHooksBefore().getHooks().get(this.task.getTool());
		} else {
			hooks = this.parent.getParent().getTaskHooksAfter().getHooks().get(this.task.getTool());
		}
		if (hooks != null) {
			for (final ITaskHook hook : hooks) {
				try {
					hook.execute(this.task);
				} catch (final Exception e) {
					Log.warning("Something went wrong while executing hook " + (before ? "before " : "after ")
							+ this.task.getTool().getName() + "(" + this.task.getTool().getVersion() + ") was executed."
							+ Log.getExceptionAppendix(e));
				}
			}
		}
	}

	public boolean isTimeoutTimerAlive() {
		if (this.timeoutTimer == null) {
			return false;
		} else {
			return this.timeoutTimer.isAlive();
		}
	}

	@Override
	public void interrupt() {
		if (this.processWrapper != null) {
			this.processWrapper.cancel();
		}
		super.interrupt();
	}

	protected boolean runInternal() throws CancelExecutionException {
		final Process process;
		try {
			process = new ProcessBuilder(Helper.getRunCommandAsArray(this.task.getRunCommand()))
					.directory(this.task.getRunInPath()).start();
		} catch (final IOException e) {
			throw new CancelExecutionException(
					"Internal tool \"" + this.task.getRunCommand() + "\" could not be executed in \""
							+ this.task.getRunInPath().getAbsolutePath() + "\"!" + Log.getExceptionAppendix(e));
		}
		final long pid = Helper.getPid(process);
		this.task.getTaskInfo().setData(TaskInfo.PID, String.valueOf(pid));
		this.processWrapper = new ProcessWrapper(this.task, process);

		final int terminationSignal = this.processWrapper.waitFor();
		if (this.timeoutTimer != null && this.timeoutTimer.isTimedOut()) {
			Log.msg("Process " + pid + " timed out!", Log.DEBUG_DETAILED);
			throw new TimeoutExecutionException(
					Helper.getQualifiedName(this.task.getTool()) + " was internally executed but timed out.");
		} else if (this.processWrapper.isCanceled()) {
			Log.msg("Process " + pid + " canceled!", Log.DEBUG_DETAILED);
			throw new CancelExecutionException(
					Helper.getQualifiedName(this.task.getTool()) + " was internally executed but canceled.");
		} else {
			Log.msg("Process " + pid + " terminated "
					+ (terminationSignal == 0 ? "sucessfully (0)" : "unsuccessfully (" + terminationSignal + ")"),
					Log.DEBUG_DETAILED);
		}
		return terminationSignal == 0;
	}

	protected boolean runExternal() throws CancelExecutionException {
		try {
			UnirestHandler.getInstance().start(this.parent.getParent());

			final HttpRequestWithBody request = Unirest.post(this.task.getTool().getExecute().getUrl());

			MultipartBody requestWithParameters = request
					.field("timeout", String.valueOf(this.parent.getTimeout()).toString())
					.field("username", this.task.getTool().getExecute().getUsername())
					.field("password", this.task.getTool().getExecute().getPassword())
					.field("files", getExternalFiles());
			if (!(this.task instanceof PreprocessorTask)) {
				requestWithParameters = requestWithParameters.field("query", getExternalQuery());
			}

			this.externalAnswerFile = FileHelper.getTempFile();
			boolean success = true;
			if (this.task.getTaskAnswer().getType() == TaskAnswer.ANSWER_TYPE_FILE) {
				final HttpResponse<byte[]> responseFile = requestWithParameters.asBytes();
				if (responseFile.getStatus() != 200) {
					Log.msg("Request error code (1): " + responseFile.getStatus() + " (" + responseFile.getStatusText()
							+ ")", Log.DEBUG_DETAILED);
					success = false;
				}
				try {
					Files.write(responseFile.getBody(), this.externalAnswerFile);
					if (FileHelper.isAPK(this.externalAnswerFile)) {
						final File externalAnswerApk = FileHelper.getTempFile(FileHelper.FILE_ENDING_APK);
						Files.move(this.externalAnswerFile, externalAnswerApk);
						this.externalAnswerFile = externalAnswerApk;
					}
				} catch (final IOException e) {
					throw new CancelExecutionException("Error while storing file answer ("
							+ this.externalAnswerFile.getAbsolutePath() + "): " + e.getMessage());
				}
			} else {
				final HttpResponse<String> responseString = requestWithParameters.asString();
				if (responseString.getStatus() != 200) {
					Log.msg("Request error code (2): " + responseString.getStatus() + " ("
							+ responseString.getStatusText() + ")", Log.DEBUG_DETAILED);
					success = false;
				}
				try (FileWriter writer = new FileWriter(this.externalAnswerFile)) {
					writer.append(responseString.getBody());
				} catch (final IOException e) {
					throw new CancelExecutionException("Error while storing string answer ("
							+ this.externalAnswerFile.getAbsolutePath() + "): " + e.getMessage());
				}
			}

			// Check for feedback answer
			if (success && this.externalAnswerFile != null && this.externalAnswerFile.exists()) {
				final String feedback = Helper.getFeedbackFromFile(this.externalAnswerFile);
				if (feedback != null) {
					throw new FeedbackAnswerException(feedback);
				} else {
					return true;
				}
			} else {
				return false;
			}
		} catch (final UnirestException e) {
			throw new CancelExecutionException(
					"Error occured while accessing external tool!" + Log.getExceptionAppendix(e));
		} finally {
			UnirestHandler.getInstance().stop();
		}
	}

	protected abstract List<File> getExternalFiles();

	protected abstract String getExternalQuery();

	// Getter & Setter
	public Task getTask() {
		return this.task;
	}
}
