package de.foellix.aql.system;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.RED;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import de.foellix.aql.Log;

public class ProcessOutputPipe extends Thread {
	private InputStream inputStream;
	private ProcessWrapper pw;
	private boolean errorStream;
	private boolean canceled;

	public ProcessOutputPipe(InputStream inputStream, ProcessWrapper pw, boolean errorStream) {
		this.inputStream = inputStream;
		this.pw = pw;
		this.errorStream = errorStream;
		this.canceled = false;
	}

	@Override
	public void run() {
		try (BufferedReader outputStream = new BufferedReader(new InputStreamReader(this.inputStream))) {
			while (!this.canceled || outputStream.ready()) {
				if (!outputStream.ready()) {
					try {
						Thread.sleep(500);
					} catch (final InterruptedException e) {
						if (this.canceled) {
							break;
						}
					}
					continue;
				}

				final String line = outputStream.readLine();
				this.pw.addOutputLine(line);
				if (this.errorStream) {
					Log.msg(ansi().fg(RED).a("Process (" + this.pw.getPID() + ") error: " + line), Log.DEBUG_DETAILED);
				} else {
					Log.msg("Process (" + this.pw.getPID() + ") output: " + line, Log.DEBUG_DETAILED);
				}
			}
		} catch (final IOException e) {
			Log.msg("Could not read the " + (this.errorStream ? "error" : "default") + " output of process: "
					+ this.pw.getPID() + Log.getExceptionAppendix(e), Log.DEBUG_DETAILED);
		}
	}

	@Override
	public void interrupt() {
		this.canceled = true;
		super.interrupt();
	}

	public void done() {
		this.canceled = true;
	}
}
