package de.foellix.aql.faketool;

import java.io.File;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.Sink;
import de.foellix.aql.datastructure.Sinks;
import de.foellix.aql.datastructure.Source;
import de.foellix.aql.datastructure.Sources;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.helper.Helper;

/**
 * Returns a source or sink depending on the input given
 *
 * @author FPauck
 *
 */
public class FakeAnalysisTool3 {
	public static Answer fakeAnalysis(String input) {
		final Answer answer = new Answer();

		if (input.equals(FakeTool.InterAppStart1Hash) || input.equals(FakeTool.InterAppStart1_preprocessedHash)
				|| input.equals(FakeTool.InterAppEnd1Hash) || input.equals(FakeTool.InterAppEnd1_preprocessedHash)) {
			System.out.println("Analyzing... (Tool3)");

			final App app = Helper.createApp(new File(FakeTool.APK_DIRECTORY,
					FakeTool.apkName + (input.equals(FakeTool.InterAppStart1_preprocessedHash) ? "_preprocessed" : "")
							+ FileHelper.FILE_ENDING_APK));
			final String classname = "de.foellix.aql.aqlbench.api19.interappstart1.MainActivity";

			final Reference sourceRef = new Reference();
			sourceRef.setStatement(Helper.createStatement(
					"$r4 = virtualinvoke $r3.<android.telephony.TelephonyManager: java.lang.String getSimSerialNumber()>()"));
			sourceRef.setMethod("<de.foellix.aql.aqlbench.api19.interappstart1.MainActivity: void source()>");
			sourceRef.setClassname(classname);
			sourceRef.setApp(app);

			final Reference sinkRef = new Reference();
			sinkRef.setStatement(Helper.createStatement(
					"virtualinvoke $r3.<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>(\"123456789\", null, $r2, null, null)"));
			sinkRef.setMethod(
					"<de.foellix.aql.aqlbench.api19.interappstart1.MainActivity: void sink(android.content.Intent)>");
			sinkRef.setClassname(classname);
			sinkRef.setApp(app);

			final Source source = new Source();
			source.setReference(sourceRef);
			answer.setSources(new Sources());
			answer.getSources().getSource().add(source);

			final Sink sink = new Sink();
			sink.setReference(sinkRef);
			answer.setSinks(new Sinks());
			answer.getSinks().getSink().add(sink);
		}

		return answer;
	}
}
