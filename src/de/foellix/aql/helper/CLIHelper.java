package de.foellix.aql.helper;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.system.Options;
import de.foellix.aql.transformations.RulesHandler;
import javafx.stage.Stage;

public class CLIHelper {
	private static final String TIMEOUT_MODESTRING_MAX = "max";
	private static final String TIMEOUT_MODESTRING_MIN = "min";
	private static final String TIMEOUT_MODESTRING_OVERRIDE = "override";
	private static final String QUOTE_NEEDLE = "#QUOTE#";

	public static boolean initialConfigAvailable = false;
	public static String lastConfigFileHashLoaded = null;

	private static Stage stage = null;

	public static int evaluateLogLevel(String debug) {
		return evaluateLogLevel(debug, true);
	}

	public static int evaluateLogLevel(String debug, boolean setImmediately) {
		int value = Log.NORMAL;
		if (debug != null) {
			if (debug.equalsIgnoreCase("none")) {
				value = Log.NONE;
			} else if (debug.equalsIgnoreCase("important")) {
				value = Log.IMPORTANT;
			} else if (debug.equalsIgnoreCase("error")) {
				value = Log.ERROR;
			} else if (debug.equalsIgnoreCase("special")) {
				value = Log.DEBUG_SPECIAL;
			} else if (debug.equalsIgnoreCase("warning")) {
				value = Log.WARNING;
			} else if (debug.equalsIgnoreCase("normal")) {
				value = Log.NORMAL;
			} else if (debug.equalsIgnoreCase("short")) {
				Log.setShorten(true);
				value = Log.NORMAL;
			} else if (debug.equalsIgnoreCase("debug")) {
				value = Log.DEBUG;
			} else if (debug.equalsIgnoreCase("detailed")) {
				value = Log.DEBUG_DETAILED;
			} else if (debug.equalsIgnoreCase("verbose")) {
				value = Log.VERBOSE;
			} else if (debug.equalsIgnoreCase("all")) {
				value = Log.ALL;
			} else {
				try {
					value = Integer.parseInt(debug);
				} catch (final NumberFormatException e) {
					Log.warning("Unknown logging level: " + debug);
				}
			}
		}
		if (setImmediately) {
			Log.setLogLevel(value);
		}
		return value;
	}

	public static boolean evaluateConfig(String config) {
		if (config != null && !config.isEmpty()) {
			URL url = null;
			String username = null;
			boolean isWebConfig = false;

			// Online
			File configFile = FileHelper.getTempFile(FileHelper.FILE_ENDING_XML);
			if (config.toLowerCase().startsWith("http") && !config.contains(",")) {
				// Get by download
				if (FileHelper.downloadFile(config, configFile, config.substring(config.lastIndexOf('/') + 1))) {
					isWebConfig = true;
				} else {
					configFile = null;
				}
			} else if (config.toLowerCase().startsWith("http") && config.contains(",")) {
				// Get from WebService
				final String[] parts = config.replace(", ", ",").split(",");
				final String urlStr = parts[0];
				url = Helper.getURL(urlStr);
				if (parts.length > 1) {
					username = parts[1];
				} else {
					username = "free";
				}
				String password;
				if (parts.length > 2) {
					password = parts[2];
				} else {
					password = "";
				}

				if (FileHelper.getConfigFromWebService(urlStr, username, password, configFile) && configFile.exists()) {
					isWebConfig = true;
				} else {
					config = urlStr;
					configFile = null;
				}
			} else {
				configFile = null;
			}

			// Handle file
			if (configFile == null) {
				configFile = new File(config);
			}
			if (configFile.exists()) {
				final String hash = HashHelper.hash(configFile, HashHelper.HASH_TYPE_MD5);
				if (lastConfigFileHashLoaded == null || !lastConfigFileHashLoaded.equals(hash)) {
					initialConfigAvailable = true;
					lastConfigFileHashLoaded = hash;
					Log.msg("Continuing with configuration: " + configFile.getAbsolutePath()
							+ (Log.logIt(Log.DEBUG_DETAILED) ? " (" + hash + ")" : ""), Log.DEBUG);
					if (url != null) {
						final String feedback = Helper.getFeedbackFromFile(configFile);
						if (feedback == null) {
							ConfigHandler.getInstance().setConfig(configFile, isWebConfig, url, username);
						} else {
							Log.warning("Configuration is invalid:\n" + feedback);
						}
					} else {
						ConfigHandler.getInstance().setConfig(configFile, isWebConfig);
					}
				}
				return true;
			} else {
				if (config.toLowerCase().startsWith("http")) {
					Log.warning("Configuration not available: " + config);
				} else {
					Log.warning("Configuration file does not exist: " + configFile.getAbsolutePath());
				}
			}
		} else {
			Log.warning("No configuration file provided.");
		}
		return false;
	}

	public static boolean evaluateRules(String rules) {
		if (rules != null && !rules.isEmpty()) {
			while (rules.contains(", ")) {
				rules = rules.replace(", ", ",");
			}
			final List<File> rulesFiles = new ArrayList<>();
			for (final String rulesSplitted : rules.split(",")) {
				final File rulesFile = new File(rulesSplitted);
				if (rulesFile.exists()) {
					rulesFiles.add(rulesFile);
				} else {
					Log.warning("Rules file does not exist: " + rulesFile.getAbsolutePath());
				}
			}
			if (rulesFiles.isEmpty()) {
				Log.warning("None of the given rules-files exist!");
			} else {
				RulesHandler.getInstance().setRulesFiles(rulesFiles);
				return true;
			}
		} else {
			Log.warning("No rules file provided.");
		}
		return false;
	}

	/**
	 * Return the timeout in seconds
	 *
	 * @param readTimeout
	 *            Examples: 100s, 5m, 1h
	 * @return the timeout in seconds
	 */
	public static long evaluateTimeout(String readTimeout) {
		if (readTimeout.contains("h")) {
			return Long.parseLong(readTimeout.replace("h", "")) * 3600;
		} else if (readTimeout.contains("m")) {
			return Long.parseLong(readTimeout.replace("m", "")) * 60;
		} else {
			return Long.parseLong(readTimeout.replace("s", ""));
		}
	}

	/**
	 * Helps dealing with timeouts set in config and via launch parameter. Returns the timeout mode which might be max, min or override.
	 *
	 * @param readTimeoutMode
	 *            the launch parameter read.
	 * @return the mode detected. Returns -1 if no mode selection was given.
	 */
	public static int evaluateTimeoutMode(String readTimeoutMode) {
		if (readTimeoutMode.equalsIgnoreCase(TIMEOUT_MODESTRING_MAX)) {
			return Options.TIMEOUT_MODE_MAX;
		} else if (readTimeoutMode.equalsIgnoreCase(TIMEOUT_MODESTRING_MIN)) {
			return Options.TIMEOUT_MODE_MIN;
		} else if (readTimeoutMode.equalsIgnoreCase(TIMEOUT_MODESTRING_OVERRIDE)) {
			return Options.TIMEOUT_MODE_OVERRIDE;
		}
		return -1;
	}

	/**
	 * Checks if the AQL-System or tool using is, is run from the correct location. If not, it terminates the execution.
	 */
	public static void checkResources() {
		final File data = new File("data");
		if (!data.exists()) {
			Log.error("The AQL-System's \"data\" directory could not be found. Please execute from correct location!");
			System.exit(1);
		}
	}

	/**
	 * Get stage for offering the Configuration wizard
	 *
	 * @return the stage offered
	 */
	public static Stage getStage() {
		return stage;
	}

	/**
	 * Sets an alternative stage for offering the Configuration wizard
	 *
	 * @param stage
	 *            the stage offered
	 */
	public static void setStage(Stage stage) {
		CLIHelper.stage = stage;
	}

	/**
	 * Replaces Quotes (") with %QUOTE%.
	 *
	 * @param arg
	 * @return
	 */
	public static String replaceQuotesWithNeedles(String arg) {
		return arg.replace("\"", QUOTE_NEEDLE);
	}

	/**
	 * Replaces %QUOTE% with Quotes (").
	 *
	 * @param arg
	 * @return
	 */
	public static String replaceNeedlesWithQuotes(String arg) {
		return arg.replace(QUOTE_NEEDLE, "\"");
	}

	/**
	 * Replaces %QUOTE% with Quotes (").
	 *
	 * @param args
	 * @return
	 */
	public static String[] replaceNeedlesWithQuotes(String[] args) {
		final String[] newArgs = new String[args.length];
		for (int i = 0; i < args.length; i++) {
			newArgs[i] = replaceNeedlesWithQuotes(args[i]);
		}
		return newArgs;
	}

	public static String removeQuotesFromFileString(String fileString) {
		if (fileString != null && !fileString.isBlank()) {
			if (fileString.startsWith("\"") && fileString.endsWith("\"")) {
				fileString = fileString.substring(1, fileString.length() - 1);
			}
		}
		return fileString;
	}

	/**
	 * Escapes "$" symbols
	 *
	 * @param str
	 *            String with "$" symbols
	 * @return same String but "$" is replaced by "\$"
	 */
	public static String escapeChars(String str) {
		return str.replace("$", "\\$");
	}
}