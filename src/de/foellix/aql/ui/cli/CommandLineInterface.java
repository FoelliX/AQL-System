package de.foellix.aql.ui.cli;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.GREEN;

import java.io.File;

import org.slf4j.LoggerFactory;

import de.foellix.aql.Log;
import de.foellix.aql.LogSilencer;
import de.foellix.aql.Properties;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.helper.CLIHelper;
import de.foellix.aql.helper.ManpageReader;
import de.foellix.aql.system.AQLSystem;
import de.foellix.aql.system.BackupAndReset;
import de.foellix.aql.system.Options;
import de.foellix.aql.ui.gui.GUI;
import de.foellix.aql.ui.gui.StartViewer;
import javafx.application.Application;
import javafx.application.Platform;

public class CommandLineInterface {
	private static String output = null;
	private static String query = null;
	private static boolean gui = false;

	public static void main(final String[] args) {
		// Hide SLF4J-Logger output (otherwise triggered e.g. by Soot)
		try (LogSilencer s = new LogSilencer()) {
			LoggerFactory.getLogger(CommandLineInterface.class);
		}

		// Information
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

		// Check resources availability
		CLIHelper.checkResources();

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
		final Options options = new Options();
		boolean firstConfig = true;
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
				} else if (args[i].equals("-ns") || args[i].equals("-noSplash")) {
					options.setNoSplashScreen(true);
				} else if (args[i].equals("-nr") || args[i].equals("-noRetry")) {
					options.setRetry(false);
				} else if (args[i].equals("-cw") || args[i].equals("-configwizard")) {
					gui = true;
					options.setShowConfigWizard(true);
				} else if (args[i].equals("-v") || args[i].equals("-view")) {
					options.setViewAnswer(true);
				} else if (args[i].equals("-dg") || args[i].equals("-draw") || args[i].equals("-drawGraph")) {
					options.setDrawGraphs(true);
				} else if (args[i].equals("-backup") || args[i].equals("-b")) {
					// do nothing
				} else if (args[i].equals("-reset") || args[i].equals("-re") || args[i].equals("-r")) {
					BackupAndReset.reset();
					if (args.length > i + 1 && (args[i + 1].equals("output") || args[i + 1].equals("temp")
							|| args[i + 1].equals("answers"))) {
						BackupAndReset.resetOutputDirectories();
						i++;
					}
				} else {
					if (args[i].equals("-c") || args[i].equals("-cfg") || args[i].equals("-config")) {
						if (firstConfig) {
							CLIHelper.evaluateConfig(args[i + 1]);
							firstConfig = false;
						} else {
							final File oldConfig = ConfigHandler.getInstance().getConfigFile();
							CLIHelper.evaluateConfig(args[i + 1]);
							ConfigHandler.getInstance().mergeWith(oldConfig);
						}
					} else if (args[i].equals("-o") || args[i].equals("-out") || args[i].equals("-output")) {
						output = args[i + 1];
					} else if (args[i].equals("-q") || args[i].equals("-query")) {
						query = args[i + 1];
					} else if (args[i].equals("-rules")) {
						CLIHelper.evaluateRules(args[i + 1]);
					} else if (args[i].equals("-d") || args[i].equals("-debug")) {
						Log.setLogLevel(CLIHelper.evaluateLogLevel(args[i + 1], false));
					} else if (args[i].equals("-df") || args[i].equals("-dtf") || args[i].equals("-debugToFile")) {
						Log.setLogToFileLevel(CLIHelper.evaluateLogLevel(args[i + 1], false));
					} else if (args[i].equals("-t") || args[i].equals("-timeout")) {
						options.setTimeout(CLIHelper.evaluateTimeout(args[i + 1]));
						if (args.length > i + 2) {
							final int mode = CLIHelper.evaluateTimeoutMode(args[i + 2]);
							if (mode != -1) {
								options.setTimeoutMode(mode);
								i++;
							}
						}
					} else {
						Log.error("Unknown launch parameter (" + args[i] + "). Canceling execution!");
						System.exit(0);
					}
					i++;
				}
			}
		}

		if (!gui) {
			// Deactivate GUI log buffer
			Log.disableGUIlogging();

			// Setup system
			final AQLSystem aqlSystem = new AQLSystem(options);
			if (output != null) {
				aqlSystem.getAnswerReceivers().add(new OutputWriter(new File(output)));
			}

			// View Answer
			if (!options.getDrawGraphs() && options.getViewAnswer()) {
				aqlSystem.getAnswerReceivers().add(new StartViewer(args, options));
			}

			// Ask query
			if (query != null) {
				if (options.getDrawGraphs() && options.getViewAnswer()) {
					startGUI(options, args, true);
				} else {
					aqlSystem.query(query);
				}
			} else {
				Log.error("Please specify a query (e.g. -q \"Flows IN App('path/to/file.apk') ?\")");
			}
		} else {
			startGUI(options, args, false);
		}
	}

	private static void startGUI(Options options, String[] args, boolean runQueryImmediately) {
		GUI.options = options;
		if (query != null) {
			new Thread() {
				@Override
				public void run() {
					try {
						while (!GUI.started || GUI.editor == null || !GUI.editor.isReady()) {
							sleep(100);
						}
						Platform.runLater(() -> {
							GUI.editor.setContent(query);
						});
						if (runQueryImmediately) {
							while (GUI.editor == null || GUI.editor.getContent() == null
									|| GUI.editor.getContent().isEmpty()) {
								sleep(100);
							}
							GUI.ask();
						}
					} catch (final InterruptedException e) {
						Log.error("Execution was interrupted when waiting for GUI to start.");
					}
				}
			}.start();
		}

		// TODO: (After 2.0.0 release) Check if future javafx versions still require this silencing (last tested with 18-ea+4; Part 1/2)
		if (!Log.logIt(Log.DEBUG_DETAILED)) {
			Log.setSilence(true);
		}
		Application.launch(GUI.class, args);
	}

	private static void help() {
		Log.msg(ManpageReader.getInstance().getManpageContent(), Log.NORMAL);
	}
}
