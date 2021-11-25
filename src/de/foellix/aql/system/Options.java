package de.foellix.aql.system;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.transformations.RulesHandler;

public class Options {
	public static final int TIMEOUT_MODE_MAX = 1;
	public static final int TIMEOUT_MODE_MIN = 2;
	public static final int TIMEOUT_MODE_OVERRIDE = 3;

	public static final boolean DEFAULT_OPTION_STORE_ANSWERS = true;
	public static final boolean DEFAULT_OPTION_RETRY = true;
	public static final long DEFAULT_OPTION_TIMEOUT = -1;
	public static final int DEFAULT_OPTION_TIMEOUT_MODE = TIMEOUT_MODE_MAX;
	public static final boolean DEFAULT_OPTION_RESET_OUTPUT_DIRECTORIES = false;
	public static final boolean DEFAULT_OPTION_FILENAME_BASED_ANSWER_HASH = false;

	// GUI-Options
	public static final boolean DEFAULT_OPTION_SHOW_CONFIG_WIZARD = false;
	public static final boolean DEFAULT_OPTION_DRAW_GRAPHS = false;
	public static final boolean DEFAULT_OPTION_VIEW_ANSWER = false;
	public static final boolean DEFAULT_OPTION_NO_SPLASH_SCREEN = false;

	private File config;
	private List<File> rules;
	private boolean storeAnswers;
	private boolean retry;
	private long timeout;
	private int timeoutMode;
	private boolean resetOutputDirectories;
	private boolean filenameBasedAnswersHash;

	// GUI-Options
	private boolean showConfigWizard;
	private boolean drawGraphs;
	private boolean viewAnswer;
	private boolean noSplashScreen;

	public Options() {
		this.config = null;
		this.rules = null;
		this.storeAnswers = DEFAULT_OPTION_STORE_ANSWERS;
		this.retry = DEFAULT_OPTION_RETRY;
		this.timeout = DEFAULT_OPTION_TIMEOUT;
		this.timeoutMode = DEFAULT_OPTION_TIMEOUT_MODE;
		this.resetOutputDirectories = DEFAULT_OPTION_RESET_OUTPUT_DIRECTORIES;
		this.filenameBasedAnswersHash = DEFAULT_OPTION_FILENAME_BASED_ANSWER_HASH;

		// GUI-Options
		this.showConfigWizard = DEFAULT_OPTION_SHOW_CONFIG_WIZARD;
		this.drawGraphs = DEFAULT_OPTION_DRAW_GRAPHS;
		this.viewAnswer = DEFAULT_OPTION_VIEW_ANSWER;
		this.noSplashScreen = DEFAULT_OPTION_NO_SPLASH_SCREEN;
	}

	public File getConfig() {
		if (this.config == null) {
			this.config = ConfigHandler.getInstance().getConfigFile();
		}
		return this.config;
	}

	public List<File> getRules() {
		if (this.rules == null) {
			this.rules = RulesHandler.getInstance().getRulesFiles();
		}
		return this.rules;
	}

	public String getRulesAsString() {
		final List<File> rules = getRules();
		final StringBuilder sb = new StringBuilder();
		for (final File ruleFile : rules) {
			if (!sb.isEmpty()) {
				sb.append(", ");
			}
			sb.append(ruleFile.getAbsolutePath());
		}
		return sb.toString();
	}

	public boolean getStoreAnswers() {
		return this.storeAnswers;
	}

	public boolean getRetry() {
		return this.retry;
	}

	public long getTimeout() {
		return this.timeout;
	}

	public int getTimeoutMode() {
		return this.timeoutMode;
	}

	public boolean getResetOutputDirectories() {
		return this.resetOutputDirectories;
	}

	public boolean getFilenameBasedAnswersHash() {
		return this.filenameBasedAnswersHash;
	}

	public Options setConfig(File config) {
		this.config = config;
		return this;
	}

	public Options setRules(File rules) {
		final List<File> temp = new ArrayList<>();
		temp.add(rules);
		return setRules(temp);
	}

	public Options setRules(List<File> rules) {
		this.rules = rules;
		return this;
	}

	public Options setStoreAnswers(boolean storeAnswers) {
		this.storeAnswers = storeAnswers;
		return this;
	}

	public Options setRetry(boolean retry) {
		this.retry = retry;
		return this;
	}

	public Options setTimeout(long timeout) {
		this.timeout = timeout;
		return this;
	}

	public Options setTimeoutMode(int timeoutMode) {
		this.timeoutMode = timeoutMode;
		return this;
	}

	public Options setResetOutputDirectories(boolean resetOutputDirectories) {
		this.resetOutputDirectories = resetOutputDirectories;
		return this;
	}

	public Options setFilenameBasedAnswersHash(boolean filenameBasedAnswersHash) {
		this.filenameBasedAnswersHash = filenameBasedAnswersHash;
		return this;
	}

	// GUI-Options
	public boolean getShowConfigWizard() {
		return this.showConfigWizard;
	}

	public boolean getDrawGraphs() {
		return this.drawGraphs;
	}

	public boolean getViewAnswer() {
		return this.viewAnswer;
	}

	public boolean getNoSplashScreen() {
		return this.noSplashScreen;
	}

	public Options setShowConfigWizard(boolean showConfigWizard) {
		this.showConfigWizard = showConfigWizard;
		return this;
	}

	public Options setDrawGraphs(boolean drawGraphs) {
		this.drawGraphs = drawGraphs;
		return this;
	}

	public Options setViewAnswer(boolean viewAnswer) {
		this.viewAnswer = viewAnswer;
		return this;
	}

	public Options setNoSplashScreen(boolean noSplashScreen) {
		this.noSplashScreen = noSplashScreen;
		return this;
	}
}