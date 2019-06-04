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
import de.foellix.aql.converter.ConverterRegistry;
import de.foellix.aql.converter.IConverter;
import de.foellix.aql.converter.NoConverter;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.IQuestionNode;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.datastructure.handler.QueryHandler;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.Storage;

public class ToolTask {
	Task parent;

	private final ToolTaskInfo taskinfo;

	private Answer answer;

	ToolTask(Task parent) {
		this.parent = parent;
		this.taskinfo = (ToolTaskInfo) parent.getTaskinfo();
	}

	public void execute() throws Exception {
		// Tool
		init();
		if (this.answer != null) {
			this.parent.getParent().localAnswerAvailable(this.taskinfo.getQuestion(), this.answer);

			this.parent.successPart2(false);
		} else {
			Log.msg("Executing " + (this.taskinfo.getTool().isExternal() ? "external" : "internal") + " Tool: "
					+ this.taskinfo.getTool().getName() + " (" + Helper.getExecuteCommand(this.taskinfo) + ")",
					Log.IMPORTANT);

			if (this.taskinfo.getTool().isExternal()) {
				this.answer = getExternalAnswer();
				if (this.answer != null) {
					// Successful
					this.parent.successPart1(this.taskinfo.getTool().getName() + " successfully executed in %TIME%s. ("
							+ Helper.getExecuteCommand(this.taskinfo) + ")");
					finish();
					this.parent.successPart2(true);

					return;
				}
			} else {
				final String[] runCmd = this.taskinfo.getTool().getExecute().getRun().split(" ");
				for (int i = 0; i < runCmd.length; i++) {
					runCmd[i] = Helper.replaceVariables(runCmd[i], this.taskinfo, this.taskinfo.getQuestion());
				}
				final String path = Helper.replaceVariables(this.taskinfo.getTool().getPath(), this.taskinfo,
						this.taskinfo.getQuestion());

				if (this.parent.waitFor(runCmd, path) == 0) {
					// Successful
					this.parent.successPart1(this.taskinfo.getTool().getName() + " successfully executed in %TIME%s. ("
							+ Helper.getExecuteCommand(this.taskinfo) + ")");

					final File result = Helper.findFileWithAsterisk(
							new File(Helper.replaceVariables(this.taskinfo.getTool().getExecute().getResult(),
									this.taskinfo, this.taskinfo.getQuestion())));
					Helper.waitForResult("Result file was not generated. " + this.taskinfo.getTool().getName()
							+ " may have not finished properly.", result);

					applyConverter(result);
					finish();

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
				Log.msg(this.taskinfo.getTool().getName() + "'s result conversion failed after " + this.parent.getTime()
						+ "s with the following error: " + err.getMessage(), Log.IMPORTANT);
			}
			if (Log.logIt(Log.DEBUG_DETAILED)) {
				err.printStackTrace();
			}
		} catch (final NullPointerException e) {
			if (!this.parent.isExecuted()) {
				Log.msg("Execution aborted!", Log.IMPORTANT);
				err.printStackTrace();
			} else {
				Log.msg("Result conversion failed!" + (err.getMessage() != null ? " (" + err.getMessage() + ")" : ""),
						Log.IMPORTANT);
			}
			if (Log.logIt(Log.DEBUG_DETAILED)) {
				e.printStackTrace();
			}
		}
	}

	private void init() {
		this.answer = Storage.getInstance().load(this.taskinfo.getTool(), this.taskinfo.getQuestion(),
				this.parent.getParent().getScheduler().isAlwaysPreferLoading());
	}

	private void applyConverter(File result) throws Exception {
		final IConverter converter = ConverterRegistry.getInstance().getConverter(this.taskinfo.getTool());
		if (converter instanceof NoConverter) {
			Log.msg("No converter found for: " + this.taskinfo.getTool().getName(), Log.DEBUG);
		}
		this.answer = converter.parse(result, this.taskinfo);
	}

	private void finish() {
		Storage.getInstance().store(this.taskinfo.getTool(), this.taskinfo.getQuestion(), this.answer);
		this.parent.getParent().localAnswerAvailable(this.taskinfo.getQuestion(), this.answer);
	}

	// External
	public Answer getExternalAnswer() {
		String query = this.taskinfo.getQuestion().toString();
		try {
			UnirestHandler.getInstance().start(this.parent.getParent());

			final HttpRequestWithBody request = Unirest.post(this.taskinfo.getTool().getExecute().getUrl())
					.header("accept", MediaType.TEXT_XML);

			final IQuestionNode parsedQuery = QueryHandler.parseQuery(query);
			final List<File> files = new ArrayList<>();
			for (final App app : parsedQuery.getAllApps(false)) {
				final String allFiles = app.getFile();
				for (final String file : allFiles.replaceAll(", ", ",").split(",")) {
					files.add(new File(file));
				}
			}
			query = replaceFilesInQuery(parsedQuery);

			final MultipartBody requestWithParameters = request.field("query", query)
					.field("timeout", this.parent.getParent().getScheduler().getTimeout())
					.field("username", this.taskinfo.getTool().getExecute().getUsername())
					.field("password", this.taskinfo.getTool().getExecute().getPassword()).field("files", files);

			final HttpResponse<String> response = requestWithParameters.asString();

			return AnswerHandler.parseXML(response.getBody());
		} catch (final UnirestException e) {
			Log.error("Error occured while accessing external tool: " + e.getMessage());
			return null;
		} finally {
			UnirestHandler.getInstance().stop();
		}
	}

	private String replaceFilesInQuery(IQuestionNode parsedQuery) {
		parsedQuery = QueryHandler.replaceWithAbsolutePaths(parsedQuery);
		String returnQuery = parsedQuery.toString();
		int i = 0;
		for (final App app : parsedQuery.getAllApps(false)) {
			final String allFiles = app.getFile();
			for (final String file : allFiles.replaceAll(", ", ",").split(",")) {
				i++;
				returnQuery = returnQuery.replaceAll(file, "%FILE_" + i + "%");
			}
		}
		return returnQuery;
	}
}
