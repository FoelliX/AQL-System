package de.foellix.aql.converter;

import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.config.Tool;
import de.foellix.aql.converter.amandroid.ConverterAmandroid;
import de.foellix.aql.converter.amandroid.ConverterAmandroid2;
import de.foellix.aql.converter.dialdroid.ConverterDIALDroid;
import de.foellix.aql.converter.didfail.ConverterDidFail;
import de.foellix.aql.converter.droidsafe.ConverterDroidSafe;
import de.foellix.aql.converter.flowdroid.ConverterFD;
import de.foellix.aql.converter.horndroid.ConverterHD;
import de.foellix.aql.converter.ic3.ConverterIC3;
import de.foellix.aql.converter.iccta.ConverterIccTA;
import de.foellix.aql.converter.panda2.ConverterPAndA2;

public class DefaultConverterRegistry {
	private final List<Identifier> converters;

	private static DefaultConverterRegistry instance = new DefaultConverterRegistry();

	private DefaultConverterRegistry() {
		this.converters = new ArrayList<>();

		// Default converters
		this.converters.add(new Identifier(new DefaultConverter("PAndA2", ConverterPAndA2.class), "PAndA2"));
		this.converters
				.add(new Identifier(new DefaultConverter("FlowDroid", ConverterIccTA.class), "FlowDroid", "1", false));
		this.converters
				.add(new Identifier(new DefaultConverter("FlowDroid*", ConverterFD.class), "FlowDroid", "2", true));
		this.converters.add(new Identifier(new DefaultConverter("IccTA", ConverterIccTA.class), "IccTA", "1", true));
		this.converters.add(new Identifier(new DefaultConverter("IccTA", ConverterFD.class), "IccTA", "2", false));
		this.converters.add(new Identifier(new DefaultConverter("IC3", ConverterIC3.class), "IC3"));
		this.converters.add(new Identifier(new DefaultConverter("DidFail", ConverterDidFail.class), "DidFail"));
		this.converters.add(
				new Identifier(new DefaultConverter("Amandroid", ConverterAmandroid.class), "Amandroid", "312", false));
		this.converters.add(new Identifier(new DefaultConverter("Amandroid*", ConverterAmandroid2.class), "Amandroid",
				"320", true));
		this.converters.add(new Identifier(new DefaultConverter("DIALDroid", ConverterDIALDroid.class), "DIALDroid"));
		this.converters.add(new Identifier(new DefaultConverter("DroidSafe", ConverterDroidSafe.class), "DroidSafe"));
		this.converters.add(new Identifier(new DefaultConverter("HornDroid", ConverterHD.class), "HornDroid"));
	}

	public static DefaultConverterRegistry getInstance() {
		return instance;
	}

	public Tool getConverter(Tool tool) {
		// Search candidates by name
		final List<Identifier> candidates = new ArrayList<>();
		final String toolLC = tool.getName().toLowerCase();
		for (final Identifier identifier : this.converters) {
			if (toolLC.startsWith(identifier.getName().toLowerCase())) {
				candidates.add(identifier);
			}
		}

		// Find most promising candidate by version
		int bestFit = -1;
		Identifier selection = null;
		for (final Identifier identifier : candidates) {
			final int fit = fits(tool, identifier);
			if (fit > bestFit) {
				selection = identifier;
				bestFit = fit;
			}
		}

		if (bestFit < 0) {
			// Find by default
			for (final Identifier identifier : candidates) {
				if (identifier.isDefault()) {
					return identifier.getConverter();
				}
			}
		}

		// Return converter
		if (selection == null) {
			return null;
		} else {
			return selection.getConverter();
		}
	}

	private int fits(Tool tool, Identifier identifier) {
		// Compare version
		final int fits1 = fits(tool.getVersion(), identifier.getVersion());

		// Compare tool-suffix
		final int fits2 = fits(tool.getName().toLowerCase().replace(identifier.getName(), ""), identifier.getVersion());

		return Math.max(fits1, fits2);
	}

	private int fits(String version1, String version2) {
		if (version1 == null || version2 == null || version1.isEmpty() || version2.isEmpty()) {
			return -1;
		}

		version1 = version1.replace(".", "");
		version2 = version2.replace(".", "");
		int fits = -1;
		for (int i = 0; i < version1.length() && i < version2.length(); i++) {
			if (Integer.valueOf(version1.charAt(i)) == Integer.valueOf(version2.charAt(i))) {
				fits += 2;
			} else if (Integer.valueOf(version1.charAt(i)) > Integer.valueOf(version2.charAt(i))) {
				fits += 1;
			} else {
				break;
			}
		}

		fits = fits * Integer.valueOf(version2);
		return fits;
	}

	private class Identifier {
		private Tool converter;
		private String name;
		private String version;
		private boolean isDefault;

		public Identifier(Tool converter, String name) {
			super();
			this.converter = converter;
			this.name = name.toLowerCase();
			this.version = null;
			this.isDefault = true;
		}

		public Identifier(Tool converter, String name, String version, boolean isDefault) {
			super();
			this.converter = converter;
			this.name = name.toLowerCase();
			this.version = version;
			this.isDefault = isDefault;
		}

		public Tool getConverter() {
			return this.converter;
		}

		public String getName() {
			return this.name;
		}

		public String getVersion() {
			return this.version;
		}

		public boolean isDefault() {
			return this.isDefault;
		}
	}
}
