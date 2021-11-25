package de.foellix.aql.system;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import de.foellix.aql.Log;

public class ProcessWrapperTest {
	@Test
	public void test01() {
		boolean noException = true;

		final String system = System.getProperty("os.name").toLowerCase();
		if (system.contains("win")) {
			Log.setLogLevel(Log.DEBUG_DETAILED);
			try {
				final ProcessBuilder pb = new ProcessBuilder(new String[] { "ping", "/t", "localhost" });
				final ProcessWrapper pw = new ProcessWrapper(pb.start());

				new Thread(() -> {
					try {
						Thread.sleep(1000);
						pw.cancel();
					} catch (final InterruptedException e) {
						pw.cancel();
					}
				}).start();

				pw.waitFor();
			} catch (final IOException e) {
				noException = false;
			}
		}

		assertTrue(noException);
	}
}
