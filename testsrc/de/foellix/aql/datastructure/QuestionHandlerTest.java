package de.foellix.aql.datastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

import de.foellix.aql.datastructure.handler.ParseException;
import de.foellix.aql.datastructure.handler.QuestionHandler;
import de.foellix.aql.datastructure.handler.QuestionParser;
import de.foellix.aql.datastructure.query.Query;

public class QuestionHandlerTest {
	private static final String TEST1APK = new File("E:/test/test.apk").getAbsolutePath();
	private static final String TEST2APK = new File("E:/test/test.apk").getAbsolutePath();

	@Test
	public void test1() {
		final String query = "Flows FROM Statement('$r3.<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>(\"+49111111111\", null, $r2, null, null)')->Method('<de.foellix.sinkapp.SinkMainActivity: void sink()>')->Class('de.foellix.sinkapp.SinkMainActivity')->App('"
				+ TEST1APK
				+ "') TO Statement('r1 = virtualinvoke r2.<android.content.Intent: java.lang.String getStringExtra(java.lang.String)>(\"Secret\")')->Method('<de.foellix.sinkapp.SinkMainActivity: void sink2()>')->Class('de.foellix.sinkapp.SinkMainActivity2')->App('"
				+ TEST2APK + "') ?";
		assertEquals(query, askAndOutput(query));
	}

	@Test
	public void test2() {
		final String query = "UNIFY [ Flows IN App('" + TEST1APK + "') ?, Permissions IN App('" + TEST2APK + "') ? ] ?";
		assertEquals(query, askAndOutput(query));
	}

	private static String askAndOutput(final String questionStr) {
		try {
			final InputStream input = new ByteArrayInputStream(questionStr.getBytes());
			final QuestionParser parser = new QuestionParser(input);
			parser.queries();
			final QuestionHandler questionHandler = parser.getQuestionHandler();
			final Query query = questionHandler.getQuery();
			return query.toString().replaceAll("\n", " ").replaceAll("\t", "").replaceAll("\r", "");
		} catch (final ParseException e) {
			return null;
		}
	}
}
