package de.foellix.aql.system.task;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;
import com.mashape.unirest.request.body.MultipartBody;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.WaitingAnswer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.DefaultOperator;
import de.foellix.aql.system.Storage;

public class OperatorTask {
	private static File tempDirectory = new File("data/temp/");
	private static int num = 0;

	Task parent;

	private final OperatorTaskInfo taskinfo;
	private final List<File> tempAnswerFiles;

	private Answer answer;

	OperatorTask(Task parent) {
		this.parent = parent;
		this.taskinfo = (OperatorTaskInfo) parent.getTaskinfo();
		this.tempAnswerFiles = new ArrayList<>();
	}

	public void execute() throws Exception {
		// Operator
		if (this.taskinfo.getTool() instanceof DefaultOperator) {
			this.answer = DefaultOperator.applyOperator(this.taskinfo);
			this.parent.getParent().operatorExecuted(this.taskinfo.getWaitingAnswer(), this.answer);

			this.parent.successPart2(false);
		} else {
			init();
			Log.msg("Executing " + (this.taskinfo.getTool().isExternal() ? "external" : "internal") + " Operator: "
					+ this.taskinfo.getTool().getName() + " ("
					+ Helper.getExecuteCommand(this.taskinfo, this.tempAnswerFiles) + ")", Log.IMPORTANT);

			if (this.taskinfo.getTool().isExternal()) {
				this.answer = getExternalAnswer();
				if (this.answer != null) {
					// Successful
					this.parent.successPart1(this.taskinfo.getTool().getName() + " successfully executed in %TIME%s. ("
							+ Helper.getExecuteCommand(this.taskinfo, this.tempAnswerFiles) + ")");
					finish();
					this.parent.successPart2(true);

					return;
				}
			} else {
				final String[] runCmd = this.taskinfo.getTool().getExecute().getRun().split(" ");
				for (int i = 0; i < runCmd.length; i++) {
					runCmd[i] = Helper.replaceVariables(runCmd[i], this.taskinfo, this.tempAnswerFiles);
				}
				final String path = Helper.replaceVariables(this.taskinfo.getTool().getPath(), this.taskinfo,
						this.tempAnswerFiles);

				if (this.parent.waitFor(runCmd, path) == 0) {
					// Successful
					this.parent.successPart1(this.taskinfo.getTool().getName() + " successfully executed in %TIME%s. ("
							+ Helper.getExecuteCommand(this.taskinfo, this.tempAnswerFiles) + ")");

					final File result = Helper.findFileWithAsterisk(new File(Helper.replaceVariables(
							this.taskinfo.getTool().getExecute().getResult(), this.taskinfo, this.tempAnswerFiles)));
					Helper.waitForResult("Result file was not generated. " + this.taskinfo.getTool().getName()
							+ " may have not finished properly.", result);

					this.answer = AnswerHandler.parseXML(result);
					finish();

					this.parent.successPart2(true);

					return;
				}
			}
			// Failed
			this.parent.failed(this.taskinfo.getTool().getName() + " execution failed after %TIME%s! ("
					+ Helper.getExecuteCommand(this.taskinfo, this.tempAnswerFiles) + ")");

		}
	}

	void abort(Exception err) {
		deleteTempFiles();
		if (Log.logIt(Log.DEBUG_DETAILED)) {
			err.printStackTrace();
		}
		try {
			if (!this.parent.isExecuted()) {
				Log.msg(this.taskinfo.getTool().getName() + " execution aborted "
						+ (err instanceof TaskAbortedBeforeException ? "before " : "") + "after "
						+ this.parent.getTime() + "s! (" + Helper.getExecuteCommand(this.taskinfo, this.tempAnswerFiles)
						+ ")", Log.IMPORTANT);
			} else {
				Log.msg(this.taskinfo.getTool().getName() + "'s result parsing failed after " + this.parent.getTime()
						+ "s with the following error: " + err.getMessage(), Log.IMPORTANT);
			}
		} catch (final NullPointerException e) {
			if (!this.parent.isExecuted()) {
				Log.msg("Execution aborted!" + (err.getMessage() != null ? " (" + err.getMessage() + ")" : ""),
						Log.IMPORTANT);
			} else {
				Log.msg("Result parsing failed!" + (err.getMessage() != null ? " (" + err.getMessage() + ")" : ""),
						Log.IMPORTANT);
			}
			if (Log.logIt(Log.DEBUG_DETAILED)) {
				e.printStackTrace();
			}
		}
	}

	private void init() {
		this.answer = null;
		tempDirectory.mkdirs();
		for (final Answer temp : this.taskinfo.getWaitingAnswer().getAnswers()) {
			File uniqueFile;
			do {
				uniqueFile = new File(tempDirectory, "temp_" + num++ + ".xml");
			} while (uniqueFile.exists());
			if (temp instanceof WaitingAnswer) {
				AnswerHandler.createXML(((WaitingAnswer) temp).getAnswer(), uniqueFile);
			} else {
				AnswerHandler.createXML(temp, uniqueFile);
			}

			this.tempAnswerFiles.add(uniqueFile);
		}
	}

	private void finish() {
		Storage.getInstance().store(this.taskinfo.getTool(), this.taskinfo.getQuestion(), this.answer);
		this.parent.getParent().operatorExecuted(this.taskinfo.getWaitingAnswer(), this.answer);
		deleteTempFiles();
	}

	private void deleteTempFiles() {
		for (final File tempFile : this.tempAnswerFiles) {
			if (tempFile.exists()) {
				tempFile.delete();
			}
		}
	}

	public static void setTempDirectory(File newTempDirectory) {
		tempDirectory = newTempDirectory;
	}

	// External
	public Answer getExternalAnswer() {
		final StringBuilder query = new StringBuilder(this.taskinfo.getQuestion().getOperator() + " [");
		for (int i = 1; i <= this.tempAnswerFiles.size(); i++) {
			query.append((i == 1 ? "" : ", ") + "'%FILE_" + i + "%' !");
		}
		query.append("]");
		try {
			UnirestHandler.getInstance().start(this.parent.getParent());

			final HttpRequestWithBody request = Unirest.post(this.taskinfo.getTool().getExecute().getUrl())
					.header("accept", MediaType.TEXT_XML);

			final MultipartBody requestWithParameters = request.field("query", query.toString())
					.field("timeout", this.parent.getParent().getScheduler().getTimeout())
					.field("username", this.taskinfo.getTool().getExecute().getUsername())
					.field("password", this.taskinfo.getTool().getExecute().getPassword())
					.field("files", this.tempAnswerFiles);

			final HttpResponse<String> response = requestWithParameters.asString();

			return AnswerHandler.parseXML(response.getBody());
		} catch (final UnirestException e) {
			Log.error("Error occured while accessing external operator: " + e.getMessage());
			return null;
		} finally {
			UnirestHandler.getInstance().stop();
		}
	}
}
