package de.foellix.aql.config;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import de.foellix.aql.helper.CLIHelper;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.helper.Helper;

public class ConfigTest {
	@AfterAll
	public static void reset() {
		CLIHelper.evaluateConfig("config.xml");
	}

	@Test
	public void test01() {
		boolean noException = true;

		final File defaultConfig = new File("config.xml");
		final File defaultConfigBAK = FileHelper.makeUnique(new File("config_test_bak.xml"));
		boolean fileWasMoved = false;

		try {
			if (defaultConfig.exists()) {
				Files.move(defaultConfig.toPath(), defaultConfigBAK.toPath(), StandardCopyOption.ATOMIC_MOVE);
				fileWasMoved = true;
			}

			CLIHelper.evaluateConfig("examples/config_example.xml");
			final Config cfg = ConfigHandler.getInstance().getConfig();

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
		} finally {
			if (fileWasMoved) {
				try {
					Files.move(defaultConfigBAK.toPath(), defaultConfig.toPath(), StandardCopyOption.ATOMIC_MOVE);
				} catch (final IOException e) {
					System.out.println("Could not move back config file \"" + defaultConfig.getAbsolutePath()
							+ "\" which was moved to \"" + defaultConfigBAK.getAbsolutePath() + "\" before.");
					e.printStackTrace();
				}
			}
		}

		assertTrue(noException);
	}

	@Test
	public void test02() {
		boolean noException = true;

		try {
			ConfigHandler.getInstance().setConfig(new File("examples/config_example.xml"));
			final Config cfg = ConfigHandler.getInstance().getConfig();

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
