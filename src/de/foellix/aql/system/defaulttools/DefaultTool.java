package de.foellix.aql.system.defaulttools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.Properties;
import de.foellix.aql.config.Execute;
import de.foellix.aql.config.Priority;
import de.foellix.aql.config.Tool;

public class DefaultTool extends Tool {
	public static final String DEFAULT_TOOL_USES = "Default";
	private static final int DEFAULT_TOOL_INSTANCES = 0;
	private static final int DEFAULT_TOOL_MEMORY_PER_INSTANCE = 1;
	private static final int DEFAULT_TOOL_PRIORITY = 1;
	private static final List<Priority> DEFAULT_TOOL_PRIORITY_LIST = new ArrayList<>();
	private static final String DEFAULT_PATH = new File(System.getProperty("user.dir")).getAbsolutePath();

	protected final Execute execute = new Execute();

	public DefaultTool() {
		super();

		if (DEFAULT_TOOL_PRIORITY_LIST.isEmpty()) {
			final Priority priority = new Priority();
			priority.setValue(DEFAULT_TOOL_PRIORITY);
			DEFAULT_TOOL_PRIORITY_LIST.add(priority);
		}

		this.setExternal(false);
		this.execute.setRun(this.getClass().getSimpleName() + " (" + Properties.info().VERSION + ")");
		this.execute.setInstances(DEFAULT_TOOL_INSTANCES);
		this.execute.setMemoryPerInstance(DEFAULT_TOOL_MEMORY_PER_INSTANCE);
	}

	@Override
	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getVersion() {
		return Properties.info().VERSION;
	}

	@Override
	public Execute getExecute() {
		return this.execute;
	}

	@Override
	public String getPath() {
		return DEFAULT_PATH;
	}

	@Override
	public List<Priority> getPriority() {
		return DEFAULT_TOOL_PRIORITY_LIST;
	}
}