package de.foellix.aql.datastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.foellix.aql.helper.Helper;

public class ReplacementTest {
	@Test
	public void test1() {
		// Start
		final Question q = new Question(KeywordsAndConstants.OPERATOR_COLLECTION);
		final QuestionPart qp1 = new QuestionPart();
		qp1.setMode(KeywordsAndConstants.QUESTION_TYPE_INTENTSINKS);
		q.addChild(qp1);
		// Edit
		final QuestionPart qp2 = new QuestionPart();
		qp2.setMode(KeywordsAndConstants.QUESTION_TYPE_INTENTSOURCES);
		// Replace
		Helper.replaceAllQuestionPart(q, qp1, qp2);

		assertEquals(qp2, q.getChildren().get(0));
	}

	@Test
	public void test2() {
		// Answer 1
		final Answer a1 = new Answer();
		final Intentsinks is1 = new Intentsinks();
		final Intentsink i1 = new Intentsink();
		i1.setTarget(new Target());
		i1.getTarget().getAction().add("TEST1");
		is1.getIntentsink().add(i1);
		a1.setIntentsinks(is1);

		// Answer 2
		final Answer a2 = new Answer();
		final Intentsinks is2 = new Intentsinks();
		final Intentsink i2 = new Intentsink();
		i2.setTarget(new Target());
		i2.getTarget().getAction().add("TEST2");
		is2.getIntentsink().add(i2);
		a2.setIntentsinks(is2);

		// Merge
		a1.getIntentsinks().getIntentsink().addAll(a2.getIntentsinks().getIntentsink());

		assertTrue(
				a1.getIntentsinks().getIntentsink().contains(i1) && a1.getIntentsinks().getIntentsink().contains(i2));
	}
}
