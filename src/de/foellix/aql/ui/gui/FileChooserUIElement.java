package de.foellix.aql.ui.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

public class FileChooserUIElement extends BorderPane {
	public static final FileChooser.ExtensionFilter FILTER_ALL = new FileChooser.ExtensionFilter("*.* All files",
			"*.*");
	public static final FileChooser.ExtensionFilter FILTER_JAR = new FileChooser.ExtensionFilter(
			"*.jar Java executable", "*.jar");
	public static final FileChooser.ExtensionFilter FILTER_SH = new FileChooser.ExtensionFilter("*.sh Bash script",
			"*.sh");
	public static final FileChooser.ExtensionFilter FILTER_BAT = new FileChooser.ExtensionFilter("*.bat Batch script",
			"*.bat");
	public static final FileChooser.ExtensionFilter FILTER_APK = new FileChooser.ExtensionFilter(
			"*.apk Android Application Package", "*.apk");
	public static final FileChooser.ExtensionFilter FILTER_XML = new FileChooser.ExtensionFilter("*.xml AQL-Answer",
			"*.xml");

	private static File lastDirectory = new File(".");

	private final Stage stage;

	private final List<FileChooser.ExtensionFilter> filters;
	private Button browseBtn;
	private final TextField textField;

	private boolean folder = false;
	private boolean save = false;
	private ExtensionFilter initFilter;

	public FileChooserUIElement(Stage stage, String btnToolTip) {
		super();

		this.filters = new ArrayList<>();
		this.filters.add(FILTER_ALL);
		this.filters.add(FILTER_JAR);
		this.filters.add(FILTER_SH);
		this.filters.add(FILTER_BAT);
		this.initFilter = FILTER_ALL;

		this.stage = stage;

		this.textField = new TextField();
		this.textField.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(this.textField, Priority.ALWAYS);
		setCenter(this.textField);
		if (btnToolTip != null) {
			this.textField.setStyle("-fx-background-radius: 2.5 0 0 2.5;");
			this.browseBtn = new Button(FontAwesome.ICON_FOLDER_OPEN_ALT);
			FontAwesome.applyFontAwesome(this.browseBtn, "-fx-background-radius: 0 2.5 2.5 0;");
			this.browseBtn.setTooltip(new Tooltip(btnToolTip));
			this.browseBtn.setPrefHeight(25d);
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

	public void setSave() {
		this.save = true;
	}

	public TextField getTextField() {
		return this.textField;
	}

	public Button getButton() {
		return this.browseBtn;
	}

	public List<FileChooser.ExtensionFilter> getFilters() {
		return this.filters;
	}

	public void setInitFilter(ExtensionFilter filter) {
		this.initFilter = filter;
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
			chooser.getExtensionFilters().addAll(this.filters);
			chooser.setSelectedExtensionFilter(this.initFilter);
			if (lastDirectory != null) {
				chooser.setInitialDirectory(lastDirectory);
			}
			if (this.save) {
				file = chooser.showSaveDialog(this.stage);
			} else {
				file = chooser.showOpenDialog(this.stage);
			}
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