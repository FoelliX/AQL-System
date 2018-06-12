package de.foellix.aql.ui.gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class MenubarViewer extends VBox {
	MenubarViewer(final Viewer parent) {
		final MenuBar menuBar = new MenuBar();

		final Menu menuEdit = new Menu(StringConstants.STR_EDIT);
		final MenuItem menuItemUndo = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_UNDO,
				StringConstants.STR_UNDO);
		menuItemUndo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
		menuItemUndo.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.viewerXML.undo();
			}
		});
		final MenuItem menuItemRedo = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_REPEAT,
				StringConstants.STR_REDO);
		menuItemRedo.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
		menuItemRedo.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.viewerXML.redo();
			}
		});
		menuEdit.getItems().addAll(menuItemUndo, menuItemRedo);

		final Menu menuView = new Menu(StringConstants.STR_VIEW);
		final MenuItem menuItemRefreshGraph = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_REFRESH,
				StringConstants.STR_REFRESH_GRAPH);
		menuItemRefreshGraph.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.refreshGraph();
			}
		});
		final MenuItem menuItemExportGraph = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_BAR_CHART,
				StringConstants.STR_EXPORT_GRAPH);
		menuItemExportGraph.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				if (parent.getParentGUI() instanceof GUI) {
					((GUI) parent.getParentGUI()).exportGraph();
				}
			}
		});
		final SeparatorMenuItem seperator4 = new SeparatorMenuItem();
		final MenuItem menuItemRotate = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_RETWEET,
				StringConstants.STR_ROTATE_GRAPH);
		menuItemRotate.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
		menuItemRotate.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.rotate();
			}
		});
		final SeparatorMenuItem seperator5 = new SeparatorMenuItem();
		final MenuItem menuItemZoomReset = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_SEARCH,
				StringConstants.STR_ZOOM_RESET);
		menuItemZoomReset.setAccelerator(new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.CONTROL_DOWN));
		menuItemZoomReset.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.zoomReset();
			}
		});
		final MenuItem menuItemZoomIn = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_ZOOM_IN,
				StringConstants.STR_ZOOM_IN);
		menuItemZoomIn.setAccelerator(new KeyCodeCombination(KeyCode.PLUS, KeyCombination.CONTROL_DOWN));
		menuItemZoomIn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.zoomIn();
			}
		});
		final MenuItem menuItemZoomOut = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_ZOOM_OUT,
				StringConstants.STR_ZOOM_OUT);
		menuItemZoomOut.setAccelerator(new KeyCodeCombination(KeyCode.MINUS, KeyCombination.CONTROL_DOWN));
		menuItemZoomOut.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.zoomOut();
			}
		});
		menuView.getItems().addAll(menuItemRefreshGraph, menuItemExportGraph, seperator4, menuItemRotate, seperator5,
				menuItemZoomReset, menuItemZoomIn, menuItemZoomOut);

		menuBar.getMenus().addAll(new MenuFile(parent.getParentGUI()), menuEdit, menuView,
				new MenuHelp(parent.getParentGUI().getStage()));

		// Toolbar
		final ToolBar toolBar = new ToolBar();

		final Separator seperator6 = new Separator();
		final Button btnRefreshGraph = FontAwesome.getInstance().createButton(FontAwesome.ICON_REFRESH);
		btnRefreshGraph.setTooltip(new Tooltip(StringConstants.STR_REFRESH_GRAPH));
		btnRefreshGraph.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.refreshGraph();
			}
		});
		final Button btnExportGraph = FontAwesome.getInstance().createButton(FontAwesome.ICON_BAR_CHART);
		btnExportGraph.setTooltip(new Tooltip(StringConstants.STR_EXPORT_GRAPH));
		btnExportGraph.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				if (parent.getParentGUI() instanceof GUI) {
					((GUI) parent.getParentGUI()).exportGraph();
				}
			}
		});
		final HBox spacer = new HBox();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		final Button btnResetZoom = FontAwesome.getInstance().createButton(FontAwesome.ICON_SEARCH);
		btnResetZoom.setTooltip(new Tooltip(StringConstants.STR_ZOOM_RESET));
		btnResetZoom.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.zoomReset();
			}
		});
		final Button btnRotate = FontAwesome.getInstance().createButton(FontAwesome.ICON_RETWEET);
		btnRotate.setTooltip(new Tooltip(StringConstants.STR_ROTATE_GRAPH));
		btnRotate.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.rotate();
			}
		});
		final Separator seperator7 = new Separator();
		final Button btnZoomIn = FontAwesome.getInstance().createButton(FontAwesome.ICON_ZOOM_IN);
		btnZoomIn.setTooltip(new Tooltip(StringConstants.STR_ZOOM_IN));
		btnZoomIn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.zoomIn();
			}
		});
		final Button btnZoomOut = FontAwesome.getInstance().createButton(FontAwesome.ICON_ZOOM_OUT);
		btnZoomOut.setTooltip(new Tooltip(StringConstants.STR_ZOOM_OUT));
		btnZoomOut.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.zoomOut();
			}
		});

		toolBar.getItems().addAll(new ToolsetFile(parent.getParentGUI()), seperator6, btnRefreshGraph, btnExportGraph,
				spacer, btnRotate, seperator7, btnResetZoom, btnZoomIn, btnZoomOut);

		if (parent.getParentGUI() instanceof GUI) {
			getChildren().addAll(menuBar, toolBar);
		} else {
			toolBar.getItems().remove(seperator6);
			toolBar.getItems().remove(btnExportGraph);
			getChildren().addAll(toolBar);
		}
	}
}
