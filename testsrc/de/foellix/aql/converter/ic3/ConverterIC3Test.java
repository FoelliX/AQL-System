package de.foellix.aql.converter.ic3;

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

public class ConverterIC3Test {
	private static final String EXPECTED_1 = "*** Intents *** #1: Action: de.foellix.CALLSINK Reference: Statement('virtualinvoke r0.<de.foellix.sourceapp.SourceMainActivity: void startActivity(android.content.Intent)>(r1)')->Method('<de.foellix.sourceapp.SourceMainActivity: void source()>')->Class('de.foellix.sourceapp.SourceMainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "SIMApp.apk') *** IntentFilters ***  *** IntentSinks *** #1: Action: de.foellix.CALLSINK Reference: Statement('virtualinvoke r0.<de.foellix.sourceapp.SourceMainActivity: void startActivity(android.content.Intent)>(r1)')->Method('<de.foellix.sourceapp.SourceMainActivity: void source()>')->Class('de.foellix.sourceapp.SourceMainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER + "SIMApp.apk') ";
	private static final String EXPECTED_2 = "*** IntentFilters *** #1: Action: de.foellix.CALLSINK Category: android.intent.category.DEFAULT Reference: Class('de.foellix.sinkapp.SinkMainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "SMSApp.apk') #2: Action: android.intent.action.MAIN Category: android.intent.category.LAUNCHER Reference: Class('de.foellix.sinkapp.SinkMainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "SMSApp.apk') *** IntentSources *** #1: Class: de.foellix.sinkapp.SinkMainActivity Reference: Statement('r1 = virtualinvoke r2.<android.content.Intent: java.lang.String getStringExtra(java.lang.String)>(\"Secret\")')->Method('<de.foellix.sinkapp.SinkMainActivity: void sink()>')->Class('de.foellix.sinkapp.SinkMainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "SMSApp.apk') #2: Action: de.foellix.CALLSINK Category: android.intent.category.DEFAULT Reference: Statement('r1 = virtualinvoke r2.<android.content.Intent: java.lang.String getStringExtra(java.lang.String)>(\"Secret\")')->Method('<de.foellix.sinkapp.SinkMainActivity: void sink()>')->Class('de.foellix.sinkapp.SinkMainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "SMSApp.apk') #3: Action: android.intent.action.MAIN Category: android.intent.category.LAUNCHER Reference: Statement('r1 = virtualinvoke r2.<android.content.Intent: java.lang.String getStringExtra(java.lang.String)>(\"Secret\")')->Method('<de.foellix.sinkapp.SinkMainActivity: void sink()>')->Class('de.foellix.sinkapp.SinkMainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER + "SMSApp.apk') ";
	private static final String EXPECTED_3 = "bcd285ed59b178d5b7edf08c61c361e0910795b1c6389fdf1978aa4ea038a8f5";
	private static final String EXPECTED_4 = "*** IntentFilters *** #1: Action: amandroid.impliciticctest_action.testaction Category: android.intent.category.DEFAULT Reference: Class('org.arguslab.icc_implicit_src_sink.FooActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "icc_implicit_src_sink.apk') *** IntentSources *** #1: Class: org.arguslab.icc_implicit_src_sink.FooActivity Reference: Statement('r8 = virtualinvoke r2.<android.content.Intent: java.lang.String getStringExtra(java.lang.String)>(r7)')->Method('<org.arguslab.icc_implicit_src_sink.FooActivity: void onCreate(android.os.Bundle)>')->Class('org.arguslab.icc_implicit_src_sink.FooActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "icc_implicit_src_sink.apk') #2: Action: amandroid.impliciticctest_action.testaction Category: android.intent.category.DEFAULT Reference: Statement('r8 = virtualinvoke r2.<android.content.Intent: java.lang.String getStringExtra(java.lang.String)>(r7)')->Method('<org.arguslab.icc_implicit_src_sink.FooActivity: void onCreate(android.os.Bundle)>')->Class('org.arguslab.icc_implicit_src_sink.FooActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER + "icc_implicit_src_sink.apk') ";

	@Test
	public void test1() {
		boolean noException = true;

		final DefaultQuestion question = new DefaultQuestion();
		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString("/some/path/SIMApp.apk"));
		question.setIn(ref);

		try {
			// Test FD SIM App
			final File appfile = new File("examples/IC3/020/simsms/SIMApp.dat");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterIC3();
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
	public void test2() {
		boolean noException = true;

		final DefaultQuestion question = new DefaultQuestion();
		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString("/some/path/SMSApp.apk"));
		question.setIn(ref);

		try {
			// Test FD SIM App
			final File appfile = new File("examples/IC3/020/simsms/SMSApp.dat");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterIC3();
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
	public void test3() {
		boolean noException = true;

		final DefaultQuestion question = new DefaultQuestion();
		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString("/some/path/com.codalata.craigslistchecker.apk"));
		question.setIn(ref);

		try {
			// Test FD SIM App
			final File appfile = new File("examples/IC3/020/other/com.codalata.craigslistchecker_71.dat");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterIC3();
			final Answer answer = converter.parse(task);

			ConverterTestHelper.assertForConvertersByHash(EXPECTED_3, "com.codalata.craigslistchecker", answer);
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		} finally {
			Storage.getInstance().getData().removeFromQuestionTaskMap(question);
		}

		assertTrue(noException);
	}

	@Test
	public void test4() {
		boolean noException = true;

		final DefaultQuestion question = new DefaultQuestion();
		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString("/some/path/icc_implicit_src_sink.apk"));
		question.setIn(ref);

		try {
			// Test FD SIM App
			final File appfile = new File("examples/IC3/020/other/org.arguslab.icc_implicit_src_sink_1.dat");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterIC3();
			final Answer answer = converter.parse(task);

			ConverterTestHelper.assertForConverters(EXPECTED_4, Helper.toString(answer));
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		} finally {
			Storage.getInstance().getData().removeFromQuestionTaskMap(question);
		}

		assertTrue(noException);
	}
}