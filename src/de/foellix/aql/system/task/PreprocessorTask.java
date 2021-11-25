package de.foellix.aql.system.task;

import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.query.QuestionReference;
import de.foellix.aql.system.TaskCreator;

public class PreprocessorTask extends Task {
	private static final long serialVersionUID = -7549464236644449952L;

	private QuestionReference questionReference;

	public PreprocessorTask(TaskCreator taskCreator, PreprocessorTaskInfo taskInfo, Tool tool,
			QuestionReference questionReference) {
		super(taskCreator, taskInfo, tool);
		this.questionReference = questionReference;
	}

	public void updateQuestionReference() {
		this.questionReference.getExecutedPreprocessorKeywords().add(this.getTool().getQuestions());
		// questionReference.setApp(new QuestionString(finalResultFile.getAbsolutePath()));
	}

	public QuestionReference getQuestionReference() {
		return this.questionReference;
	}

	@Override
	public void refreshVariables(Task child) {
	}
}