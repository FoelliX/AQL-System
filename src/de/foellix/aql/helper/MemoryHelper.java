package de.foellix.aql.helper;

import java.lang.management.ManagementFactory;
import java.util.Set;

import com.sun.management.OperatingSystemMXBean;

import de.foellix.aql.Log;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.system.task.Task;

public class MemoryHelper {
	private final OperatingSystemMXBean operatingSystemMXBean;
	private final int availableInGB;

	private static MemoryHelper instance = new MemoryHelper();

	private MemoryHelper() {
		this.operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		this.availableInGB = Math.max(1, (int) Math.floor(
				(this.operatingSystemMXBean.getFreeMemorySize() - Runtime.getRuntime().maxMemory()) / 1000000000d));

		// Override config
		if (ConfigHandler.getInstance().getConfig() != null) {
			if (this.availableInGB < ConfigHandler.getInstance().getConfig().getMaxMemory()) {
				Log.warning("Maximum memory (" + ConfigHandler.getInstance().getConfig().getMaxMemory()
						+ " GB) specified in config file will never become available. Using maximal available memory instead: "
						+ this.availableInGB + " GB");
				ConfigHandler.getInstance().getConfig().setMaxMemory(this.availableInGB);
			}
		}
	}

	public static MemoryHelper getInstance() {
		return instance;
	}

	/**
	 * Return the memory currently available with respect to the tasks running.
	 *
	 * @param tasks
	 *            Currently running tasks
	 * @return the memory available
	 */
	public int getCurrentlyAvailableMemory(Set<Task> tasks) {
		int memoryInUse = 0;
		for (final Task task : tasks) {
			if (!task.getTool().isExternal()) {
				memoryInUse += task.getTool().getExecute().getMemoryPerInstance();
			}
		}
		return getMaxAssignableMemory() - memoryInUse;
	}

	/**
	 * Returns the max memory available in the system
	 *
	 * @return memory in GB
	 */
	public int getMaxAvailableMemory() {
		return this.availableInGB;
	}

	/**
	 * Returns the max memory available and assigned to be used
	 *
	 * @return memory in GB
	 */
	public int getMaxAssignableMemory() {
		if (ConfigHandler.getInstance().getConfig() != null) {
			return Math.min(ConfigHandler.getInstance().getConfig().getMaxMemory(), this.availableInGB);
		} else {
			return this.availableInGB;
		}
	}

	public String getMemoryInfo() {
		return "Maximum memory available: " + this.availableInGB + " GB, Maximum memory assigned in config: "
				+ ConfigHandler.getInstance().getConfig().getMaxMemory() + " GB";
	}
}