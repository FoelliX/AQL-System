package de.foellix.aql;

import java.io.InputStream;

public class Properties {
	private static final String PROPERTIES_FILE = "tool.properties";
	private static final String UNKNOWN = "Unknown";

	public String ABBREVIATION = UNKNOWN;
	public String NAME = UNKNOWN;
	public String VERSION = UNKNOWN;
	public String BUILDNUMBER = UNKNOWN;
	public String AUTHOR = UNKNOWN;
	public String AUTHOR_EMAIL = UNKNOWN;
	public String GITHUB_LINK = UNKNOWN;

	private static Properties info = new Properties();

	private Properties() {
		final java.util.Properties prop = new java.util.Properties();
		try {
			final InputStream in = this.getClass().getClassLoader().getResourceAsStream(PROPERTIES_FILE);
			prop.load(in);
			in.close();

			this.ABBREVIATION = prop.getProperty("abbreviation");
			this.NAME = prop.getProperty("name");
			this.VERSION = prop.getProperty("version");
			this.BUILDNUMBER = prop.getProperty("buildnumber");
			this.AUTHOR = prop.getProperty("author");
			this.AUTHOR_EMAIL = prop.getProperty("email");
			this.GITHUB_LINK = prop.getProperty("github");
		} catch (final Exception e) {
			this.ABBREVIATION = UNKNOWN;
			this.NAME = UNKNOWN;
			this.VERSION = UNKNOWN;
			this.BUILDNUMBER = UNKNOWN;
			this.AUTHOR = UNKNOWN;
			this.AUTHOR_EMAIL = UNKNOWN;
			this.GITHUB_LINK = UNKNOWN;
			Log.setPrefixEnabled(false);
			Log.warning("Could not read properties file: tool.properties");
			Log.setPrefixEnabled(true);
		}
	}

	public static Properties info() {
		return info;
	}
}
