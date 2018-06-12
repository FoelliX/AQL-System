package de.foellix.aql.config;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import de.foellix.aql.Log;

public class ConfigParser {
	private static File configFile = new File("config.xml");

	private static Config config = null;

	public static Config parseXML() {
		return parseXML(false);
	}

	public static Config parseXML(final boolean reload) {
		if (config == null || reload) {
			try {
				final JAXBContext jaxbContext = JAXBContext.newInstance(Config.class);

				final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				final Config config = (Config) jaxbUnmarshaller.unmarshal(configFile);

				ConfigParser.config = config;
			} catch (final JAXBException e) {
				e.printStackTrace();
				Log.error("Something went wrong while reading config.xml file.");
			}
		}

		return config;
	}

	public static void setConfig(final File config) {
		configFile = config;
		parseXML(true);
	}
}