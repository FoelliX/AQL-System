package de.foellix.aql.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class HelperTest {
	@Test
	void cutFromFirstToLastTest() {
		String test = Helper.cutFromFirstToLast("$r3 := @parameter0: android.content.Intent", "<", ">");
		assertEquals("$r3 := @parameter0: android.content.Intent", test);

		test = Helper.cutFromFirstToLast(
				"staticinvoke <android.util.Log: int i(java.lang.String,java.lang.String)>(\"post\", $r6)", "<", ">");
		assertEquals("android.util.Log: int i(java.lang.String,java.lang.String)", test);
	}

	@Test
	void getAnswerFilesAsStringTest() {
		final List<FileWithHash> list = new ArrayList<>();

		final File file1 = new File("test1.xml");
		list.add(new FileWithHash(file1));
		String actual = Helper.getAnswerFilesAsString(list);
		assertEquals(file1.getAbsolutePath().replace("\\", "/"), actual);

		final File file2 = new File("test2.xml");
		list.add(new FileWithHash(file2));
		actual = Helper.getAnswerFilesAsString(list);
		assertEquals(file1.getAbsolutePath().replace("\\", "/") + ", " + file2.getAbsolutePath().replace("\\", "/"),
				actual);
	}

	@Test
	void autoformatTest() {
		String query = "Flows FROM Statement('X')->App('A.apk'|'KEYWORD') TO App('B.apk') FEATURING 'GUI', 'IAC' WITH 'Sas'='SaS.txt' ?";
		assertEquals(query, Helper.autoformat(query, false));

		query = "Flows FROM Statement('$r3.<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>(\\\"+49111111111\\\", null, $r2, null, null)')->Method('<de.foellix.sinkapp.SinkMainActivity: void sink()>')->Class('de.foellix.sinkapp.SinkMainActivity')->App('/path/to/test.apk') TO Statement('r1 = virtualinvoke r2.<android.content.Intent: java.lang.String getStringExtra(java.lang.String)>(\\\"Secret\\\")')->Method('<de.foellix.sinkapp.SinkMainActivity: void sink2()>')->Class('de.foellix.sinkapp.SinkMainActivity2')->App('/path/to/test2.apk') ?";
		assertEquals(query, Helper.autoformat(query, false));

		query = "Flows FROM App('A.apk'|'KEYWORD') TO App('B.apk') ?";
		assertEquals(query, Helper.autoformat(query, false));
	}

	@Test
	void getRunCommandAsArrayTest() {
		String[] actual = Helper.getRunCommandAsArray("test1 test2 \"test3\" test4 \"test5 test6\" test7 ?");
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < actual.length; i++) {
			sb.append(actual[i] + "#");
		}
		assertEquals("test1#test2#\"test3\"#test4#\"test5 test6\"#test7#?#", sb.toString());

		actual = Helper.getRunCommandAsArray("test1 test2 test3 test4 test5 test6 test7 ?");
		sb.setLength(0);
		for (int i = 0; i < actual.length; i++) {
			sb.append(actual[i] + "#");
		}
		assertEquals("test1#test2#test3#test4#test5#test6#test7#?#", sb.toString());
	}

	@Test
	void cleanupParametersTest() {
		String generic = "android.util.Log: int i(java.lang.String, java.lang.String)";
		generic = Helper.cleanupParameters(generic);
		assertEquals("android.util.Log: int i(java.lang.String,java.lang.String)", generic);
	}

}