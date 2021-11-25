package de.foellix.aql.config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.foellix.aql.Log;
import de.foellix.aql.config.wizard.ConfigWizard;
import de.foellix.aql.helper.CLIHelper;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.ui.gui.GUI;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ConfigHandler {
	public static final File DEFAULT_CONFIG_FILE = new File("config.xml");
	public static final String FREE_ACCOUNT = "free";

	private File configFile = DEFAULT_CONFIG_FILE;
	private Config config = null;

	private static ConfigHandler instance = new ConfigHandler();

	private boolean offeredWizard = false;

	private ConfigHandler() {
		getConfig();
	}

	public static ConfigHandler getInstance() {
		return instance;
	}

	public Config getConfig() {
		return getConfig(false);
	}

	public Config getConfig(final boolean reload) {
		if (this.config == null || reload) {
			if (this.configFile.exists()) {
				this.config = parseXML(this.configFile);
				if (this.config.getAndroidPlatforms().contains("\\")) {
					this.config.setAndroidPlatforms(this.config.getAndroidPlatforms().replace("\\", "/"));
				}
			} else if (!this.offeredWizard) {
				this.offeredWizard = true;
				if (!GUI.options.getShowConfigWizard()) {
					if (GUI.started) {
						offerConfigWizard(GUI.stage);
					} else {
						if (!CLIHelper.initialConfigAvailable) {
							if (CLIHelper.getStage() != null) {
								offerConfigWizard(CLIHelper.getStage());
							} else {
								Log.warning(
										"Cannot find default configuration (config.xml) and no other configuration specified.");
							}
						}
					}
				}
			}
		}
		return this.config;
	}

	private void offerConfigWizard(Stage stage) {
		final Alert alert = new Alert(AlertType.CONFIRMATION);
		final Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons()
				.add(new Image(new File("data/gui/images/icon_16.png").toURI().toString(), 16, 16, false, true));
		alertStage.getIcons()
				.add(new Image(new File("data/gui/images/icon_32.png").toURI().toString(), 32, 32, false, true));
		alertStage.getIcons()
				.add(new Image(new File("data/gui/images/icon_64.png").toURI().toString(), 64, 64, false, true));
		alert.setTitle("Missing configuration");
		alert.setHeaderText("Configuration file could not be found.");
		alert.setContentText("Do you want to start the configuration wizard?");

		final Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			new ConfigWizard(stage, this.configFile, false);
		} else {
			Log.warning("No configuration specified.");
		}
	}

	public static Config parseXML(final File configFile) {
		if (configFile != null) {
			try {
				return parseXML(new FileReader(configFile));
			} catch (final FileNotFoundException e) {
				Log.error("Cannot find config file: " + configFile.getAbsolutePath());
			}
		}
		return null;
	}

	public static Config parseXML(final String configString) {
		if (configString != null) {
			return parseXML(new StringReader(configString));
		}
		return null;
	}

	private static Config parseXML(Reader reader) {
		if (reader != null) {
			try {
				final JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);

				final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				final Config config = (Config) jaxbUnmarshaller.unmarshal(reader);
				reader.close();

				if (config.getAndroidPlatforms() == null
						|| (config.getTools() == null && config.getPreprocessors() == null
								&& config.getOperators() == null && config.getConverters() == null)) {
					Log.error("Configuration incomplete!");
				}

				return config;
			} catch (final JAXBException | IOException e) {
				Log.error("Cannot parse configuration (XML-)file. It must be corrupted!" + Log.getExceptionAppendix(e));
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return toXML(this.config);
	}

	public static String toXML(final Config config) {
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);
			final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.toString());

			final OutputStream outputStream = new ByteArrayOutputStream();
			jaxbMarshaller.marshal(config, outputStream);
			final String returnStr = outputStream.toString();
			outputStream.close();

			return returnStr;
		} catch (final JAXBException | IOException e) {
			Log.error("Something went wrong while creating the XML string (" + e.getMessage() + ").");
			return null;
		}
	}

	public void setConfig(final File config) {
		setConfig(config, false);
	}

	public void setConfig(File config, boolean restoreFromDefault) {
		setConfig(config, restoreFromDefault, null, null);
	}

	public void setConfig(final File config, boolean restoreFromDefault, URL url, String username) {
		this.configFile = config;
		getConfig(true);

		updateConfig(this.config, config, restoreFromDefault, url, username);
	}

	public static void updateConfig(final File configFile, boolean restoreFromDefault, URL url, String username) {
		updateConfig(ConfigHandler.parseXML(configFile), configFile, restoreFromDefault, url, username);
	}

	public static void updateConfig(Config config, final File configFile, boolean restoreFromDefault, URL url,
			String username) {
		// Android Platforms & Build Tools
		boolean changed1 = false;
		if (restoreFromDefault && DEFAULT_CONFIG_FILE.exists()) {
			final Config defaultConfig = ConfigHandler.parseXML(DEFAULT_CONFIG_FILE);
			if (defaultConfig.getAndroidBuildTools() != null && !defaultConfig.getAndroidBuildTools().isBlank()) {
				config.setAndroidBuildTools(defaultConfig.getAndroidBuildTools());
				changed1 = true;
			}
			if (defaultConfig.getAndroidPlatforms() != null && !defaultConfig.getAndroidPlatforms().isBlank()) {
				config.setAndroidPlatforms(defaultConfig.getAndroidPlatforms());
				changed1 = true;
			}
			if (changed1) {
				Log.msg("Adapted online configuration: Changed build tools and platform paths to local ones.",
						Log.DEBUG_DETAILED);
			}
		}

		// Ports of external tools
		boolean changed2 = false;
		if (url != null) {
			for (final Tool tool : getAllToolsOfAnyKind(config)) {
				if (tool.isExternal()) {
					if (tool.getExecute() != null && tool.getExecute().getUrl() != null
							&& !tool.getExecute().getUrl().isBlank()) {
						String urlStr = tool.getExecute().getUrl();
						final URL wrongUrl = Helper.getURL(urlStr);
						if (url.getPort() < 0) {
							urlStr = urlStr.replaceFirst(":" + wrongUrl.getPort(), "");
						} else {
							urlStr = urlStr.replaceFirst(":" + wrongUrl.getPort(), ":" + url.getPort());
						}
						if (!url.getHost().equals(wrongUrl.getHost())) {
							urlStr = urlStr.replaceFirst(wrongUrl.getHost(), url.getHost());
						}
						tool.getExecute().setUrl(urlStr);
						changed2 = true;
					}
				}
			}
			if (changed2) {
				Log.msg("Adapted online configuration: Changed ip and/or ports to those used while requesting the configuration.",
						Log.DEBUG_DETAILED);
			}
		}

		// Free username
		boolean changed3 = false;
		if (url != null) {
			for (final Tool tool : getAllToolsOfAnyKind(config)) {
				if (tool.isExternal()) {
					if (tool.getExecute() != null && tool.getExecute().getUrl() != null
							&& !tool.getExecute().getUrl().isBlank()) {
						if (username.equals(FREE_ACCOUNT)) {
							tool.getExecute().setUsername(username);
							changed3 = true;
						}
					}
				}
			}
			if (changed3) {
				Log.msg("Adapted online configuration: Changed username - chopped off IP suffix.", Log.DEBUG_DETAILED);
			}
		}

		if (changed1 || changed2 || changed3) {
			// Write to file
			try {
				Files.write(configFile.toPath(), toXML(config).getBytes());
			} catch (final IOException e) {
				Log.warning("Could not write adapted config file: " + configFile.getAbsolutePath());
			}
		}
	}

	public File getConfigFile() {
		return this.configFile;
	}

	public Tool getToolByName(String name) {
		return getByName(name, null, this.config.getTools().getTool());
	}

	public Tool getToolByName(String name, String version) {
		return getByName(name, version, this.config.getTools().getTool());
	}

	public Tool getPreprocessorByName(String name) {
		return getByName(name, null, this.config.getPreprocessors().getTool());
	}

	public Tool getPreprocessorByName(String name, String version) {
		return getByName(name, version, this.config.getPreprocessors().getTool());
	}

	public Tool getOperatorByName(String name) {
		return getByName(name, null, this.config.getOperators().getTool());
	}

	public Tool getOperatorByName(String name, String version) {
		return getByName(name, version, this.config.getOperators().getTool());
	}

	public Tool getConverterByName(String name) {
		return getByName(name, null, this.config.getConverters().getTool());
	}

	public Tool getConverterByName(String name, String version) {
		return getByName(name, version, this.config.getConverters().getTool());
	}

	private Tool getByName(String name, String version, List<Tool> list) {
		for (final Tool tool : list) {
			if (tool.getName().equals(name) && (version == null || tool.getVersion().equals(version))) {
				return tool;
			}
		}
		return null;
	}

	public List<Tool> getAllToolsOfAnyKind() {
		return getAllToolsOfAnyKind(this.config);
	}

	public static List<Tool> getAllToolsOfAnyKind(Config config) {
		final List<Tool> tempList = new ArrayList<>();
		if (config.getTools() != null && !config.getTools().getTool().isEmpty()) {
			tempList.addAll(config.getTools().getTool());
		}
		if (config.getPreprocessors() != null && !config.getPreprocessors().getTool().isEmpty()) {
			tempList.addAll(config.getPreprocessors().getTool());
		}
		if (config.getOperators() != null && !config.getOperators().getTool().isEmpty()) {
			tempList.addAll(config.getOperators().getTool());
		}
		if (config.getConverters() != null && !config.getConverters().getTool().isEmpty()) {
			tempList.addAll(config.getConverters().getTool());
		}
		return tempList;
	}

	public int getMaxConfiguredPriority() {
		return getMaxConfiguredPriority(this.config);
	}

	public static int getMaxConfiguredPriority(Config config) {
		int returnValue = -1;
		for (final Tool t : getAllToolsOfAnyKind(config)) {
			int temp = 0;
			for (final Priority p : t.getPriority()) {
				temp += p.getValue();
			}
			if (temp > returnValue) {
				returnValue = temp;
			}
		}
		return returnValue;
	}

	public File getAndroidBuildTools() {
		if (this.config != null && this.config.getAndroidBuildTools() != null
				&& !this.config.getAndroidBuildTools().isEmpty()) {
			return new File(this.config.getAndroidBuildTools());
		} else {
			if (this.config != null && this.config.getAndroidPlatforms() != null
					&& !this.config.getAndroidPlatforms().isEmpty()) {
				final File androidPlatforms = new File(this.config.getAndroidPlatforms());
				if (androidPlatforms.exists() && androidPlatforms.getParentFile().exists()) {
					final File androidBuildToolsDirectories = new File(androidPlatforms.getParentFile(), "build-tools");
					final File[] androidBuildToolsDirectory = androidBuildToolsDirectories.listFiles();
					Arrays.sort(androidBuildToolsDirectory, new Comparator<File>() {
						@Override
						public int compare(File f1, File f2) {
							String f1Str = f1.getName().replaceAll("[^0-9]", "");
							f1Str = f1Str.substring(0, Math.min(4, f1Str.length()));

							String f2Str = f2.getName().replaceAll("[^0-9]", "");
							f2Str = f2Str.substring(0, Math.min(4, f2Str.length()));

							final int f1i = Integer.parseInt(f1Str);
							final int f2i = Integer.parseInt(f2Str);

							if (f1i < f2i) {
								return 1;
							} else if (f1i > f2i) {
								return -1;
							} else {
								return 0;
							}
						}
					});
					return androidBuildToolsDirectory[0];
				}
			}
		}
		return null;
	}

	public void mergeWith(File secondConfigFile) {
		if (!this.configFile.equals(secondConfigFile)) {
			Log.msg("Merging config \"" + this.configFile.getAbsolutePath() + "\" with \""
					+ secondConfigFile.getAbsolutePath() + "\".", Log.NORMAL);

			final Config secondConfig = parseXML(secondConfigFile);

			boolean changed = false;

			// Tools
			if (secondConfig.getTools() != null && !secondConfig.getTools().getTool().isEmpty()) {
				if (this.config.getTools() == null) {
					this.config.setTools(secondConfig.getTools());
				} else {
					this.config.getTools().getTool().addAll(secondConfig.getTools().getTool());
				}
				changed = true;
			}

			// Preprocessors
			if (secondConfig.getPreprocessors() != null && !secondConfig.getPreprocessors().getTool().isEmpty()) {
				if (this.config.getPreprocessors() == null) {
					this.config.setPreprocessors(secondConfig.getPreprocessors());
				} else {
					this.config.getPreprocessors().getTool().addAll(secondConfig.getPreprocessors().getTool());
				}
				changed = true;
			}

			// Operators
			if (secondConfig.getOperators() != null && !secondConfig.getOperators().getTool().isEmpty()) {
				if (this.config.getOperators() == null) {
					this.config.setOperators(secondConfig.getOperators());
				} else {
					this.config.getOperators().getTool().addAll(secondConfig.getOperators().getTool());
				}
				changed = true;
			}

			// Converters
			if (secondConfig.getConverters() != null && !secondConfig.getConverters().getTool().isEmpty()) {
				if (this.config.getConverters() == null) {
					this.config.setConverters(secondConfig.getConverters());
				} else {
					this.config.getConverters().getTool().addAll(secondConfig.getConverters().getTool());
				}
				changed = true;
			}

			// Write to file
			if (changed) {
				final File newConfig = FileHelper.getTempFile(FileHelper.FILE_ENDING_XML);
				try {
					Files.write(newConfig.toPath(), toXML(this.config).getBytes());
					setConfig(newConfig);
				} catch (final IOException e) {
					Log.error("Could not write merged config file: " + newConfig.getAbsolutePath()
							+ Log.getExceptionAppendix(e));
				}
			}
		}
	}
}