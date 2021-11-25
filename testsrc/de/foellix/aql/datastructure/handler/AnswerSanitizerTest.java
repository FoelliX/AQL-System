package de.foellix.aql.datastructure.handler;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import de.foellix.aql.datastructure.Answer;

public class AnswerSanitizerTest {
	@Test
	public void test01() {
		boolean noException = true;

		try {
			final File input = new File("examples/answers/flowdroid_flows_dirty.xml");

			final Answer answer = AnswerHandler.parseXML(AnswerHandler.sanitize(input));

			assertNotNull(answer);
			assertNotNull(answer.getFlows());
			assertNotNull(answer.getFlows().getFlow());
			assertFalse(answer.getFlows().getFlow().isEmpty());
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}
}
