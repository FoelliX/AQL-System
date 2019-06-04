package de.foellix.aql.converter;

import java.io.File;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.system.task.ToolTaskInfo;

public class NoConverter implements IConverter {
	@Override
	public Answer parse(File resultFile, ToolTaskInfo taskInfo) throws Exception {
		final Answer answer = AnswerHandler.parseXML(resultFile);
		if (answer == null) {
			Log.error("Converter required! " + resultFile.getAbsolutePath() + " seems to be no valid AQL-Answer.");
		} else {
			Log.msg("No converter required!", Log.DEBUG);
		}
		return answer;
	}
}
