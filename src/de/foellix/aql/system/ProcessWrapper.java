package de.foellix.aql.system;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.foellix.aql.Log;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.Task;
import de.foellix.aql.system.task.TaskInfo;
import de.foellix.aql.system.task.ToolTask;

public class ProcessWrapper {
	private final Process process;
	private final long pid;
	private ProcessOutputPipe tOut, tErr;
	private boolean canceled;
	private Lock lock;
	private List<String> output;

	/**
	 * Use this to create a wrapper for extra tasks
	 *
	 * @param process
	 *            The process to be wrapped
	 */
	public ProcessWrapper(Process process) {
		this(null, process);
	}

	/**
	 * Use this to create a wrapper for any other task
	 *
	 * @param task
	 *            parent of the associated process
	 * @param process
	 *            The process to be wrapped
	 */
	public ProcessWrapper(Task task, Process process) {
		ProcessWrapperRegistry.getInstance().register(this);
		this.process = process;
		this.pid = Helper.getPid(process);
		this.canceled = false;
		this.lock = new ReentrantLock();
		this.output = null;
		if (task != null) {
			task.getTaskInfo().setData(TaskInfo.PID, String.valueOf(this.pid));
			if (task instanceof ToolTask) {
				this.output = new LinkedList<>();
			}
		}
	}

	/**
	 * Wraps a process including its commandline output.
	 *
	 * @return 0 on normal termination
	 */
	public int waitFor() {
		try {
			this.tOut = new ProcessOutputPipe(this.process.getInputStream(), this, false);
			this.tErr = new ProcessOutputPipe(this.process.getErrorStream(), this, true);
			this.tOut.start();
			this.tErr.start();

			int returnValue;
			try {
				returnValue = this.process.waitFor();
			} catch (final InterruptedException e) {
				returnValue = 1;
			}
			this.tOut.done();
			this.tErr.done();

			this.lock.lock();
			try {
				if (this.tOut.isAlive()) {
					this.tOut.join();
				}
				if (this.tErr.isAlive()) {
					this.tErr.join();
				}
			} catch (final InterruptedException e) {
				Log.msg("Output forwarding was interrupted!", Log.DEBUG_DETAILED);
			}
			this.lock.unlock();

			return returnValue;
		} finally {
			ProcessWrapperRegistry.getInstance().unregister(this);
		}
	}

	public boolean cancel() {
		this.lock.lock();

		// Try graceful destroy
		if (this.process.isAlive()) {
			this.process.destroy();
			try {
				Thread.sleep(500);
			} catch (final InterruptedException e) {
				Log.error("Someting went wrong while killing processes!" + Log.getExceptionAppendix(e));
			}

			// Destroy forcibly
			if (this.process.isAlive()) {
				this.process.destroyForcibly();
				try {
					Thread.sleep(500);
				} catch (final InterruptedException e) {
					Log.error("Someting went wrong while killing processes forcibly!" + Log.getExceptionAppendix(e));
				}

				// Output warning
				if (this.process.isAlive()) {
					Log.warning("Process " + this.pid + " could not be canceled! Consider canceling manually!");
				}
			}
		}

		// Cancel output pipes
		if (this.tOut.isAlive()) {
			this.tOut.interrupt();
		}
		if (this.tErr.isAlive()) {
			this.tErr.interrupt();
		}
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
			Log.error("Someting went wrong while canceling output pipes!" + Log.getExceptionAppendix(e));
		}

		this.canceled = true;
		this.lock.unlock();
		return !this.process.isAlive();
	}

	public boolean isAlive() {
		return this.process.isAlive();
	}

	public boolean isCanceled() {
		return this.canceled;
	}

	public long getPID() {
		return this.pid;
	}

	public List<String> getOutput() {
		return this.output;
	}

	protected void addOutputLine(String line) {
		if (this.output != null) {
			this.output.add(line);
		}
	}
}