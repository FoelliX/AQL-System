package de.foellix.aql.helper;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import de.foellix.aql.Log;
import de.foellix.aql.config.ConfigHandler;

public class MemoryHelper {
	private final static long PUFFER = 250000000L;

	private final OperatingSystemMXBean operatingSystemMXBean;
	private final long max;
	private final int adapted;

	private static MemoryHelper instance = new MemoryHelper();

	private MemoryHelper() {
		this.operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
		this.max = ((com.sun.management.OperatingSystemMXBean) this.operatingSystemMXBean).getFreePhysicalMemorySize()
				- Runtime.getRuntime().maxMemory() - PUFFER;
		this.adapted = Math.max(1, (int) Math.floor(this.max / 1000000000d));

		// Override config
		if (this.adapted < ConfigHandler.getInstance().getConfig().getMaxMemory()) {
			Log.warning("Maximum memory (" + ConfigHandler.getInstance().getConfig().getMaxMemory()
					+ " GB) specified in config file will never become available. Using maximal available memory instead: "
					+ this.adapted + " GB");
			ConfigHandler.getInstance().getConfig().setMaxMemory(this.adapted);
		}
	}

	public static MemoryHelper getInstance() {
		return instance;
	}

	public boolean checkMemoryAvailable(int required, boolean ever) {
		if (ever) {
			return this.max >= (required * 1000000000L);
		} else {
			return ((com.sun.management.OperatingSystemMXBean) this.operatingSystemMXBean)
					.getFreePhysicalMemorySize() >= ((required * 1000000000L) + PUFFER);
		}

	}

	public int getPossibleMemory(int want) {
		if (this.max < (want * 1000000000L)) {
			return this.adapted;
		} else {
			return want;
		}
	}
}
