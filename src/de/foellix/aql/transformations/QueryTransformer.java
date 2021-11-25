package de.foellix.aql.transformations;

import java.util.HashSet;
import java.util.Set;

import de.foellix.aql.datastructure.query.Query;
import de.foellix.aql.datastructure.query.Question;

public class QueryTransformer {
	public static boolean transform(Query query, Question question) {
		return transform(query, question, new HashSet<>());
	}

	public static boolean transform(Query query, Question question, Set<String> transformed) {
		transformed.add(question.toString());
		Question newQuestion = null;
		Question tempQuestion = transformQuestion(query, question);
		while (tempQuestion != null) {
			newQuestion = tempQuestion;
			tempQuestion = transformQuestion(query, tempQuestion);
		}
		boolean changed = false;
		if (newQuestion != null) {
			for (final Question leaf : newQuestion.getLeafs()) {
				if (!transformed.contains(leaf.toString())) {
					transform(query, leaf, transformed);
				}
			}
			if (question.getParent() != null) {
				if (!transformed.contains(question.getParent().toString())) {
					transform(query, question.getParent(), transformed);
				}
			}
			changed = true;
		} else {
			for (final Question leaf : question.getLeafs()) {
				if (!transformed.contains(leaf.toString())) {
					if (transform(query, leaf, transformed)) {
						changed = true;
					}
				}
			}
		}
		return changed;
	}

	private static Question transformQuestion(Query query, Question question) {
		final QuestionTransformer transformer = new QuestionTransformer(question);
		if (transformer.isApplicable()) {
			final Question newQuestion = transformer.transform();
			if (newQuestion != question) {
				if (question.getParent() != null) {
					question.getParent().replaceChild(question, newQuestion);
				} else if (query.getQuestions().contains(question)) {
					query.getQuestions().remove(question);
					query.getQuestions().add(newQuestion);
				}
				return newQuestion;
			}
		}
		return null;
	}
}