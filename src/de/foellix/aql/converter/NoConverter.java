package de.foellix.aql.converter;

import java.io.File;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.system.task.ConverterTask;
import de.foellix.aql.system.task.ConverterTaskInfo;

public class NoConverter implements IConverter {
	@Override
	public Answer parse(ConverterTask task) throws Exception {
		final File resultFile = new File(task.getTaskInfo().getData(ConverterTaskInfo.RESULT_FILE));
		final Answer answer = AnswerHandler.parseXML(resultFile);
		if (answer == null) {
			Log.error("Converter required! " + resultFile.getAbsolutePath() + " seems to be no valid AQL-Answer.");
		} else {
			Log.msg("No converter required!", Log.DEBUG);
		}
		return answer;
	}
}
