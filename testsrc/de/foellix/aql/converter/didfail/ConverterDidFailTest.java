package de.foellix.aql.converter.didfail;

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

public class ConverterDidFailTest {
	private static final String EXPRECTED = "*** Flows *** #1: From: Statement('<android.telephony.TelephonyManager: java.lang.String getSimSerialNumber()>')->Method('<de.upb.fpauck.simapp.SIMAppMainActivity: void source()>')->Class('de.upb.fpauck.simapp.SIMAppMainActivity')->App('/some/path/SIMApp.apk') To: Statement('<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>')->Method('<de.upb.fpauck.smsapp.SMSAppMainActivity: void sink()>')->Class('de.upb.fpauck.smsapp.SMSAppMainActivity')->App('/some/path/SMSApp.apk') ";

	@Test
	public void test() {
		final QuestionPart qp = new QuestionPart();
		final ToolTaskInfo taskInfo = new ToolTaskInfo(null, qp);

		boolean noException = true;
		try {
			// Test DidFail SIM&SMS App
			final App app1 = new App();
			app1.setFile("/some/path/SIMApp.apk");
			final Reference ref1 = new Reference();
			ref1.setApp(app1);
			qp.addReference(ref1);
			final App app2 = new App();
			app2.setFile("/some/path/SMSApp.apk");
			final Reference ref2 = new Reference();
			ref2.setApp(app2);
			qp.addReference(ref2);

			final File appfile1 = new File("examples/scenario1/didfail");
			final IConverter compiler1 = new ConverterDidFail();
			final Answer answer1 = compiler1.parse(appfile1, taskInfo);

			assertEquals(EXPRECTED, Helper.toString(answer1).replaceAll("\n", " "));
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}
}
