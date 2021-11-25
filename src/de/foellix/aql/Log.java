package de.foellix.aql;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.CYAN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.Color.YELLOW;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import de.foellix.aql.helper.Helper;
import de.foellix.aql.ui.gui.GUI;
import de.foellix.aql.ui.gui.LogMsg;
import de.foellix.aql.ui.gui.LogViewer;

public class Log {
	public static final int NONE = -1;
	public static final int IMPORTANT = 0;
	public static final int ERROR = 1;
	public static final int DEBUG_SPECIAL = 2;
	public static final int WARNING = 3;
	public static final int NORMAL = 4;
	public static final int DEBUG = 5;
	public static final int DEBUG_DETAILED = 6;
	public static final int VERBOSE = 7;
	public static final int ALL = 7;

	public static final int TYPE_MSG = 0;
	public static final int TYPE_NOTE = 1;
	public static final int TYPE_WARNING = 2;
	public static final int TYPE_ERROR = 3;

	public static final int SILENCE_LEVEL_NONE = -1;
	public static final int SILENCE_LEVEL_MSG = 0;
	public static final int SILENCE_LEVEL_NOTE = 1;
	public static final int SILENCE_LEVEL_WARNING = 2;
	public static final int SILENCE_LEVEL_ERROR = 3;
	public static final int SILENCE_LEVEL_ALL = 4;

	public static final String LOG_DATE_FORMAT = "MM/dd/yyyy - HH:mm:ss";

	private static int logLevel = NORMAL;
	private static int logToFileLevel = IMPORTANT;
	private static File logfile = new File("log.txt");
	private static PrintWriter logfileWriter;
	private static Set<String> log = new HashSet<>();
	private static boolean prefixEnabled = true;
	private static boolean shorten = false;
	private static int silence = -1;
	private static int ignoredSilenceLevel;
	private static boolean alwaysIgnoreSilence = false;
	private static boolean ansiInstalled = false;
	private static Queue<LogMsg> logBuffer = new LinkedList<>();

	private static PrintStream out = System.out;
	private static PrintStream err = System.err;
	private static PrintStream dummy = new PrintStream(new OutputStream() {
		@Override
		public void write(int b) throws IOException {
			// do nothing
		}
	});
	private static Lock lock = new ReentrantLock();

	public static void msg(final String msg, final int loglevel) {
		msg(msg, loglevel, true);
	}

	public static void msg(final String msg, final int loglevel, boolean newLine) {
		msg(msg, loglevel, newLine, false);
	}

	public static void msg(final String msg, final int loglevel, boolean newLine, boolean ignoreSilence) {
		ignoreSilenceStart(TYPE_MSG, ignoreSilence);
		if (loglevel <= Log.logLevel) {
			outPrint(prefix(msg.contains("\n")) + msg + (newLine ? "\n" : ""));
			logToGUI(new LogMsg(msg, (loglevel == Log.IMPORTANT ? LogMsg.TYPE_IMPORTANT : LogMsg.TYPE_MSG)));
		}
		if (loglevel <= Log.logToFileLevel) {
			outOrErrPrintFile(prefix(msg.contains("\n")) + msg + (newLine ? "\n" : ""));
		}
		ignoreSilenceEnd(TYPE_MSG);
	}

	public static void msg(final Ansi msg, final int loglevel) {
		msg(msg, loglevel, true);
	}

	public static void msg(final Ansi msg, final int loglevel, boolean newLine) {
		msg(msg, loglevel, true, false);
	}

	public static void msg(final Ansi msg, final int loglevel, boolean newLine, boolean ignoreSilence) {
		ignoreSilenceStart(TYPE_MSG, ignoreSilence);
		if (loglevel <= Log.logLevel) {
			checkAnsiInstalled();
			outPrint(prefix(msg.toString().contains("\n")) + msg.reset() + (newLine ? "\n" : ""));
			logToGUI(new LogMsg(msg.toString(), (loglevel == Log.IMPORTANT ? LogMsg.TYPE_IMPORTANT : LogMsg.TYPE_MSG)));
		}

		if (loglevel <= Log.logToFileLevel) {
			outOrErrPrintFile(prefix(msg.toString().contains("\n")) + msg.reset() + (newLine ? "\n" : ""));
		}
		ignoreSilenceEnd(TYPE_MSG);
	}

	public static void note(final String msg) {
		note(msg, logToFileLevel >= Log.NORMAL);
	}

	public static void note(final String msg, boolean logToFile) {
		note(msg, logToFile, false);
	}

	public static void note(String msg, boolean logToFile, boolean ignoreSilence) {
		ignoreSilenceStart(TYPE_NOTE, ignoreSilence);
		msg = "Note: " + msg;
		if (!log.contains(msg)) {
			if (Log.NORMAL <= Log.logLevel) {
				checkAnsiInstalled();
				errPrint(prefix(msg.contains("\n")) + ansi().fg(CYAN).a(msg).reset() + "\n");
				logToGUI(new LogMsg(msg, LogMsg.TYPE_NOTE));
				log.add(msg);
			}
			if (logToFile) {
				outOrErrPrintFile(prefix(msg.contains("\n")) + msg + "\n");
			}
		}
		ignoreSilenceEnd(TYPE_NOTE);
	}

	public static void warning(final String msg) {
		warning(msg, logToFileLevel >= Log.WARNING);
	}

	public static void warning(final String msg, boolean logToFile) {
		warning(msg, logToFile, false);
	}

	public static void warning(String msg, boolean logToFile, boolean ignoreSilence) {
		ignoreSilenceStart(TYPE_WARNING, ignoreSilence);
		msg = "Warning: " + msg;
		if (!log.contains(msg)) {
			if (Log.WARNING <= Log.logLevel) {
				checkAnsiInstalled();
				errPrint(prefix(msg.contains("\n")) + ansi().fg(YELLOW).a(msg).reset() + "\n");
				logToGUI(new LogMsg(msg, LogMsg.TYPE_WARNING));
				log.add(msg);
			}
			if (logToFile) {
				outOrErrPrintFile(prefix(msg.contains("\n")) + msg + "\n");
			}
		}
		ignoreSilenceEnd(TYPE_WARNING);
	}

	public static void error(final String msg) {
		error(msg, logToFileLevel >= Log.ERROR);
	}

	public static void error(final String msg, boolean logToFile) {
		error(msg, logToFile, false);
	}

	public static void error(String msg, boolean logToFile, boolean ignoreSilence) {
		ignoreSilenceStart(TYPE_ERROR, ignoreSilence);
		msg = "Error: " + msg;
		if (!log.contains(msg)) {
			if (Log.ERROR <= Log.logLevel) {
				checkAnsiInstalled();
				errPrint(prefix(msg.contains("\n")) + ansi().fg(RED).a(msg).reset() + "\n");
				logToGUI(new LogMsg(msg, LogMsg.TYPE_ERROR));
				log.add(msg);
			}
			if (logToFile) {
				outOrErrPrintFile(prefix(msg.contains("\n")) + msg + "\n");
			}
		}
		ignoreSilenceEnd(TYPE_ERROR);
	}

	public static boolean logIt(final int loglevel) {
		return logIt(loglevel, false);
	}

	public static boolean logIt(final int loglevel, boolean considerFileLevel) {
		return (loglevel <= Log.logLevel) || (considerFileLevel && (loglevel <= Log.logToFileLevel));
	}

	private static String prefix(final boolean nl) {
		if (prefixEnabled) {
			return Properties.info().ABBREVIATION + " " + date() + (nl ? "\n" : " ");
		} else {
			return "";
		}
	}

	public static String date() {
		return date(System.currentTimeMillis());
	}

	public static String date(long timestamp) {
		return Helper.getDate(timestamp, LOG_DATE_FORMAT);
	}

	private static void outPrint(String str) {
		lock.lock();
		System.out.print(str);
		lock.unlock();
	}

	private static void errPrint(String str) {
		lock.lock();
		System.err.print(str);
		lock.unlock();
	}

	private static void outOrErrPrintFile(String msg) {
		if (logfileWriter == null) {
			initLogfileWriter();
		}
		logfileWriter.print(msg);
		logfileWriter.flush();
	}

	private static void initLogfileWriter() {
		try {
			if (logfileWriter != null) {
				logfileWriter.close();
			}
			final FileWriter fw = new FileWriter(logfile, true);
			logfileWriter = new PrintWriter(fw);
		} catch (final IOException e) {
			Log.error("Cannot access logfile for writing: " + logfile.getAbsolutePath() + getExceptionAppendix(e));
		}
	}

	private static void ignoreSilenceStart(int type, boolean ignoreSilence) {
		ignoredSilenceLevel = getSilenceLevel();
		if (!isSilenced(type) || (ignoredSilenceLevel > SILENCE_LEVEL_NONE && (ignoreSilence || alwaysIgnoreSilence))) {
			setSilence(SILENCE_LEVEL_NONE);
		}
	}

	private static void ignoreSilenceEnd(int type) {
		if (ignoredSilenceLevel > SILENCE_LEVEL_NONE) {
			setSilence(ignoredSilenceLevel);
		}
	}

	public static String getExceptionAppendix(Throwable e) {
		return getExceptionAppendix(e, false);
	}

	public static String getExceptionAppendix(Throwable e, boolean newLine) {
		return (newLine ? "\n" : " ") + "(" + e.getClass().getSimpleName()
				+ (e.getMessage() != null ? ": " + e.getMessage() : "") + ")";
	}

	public static String getExceptionAppendixNoFormatting(Throwable e) {
		return e.getClass().getSimpleName() + (e.getMessage() != null ? ": " + e.getMessage() : "");
	}

	private static void checkAnsiInstalled() {
		if (!ansiInstalled) {
			ansiInstalled = true;
			AnsiConsole.systemInstall();
		}
	}

	public static void emptyLine() {
		outPrint("\n");
	}

	public static void reset() {
		log.clear();
	}

	// Setters & Getters
	public static void setLogLevel(final int loglevel) {
		Log.logLevel = loglevel;
	}

	public static int getLogLevel() {
		return logLevel;
	}

	public static void setLogToFileLevel(final int loglevel) {
		Log.logToFileLevel = loglevel;
	}

	public static int getLogToFileLevel() {
		return logToFileLevel;
	}

	public static void setPrefixEnabled(final boolean value) {
		Log.prefixEnabled = value;
	}

	public static boolean getShorten() {
		return shorten;
	}

	public static void setShorten(boolean shorten) {
		Log.shorten = shorten;
	}

	public static void setDifferentLogFile(File newLogFile) {
		logfile = newLogFile;
		initLogfileWriter();
	}

	public static void setSilence(boolean silenceAll) {
		setSilence(silenceAll, false);
	}

	public static void setSilence(boolean silenceAll, boolean ignoreSilenceInCaseOfAQLLog) {
		if (silenceAll) {
			setSilence(SILENCE_LEVEL_ALL, ignoreSilenceInCaseOfAQLLog);
		} else {
			setSilence(SILENCE_LEVEL_NONE, ignoreSilenceInCaseOfAQLLog);
		}
	}

	/**
	 * Sliences everything up to the given level
	 *
	 * @param silenceLevel
	 *            E.g.: SILENCE_LEVEL_WARNING - Everything more important and warnings themselves are shown. Everything with lower priority is silenced.
	 */
	public static void setSilence(int silenceLevel) {
		setSilence(silenceLevel, false);
	}

	/**
	 * Sliences everything up to the given level
	 *
	 * @param silenceLevel
	 *            E.g.: SILENCE_LEVEL_WARNING - Everything more important and warnings themselves are shown. Everything with lower priority is silenced.
	 * @param ignoreSilenceInCaseOfAQLLog
	 *            Any calls via Log.msg, Log.error, ... will still be printed.
	 */
	public static void setSilence(int silenceLevel, boolean ignoreSilenceInCaseOfAQLLog) {
		if (silenceLevel > SILENCE_LEVEL_NONE && Log.getLogLevel() < Log.ALL) {
			silence = silenceLevel;
			System.setOut(dummy);
			System.setErr(dummy);
		} else {
			silence = SILENCE_LEVEL_NONE;
			System.setOut(out);
			System.setErr(err);
		}
		alwaysIgnoreSilence = ignoreSilenceInCaseOfAQLLog;
	}

	public static boolean isSilenced() {
		return silence > SILENCE_LEVEL_NONE;
	}

	public static boolean isSilenced(int type) {
		switch (type) {
			case TYPE_MSG:
				return silence > SILENCE_LEVEL_MSG;
			case TYPE_NOTE:
				return silence > SILENCE_LEVEL_NOTE;
			case TYPE_WARNING:
				return silence > SILENCE_LEVEL_WARNING;
			case TYPE_ERROR:
				return silence > SILENCE_LEVEL_ERROR;
			default:
				return silence > SILENCE_LEVEL_NONE;
		}
	}

	public static int getSilenceLevel() {
		return silence;
	}

	private static void logToGUI(LogMsg msg) {
		if (GUI.started) {
			LogViewer.log(msg);
		} else if (logBuffer != null) {
			logBuffer.add(msg);
		}
	}

	public static void enableGUIlogging() {
		if (logBuffer != null) {
			while (!logBuffer.isEmpty()) {
				LogViewer.log(logBuffer.poll());
			}
		}
	}

	public static void disableGUIlogging() {
		logBuffer = null;
	}

	public static String stripAnsi(String string) {
		return string.replaceAll("\\e\\[[\\d;]*[^\\d;]", "");
	}
}