package de.foellix.aql.config;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.foellix.aql.Log;
import de.foellix.aql.config.wizard.ConfigWizard;
import de.foellix.aql.ui.gui.GUI;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ConfigHandler {
	private File configFile = new File("config.xml");
	private Config config = null;

	private static ConfigHandler instance = new ConfigHandler();

	private boolean offeredWizard = false;

	private ConfigHandler() {
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
			} else if (!this.offeredWizard) {
				this.offeredWizard = true;
				if (!GUI.showConfigWizard) {
					offerConfigWizard(GUI.stage);
				}
			}
		}
		return this.config;
	}

	public void offerConfigWizard(Stage stage) {
		final Alert alert = new Alert(AlertType.CONFIRMATION);
		final Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons().add(new Image("file:data/gui/images/icon_16.png", 16, 16, false, true));
		alertStage.getIcons().add(new Image("file:data/gui/images/icon_32.png", 32, 32, false, true));
		alertStage.getIcons().add(new Image("file:data/gui/images/icon_64.png", 64, 64, false, true));
		alert.setTitle("Missing configuration");
		alert.setHeaderText("Configuration file could not be found.");
		alert.setContentText("Do you want to start the configuration wizard?");

		final Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			new ConfigWizard(stage, ConfigHandler.getInstance().getConfigFile());
		} else {
			Log.warning("No configuration specified.");
		}
	}

	public static Config parseXML(final File configFile) {
		if (configFile != null) {
			try {
				final JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);

				final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				final Config config = (Config) jaxbUnmarshaller.unmarshal(configFile);

				return config;
			} catch (final JAXBException e) {
				Log.error("Something went wrong while reading config file: " + configFile.getAbsolutePath() + " ("
						+ e.getMessage() + ")");
			}
		}
		return null;
	}

	public static Config parseXML(final String configString) {
		if (configString != null) {
			try {
				final StringReader reader = new StringReader(configString);

				final JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);

				final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				final Config config = (Config) jaxbUnmarshaller.unmarshal(reader);
				reader.close();

				return config;
			} catch (final JAXBException e) {
				Log.error("Cannot parse XML document currently. It must be corrupted: " + e.getMessage());
			}
		}
		return null;
	}

	public String toXML() {
		return toXML(this.config);
	}

	public static String toXML(final Config config) {
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);
			final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");

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
		this.configFile = config;
		getConfig(true);
	}

	public File getConfigFile() {
		return this.configFile;
	}
}