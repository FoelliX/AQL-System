package de.foellix.aql.faketool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import de.foellix.aql.Log;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.helper.CLIHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.AQLSystem;
import de.foellix.aql.system.BackupAndReset;

@Tag("requiresExternal")
@Tag("requiresBuild")
@Tag("systemIsSetup")
public class ExternalFakeToolTest {
	private static AQLSystem aqlSystem;
	private static boolean serverReachable;

	@BeforeAll
	public static void setup() {
		Log.setLogLevel(Log.NORMAL);
		Log.setShorten(true);

		// Setup config
		CLIHelper.evaluateConfig("examples/faketool/config_faketool_external.xml");

		// Check reachability
		serverReachable = true;
		for (final Tool tool : ConfigHandler.getInstance().getAllToolsOfAnyKind()) {
			if (tool.isExternal()) {
				if (!pingHost(Helper.cut(tool.getExecute().getUrl(), "://", ":"),
						Integer.valueOf(Helper.cut(tool.getExecute().getUrl(), ":", "/", 2)), 3)) {
					serverReachable = false;
					break;
				}
			}
		}

		// Initialize AQL-System
		aqlSystem = new AQLSystem();
	}

	private static boolean pingHost(String host, int port, int timeoutInS) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(host, port), timeoutInS * 1000);
			return true;
		} catch (final IOException e) {
			Log.error("External tool not reachable after " + timeoutInS + " seconds: " + host + ":" + port);
			return false;
		}
	}

	@BeforeEach
	public void reset() {
		BackupAndReset.reset();
	}

	@Test
	public void test01() {
		if (serverReachable) {
			Object answer = null;
			boolean noException = true;
			try {
				final String query = "Flows IN App('examples/faketool/InterAppStart1.apk') ?";
				answer = aqlSystem.queryAndWait(query).iterator().next();
			} catch (final Exception e) {
				noException = false;
				e.printStackTrace();
			}

			assertTrue(noException);
			assertEquals(2, ((Answer) answer).getFlows().getFlow().size());
		}
	}

	@Test
	public void test02() {
		if (serverReachable) {
			Object answer = null;
			boolean noException = true;
			try {
				final String query = "Flows IN App('examples/faketool/InterAppEnd1.apk') ?";
				answer = aqlSystem.queryAndWait(query).iterator().next();
			} catch (final Exception e) {
				noException = false;
				e.printStackTrace();
			}

			assertTrue(noException);
			assertEquals(2, ((Answer) answer).getFlows().getFlow().size());
		}
	}

	@Test
	public void test03() {
		if (serverReachable) {
			Object answer = null;
			boolean noException = true;
			try {
				final String query = "IntentSinks IN App('examples/faketool/InterAppStart1.apk') ?";
				answer = aqlSystem.queryAndWait(query).iterator().next();
			} catch (final Exception e) {
				noException = false;
				e.printStackTrace();
			}

			assertTrue(noException);
			assertEquals(1, ((Answer) answer).getIntentsinks().getIntentsink().size());
		}
	}

	@Test
	public void test04() {
		if (serverReachable) {
			Object answer = null;
			boolean noException = true;
			try {
				final String query = "IntentSources IN App('examples/faketool/InterAppEnd1.apk') ?";
				answer = aqlSystem.queryAndWait(query).iterator().next();
			} catch (final Exception e) {
				noException = false;
				e.printStackTrace();
			}

			assertTrue(noException);
			assertEquals(1, ((Answer) answer).getIntentsources().getIntentsource().size());
		}
	}

	@Test
	public void test05() {
		if (serverReachable) {
			Object answer = null;
			boolean noException = true;
			try {
				final String query = "CONNECT [ IntentSinks IN App('examples/faketool/InterAppStart1.apk') ?, IntentSources IN App('examples/faketool/InterAppEnd1.apk') ? ]";
				answer = aqlSystem.queryAndWait(query).iterator().next();
			} catch (final Exception e) {
				noException = false;
				e.printStackTrace();
			}

			assertTrue(noException);
			assertEquals(1, ((Answer) answer).getFlows().getFlow().size());
			assertEquals(1, ((Answer) answer).getIntentsinks().getIntentsink().size());
			assertEquals(1, ((Answer) answer).getIntentsources().getIntentsource().size());
		}
	}

	@Test
	public void test06() {
		if (serverReachable) {
			Object answer = null;
			boolean noException = true;
			try {
				final String query = "Flows IN App('examples/faketool/InterAppStart1.apk' | 'FAKE') ?";
				answer = aqlSystem.queryAndWait(query).iterator().next();
			} catch (final Exception e) {
				noException = false;
				e.printStackTrace();
			}

			assertTrue(noException);
			assertEquals(2, ((Answer) answer).getFlows().getFlow().size());
		}
	}

	@Test
	public void test07() {
		if (serverReachable) {
			Object answer = null;
			boolean noException = true;
			try {
				final String query = "UNIFY [ Flows IN App('examples/faketool/InterAppStart1.apk') ?, Flows IN App('examples/faketool/InterAppEnd1.apk') ? ]";
				answer = aqlSystem.queryAndWait(query).iterator().next();
			} catch (final Exception e) {
				noException = false;
				e.printStackTrace();
			}

			assertTrue(noException);
			assertEquals(4, ((Answer) answer).getFlows().getFlow().size());
		}
	}

	@Test
	public void test08() {
		if (serverReachable) {
			Object answer = null;
			boolean noException = true;
			try {
				final String query = "UNIFY [ Flows IN App('examples/faketool/InterAppStart1.apk') ?, Flows IN App('examples/faketool/InterAppEnd1.apk') ?, MATCH [ IntentSinks IN App('examples/faketool/InterAppStart1.apk') ?, IntentSources IN App('examples/faketool/InterAppEnd1.apk') ? ]]";
				answer = aqlSystem.queryAndWait(query).iterator().next();
			} catch (final Exception e) {
				noException = false;
				e.printStackTrace();
			}

			assertTrue(noException);
			assertEquals(5, ((Answer) answer).getFlows().getFlow().size());
			assertEquals(1, ((Answer) answer).getIntentsinks().getIntentsink().size());
			assertEquals(1, ((Answer) answer).getIntentsources().getIntentsource().size());
		}
	}

	@Test
	public void test09() {
		if (serverReachable) {
			Object answer = null;
			boolean noException = true;
			try {
				final String query = "CONNECT [ Flows IN App('examples/faketool/InterAppStart1.apk') ?, Flows IN App('examples/faketool/InterAppEnd1.apk') ?, MATCH [ IntentSinks IN App('examples/faketool/InterAppStart1.apk') ?, IntentSources IN App('examples/faketool/InterAppEnd1.apk') ? ]]";
				answer = aqlSystem.queryAndWait(query).iterator().next();
			} catch (final Exception e) {
				noException = false;
				e.printStackTrace();
			}

			assertTrue(noException);
			assertEquals(6, ((Answer) answer).getFlows().getFlow().size());
			assertEquals(1, ((Answer) answer).getIntentsinks().getIntentsink().size());
			assertEquals(1, ((Answer) answer).getIntentsources().getIntentsource().size());
		}
	}

	@Test
	public void test10() {
		if (serverReachable) {
			Object answer = null;
			boolean noException = true;
			try {
				final String query = "MATCH [ IntentSinks IN App('examples/faketool/InterAppStart1.apk') ?, IntentSources IN App('examples/faketool/InterAppEnd1.apk') ? ]";
				answer = aqlSystem.queryAndWait(query).iterator().next();
			} catch (final Exception e) {
				noException = false;
				e.printStackTrace();
			}

			assertTrue(noException);
			assertEquals(1, ((Answer) answer).getFlows().getFlow().size());
			assertEquals(1, ((Answer) answer).getIntentsinks().getIntentsink().size());
			assertEquals(1, ((Answer) answer).getIntentsources().getIntentsource().size());
		}
	}
}
