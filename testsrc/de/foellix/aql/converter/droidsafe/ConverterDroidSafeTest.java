package de.foellix.aql.converter.droidsafe;

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

public class ConverterDroidSafeTest {
	private static final String EXPECTED = "*** Flows *** #1: From: Statement('$r4 = virtualinvoke $r3.<android.telephony.TelephonyManager: java.lang.String getSimSerialNumber()>()')->Method('<de.upb.fpauck.simapp.SIMAppMainActivity: void source()>')->Class('de.upb.fpauck.simapp.SIMAppMainActivity')->App('Test') To: Statement('android.app.Activity: void startActivity(android.content.Intent)')->Method('<de.upb.fpauck.simapp.SIMAppMainActivity: void source()>')->Class('de.upb.fpauck.simapp.SIMAppMainActivity')->App('Test') #2: From: Statement('$r4 = virtualinvoke $r3.<android.telephony.TelephonyManager: java.lang.String getSimSerialNumber()>()')->Method('<de.upb.fpauck.simapp.SIMAppMainActivity: void source()>')->Class('de.upb.fpauck.simapp.SIMAppMainActivity')->App('Test') To: Statement('android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)')->Method('<de.upb.fpauck.smsapp.SMSAppMainActivity: void sink()>')->Class('de.upb.fpauck.smsapp.SMSAppMainActivity')->App('Test') ";

	@Test
	public void test() {
		final QuestionPart qp = new QuestionPart();
		final App app = new App();
		app.setFile("Test");
		final Reference ref = new Reference();
		ref.setApp(app);
		qp.addReference(ref);
		final ToolTaskInfo taskInfo = new ToolTaskInfo(null, qp);

		boolean noException = true;
		try {
			// Test DroidSafe SIM + SMS App
			final File appfile1 = new File("examples/scenario1/droidsafe");
			final IConverter compiler1 = new ConverterDroidSafe();
			final Answer answer1 = compiler1.parse(appfile1, taskInfo);

			assertEquals(EXPECTED, Helper.toString(answer1).replaceAll("\n", " "));
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}
}
