package de.foellix.aql.faketool;

import java.io.File;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Attribute;
import de.foellix.aql.datastructure.Attributes;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.handler.AnswerHandler;

public class FakeConverter {
	public static Answer fakeConversion(String input) {
		final Answer answer = AnswerHandler.parseXML(new File(input));

		if (answer.getFlows() != null) {
			for (final Flow flow : answer.getFlows().getFlow()) {
				final Attribute attribute = new Attribute();
				attribute.setName("converted");
				attribute.setValue("true");

				flow.setAttributes(new Attributes());
				flow.getAttributes().getAttribute().add(attribute);
			}
		} else if (answer.getIntentsinks() != null) {
			for (final Intentsink intentsink : answer.getIntentsinks().getIntentsink()) {
				final Attribute attribute = new Attribute();
				attribute.setName("converted");
				attribute.setValue("true");

				intentsink.setAttributes(new Attributes());
				intentsink.getAttributes().getAttribute().add(attribute);
			}
		} else if (answer.getIntentsources() != null) {
			for (final Intentsource intentsource : answer.getIntentsources().getIntentsource()) {
				final Attribute attribute = new Attribute();
				attribute.setName("converted");
				attribute.setValue("true");

				intentsource.setAttributes(new Attributes());
				intentsource.getAttributes().getAttribute().add(attribute);
			}
		}

		return answer;
	}
}
