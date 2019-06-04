package de.foellix.aql.system.task;

import java.io.File;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.System;

public class Task extends Thread {
	private final System parent;
	private final TaskInfo taskinfo;

	private long start, time;
	private final long timeout;
	private boolean done;
	private boolean interrupted;
	private boolean executed;

	private Process process;
	private ProcessWrapper processWrapper;

	public Task(final System parent, final TaskInfo taskinfo, final long timeout) {
		super();

		this.parent = parent;
		this.taskinfo = taskinfo;
		this.timeout = timeout;
		this.done = false;
		this.interrupted = false;
		this.executed = false;
	}

	@Override
	public void run() {
		try {
			if (TaskMemory.getInstance().contains(this)) {
				this.start = java.lang.System.currentTimeMillis() - this.timeout * 1000;
				throw new TaskAbortedBeforeException("The associated task has been aborted before.");
			}

			if (this.timeout > 0) {
				new TaskTimer(this, this.timeout).start();
			}

			this.start = java.lang.System.currentTimeMillis();
			executeTaskHooks(true);
			new ExtraTask(this.taskinfo, TaskStatus.STATUS_ENTRY).runAndWait();
			if (this.taskinfo instanceof ToolTaskInfo) {
				new ToolTask(this).execute();
			} else if (this.taskinfo instanceof PreprocessorTaskInfo) {
				new PreprocessorTask(this).execute();
			} else {
				new OperatorTask(this).execute();
			}
			executeTaskHooks(false);
		} catch (final Exception err) {
			this.time = Math.max(1, (java.lang.System.currentTimeMillis() - this.start) / 1000);
			TaskMemory.getInstance().aborted(this);
			if (this.taskinfo instanceof ToolTaskInfo) {
				new ToolTask(this).abort(err);
			} else if (this.taskinfo instanceof PreprocessorTaskInfo) {
				new PreprocessorTask(this).abort(err);
			} else {
				new OperatorTask(this).abort(err);
			}
			// Remember: Extra task is performed before the process is killed
			this.parent.getScheduler().finishedTask(this, this.taskinfo, TaskStatus.STATUS_ABORT);
		} finally {
			this.done = true;
		}
	}

	@Override
	public void interrupt() {
		if (this.process != null) {
			this.interrupted = true;

			new ExtraTask(this.taskinfo, TaskStatus.STATUS_ABORT).runAndWait();

			// Try graceful destroy
			if (this.process.isAlive()) {
				this.process.destroy();
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					Log.error("Someting went wrong while killing processes: " + e.getMessage());
				}

				// Destroy forcibly
				if (this.process.isAlive()) {
					this.process.destroyForcibly();
					try {
						Thread.sleep(1000);
					} catch (final InterruptedException e) {
						Log.error("Someting went wrong while killing processes: " + e.getMessage());
					}
				}
			}
		}
		super.interrupt();
	}

	public int waitFor(String[] runCmd, String path) throws Exception {
		this.process = new ProcessBuilder(runCmd).directory(new File(path)).start();
		this.taskinfo.setPID(Helper.getPid(this.process));

		this.processWrapper = new ProcessWrapper(this.process);
		return this.processWrapper.waitFor();
	}

	public void successPart1(String msg) {
		Log.msg(replaceTime(msg), Log.IMPORTANT);
		this.executed = true;
	}

	public void successPart2(boolean wait) throws InterruptedException {
		if (wait && !this.taskinfo.getTool().isExternal() && this.taskinfo.getTool().getExecute().getInstances() == 1) {
			Thread.sleep(1000);
		}

		new ExtraTask(this.taskinfo, TaskStatus.STATUS_SUCCESS).runAndWait();
		getParent().getScheduler().finishedTask(this, this.taskinfo, TaskStatus.STATUS_SUCCESS);
	}

	public void failed(String msg) throws InterruptedException {
		if (this.interrupted) {
			Log.msg("Task was interrupted!", Log.DEBUG);
		}

		Log.msg(replaceTime(msg), Log.IMPORTANT);

		new ExtraTask(this.taskinfo, TaskStatus.STATUS_FAIL).runAndWait();
		this.parent.getScheduler().finishedTask(this, this.taskinfo, TaskStatus.STATUS_FAIL);
	}

	private void executeTaskHooks(boolean before) {
		final List<ITaskHook> hooks;
		if (before) {
			hooks = this.parent.getTaskHooksBefore().getHooks().get(this.taskinfo.getTool());
		} else {
			hooks = this.parent.getTaskHooksAfter().getHooks().get(this.taskinfo.getTool());
		}
		if (hooks != null) {
			for (final ITaskHook hook : hooks) {
				try {
					hook.execute(this.taskinfo);
				} catch (final Exception e) {
					Log.warning("Something went wrong while executing hook (" + e.getClass().getName() + "): "
							+ e.getMessage());
				}
			}
		}
	}

	// public void interruptJava9() {
	// if (this.process != null) {
	// this.destroyed = true;
	//
	// final StringBuilder sb = new StringBuilder();
	// if (Log.logIt(Log.DEBUG)) {
	// sb.append("Waiting for kill of process (pid: " + this.process.pid() + ") and
	// subprocesses (pid/s: ");
	// final int initiallength = sb.length();
	// this.process.descendants()
	// .forEach(handler -> sb.append((sb.length() > initiallength ? ", " : "") +
	// handler.pid()));
	// sb.append(").");
	// }
	//
	// // Kill descendants
	// this.process.descendants().filter(ProcessHandle::isAlive).forEach(handler ->
	// handler.destroy());
	// try {
	// Thread.sleep(1000);
	// } catch (final InterruptedException e) {
	// Log.error("Someting went wrong while killing processes: " + e.getMessage());
	// }
	// this.process.descendants().filter(ProcessHandle::isAlive).forEach(handler ->
	// handler.destroyForcibly());
	//
	// // Kill process
	// this.process.destroy();
	// try {
	// Thread.sleep(1000);
	// } catch (final InterruptedException e) {
	// Log.error("Someting went wrong while killing processes: " + e.getMessage());
	// }
	// if (this.process.isAlive()) {
	// this.process.destroyForcibly();
	// }
	//
	// // Debug message
	// Log.msg(sb.toString(), Log.DEBUG);
	//
	// // Wait for killall
	// this.allDestroyed = true;
	// do {
	// this.process.descendants().filter(ProcessHandle::isAlive).forEach(handle ->
	// this.allDestroyed = false);
	// if (this.allDestroyed && this.process.isAlive()) {
	// this.allDestroyed = false;
	// }
	// if (!this.allDestroyed) {
	// try {
	// Thread.sleep(100);
	// } catch (final InterruptedException e) {
	// Log.error("Someting went wrong while killing processes: " + e.getMessage());
	// }
	// }
	// } while (!this.allDestroyed);
	// }
	//
	// super.interrupt();
	// }

	// Replace %TIME% by actual time
	private String replaceTime(String msg) {
		this.time = Math.max(1, (java.lang.System.currentTimeMillis() - this.start) / 1000);
		return msg.replaceAll("%TIME%", String.valueOf(this.time));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Task other = (Task) obj;
		if (this.parent == null) {
			if (other.parent != null) {
				return false;
			}
		} else if (!this.parent.equals(other.parent)) {
			return false;
		}
		if (this.taskinfo == null) {
			if (other.taskinfo != null) {
				return false;
			}
		} else if (!this.taskinfo.equals(other.taskinfo)) {
			return false;
		}
		if (this.timeout != other.timeout) {
			return false;
		}
		return true;
	}

	public System getParent() {
		return this.parent;
	}

	public TaskInfo getTaskinfo() {
		return this.taskinfo;
	}

	public long getTime() {
		return this.time;
	}

	public Process getProcess() {
		return this.process;
	}

	public boolean isExecuted() {
		return this.executed;
	}

	public boolean isDone() {
		return this.done;
	}
}
