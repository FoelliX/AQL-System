package de.foellix.aql.system.storage;

import de.foellix.aql.Log;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.TaskAnswer;

public class StorageEntry {
	private int id;
	private String runCmd;
	private TaskAnswer answer;
	private long creationTime;

	public StorageEntry(StorageExplorer parent, String runCmd, TaskAnswer answer, long creationTime) {
		super();
		this.id = ++parent.currentId;
		this.runCmd = runCmd;
		this.answer = answer;
		this.creationTime = creationTime;
	}

	public int getId() {
		return this.id;
	}

	public String getIdAsString() {
		return Helper.addZeroDigits(this.id);
	}

	public String getRunCmd() {
		return this.runCmd;
	}

	public TaskAnswer getAnswer() {
		return this.answer;
	}

	public long getCreationTime() {
		return this.creationTime;
	}

	@Override
	public int hashCode() {
		return this.id;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof StorageEntry) {
			return this.id == ((StorageEntry) other).getId();
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return this.id + ") " + Helper.getDate(this.creationTime, Log.LOG_DATE_FORMAT) + ", "
				+ this.answer.getAnswerFile().getAbsolutePath() + ", "
				+ Helper.typeToSoi(this.answer.getSubjectOfInterest());
	}
}