package de.foellix.aql.faketool;

import java.io.File;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.DefaultOperator;

public class FakeOperator {
	public static Answer fakeOperation(String input, File apkDirectory) {
		final Answer answer = new Answer();

		final Reference from = new Reference();
		from.setType(KeywordsAndConstants.REFERENCE_TYPE_FROM);
		from.setStatement(Helper.createStatement(
				"virtualinvoke r0.<de.foellix.aql.aqlbench.api19.interappstart1.MainActivity: void startActivityForResult(android.content.Intent,int)>(r1, b1)"));
		from.setMethod("<de.foellix.aql.aqlbench.api19.interappstart1.MainActivity: void source()>");
		from.setClassname("de.foellix.aql.aqlbench.api19.interappstart1.MainActivity");
		from.setApp(Helper.createApp(new File(apkDirectory, "InterAppStart1.apk")));

		final Reference to = new Reference();
		to.setType(KeywordsAndConstants.REFERENCE_TYPE_TO);
		to.setStatement(Helper.createStatement(
				"r3 = virtualinvoke r2.<android.content.Intent: java.lang.String getStringExtra(java.lang.String)>(\"secret\")"));
		to.setMethod("<de.foellix.aql.aqlbench.api19.interappend1.MainActivity: void sink()>");
		to.setClassname("de.foellix.aql.aqlbench.api19.interappend1.MainActivity");
		to.setApp(Helper.createApp(new File(apkDirectory, "InterAppEnd1.apk")));

		final Flow fakeFlow = new Flow();
		fakeFlow.getReference().add(from);
		fakeFlow.getReference().add(to);

		answer.setFlows(new Flows());
		answer.getFlows().getFlow().add(fakeFlow);

		System.err.println("TEST 1: " + input.split(", ")[0]);
		System.err.println("TEST 2: " + input.split(", ")[1]);

		return DefaultOperator.unify(AnswerHandler.parseXML(new File(input.split(", ")[0])),
				DefaultOperator.unify(AnswerHandler.parseXML(new File(input.split(", ")[1])), answer));
	}
}