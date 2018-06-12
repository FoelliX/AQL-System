package de.foellix.aql.ui.gui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import de.foellix.aql.Log;
import de.foellix.aql.Properties;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.config.wizard.ConfigWizard;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.system.System;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class GUI extends Application implements IGUI {
	public static Stage stage;

	public System system;

	public static Editor editor;
	public static Viewer viewer;

	private FileChooser openDialog, saveDialog;
	private FileChooser.ExtensionFilter allFilter, aqlFilter, xmlFilter, pngFilter;

	private File currentQuery;
	private File currentAnswer;
	private File currentExport;

	public static boolean viewerActive = false;
	public static boolean started = false;
	public static boolean alwaysPreferLoading = true;
	public static long timeout = -1;
	public static boolean showConfigWizard = false;

	private long time;

	public GUI() {
		this.system = new System();
		this.system.getScheduler().setAlwaysPreferLoading(alwaysPreferLoading);
		this.system.getScheduler().setTimeout(timeout);
	}

	@Override
	public void start(final Stage stage) throws Exception {
		this.time = java.lang.System.currentTimeMillis();

		this.stage = stage;

		stage.setTitle("AQL-Editor");
		stage.getIcons().add(new Image("file:data/gui/images/icon_16.png", 16, 16, false, true));
		stage.getIcons().add(new Image("file:data/gui/images/icon_32.png", 32, 32, false, true));
		stage.getIcons().add(new Image("file:data/gui/images/icon_64.png", 64, 64, false, true));

		editor = new Editor(this);
		viewer = new Viewer(this);

		final TabPane tabPane = new TabPane();
		final Tab tabEditor = new Tab("Editor");
		tabEditor.setOnSelectionChanged(new EventHandler<Event>() {
			@Override
			public void handle(final Event event) {
				viewerActive = false;
				adjustTitle();
			}
		});
		tabEditor.setGraphic(new ImageView(new Image("file:data/gui/images/editor_icon_32.png", 32, 32, false, true)));
		tabEditor.setContent(editor);
		tabEditor.setClosable(false);

		final Tab tabViewer = new Tab("Viewer");
		tabViewer.setOnSelectionChanged(new EventHandler<Event>() {
			@Override
			public void handle(final Event event) {
				viewerActive = true;
				adjustTitle();
			}
		});
		tabViewer.setGraphic(new ImageView(new Image("file:data/gui/images/viewer_icon_32.png", 32, 32, false, true)));
		tabViewer.setContent(viewer);
		tabViewer.setClosable(false);

		tabPane.getTabs().addAll(tabEditor, tabViewer);

		final Scene scene = new Scene(tabPane, 1024, 768);
		scene.getStylesheets().add("file:data/gui/style.css");
		scene.getStylesheets().add("file:data/gui/keywords.css");
		scene.getStylesheets().add("file:data/gui/xml_highlighting.css");
		stage.setScene(scene);

		stage.maximizedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				viewer.viewerGraph.refresh();
			}
		});
		stage.getScene().widthProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observableValue, final Number oldSceneWidth,
					final Number newSceneWidth) {
				viewer.viewerGraph.refresh();
			}
		});
		stage.getScene().heightProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(final ObservableValue<? extends Number> observableValue, final Number oldSceneHeight,
					final Number newSceneHeight) {
				viewer.viewerGraph.refresh();
			}
		});
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
		this.pngFilter = new FileChooser.ExtensionFilter("*.png Image", "*.png");

		// Flag
		started = true;

		// Additional Shortcuts
		Platform.runLater(() -> {
			stage.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.NUMPAD0, KeyCombination.CONTROL_ANY),
					new Runnable() {
						@Override
						public void run() {
							viewer.zoomReset();
						}
					});
			stage.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.ADD, KeyCombination.CONTROL_ANY),
					new Runnable() {
						@Override
						public void run() {
							viewer.zoomIn();
						}
					});
			stage.getScene().getAccelerators().put(new KeyCodeCombination(KeyCode.SUBTRACT, KeyCombination.CONTROL_ANY),
					new Runnable() {
						@Override
						public void run() {
							viewer.zoomOut();
						}
					});
		});

		// SplashScreen
		final SplashScreen splashScreen = new SplashScreen(
				Properties.info().ABBRRVIATION + " (v. " + Properties.info().VERSION + ")",
				"by " + Properties.info().AUTHOR, Color.WHITE);
		new Thread(() -> {
			try {
				this.time = java.lang.System.currentTimeMillis() - this.time;
				if (this.time < 2000) {
					Thread.sleep(2000 - this.time);
				}
			} catch (final Exception e) {
				// do nothing
			}
			Platform.runLater(() -> {
				splashScreen.setDone(true);
				if (!showConfigWizard) {
					stage.show();
					ConfigHandler.getInstance().getConfig();
				} else {
					new ConfigWizard(stage, ConfigHandler.getInstance().getConfigFile());
				}
			});
		}).start();
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
				this.openDialog.setInitialDirectory(new File("answers"));
			}
		}

		final File file = this.openDialog.showOpenDialog(this.stage);
		if (file != null) {
			try {
				if (!viewerActive) {
					// Editor
					editor.openFile(file);
					this.currentQuery = file;
				} else {
					// Viewer
					viewer.openFile(file);
					this.currentAnswer = file;
				}
			} catch (final Exception e) {
				Log.msg("File not found: " + file.toString(), Log.ERROR);
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
			this.currentQuery = this.saveDialog.showSaveDialog(this.stage);
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
				this.saveDialog.setInitialDirectory(new File("answers"));
			}
			this.currentAnswer = this.saveDialog.showSaveDialog(this.stage);
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
				Files.write(Paths.get(file.toURI()), editor.getContent().getBytes(), StandardOpenOption.CREATE_NEW);
				Storage.getInstance().store(file, false);
			} catch (final IOException e) {
				Log.msg("Could not write file: " + file.getAbsolutePath() + " (" + e.getMessage() + ")", Log.ERROR);
			}
		} else {
			// Viewer
			Answer temp = AnswerHandler.parseXML(viewer.viewerXML.getContent());
			if (temp == null) {
				temp = new Answer();
			}
			AnswerHandler.createXML(temp, file);
			Storage.getInstance().store(file, true);
			((BottomViewer) viewer.getBottom()).refresh();
		}
		adjustTitle();
	}

	@Override
	public void exit() {
		new ExitDialog("Exit", "You will exit the AQL-System now.", "Proceed?");
	}

	public void exportGraph() {
		this.saveDialog.getExtensionFilters().clear();
		this.saveDialog.getExtensionFilters().addAll(this.allFilter, this.pngFilter);
		this.saveDialog.setSelectedExtensionFilter(this.pngFilter);
		if (this.currentExport != null) {
			this.saveDialog.setInitialDirectory(this.currentExport.getParentFile());
		} else {
			this.saveDialog.setInitialDirectory(new File("./"));
		}
		this.currentExport = this.saveDialog.showSaveDialog(this.stage);
		if (this.currentExport != null) {
			viewer.viewerGraph.exportGraph(this.currentExport);
		}
	}

	private void adjustTitle() {
		Platform.runLater(() -> {
			if (viewerActive) {
				if (this.currentAnswer == null) {
					this.stage.setTitle("AQL-Viewer");
				} else {
					this.stage.setTitle("AQL-Viewer (" + this.currentAnswer.getAbsolutePath() + ")");
				}
			} else {
				if (this.currentQuery == null) {
					this.stage.setTitle("AQL-Editor");
				} else {
					this.stage.setTitle("AQL-Editor (" + this.currentQuery.getAbsolutePath() + ")");
				}
			}
		});
	}

	@Override
	public Stage getStage() {
		return this.stage;
	}

	@Override
	public System getSystem() {
		return this.system;
	}
}
