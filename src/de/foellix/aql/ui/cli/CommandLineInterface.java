package de.foellix.aql.ui.cli;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.GREEN;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.fusesource.jansi.AnsiConsole;

import de.foellix.aql.Log;
import de.foellix.aql.Properties;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.system.BackupAndReset;
import de.foellix.aql.system.System;
import de.foellix.aql.ui.gui.GUI;
import javafx.application.Application;

public class CommandLineInterface {
	private static String config = null;
	private static String output = null;
	private static String query = null;
	private static String debug = null;
	private static long timeout = -1;

	private static boolean gui = false;
	private static boolean cw = false;
	private static boolean alwaysPreferLoading = true;

	public static void main(final String[] args) {
		// Information
		AnsiConsole.systemInstall();
		final String authorStr = "Author: " + Properties.info().AUTHOR + " (" + Properties.info().AUTHOR_EMAIL + ")";
		final String space = "                 ".substring(Math.min(Properties.info().VERSION.length() + 3, 17));
		final String centerspace = "                               ".substring(Math.min(32, authorStr.length() / 2));
		Log.msg(ansi().bold().fg(GREEN).a("           ____  _          _____           _" + space).reset()
				.a("v. " + Properties.info().VERSION).bold().fg(GREEN)
				.a("\r\n" + "     /\\   / __ \\| |        / ____|         | |                \r\n"
						+ "    /  \\ | |  | | |  _____| (___  _   _ ___| |_ ___ _ __ ___  \r\n"
						+ "   / /\\ \\| |  | | | |______\\___ \\| | | / __| __/ _ \\ '_ ` _ \\ \r\n"
						+ "  / ____ \\ |__| | |____    ____) | |_| \\__ \\ ||  __/ | | | | |\r\n"
						+ " /_/    \\_\\___\\_\\______|  |_____/ \\__, |___/\\__\\___|_| |_| |_|\r\n"
						+ "                                   __/ |                      \r\n"
						+ "                                  |___/                       \r\n")
				.reset().a("\r\n" + centerspace + authorStr + "\r\n\r\n"), Log.NORMAL);

		// Check for help parameter
		if (args == null) {
			help();
			return;
		}
		for (final String arg : args) {
			if (arg.equals("-help") || arg.equals("-h") || arg.equals("-?") || arg.equals("-man")
					|| arg.equals("-manpage")) {
				help();
				return;
			}
		}

		// Parse parameters and check for GUI
		if (args == null || args.length == 0) {
			gui = true;
		} else {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-backup") || args[i].equals("-b")) {
					BackupAndReset.backup();
				}
			}
			for (int i = 0; i < args.length; i++) {
				if (args[i].equals("-gui")) {
					gui = true;
				} else if (args[i].equals("-cw") || args[i].equals("-configwizard")) {
					gui = true;
					cw = true;
				} else if (args[i].equals("-pe") || args[i].equals("-preferexecute")) {
					alwaysPreferLoading = false;
				} else if (args[i].equals("-backup") || args[i].equals("-b")) {
					// do nothing
				} else if (args[i].equals("-reset") || args[i].equals("-re") || args[i].equals("-r")) {
					BackupAndReset.reset();
				} else {
					if (args[i].equals("-c") || args[i].equals("-cfg") || args[i].equals("-config")) {
						config = args[i + 1];
					} else if (args[i].equals("-o") || args[i].equals("-out") || args[i].equals("-output")) {
						output = args[i + 1];
					} else if (args[i].equals("-q") || args[i].equals("-query")) {
						query = args[i + 1];
					} else if (args[i].equals("-d") || args[i].equals("-debug")) {
						debug = args[i + 1];
					} else if (args[i].equals("-t") || args[i].equals("-timeout")) {
						final String readTimeout = args[i + 1];
						if (readTimeout.contains("h")) {
							timeout = Integer.parseInt(readTimeout.replaceAll("h", "")) * 3600;
						} else if (readTimeout.contains("m")) {
							timeout = Integer.parseInt(readTimeout.replaceAll("m", "")) * 60;
						} else {
							timeout = Integer.parseInt(readTimeout.replaceAll("s", ""));
						}
					} else {
						java.lang.System.exit(0);
					}
					i++;
				}
			}
		}

		// Debug settings
		if (debug != null) {
			if (debug.equals("normal")) {
				Log.setLogLevel(Log.NORMAL);
			} else if (debug.equals("short")) {
				Log.setLogLevel(Log.NORMAL);
				Log.setShorten(true);
			} else if (debug.equals("warning")) {
				Log.setLogLevel(Log.WARNING);
			} else if (debug.equals("error")) {
				Log.setLogLevel(Log.ERROR);
			} else if (debug.equals("debug")) {
				Log.setLogLevel(Log.DEBUG);
			} else if (debug.equals("detailed")) {
				Log.setLogLevel(Log.DEBUG_DETAILED);
			} else if (debug.equals("special")) {
				Log.setLogLevel(Log.DEBUG_SPECIAL);
			} else {
				Log.setLogLevel(Integer.valueOf(debug).intValue());
			}
		}

		// Load custom config
		final File configFile = new File(config);
		if (config != null && configFile.exists()) {
			ConfigHandler.getInstance().setConfig(configFile);
		} else {
			Log.warning("Configuration file does not exist: " + configFile.getAbsolutePath());
		}

		if (!gui) {
			// Setup system
			final System aqlSystem = new System();
			aqlSystem.getScheduler().setAlwaysPreferLoading(alwaysPreferLoading);
			aqlSystem.getScheduler().setTimeout(timeout);
			if (output != null) {
				aqlSystem.getAnswerReceivers().add(new OutputWriter(new File(output)));
			}

			// Ask query
			if (query != null) {
				aqlSystem.query(query);
			} else {
				Log.error("Please specify a query (e.g. -q \"Flows IN App('path/to/file.apk') ?\")");
			}
		} else {
			// Start GUI
			GUI.alwaysPreferLoading = alwaysPreferLoading;
			GUI.timeout = timeout;
			GUI.showConfigWizard = cw;

			if (query != null) {
				new Thread() {
					@Override
					public void run() {
						try {
							while (!GUI.started) {
								sleep(100);
							}
						} catch (final InterruptedException e) {
							e.printStackTrace();
						}

						GUI.editor.setContent(query);
					}
				}.start();
			}

			Application.launch(GUI.class, args);
		}
	}

	private static void help() {
		try {
			final File manpage = new File("manpage");
			final byte[] encoded = Files.readAllBytes(Paths.get(manpage.toURI()));
			Log.msg(new String(encoded, StandardCharsets.UTF_8), Log.NORMAL);
		} catch (final IOException e) {
			Log.error("Could not find manpage file.");
		}
	}
}
