package de.foellix.aql.system.defaulttools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Sinks;
import de.foellix.aql.datastructure.Sources;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.helper.tools.SourceSinkFinder;
import de.foellix.aql.system.AQLSystem;
import de.foellix.aql.system.BackupAndReset;
import de.foellix.aql.system.defaulttools.analysistools.DefaultSourceSinkFinder;
import de.foellix.aql.system.task.TaskAnswer;
import de.foellix.aql.system.task.ToolTask;
import de.foellix.aql.system.task.ToolTaskInfo;

public class DefaultSourceSinkFinderTest {
	private static final File DIRECTLEAK_APK = new File("examples/scenarios/DirectLeak1.apk");

	@Test
	public void testIntentInformationFinder01() {
		if (new File("config.xml").exists()) {
			final SourceSinkFinder ssf = new SourceSinkFinder(DIRECTLEAK_APK);
			final Sources sources = ssf.getSources();
			final Sinks sinks = ssf.getSinks();
			assertEquals(
					"$r5 = virtualinvoke r3.<android.telephony.TelephonyManager: java.lang.String getDeviceId()>()",
					sources.getSource().get(0).getReference().getStatement().getStatementfull());
			assertEquals(
					"virtualinvoke $r4.<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>(\"+49 1234\", null, $r5, null, null)",
					sinks.getSink().get(0).getReference().getStatement().getStatementfull());
		}
	}

	@Test
	public void testRAW() {
		if (new File("config.xml").exists()) {
			boolean noException = true;

			try {
				final DefaultSourceSinkFinder dssf = new DefaultSourceSinkFinder();
				final ToolTaskInfo taskinfo = new ToolTaskInfo();
				taskinfo.setData(ToolTaskInfo.APP_APK, DIRECTLEAK_APK.getAbsolutePath());
				final ToolTask task = new ToolTask(null, taskinfo, dssf);
				task.setTaskAnswer(new TaskAnswer(task, TaskAnswer.ANSWER_TYPE_AQL, Question.QUESTION_TYPE_SOURCES));
				final File resultFile = dssf.applyAnalysisTool(task);
				final Sources sources = AnswerHandler.parseXML(resultFile).getSources();

				System.out.println(sources.getSource().get(0).getReference().getStatement().getStatementfull());

				assertEquals(
						"$r5 = virtualinvoke r3.<android.telephony.TelephonyManager: java.lang.String getDeviceId()>()",
						sources.getSource().get(0).getReference().getStatement().getStatementfull());
			} catch (final Exception e) {
				noException = false;
				e.printStackTrace();
			}

			assertTrue(noException);
		}
	}

	@Test
	public void testQuery01() {
		if (new File("config.xml").exists()) {
			BackupAndReset.reset();
			final AQLSystem system = new AQLSystem();
			final Sources sources = ((Answer) system
					.queryAndWait("Sources IN App('" + DIRECTLEAK_APK.getAbsolutePath() + "') ?").iterator().next())
							.getSources();
			assertEquals(
					"$r5 = virtualinvoke r3.<android.telephony.TelephonyManager: java.lang.String getDeviceId()>()",
					sources.getSource().get(0).getReference().getStatement().getStatementfull());
		}
	}

	@Test
	public void testQuery02() {
		if (new File("config.xml").exists()) {
			BackupAndReset.reset();
			final AQLSystem system = new AQLSystem();
			final Answer answer = (Answer) system
					.queryAndWait("UNIFY [ Sources IN App('" + DIRECTLEAK_APK.getAbsolutePath() + "') ?, Sinks IN App('"
							+ DIRECTLEAK_APK.getAbsolutePath() + "') ? ] ?")
					.iterator().next();
			assertEquals(
					"$r5 = virtualinvoke r3.<android.telephony.TelephonyManager: java.lang.String getDeviceId()>()",
					answer.getSources().getSource().get(0).getReference().getStatement().getStatementfull());
			assertEquals(
					"virtualinvoke $r4.<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>(\"+49 1234\", null, $r5, null, null)",
					answer.getSinks().getSink().get(0).getReference().getStatement().getStatementfull());
		}
	}
}