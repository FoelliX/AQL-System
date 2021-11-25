package de.foellix.aql.system.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.converter.DefaultConverter;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.TaskScheduler;
import de.foellix.aql.system.exceptions.CancelExecutionException;
import de.foellix.aql.system.exceptions.FailedExecutionException;
import de.foellix.aql.system.storage.Storage;

public class ConverterTaskRunner extends TaskRunner {
	public ConverterTaskRunner(TaskScheduler parent, Task task) {
		super(parent, task);
	}

	@Override
	protected boolean runSpecificRunner() throws CancelExecutionException {
		boolean success;
		final File finalResultFile;
		if (this.task.getTool() instanceof DefaultConverter) {
			// Default
			final DefaultConverter defaultConverter = (DefaultConverter) this.task.getTool();
			try {
				final Answer answer = defaultConverter.getConverter().parse((ConverterTask) this.task);
				finalResultFile = FileHelper.getTempFile(FileHelper.FILE_ENDING_XML);
				AnswerHandler.createXML(answer, finalResultFile);
				success = true;
			} catch (final Exception e) {
				throw new FailedExecutionException(Helper.getQualifiedName(this.task.getTool()) + " execution failed!"
						+ Log.getExceptionAppendix(e));
			}
		} else if (!this.task.getTool().isExternal()) {
			// Internal
			try {
				success = runInternal();
				if (success) {
					final File resultWithAsteriskFile = new File(
							Helper.replaceVariables(this.task.getTool().getExecute().getResult(), this.task,
									this.task.getParent().getParent().getGlobalVariables()));
					finalResultFile = FileHelper.waitForResult(resultWithAsteriskFile, this.task);
					if (finalResultFile == null) {
						throw new FileNotFoundException("Result file was not generated. Maybe "
								+ Helper.getQualifiedName(this.task.getTool()) + " has not finished properly." + "\n("
								+ resultWithAsteriskFile.getAbsolutePath() + ")");
					}
				} else {
					throw new FailedExecutionException(Helper.getQualifiedName(this.task.getTool())
							+ " was internally executed but did not finish successfully.");
				}
			} catch (final FileNotFoundException e) {
				throw new CancelExecutionException(e.getMessage());
			}
		} else {
			// External
			success = runExternal();
			if (success) {
				finalResultFile = this.externalAnswerFile;
			} else {
				throw new FailedExecutionException(Helper.getQualifiedName(this.task.getTool())
						+ " was externally executed but did not finish successfully.");
			}
		}
		this.task.getTaskAnswer().setAnswerFile(finalResultFile);

		// Store answer
		Storage.getInstance().getData().store(this.task);

		return success;
	}

	@Override
	protected List<File> getExternalFiles() {
		final Question query = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(this.task);

		final List<File> files = new ArrayList<>();
		for (final App app : query.getAllApps(true)) {
			final String allFiles = app.getFile();
			for (final String file : allFiles.replace(", ", ",").split(",")) {
				files.add(new File(file));
			}
		}
		return files;
	}

	@Override
	protected String getExternalQuery() {
		final Question query = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(this.task);

		String externalQuery = query.toString();
		int i = 0;
		for (final App app : query.getAllApps(true)) {
			final String allFiles = app.getFile();
			for (final String file : allFiles.replace(", ", ",").split(",")) {
				externalQuery = externalQuery.replace(file, "%FILE_" + ++i + "%");
			}
		}
		return externalQuery;
	}
}
