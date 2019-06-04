package de.foellix.aql.converter.flowdroid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import de.foellix.aql.converter.IConverter;
import de.foellix.aql.converter.iccta.ConverterIccTA;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.QuestionPart;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.ToolTaskInfo;

public class ConverterFDTest {
	// FIXME: Updated result resources to newer FlowDroid version. Use IccTA
	// converter until then.

	private static final String EXPECTED_1 = "*** Flows *** #1: To: Statement('virtualinvoke $r0.<de.foellix.sourceapp.SourceMainActivity: void startActivity(android.content.Intent)>($r1)')->Method('<de.foellix.sourceapp.SourceMainActivity: void source()>')->Class('de.foellix.sourceapp.SourceMainActivity')->App('Test') From: Statement('$r4 = virtualinvoke $r3.<android.telephony.TelephonyManager: java.lang.String getSimSerialNumber()>()')->Method('<de.foellix.sourceapp.SourceMainActivity: void source()>')->Class('de.foellix.sourceapp.SourceMainActivity')->App('Test') ";
	private static final String EXPECTED_2 = "*** Flows *** #1: To: Statement('virtualinvoke $r3.<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>(\"+49111111111\", null, $r2, null, null)')->Method('<de.foellix.sinkapp.SinkMainActivity: void sink()>')->Class('de.foellix.sinkapp.SinkMainActivity')->App('Test') From: Statement('$r1 = virtualinvoke $r0.<de.foellix.sinkapp.SinkMainActivity: android.content.Intent getIntent()>()')->Method('<de.foellix.sinkapp.SinkMainActivity: void sink()>')->Class('de.foellix.sinkapp.SinkMainActivity')->App('Test') ";

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
			// Test FD SIM App
			final File appfile1 = new File("examples/scenario1/SIMApp_result.txt");
			final IConverter compiler1 = new ConverterIccTA();
			final Answer answer1 = compiler1.parse(appfile1, taskInfo);

			assertEquals(EXPECTED_1, Helper.toString(answer1).replaceAll("\n", " "));

			// Test FD SMS App
			final File appfile2 = new File("examples/scenario1/SMSApp_result.txt");
			final IConverter compiler2 = new ConverterIccTA();
			final Answer answer2 = compiler2.parse(appfile2, taskInfo);

			assertEquals(EXPECTED_2, Helper.toString(answer2).replaceAll("\n", " "));
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}
}
