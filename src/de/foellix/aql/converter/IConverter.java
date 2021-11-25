package de.foellix.aql.converter;

import java.io.File;
import java.util.List;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.system.task.ConverterTask;

/**
 *
 * Interface for converter subclasses
 *
 */
public interface IConverter {
	/**
	 * Converts a tool's result.
	 *
	 * @param task
	 *            the converter task. Example: For getting the result file use: "new File(task.getTaskInfo().getData(ConverterTaskInfo.RESULT_FILE))"
	 * @return answer the converted AQL-Answer
	 * @throws Exception
	 *             in case something goes wrong exception message will be forwarded to AQL-System and be logged.
	 */
	public Answer parse(ConverterTask task) throws Exception;

	/**
	 * When a tool fails there is a a chance to recover its result from the tool's output and store it in a file. Example: FlowDroid does not create an result if it simply does not find an analysis entry point. However, the tool ran successfully (intended behavior), thus, we create an emtpy result file (see ConverterFD).
	 *
	 * @param output
	 *            the tool's output
	 * @param expectedResultFile
	 *            The result file which was not created.
	 * @return the recovered result file
	 */
	public default File recoverResultFromOutput(List<String> output, File expectedResultFile) {
		return null;
	}
}