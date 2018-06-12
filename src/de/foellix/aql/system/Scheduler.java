package de.foellix.aql.system;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.foellix.aql.Log;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.WaitingAnswer;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.helper.MemoryHelper;
import de.foellix.aql.system.task.ExtraTask;
import de.foellix.aql.system.task.OperatorTaskInfo;
import de.foellix.aql.system.task.PreprocessorTaskInfo;
import de.foellix.aql.system.task.Task;
import de.foellix.aql.system.task.TaskInfo;
import de.foellix.aql.system.task.TaskStatus;
import de.foellix.aql.system.task.ToolTaskInfo;

public class Scheduler {
	private final System parent;

	private boolean alwaysPreferLoading = true;
	private long timeout = -1;

	private long waittime;
	private TaskInfo lastOnExitTask;

	private final Lock lock;
	private final List<Task> schedule;
	private final Map<Tool, Integer> runningInstances;
	private final List<Task> currentlyRunningThreads;
	private int memoryInUse;
	private int status;
	private int waiting;

	Scheduler(System parent) {
		this.parent = parent;

		this.lock = new ReentrantLock();
		this.schedule = new ArrayList<>();
		this.runningInstances = new HashMap<>();
		this.currentlyRunningThreads = new ArrayList<>();
		this.memoryInUse = 0;
		this.status = 0;
		this.waiting = 0;
	}

	void scheduleTool(final ToolTaskInfo taskInfo) {
		Answer storedAnswer = Storage.getInstance().load(taskInfo.getTool(), taskInfo.getQuestion());
		if (storedAnswer == null && this.alwaysPreferLoading) {
			storedAnswer = Storage.getInstance().load(null, taskInfo.getQuestion(), true);
		}
		if (storedAnswer == null) {
			this.schedule.add(new Task(this.parent, taskInfo, this.timeout));
		} else {
			this.parent.localAnswerAvailable(taskInfo.getQuestion(), storedAnswer);
		}
	}

	void schedulePreprocessor(final PreprocessorTaskInfo taskInfo) {
		final App storedPreprocessedVersion = Storage.getInstance().load(taskInfo.getTool(), taskInfo.getApp());
		if (storedPreprocessedVersion == null) {
			this.schedule.add(new Task(this.parent, taskInfo, this.timeout));
		} else {
			this.parent.preprocessingFinished(taskInfo, storedPreprocessedVersion);
		}
	}

	void scheduleOperator(final OperatorTaskInfo taskInfo) {
		final Answer storedAnswer = Storage.getInstance().load(taskInfo.getTool(), taskInfo.getQuestion());
		if (storedAnswer == null) {
			this.schedule.add(new Task(this.parent, taskInfo, this.timeout));
		} else {
			this.parent.operatorExecuted(taskInfo.getWaitingAnswer(), storedAnswer);
		}
	}

	void runSchedule() {
		if (this.timeout > 0) {
			this.waittime = this.timeout * 1000;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (Scheduler.this.waiting > 0) {
					for (int i = 0; true; i++) {
						try {
							Scheduler.this.lock.lock();

							if (i < Scheduler.this.schedule.size() && Scheduler.this.schedule.size() > 0) {
								final Task task = Scheduler.this.schedule.get(i);

								// Memory available?
								if (!MemoryHelper.getInstance()
										.checkMemoryAvailable(task.getTaskinfo().getMemoryUsage(), true)) {
									final int temp = task.getTaskinfo().getMemoryUsage();
									task.getTaskinfo().setMemoryUsage(MemoryHelper.getInstance()
											.getPossibleMemory(task.getTaskinfo().getMemoryUsage()));
									Log.warning("Insufficient memory for task " + task.getId() + ": " + temp
											+ " GB memory will never become available. Decreasing demand to "
											+ task.getTaskinfo().getMemoryUsage() + " GB.");
								}
								if (task.getTaskinfo()
										.getMemoryUsage() <= ConfigHandler.getInstance().getConfig().getMaxMemory()
												- Scheduler.this.memoryInUse
										&& MemoryHelper.getInstance()
												.checkMemoryAvailable(task.getTaskinfo().getMemoryUsage(), false)) {
									// Another concurrent instance allowed?
									if (Scheduler.this.runningInstances.get(task.getTaskinfo().getTool()) == null
											|| (Scheduler.this.runningInstances.get(task.getTaskinfo().getTool()) < task
													.getTaskinfo().getTool().getInstances()
													|| task.getTaskinfo().getTool().getInstances() <= 0)) {
										boolean ready = true;
										if (task.getTaskinfo() instanceof OperatorTaskInfo) {
											for (final Answer answerPart : ((OperatorTaskInfo) task.getTaskinfo())
													.getWaitingAnswer().getAnswers()) {
												if (answerPart instanceof WaitingAnswer
														&& !((WaitingAnswer) answerPart).hasBeenExecuted()) {
													ready = false;
												}
											}
										}
										if (ready) {
											Scheduler.this.memoryInUse += task.getTaskinfo().getMemoryUsage();
											if (Scheduler.this.runningInstances
													.get(task.getTaskinfo().getTool()) == null) {
												Scheduler.this.runningInstances.put(task.getTaskinfo().getTool(), 1);
											} else {
												Scheduler.this.runningInstances.replace(task.getTaskinfo().getTool(),
														Scheduler.this.runningInstances
																.get(task.getTaskinfo().getTool()) + 1);
											}

											Scheduler.this.currentlyRunningThreads.add(task);
											task.start();

											if (Scheduler.this.timeout > 0) {
												Scheduler.this.waittime = Scheduler.this.timeout * 1000;
											}

											Scheduler.this.schedule.remove(i);
											i--;
										}
									}
								}
							} else {
								break;
							}
						} finally {
							Scheduler.this.lock.unlock();
							Scheduler.this.parent.progress();
						}
					}

					try {
						Thread.sleep(100);
						if (Scheduler.this.timeout > 0 && Scheduler.this.currentlyRunningThreads.size() == 0) {
							Scheduler.this.waittime -= 100;
							if (Scheduler.this.lastOnExitTask != null && Scheduler.this.waittime <= 0) {
								Log.warning("System seems to be stuck. Reexecuting last onExit task! (" + Helper
										.replaceVariables(Scheduler.this.lastOnExitTask.getTool().getRunOnExit(),
												Scheduler.this.lastOnExitTask)
										+ ")");
								try {
									new ExtraTask(Scheduler.this.lastOnExitTask, null).runAndWait();
								} catch (final Exception e) {
									Log.warning("Could not execute last onExit task: " + e.getMessage());
								}
							}
						}
					} catch (final InterruptedException e) {
						Log.error("Interrupted while reexecuting last onExit task.");
					}
				}
			}
		}).start();

		if (Log.logIt(Log.DEBUG)) {
			new Thread(new Runnable() {
				private int localStatus;

				@Override
				public void run() {
					Scheduler.this.status++;
					this.localStatus = Scheduler.this.status;

					boolean first = true;
					while (Scheduler.this.status == this.localStatus && Scheduler.this.waiting > 0) {
						if (!first) {
							final StringBuilder sb = new StringBuilder("Running processes:\n");
							for (final Task outTask : Scheduler.this.currentlyRunningThreads) {
								if (outTask.getTaskinfo() instanceof ToolTaskInfo) {
									sb.append("# " + outTask.getTaskinfo().getTool().getName() + ": "
											+ ((ToolTaskInfo) outTask.getTaskinfo()).getQuestion().toString() + "\n");
								} else if (outTask.getTaskinfo() instanceof PreprocessorTaskInfo) {
									sb.append("# " + outTask.getTaskinfo().getTool().getName() + ": "
											+ ((PreprocessorTaskInfo) outTask.getTaskinfo()).getApp().getFile() + " | "
											+ ((PreprocessorTaskInfo) outTask.getTaskinfo()).getKeyword() + "\n");
								} else if (outTask.getTaskinfo() instanceof OperatorTaskInfo) {
									sb.append("# " + outTask.getTaskinfo().getTool().getName() + ": "
											+ ((OperatorTaskInfo) outTask.getTaskinfo()).getQuestion().getOperator()
											+ "\n");
								}
							}
							Log.msg(sb.toString().substring(0, sb.toString().lastIndexOf("\n")), Log.DEBUG);
						} else {
							first = false;
						}

						for (int i = 0; i < 300000; i += 500) {
							try {
								Thread.sleep(500);
								if (Scheduler.this.waiting <= 0) {
									return;
								}
							} catch (final InterruptedException e) {
								// do nothing
							}
						}
					}
				}
			}).start();
		}
	}

	public void finishedTask(final Task from, final TaskInfo taskInfo, final TaskStatus status) {
		// Execute OnExit
		this.lastOnExitTask = taskInfo;
		new ExtraTask(taskInfo, null).runAndWait();

		// Finish task
		try {
			this.lock.lock();

			this.currentlyRunningThreads.remove(from);
			this.memoryInUse -= taskInfo.getMemoryUsage();
			this.runningInstances.replace(taskInfo.getTool(), this.runningInstances.get(taskInfo.getTool()) - 1);

			if (status != TaskStatus.STATUS_SUCCESS) {
				if (taskInfo instanceof ToolTaskInfo) {
					taskInfo.setTool(ToolSelector.getInstance().selectTool(((ToolTaskInfo) taskInfo).getQuestion(),
							ToolSelector.getInstance().getPriority(taskInfo.getTool(),
									((ToolTaskInfo) taskInfo).getQuestion().getFeatures())));
				} else if (taskInfo instanceof PreprocessorTaskInfo) {
					taskInfo.setTool(ToolSelector.getInstance().selectPreprocessor(
							((PreprocessorTaskInfo) taskInfo).getQuestion(),
							((PreprocessorTaskInfo) taskInfo).getKeyword(),
							ToolSelector.getInstance().getPriority(taskInfo.getTool(),
									((PreprocessorTaskInfo) taskInfo).getQuestion().getFeatures())));
				} else {
					taskInfo.setTool(
							ToolSelector.getInstance().selectOperator(((OperatorTaskInfo) taskInfo).getQuestion(),
									ToolSelector.getInstance().getPriority(taskInfo.getTool())));
				}
				if (taskInfo.getTool() != null) {
					this.schedule.add(new Task(this.parent, taskInfo, this.timeout));
				} else {
					if (taskInfo instanceof ToolTaskInfo) {
						this.parent.localAnswerAvailable(((ToolTaskInfo) taskInfo).getQuestion(), null);
					} else if (taskInfo instanceof PreprocessorTaskInfo) {
						this.parent.preprocessingFinished(((PreprocessorTaskInfo) taskInfo), null);
					} else {
						this.parent.operatorExecuted(((OperatorTaskInfo) taskInfo).getWaitingAnswer(), null);
					}
				}
			}
		} catch (final NullPointerException e) {
			Log.msg("Task finished after abort: " + e.getMessage(), Log.DEBUG_DETAILED);
		} finally {
			this.lock.unlock();
		}
	}

	public void decreaseWaiting() {
		this.waiting--;
	}

	// Getters & Setters
	public boolean isAlwaysPreferLoading() {
		return this.alwaysPreferLoading;
	}

	public List<Task> getSchedule() {
		return this.schedule;
	}

	public Map<Tool, Integer> getRunningInstances() {
		return this.runningInstances;
	}

	public List<Task> getCurrentlyRunningThreads() {
		return this.currentlyRunningThreads;
	}

	public int getWaiting() {
		return this.waiting;
	}

	public void setAlwaysPreferLoading(boolean alwaysPreferLoading) {
		this.alwaysPreferLoading = alwaysPreferLoading;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public void setWaiting(int waiting) {
		this.waiting = waiting;
	}
}
