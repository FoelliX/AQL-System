package de.foellix.aql.system.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.query.OperatorQuestion;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.TaskScheduler;
import de.foellix.aql.system.defaulttools.operators.DefaultOperator;
import de.foellix.aql.system.exceptions.CancelExecutionException;
import de.foellix.aql.system.exceptions.FailedExecutionException;
import de.foellix.aql.system.storage.Storage;

public class OperatorTaskRunner extends TaskRunner {
	public OperatorTaskRunner(TaskScheduler parent, Task task) {
		super(parent, task);
	}

	@Override
	protected boolean runSpecificRunner() throws CancelExecutionException {
		boolean success;
		final File finalResultFile;
		if (this.task.getTool() instanceof DefaultOperator) {
			// Default
			final DefaultOperator defaultOperator = (DefaultOperator) this.task.getTool();
			try {
				finalResultFile = defaultOperator.applyOperator((OperatorTask) this.task);
				success = true;
			} catch (final Exception e) {
				e.printStackTrace();
				System.exit(0);
				success = false;
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
		final List<File> files = new ArrayList<>();
		for (final Task child : this.task.getChildren()) {
			files.add(child.getTaskAnswer().getAnswerFile());
		}
		return files;
	}

	@Override
	protected String getExternalQuery() {
		final OperatorQuestion query = (OperatorQuestion) Storage.getInstance().getData()
				.getQuestionFromQuestionTaskMap(this.task);
		final StringBuilder sb = new StringBuilder(query.getOperator()).append(" [ ");
		for (int i = 0; i < this.task.getChildren().size(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append("'%FILE_" + (i + 1) + "%' ?");
		}
		sb.append(" ] ").append(query.getEndingSymbol());
		return sb.toString();
	}
}
