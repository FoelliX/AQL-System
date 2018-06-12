package de.foellix.aql;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.Color.YELLOW;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.fusesource.jansi.Ansi;

import de.foellix.aql.ui.gui.BottomEditor;
import de.foellix.aql.ui.gui.GUI;

public class Log {
	public static final int NONE = -1;
	public static final int IMPORTANT = 0;
	public static final int ERROR = 1;
	public static final int DEBUG_SPECIAL = 2;
	public static final int WARNING = 3;
	public static final int NORMAL = 4;
	public static final int DEBUG = 5;
	public static final int DEBUG_DETAILED = 6;

	private static int loglevel = NORMAL;
	private static File logfile = new File("log.txt");

	private static List<String> log = new ArrayList<>();

	private static boolean prefixEnabled = true;

	private static boolean shorten = false;

	public static void msg(final String msg, final int loglevel) {
		if (loglevel <= Log.loglevel) {
			System.out.println(prefix(true) + msg);

			if (loglevel == IMPORTANT) {
				logToGUI(msg);
				logToFile(msg);
			}
		}
	}

	public static void msg(final Ansi msg, final int loglevel) {
		if (loglevel <= Log.loglevel) {
			System.out.println(prefix(true) + msg);
		}
	}

	public static void warning(final String msg) {
		if (!log.contains(msg)) {
			if (Log.WARNING <= Log.loglevel) {
				System.err.println(prefix(true) + ansi().fg(YELLOW).a("Warning: " + msg).reset());
				logToGUI("Warning: " + msg);
				log.add(msg);
			}
		}
	}

	public static void error(final String msg) {
		error(msg, false);
	}

	public static void error(final String msg, boolean logToFile) {
		if (!log.contains(msg)) {
			System.err.println(prefix(true) + ansi().fg(RED).a("Error: " + msg).reset());
			logToGUI("Error: " + msg);
			if (logToFile) {
				logToFile(msg);
			}
			log.add(msg);
		}
	}

	public static void setLogLevel(final int loglevel) {
		Log.loglevel = loglevel;
	}

	public static boolean logIt(final int loglevel) {
		return (loglevel <= Log.loglevel);
	}

	public static void reset() {
		log.clear();
	}

	private static String prefix(final boolean nl) {
		if (prefixEnabled) {
			final Date date = new Date();
			final SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy - hh:mm:ss");

			return Properties.info().ABBRRVIATION + " " + format.format(date) + (nl ? "\n" : " ");
		} else {
			return "";
		}
	}

	public static void setPrefixEnabled(final boolean value) {
		Log.prefixEnabled = value;
	}

	private static void logToGUI(final String msg) {
		if (GUI.started) {
			BottomEditor.log(msg);
		}
	}

	private static void logToFile(String msg) {
		new Thread(() -> {
			try {
				Files.write(Paths.get(logfile.toURI()), (prefix(false) + msg + "\n").getBytes(),
						StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			} catch (final IOException e) {
				Log.error("Could not wirte logfile: " + logfile.getAbsolutePath());
			}
		}).start();
	}

	public static boolean getShorten() {
		return shorten;
	}

	public static void setShorten(boolean shorten) {
		Log.shorten = shorten;
	}

	public static int getLogLevel() {
		return loglevel;
	}
}
