package de.foellix.aql.config.wizard;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

import de.foellix.aql.Log;
import de.foellix.aql.config.Config;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.config.Converters;
import de.foellix.aql.config.Operators;
import de.foellix.aql.config.Preprocessors;
import de.foellix.aql.config.Tool;
import de.foellix.aql.config.Tools;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.AQLSystem;
import de.foellix.aql.ui.gui.GUI;
import de.foellix.aql.ui.gui.IGUI;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ConfigWizard extends Stage implements IGUI {
	private final Stage parent;
	private boolean started = false;

	private final Overview overview;
	private final EditorXML editor;
	private final Menubar menubar;

	private final FileChooser openDialogFile, saveDialog;

	private Config currentConfig = null;
	private File currentConfigFile = null;

	private int newToolCounter = 0;

	public ConfigWizard(Stage parent, final File configFile, boolean showImmediately) {
		super();

		this.parent = parent;

		if (configFile.exists()) {
			this.currentConfig = ConfigHandler.parseXML(configFile);
		} else {
			this.currentConfig = new Config();
		}
		this.currentConfigFile = configFile;

		adjustTitle();
		getIcons()
				.add(new Image(new File("data/gui/images/editor_icon_16.png").toURI().toString(), 16, 16, false, true));
		getIcons()
				.add(new Image(new File("data/gui/images/editor_icon_32.png").toURI().toString(), 32, 32, false, true));
		getIcons()
				.add(new Image(new File("data/gui/images/editor_icon_64.png").toURI().toString(), 64, 64, false, true));

		this.overview = new Overview(this);
		this.editor = new EditorXML(this);

		final BorderPane root = new BorderPane();

		final TabPane tabPane = new TabPane();
		tabPane.setSide(Side.BOTTOM);
		final Tab tabEditor = new Tab("Overview");
		tabEditor.setOnSelectionChanged(new EventHandler<>() {
			@Override
			public void handle(final Event event) {
				if (ConfigWizard.this.started && ((Tab) event.getSource()).isSelected()) {
					sync(false);
					ConfigWizard.this.menubar.activate(true);
				}
			}
		});
		tabEditor.setContent(this.overview);
		tabEditor.setClosable(false);

		final Tab tabViewer = new Tab("XML");
		tabViewer.setOnSelectionChanged(new EventHandler<>() {
			@Override
			public void handle(final Event event) {
				if (ConfigWizard.this.started && ((Tab) event.getSource()).isSelected()) {
					sync(true);
					ConfigWizard.this.menubar.activate(false);
				}
			}
		});
		tabViewer.setContent(this.editor);
		tabViewer.setClosable(false);

		tabPane.getTabs().addAll(tabEditor, tabViewer);

		this.menubar = new Menubar(this);
		root.setTop(this.menubar);
		root.setCenter(tabPane);
		final Scene scene = new Scene(root, 1200, parent.getScene().getHeight() - 32);
		scene.getStylesheets().add(new File("data/gui/style.css").toURI().toString());
		scene.getStylesheets().add(new File("data/gui/highlight.css").toURI().toString());
		setScene(scene);

		// Dialogs
		this.openDialogFile = new FileChooser();
		this.saveDialog = new FileChooser();
		final FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("*.* All files", "*.*");
		final FileChooser.ExtensionFilter xmlFilter = new FileChooser.ExtensionFilter("*.xml Config File", "*.xml");
		this.openDialogFile.getExtensionFilters().addAll(allFilter, xmlFilter);
		this.openDialogFile.setSelectedExtensionFilter(xmlFilter);
		this.saveDialog.getExtensionFilters().addAll(allFilter, xmlFilter);
		this.saveDialog.setSelectedExtensionFilter(xmlFilter);

		// Show after parent
		if (showImmediately || parent.isShowing()) {
			this.show();
		} else {
			new Thread(() -> {
				while (!parent.isShowing()) {
					try {
						Thread.sleep(500);
					} catch (final InterruptedException e) {
						// do nothing
					}
				}
				Platform.runLater(() -> {
					this.show();
				});
			}).start();
		}

		// Initialize
		this.started = true;
	}

	@Override
	public void newFile() {
		this.currentConfig = new Config();
		sync();
		this.overview.clear();
	}

	@Override
	public void open() {
		final Alert alert = new Alert(AlertType.CONFIRMATION);
		final Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons()
				.add(new Image(new File("data/gui/images/icon_16.png").toURI().toString(), 16, 16, false, true));
		alertStage.getIcons()
				.add(new Image(new File("data/gui/images/icon_32.png").toURI().toString(), 32, 32, false, true));
		alertStage.getIcons()
				.add(new Image(new File("data/gui/images/icon_64.png").toURI().toString(), 64, 64, false, true));
		alert.setTitle("Open");
		alert.setHeaderText("Opening configuration!");
		alert.setContentText("Get configuration from:");

		final ButtonType[] btnType = new ButtonType[4];
		btnType[0] = new ButtonType("Local File");
		btnType[1] = new ButtonType("Online File");
		btnType[2] = new ButtonType("AQL-WebService");
		btnType[3] = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(btnType);

		final Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == btnType[0]) {
			openFile();
		} else if (result.get() == btnType[1]) {
			openURL();
		} else if (result.get() == btnType[2]) {
			openWebService();
		} else {
			return;
		}
	}

	public void openFile(File configFile) {
		if (configFile != null) {
			this.currentConfigFile = configFile;
			this.currentConfig = ConfigHandler.parseXML(this.currentConfigFile);
			this.editor.setContent(ConfigHandler.toXML(this.currentConfig));

			sync();
			this.overview.clear();
		}
	}

	public void openFile() {
		if (this.currentConfigFile != null) {
			this.openDialogFile.setInitialDirectory(this.currentConfigFile.getParentFile());
		}
		openFile(this.openDialogFile.showOpenDialog(this));
	}

	private void openURL() {
		final File tempConfigFile = FileHelper.getTempFile(FileHelper.FILE_ENDING_XML);

		final TextInputDialog alert = new TextInputDialog("https://.../config.xml");
		final Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons()
				.add(new Image(new File("data/gui/images/icon_16.png").toURI().toString(), 16, 16, false, true));
		alertStage.getIcons()
				.add(new Image(new File("data/gui/images/icon_32.png").toURI().toString(), 32, 32, false, true));
		alertStage.getIcons()
				.add(new Image(new File("data/gui/images/icon_64.png").toURI().toString(), 64, 64, false, true));
		alert.setTitle("Open URL");
		alert.setHeaderText("Please specify the URL of the configuration (.xml) file.");
		alert.setContentText("URL:");
		final Optional<String> result = alert.showAndWait();
		if (result.isPresent()) {
			FileHelper.downloadFile(result.get(), tempConfigFile);
			openFile(tempConfigFile);
			sync();
			this.overview.clear();
		} else {
			Log.warning("Could not get configuration from online file!");
		}
	}

	private void openWebService() {
		final File tempConfigFile = FileHelper.getTempFile(FileHelper.FILE_ENDING_XML);

		final Dialog<String> alert = new Dialog<>();
		final Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons()
				.add(new Image(new File("data/gui/images/icon_16.png").toURI().toString(), 16, 16, false, true));
		alertStage.getIcons()
				.add(new Image(new File("data/gui/images/icon_32.png").toURI().toString(), 32, 32, false, true));
		alertStage.getIcons()
				.add(new Image(new File("data/gui/images/icon_64.png").toURI().toString(), 64, 64, false, true));
		alert.setGraphic(new ImageView(new Image(new File("data/gui/images/icon_64.png").toURI().toString())));
		alert.setTitle("AQL-WebSerivce Credentials");
		alert.setHeaderText("Please provide username and password now!");
		alert.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		final GridPane grid = new GridPane();
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 10, 10, 10));
		final TextField url = new TextField();
		final TextField username = new TextField();
		final PasswordField password = new PasswordField();
		grid.add(new Label("URL:"), 0, 0);
		grid.add(url, 1, 0);
		grid.add(new Label("Username:"), 0, 1);
		grid.add(username, 1, 1);
		grid.add(new Label("Password:"), 0, 2);
		grid.add(password, 1, 2);
		final ColumnConstraints column1 = new ColumnConstraints();
		column1.setPercentWidth(20);
		final ColumnConstraints column2 = new ColumnConstraints();
		column2.setPercentWidth(80);
		grid.getColumnConstraints().clear();
		grid.getColumnConstraints().addAll(column1, column2);
		alert.getDialogPane().setContent(grid);
		alert.setResultConverter(dialogButton -> {
			if (dialogButton == ButtonType.OK && url.getText() != null && !url.getText().isBlank()
					&& username.getText() != null && !username.getText().isBlank() && password.getText() != null) {
				return url.getText() + ", " + username.getText() + ", " + password.getText();
			}
			return null;
		});

		Platform.runLater(() -> url.requestFocus());

		final Optional<String> result = alert.showAndWait();
		if (result.isPresent()) {
			String[] parts = result.get().replace(", ", ",").split(",");
			if (parts.length < 3) {
				parts = new String[] { parts[0], parts[1], "" };
			}
			if (FileHelper.getConfigFromWebService(parts[0], parts[1], parts[2], tempConfigFile)) {
				ConfigHandler.updateConfig(tempConfigFile, true, Helper.getURL(parts[0]), parts[1]);
				openFile(tempConfigFile);
				sync();
				this.overview.clear();
			}
		} else {
			Log.warning("Could not get configuration from WebService!");
		}
	}

	@Override
	public void saveAs() {
		if (this.currentConfigFile != null) {
			this.saveDialog.setInitialDirectory(this.currentConfigFile.getParentFile());
		}
		this.currentConfigFile = this.saveDialog.showSaveDialog(this);
		if (this.currentConfigFile != null) {
			save();
		}
	}

	@Override
	public void save() {
		sync(true);
		try {
			Files.write(this.currentConfigFile.toPath(), this.editor.getContent().getBytes());
		} catch (final IOException e) {
			Log.msg("Could not write file: " + this.currentConfigFile.getAbsolutePath() + " (" + e.getMessage() + ")",
					Log.ERROR);
		}
	}

	@Override
	public void exit() {
		if (GUI.options.getShowConfigWizard()) {
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
	public AQLSystem getSystem() {
		return null;
	}

	public void undo() {
		this.editor.undo();
	}

	public void redo() {
		this.editor.redo();
	}

	public void autoformat() {
		sync(true);
	}

	public void addTool() {
		final Alert alert = new Alert(AlertType.CONFIRMATION);
		final Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons()
				.add(new Image(new File("data/gui/images/icon_16.png").toURI().toString(), 16, 16, false, true));
		alertStage.getIcons()
				.add(new Image(new File("data/gui/images/icon_32.png").toURI().toString(), 32, 32, false, true));
		alertStage.getIcons()
				.add(new Image(new File("data/gui/images/icon_64.png").toURI().toString(), 64, 64, false, true));
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

		sync();
	}

	public Config getConfig() {
		return this.currentConfig;
	}

	public void sync() {
		sync(true);
		sync(false);
	}

	public void sync(boolean overviewActiveBefore) {
		if (overviewActiveBefore) {
			// Sync EditorXML
			this.editor.setContent(ConfigHandler.toXML(this.currentConfig));
		} else {
			// Sync EditorOverview
			this.currentConfig = ConfigHandler.parseXML(this.editor.getContent());
			this.overview.sync();
			this.overview.clear();
		}

		// Sync menubar
		this.menubar.sync();

		// Adjust title
		adjustTitle();
	}

	private void adjustTitle() {
		Platform.runLater(() -> {
			if (this.currentConfig == null) {
				setTitle("AQL-ConfigWizard");
			} else {
				setTitle("AQL-ConfigWizard (" + this.currentConfigFile.getAbsolutePath() + ")");
			}
		});
	}

	public void continueWithCurrentConfig() {
		ConfigHandler.getInstance().setConfig(this.currentConfigFile);
		this.hide();
		if (!this.parent.isShowing()) {
			this.parent.show();
		}
	}
}