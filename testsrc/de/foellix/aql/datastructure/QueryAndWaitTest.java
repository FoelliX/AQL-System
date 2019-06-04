package de.foellix.aql.datastructure;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.Test;

import de.foellix.aql.Log;
import de.foellix.aql.config.ConfigHandler;

public class QueryAndWaitTest {
	@Test
	public void test() {
		Log.setLogLevel(Log.NORMAL);

		if (ConfigHandler.getInstance().getToolByName("PAndA2") != null) {
			final de.foellix.aql.system.System aqlSystem = new de.foellix.aql.system.System();
			aqlSystem.setStoreAnswers(false);
			final File simApp = new File("examples/simsms/SIMApp.apk");
			final File smsApp = new File("examples/simsms/SMSApp.apk");
			final Answer answer = aqlSystem.queryAndWait("UNIFY [ Permissions IN App('" + simApp.getAbsolutePath()
					+ "') ?, Permissions IN App('" + smsApp.getAbsolutePath() + "') ? ]").iterator().next();

			assertNotNull(answer);
		}
	}
}
