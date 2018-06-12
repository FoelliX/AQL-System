package de.foellix.aql.tests;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsinks;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.Question;
import de.foellix.aql.datastructure.QuestionPart;
import de.foellix.aql.helper.Helper;

public class ReplacementTest {
	public static void main(final String[] args) {
		// Test 1
		System.out.println("--- TEST 1 ---");
		final Question q = new Question(KeywordsAndConstants.OPERATOR_COLLECTION);
		final QuestionPart qp1 = new QuestionPart();
		qp1.setMode(KeywordsAndConstants.QUESTION_TYPE_INTENTSINKS);
		q.addChild(qp1);

		final QuestionPart qp2 = new QuestionPart();
		qp2.setMode(KeywordsAndConstants.QUESTION_TYPE_INTENTSOURCES);

		System.out.println("*** Original ***\n" + q.toString());

		Helper.replaceAllQuestionPart(q, qp1, qp2);

		System.out.println("*** Replaced ***\n" + q.toString());

		// Test 2
		System.out.println("--- TEST 2 ---");
		final Answer a1 = new Answer();
		final Intentsinks is1 = new Intentsinks();
		final Intentsink i1 = new Intentsink();
		i1.getTarget().setAction("TEST1");
		is1.getIntentsink().add(i1);
		a1.setIntentsinks(is1);
		System.out.println(Helper.toString(a1));

		final Answer a2 = new Answer();
		final Intentsinks is2 = new Intentsinks();
		final Intentsink i2 = new Intentsink();
		i2.getTarget().setAction("TEST2");
		is2.getIntentsink().add(i2);
		a2.setIntentsinks(is2);
		System.out.println(Helper.toString(a2));

		a1.getIntentsinks().getIntentsink().addAll(a2.getIntentsinks().getIntentsink());

		System.out.println(Helper.toString(a1));
	}
}
