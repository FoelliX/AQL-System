package de.foellix.aql.ui.gui;

import de.foellix.aql.datastructure.Answer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
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
	public static final int MODE_VIEWER_XML = 0;
	public static final int MODE_VIEWER_GRAPH = 1;
	public static final int MODE_VIEWER_WEB = 2;

	private Label[] stats = new Label[8];

	private final Button btnInBrowser;

	MenubarViewer(final Viewer parent) {
		final MenuBar menuBar = new MenuBar();

		final MenuFile menuFile = new MenuFile(parent.getParentGUI(), false);
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
				StringConstants.STR_REFRESH_GRAPH_AND_STATS);
		menuItemRefreshGraph.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.refreshGraph();
			}
		});
		final MenuItem menuItemInBrowser = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_GLOBE,
				StringConstants.STR_OPEN_IN_BROWSER);
		menuItemInBrowser.setAccelerator(new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN));
		menuItemInBrowser.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.inBrowser();
			}
		});
		final CheckMenuItem menuConsiderLinenumbers = FontAwesome.getInstance()
				.createCheckMenuItem(StringConstants.STR_CONSIDER_LINENUMBERS);
		menuConsiderLinenumbers.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.viewerWeb.setConsiderLinenumbers(((CheckMenuItem) event.getSource()).isSelected());
				parent.refreshGraph();
			}
		});
		menuConsiderLinenumbers.setSelected(true);

		menuView.getItems().addAll(menuItemRefreshGraph, menuItemInBrowser, new SeparatorMenuItem(),
				menuConsiderLinenumbers);

		menuBar.getMenus().addAll(menuFile, menuEdit, menuView, new MenuHelp(parent.getParentGUI().getStage()));

		// Toolbar
		final ToolBar toolBar = new ToolBar();

		final Button btnRefreshGraph = FontAwesome.getInstance().createButton(FontAwesome.ICON_REFRESH);
		btnRefreshGraph.setTooltip(new Tooltip(StringConstants.STR_REFRESH_GRAPH_AND_STATS));
		btnRefreshGraph.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.refreshGraph();
			}
		});
		final HBox statsOuterBox = new HBox(10);
		final VBox statsInnerBox1 = new VBox(0);
		final VBox statsInnerBox2 = new VBox(0);
		final VBox statsInnerBox3 = new VBox(0);
		final VBox statsInnerBox4 = new VBox(0);
		this.stats[0] = new Label("Flows: ");
		this.stats[1] = new Label("Permissions: ");
		this.stats[2] = new Label("Intents: ");
		this.stats[3] = new Label("IntentFilters: ");
		this.stats[4] = new Label("IntentSinks: ");
		this.stats[5] = new Label("IntentSources: ");
		this.stats[6] = new Label("Sources: ");
		this.stats[7] = new Label("Sinks: ");
		statsInnerBox1.getChildren().addAll(this.stats[0], this.stats[1]);
		statsInnerBox2.getChildren().addAll(this.stats[2], this.stats[3]);
		statsInnerBox3.getChildren().addAll(this.stats[4], this.stats[5]);
		statsInnerBox4.getChildren().addAll(this.stats[6], this.stats[7]);
		statsOuterBox.getChildren().addAll(statsInnerBox1, statsInnerBox2, statsInnerBox3, statsInnerBox4);
		final HBox spacer = new HBox();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		this.btnInBrowser = FontAwesome.getInstance().createButton(FontAwesome.ICON_GLOBE);
		this.btnInBrowser.setTooltip(new Tooltip(StringConstants.STR_OPEN_IN_BROWSER));
		this.btnInBrowser.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.inBrowser();
			}
		});

		toolBar.getItems().addAll(new ToolsetFile(parent.getParentGUI(), menuFile), new Separator(), btnRefreshGraph,
				new Separator(), statsOuterBox, spacer, this.btnInBrowser);

		if (parent.getParentGUI() instanceof GUI) {
			getChildren().addAll(menuBar, toolBar);
		} else {
			getChildren().addAll(toolBar);
		}
	}

	public void refresh(Answer answer) {
		if (answer != null) {
			if (answer.getFlows() != null) {
				this.stats[0].setText("Flows: " + answer.getFlows().getFlow().size());
			} else {
				this.stats[0].setText("Flows: 0");
			}
			if (answer.getPermissions() != null) {
				this.stats[1].setText("Permissions: " + answer.getPermissions().getPermission().size());
			} else {
				this.stats[1].setText("Permissions: 0");
			}
			if (answer.getIntents() != null) {
				this.stats[2].setText("Intents: " + answer.getIntents().getIntent().size());
			} else {
				this.stats[2].setText("Intents: 0");
			}
			if (answer.getIntentfilters() != null) {
				this.stats[3].setText("IntentFilters: " + answer.getIntentfilters().getIntentfilter().size());
			} else {
				this.stats[3].setText("IntentFilters: 0");
			}
			if (answer.getIntentsinks() != null) {
				this.stats[4].setText("IntentSinks: " + answer.getIntentsinks().getIntentsink().size());
			} else {
				this.stats[4].setText("IntentSinks: 0");
			}
			if (answer.getIntentsources() != null) {
				this.stats[5].setText("IntentSources: " + answer.getIntentsources().getIntentsource().size());
			} else {
				this.stats[5].setText("IntentSources: 0");
			}
			if (answer.getSources() != null) {
				this.stats[6].setText("Sources: " + answer.getSources().getSource().size());
			} else {
				this.stats[6].setText("Sources: 0");
			}
			if (answer.getSinks() != null) {
				this.stats[7].setText("Sinks: " + answer.getSinks().getSink().size());
			} else {
				this.stats[7].setText("Sinks: 0");
			}
		} else {
			this.stats[0].setText("Flows: 0");
			this.stats[1].setText("Permissions: 0");
			this.stats[2].setText("Intents: 0");
			this.stats[3].setText("IntentFilters: 0");
			this.stats[4].setText("IntentSinks: 0");
			this.stats[5].setText("IntentSources: 0");
			this.stats[6].setText("Sources: 0");
			this.stats[7].setText("Sinks: 0");
		}
	}

	public void buttonState(int mode) {
		if (mode == MODE_VIEWER_XML) {
			this.btnInBrowser.setDisable(true);
		} else if (mode == MODE_VIEWER_GRAPH) {
			this.btnInBrowser.setDisable(true);
		} else {
			this.btnInBrowser.setDisable(false);
		}
	}
}
