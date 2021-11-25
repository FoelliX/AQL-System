package de.foellix.aql.system.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.foellix.aql.Log;
import de.foellix.aql.converter.DefaultConverter;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.TaskScheduler;
import de.foellix.aql.system.defaulttools.analysistools.DefaultAnalysisTool;
import de.foellix.aql.system.exceptions.CancelExecutionException;
import de.foellix.aql.system.exceptions.FailedExecutionException;
import de.foellix.aql.system.storage.Storage;

public class ToolTaskRunner extends TaskRunner {
	public ToolTaskRunner(TaskScheduler parent, Task task) {
		super(parent, task);
	}

	@Override
	protected boolean runSpecificRunner() throws CancelExecutionException {
		boolean success;
		File finalResultFile;
		if (this.task.getTool() instanceof DefaultAnalysisTool) {
			// Default
			final DefaultAnalysisTool defaultAnalysisTool = (DefaultAnalysisTool) this.task.getTool();
			try {
				finalResultFile = defaultAnalysisTool.applyAnalysisTool((ToolTask) this.task);
				success = true;
			} catch (final Exception e) {
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
						// Recover answer file
						final ConverterTask converterParent = Helper.getConverterParent(this.task);
						if (converterParent != null && converterParent.getTool() instanceof DefaultConverter) {
							finalResultFile = ((DefaultConverter) converterParent.getTool()).getConverter()
									.recoverResultFromOutput(this.processWrapper.getOutput(), resultWithAsteriskFile);
						}
					}

					if (finalResultFile == null) {
						throw new FileNotFoundException("Result file was not generated. Maybe "
								+ Helper.getQualifiedName(this.task.getTool()) + " has not finished properly." + "\n("
								+ resultWithAsteriskFile.getAbsolutePath() + ")");
					}
				} else {
					throw new FailedExecutionException(Helper.getQualifiedName(this.task.getTool())
							+ " was internally executed but did not finish successfully.");
				}
			} catch (final CancelExecutionException e) {
				throw e;
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

		// Store answer or assign result file to the associated converter
		final ConverterTask converterParent = Helper.getConverterParent(this.task);
		if (converterParent == null || this.task.getTool().isExternal()) {
			this.task.getTaskAnswer().setAnswerFile(finalResultFile, false);
			Storage.getInstance().getData().store(this.task);
		} else {
			this.task.getTaskAnswer().setAnswerFile(finalResultFile, true);
		}

		return success;
	}

	@Override
	protected List<File> getExternalFiles() {
		final List<File> files = new ArrayList<>();

		// App files
		for (final File file : Helper.getExternalAppFiles(this.task)) {
			files.add(file);
		}

		// Other files
		for (final File file : Helper.getExternalQueryFiles(this.task)) {
			files.add(file);
		}

		return new ArrayList<>(files);
	}

	@Override
	protected String getExternalQuery() {
		String query = Helper.getExternalQuery(this.task);
		final List<File> files = getExternalFiles();
		final Set<File> alreadyReplaced = new HashSet<>();
		int i = 0;
		for (final File file : files) {
			if (alreadyReplaced.contains(file)) {
				continue;
			} else {
				alreadyReplaced.add(file);
			}
			if (!query.contains(file.getAbsolutePath()) && !query.contains(file.getAbsolutePath().replace("\\", "/"))) {
				Log.warning("File not found in query (" + query + "): " + file.getAbsolutePath());
				continue;
			}
			i++;
			query = query.replace(file.getAbsolutePath(), "%FILE_" + i + "%");
			query = query.replace(file.getAbsolutePath().replace("\\", "/"), "%FILE_" + i + "%");
		}
		query = Helper.replaceCustomVariables(query, this.parent.getParent().getGlobalVariables());
		Log.msg("External Query \"" + query + "\" with " + files + " constructed.", Log.DEBUG_DETAILED);
		return query;
	}
}