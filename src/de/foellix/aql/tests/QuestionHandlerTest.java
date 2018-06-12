package de.foellix.aql.tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Question;
import de.foellix.aql.datastructure.handler.ParseException;
import de.foellix.aql.datastructure.handler.QuestionHandler;
import de.foellix.aql.datastructure.handler.QuestionParser;

public class QuestionHandlerTest {
	public static void main(final String[] args) {
		askAndOutput(
				"Flows FROM Statement('$r3.<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>(\"+49111111111\", null, $r2, null, null)') -> Method('<de.foellix.sinkapp.SinkMainActivity: void sink()>') -> Class('de.foellix.sinkapp.SinkMainActivity') -> App('E:\\test\\test.apk') TO Statement('r1 = virtualinvoke r2.<android.content.Intent: java.lang.String getStringExtra(java.lang.String)>(\"Secret\")') -> Method('<de.foellix.sinkapp.SinkMainActivity: void sink2()>') -> Class('de.foellix.sinkapp.SinkMainActivity2') -> App('E:\\test\\test2.apk') ?");
		askAndOutput("UNIFY[Flows IN App('E:\\test\\test.apk') ?, Permissions IN App('E:\\test\\test2.apk') ?]");
	}

	private static void askAndOutput(final String questionStr) {
		try {
			final InputStream input = new ByteArrayInputStream(questionStr.getBytes());
			final QuestionParser parser = new QuestionParser(input);
			parser.query();
			final QuestionHandler questionHandler = parser.getQuestionHandler();
			final Question collection = questionHandler.getCollection();
			System.out.println(collection.toString());
		} catch (final ParseException e) {
			e.printStackTrace();
			Log.error("Error while parsing question.");
		}
	}
}
