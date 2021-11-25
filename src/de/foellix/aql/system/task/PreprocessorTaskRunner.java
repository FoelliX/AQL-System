package de.foellix.aql.system.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.TaskScheduler;
import de.foellix.aql.system.exceptions.CancelExecutionException;
import de.foellix.aql.system.exceptions.FailedExecutionException;
import de.foellix.aql.system.storage.Storage;

public class PreprocessorTaskRunner extends TaskRunner {
	public PreprocessorTaskRunner(TaskScheduler parent, Task task) {
		super(parent, task);
	}

	@Override
	protected boolean runSpecificRunner() throws CancelExecutionException {
		boolean success;
		final File finalResultFile;
		if (!this.task.getTool().isExternal()) {
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

		// Update question reference
		((PreprocessorTask) this.task).updateQuestionReference();

		return success;
	}

	@Override
	protected List<File> getExternalFiles() {
		return Helper.getPreprocessorFiles(this.task);
	}

	@Override
	protected String getExternalQuery() {
		throw new UnsupportedOperationException("A preprocessor cannot be associated with a query.");
	}
}
