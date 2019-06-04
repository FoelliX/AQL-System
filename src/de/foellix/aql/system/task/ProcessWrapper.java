package de.foellix.aql.system.task;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import de.foellix.aql.Log;

public class ProcessWrapper {
	private final Process process;
	private IOException tException;
	private Thread tOut, tErr;

	public ProcessWrapper(Process process) {
		this.process = process;
	}

	public int waitFor() throws Exception {
		this.tOut = new Thread() {
			@Override
			public void run() {
				try (BufferedReader stdOut = new BufferedReader(
						new InputStreamReader(ProcessWrapper.this.process.getInputStream()))) {
					String line;
					while ((line = stdOut.readLine()) != null) {
						Log.msg("Process (output): " + line, Log.DEBUG_DETAILED);
					}
					stdOut.close();
				} catch (final IOException e) {
					ProcessWrapper.this.tException = e;
				}
			}
		};
		this.tErr = new Thread() {
			@Override
			public void run() {
				try (BufferedReader stdErr = new BufferedReader(
						new InputStreamReader(ProcessWrapper.this.process.getErrorStream()))) {
					String line;
					while ((line = stdErr.readLine()) != null) {
						Log.msg("Process (error): " + line, Log.DEBUG_DETAILED);
					}
				} catch (final IOException e) {
					ProcessWrapper.this.tException = e;
				}
			}
		};

		this.tOut.start();
		this.tErr.start();

		final int returnValue = this.process.waitFor();

		if (this.tException != null) {
			throw this.tException;
		}
		this.tOut.join();
		this.tErr.join();

		return returnValue;
	}
}
