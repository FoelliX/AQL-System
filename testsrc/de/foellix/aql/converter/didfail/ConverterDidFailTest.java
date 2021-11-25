package de.foellix.aql.converter.didfail;

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

public class ConverterDidFailTest {
	private static final String EXPECTED = "*** Flows *** #1: From: Statement('<android.telephony.TelephonyManager: java.lang.String getSimSerialNumber()>')->Method('<de.upb.fpauck.simapp.SIMAppMainActivity: void source()>')->Class('de.upb.fpauck.simapp.SIMAppMainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "SIMApp.apk') To: Statement('<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>')->Method('<de.upb.fpauck.smsapp.SMSAppMainActivity: void sink()>')->Class('de.upb.fpauck.smsapp.SMSAppMainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER + "SMSApp.apk') ";

	@Test
	public void test() {
		boolean noException = true;

		final DefaultQuestion question = new DefaultQuestion();
		final QuestionReference ref1 = new QuestionReference();
		ref1.setApp(new QuestionString("/some/path/SIMApp.apk"));
		question.setFrom(ref1);
		final QuestionReference ref2 = new QuestionReference();
		ref2.setApp(new QuestionString("/some/path/SMSApp.apk"));
		question.setTo(ref2);

		try {
			// Test DidFail SIM + SMS App
			final File appfile = new File("examples/DidFail/final/simsms");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK_FROM, ref1.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.APP_APK_TO, ref2.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterDidFail();
			final Answer answer = converter.parse(task);

			ConverterTestHelper.assertForConverters(EXPECTED, Helper.toString(answer));
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		} finally {
			Storage.getInstance().getData().removeFromQuestionTaskMap(question);
		}

		assertTrue(noException);
	}
}