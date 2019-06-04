package de.foellix.aql.ui.gui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.foellix.aql.Log;
import de.foellix.aql.ui.cli.CommandLineInterface;
import javafx.application.Platform;

public class GUITest {
	private boolean noException = true;

	@Test
	public void test() {
		Log.setLogLevel(Log.DEBUG);

		new Thread(() -> {
			try {
				while (!GUI.started) {
					Thread.sleep(10);
				}
				Platform.exit();
				assertTrue(this.noException);
			} catch (final InterruptedException e) {
				// do nothing
			}
		}).start();

		final String query = "FILTER [UNIFY [Flows FROM App('SIMApp.apk') TO App('SMSApp.apk') ?, UNIFY [Permissions IN App('SIMApp.apk') ?, Permissions IN App('SMSApp.apk') ?]]]";
		try {
			CommandLineInterface.main(new String[] { "-gui", "-q", query });
		} catch (final Exception e) {
			this.noException = false;
			e.printStackTrace();
		}

		assertTrue(this.noException);
	}
}
