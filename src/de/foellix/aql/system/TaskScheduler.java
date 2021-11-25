package de.foellix.aql.system;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import de.foellix.aql.Log;
import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.query.DefaultQuestion;
import de.foellix.aql.datastructure.query.OperatorQuestion;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.helper.MemoryHelper;
import de.foellix.aql.system.defaulttools.DefaultTool;
import de.foellix.aql.system.defaulttools.analysistools.DefaultSootAnalysisTool;
import de.foellix.aql.system.storage.Storage;
import de.foellix.aql.system.task.ConverterTask;
import de.foellix.aql.system.task.ConverterTaskRunner;
import de.foellix.aql.system.task.LoadAnswerTask;
import de.foellix.aql.system.task.LoadAnswerTaskRunner;
import de.foellix.aql.system.task.OperatorTask;
import de.foellix.aql.system.task.OperatorTaskRunner;
import de.foellix.aql.system.task.PreprocessorTask;
import de.foellix.aql.system.task.PreprocessorTaskRunner;
import de.foellix.aql.system.task.RootTask;
import de.foellix.aql.system.task.Task;
import de.foellix.aql.system.task.TaskInfo;
import de.foellix.aql.system.task.TaskRunner;
import de.foellix.aql.system.task.ToolTask;
import de.foellix.aql.system.task.ToolTaskRunner;
import de.foellix.aql.system.task.gui.TaskTreeViewer;
import de.foellix.aql.ui.gui.GUI;

public class TaskScheduler {
	private final AQLSystem parent;

	private long timeout;

	private Task taskTree;
	private Map<Task, TaskRunner> runningTasks;
	private Lock lock;
	private int iterationCounter;

	public TaskScheduler(AQLSystem aqlSystem) {
		this.parent = aqlSystem;
		this.timeout = -1;
		this.runningTasks = new ConcurrentHashMap<>();
		this.lock = new ReentrantLock(true);
	}

	/**
	 * Start scheduling tasks for initial query.
	 *
	 * @param taskTree
	 *            The initial tree of tasks.
	 */
	public void start(Task taskTree) {
		this.iterationCounter = 0;
		this.runningTasks.clear();
		this.taskTree = taskTree;
		next();
	}

	private void next() {
		updateExecutionGraph();
		final Set<Task> leafs;
		final Set<Task> converterLeafs = filterConverterLeafs(this.taskTree.getLeafs(true));
		if (!converterLeafs.isEmpty()) {
			// Prioritize converter leafs
			leafs = filterDoubleLeafs(converterLeafs);
		} else {
			leafs = filterDoubleLeafs(this.taskTree.getLeafs(true));
		}
		for (final Task task : leafs) {
			if (task instanceof RootTask) {
				this.parent.queryFinished(task, Task.STATUS_EXECUTION_SUCCESSFUL);
				updateProgress(false);
				return;
			} else if (!task.getTaskAnswer().isAnswered()) {
				if (!this.runningTasks.containsKey(task)) {
					// TODO: (After 2.0.0 release) Consider time estimation
					if (checkMemory(task) && checkInstances(task)) {
						if (task instanceof ToolTask) {
							this.runningTasks.put(task, new ToolTaskRunner(this, task));
						} else if (task instanceof ConverterTask) {
							this.runningTasks.put(task, new ConverterTaskRunner(this, task));
						} else if (task instanceof PreprocessorTask) {
							this.runningTasks.put(task, new PreprocessorTaskRunner(this, task));
						} else if (task instanceof OperatorTask) {
							this.runningTasks.put(task, new OperatorTaskRunner(this, task));
						} else if (task instanceof LoadAnswerTask) {
							this.runningTasks.put(task, new LoadAnswerTaskRunner(this, task));
						}

						// Start next task
						if (!this.runningTasks.get(task).isAlive()) {
							this.runningTasks.get(task).start();
						}
					}
				}
			}
		}
		updateProgress(false);
	}

	private boolean checkMemory(Task task) {
		if (!task.getTool().isExternal()) {
			if (task.getTool().getExecute().getMemoryPerInstance() <= MemoryHelper.getInstance()
					.getCurrentlyAvailableMemory(this.runningTasks.keySet())) {
				return true;
			} else {
				if (task.getTool().getExecute().getMemoryPerInstance() > MemoryHelper.getInstance()
						.getMaxAssignableMemory()) {
					Log.warning(Helper.getQualifiedName(task.getTool()) + " requests "
							+ task.getTool().getExecute().getMemoryPerInstance()
							+ " GB memory. That amount of memory will never become available. Reducing requested memory to: "
							+ MemoryHelper.getInstance().getMaxAssignableMemory() + " GB ("
							+ MemoryHelper.getInstance().getMemoryInfo() + ")");
					task.getTool().getExecute()
							.setMemoryPerInstance(MemoryHelper.getInstance().getMaxAssignableMemory());
					for (final Task taskToUpdate : this.taskTree.getChildren(true)) {
						if (taskToUpdate.getTool() == task.getTool()) {
							taskToUpdate.getTaskInfo().setData(TaskInfo.MEMORY,
									String.valueOf(taskToUpdate.getTool().getExecute().getMemoryPerInstance()));
						}
					}
					if (task.getTool().getExecute().getMemoryPerInstance() <= MemoryHelper.getInstance()
							.getCurrentlyAvailableMemory(this.runningTasks.keySet())) {
						return true;
					}
				}
				return false;
			}
		}
		return true;
	}

	private boolean checkInstances(Task task) {
		if (!task.getTool().isExternal()) {
			final int instances = task.getTool().getExecute().getInstances();
			if (instances != 0) {
				int counter = 0;
				for (final Task taskToCheck : this.runningTasks.keySet()) {
					if (taskToCheck.getTool() == task.getTool()
							|| (task.getTool() instanceof DefaultTool
									&& task.getTool().getClass().equals(taskToCheck.getTool().getClass()))
							|| (task.getTool() instanceof DefaultSootAnalysisTool
									&& taskToCheck.getTool() instanceof DefaultSootAnalysisTool)) {
						counter++;
					}
				}
				if (counter >= instances) {
					return false;
				}
			}
		}
		return true;
	}

	private void updateProgress(boolean aborted) {
		if (this.parent.getProgressHandler().hasListener()) {
			if (aborted) {
				this.parent.getProgressHandler().progress("Aborted", 0, 0, 0);
			} else {
				this.parent.getProgressHandler().progress("Running iteration " + ++this.iterationCounter,
						this.runningTasks.size(), this.taskTree.countAvailableAnswersOfChildren(),
						this.taskTree.getChildren().size());
			}
		}
	}

	private void updateExecutionGraph() {
		if (this.parent.getOptions().getDrawGraphs()) {
			TaskTreeViewer.update(this.taskTree, this.runningTasks.keySet());
			if (!GUI.started || Log.logIt(Log.DEBUG, true)) {
				Log.msg(this.taskTree.toString(), Log.NORMAL);
			}
		}
	}

	private Set<Task> filterDoubleLeafs(Set<Task> leafs) {
		final Set<Task> filteredLeafs = new HashSet<>();
		final Set<String> hashes = new HashSet<>();
		for (final Task leaf : leafs) {
			final String hash = leaf.getRunCommand(true, false);
			if (!hashes.contains(hash)) {
				filteredLeafs.add(leaf);
				hashes.add(hash);
			}
		}
		return filteredLeafs;
	}

	private Set<Task> filterConverterLeafs(Set<Task> leafs) {
		final Set<Task> filteredLeafs = new HashSet<>();
		for (final Task leaf : leafs) {
			if (leaf instanceof ConverterTask) {
				filteredLeafs.add(leaf);
			}
		}
		return filteredLeafs;
	}

	/**
	 * Cancels any processes running
	 */
	public void cancel() {
		Log.msg("Stopping scheduler!", Log.DEBUG);
		for (final TaskRunner taskRunner : this.runningTasks.values()) {
			if (taskRunner.isAlive()) {
				taskRunner.interrupt();
			}
		}
		updateProgress(true);
	}

	/**
	 * Change tool selected for class to the tool with next lowest priority
	 *
	 * @param task
	 */
	private boolean retry(Task task) {
		final Question question = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(task, true);

		// Select alternative tool
		Tool altTool;
		if (task instanceof ToolTask && question instanceof DefaultQuestion) {
			altTool = ToolSelector.selectAnalysisTool((DefaultQuestion) question, task.getToolsFailedAsArray());
		} else if (task instanceof ConverterTask) {
			altTool = ToolSelector.selectConverter(task.getChildren().iterator().next().getTool(),
					task.getToolsFailedAsArray());
		} else if (task instanceof PreprocessorTask) {
			altTool = ToolSelector.selectPreprocessor(task.getTool().getQuestions(), task.getToolsFailedAsArray());
		} else if (task instanceof OperatorTask && question instanceof OperatorQuestion) {
			altTool = ToolSelector.selectOperator((OperatorQuestion) question, task.getToolsFailedAsArray());
		} else {
			// Could not recognize task while retrying it
			altTool = null;
		}

		// Adapt task and return result
		if (altTool != null && altTool != task.getTool()) {
			// Check default tool
			if (!(altTool instanceof DefaultTool) || altTool.getName() != task.getTool().getName()
					|| altTool.getVersion() != task.getTool().getVersion()) {
				task.setTool(altTool);
				return true;
			}
		}
		return false;
	}

	/**
	 * Called whenever a transformation led to a change of the available task as a side-effect.
	 *
	 * @param taskTree
	 *            The new tree of tasks.
	 */
	public void changed(Task taskTree) {
		this.taskTree = taskTree;
	}

	public void taskFinished(Task task, int status) {
		taskFinished(task, status, null);
	}

	public void taskFinished(Task task, String feedback) {
		taskFinished(task, Task.STATUS_EXECUTION_FAILED, feedback);
	}

	public void taskFinished(Task task, int status, String feedback) {
		this.lock.lock();
		this.runningTasks.remove(task);
		updateProgress(false);
		if (status == Task.STATUS_EXECUTION_SUCCESSFUL) {
			task.update();
			next();
		} else {
			task.getToolsFailed().add(task.getTool());
			updateExecutionGraph();
			if (feedback != null) {
				this.parent.queryCanceled(Task.STATUS_EXECUTION_FAILED,
						"Feedback-Answer received while answering: "
								+ Storage.getInstance().getData().getQuestionFromQuestionTaskMap(task, true)
								+ "\nFeedback: " + feedback);
			} else if (status <= Task.STATUS_EXECUTION_FAILED) {
				final Tool oldTool = task.getTool();
				if (getParent().getOptions().getRetry() && retry(task)) {
					Log.msg(Helper.getQualifiedName(oldTool) + " execution failed! Retrying with "
							+ Helper.getQualifiedName(task.getTool()) + ".", Log.NORMAL);
					next();
				} else {
					if (status == Task.STATUS_EXECUTION_FAILED) {
						this.parent.queryCanceled(Task.STATUS_EXECUTION_FAILED, "Answering a part of the query failed: "
								+ Storage.getInstance().getData().getQuestionFromQuestionTaskMap(task, true));
					} else if (status == Task.STATUS_EXECUTION_TIMEOUT) {
						this.parent.queryCanceled(Task.STATUS_EXECUTION_FAILED,
								"Answering a part of the query timed out: "
										+ Storage.getInstance().getData().getQuestionFromQuestionTaskMap(task, true));
					} else if (status == Task.STATUS_EXECUTION_INTERRUPTED) {
						this.parent.queryCanceled(Task.STATUS_EXECUTION_FAILED,
								"Answering a part of the query canceled: "
										+ Storage.getInstance().getData().getQuestionFromQuestionTaskMap(task, true));
					}
				}
			}
		}
		this.lock.unlock();
	}

	/**
	 * Sets the timeout value (in seconds).
	 *
	 * @param timeout
	 *            value (in seconds)
	 */
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	/**
	 * Returns the timeout in seconds
	 *
	 * @return timeout (in seconds)
	 */
	public long getTimeout() {
		return this.timeout;
	}

	public AQLSystem getParent() {
		return this.parent;
	}

	public Task getTaskTree() {
		return this.taskTree;
	}

	public boolean inProgress(Task task) {
		return this.runningTasks.keySet().contains(task);
	}
}