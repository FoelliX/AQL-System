package de.foellix.aql.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import de.foellix.aql.helper.Helper;

public class ConfigTest {
	@Test
	public void test() {
		ConfigHandler.getInstance().setConfig(new File("examples/config_example.xml"));
		final Config cfg = ConfigHandler.getInstance().getConfig();

		boolean noException = true;
		try {
			if (cfg.getTools() != null) {
				System.out.println("*** TOOLS ***\n" + Helper.toString(cfg.getTools().getTool()) + "\n\n");
			}
			if (cfg.getPreprocessors() != null) {
				System.out.println(
						"*** PREPROCESSORS ***\n" + Helper.toString(cfg.getPreprocessors().getTool()) + "\n\n");
			}
			if (cfg.getOperators() != null) {
				System.out.println("*** OPERATORS ***\n" + Helper.toString(cfg.getOperators().getTool()) + "\n\n");
			}
			if (cfg.getConverters() != null) {
				System.out.println("*** CONVERTERS ***\n" + Helper.toString(cfg.getConverters().getTool()));
			}
		} catch (final Exception e) {
			noException = false;
			e.printStackTrace();
		}

		assertTrue(noException);
	}
}
