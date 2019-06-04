package de.foellix.aql.converter.horndroid;

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

public class ConverterHDTest {
	private static final String EXPECTED = "*** Permissions *** #1: HORNDROID_SUCCESSFULLY_EXECUTED -> No Reference *** Flows *** #1: To: Statement('<unknown.pkg.and.Class: unknown.return.Type sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>')->Method('onCreate(android.os.Bundle)')->Class('de.ecspride.MainActivity')->App('Test') ";

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
			// Test DirectLeak1
			final File appfile = new File("examples/HornDroid_DirectLeak1.apk.json");
			final IConverter converter = new ConverterHD();
			final Answer answer = converter.parse(appfile, taskInfo);

			assertEquals(EXPECTED, Helper.toString(answer).replaceAll("\n", " "));
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}
}
