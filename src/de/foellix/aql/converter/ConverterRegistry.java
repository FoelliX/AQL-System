package de.foellix.aql.converter;

import java.util.HashMap;
import java.util.Map;

import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.config.Tool;
import de.foellix.aql.converter.amandroid.ConverterAmandroid;
import de.foellix.aql.converter.dialdroid.ConverterDIALDroid;
import de.foellix.aql.converter.didfail.ConverterDidFail;
import de.foellix.aql.converter.droidsafe.ConverterDroidSafe;
import de.foellix.aql.converter.flowdroid.ConverterFD;
import de.foellix.aql.converter.ic3.ConverterIC3;
import de.foellix.aql.converter.iccta.ConverterIccTA;
import de.foellix.aql.converter.panda2.ConverterPAndA2;

public class ConverterRegistry {
	private final Map<String, IConverter> map;

	private static ConverterRegistry instance = new ConverterRegistry();

	private ConverterRegistry() {
		this.map = new HashMap<>();

		// Built-in converters
		this.map.put("PAndA2".toLowerCase(), new ConverterPAndA2());
		this.map.put("FlowDroid".toLowerCase(), new ConverterIccTA());
		this.map.put("FlowDroid2".toLowerCase(), new ConverterFD());
		this.map.put("IccTA".toLowerCase(), new ConverterIccTA());
		this.map.put("IC3".toLowerCase(), new ConverterIC3());
		this.map.put("DidFail".toLowerCase(), new ConverterDidFail());
		this.map.put("Amandroid".toLowerCase(), new ConverterAmandroid());
		this.map.put("DIALDroid".toLowerCase(), new ConverterDIALDroid());
		this.map.put("DroidSafe".toLowerCase(), new ConverterDroidSafe());

		loadCompilersFromConfig();
	}

	public static ConverterRegistry getInstance() {
		return instance;
	}

	private void loadCompilersFromConfig() {
		if (ConfigHandler.getInstance().getConfig().getConverters() != null
				&& !ConfigHandler.getInstance().getConfig().getConverters().getTool().isEmpty()) {
			for (final Tool converter : ConfigHandler.getInstance().getConfig().getConverters().getTool()) {
				this.map.put(converter.getName().toLowerCase(), new ExternalConverter(converter));
			}
		}
	}

	public IConverter getConverter(final Tool tool) {
		return getConverter(tool.getName());
	}

	public IConverter getConverter(final String toolname) {
		return this.map.get(toolname.toLowerCase());
	}
}
