package de.foellix.aql.system.task;

import java.util.List;

import de.foellix.aql.config.Tool;
import de.foellix.aql.helper.FileWithHash;
import de.foellix.aql.helper.HashHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.TaskCreator;

public class OperatorTask extends Task {
	private static final long serialVersionUID = 7627075773642008834L;

	public OperatorTask(TaskCreator taskCreator, OperatorTaskInfo taskInfo, Tool tool) {
		super(taskCreator, taskInfo, tool);
	}

	@Override
	public void refreshVariables(Task child) {
		final List<FileWithHash> answersAsFiles = Helper.getAnswerChildrenAsString(this);
		this.taskInfo.setData(OperatorTaskInfo.ANSWERS, Helper.getAnswerFilesAsString(answersAsFiles));
		if (!this.parent.getParent().getOptions().getFilenameBasedAnswersHash()) {
			// Default since version 2.0.0
			final String hash256 = Helper.getAnswerFilesAsHash(answersAsFiles, HashHelper.HASH_TYPE_SHA256);
			this.taskInfo.setData(OperatorTaskInfo.ANSWERSHASH, hash256);
			this.taskInfo.setData(OperatorTaskInfo.ANSWERSHASH_MD5,
					Helper.getAnswerFilesAsHash(answersAsFiles, HashHelper.HASH_TYPE_MD5));
			this.taskInfo.setData(OperatorTaskInfo.ANSWERSHASH_SHA1,
					Helper.getAnswerFilesAsHash(answersAsFiles, HashHelper.HASH_TYPE_SHA1));
			this.taskInfo.setData(OperatorTaskInfo.ANSWERSHASH_SHA256, hash256);
		} else {
			// As in version 1.2.0 and before
			final String hash256 = HashHelper.hash(Helper.getAnswerFilesAsString(answersAsFiles),
					HashHelper.HASH_TYPE_SHA256);
			this.taskInfo.setData(OperatorTaskInfo.ANSWERSHASH, hash256);
			this.taskInfo.setData(OperatorTaskInfo.ANSWERSHASH_MD5,
					HashHelper.hash(Helper.getAnswerFilesAsString(answersAsFiles), HashHelper.HASH_TYPE_MD5));
			this.taskInfo.setData(OperatorTaskInfo.ANSWERSHASH_SHA1,
					HashHelper.hash(Helper.getAnswerFilesAsString(answersAsFiles), HashHelper.HASH_TYPE_SHA1));
			this.taskInfo.setData(OperatorTaskInfo.ANSWERSHASH_SHA256,
					HashHelper.hash(Helper.getAnswerFilesAsString(answersAsFiles), HashHelper.HASH_TYPE_SHA256));
		}
	}
}