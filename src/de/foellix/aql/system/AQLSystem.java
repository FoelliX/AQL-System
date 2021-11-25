package de.foellix.aql.system;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.GREEN;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.LoggerFactory;

import de.foellix.aql.Log;
import de.foellix.aql.LogSilencer;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.datastructure.handler.QueryHandler;
import de.foellix.aql.datastructure.query.Query;
import de.foellix.aql.helper.CLIHelper;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.exceptions.CancelExecutionException;
import de.foellix.aql.system.task.Task;
import de.foellix.aql.transformations.RulesHandler;
import de.foellix.aql.ui.cli.AnswerToConsole;
import de.foellix.aql.ui.gui.GUI;
import de.foellix.aql.ui.gui.Storage;

public class AQLSystem {
	private final Options options;

	private final TaskScheduler taskScheduler;
	private final TaskCreator taskCreator;
	private final ProgressHandler progressHandler;

	private Map<String, String> globalVariables;
	private final TaskHooks taskHooksBefore;
	private final TaskHooks taskHooksAfter;
	private final List<IAnswerAvailable> answerReceivers;
	private final List<IProgressChanged> progressListener;

	private boolean wait;
	private Collection<Object> lastAnswer;
	private Lock systemLock;
	private String currentInputQuery;
	private double time;

	public AQLSystem() {
		this(new Options());
	}

	public AQLSystem(Options options) {
		// Hide SLF4J-Logger output (otherwise triggered e.g. by Soot)
		try (LogSilencer s = new LogSilencer()) {
			LoggerFactory.getLogger(AQLSystem.class);
		}

		// Load initial configuration
		CLIHelper.evaluateConfig(options.getConfig().getAbsolutePath());

		// Load initial rules
		CLIHelper.evaluateRules(options.getRulesAsString());

		// Setup AQL-System
		this.options = options;
		FileHelper.initializeFileSystem();
		if (options.getResetOutputDirectories()) {
			BackupAndReset.resetOutputDirectories();
		}

		this.taskScheduler = new TaskScheduler(this);
		this.taskCreator = new TaskCreator(this);
		this.progressHandler = new ProgressHandler(this);

		this.globalVariables = new HashMap<>();
		this.taskHooksBefore = new TaskHooks();
		this.taskHooksAfter = new TaskHooks();
		this.answerReceivers = new LinkedList<>();
		this.progressListener = new LinkedList<>();

		this.wait = false;
		this.lastAnswer = null;
		this.systemLock = new ReentrantLock();

		if (Log.logIt(Log.NORMAL)) {
			this.answerReceivers.add(new AnswerToConsole());
		}
	}

	public void query(final String query) {
		this.systemLock.lock();

		this.time = System.currentTimeMillis();
		this.lastAnswer = null;
		Log.reset();
		this.currentInputQuery = Helper.replaceAllWhiteSpaceChars(query, true);
		Log.msg(ansi().bold().fg(GREEN).a("Starting: " + this.currentInputQuery), Log.IMPORTANT);
		final Query queryObj = QueryHandler.parseQuery(query);
		if (queryObj != null) {
			final String parsed = queryObj.toString();
			Log.msg("Parsed:" + (parsed.contains("\n") ? '\n' : ' ') + parsed, Log.NORMAL);
			try {
				this.taskScheduler.start(this.taskCreator.query(queryObj));
			} catch (final CancelExecutionException e) {
				Log.error(e.getMessage());
			}
		} else {
			Log.error("Query could not be parsed: " + query);
		}

		this.systemLock.unlock();
	}

	public Collection<Object> queryAndWait(final String query) {
		this.wait = true;
		new Thread(() -> {
			query(query);
		}).start();
		while (this.wait) {
			try {
				Thread.sleep(250);
			} catch (final InterruptedException e) {
				return null;
			}
		}
		return this.lastAnswer;
	}

	public void queryCanceled(int status, String message) {
		this.time = (System.currentTimeMillis() - this.time) / 1000d;

		this.wait = false;
		this.taskScheduler.cancel();

		// Canceled Query
		Log.error("Canceled (after " + this.time + "s): " + this.currentInputQuery + "\nReason: " + message);
		if (GUI.started && Boolean
				.parseBoolean(Storage.getInstance().getGuiConfigProperty(Storage.PROPERTY_CONFIRM_QUERY_ENDED))) {
			GUI.alert(true, "Error", "Canceled (after " + this.time + "s): "
					+ Helper.shorten(Helper.autoformat(this.currentInputQuery), 5), "Reason: " + message);
		}
		for (final IAnswerAvailable receiver : this.answerReceivers) {
			receiver.answerAvailable(new Answer(), status);
		}
	}

	protected void queryFinished(Task task, int status) {
		this.time = ((System.currentTimeMillis() - this.time) / 1000d);

		// Get answer collection from root-task's children
		this.lastAnswer = new ArrayList<>();
		for (final Task child : task.getChildren()) {
			this.lastAnswer.add(child.getTaskAnswer().getAnswer());
		}
		this.wait = false;

		// Create root answer stats file
		createStats(task);

		// Output and optionally store unique answers
		if (GUI.started && Boolean
				.parseBoolean(Storage.getInstance().getGuiConfigProperty(Storage.PROPERTY_CONFIRM_QUERY_ENDED))) {
			GUI.alert(false, "Done", "Successfully finished (after " + this.time + "s)!",
					"Query:\n" + Helper.shorten(Helper.autoformat(this.currentInputQuery), 5));
		}
		Log.msg(ansi().bold().fg(GREEN).a("Finished (after " + this.time + "s): " + this.currentInputQuery),
				Log.IMPORTANT);
		for (final Object answer : this.lastAnswer) {
			if (answer instanceof Answer) {
				final Answer castedAnswer = (Answer) answer;

				File xmlFile = null;
				if (this.options.getStoreAnswers()) {
					xmlFile = FileHelper.makeUnique(
							new File(FileHelper.getAnswersDirectory(), "answer_" + Helper.getDate() + "-0.xml"));
					AnswerHandler.createXML(castedAnswer, xmlFile);
				}

				Log.msg(ansi().bold()
						.a("***** Answer "
								+ (this.options.getStoreAnswers() ? "(" + xmlFile.getAbsolutePath() + ") " : "")
								+ "*****\n")
						.reset().a(Helper.toString(castedAnswer)), Log.DEBUG);
			}
			for (final IAnswerAvailable receiver : this.answerReceivers) {
				receiver.answerAvailable(answer, status);
			}
		}
	}

	private void createStats(Task task) {
		final StringBuilder sb = new StringBuilder("*** Statistics ***\n");
		sb.append("- Status: " + task.getStatus() + "\n\n");
		sb.append("- Query: " + this.currentInputQuery + "\n\n");
		sb.append("- Answerfile" + (this.lastAnswer.size() <= 1 ? ": " : "s: ") + "");
		for (final Task child : task.getChildren()) {
			sb.append((this.lastAnswer.size() <= 1 ? "" : "\n\t- ")
					+ child.getTaskAnswer().getAnswerFile().getAbsolutePath());
		}
		sb.append("\n\n");
		sb.append("- Time: " + this.time + "s (now is "
				+ Helper.getDate(System.currentTimeMillis(), "dd.MM.yyyy HH:mm:ss.SSS") + ")\n\n");
		try {
			Files.write(task.getTaskAnswer().getAnswerFile().toPath(), sb.toString().getBytes());
		} catch (final IOException e) {
			Log.error("Could not write statistics file \"" + task.getTaskAnswer().getAnswerFile().getAbsolutePath()
					+ "\"." + Log.getExceptionAppendix(e));
		}
	}

	public TaskScheduler getTaskScheduler() {
		return this.taskScheduler;
	}

	public TaskCreator getTaskCreator() {
		return this.taskCreator;
	}

	public ProgressHandler getProgressHandler() {
		return this.progressHandler;
	}

	public List<IAnswerAvailable> getAnswerReceivers() {
		return this.answerReceivers;
	}

	public List<IProgressChanged> getProgressListener() {
		return this.progressListener;
	}

	public TaskHooks getTaskHooksBefore() {
		return this.taskHooksBefore;
	}

	public TaskHooks getTaskHooksAfter() {
		return this.taskHooksAfter;
	}

	public Options getOptions() {
		// Refresh
		this.options.setConfig(ConfigHandler.getInstance().getConfigFile());
		this.options.setRules(RulesHandler.getInstance().getRulesFiles());

		return this.options;
	}

	public void setGlobalVariables(Map<String, String> globalVariables) {
		this.globalVariables = globalVariables;
	}

	public Map<String, String> getGlobalVariables() {
		return this.globalVariables;
	}

	public boolean isRunning() {
		if (this.wait) {
			return true;
		} else if (this.systemLock.tryLock()) {
			this.systemLock.unlock();
			return false;
		} else {
			return true;
		}
	}
}