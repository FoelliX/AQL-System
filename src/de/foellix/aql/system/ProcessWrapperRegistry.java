package de.foellix.aql.system;

import java.util.HashSet;
import java.util.Set;

import de.foellix.aql.Log;

public class ProcessWrapperRegistry {
	private Set<ProcessWrapper> processWrappers;

	private static ProcessWrapperRegistry instance = new ProcessWrapperRegistry();

	private ProcessWrapperRegistry() {
		this.processWrappers = new HashSet<>();
	}

	public static ProcessWrapperRegistry getInstance() {
		return instance;
	}

	public void register(ProcessWrapper processWrapper) {
		this.processWrappers.add(processWrapper);
	}

	public void unregister(ProcessWrapper processWrapper) {
		this.processWrappers.remove(processWrapper);
	}

	public void reportAlive() {
		int counter = 0;
		for (final ProcessWrapper processWrapper : this.processWrappers) {
			if (processWrapper.isAlive()) {
				counter++;
			}
		}
		Log.msg("Processes alive: " + counter + "/" + this.processWrappers.size(), Log.NORMAL);
	}

	public void cancelAll() {
		for (final ProcessWrapper processWrapper : this.processWrappers) {
			if (!processWrapper.isCanceled()) {
				processWrapper.cancel();
			}
		}
	}

}