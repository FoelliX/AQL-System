package de.foellix.aql.converter.iccta;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import de.foellix.aql.converter.ConverterTestHelper;
import de.foellix.aql.converter.IConverter;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.query.DefaultQuestion;
import de.foellix.aql.datastructure.query.QuestionReference;
import de.foellix.aql.datastructure.query.QuestionString;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.storage.Storage;
import de.foellix.aql.system.task.ConverterTask;
import de.foellix.aql.system.task.ConverterTaskInfo;

public class ConverterIccTATest {
	private static final String EXPECTED_1 = "*** Flows *** #1: From: Statement('$r4 = virtualinvoke $r3.<android.telephony.TelephonyManager: java.lang.String getSimSerialNumber()>()')->Method('<de.foellix.sourceapp.SourceMainActivity: void source()>')->Class('de.foellix.sourceapp.SourceMainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "SIMApp.apk') To: Statement('virtualinvoke $r0.<de.foellix.sourceapp.SourceMainActivity: void startActivity(android.content.Intent)>($r1)')->Method('<de.foellix.sourceapp.SourceMainActivity: void source()>')->Class('de.foellix.sourceapp.SourceMainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER + "SIMApp.apk') ";
	private static final String EXPECTED_2 = "*** Flows *** #1: From: Statement('$r4 = virtualinvoke $r3.<android.telephony.TelephonyManager: java.lang.String getSimSerialNumber()>()', 34)->Method('<de.foellix.sourceapp.SourceMainActivity: void source()>')->Class('de.foellix.sourceapp.SourceMainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "SIMApp.apk') To: Statement('virtualinvoke $r3.<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>(\"+49111111111\", null, $r2, null, null)', 34)->Method('<de.foellix.sinkapp.SinkMainActivity: void sink()>')->Class('de.foellix.sinkapp.SinkMainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER + "SIMApp.apk') ";

	@Test
	public void test01() {
		boolean noException = true;

		final DefaultQuestion question = new DefaultQuestion();
		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString("/some/path/SIMApp.apk"));
		question.setIn(ref);

		try {
			// Test IccTA SIM + SMS App
			final File appfile = new File("examples/IccTA/ReproDroid/simsms/IccTA_SIM_SMS_App.txt");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterIccTA();
			final Answer answer = converter.parse(task);

			ConverterTestHelper.assertForConverters(EXPECTED_2, Helper.toString(answer));
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		} finally {
			Storage.getInstance().getData().removeFromQuestionTaskMap(question);
		}

		assertTrue(noException);
	}

	@Test
	public void test02() {
		// Test for old version (used for ReproDroid) of FlowDroid
		boolean noException = true;

		final DefaultQuestion question = new DefaultQuestion();
		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString("/some/path/SIMApp.apk"));
		question.setIn(ref);

		try {
			// Test IccTA SIM + SMS App
			final File appfile = new File("examples/FlowDroid/ReproDroid/simsms/SIMApp_result.txt");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterIccTA();
			final Answer answer = converter.parse(task);

			ConverterTestHelper.assertForConverters(EXPECTED_1, Helper.toString(answer));
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		} finally {
			Storage.getInstance().getData().removeFromQuestionTaskMap(question);
		}

		assertTrue(noException);
	}
}