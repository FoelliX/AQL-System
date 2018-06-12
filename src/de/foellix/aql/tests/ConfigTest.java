package de.foellix.aql.tests;

import java.io.File;

import de.foellix.aql.config.Config;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.helper.Helper;

public class ConfigTest {
	public static void main(final String[] args) {
		if (args != null && args.length > 0) {
			ConfigHandler.getInstance().setConfig(new File(args[0]));
		}
		final Config cfg = ConfigHandler.getInstance().getConfig();

		System.out.println("*** TOOLS ***\n" + Helper.toString(cfg.getTools().getTool()) + "\n\n");
		System.out.println("*** PREPROCESSORS ***\n" + Helper.toString(cfg.getPreprocessors().getTool()) + "\n\n");
		System.out.println("*** OPERATORS ***\n" + Helper.toString(cfg.getOperators().getTool()));
	}
}
