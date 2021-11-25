package de.foellix.aql.ui.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import de.foellix.aql.Log;
import de.foellix.aql.Properties;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.config.wizard.ConfigWizard;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.system.AQLSystem;
import de.foellix.aql.system.Options;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GUI extends Application implements IGUI {
	private static final int[] DEFAULT_POSITION_AND_SIZE = new int[] { -1, -1 };

	public static Stage stage;

	private AQLSystem aqlSystem;

	public static TabPane tabs;
	public static Tab tabEditor;
	public static Tab tabViewer;
	public static Editor editor;
	public static Viewer viewer;

	private FileChooser openDialog, saveDialog;
	private FileChooser.ExtensionFilter allFilter, aqlFilter, xmlFilter;

	private File currentQuery;
	private File currentAnswer;

	public static boolean viewerActive = false;
	public static boolean started = false;
	public static Options options = new Options();

	private long time;

	@Override
	public void start(final Stage stage) throws Exception {
		// Init
		this.time = java.lang.System.currentTimeMillis();
		init(stage);
		editor.init(this);
		viewer.init(this);
		Log.msg("Initialization took: " + (System.currentTimeMillis() - this.time) + "ms", Log.DEBUG_DETAILED);

		// SplashScreen
		if (SplashScreen.SPLASH_SCREEN.exists() && !options.getNoSplashScreen()) {
			final SplashScreen splashScreen = new SplashScreen(
					Properties.info().ABBREVIATION + " (v. " + Properties.info().VERSION + ")",
					"by " + Properties.info().AUTHOR, Color.WHITE);
			new Thread(() -> {
				try {
					this.time = java.lang.System.currentTimeMillis() - this.time;
					if (this.time < 3000) {
						Thread.sleep(3000 - this.time);
					}
				} catch (final Exception e) {
					// do nothing
				}
				Platform.runLater(() -> {
					splashScreen.setDone(true);
					showConfigWizard();
				});
			}).start();
		} else {
			showConfigWizard();
		}
	}

	private void init(Stage stage) {
		// TODO: (After 2.0.0 release) Check if future javafx versions still require this silencing (last tested with 18-ea+4; Part 2/2)
		Log.setSilence(false);

		// Load GUI settings
		int[] position;
		try {
			position = Arrays.stream(
					Storage.getInstance().getGuiConfigProperty(Storage.PROPERTY_POSITION).replace(" ", "").split(","))
					.mapToInt(Integer::parseInt).toArray();
			if (position.length < 2) {
				position = DEFAULT_POSITION_AND_SIZE;
			}
		} catch (final NumberFormatException | NullPointerException e) {
			position = DEFAULT_POSITION_AND_SIZE;
		}
		int[] size;
		try {
			size = Arrays.stream(
					Storage.getInstance().getGuiConfigProperty(Storage.PROPERTY_SIZE).replace(" ", "").split(","))
					.mapToInt(Integer::parseInt).toArray();
			if (size.length < 2) {
				position = DEFAULT_POSITION_AND_SIZE;
			}
		} catch (final NumberFormatException | NullPointerException e) {
			size = DEFAULT_POSITION_AND_SIZE;
		}

		// Init GUI
		GUI.stage = stage;

		stage.setTitle("AQL-Editor");
		stage.getIcons()
				.add(new Image(new File("data/gui/images/icon_16.png").toURI().toString(), 16, 16, false, true));
		stage.getIcons()
				.add(new Image(new File("data/gui/images/icon_32.png").toURI().toString(), 32, 32, false, true));
		stage.getIcons()
				.add(new Image(new File("data/gui/images/icon_64.png").toURI().toString(), 64, 64, false, true));

		tabs = new TabPane();
		if (position[0] >= 0) {
			stage.setX(position[0]);
		}
		if (position[1] >= 0) {
			stage.setY(position[1]);
		}
		final Scene scene;
		if (size[0] >= 0 && size[1] >= 0) {
			scene = new Scene(tabs, size[0], size[1]);
		} else {
			scene = new Scene(tabs, 1024d, 768d);
		}
		if (Boolean.parseBoolean(Storage.getInstance().getGuiConfigProperty(Storage.PROPERTY_FULLSCREEN))) {
			stage.setMaximized(true);
		}
		scene.getStylesheets().add(new File("data/gui/style.css").toURI().toString());
		scene.getStylesheets().add(new File("data/gui/keywords.css").toURI().toString());
		scene.getStylesheets().add(new File("data/gui/highlight.css").toURI().toString());
		scene.getStylesheets().add(new File("data/gui/executionGraph.css").toURI().toString());
		stage.setScene(scene);

		started = true;

		// Enable logging to GUI
		Log.enableGUIlogging();

		// Init AQL-System
		this.aqlSystem = new AQLSystem(options);

		// Setup GUI
		editor = new Editor();
		viewer = new Viewer();

		tabEditor = new Tab("Editor");
		tabEditor.setOnSelectionChanged(new EventHandler<>() {
			@Override
			public void handle(final Event event) {
				viewerActive = false;
				adjustTitle();
			}
		});
		tabEditor.setGraphic(new ImageView(
				new Image(new File("data/gui/images/editor_icon_32.png").toURI().toString(), 32, 32, false, true)));
		tabEditor.setContent(editor);
		tabEditor.setClosable(false);

		tabViewer = new Tab("Viewer");
		tabViewer.setOnSelectionChanged(new EventHandler<>() {
			@Override
			public void handle(final Event event) {
				viewerActive = true;
				adjustTitle();
			}
		});
		tabViewer.setGraphic(new ImageView(
				new Image(new File("data/gui/images/viewer_icon_32.png").toURI().toString(), 32, 32, false, true)));
		tabViewer.setContent(viewer);
		tabViewer.setClosable(false);

		tabs.getTabs().addAll(tabEditor, tabViewer);

		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(final WindowEvent we) {
				we.consume();
				exit();
			}
		});

		// Open Dialog
		this.openDialog = new FileChooser();
		this.saveDialog = new FileChooser();
		this.allFilter = new FileChooser.ExtensionFilter("*.* All files", "*.*");
		this.aqlFilter = new FileChooser.ExtensionFilter("*.aql AQL-Query", "*.aql");
		this.xmlFilter = new FileChooser.ExtensionFilter("*.xml AQL-Answer", "*.xml");

		// Drag and Drop
		scene.setOnDragOver(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				final Dragboard db = event.getDragboard();
				if (db.hasFiles()) {
					event.acceptTransferModes(TransferMode.COPY);
				} else {
					event.consume();
				}
			}
		});
		scene.setOnDragDropped(new EventHandler<DragEvent>() {
			@Override
			public void handle(DragEvent event) {
				final Dragboard db = event.getDragboard();
				boolean success = false;
				if (db.hasFiles()) {
					success = true;

					if (viewerActive) {
						open(db.getFiles().get(0));
					} else {
						if (db.getFiles().get(0).getName().endsWith(".aql")) {
							open(db.getFiles().get(0));
						} else {
							final StringBuilder sb = new StringBuilder();
							for (int i = 0; i < db.getFiles().size(); i++) {
								final File file = db.getFiles().get(i);
								sb.append((i == 0 ? "" : " ") + file.getAbsolutePath());
							}
							editor.insert(sb.toString());
						}
					}
				}

				event.setDropCompleted(success);
				event.consume();
			}
		});
	}

	private void showConfigWizard() {
		if (!options.getShowConfigWizard()) {
			stage.show();
			ConfigHandler.getInstance().getConfig();
		} else {
			final ConfigWizard cw = new ConfigWizard(stage, ConfigHandler.getInstance().getConfigFile(), true);
			cw.getStage().setOnCloseRequest(event -> {
				System.exit(0);
			});
		}
	}

	@Override
	public void newFile() {
		if (!viewerActive) {
			// Editor
			editor.resetContent();
			this.currentQuery = null;
		} else {
			// Viewer
			viewer.resetContent();
			this.currentAnswer = null;
		}
	}

	@Override
	public void open() {
		this.openDialog.getExtensionFilters().clear();
		if (!viewerActive) {
			// Editor
			this.openDialog.getExtensionFilters().addAll(this.allFilter, this.aqlFilter);
			this.openDialog.setSelectedExtensionFilter(this.aqlFilter);
			if (this.currentQuery != null) {
				this.openDialog.setInitialDirectory(this.currentQuery.getParentFile());
			} else {
				this.openDialog.setInitialDirectory(new File("queries"));
			}
		} else {
			// Viewer
			this.openDialog.getExtensionFilters().addAll(this.allFilter, this.xmlFilter);
			this.openDialog.setSelectedExtensionFilter(this.xmlFilter);
			if (this.currentAnswer != null) {
				this.openDialog.setInitialDirectory(this.currentAnswer.getParentFile());
			} else {
				this.openDialog.setInitialDirectory(FileHelper.getAnswersDirectory());
			}
		}

		open(this.openDialog.showOpenDialog(getStage()));
	}

	public void open(File file) {
		if (file != null) {
			try {
				if (!viewerActive) {
					// Editor
					editor.openFile(file);
					this.currentQuery = file;
					Storage.getInstance().store(file, Storage.TYPE_QUERIES);
				} else {
					// Viewer
					viewer.openFile(file);
					this.currentAnswer = file;
					Storage.getInstance().store(file, Storage.TYPE_ANSWERS);
				}
			} catch (final FileNotFoundException e) {
				Log.msg("File not found: " + file.toString(), Log.ERROR);
			} catch (final Exception e) {
				Log.msg("Error occurred while opening file: " + file.toString() + Log.getExceptionAppendix(e),
						Log.ERROR);
			}
		}

		adjustTitle();
	}

	@Override
	public void saveAs() {
		this.saveDialog.getExtensionFilters().clear();
		if (!viewerActive) {
			// Editor
			this.saveDialog.getExtensionFilters().addAll(this.allFilter, this.aqlFilter);
			this.saveDialog.setSelectedExtensionFilter(this.aqlFilter);
			if (this.currentQuery != null) {
				this.saveDialog.setInitialDirectory(this.currentQuery.getParentFile());
			} else {
				this.saveDialog.setInitialDirectory(new File("queries"));
			}
			this.currentQuery = this.saveDialog.showSaveDialog(getStage());
			if (this.currentQuery != null) {
				save();
			}
		} else {
			// Viewer
			this.saveDialog.getExtensionFilters().addAll(this.allFilter, this.xmlFilter);
			this.saveDialog.setSelectedExtensionFilter(this.xmlFilter);
			if (this.currentAnswer != null) {
				this.saveDialog.setInitialDirectory(this.currentAnswer.getParentFile());
			} else {
				this.saveDialog.setInitialDirectory(FileHelper.getAnswersDirectory());
			}
			this.currentAnswer = this.saveDialog.showSaveDialog(getStage());
			if (this.currentAnswer != null) {
				save();
			}
		}
	}

	@Override
	public void save() {
		if (!viewerActive) {
			// Editor
			if (this.currentQuery == null) {
				saveAs();
			} else {
				save(this.currentQuery);
			}
		} else {
			// Viewer
			if (this.currentAnswer == null) {
				saveAs();
			} else {
				save(this.currentAnswer);
			}
		}
	}

	private void save(final File file) {
		if (!viewerActive) {
			// Editor
			try {
				file.delete();
				Files.write(file.toPath(), editor.getContent().getBytes());
			} catch (final IOException e) {
				Log.msg("Could not write file: " + file.getAbsolutePath() + " (" + e.getMessage() + ")", Log.ERROR);
			}
			Storage.getInstance().store(file, Storage.TYPE_QUERIES);
		} else {
			// Viewer
			Answer temp = AnswerHandler.parseXML(viewer.viewerXML.getContent());
			if (temp == null) {
				temp = new Answer();
			}
			AnswerHandler.createXML(temp, file);
			Storage.getInstance().store(file, Storage.TYPE_ANSWERS);
		}
		adjustTitle();
	}

	@Override
	public void exit() {
		Storage.getInstance().setGuiConfigProperty(Storage.PROPERTY_FULLSCREEN, String.valueOf(stage.isMaximized()));
		if (!stage.isMaximized()) {
			Storage.getInstance().setGuiConfigProperty(Storage.PROPERTY_POSITION,
					(int) stage.getX() + ", " + (int) stage.getY());
			Storage.getInstance().setGuiConfigProperty(Storage.PROPERTY_SIZE,
					(int) stage.getScene().getWidth() + ", " + (int) stage.getScene().getHeight());
		}
		new ExitDialog("Exit", "You will exit the " + Properties.info().ABBREVIATION + " now.", "Proceed?");
	}

	private void adjustTitle() {
		Platform.runLater(() -> {
			if (viewerActive) {
				if (this.currentAnswer == null) {
					getStage().setTitle("AQL-Viewer");
				} else {
					getStage().setTitle("AQL-Viewer (" + this.currentAnswer.getAbsolutePath() + ")");
				}
			} else {
				if (this.currentQuery == null) {
					getStage().setTitle("AQL-Editor");
				} else {
					getStage().setTitle("AQL-Editor (" + this.currentQuery.getAbsolutePath() + ")");
				}
			}
		});
	}

	@Override
	public Stage getStage() {
		return GUI.stage;
	}

	@Override
	public AQLSystem getSystem() {
		return this.aqlSystem;
	}

	public static void switchToEditor() {
		tabs.getSelectionModel().select(tabEditor);
	}

	public static void switchToViewer() {
		tabs.getSelectionModel().select(tabViewer);
	}

	public static void ask() {
		editor.ask();
	}

	public static void alert(boolean error, String title, String header, String msg) {
		Platform.runLater(() -> {
			final Alert alert = getAlertTemplate(error, title, header, msg);
			alert.setContentText(msg);
			alert.show();
		});
	}

	public static void alert(boolean error, String title, String header, Pane pane) {
		Platform.runLater(() -> {
			final Alert alert = getAlertTemplate(error, title, header, "");
			alert.getDialogPane().setContent(pane);
			alert.initModality(Modality.NONE);
			alert.show();
		});
	}

	private static Alert getAlertTemplate(boolean error, String title, String header, String msg) {
		final Alert alert = new Alert((error ? AlertType.ERROR : AlertType.INFORMATION));
		final Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons()
				.add(new Image(new File("data/gui/images/icon_16.png").toURI().toString(), 16, 16, false, true));
		alertStage.getIcons()
				.add(new Image(new File("data/gui/images/icon_32.png").toURI().toString(), 32, 32, false, true));
		alertStage.getIcons()
				.add(new Image(new File("data/gui/images/icon_64.png").toURI().toString(), 64, 64, false, true));
		if (header.length() > 100 || msg.length() > 100) {
			alert.getDialogPane().setPrefWidth(600);
		}
		alert.setTitle(title);
		alert.setHeaderText(header);
		return alert;
	}
}