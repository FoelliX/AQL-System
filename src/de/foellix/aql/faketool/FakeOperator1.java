package de.foellix.aql.faketool;

import java.io.File;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.helper.KeywordsAndConstantsHelper;
import de.foellix.aql.system.defaulttools.operators.DefaultUnifyOperator;

public class FakeOperator1 {
	public static Answer fakeOperation(String input) {
		final Answer answer = new Answer();

		final Reference from = new Reference();
		from.setType(KeywordsAndConstantsHelper.REFERENCE_TYPE_FROM);
		from.setStatement(Helper.createStatement(
				"virtualinvoke r0.<de.foellix.aql.aqlbench.api19.interappstart1.MainActivity: void startActivityForResult(android.content.Intent,int)>(r1, b1)"));
		from.setMethod("<de.foellix.aql.aqlbench.api19.interappstart1.MainActivity: void source()>");
		from.setClassname("de.foellix.aql.aqlbench.api19.interappstart1.MainActivity");
		from.setApp(Helper.createApp(new File(FakeTool.APK_DIRECTORY, "InterAppStart1.apk")));

		final Reference to = new Reference();
		to.setType(KeywordsAndConstantsHelper.REFERENCE_TYPE_TO);
		to.setStatement(Helper.createStatement(
				"r3 = virtualinvoke r2.<android.content.Intent: java.lang.String getStringExtra(java.lang.String)>(\"secret\")"));
		to.setMethod("<de.foellix.aql.aqlbench.api19.interappend1.MainActivity: void sink()>");
		to.setClassname("de.foellix.aql.aqlbench.api19.interappend1.MainActivity");
		to.setApp(Helper.createApp(new File(FakeTool.APK_DIRECTORY, "InterAppEnd1.apk")));

		final Flow fakeFlow = new Flow();
		fakeFlow.getReference().add(from);
		fakeFlow.getReference().add(to);

		answer.setFlows(new Flows());
		answer.getFlows().getFlow().add(fakeFlow);

		final DefaultUnifyOperator operator = new DefaultUnifyOperator();

		final String[] inputs = input.split(",");
		if (inputs[0].startsWith(" ")) {
			inputs[0] = inputs[0].substring(1);
		}
		if (inputs[1].startsWith(" ")) {
			inputs[1] = inputs[1].substring(1);
		}

		return operator.unify(AnswerHandler.parseXML(new File(inputs[0])),
				operator.unify(AnswerHandler.parseXML(new File(inputs[1])), answer));
	}
}