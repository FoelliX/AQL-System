package de.foellix.aql.converter.amandroid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import de.foellix.aql.converter.IConverter;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.QuestionPart;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.ToolTaskInfo;

public class ConverterAmandroidTest {
	private static final String EXPECTED = "*** Flows *** #1: From: Statement('android.telephony.TelephonyManager: java.lang.String getDeviceId()')->Method('<edu.mit.icc_action_string_operations.OutFlowActivity: void onCreate(android.os.Bundle)>')->Class('edu.mit.icc_action_string_operations.OutFlowActivity')->App('Test') To: Statement('android.util.Log: int i(java.lang.String,java.lang.String)')->Method('<edu.mit.icc_action_string_operations.IsolateActivity: void onCreate(android.os.Bundle)>')->Class('edu.mit.icc_action_string_operations.IsolateActivity')->App('Test') #2: From: Statement('android.telephony.TelephonyManager: java.lang.String getDeviceId()')->Method('<edu.mit.icc_action_string_operations.OutFlowActivity: void onCreate(android.os.Bundle)>')->Class('edu.mit.icc_action_string_operations.OutFlowActivity')->App('Test') To: Statement('android.util.Log: int i(java.lang.String,java.lang.String)')->Method('<edu.mit.icc_action_string_operations.InFlowActivity: void onCreate(android.os.Bundle)>')->Class('edu.mit.icc_action_string_operations.InFlowActivity')->App('Test') ";

	@Test
	public void test() {
		QuestionPart qp = new QuestionPart();
		App app = new App();
		app.setFile("Test");
		Reference ref = new Reference();
		ref.setApp(app);
		qp.addReference(ref);
		final ToolTaskInfo taskInfo = new ToolTaskInfo(null, qp);

		boolean noException = true;
		try {
			// Test Amandroid DroidBench ActivityCommunication2 App
			qp = new QuestionPart();
			app = Helper.createApp("/some/path/ActivityCommunication2.apk");
			ref = new Reference();
			ref.setApp(app);
			qp.addReference(ref);

			final File appfile1 = new File("examples/ActivityCommunication2/amandroid2/result/AppData.txt");
			final IConverter compiler1 = new ConverterAmandroid();
			final Answer answer1 = compiler1.parse(appfile1, taskInfo);

			assertEquals(EXPECTED, Helper.toString(answer1).replaceAll("\n", " "));
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}
}
