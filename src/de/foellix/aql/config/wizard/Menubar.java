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
	private final FileChooserUIElement fileChooserUIElement;

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
		final SeparatorMenuItem sep1 = new SeparatorMenuItem();
		final MenuItem menuItemAutoFormat = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_INDENT_RIGHT,
				StringConstants.STR_AUTO_FORMAT);
		menuItemAutoFormat.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.autoformat();
			}
		});
		final SeparatorMenuItem sep2 = new SeparatorMenuItem();
		final MenuItem menuItemAddTool = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_PLUS,
				StringConstants.STR_ADD_TOOL);
		menuItemAddTool.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.addTool();
			}
		});
		final SeparatorMenuItem sep3 = new SeparatorMenuItem();
		final MenuItem menuItemContinue = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_PLAY,
				StringConstants.STR_CONTINUE);
		menuItemContinue.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.continueWithCurrentConfig();
			}
		});
		menuEdit.getItems().addAll(menuItemUndo, menuItemRedo, sep1, menuItemAutoFormat, sep2, menuItemAddTool, sep3,
				menuItemContinue);

		menuBar.getMenus().addAll(new MenuFile(parent), menuEdit, new MenuHelp(null).removeConfigWizard());

		// Toolbar
		final ToolBar toolBar = new ToolBar();

		final Separator sep4 = new Separator();
		final Button btnAutoFormat = FontAwesome.getInstance().createButton(FontAwesome.ICON_INDENT_RIGHT);
		btnAutoFormat.setTooltip(new Tooltip(StringConstants.STR_AUTO_FORMAT));
		btnAutoFormat.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.autoformat();
			}
		});
		final Separator sep5 = new Separator();
		final Button btnAddTool = FontAwesome.getInstance().createButton(FontAwesome.ICON_PLUS);
		btnAddTool.setTooltip(new Tooltip(StringConstants.STR_ADD_TOOL));
		btnAddTool.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.addTool();
			}
		});
		FontAwesome.getInstance().setGreen(btnAddTool);
		final HBox spacer = new HBox();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		final Label labelMaxMem = new Label(StringConstants.STR_MAX_MEMORY);
		this.maxMemChooser = new FileChooserUIElement(parent.getStage(), null);
		this.maxMemChooser.setNumeric();
		this.maxMemChooser.getTextField().setPrefWidth(50d);
		this.maxMemChooser.getTextField().textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.equals("")) {
				parent.getConfig().setMaxMemory(Integer.parseInt(newValue));
				parent.syncEditorXML();
			}
		});
		final Label labelAndroidSDK = new Label("   " + StringConstants.STR_ANDROID_PLATFORMS);
		this.fileChooserUIElement = new FileChooserUIElement(parent.getStage(), StringConstants.STR_BROWSE);
		this.fileChooserUIElement.getTextField().setPrefWidth(200d);
		this.fileChooserUIElement.getTextField().textProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue.equals("")) {
				parent.getConfig().setAndroidPlatforms(newValue);
				parent.syncEditorXML();
			}
		});
		this.fileChooserUIElement.setFolder();
		final Separator sep6 = new Separator();
		final Button btnContinue = FontAwesome.getInstance().createButton(FontAwesome.ICON_PLAY);
		btnContinue.setTooltip(new Tooltip(StringConstants.STR_CONTINUE));
		btnContinue.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.continueWithCurrentConfig();
			}
		});
		FontAwesome.getInstance().setGreen(btnContinue);
		toolBar.getItems().addAll(new ToolsetFile(parent), sep4, btnAutoFormat, sep5, btnAddTool, spacer, labelMaxMem,
				this.maxMemChooser, labelAndroidSDK, this.fileChooserUIElement, sep6, btnContinue);
		sync();

		this.getChildren().addAll(menuBar, toolBar);
	}

	public void sync() {
		if (this.parent.getConfig().getMaxMemory() > 0) {
			this.maxMemChooser.getTextField().setText(String.valueOf(this.parent.getConfig().getMaxMemory()));
		}
		if (this.parent.getConfig().getAndroidPlatforms() != null) {
			this.fileChooserUIElement.getTextField().setText(this.parent.getConfig().getAndroidPlatforms());
		}
	}
}