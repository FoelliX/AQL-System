package de.foellix.aql.converter.amandroid;

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

public class ConverterAmandroid2Test {
	private static final String EXPECTED_1 = "*** Flows *** #1: From: Statement('<android.telephony.TelephonyManager: java.lang.String getDeviceId()>')->Method('<de.ecspride.MainActivity: void onCreate(android.os.Bundle)>')->Class('de.ecspride.MainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "DirectLeak1.apk') To: Statement('<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>')->Method('<de.ecspride.MainActivity: void onCreate(android.os.Bundle)>')->Class('de.ecspride.MainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER + "DirectLeak1.apk') ";
	private static final String EXPECTED_2 = "b1e47b47600f6d03de8db0d0c2e7f1a9f6523c377a049410fbb70b2a293a169d";
	private static final String EXPECTED_3 = "4cd13639495954e80d3860a9d3a85c5e2865c9c1faa94cf1b1b87e99ddd59b2d";

	@Test
	public void test01() {
		boolean noException = true;

		final DefaultQuestion question = new DefaultQuestion();
		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString("/some/path/DirectLeak1.apk"));
		question.setIn(ref);

		try {
			// Test Amandroid DroidBench's DirectLeak1 App
			final File appfile = new File("examples/Amandroid/320/DirectLeak1/result/AppData.txt");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterAmandroid2();
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

	@Test
	public void test02() {
		boolean noException = true;

		final DefaultQuestion question = new DefaultQuestion();
		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString("/some/path/backflash.apk"));
		question.setIn(ref);

		try {
			// Test Amandroid TaintBench's backflash App
			final File appfile = new File("examples/Amandroid/320/backflash/result/AppData.txt");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterAmandroid2();
			final Answer answer = converter.parse(task);

			ConverterTestHelper.assertForConvertersByHash(EXPECTED_2, "backflash", answer);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		} finally {
			Storage.getInstance().getData().removeFromQuestionTaskMap(question);
		}

		assertTrue(noException);
	}

	@Test
	public void test03() {
		boolean noException = true;

		final DefaultQuestion question = new DefaultQuestion();
		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString("/some/path/ImplicitIntentMatching3.apk"));
		question.setIn(ref);

		try {
			// Test Amandroid IntentMatching's ImplicitIntentMatching3 App
			final File appfile = new File("examples/Amandroid/320/ImplicitIntentMatching3/result/AppData.txt");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterAmandroid2();
			final Answer answer = converter.parse(task);

			ConverterTestHelper.assertForConvertersByHash(EXPECTED_3, "ImplicitIntentMatching3", answer);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		} finally {
			Storage.getInstance().getData().removeFromQuestionTaskMap(question);
		}

		assertTrue(noException);
	}
}