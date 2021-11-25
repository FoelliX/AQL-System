package de.foellix.aql.system;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.foellix.aql.datastructure.query.DefaultQuestion;
import de.foellix.aql.datastructure.query.OperatorQuestion;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.datastructure.query.QuestionReference;
import de.foellix.aql.datastructure.query.QuestionString;
import de.foellix.aql.helper.FileHelper;

public class DirectoryResolver {
	private static final int MODE_IN = 0;
	private static final int MODE_FROM = 1;
	private static final int MODE_TO = 2;

	protected static List<Question> resolveDirectoryReference(List<Question> questions) {
		final List<Question> resolvedQuestions = new ArrayList<>();

		for (final Question question : questions) {
			if (question instanceof DefaultQuestion) {
				final DefaultQuestion castedQuestion = (DefaultQuestion) question;
				boolean changed = false;
				if ((castedQuestion.getFrom() == null && castedQuestion.getTo() == null)
						&& (castedQuestion.getIn() != null && castedQuestion.getIn().getApp().isComplete(true))) {
					final File temp = new File(castedQuestion.getIn().getApp().toStringInAnswer(false));
					if (temp.getName().contains("*")) {
						final List<File> files = FileHelper.findFilesWithAsteriskInParent(temp);
						if (!files.isEmpty()) {
							resolvedQuestions.addAll(createResolvedQuestions(castedQuestion, files, MODE_IN));
							changed = true;
						}
					}
					if (temp.exists() && temp.isDirectory()) {
						resolvedQuestions.addAll(
								createResolvedQuestions(castedQuestion, Arrays.asList(temp.listFiles()), MODE_IN));
						changed = true;
					}
				} else {
					final List<DefaultQuestion> tempResolvedQuestions = new ArrayList<>();
					if (castedQuestion.getFrom() != null && castedQuestion.getFrom().getApp().isComplete(true)) {
						final File temp = new File(castedQuestion.getFrom().getApp().toStringInAnswer(false));
						if (temp.getName().contains("*")) {
							final List<File> files = FileHelper.findFilesWithAsteriskInParent(temp);
							if (!files.isEmpty()) {
								tempResolvedQuestions.addAll(createResolvedQuestions(castedQuestion, files, MODE_FROM));
								changed = true;
							}
						}
						if (temp.exists() && temp.isDirectory()) {
							tempResolvedQuestions.addAll(createResolvedQuestions(castedQuestion,
									Arrays.asList(temp.listFiles()), MODE_FROM));
							changed = true;
						}
					} else {
						tempResolvedQuestions.add(castedQuestion);
					}
					for (final DefaultQuestion tempQuestion : tempResolvedQuestions) {
						if (tempQuestion.getTo() != null && tempQuestion.getTo().getApp().isComplete(true)) {
							final File temp = new File(tempQuestion.getTo().getApp().toStringInAnswer(false));
							if (temp.getName().contains("*")) {
								final List<File> files = FileHelper.findFilesWithAsteriskInParent(temp);
								if (!files.isEmpty()) {
									resolvedQuestions.addAll(createResolvedQuestions(tempQuestion, files, MODE_TO));
									changed = true;
								}
							}
							if (temp.exists() && temp.isDirectory()) {
								resolvedQuestions.addAll(createResolvedQuestions(tempQuestion,
										Arrays.asList(temp.listFiles()), MODE_TO));
								changed = true;
							}
						} else {
							if (changed) {
								resolvedQuestions.add(tempQuestion);
							}
						}
					}
				}
				if (!changed) {
					resolvedQuestions.add(question);
				}
			} else {
				if (question instanceof OperatorQuestion) {
					final OperatorQuestion castedQuestion = (OperatorQuestion) question;
					castedQuestion.setQuestions(resolveDirectoryReference(castedQuestion.getQuestions()));
				} else {
					resolveDirectoryReference(question);
				}
				resolvedQuestions.add(question);
			}
		}

		questions = null;
		return resolvedQuestions;
	}

	private static void resolveDirectoryReference(Question question) {
		if (question instanceof OperatorQuestion) {
			final OperatorQuestion castedQuestion = (OperatorQuestion) question;
			castedQuestion.setQuestions(resolveDirectoryReference(castedQuestion.getQuestions()));
		} else {
			for (final Question child : question.getChildren()) {
				resolveDirectoryReference(child);
			}
		}
	}

	private static List<DefaultQuestion> createResolvedQuestions(DefaultQuestion question, List<File> files, int mode) {
		final List<DefaultQuestion> createdQuestions = new ArrayList<>();
		for (final File file : files) {
			final DefaultQuestion copy = question.copy();
			final QuestionReference resolvedReference;
			if (mode == MODE_FROM) {
				resolvedReference = question.getFrom().copy();
			} else if (mode == MODE_TO) {
				resolvedReference = question.getTo().copy();
			} else {
				resolvedReference = question.getIn().copy();
			}
			resolvedReference.setApp(new QuestionString(file.getAbsolutePath()));
			if (mode == MODE_FROM) {
				copy.setFrom(resolvedReference);
			} else if (mode == MODE_TO) {
				copy.setTo(resolvedReference);
			} else {
				copy.setIn(resolvedReference);
			}
			if (!(copy.getFrom() != null && copy.getTo() != null
					&& copy.getFrom().toString().equals(copy.getTo().toString()))) {
				createdQuestions.add(copy);
			}
		}
		return createdQuestions;
	}
}