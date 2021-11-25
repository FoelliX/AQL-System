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

public class ConverterAmandroidTest {
	// System.out.println("\"" + Helper.toString(answer).replace("\n", " ").replace("D:/some/path/", "\" + ConverterTestHelper.PLACEHOLDER + \"") + "\"");

	private static final String EXPECTED_1 = "*** Flows *** #1: From: Statement('<android.telephony.TelephonyManager: java.lang.String getDeviceId()>')->Method('<de.ecspride.MainActivity: void onCreate(android.os.Bundle)>')->Class('de.ecspride.MainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "DirectLeak1.apk') To: Statement('<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>')->Method('<de.ecspride.MainActivity: void onCreate(android.os.Bundle)>')->Class('de.ecspride.MainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER + "DirectLeak1.apk') ";
	private static final String EXPECTED_2 = "*** Flows *** #1: From: Statement('<android.location.Location: double getLatitude()>')->Method('<de.ecspride.LocationLeak1$MyLocationListener: void onLocationChanged(android.location.Location)>')->Class('de.ecspride.LocationLeak1$MyLocationListener')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "LocationLeak1.apk') To: Statement('<android.util.Log: int d(java.lang.String,java.lang.String)>')->Method('<de.ecspride.LocationLeak1: void onResume()>')->Class('de.ecspride.LocationLeak1')->App('"
			+ ConverterTestHelper.PLACEHOLDER + "LocationLeak1.apk') ";
	private static final String EXPECTED_3 = "*** Flows *** #1: From: Statement('<android.telephony.TelephonyManager: java.lang.String getDeviceId()>')->Method('<de.ecspride.IntentSink1: void onCreate(android.os.Bundle)>')->Class('de.ecspride.IntentSink1')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "IntentSink1.apk') To: Statement('<de.ecspride.IntentSink1: void setResult(Landroid.content.Intent)>')->Method('<de.ecspride.IntentSink1: void onCreate(android.os.Bundle)>')->Class('de.ecspride.IntentSink1')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "IntentSink1.apk') #2: From: Statement('<android.telephony.TelephonyManager: java.lang.String getDeviceId()>')->Method('<de.ecspride.IntentSink1: void onCreate(android.os.Bundle)>')->Class('de.ecspride.IntentSink1')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "IntentSink1.apk') To: Statement('<android.content.Intent: android.content.Intent putExtra(java.lang.String,java.lang.String)>')->Method('<de.ecspride.IntentSink1: void onCreate(android.os.Bundle)>')->Class('de.ecspride.IntentSink1')->App('"
			+ ConverterTestHelper.PLACEHOLDER + "IntentSink1.apk') ";
	private static final String EXPECTED_4 = "8ab23576816b6f7a78a7cc2b5ed424c1c7c0fd52d9b6db062af075647e11e359";
	private static final String EXPECTED_4A = "da1eb6cc6823f1ddec5bffe0f9f8533b1bafa93a8b4acd38c25fcdc8b7a85edb";
	private static final String EXPECTED_5 = "4f7f855b79f55b604e80075f513512aeaddc625b700458a3e153a6a9aa2abbae";
	private static final String EXPECTED_5_OLD = "*** Flows *** #1: From: Statement('<android.telephony.TelephonyManager: java.lang.String getDeviceId()>')->Method('<edu.mit.icc_action_string_operations.OutFlowActivity: void onCreate(android.os.Bundle)>')->Class('edu.mit.icc_action_string_operations.OutFlowActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "ActivityCommunication2.apk') To: Statement('<edu.mit.icc_action_string_operations.OutFlowActivity: void startActivity(android.content.Intent)>')->Method('<edu.mit.icc_action_string_operations.OutFlowActivity: void onCreate(android.os.Bundle)>')->Class('edu.mit.icc_action_string_operations.OutFlowActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "ActivityCommunication2.apk') #2: From: Statement('<android.telephony.TelephonyManager: java.lang.String getDeviceId()>')->Method('<edu.mit.icc_action_string_operations.OutFlowActivity: void onCreate(android.os.Bundle)>')->Class('edu.mit.icc_action_string_operations.OutFlowActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "ActivityCommunication2.apk') To: Statement('<android.util.Log: int i(java.lang.String,java.lang.String)>')->Method('<edu.mit.icc_action_string_operations.IsolateActivity: void onCreate(android.os.Bundle)>')->Class('edu.mit.icc_action_string_operations.IsolateActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "ActivityCommunication2.apk') #3: From: Statement('<android.telephony.TelephonyManager: java.lang.String getDeviceId()>')->Method('<edu.mit.icc_action_string_operations.OutFlowActivity: void onCreate(android.os.Bundle)>')->Class('edu.mit.icc_action_string_operations.OutFlowActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "ActivityCommunication2.apk') To: Statement('<android.util.Log: int i(java.lang.String,java.lang.String)>')->Method('<edu.mit.icc_action_string_operations.InFlowActivity: void onCreate(android.os.Bundle)>')->Class('edu.mit.icc_action_string_operations.InFlowActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "ActivityCommunication2.apk') *** IntentFilters *** #1: Action: edu.mit.icc_action_string_operations.EDIT Category: android.intent.category.DEFAULT Reference: Class('edu.mit.icc_action_string_operations.IsolateActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "ActivityCommunication2.apk') #2: Action: android.intent.action.MAIN Category: android.intent.category.LAUNCHER Reference: Class('edu.mit.icc_action_string_operations.OutFlowActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "ActivityCommunication2.apk') #3: Action: edu.mit.icc_action_string_operations.ACTION Category: android.intent.category.DEFAULT Reference: Class('edu.mit.icc_action_string_operations.InFlowActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER + "ActivityCommunication2.apk') ";

	@Test
	public void test01() {
		boolean noException = true;

		final DefaultQuestion question = new DefaultQuestion();
		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString("/some/path/DirectLeak1.apk"));
		question.setIn(ref);

		try {
			// Test Amandroid DroidBench's DirectLeak1 App
			final File appfile = new File("examples/Amandroid/312/DirectLeak1/result/AppData.txt");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterAmandroid();
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
		ref.setApp(new QuestionString("/some/path/LocationLeak1.apk"));
		question.setIn(ref);

		try {
			// Test Amandroid DroidBench's DirectLeak1 App
			final File appfile = new File("examples/Amandroid/312/LocationLeak1/result/AppData.txt");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterAmandroid();
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
	public void test03() {
		boolean noException = true;

		final DefaultQuestion question = new DefaultQuestion();
		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString("/some/path/IntentSink1.apk"));
		question.setIn(ref);

		try {
			// Test Amandroid DroidBench's DirectLeak1 App
			final File appfile = new File("examples/Amandroid/312/IntentSink1/result/AppData.txt");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterAmandroid();
			final Answer answer = converter.parse(task);

			ConverterTestHelper.assertForConverters(EXPECTED_3, Helper.toString(answer));
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		} finally {
			Storage.getInstance().getData().removeFromQuestionTaskMap(question);
		}

		assertTrue(noException);
	}

	@Test
	public void test04() {
		boolean noException = true;

		final DefaultQuestion question = new DefaultQuestion();
		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString("/some/path/backflash.apk"));
		question.setIn(ref);

		try {
			// Test Amandroid TaintBench's backflash App
			final File appfile = new File("examples/Amandroid/312/backflash/result/AppData.txt");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterAmandroid();
			final Answer answer = converter.parse(task);

			ConverterTestHelper.assertForConvertersByHash(EXPECTED_4, "backflash", answer);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		} finally {
			Storage.getInstance().getData().removeFromQuestionTaskMap(question);
		}

		assertTrue(noException);
	}

	@Test
	public void test04a() {
		boolean noException = true;

		final DefaultQuestion question = new DefaultQuestion();
		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString("/some/path/backflash.apk"));
		question.setIn(ref);

		try {
			// Test Amandroid TaintBench's backflash App
			final File appfile = new File("examples/Amandroid/312/backflash2/result/AppData.txt");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterAmandroid();
			final Answer answer = converter.parse(task);

			ConverterTestHelper.assertForConvertersByHash(EXPECTED_4A, "backflash", answer);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		} finally {
			Storage.getInstance().getData().removeFromQuestionTaskMap(question);
		}

		assertTrue(noException);
	}

	@Test
	public void test05() {
		boolean noException = true;

		final DefaultQuestion question = new DefaultQuestion();
		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString("/some/path/ImplicitIntentMatching3.apk"));
		question.setIn(ref);

		try {
			// Test Amandroid IntentMatching's ImplicitIntentMatching3 App
			final File appfile = new File("examples/Amandroid/312/ImplicitIntentMatching3/result/AppData.txt");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterAmandroid();
			final Answer answer = converter.parse(task);

			ConverterTestHelper.assertForConvertersByHash(EXPECTED_5, "ImplicitIntentMatching3", answer);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		} finally {
			Storage.getInstance().getData().removeFromQuestionTaskMap(question);
		}

		assertTrue(noException);
	}

	// Older tests
	@Test
	public void test05old() {
		boolean noException = true;

		final DefaultQuestion question = new DefaultQuestion();
		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString("/some/path/ActivityCommunication2.apk"));
		question.setIn(ref);

		try {
			// Test Amandroid DroidBench's ActivityCommunication2 App
			final File appfile = new File("examples/Amandroid/312/ActivityCommunication2/result/AppData.txt");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterAmandroid();
			final Answer answer = converter.parse(task);

			ConverterTestHelper.assertForConverters(EXPECTED_5_OLD, Helper.toString(answer));
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		} finally {
			Storage.getInstance().getData().removeFromQuestionTaskMap(question);
		}

		assertTrue(noException);
	}
}