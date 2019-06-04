package de.foellix.aql.system.task;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.Storage;

public class PreprocessorTask {
	Task parent;

	private final PreprocessorTaskInfo taskinfo;

	private App preprocessedApp;

	PreprocessorTask(Task parent) {
		this.parent = parent;
		this.taskinfo = (PreprocessorTaskInfo) parent.getTaskinfo();
	}

	public void execute() throws Exception {
		// Preprocessor
		init();
		if (this.preprocessedApp != null) {
			this.parent.getParent().preprocessingFinished(this.taskinfo, this.preprocessedApp);

			this.parent.successPart2(false);
		} else {
			Log.msg("Executing " + (this.taskinfo.getTool().isExternal() ? "external" : "internal") + " Preprocessor: "
					+ this.taskinfo.getTool().getName() + " (" + Helper.getExecuteCommand(this.taskinfo) + ")",
					Log.IMPORTANT);

			if (this.taskinfo.getTool().isExternal()) {
				final File result = getExternalPreprocessedApp();
				if (result != null) {
					// Successful
					this.parent.successPart1(this.taskinfo.getTool().getName() + " successfully executed in %TIME%s. ("
							+ Helper.getExecuteCommand(this.taskinfo) + ")");
					finish(result);
					this.parent.successPart2(true);

					return;
				}
			} else {
				final String[] runCmd = this.taskinfo.getTool().getExecute().getRun().split(" ");
				for (int i = 0; i < runCmd.length; i++) {
					runCmd[i] = Helper.replaceVariables(runCmd[i], this.taskinfo, this.taskinfo.getApp());
				}
				final String path = Helper.replaceVariables(this.taskinfo.getTool().getPath(), this.taskinfo,
						this.taskinfo.getApp());

				if (this.parent.waitFor(runCmd, path) == 0) {
					// Successful
					this.parent.successPart1(this.taskinfo.getTool().getName() + " successfully executed in %TIME%s. ("
							+ Helper.getExecuteCommand(this.taskinfo) + ")");

					final File result = Helper.findFileWithAsterisk(new File(Helper.replaceVariables(
							this.taskinfo.getTool().getExecute().getResult(), this.taskinfo, this.taskinfo.getApp())));
					Helper.waitForResult("Preprocessed app was not generated. " + this.taskinfo.getTool().getName()
							+ " may have not finished properly.", result);

					finish(result);

					this.parent.successPart2(true);

					return;
				}
			}
			// Failed
			this.parent.failed(this.taskinfo.getTool().getName() + " execution failed after %TIME%s! ("
					+ Helper.getExecuteCommand(this.taskinfo) + ")");

		}
	}

	void abort(Exception err) {
		try {
			if (!this.parent.isExecuted()) {
				Log.msg(this.taskinfo.getTool().getName() + " execution aborted "
						+ (err instanceof TaskAbortedBeforeException ? "before " : "") + "after "
						+ this.parent.getTime() + "s! (" + Helper.getExecuteCommand(this.taskinfo) + ")",
						Log.IMPORTANT);
			} else {
				err.printStackTrace();
				Log.msg(this.taskinfo.getTool().getName() + "'s result unavailable after " + this.parent.getTime()
						+ "s because of the following error: " + err.getMessage(), Log.IMPORTANT);
			}
			if (Log.logIt(Log.DEBUG_DETAILED)) {
				err.printStackTrace();
			}
		} catch (final NullPointerException e) {
			if (!this.parent.isExecuted()) {
				Log.msg("Execution aborted!", Log.IMPORTANT);
			} else {
				Log.msg("Result unavailable!" + (err.getMessage() != null ? " (" + err.getMessage() + ")" : ""),
						Log.IMPORTANT);
			}
			if (Log.logIt(Log.DEBUG_DETAILED)) {
				e.printStackTrace();
			}
		}
	}

	private void init() {
		this.preprocessedApp = Storage.getInstance().load(this.taskinfo.getTool(), this.taskinfo.getApp());
	}

	private void finish(File result) {
		this.preprocessedApp = Helper.createApp(result);
		Storage.getInstance().store(this.taskinfo.getTool(), this.taskinfo.getApp(), this.preprocessedApp);
		this.parent.getParent().preprocessingFinished(this.taskinfo, this.preprocessedApp);

	}

	// External
	public File getExternalPreprocessedApp() {
		final File original = new File(this.taskinfo.getApp().getFile());
		try {
			UnirestHandler.getInstance().start(this.parent.getParent());

			final HttpRequestWithBody request = Unirest
					.post(this.taskinfo.getTool().getExecute().getUrl() + "/"
							+ this.taskinfo.getKeyword().substring(1, this.taskinfo.getKeyword().length() - 1))
					.header("accept", "application/vnd.android.package-archive");

			final MultipartBody requestWithParameters = request
					.field("timeout", this.parent.getParent().getScheduler().getTimeout())
					.field("username", this.taskinfo.getTool().getExecute().getUsername())
					.field("password", this.taskinfo.getTool().getExecute().getPassword()).field("app", original);

			final HttpResponse<InputStream> response = requestWithParameters.asBinary();

			final File result = getPreprocessedFilename(original);
			try {
				Files.copy(response.getBody(), result.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (final IOException e) {
				Log.error(
						"Error while storing preprocessed file (" + result.getAbsolutePath() + "): " + e.getMessage());
				return null;
			}

			return result;
		} catch (final UnirestException e) {
			Log.error("Error occured while accessing external perprocessor: " + e.getMessage());
			return null;
		} finally {
			UnirestHandler.getInstance().stop();
		}
	}

	private File getPreprocessedFilename(File original) {
		final File preprocessed = new File(
				original.getAbsolutePath().substring(0, original.getAbsolutePath().lastIndexOf("."))
						+ "_preprocessed-0.apk");
		return Helper.makeUnique(preprocessed);
	}
}
