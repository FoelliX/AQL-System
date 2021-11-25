package de.foellix.aql.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class JawaHelperTest {
	// Should: Landroid/util/Log;.d:(Ljava/lang/String;Ljava/lang/String;)I -> _SINK_
	// Is: Landroid/util/Log;.d:(Ljava/lang/String;Ljava/lang/String;)I -> _SINK_ 1|2
	// OK

	// Should: Landroid/telephony/SmsManager;.sendTextMessage:(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/app/PendingIntent;Landroid/app/PendingIntent;)V -> _SINK_ 3
	// Is: Landroid/telephony/SmsManager;.sendTextMessage:(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroid/app/PendingIntent;Landroid/app/PendingIntent;)V -> _SINK_ 1|2|3|4|5
	// OK

	@Test
	void toJawa1Test() {
		final String jawa = JawaHelper.toJawa(Helper.createStatement(
				"<android.accounts.AccountManager: android.accounts.AccountManager get(android.content.Context)>"));
		assertEquals(
				"Landroid/accounts/AccountManager;.get:(Landroid/content/Context;)Landroid/accounts/AccountManager;",
				jawa);
	}
}