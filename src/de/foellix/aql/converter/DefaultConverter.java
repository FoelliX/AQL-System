package de.foellix.aql.converter;

import java.lang.reflect.Constructor;

import de.foellix.aql.Log;
import de.foellix.aql.system.defaulttools.DefaultTool;

public class DefaultConverter extends DefaultTool {
	private final String targetToolsName;
	private final Class<? extends IConverter> converter;

	public DefaultConverter(String targetToolsName, Class<? extends IConverter> converter) {
		super();
		this.targetToolsName = targetToolsName;
		this.converter = converter;
	}

	@Override
	public String getName() {
		return "DefaultConverter for " + this.targetToolsName;
	}

	public IConverter getConverter() {
		try {
			final Constructor<? extends IConverter> constructor = this.converter.getConstructor();
			final IConverter converterInstance = constructor.newInstance();
			return converterInstance;
		} catch (final Exception e) {
			Log.error("Could not instantiate default converter \"" + this.converter.getName() + "\" for \""
					+ this.targetToolsName + "\"!");
			return null;
		}
	}
}