package de.foellix.aql.system.defaulttools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Intents;
import de.foellix.aql.datastructure.Intentsources;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.helper.tools.IntentInformationFinder;
import de.foellix.aql.system.AQLSystem;
import de.foellix.aql.system.BackupAndReset;
import de.foellix.aql.system.defaulttools.analysistools.DefaultIntentInformationFinder;
import de.foellix.aql.system.task.TaskAnswer;
import de.foellix.aql.system.task.ToolTask;
import de.foellix.aql.system.task.ToolTaskInfo;

public class DefaultIntentInformationFinderTest {
	private static final File SIM_APP_APK = new File("examples/scenarios/simsms/SIMApp.apk");
	private static final File SMS_APP_APK = new File("examples/scenarios/simsms/SMSApp.apk");

	@Test
	public void testIntentInformationFinder01() {
		if (new File("config.xml").exists()) {
			final IntentInformationFinder iif = new IntentInformationFinder(SIM_APP_APK);
			final Intents intents = iif.getIntents();
			assertEquals(
					"specialinvoke r1.<android.content.Intent: void <init>(java.lang.String)>(\"de.foellix.CALLSINK\")",
					intents.getIntent().get(0).getReference().getStatement().getStatementfull());
		}
	}

	@Test
	public void testIntentInformationFinder02() {
		if (new File("config.xml").exists()) {
			final IntentInformationFinder iif = new IntentInformationFinder(SMS_APP_APK);
			final Intentsources intentsources = iif.getIntentsources();
			assertEquals(
					"$r1 = virtualinvoke r0.<de.foellix.sinkapp.SinkMainActivity: android.content.Intent getIntent()>()",
					intentsources.getIntentsource().get(0).getReference().getStatement().getStatementfull());
			assertEquals(
					"$r2 = virtualinvoke $r1.<android.content.Intent: java.lang.String getStringExtra(java.lang.String)>(\"Secret\")",
					intentsources.getIntentsource().get(1).getReference().getStatement().getStatementfull());
		}
	}

	@Test
	public void testRAW() {
		if (new File("config.xml").exists()) {
			boolean noException = true;

			try {
				final DefaultIntentInformationFinder diif = new DefaultIntentInformationFinder();
				final ToolTaskInfo taskinfo = new ToolTaskInfo();
				taskinfo.setData(ToolTaskInfo.APP_APK, SIM_APP_APK.getAbsolutePath());
				final ToolTask task = new ToolTask(null, taskinfo, diif);
				task.setTaskAnswer(new TaskAnswer(task, TaskAnswer.ANSWER_TYPE_RAW, Question.QUESTION_TYPE_INTENTS));
				final File resultFile = diif.applyAnalysisTool(task);
				final Intents intents = AnswerHandler.parseXML(resultFile).getIntents();
				assertEquals(
						"specialinvoke r1.<android.content.Intent: void <init>(java.lang.String)>(\"de.foellix.CALLSINK\")",
						intents.getIntent().get(0).getReference().getStatement().getStatementfull());
			} catch (final Exception e) {
				noException = false;
			}

			assertTrue(noException);
		}
	}

	@Test
	public void testQuery() {
		if (new File("config.xml").exists()) {
			BackupAndReset.reset();
			final AQLSystem system = new AQLSystem();
			final Intents intents = ((Answer) system
					.queryAndWait("Intents IN App('" + SIM_APP_APK.getAbsolutePath() + "') ?").iterator().next())
							.getIntents();
			assertEquals(
					"specialinvoke r1.<android.content.Intent: void <init>(java.lang.String)>(\"de.foellix.CALLSINK\")",
					intents.getIntent().get(0).getReference().getStatement().getStatementfull());
		}
	}
}