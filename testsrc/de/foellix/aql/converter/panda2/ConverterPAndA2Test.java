package de.foellix.aql.converter.panda2;

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

public class ConverterPAndA2Test {
	private static final String EXPECTED_1 = "*** Permissions *** #1: Name: android.permission.READ_PHONE_STATE Reference: Statement('-----de.foellix.sourceapp.R$dimen: void <init>()-----')->Method('ellix.sourceapp.R$dimen: void <init>()')->Class('ellix.sourceapp.R$dimen: void <init>()')->App('"
			+ ConverterTestHelper.PLACEHOLDER + "SIMApp.apk') Attributes: - PermissionGroup = REQUIRED ";
	private static final String EXPECTED_2 = "*** Permissions *** #1: Name: android.permission.SEND_SMS Reference: Statement('-----de.foellix.sinkapp.R$string: void <init>()-----')->Method('ellix.sinkapp.R$string: void <init>()')->Class('ellix.sinkapp.R$string: void <init>()')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "SMSApp.apk') Attributes: - PermissionGroup = REQUIRED #2: Name: android.permission.WRITE_SMS Reference: Statement('-----de.foellix.sinkapp.R$string: void <init>()-----')->Method('ellix.sinkapp.R$string: void <init>()')->Class('ellix.sinkapp.R$string: void <init>()')->App('"
			+ ConverterTestHelper.PLACEHOLDER + "SMSApp.apk') Attributes: - PermissionGroup = MISSING ";

	@Test
	public void test1() {
		boolean noException = true;

		final DefaultQuestion question = new DefaultQuestion();
		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString("/some/path/SIMApp.apk"));
		question.setIn(ref);

		try {
			// Test FD SIM App
			final File appfile = new File("examples/PAndA2/10/simsms/SIMApp_panda2_result.txt");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterPAndA2();
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
			final File appfile = new File("examples/PAndA2/10/simsms/SMSApp_panda2_result.txt");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterPAndA2();
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
}