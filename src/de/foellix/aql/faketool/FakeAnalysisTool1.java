package de.foellix.aql.faketool;

import java.io.File;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.Helper;

public class FakeAnalysisTool1 {
	public static Answer fakeAnalysis(String input, File apkDirectory) {
		final Answer answer = new Answer();
		answer.setFlows(new Flows());

		final Flow fakeFlow1 = new Flow();
		final Reference from1 = new Reference();
		from1.setType(KeywordsAndConstants.REFERENCE_TYPE_FROM);
		final Reference to1 = new Reference();
		to1.setType(KeywordsAndConstants.REFERENCE_TYPE_TO);

		final Flow fakeFlow2 = new Flow();
		final Reference from2 = new Reference();
		from2.setType(KeywordsAndConstants.REFERENCE_TYPE_FROM);
		final Reference to2 = new Reference();
		to2.setType(KeywordsAndConstants.REFERENCE_TYPE_TO);

		if (input.contains("Start1.apk") || input.contains("End1.apk")) {
			if (input.contains("Start1.apk")) {
				final App app = Helper.createApp(new File(apkDirectory, "InterAppStart1.apk"));
				final String classname = "de.foellix.aql.aqlbench.api19.interappstart1.MainActivity";

				// Flow 1
				from1.setStatement(Helper.createStatement(
						"$r4 = virtualinvoke $r3.<android.telephony.TelephonyManager: java.lang.String getSimSerialNumber()>()"));
				from1.setMethod("<de.foellix.aql.aqlbench.api19.interappstart1.MainActivity: void source()>");
				from1.setClassname(classname);
				from1.setApp(app);

				to1.setStatement(Helper.createStatement(
						"virtualinvoke $r0.<de.foellix.aql.aqlbench.api19.interappstart1.MainActivity: void startActivityForResult(android.content.Intent,int)>($r1, 1)"));
				to1.setMethod("<de.foellix.aql.aqlbench.api19.interappstart1.MainActivity: void source()>");
				to1.setClassname(classname);
				to1.setApp(app);

				// Flow 2
				from2.setStatement(Helper.createStatement("$r1 := @parameter2: android.content.Intent"));
				from2.setMethod(
						"<de.foellix.aql.aqlbench.api19.interappstart1.MainActivity: void onActivityResult(int,int,android.content.Intent)>");
				from2.setClassname(classname);
				from2.setApp(app);

				to2.setStatement(Helper.createStatement(
						"virtualinvoke $r3.<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>(\"123456789\", null, $r2, null, null)"));
				to2.setMethod(
						"<de.foellix.aql.aqlbench.api19.interappstart1.MainActivity: void sink(android.content.Intent)>");
				to2.setClassname(classname);
				to2.setApp(app);
			} else if (input.contains("End1.apk")) {
				final App app = Helper.createApp(new File(apkDirectory, "InterAppEnd1.apk"));
				final String method = "<de.foellix.aql.aqlbench.api19.interappend1.MainActivity: void sink()>";
				final String classname = "de.foellix.aql.aqlbench.api19.interappend1.MainActivity";

				// Flow 1
				from1.setStatement(Helper.createStatement(
						"$r1 = virtualinvoke $r0.<de.foellix.aql.aqlbench.api19.interappend1.MainActivity: android.content.Intent getIntent()>()"));
				from1.setMethod(method);
				from1.setClassname(classname);
				from1.setApp(app);

				to1.setStatement(Helper.createStatement(
						"virtualinvoke $r3.<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>(\"123456789\", null, $r2, null, null)"));
				to1.setMethod(method);
				to1.setClassname(classname);
				to1.setApp(app);

				// Flow 2
				from2.setStatement(Helper.createStatement(
						"$r1 = virtualinvoke $r0.<de.foellix.aql.aqlbench.api19.interappend1.MainActivity: android.content.Intent getIntent()>()"));
				from2.setMethod(method);
				from2.setClassname(classname);
				from2.setApp(app);

				to2.setStatement(Helper.createStatement(
						"virtualinvoke $r0.<de.foellix.aql.aqlbench.api19.interappend1.MainActivity: void setResult(int,android.content.Intent)>(1, $r1)"));
				to2.setMethod(method);
				to2.setClassname(classname);
				to2.setApp(app);
			}

			fakeFlow1.getReference().add(from1);
			fakeFlow1.getReference().add(to1);
			fakeFlow2.getReference().add(from2);
			fakeFlow2.getReference().add(to2);
			answer.getFlows().getFlow().add(fakeFlow1);
			answer.getFlows().getFlow().add(fakeFlow2);
		}

		return answer;
	}
}