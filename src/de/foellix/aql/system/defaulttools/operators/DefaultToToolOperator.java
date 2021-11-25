package de.foellix.aql.system.defaulttools.operators;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.google.common.io.Files;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Sink;
import de.foellix.aql.datastructure.Source;
import de.foellix.aql.datastructure.Statement;
import de.foellix.aql.datastructure.query.OperatorQuestion;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.system.storage.Storage;
import de.foellix.aql.system.task.OperatorTask;
import de.foellix.aql.system.task.TaskAnswer;

public abstract class DefaultToToolOperator extends DefaultOperator {
	@Override
	public File applyOperator(OperatorTask task) {
		// Get answer
		final Collection<Question> relatedQuestions = ((OperatorQuestion) Storage.getInstance().getData()
				.getQuestionFromQuestionTaskMap(task)).getChildren(false);
		Answer temp = null;
		if (relatedQuestions.size() == 1) {
			final TaskAnswer tempTA = Storage.getInstance().getData()
					.getTaskFromQuestionTaskMap(relatedQuestions.iterator().next()).getTaskAnswer();
			if (tempTA.getType() == TaskAnswer.ANSWER_TYPE_AQL) {
				temp = (Answer) tempTA.getAnswer();
			}
		}
		if (temp == null) {
			Log.error("Could not apply TOFD operator! (Can only be applied on a single AQL-Answer.)");
			return null;
		}

		// Convert
		final StringBuilder sb = new StringBuilder();
		final Set<String> lines = new HashSet<>();
		if (temp.getSources() != null) {
			for (final Source item : temp.getSources().getSource()) {
				lines.add(toSource(item.getReference().getStatement()));
			}
		}
		for (final String line : lines) {
			if (!sb.isEmpty()) {
				sb.append("\n");
			}
			sb.append(line);
		}
		if (!sb.isEmpty()) {
			sb.append("\n");
		}
		lines.clear();
		if (temp.getSinks() != null) {
			for (final Sink item : temp.getSinks().getSink()) {
				lines.add(toSink(item.getReference().getStatement()));
			}
		}
		for (final String line : lines) {
			if (!sb.isEmpty()) {
				sb.append("\n");
			}
			sb.append(line);
		}

		// Write to file
		try {
			final File answerFile = FileHelper.getTempFile(FileHelper.FILE_ENDING_TXT);
			Files.write(sb.toString().getBytes(), answerFile);
			return answerFile;
		} catch (final IOException e) {
			Log.error("Cannot write file: " + task.getTaskAnswer().getAnswerFile().getAbsolutePath()
					+ Log.getExceptionAppendix(e));
			return null;
		}
	}

	public abstract String toSource(Statement statement);

	public abstract String toSink(Statement statement);
}