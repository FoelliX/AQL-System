package de.foellix.aql.config.wizard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

import de.foellix.aql.Log;
import de.foellix.aql.config.Config;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.config.Converters;
import de.foellix.aql.config.Operators;
import de.foellix.aql.config.Preprocessors;
import de.foellix.aql.config.Tool;
import de.foellix.aql.config.Tools;
import de.foellix.aql.system.System;
import de.foellix.aql.ui.gui.GUI;
import de.foellix.aql.ui.gui.IGUI;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ConfigWizard extends Stage implements IGUI {
	private final Stage parent;

	private boolean overviewActive = true;

	private final Overview overview;
	private final EditorXML editor;

	private final DirectoryChooser openDialogFolder;
	private final FileChooser openDialogFile, saveDialog;
	private final File lastDirectory = new File(".");

	private Config currentConfig = null;
	private File currentConfigFile = null;

	private int newToolCounter = 0;

	public ConfigWizard(Stage parent, final File configFile) {
		super();

		this.parent = parent;

		if (configFile.exists()) {
			this.currentConfig = ConfigHandler.parseXML(configFile);
		} else {
			this.currentConfig = new Config();
		}
		this.currentConfigFile = configFile;

		setTitle("AQL-ConfigWizard");
		getIcons().add(new Image("file:data/gui/images/editor_icon_16.png", 16, 16, false, true));
		getIcons().add(new Image("file:data/gui/images/editor_icon_32.png", 32, 32, false, true));
		getIcons().add(new Image("file:data/gui/images/editor_icon_64.png", 64, 64, false, true));

		this.overview = new Overview(this);
		this.editor = new EditorXML(this);

		final BorderPane root = new BorderPane();

		final TabPane tabPane = new TabPane();
		tabPane.setSide(Side.BOTTOM);
		final Tab tabEditor = new Tab("Overview");
		tabEditor.setOnSelectionChanged(new EventHandler<Event>() {
			@Override
			public void handle(final Event event) {
				ConfigWizard.this.overviewActive = false;
				adjustTitle();
			}
		});
		tabEditor.setContent(this.overview);
		tabEditor.setClosable(false);

		final Tab tabViewer = new Tab("XML");
		tabViewer.setOnSelectionChanged(new EventHandler<Event>() {
			@Override
			public void handle(final Event event) {
				ConfigWizard.this.overviewActive = true;
				adjustTitle();
			}
		});
		tabViewer.setContent(this.editor);
		tabViewer.setClosable(false);

		tabPane.getTabs().addAll(tabEditor, tabViewer);

		root.setTop(new Menubar(this));
		root.setCenter(tabPane);
		final Scene scene = new Scene(root, parent.getScene().getWidth() - 32, parent.getScene().getHeight() - 32);
		scene.getStylesheets().add("file:data/gui/style.css");
		scene.getStylesheets().add("file:data/gui/xml_highlighting.css");
		setScene(scene);

		this.show();

		// Dialogs
		this.openDialogFolder = new DirectoryChooser();
		this.openDialogFile = new FileChooser();
		this.saveDialog = new FileChooser();
		final FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("*.* All files", "*.*");
		final FileChooser.ExtensionFilter xmlFilter = new FileChooser.ExtensionFilter("*.xml Config File", "*.xml");
		this.openDialogFile.getExtensionFilters().addAll(allFilter, xmlFilter);
		this.openDialogFile.setSelectedExtensionFilter(xmlFilter);
		this.saveDialog.getExtensionFilters().addAll(allFilter, xmlFilter);
		this.saveDialog.setSelectedExtensionFilter(xmlFilter);
	}

	@Override
	public void newFile() {
		this.currentConfig = new Config();
		syncEditorXML();
		syncOverview();
	}

	@Override
	public void open() {
		open(false);
	}

	public void open(final boolean folder) {
		if (folder) {
			// Folder
			if (this.lastDirectory != null) {
				this.openDialogFolder.setInitialDirectory(this.lastDirectory);
			}
		} else {
			// File
			if (this.lastDirectory != null) {
				this.openDialogFile.setInitialDirectory(this.lastDirectory);
			}
		}

		this.currentConfigFile = this.openDialogFile.showOpenDialog(this);
		if (this.currentConfigFile != null) {
			this.currentConfig = ConfigHandler.parseXML(this.currentConfigFile);
			this.editor.setContent(ConfigHandler.toXML(this.currentConfig));
		}

		adjustTitle();
	}

	@Override
	public void saveAs() {
		if (this.currentConfigFile != null) {
			this.saveDialog.setInitialDirectory(this.currentConfigFile.getParentFile());
		} else if (this.lastDirectory != null) {
			this.saveDialog.setInitialDirectory(this.lastDirectory);
		}
		this.currentConfigFile = this.saveDialog.showSaveDialog(this);
		if (this.currentConfigFile != null) {
			save();
		}
	}

	@Override
	public void save() {
		try {
			this.currentConfigFile.delete();
			Files.write(Paths.get(this.currentConfigFile.toURI()), this.editor.getContent().getBytes(),
					StandardOpenOption.CREATE_NEW);
		} catch (final IOException e) {
			Log.msg("Could not write file: " + this.currentConfigFile.getAbsolutePath() + " (" + e.getMessage() + ")",
					Log.ERROR);
		}

		adjustTitle();
	}

	@Override
	public void exit() {
		if (GUI.showConfigWizard) {
			java.lang.System.exit(0);
		} else {
			this.hide();
		}
	}

	@Override
	public Stage getStage() {
		return this;
	}

	@Override
	public System getSystem() {
		return null;
	}

	public void undo() {
		this.editor.undo();
	}

	public void redo() {
		this.editor.redo();
	}

	public void autoformat() {
		syncOverview();
		syncEditorXML();
	}

	public void addTool() {
		final Alert alert = new Alert(AlertType.CONFIRMATION);
		final Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons().add(new Image("file:data/gui/images/icon_16.png", 16, 16, false, true));
		alertStage.getIcons().add(new Image("file:data/gui/images/icon_32.png", 32, 32, false, true));
		alertStage.getIcons().add(new Image("file:data/gui/images/icon_64.png", 64, 64, false, true));
		alert.setTitle("New Tool");
		alert.setHeaderText("What type of tool you want to add?");
		alert.setContentText("Choose your option!");

		final ButtonType[] btnType = new ButtonType[5];
		btnType[0] = new ButtonType("Analysis Tool");
		btnType[1] = new ButtonType("Preprocessor");
		btnType[2] = new ButtonType("Operator");
		btnType[3] = new ButtonType("Converter");
		btnType[4] = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(btnType);

		final Optional<ButtonType> result = alert.showAndWait();
		final Tool temp = new Tool();
		temp.setName("New Tool #" + ++this.newToolCounter);
		if (result.get() == btnType[0]) {
			if (this.currentConfig.getTools() == null) {
				this.currentConfig.setTools(new Tools());
			}
			this.currentConfig.getTools().getTool().add(temp);
		} else if (result.get() == btnType[1]) {
			if (this.currentConfig.getPreprocessors() == null) {
				this.currentConfig.setPreprocessors(new Preprocessors());
			}
			this.currentConfig.getPreprocessors().getTool().add(temp);
		} else if (result.get() == btnType[2]) {
			if (this.currentConfig.getOperators() == null) {
				this.currentConfig.setOperators(new Operators());
			}
			this.currentConfig.getOperators().getTool().add(temp);
		} else if (result.get() == btnType[3]) {
			if (this.currentConfig.getConverters() == null) {
				this.currentConfig.setConverters(new Converters());
			}
			this.currentConfig.getConverters().getTool().add(temp);
		} else {
			return;
		}

		syncEditorXML();
		syncOverview();
	}

	private void adjustTitle() {
		Platform.runLater(() -> {
			if (this.overviewActive) {
				if (this.currentConfig == null) {
					setTitle("AQL-ConfigWizard");
				} else {
					setTitle("AQL-ConfigWizard (" + this.currentConfigFile.getAbsolutePath() + ")");
				}
			} else {
				if (this.currentConfig == null) {
					setTitle("AQL-ConfigWizard");
				} else {
					setTitle("AQL-ConfigWizard (" + this.currentConfigFile.getAbsolutePath() + ")");
				}
			}
		});
	}

	public Config getConfig() {
		return this.currentConfig;
	}

	public void syncOverview() {
		this.currentConfig = ConfigHandler.parseXML(this.editor.getContent());
		this.overview.sync();
	}

	public void syncEditorXML() {
		this.editor.setContent(ConfigHandler.toXML(this.currentConfig));
	}

	public void continueWithCurrentConfig() {
		ConfigHandler.getInstance().setConfig(this.currentConfigFile);
		this.hide();
		if (!this.parent.isShowing()) {
			this.parent.show();
		}
	}
}
