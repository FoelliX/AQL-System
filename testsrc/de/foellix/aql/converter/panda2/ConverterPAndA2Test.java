package de.foellix.aql.converter.panda2;

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

public class ConverterPAndA2Test {
	private static final String EXPECTED_1 = "*** Permissions *** #1: android.permission.READ_PHONE_STATE -> Statement('-----de.foellix.sourceapp.R$dimen: void <init>()-----')->Method('ellix.sourceapp.R$dimen: void <init>()')->Class('ellix.sourceapp.R$dimen: void <init>()')->App('Test') ";
	private static final String EXPECTED_2 = "*** Permissions *** #1: android.permission.SEND_SMS -> Statement('-----de.foellix.sinkapp.R$string: void <init>()-----')->Method('ellix.sinkapp.R$string: void <init>()')->Class('ellix.sinkapp.R$string: void <init>()')->App('Test') #2: android.permission.WRITE_SMS -> Statement('-----de.foellix.sinkapp.R$string: void <init>()-----')->Method('ellix.sinkapp.R$string: void <init>()')->Class('ellix.sinkapp.R$string: void <init>()')->App('Test') ";

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
			// Test Panda SIM App
			final File appfile1 = new File("examples/scenario1/SIMApp_panda2_result.txt");
			final IConverter compiler1 = new ConverterPAndA2();
			final Answer answer1 = compiler1.parse(appfile1, taskInfo);

			assertEquals(EXPECTED_1, Helper.toString(answer1).replaceAll("\n", " "));

			// Test Panda SMS App
			final File appfile2 = new File("examples/scenario1/SMSApp_panda2_result.txt");
			final IConverter compiler2 = new ConverterPAndA2();
			final Answer answer2 = compiler2.parse(appfile2, taskInfo);

			assertEquals(EXPECTED_2, Helper.toString(answer2).replaceAll("\n", " "));
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}
}
