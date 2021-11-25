package de.foellix.aql.system.task.gui;

import java.io.File;
import java.util.Set;

import de.foellix.aql.Log;
import de.foellix.aql.system.task.Task;
import de.foellix.aql.system.task.TaskSummary;
import de.foellix.aql.ui.gui.Editor;
import de.foellix.aql.ui.gui.GUI;
import de.foellix.jfx.graphs.ActionHandler;
import de.foellix.jfx.graphs.Graph;
import de.foellix.jfx.graphs.GraphDrawer;
import de.foellix.jfx.graphs.Node;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class TaskTreeViewer extends Application {
	private static final ActionHandler TASK_CLICK_HANDLER = new ActionHandler() {
		@Override
		public void handle(Event e, GraphDrawer graphDrawer, Node node) {
			if (node.getObject() != null && node.getObject() instanceof Task) {
				final Task task = (Task) node.getObject();
				final TaskSummary summary = new TaskSummary(task);

				// Popup
				summary.showInfoDialog();

				// Log
				Log.msg(summary.getSummary(true), Log.NORMAL);
			}
		}
	};
	private static final ActionHandler TASK_HOVER_HANDLER = new ActionHandler() {
		@Override
		public void handle(Event e, GraphDrawer graphDrawer, Node node) {
			if (node.getObject() != null && node.getObject() instanceof Task) {
				final Task task = (Task) node.getObject();
				final TaskSummary summary = new TaskSummary(task);

				final Tooltip tooltip = new Tooltip(summary.getSummary(true));
				tooltip.setMaxWidth(800);
				node.getButton().setTooltip(tooltip);
			}
		}
	};

	private static Pane pane;
	private static int counter;
	private static GraphDrawer drawer;
	private static long updating;
	private static Stage stage;
	private static TaskTreeSnapshot taskTreeSnapshot;

	public TaskTreeViewer() {
		pane = null;
		counter = -1;
		drawer = null;
		updating = 0;
	}

	@Override
	public void start(Stage stage) throws Exception {
		TaskTreeViewer.stage = stage;
		getPane();
		final ScrollPane scrollPane = new ScrollPane(pane);
		scrollPane.setPadding(new Insets(10));
		final Scene scene = new Scene(scrollPane, 600, 800);
		scene.getStylesheets().add(new File("data/gui/executionGraph.css").toURI().toString());

		stage.setTitle("AQL-System: Execution Monitor");
		stage.getIcons()
				.add(new Image(new File("data/gui/images/icon_16.png").toURI().toString(), 16, 16, false, true));
		stage.getIcons()
				.add(new Image(new File("data/gui/images/icon_32.png").toURI().toString(), 32, 32, false, true));
		stage.getIcons()
				.add(new Image(new File("data/gui/images/icon_64.png").toURI().toString(), 64, 64, false, true));
		stage.setScene(scene);

		stage.show();
	}

	public static Stage getStage() {
		return stage;
	}

	public static Pane getPane() {
		if (pane == null) {
			pane = new Pane();
			pane.addEventFilter(ScrollEvent.ANY, e -> {
				// Zoom with Mouse
				if (e.isControlDown()) {
					if (e.getDeltaY() < 0) {
						if (pane.getScaleX() > Editor.ZOOM_LOWER_BOUND / 2d) {
							pane.setScaleX(pane.getScaleX() * Editor.ZOOM_FACTOR);
							pane.setScaleY(pane.getScaleY() * Editor.ZOOM_FACTOR);
						} else {
							pane.setScaleX(1d);
							pane.setScaleY(1d);
						}
					} else if (e.getDeltaY() > 0) {
						if (pane.getScaleX() < Editor.ZOOM_UPPER_BOUND / 2d) {
							pane.setScaleX(pane.getScaleX() / Editor.ZOOM_FACTOR);
							pane.setScaleY(pane.getScaleY() / Editor.ZOOM_FACTOR);
						} else {
							pane.setScaleX(1d);
							pane.setScaleY(1d);
						}
					}
				}
			});
		}
		return pane;
	}

	public static TaskTreeSnapshot getTaskTreeSnapshot() {
		return taskTreeSnapshot;
	}

	public static void update(TaskTreeSnapshot taskTreeSnapshot) {
		update(taskTreeSnapshot.getRootTask(), taskTreeSnapshot.getRunningTasks());
	}

	public static void update(Task rootTask, Set<Task> runningTasks) {
		// Graph
		counter = 0;
		final Graph g = new Graph();
		final Node root = g.getRoot();
		if (rootTask.getChildren().size() == 1) {
			root.setValue("Query");
		} else {
			root.setValue("Queries");
		}
		root.setObject(rootTask);
		appendChildren(root, rootTask);

		// Snapshot
		TaskTreeViewer.taskTreeSnapshot = new TaskTreeSnapshot(rootTask, runningTasks);

		// Show
		if (GUI.started || (stage != null && stage.isShowing())) {
			new Thread(() -> {
				final long time = System.currentTimeMillis() - runningTasks.size();
				updating = time;
				try {
					Thread.sleep(250);
				} catch (final Exception e) {
					return;
				}
				if (time == updating || updating == 0) {
					Platform.runLater(() -> {
						if (drawer == null) {
							drawer = new GraphDrawer(g, pane, new AQLSkin());
						} else {
							drawer.setGraph(g);
							drawer.redraw();
						}
					});
				}
			}).start();
		}
	}

	public static String getNodeLabel(Task task) {
		return ++counter + ") " + task.getTitle();
	}

	private static void appendChildren(Node node, Task task) {
		node.setOnClickListener(TASK_CLICK_HANDLER);
		node.setOnHoverListener(TASK_HOVER_HANDLER);
		for (final Task childTask : task.getChildren()) {
			final Node childNode = node.appendChild(getNodeLabel(childTask), childTask);
			appendChildren(childNode, childTask);
		}
	}
}