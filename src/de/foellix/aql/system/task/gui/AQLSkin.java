package de.foellix.aql.system.task.gui;

import de.foellix.aql.system.task.PlaceholderTask;
import de.foellix.aql.system.task.Task;
import de.foellix.jfx.graphs.Hasher;
import de.foellix.jfx.graphs.Node;
import de.foellix.jfx.graphs.Options;
import de.foellix.jfx.graphs.style.Style;
import de.foellix.jfx.graphs.style.StyleCondition;

public class AQLSkin extends Options {
	public AQLSkin() {
		super();
		setPadding(10);
		setSameObjectEdgesEnabled(true);
		setHasher(new TaskHasher());

		// Placeholder
		addNodeStyle(new Style("nodePlaceholder", null, new StyleCondition() {
			@Override
			public boolean fulfilled(Node node) {
				if (node.getButton() != null) {
					node.getButton().getStyleClass().clear();
				}
				final Task task = (Task) node.getObject();
				return task instanceof PlaceholderTask;
			}
		}));
		// Done
		addNodeStyle(new Style("nodeDone", null, new StyleCondition() {
			@Override
			public boolean fulfilled(Node node) {
				if (node.getButton() != null) {
					node.getButton().getStyleClass().clear();
				}
				final Task task = (Task) node.getObject();
				return task.getTaskAnswer().isAnswered();
			}
		}));
		// Failed
		addNodeStyle(new Style("nodeFailed", null, new StyleCondition() {
			@Override
			public boolean fulfilled(Node node) {
				if (node.getButton() != null) {
					node.getButton().getStyleClass().clear();
				}
				final Task task = (Task) node.getObject();
				return !task.getToolsFailed().isEmpty();
			}
		}));
		// Running
		addNodeStyle(new Style("nodeRunning", null, new StyleCondition() {
			@Override
			public boolean fulfilled(Node node) {
				if (node.getButton() != null) {
					node.getButton().getStyleClass().clear();
				}
				final Task task = (Task) node.getObject();
				return TaskTreeViewer.getTaskTreeSnapshot().getRunningTasks().contains(task);
			}
		}));
		// Ready
		addNodeStyle(new Style("nodeReady", null, new StyleCondition() {
			@Override
			public boolean fulfilled(Node node) {
				if (node.getButton() != null) {
					node.getButton().getStyleClass().clear();
				}
				final Task task = (Task) node.getObject();
				return task.getToolsFailed().isEmpty() && task.isReady();
			}
		}));
	}

	private class TaskHasher implements Hasher {
		@Override
		public int hashCode(Object obj) {
			if (obj instanceof Task) {
				final Task task = (Task) obj;
				final int prime = 31;
				int result = 1;
				result = prime * result
						+ ((task.getTaskInfo() == null) ? 0 : task.getTaskInfo().toStringNoPID().hashCode());
				result = prime * result + ((task.getTool() == null) ? 0 : task.getTool().hashCode());
				return result;
			}
			return -1;
		}
	}
}