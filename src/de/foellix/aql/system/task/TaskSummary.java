package de.foellix.aql.system.task;

import java.io.File;
import java.util.Collections;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.config.Tool;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.defaulttools.DefaultTool;
import de.foellix.aql.system.storage.Storage;
import de.foellix.aql.ui.gui.GUI;
import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

public class TaskSummary {
	private String status;
	private String title;
	private String tool;
	private String failedTools;
	private String variables;
	private String execution;
	private String query;
	private String executionGraph;
	private String answer;

	public TaskSummary(Task task) {
		boolean incomplete = false;

		// Status
		try {
			this.status = "Status: " + task.getStatus();
		} catch (final Exception e) {
			incomplete = true;
		}

		// Title
		try {
			this.title = task.getTitle().replace("\n", " ");
			if (this.title.equals("Root")) {
				if (task.getChildren().size() == 1) {
					this.title = "Query";
				} else {
					this.title = "Queries";
				}
			}
		} catch (final Exception e) {
			incomplete = true;
		}

		// Tool
		try {
			if (task.getTool() != null) {
				this.tool = "Tool: " + task.getTool().getName() + " (" + task.getTool().getVersion() + ")";
			}
		} catch (final Exception e) {
			incomplete = true;
		}

		// Failed tools
		try {
			if (!(task instanceof RootTask)) {
				final StringBuilder sb = new StringBuilder("Failed Tools: ");
				if (task.getToolsFailed().isEmpty()) {
					sb.append("-");
				} else {
					boolean first = true;
					for (final Tool tool : task.getToolsFailed()) {
						if (first) {
							first = false;
						} else {
							sb.append(',');
						}
						sb.append(tool.getName() + " (" + tool.getVersion() + ")");
					}
				}
				this.failedTools = sb.toString();
			}
		} catch (final Exception e) {
			incomplete = true;
		}

		// Taskinfo
		try {
			if (task.getTaskInfo() != null) {
				this.variables = task.getTaskInfo().toString();
			}
		} catch (final Exception e) {
			incomplete = true;
		}

		// Execution
		try {
			if (task.getTool() != null && !(task.getTool() instanceof DefaultTool)) {
				final StringBuilder sb = new StringBuilder();
				if (!task.getTool().isExternal()) {
					sb.append("Run command: " + task.getTool().getExecute().getRun());
					final String runCmd = task.getRunCommand();
					sb.append("\nRun command (variables resolved): " + runCmd);
					String missingVars = Helper.reportMissingVariables(runCmd, task);
					if (missingVars == null) {
						missingVars = "-";
					}
					sb.append("\nMissing variables: " + missingVars);
				} else {
					if (!(task instanceof PreprocessorTask)) {
						sb.append("External query: " + Helper.getExternalQuery(task));
						final List<File> attachedFiles = Helper.getExternalAppFiles(task);
						attachedFiles.addAll(Helper.getExternalQueryFiles(task));
						Collections.sort(attachedFiles);
						sb.append("\nAttached files: " + attachedFiles);
					} else {
						sb.append("Preprocessor URL: " + task.getTool().getExecute().getUrl());
						sb.append("\nAttached files: " + Helper.getPreprocessorFiles(task));
					}
				}
				this.execution = sb.toString();
			}
		} catch (final Exception e) {
			incomplete = true;
		}

		// Query
		try {
			if (Storage.getInstance().getData().getQuestionFromQuestionTaskMap(task, true) != null) {
				final String temp = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(task, true)
						.toString();
				this.query = "Partial Query:" + (temp.contains("\n") ? "\n" : " ") + temp;
			}
		} catch (final Exception e) {
			incomplete = true;
		}

		// Execution Graph
		try {
			this.executionGraph = task.toString();
		} catch (final Exception e) {
			incomplete = true;
		}

		// Answer
		try {
			if (task.getTaskAnswer() != null && task.getTaskAnswer().getAnswerFile() != null) {
				this.answer = "Answer file: " + task.getTaskAnswer().getAnswerFile().getAbsolutePath();
			}
		} catch (final Exception e) {
			incomplete = true;
		}

		if (incomplete) {
			Log.msg("Task summary incomplete!\n" + getSummary(true), Log.DEBUG_DETAILED);
		}
	}

	public String getStatus() {
		return this.status;
	}

	public String getTitle() {
		return this.title;
	}

	public String getTool() {
		return this.tool;
	}

	public String getVariables() {
		return this.variables;
	}

	public String getExecution() {
		return this.execution;
	}

	public String getQuery() {
		return this.query;
	}

	public String getAnswer() {
		return this.answer;
	}

	public String getSummary(boolean withTitle) {
		final StringBuilder sb = new StringBuilder();

		// Title
		if (withTitle) {
			sb.append("*** " + this.title + " ***\n");
		}

		// Status
		sb.append("- " + this.status);
		sb.append("\n\n");

		// Tool
		if (this.tool != null) {
			sb.append("- " + this.tool);
			sb.append("\n\n");
		}

		// Failed tools
		if (this.failedTools != null) {
			sb.append("- " + this.failedTools);
			sb.append("\n\n");
		}

		// Taskinfo
		if (this.variables != null) {
			sb.append("- " + this.variables);
			sb.append("\n\n");
		}

		// Execution
		if (this.execution != null) {
			sb.append("- " + this.execution.replace("\n", "\n\n- "));
			sb.append("\n\n");
		}

		// Query
		if (this.query != null) {
			sb.append("- " + this.query);
			sb.append("\n\n");
		}

		// Execution Graph
		if (this.executionGraph != null) {
			sb.append("- " + this.executionGraph.replace("\n", "\n\t"));
			sb.append("\n\n");
		}

		// Answer
		if (this.answer != null) {
			sb.append("- " + this.answer);
			sb.append("\n\n");
		}

		return sb.toString().substring(0, sb.toString().length() - 2);
	}

	public void showInfoDialog() {
		final BorderPane pane = new BorderPane();
		pane.setPrefHeight(450);
		pane.setPrefWidth(650);
		pane.setPadding(new Insets(10));
		final TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setWrapText(true);
		textArea.setText(getSummary(false));
		textArea.setFont(Font.font("Consolas", 14));
		pane.setCenter(textArea);
		GUI.alert(false, "Details", getTitle(), pane);
	}
}