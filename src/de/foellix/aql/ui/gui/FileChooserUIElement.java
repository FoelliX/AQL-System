package de.foellix.aql.ui.gui;

import java.io.File;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FileChooserUIElement extends BorderPane {
	private static File lastDirectory;

	private final Stage stage;

	private final FileChooser.ExtensionFilter allFilter = new FileChooser.ExtensionFilter("*.* All files", "*.*");
	private final FileChooser.ExtensionFilter jarFilter = new FileChooser.ExtensionFilter("*.jar Java executable",
			"*.jar");
	private final FileChooser.ExtensionFilter shFilter = new FileChooser.ExtensionFilter("*.sh Bash script", "*.sh");
	private final FileChooser.ExtensionFilter batFilter = new FileChooser.ExtensionFilter("*.bat Batch script",
			"*.bat");

	private Button browseBtn;
	private final TextField textField;

	private boolean folder = false;

	public FileChooserUIElement(Stage stage, String btnText) {
		super();
		this.stage = stage;

		this.textField = new TextField();
		this.textField.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(this.textField, Priority.ALWAYS);
		setCenter(this.textField);
		if (btnText != null) {
			this.textField.setStyle("-fx-background-radius: 2.5 0 0 2.5;");
			this.browseBtn = new Button(btnText);
			this.browseBtn.setStyle("-fx-background-radius: 0 2.5 2.5 0;");
			this.browseBtn.setOnAction(eh -> buttonClicked());
			BorderPane.setAlignment(this.browseBtn, Pos.CENTER);
			setRight(this.browseBtn);
		}
	}

	public void setNumeric() {
		this.textField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (!newValue.matches("\\d*")) {
					FileChooserUIElement.this.textField.setText(newValue.replaceAll("[^\\d]", ""));
				}
			}
		});
	}

	public void setFolder() {
		this.folder = true;
	}

	public TextField getTextField() {
		return this.textField;
	}

	Button getButton() {
		return this.browseBtn;
	}

	void buttonClicked() {
		final File file;
		if (this.folder) {
			final DirectoryChooser chooser = new DirectoryChooser();
			if (lastDirectory != null) {
				chooser.setInitialDirectory(lastDirectory);
			}
			file = chooser.showDialog(this.stage);
		} else {
			final FileChooser chooser = new FileChooser();
			chooser.getExtensionFilters().addAll(this.allFilter, this.jarFilter, this.shFilter, this.batFilter);
			chooser.setSelectedExtensionFilter(this.allFilter);
			if (lastDirectory != null) {
				chooser.setInitialDirectory(lastDirectory);
			}
			file = chooser.showOpenDialog(this.stage);
		}
		if (file != null) {
			this.textField.setText(file.getAbsolutePath());
			if (file.isDirectory()) {
				lastDirectory = file;
			} else {
				lastDirectory = file.getParentFile();
			}
		}
	}
}