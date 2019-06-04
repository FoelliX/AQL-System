package de.foellix.aql.faketool;

import java.io.File;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsinks;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.Intentsources;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.Target;
import de.foellix.aql.helper.Helper;

public class FakeAnalysisTool2 {
	public static Answer fakeAnalysis(String input, File apkDirectory) {
		final Answer answer = new Answer();

		if (input.contains("Start1.apk") || input.contains("End1.apk")) {
			if (input.contains("Start1.apk")) {
				final Reference reference = new Reference();
				reference.setStatement(Helper.createStatement(
						"virtualinvoke r0.<de.foellix.aql.aqlbench.api19.interappstart1.MainActivity: void startActivityForResult(android.content.Intent,int)>(r1, b1)"));
				reference.setMethod("<de.foellix.aql.aqlbench.api19.interappstart1.MainActivity: void source()>");
				reference.setClassname("de.foellix.aql.aqlbench.api19.interappstart1.MainActivity");
				reference.setApp(Helper.createApp(new File(apkDirectory, "InterAppStart1.apk")));

				final Target target = new Target();
				target.getAction().add("de.foellix.aql.aqlbench.LEAK");

				final Intentsink sink = new Intentsink();
				sink.setReference(reference);
				sink.setTarget(target);

				answer.setIntentsinks(new Intentsinks());
				answer.getIntentsinks().getIntentsink().add(sink);
			} else if (input.contains("End1.apk")) {
				final Reference reference = new Reference();
				reference.setStatement(Helper.createStatement(
						"r3 = virtualinvoke r2.<android.content.Intent: java.lang.String getStringExtra(java.lang.String)>(\"secret\")"));
				reference.setMethod("<de.foellix.aql.aqlbench.api19.interappend1.MainActivity: void sink()>");
				reference.setClassname("de.foellix.aql.aqlbench.api19.interappend1.MainActivity");
				reference.setApp(Helper.createApp(new File(apkDirectory, "InterAppEnd1.apk")));

				final Target target = new Target();
				target.getAction().add("de.foellix.aql.aqlbench.LEAK");
				target.getCategory().add("android.intent.category.DEFAULT");

				final Intentsource source = new Intentsource();
				source.setReference(reference);
				source.setTarget(target);

				answer.setIntentsources(new Intentsources());
				answer.getIntentsources().getIntentsource().add(source);
			}
		}

		return answer;
	}
}
