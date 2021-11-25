package de.foellix.aql.converter.flowdroid;

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

public class ConverterFDTest {
	private static final String EXPECTED_1 = "*** Flows *** #1: From: Statement('$r5 = virtualinvoke $r3.<android.telephony.TelephonyManager: java.lang.String getDeviceId()>()', 17)->Method('<de.ecspride.MainActivity: void onCreate(android.os.Bundle)>')->Class('de.ecspride.MainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER
			+ "DirectLeak1.apk') To: Statement('virtualinvoke $r4.<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>(\"+49 1234\", null, $r5, null, null)', 17)->Method('<de.ecspride.MainActivity: void onCreate(android.os.Bundle)>')->Class('de.ecspride.MainActivity')->App('"
			+ ConverterTestHelper.PLACEHOLDER + "DirectLeak1.apk') ";

	@Test
	public void test01() {
		boolean noException = true;

		final DefaultQuestion question = new DefaultQuestion();
		final QuestionReference ref = new QuestionReference();
		ref.setApp(new QuestionString("/some/path/DirectLeak1.apk"));
		question.setIn(ref);

		try {
			// Test FD SIM App
			final File appfile = new File("examples/FlowDroid/271/DirectLeak1/DirectLeak1.xml");
			final ConverterTaskInfo taskInfo = new ConverterTaskInfo();
			taskInfo.setData(ConverterTaskInfo.APP_APK, ref.getApp().toStringInAnswer(false));
			taskInfo.setData(ConverterTaskInfo.RESULT_FILE, appfile.getAbsolutePath());
			final ConverterTask task = new ConverterTask(null, taskInfo, null);

			Storage.getInstance().getData().putIntoQuestionTaskMap(question, task);

			final IConverter converter = new ConverterFD();
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