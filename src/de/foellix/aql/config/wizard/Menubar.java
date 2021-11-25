package de.foellix.aql.config.wizard;

import de.foellix.aql.ui.gui.FileChooserUIElement;
import de.foellix.aql.ui.gui.FontAwesome;
import de.foellix.aql.ui.gui.MenuFile;
import de.foellix.aql.ui.gui.MenuHelp;
import de.foellix.aql.ui.gui.StringConstants;
import de.foellix.aql.ui.gui.ToolsetFile;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
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

public class Menubar extends VBox {
	private final ConfigWizard parent;
	private final FileChooserUIElement maxMemChooser;
	private final FileChooserUIElement fileChooserPlatforms;
	private final FileChooserUIElement fileChooserBuildTools;
	private final Button btnAutoFormat, btnAddTool;
	private final Label labelMaxMem, labelAndroidPlatforms, labelAndroidBuildTools;

	Menubar(final ConfigWizard parent) {
		this.parent = parent;

		final MenuBar menuBar = new MenuBar();

		final Menu menuEdit = new Menu(StringConstants.STR_EDIT);
		final MenuItem menuItemUndo = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_UNDO,
				StringConstants.STR_UNDO);
		menuItemUndo.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
		menuItemUndo.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.undo();
			}
		});
		final MenuItem menuItemRedo = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_REPEAT,
				StringConstants.STR_REDO);
		menuItemRedo.setAccelerator(new KeyCodeCombination(KeyCode.Y, KeyCombination.CONTROL_DOWN));
		menuItemRedo.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.redo();
			}
		});
		final MenuItem menuItemAutoFormat = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_INDENT_RIGHT,
				StringConstants.STR_AUTO_FORMAT);
		menuItemAutoFormat.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.autoformat();
			}
		});
		final MenuItem menuItemAddTool = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_PLUS,
				StringConstants.STR_ADD_TOOL);
		menuItemAddTool.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.addTool();
			}
		});
		final MenuItem menuItemContinue = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_PLAY,
				StringConstants.STR_CONTINUE);
		menuItemContinue.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.continueWithCurrentConfig();
			}
		});
		menuEdit.getItems().addAll(menuItemUndo, menuItemRedo, new SeparatorMenuItem(), menuItemAutoFormat,
				new SeparatorMenuItem(), menuItemAddTool, new SeparatorMenuItem(), menuItemContinue);

		menuBar.getMenus().addAll(new MenuFile(parent), menuEdit, new MenuHelp(null).removeConfigWizard());

		// Toolbar
		final ToolBar toolBar = new ToolBar();

		this.btnAutoFormat = FontAwesome.getInstance().createButton(FontAwesome.ICON_INDENT_RIGHT);
		this.btnAutoFormat.setTooltip(new Tooltip(StringConstants.STR_AUTO_FORMAT));
		this.btnAutoFormat.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.autoformat();
			}
		});
		this.btnAddTool = FontAwesome.getInstance().createButton(FontAwesome.ICON_PLUS);
		this.btnAddTool.setTooltip(new Tooltip(StringConstants.STR_ADD_TOOL));
		this.btnAddTool.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.addTool();
			}
		});
		final HBox spacer = new HBox();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		this.labelMaxMem = new Label(StringConstants.STR_MAX_MEMORY);
		this.maxMemChooser = new FileChooserUIElement(parent.getStage(), null);
		this.maxMemChooser.setNumeric();
		this.maxMemChooser.getTextField().setPrefWidth(50d);
		this.maxMemChooser.getTextField().textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.equals("")) {
				parent.getConfig().setMaxMemory(Integer.parseInt(newValue));
			}
		});
		this.labelAndroidPlatforms = new Label("   " + StringConstants.STR_ANDROID_PLATFORMS);
		this.fileChooserPlatforms = new FileChooserUIElement(parent.getStage(), StringConstants.STR_BROWSE);
		this.fileChooserPlatforms.getTextField().setPrefWidth(200d);
		this.fileChooserPlatforms.getTextField().textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.equals("")) {
				parent.getConfig().setAndroidPlatforms(newValue);
			}
		});
		this.fileChooserPlatforms.setFolder();
		this.labelAndroidBuildTools = new Label("   " + StringConstants.STR_ANDROID_BUILD_TOOLS);
		this.fileChooserBuildTools = new FileChooserUIElement(parent.getStage(), StringConstants.STR_BROWSE);
		this.fileChooserBuildTools.getTextField().setPrefWidth(200d);
		this.fileChooserBuildTools.getTextField().textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.equals("")) {
				parent.getConfig().setAndroidBuildTools(newValue);
			}
		});
		this.fileChooserBuildTools.setFolder();
		final Button btnContinue = FontAwesome.getInstance().createButton(FontAwesome.ICON_PLAY);
		btnContinue.setTooltip(new Tooltip(StringConstants.STR_CONTINUE));
		btnContinue.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.continueWithCurrentConfig();
			}
		});
		FontAwesome.getInstance().setGreen(btnContinue);
		toolBar.getItems().addAll(new ToolsetFile(parent), new Separator(), this.btnAutoFormat, new Separator(),
				this.btnAddTool, spacer, this.labelMaxMem, this.maxMemChooser, this.labelAndroidPlatforms,
				this.fileChooserPlatforms, new Separator(), this.labelAndroidBuildTools, this.fileChooserBuildTools,
				new Separator(), btnContinue);

		activate(true);
		sync();

		this.getChildren().addAll(menuBar, toolBar);
	}

	public void activate(boolean overviewActiveAfterwards) {
		this.btnAutoFormat.setDisable(overviewActiveAfterwards);
		this.btnAddTool.setDisable(!overviewActiveAfterwards);
		this.labelMaxMem.setDisable(!overviewActiveAfterwards);
		this.labelAndroidPlatforms.setDisable(!overviewActiveAfterwards);
		this.labelAndroidBuildTools.setDisable(!overviewActiveAfterwards);
		this.maxMemChooser.setDisable(!overviewActiveAfterwards);
		this.fileChooserPlatforms.setDisable(!overviewActiveAfterwards);
		this.fileChooserBuildTools.setDisable(!overviewActiveAfterwards);
	}

	public void sync() {
		if (this.parent.getConfig() != null) {
			if (this.parent.getConfig().getMaxMemory() > 0) {
				this.maxMemChooser.getTextField().setText(String.valueOf(this.parent.getConfig().getMaxMemory()));
			} else {
				this.maxMemChooser.getTextField().clear();
			}
			if (this.parent.getConfig().getAndroidPlatforms() != null
					&& !this.parent.getConfig().getAndroidPlatforms().isEmpty()) {
				this.fileChooserPlatforms.getTextField().setText(this.parent.getConfig().getAndroidPlatforms());
			} else {
				this.fileChooserPlatforms.getTextField().clear();
			}
			if (this.parent.getConfig().getAndroidBuildTools() != null
					&& !this.parent.getConfig().getAndroidBuildTools().isEmpty()) {
				this.fileChooserBuildTools.getTextField().setText(this.parent.getConfig().getAndroidBuildTools());
			} else {
				this.fileChooserBuildTools.getTextField().clear();
			}
		} else {
			this.maxMemChooser.getTextField().clear();
			this.fileChooserPlatforms.getTextField().clear();
			this.fileChooserBuildTools.getTextField().clear();
		}
	}
}