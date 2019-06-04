package de.foellix.aql.converter.iccta;

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

public class ConverterIccTATest {
	private static final String EXPECTED = "*** Flows *** #1: To: Statement('virtualinvoke $r0.<de.foellix.sourceapp.SourceMainActivity: void startActivity(android.content.Intent)>($r1)')->Method('<de.foellix.sourceapp.SourceMainActivity: void source()>')->Class('de.foellix.sourceapp.SourceMainActivity')->App('Test') From: Statement('$r4 = virtualinvoke $r3.<android.telephony.TelephonyManager: java.lang.String getSimSerialNumber()>()')->Method('<de.foellix.sourceapp.SourceMainActivity: void source()>')->Class('de.foellix.sourceapp.SourceMainActivity')->App('Test') ";

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
			// Test IccTa SIM + SMS App
			final File appfile1 = new File("examples/scenario1/SIMApp_result.txt"); // FIXME: Generate real iccta
																					// output.
			final IConverter compiler1 = new ConverterIccTA();
			final Answer answer1 = compiler1.parse(appfile1, taskInfo);

			assertEquals(EXPECTED, Helper.toString(answer1).replaceAll("\n", " "));
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}
}
