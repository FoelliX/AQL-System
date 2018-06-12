package de.foellix.aql.system.task;

import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.QuestionPart;

public class ToolTaskInfo extends TaskInfo {
	private final QuestionPart question;

	public ToolTaskInfo(final Tool tool, final QuestionPart question) {
		super(tool);
		this.question = question;
	}

	public QuestionPart getQuestion() {
		return this.question;
	}
}
