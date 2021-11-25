package de.foellix.aql.system.task;

import java.io.File;

import de.foellix.aql.helper.FileHelper;

public class RootTaskAnswer extends TaskAnswer {
	private static final long serialVersionUID = -3129816903352293308L;

	public static final File STATS_FILE = new File(FileHelper.getTempDirectory(), "stats.txt");

	public RootTaskAnswer(Task parent) {
		super(parent, TaskAnswer.ANSWER_TYPE_RAW);
	}

	@Override
	public boolean isAnswered() {
		return this.parent.isReady();
	}
}