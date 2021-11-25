package de.foellix.aql.system.task;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.foellix.aql.Log;
import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.datastructure.query.DefaultQuestion;
import de.foellix.aql.datastructure.query.OperatorQuestion;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.TaskCreator;
import de.foellix.aql.system.defaulttools.DefaultTool;
import de.foellix.aql.system.storage.Storage;
import de.foellix.aql.system.task.gui.TaskTreeViewer;

public abstract class Task implements Serializable {
	private static final long serialVersionUID = 4800793822439273893L;

	public static final int STATUS_APK_NOT_FOUND = -5;
	public static final int STATUS_NO_APPROPRIATE_TOOL = -4;
	public static final int STATUS_EXECUTION_INTERRUPTED = -3;
	public static final int STATUS_EXECUTION_TIMEOUT = -2;
	public static final int STATUS_EXECUTION_FAILED = -1;
	public static final int STATUS_EXECUTION_UNKNOWN = 0;
	public static final int STATUS_EXECUTION_SUCCESSFUL = 1;

	public static final String STATUS_TEXT_PLACEHODLER = "Incomplete/Placeholder";
	public static final String STATUS_TEXT_DONE = "Done";
	public static final String STATUS_TEXT_FAILED = "Failed";
	public static final String STATUS_TEXT_RUNNING = "Running";
	public static final String STATUS_TEXT_READY = "Ready";
	public static final String STATUS_TEXT_UNKNOWN = "Unknown";

	transient protected final TaskCreator parent;

	private TaskAnswer taskAnswer;

	private boolean updated;
	private Set<Task> parents;
	private Set<Task> children;

	transient protected Tool tool;
	protected final TaskInfo taskInfo;
	transient protected Set<Tool> toolsFailed;

	public Task(TaskCreator taskCreator, TaskInfo taskInfo, Tool tool) {
		this.parent = taskCreator;

		this.updated = false;
		this.parents = new HashSet<>();
		this.children = new LinkedHashSet<>();

		this.tool = tool;
		this.taskInfo = taskInfo;
	}

	public void update() {
		// Replace placeholder
		for (final Task parent : new HashSet<>(this.parents)) {
			this.parent.tryPlaceholderReplacement(parent);
		}

		// Refresh variables
		for (final Task parent : this.parents) {
			if (!(parent instanceof PlaceholderTask)) {
				parent.refreshVariables(this);
			}
		}

		// Check if query can be transformed now
		this.parent.checkTransformations(this);

		// Set updated
		this.updated = true;
	}

	public abstract void refreshVariables(Task child);

	public boolean isReady() {
		// Check if updated
		for (final Task child : this.children) {
			if (!child.isUpdated()) {
				return false;
			}
		}

		// Check if answered
		for (final Task child : this.children) {
			if (!child.getTaskAnswer().isAnswered()) {
				return false;
			}
		}

		// Ready
		return true;
	}

	private boolean isUpdated() {
		return this.updated;
	}

	public Set<Task> getChildren() {
		return getChildren(false);
	}

	public Set<Task> getChildren(boolean recursive) {
		if (!recursive) {
			return this.children;
		} else {
			final Set<Task> temp = new LinkedHashSet<>();
			temp.addAll(this.children);
			for (final Task child : this.children) {
				temp.addAll(child.getChildren(true));
			}
			return temp;
		}
	}

	public int countAvailableAnswersOfChildren() {
		int counter = 0;
		for (final Task task : this.children) {
			if (task.getTaskAnswer().isAnswered()) {
				counter++;
			}
		}
		return counter;
	}

	public Set<Answer> getAvailableAnswersOfChildren() {
		final Set<Answer> answers = new LinkedHashSet<>();
		for (final Task task : this.children) {
			if (task.getTaskAnswer().getType() == TaskAnswer.ANSWER_TYPE_AQL && task.getTaskAnswer().isAnswered()) {
				answers.add((Answer) task.getTaskAnswer().getAnswer());
			} else if (task.getTaskAnswer().getType() == TaskAnswer.ANSWER_TYPE_FILE
					&& task.getTaskAnswer().isAnswered()) {
				final Answer castedAnswer = AnswerHandler.castToAnswer(task.getTaskAnswer().getAnswerFile());
				if (castedAnswer != null) {
					answers.add(castedAnswer);
				}
			}
		}
		return answers;
	}

	public Set<Task> getLeafs() {
		return getLeafs(false);
	}

	public Set<Task> getLeafs(boolean ignoreAnsweredChildren) {
		final Set<Task> leafs = new HashSet<>();
		if ((this.children == null || this.children.isEmpty() || (ignoreAnsweredChildren && isReady()))
				&& (this instanceof RootTask
						|| (!ignoreAnsweredChildren || (ignoreAnsweredChildren && !this.taskAnswer.isAnswered())))) {
			leafs.add(this);
		} else {
			for (final Task child : this.children) {
				leafs.addAll(child.getLeafs(ignoreAnsweredChildren));
			}
		}
		return leafs;
	}

	public void addChild(Task child) {
		if (!this.children.contains(child)) {
			this.children.add(child);
		}
		if (!child.getParents().contains(this)) {
			child.addParent(this);
		}
	}

	public void removeChild(Task child) {
		this.children.remove(child);
		if (child.getParents().contains(this)) {
			child.removeParent(this);
		}
	}

	public Set<Task> getParents() {
		return this.parents;
	}

	public void addParent(Task parent) {
		if (!this.parents.contains(parent)) {
			this.parents.add(parent);
		}
		if (!parent.getChildren().contains(this)) {
			parent.addChild(this);
		}
	}

	public void removeParent(Task parent) {
		this.parents.remove(parent);
		if (parent.getChildren().contains(this)) {
			parent.removeChild(this);
		}
	}

	public void replaceByTask(Task newTask) {
		// Remember parents
		final Set<Task> parents = new HashSet<>(this.getParents());

		// Adapt new task
		for (final Task parent : parents) {
			// Do not use newTask.addParent(parent) here. It will append instead of replace the child for this parent.
			if (!newTask.getParents().contains(parent)) {
				newTask.getParents().add(parent);
			}
			// Replacing child - not appending
			final Set<Task> newChildren = new LinkedHashSet<>();
			for (final Task child : parent.getChildren()) {
				if (child == this) {
					newChildren.add(newTask);
				} else {
					newChildren.add(child);
				}
			}
			parent.children = newChildren;
		}
		for (final Task leaf : newTask.getLeafs()) {
			for (final Task child : this.getChildren()) {
				leaf.addChild(child);
			}
		}

		// Remove old task
		for (final Task parent : parents) {
			parent.removeChild(this);
		}
	}

	public String getHashableString() {
		if (isReady()) {
			if (this.tool != null) {
				return getRunCommand(true, false);
			}
		} else {
			Log.msg("Cannot generate hash for task since it is not in ready state.", Log.NORMAL);
		}
		return null;
	}

	public String getRunCommand() {
		return getRunCommand(false, true);
	}

	public String getRunCommand(boolean useHashes, boolean reportMissingVariables) {
		if (this.tool != null && this.tool.getExecute() != null) {
			String runCmd;
			if (!this.tool.isExternal()) {
				// Internal
				if (this.tool instanceof DefaultTool) {
					final StringBuilder sb = new StringBuilder(this.tool.getExecute().getRun() + " with { ");
					final Set<String> allVariables = this.taskInfo.getAllSetVariableNames();
					boolean first = true;
					for (final String variable : allVariables) {
						if (!first) {
							sb.append(", ");
						} else {
							first = false;
						}
						sb.append(variable.replace("%", "#PERCENTAGE#") + " = '" + variable + "'");
					}
					sb.append(" }");
					runCmd = Helper.replaceVariables(sb.toString(), this, this.parent.getParent().getGlobalVariables(),
							useHashes);
					if (reportMissingVariables) {
						Helper.reportMissingVariables(runCmd, this);
					}
					runCmd = runCmd.replace("#PERCENTAGE#", "%");
				} else {
					runCmd = Helper.replaceVariables(this.tool.getExecute().getRun(), this,
							this.parent.getParent().getGlobalVariables(), useHashes);
					if (reportMissingVariables) {
						Helper.reportMissingVariables(runCmd, this);
					}
				}
				return runCmd;
			} else {
				// External
				String questionReference = null;
				final Question question = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(this);
				if (question instanceof DefaultQuestion) {
					questionReference = Helper.typeToSoi(((DefaultQuestion) question).getSubjectOfInterest());
				} else if (question instanceof OperatorQuestion) {
					questionReference = ((OperatorQuestion) question).getOperator();
				}
				final StringBuilder sb = new StringBuilder(Helper.getQualifiedName(this.tool) + ": "
						+ (questionReference != null ? questionReference + " " : "") + "@ "
						+ this.tool.getExecute().getUrl() + " with { ");
				boolean first = true;
				for (final String variable : this.taskInfo.getAllSetVariableNames()) {
					if (!first) {
						sb.append(", ");
					} else {
						first = false;
					}
					sb.append(variable.replace("%", "#PERCENTAGE#") + " = '" + variable + "'");
				}
				sb.append(" }");
				if (!(this instanceof PreprocessorTask)) {
					sb.append(" and query: ").append(Helper.replaceDoubleSpaces(
							Helper.replaceAllWhiteSpaceChars(Helper.getExternalQuery(this), true)));
				}
				runCmd = Helper.replaceVariables(sb.toString(), this, this.parent.getParent().getGlobalVariables(),
						useHashes);
				if (reportMissingVariables) {
					Helper.reportMissingVariables(runCmd, this);
				}
				runCmd = runCmd.replace("#PERCENTAGE#", "%");
				return runCmd;
			}
		}
		return null;
	}

	public String getRunOnEntry() {
		return getRunOnEntry(false);
	}

	public String getRunOnEntry(boolean useHashes) {
		if (this.tool != null && this.tool.getRunOnEntry() != null) {
			return Helper.replaceVariables(this.tool.getRunOnEntry(), this,
					this.parent.getParent().getGlobalVariables(), useHashes);
		}
		return null;
	}

	public String getRunOnExit() {
		return getRunOnExit(false);
	}

	public String getRunOnExit(boolean useHashes) {
		if (this.tool != null && this.tool.getRunOnExit() != null) {
			return Helper.replaceVariables(this.tool.getRunOnExit(), this, this.parent.getParent().getGlobalVariables(),
					useHashes);
		}
		return null;
	}

	public String getRunOnSuccess() {
		return getRunOnSuccess(false);
	}

	public String getRunOnSuccess(boolean useHashes) {
		if (this.tool != null && this.tool.getRunOnSuccess() != null) {
			return Helper.replaceVariables(this.tool.getRunOnSuccess(), this,
					this.parent.getParent().getGlobalVariables(), useHashes);
		}
		return null;
	}

	public String getRunOnFail() {
		return getRunOnFail(false);
	}

	public String getRunOnFail(boolean useHashes) {
		if (this.tool != null && this.tool.getRunOnFail() != null) {
			return Helper.replaceVariables(this.tool.getRunOnFail(), this, this.parent.getParent().getGlobalVariables(),
					useHashes);
		}
		return null;
	}

	public String getRunOnAbort() {
		return getRunOnAbort(false);
	}

	public String getRunOnAbort(boolean useHashes) {
		if (this.tool != null && this.tool.getRunOnAbort() != null) {
			return Helper.replaceVariables(this.tool.getRunOnAbort(), this,
					this.parent.getParent().getGlobalVariables(), useHashes);
		}
		return null;
	}

	public File getRunInPath() {
		return getRunInPath(false);
	}

	public File getRunInPath(boolean useHashes) {
		if (this.tool != null) {
			if (this.tool.getPath() != null) {
				return new File(Helper.replaceVariables(this.tool.getPath(), this,
						this.parent.getParent().getGlobalVariables(), useHashes));
			}
		}
		return null;
	}

	public TaskCreator getParent() {
		return this.parent;
	}

	public Tool getTool() {
		return this.tool;
	}

	public TaskInfo getTaskInfo() {
		return this.taskInfo;
	}

	public void setTool(Tool tool) {
		this.tool = tool;
	}

	public TaskAnswer getTaskAnswer() {
		return this.taskAnswer;
	}

	public void setTaskAnswer(TaskAnswer taskAnswer) {
		this.taskAnswer = taskAnswer;
	}

	public Set<Tool> getToolsFailed() {
		if (this.toolsFailed == null) {
			this.toolsFailed = new HashSet<>();
		}
		return this.toolsFailed;
	}

	public Tool[] getToolsFailedAsArray() {
		return this.toolsFailed.toArray(new Tool[0]);
	}

	@Override
	public String toString() {
		final List<String> lines = new LinkedList<>();
		toString(0, lines, false, null);
		final StringBuilder sb = new StringBuilder(
				"Execution Graph (W: waiting, R: ready, P: in progress, F: failed, D: done):\n");
		for (final String line : lines) {
			sb.append(line).append('\n');
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	private String toString(int level, List<String> lines, boolean hasNextChild, String lastLine) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < level; i++) {
			if (i == level - 1) {
				if (hasNextChild) {
					sb.append("  |--");
				} else {
					sb.append("  \\--");
				}
			} else {
				sb.append("     ");
			}
		}
		sb.append(
				"- ").append(
						this.isReady()
								? this.getTaskAnswer().isAnswered() ? "D"
										: !this.getToolsFailed().isEmpty() ? "F"
												: this.parent.getParent().getTaskScheduler().inProgress(this) ? "P"
														: "R"
								: "W")
				.append(": ").append(this.tool != null ? this.tool.getName() + " (" + this.tool.getVersion() + ")"
						: this instanceof RootTask ? "Root" : "Placeholder");
		if (lastLine != null && lastLine.contains("|")) {
			for (int i = 0; i < lastLine.length(); i++) {
				final char c = lastLine.charAt(i);
				if (c == '|') {
					sb.replace(i, i + 1, "|");
				}
			}
		}
		lines.add(sb.toString());
		for (final Iterator<Task> i = this.getChildren().iterator(); i.hasNext();) {
			final Task child = i.next();
			child.toString(level + 1, lines, i.hasNext(), sb.toString());
		}
		return sb.toString();
	}

	public String getTitle() {
		if (this.getTool() != null) {
			return this.getTool().getName().replaceFirst(" for ", "\nfor ") + "\n(" + this.getTool().getVersion() + ")";
		} else if (this instanceof RootTask) {
			return "Root";
		} else if (this instanceof PlaceholderTask) {
			return "Placeholder";
		} else {
			return "Incomplete task (No tool assigned)";
		}
	}

	public String getStatus() {
		if (this instanceof PlaceholderTask) {
			return STATUS_TEXT_PLACEHODLER;
		} else if (getTaskAnswer().isAnswered()) {
			return STATUS_TEXT_DONE;
		} else if (!getToolsFailed().isEmpty()) {
			return STATUS_TEXT_FAILED;
		} else if (TaskTreeViewer.getTaskTreeSnapshot().getRunningTasks().contains(this)) {
			return STATUS_TEXT_RUNNING;
		} else if (isReady()) {
			return STATUS_TEXT_READY;
		} else {
			return STATUS_TEXT_UNKNOWN;
		}
	}
}