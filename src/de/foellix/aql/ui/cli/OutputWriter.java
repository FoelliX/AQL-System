package de.foellix.aql.ui.cli;

import java.io.File;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.system.IAnswerAvailable;

public class OutputWriter implements IAnswerAvailable {
	private final File file;

	public OutputWriter(final File file) {
		this.file = file;
	}

	@Override
	public void answerAvailable(Object answer, int status) {
		if (answer instanceof Answer) {
			AnswerHandler.createXML((Answer) answer, this.file);
		}
	}
}
