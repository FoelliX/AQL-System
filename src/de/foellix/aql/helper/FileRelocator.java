package de.foellix.aql.helper;

import java.io.File;
import java.util.Optional;

import de.foellix.aql.Log;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FileRelocator {
	private final Stage stage;

	private final FileChooser loadFileDialog;
	private final DirectoryChooser loadFolderDialog;
	private final Alert alert;
	private final ButtonType buttonTypeFolderIgnoreParent, buttonTypeFolder, buttonTypeFile, buttonTypeCancel;

	private boolean ignoreParent = false;
	private File relocateFolder = null;

	public FileRelocator(Stage stage) {
		this.stage = stage;

		this.loadFolderDialog = new DirectoryChooser();
		this.loadFileDialog = new FileChooser();
		final FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("*.* All files", "*.*");
		final FileChooser.ExtensionFilter apkFilter = new FileChooser.ExtensionFilter(
				"*.apk Android Application Package", "*.apk");
		this.loadFileDialog.getExtensionFilters().addAll(allFilter, apkFilter);
		this.loadFileDialog.setSelectedExtensionFilter(apkFilter);

		this.alert = new Alert(AlertType.CONFIRMATION);
		final Stage alertStage = (Stage) this.alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons().add(new Image("file:data/gui/images/icon_16.png", 16, 16, false, true));
		alertStage.getIcons().add(new Image("file:data/gui/images/icon_32.png", 32, 32, false, true));
		alertStage.getIcons().add(new Image("file:data/gui/images/icon_64.png", 64, 64, false, true));
		this.alert.setTitle("File not found");
		this.alert.setContentText("Relocate it now by selecting a parent directory or choosing the file precisely.");
		this.buttonTypeFolderIgnoreParent = new ButtonType("Directory (Ignore parent directory)");
		this.buttonTypeFolder = new ButtonType("Directory");
		this.buttonTypeFile = new ButtonType("File");
		this.buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
		this.alert.getButtonTypes().setAll(this.buttonTypeFolderIgnoreParent, this.buttonTypeFolder,
				this.buttonTypeFile, this.buttonTypeCancel);

		this.relocateFolder = null;
	}

	public File relocateFile(File file) {
		File relocateFile = recursivelySearchFile(file, this.relocateFolder, this.ignoreParent);
		if (this.relocateFolder != null) {
			this.loadFileDialog.setInitialDirectory(this.relocateFolder);
			this.loadFolderDialog.setInitialDirectory(this.relocateFolder);
		}
		while (relocateFile == null || !relocateFile.exists()) {
			this.alert.setHeaderText("The following file could not be found:\n" + file.getAbsolutePath());
			this.loadFileDialog.setTitle("Relocate: " + file.getAbsolutePath());
			this.loadFolderDialog.setTitle("Relocate: " + file.getAbsolutePath());
			final Optional<ButtonType> result = this.alert.showAndWait();
			if (result.get() == this.buttonTypeFile) {
				relocateFile = this.loadFileDialog.showOpenDialog(this.stage);
				if (relocateFile != null && relocateFile.exists() && relocateFile.getParentFile().exists()) {
					this.relocateFolder = relocateFile.getParentFile();
				}
			} else if (result.get() == this.buttonTypeFolder) {
				this.ignoreParent = false;
				this.relocateFolder = this.loadFolderDialog.showDialog(this.stage);
				relocateFile = recursivelySearchFile(file, this.relocateFolder, this.ignoreParent);
			} else if (result.get() == this.buttonTypeFolderIgnoreParent) {
				this.ignoreParent = true;
				this.relocateFolder = this.loadFolderDialog.showDialog(this.stage);
				relocateFile = recursivelySearchFile(file, this.relocateFolder, this.ignoreParent);
			}
		}
		Log.msg(file.getAbsolutePath() + "\nrelocated at\n" + relocateFile.getAbsolutePath(), Log.NORMAL);

		return relocateFile;
	}

	public static File recursivelySearchFile(File file, File folder) {
		return recursivelySearchFile(file, folder, false);
	}

	public static File recursivelySearchFile(File file, File folder, boolean ignoreParent) {
		if (folder == null) {
			return null;
		}
		for (final File child : folder.listFiles()) {
			if (file.getName().equals(child.getName())
					&& (ignoreParent || file.getParentFile().getName().equals(child.getParentFile().getName()))) {
				return child;
			} else if (child.isDirectory()) {
				final File recursiveFile = recursivelySearchFile(file, child, ignoreParent);
				if (recursiveFile != null) {
					return recursiveFile;
				}
			}
		}
		return null;
	}
}
