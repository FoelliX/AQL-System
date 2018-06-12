package de.foellix.aql.system.task;

import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.QuestionPart;
import de.foellix.aql.helper.HashHelper;

public class PreprocessorTaskInfo extends TaskInfo {
	private final App app;
	private final QuestionPart question;
	private final String keyword;

	public PreprocessorTaskInfo(final Tool preprocessor, final App app, QuestionPart question, final String keyword) {
		super(preprocessor);
		this.app = app;
		this.question = question;
		this.keyword = keyword;
	}

	public String getHash() {
		return HashHelper.createHash(super.getTool(), this.app);
	}

	public App getApp() {
		return this.app;
	}

	public QuestionPart getQuestion() {
		return this.question;
	}

	public String getKeyword() {
		return this.keyword;
	}
}
