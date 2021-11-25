package de.foellix.aql.converter.horndroid;

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

public class ConverterHDTest {
	private final static String EXPECTED = "*** Flows *** #1: From: No Reference To: Statement('<unknown.pkg.and.Class: unknown.return.Type sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>()')->Method('onCreate(android.os.Bundle)')->Class('de.ecspride.MainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER + "DirectLeak1.apk') ";

	@Test
	public void test() {
		boolean noException = true;

		final DefaultQuestion question = new DefaultQuestion();
		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString("/some/path/DirectLeak1.apk"));
		question.setIn(ref);

		try {
			// Test HD DroidBench's DirectLeak1 App
			final File appfile = new File("examples/HornDroid/001/DirectLeak1/DirectLeak1.apk.json");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterHD();
			final Answer answer = converter.parse(task);

			System.out.println(Helper.toString(answer));

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