package de.foellix.aql.system.task;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.defaulttools.operators.DefaultFilterOperator;
import de.foellix.aql.system.storage.Storage;

public class TaskAnswer implements Serializable {
	private static final long serialVersionUID = -2279233633809417421L;

	private static final DefaultFilterOperator soiFilter = new DefaultFilterOperator();

	private static final String FILE_ENDING_PLACEHOLDER = ".%FILE_ENDING%";
	public static final int ANSWER_TYPE_AQL = 0;
	public static final int ANSWER_TYPE_FILE = 1;
	public static final int ANSWER_TYPE_RAW = 2;

	protected Task parent;
	private File answerFile;
	private int type;
	private int subjectOfInterest;
	private boolean answered;

	public TaskAnswer(Task parent, int type) {
		this(parent, type, Question.QUESTION_TYPE_UNKNOWN);
	}

	public TaskAnswer(Task parent, int type, int subjectOfInterest) {
		this.parent = parent;
		this.type = type;
		this.subjectOfInterest = subjectOfInterest;
		this.answered = false;

		if (parent instanceof RootTask) {
			this.answerFile = RootTaskAnswer.STATS_FILE;
		} else {
			this.answerFile = Storage.getInstance().getData().getNewAnswerFile();
			this.answerFile = new File(this.answerFile.getParentFile(), this.answerFile.getName() + getFileEnding());
		}
	}

	public boolean isAnswered() {
		return this.answered;
	}

	public void setAnswerFile(File answerFile) {
		setAnswerFile(answerFile, false);
	}

	public void setAnswerFile(File answerFile, boolean doNotCopy) {
		if (!doNotCopy) {
			// Find associated extension
			if (this.type == ANSWER_TYPE_FILE) {
				final int i = answerFile.getName().lastIndexOf('.');
				if (i > 0) {
					this.answerFile = new File(this.answerFile.getAbsolutePath().replace(FILE_ENDING_PLACEHOLDER,
							answerFile.getName().substring(i)));
				} else {
					this.answerFile = new File(this.answerFile.getAbsolutePath().replace(FILE_ENDING_PLACEHOLDER, ""));
				}
			}

			// Copy to designated file
			if (!answerFile.equals(this.answerFile)) {
				try {
					Files.copy(answerFile.toPath(), this.answerFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (final IOException e) {
					Log.warning(
							"Could not write answer to file storage: " + this.answerFile + Log.getExceptionAppendix(e));
				}
			}
		} else {
			// Replace designated file by loaded file
			this.answerFile = answerFile;
		}

		this.answered = true;
	}

	public File getAnswerFile() {
		return this.answerFile;
	}

	public Object getAnswer() {
		switch (this.type) {
			case ANSWER_TYPE_AQL: {
				Answer answer = AnswerHandler.parseXML(this.answerFile);
				if (this.subjectOfInterest != Question.QUESTION_TYPE_UNKNOWN) {
					answer = soiFilter.filterToSOI(answer, this.subjectOfInterest);
				}
				return answer;
			}
			case ANSWER_TYPE_FILE:
				return this.answerFile;
			case ANSWER_TYPE_RAW:
				return getRawContent();
			default:
				return null;
		}
	}

	public String getAnswerForQuery(boolean withQuotes) {
		if (withQuotes) {
			if (this.type == ANSWER_TYPE_RAW) {
				return "'" + getRawContent() + "'";
			} else {
				return "'" + this.answerFile.getAbsolutePath() + "'";
			}
		} else {
			if (this.type == ANSWER_TYPE_RAW) {
				return getRawContent();
			} else {
				return this.answerFile.getAbsolutePath();
			}
		}
	}

	public String getRawContent() {
		try {
			return FileHelper.getRawContent(this.answerFile);
		} catch (final IOException e) {
			Log.error("Cannot read raw data from answer file: " + this.answerFile.getAbsolutePath()
					+ Log.getExceptionAppendix(e));
			this.parent.getParent().getParent().getTaskScheduler().taskFinished(this.parent,
					Task.STATUS_EXECUTION_FAILED);
			return null;
		}
	}

	private String getFileEnding() {
		switch (this.type) {
			case ANSWER_TYPE_AQL:
				return FileHelper.FILE_ENDING_XML;
			case ANSWER_TYPE_RAW:
				return ".txt";
			case ANSWER_TYPE_FILE:
				return FILE_ENDING_PLACEHOLDER;
			default:
				return "";
		}
	}

	public int getType() {
		return this.type;
	}

	public String typeToString() {
		switch (this.type) {
			case ANSWER_TYPE_FILE: {
				return "File";
			}
			case ANSWER_TYPE_RAW: {
				return "Raw";
			}
			default:
				return "AQL";
		}
	}

	public int getSubjectOfInterest() {
		return this.subjectOfInterest;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(typeToString()).append(": ");
		if (this.answered) {
			sb.append(this.answerFile.getAbsolutePath());
			sb.append(" (");
			final String date = Helper.getDate(FileHelper.getFileProperties(this.answerFile).creationTime().toMillis(),
					Log.LOG_DATE_FORMAT);
			switch (this.type) {
				case ANSWER_TYPE_FILE: {
					sb.append(date);
				}
				case ANSWER_TYPE_RAW: {
					sb.append(getRawContent()).append(" - ").append(date);
				}
				default:
					sb.append(Helper.typeToSoi(this.subjectOfInterest)).append(" - ").append(date);
			}
			sb.append(")");
		} else {
			sb.append("Not answered!");
		}
		return sb.toString();
	}
}