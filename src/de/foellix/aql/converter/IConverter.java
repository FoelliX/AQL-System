package de.foellix.aql.converter;

import java.io.File;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.system.task.ToolTaskInfo;

/**
 *
 * Interface for converter subclasses
 *
 */
public interface IConverter {
	public Answer parse(final File resultFile, ToolTaskInfo taskInfo) throws Exception;
}
