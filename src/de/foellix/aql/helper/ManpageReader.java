package de.foellix.aql.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ManpageReader {
	private static ManpageReader instance = new ManpageReader();

	private final String manpageStr;

	private ManpageReader() {
		final File manpage = new File("manpage");
		this.manpageStr = new BufferedReader(
				new InputStreamReader(getClass().getClassLoader().getResourceAsStream(manpage.getName()))).lines()
						.parallel().collect(Collectors.joining("\n"));
	}

	public static ManpageReader getInstance() {
		return instance;
	}

	public String getManpageContent() {
		return this.manpageStr;
	}
}
