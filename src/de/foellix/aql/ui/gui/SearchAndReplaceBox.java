package de.foellix.aql.ui.gui;

import java.io.File;

import org.fxmisc.richtext.CodeArea;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SearchAndReplaceBox {
	private Dialog<ButtonType> dialog;
	private CodeArea ta;
	private TextField needle;
	private boolean close;
	private int offset;
	private int caret;

	public SearchAndReplaceBox(CodeArea ta) {
		this(ta, null);
	}

	public SearchAndReplaceBox(CodeArea ta, String titleAddition) {
		this.ta = ta;
		ta.focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (this.ta.getSelectedText() != null && !this.ta.getSelectedText().isEmpty()) {
				this.caret = ta.getSelection().getStart();
			} else {
				this.caret = ta.getCaretPosition();
			}
			this.offset = -1;
		});
		this.dialog = init(titleAddition);
		this.close = false;
		this.offset = -1;
		this.caret = ta.getCaretPosition();
	}

	public void show() {
		if (this.ta.getSelectedText() != null && !this.ta.getSelectedText().isEmpty()) {
			this.needle.setText(this.ta.getSelectedText());
		}
		if (!this.dialog.isShowing()) {
			this.dialog.show();
			this.needle.requestFocus();
		} else {
			toFront(this.needle);
		}
	}

	private void toFront(Node focus) {
		((Stage) this.dialog.getDialogPane().getScene().getWindow()).toFront();
		focus.requestFocus();
	}

	private Dialog<ButtonType> init(String titleAddition) {
		final Dialog<ButtonType> dialog = new Dialog<>();
		if (titleAddition == null) {
			dialog.setTitle("Search and Replace");
		} else {
			dialog.setTitle("Search and Replace (" + titleAddition + ")");
		}
		dialog.setHeaderText(null);
		dialog.setGraphic(null);
		final Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		stage.getIcons()
				.add(new Image(new File("data/gui/images/icon_16.png").toURI().toString(), 16, 16, false, true));
		stage.getIcons()
				.add(new Image(new File("data/gui/images/icon_32.png").toURI().toString(), 32, 32, false, true));
		stage.getIcons()
				.add(new Image(new File("data/gui/images/icon_64.png").toURI().toString(), 64, 64, false, true));
		dialog.getDialogPane().getScene().getStylesheets().add(new File("data/gui/style.css").toURI().toString());
		final Button btnSearch = new Button("Search");
		final Button btnReplace = new Button("Replace");
		final ButtonType btnReplaceAndSearch = new ButtonType("Replace & Search");
		final ButtonType btnReplaceAll = new ButtonType("Replace All");
		dialog.getDialogPane().getButtonTypes().addAll(btnReplaceAndSearch, btnReplaceAll, ButtonType.CANCEL); // btnReplace, btnReplaceAndSearch, btnReplaceAll,
		dialog.initModality(Modality.WINDOW_MODAL);

		this.needle = new TextField();
		this.needle.setPrefWidth(225);
		if (this.ta.getSelectedText() != null && !this.ta.getSelectedText().isEmpty()) {
			this.needle.setText(this.ta.getSelectedText());
		} else {
			btnSearch.setDisable(true);
			btnReplace.setDisable(true);
			dialog.getDialogPane().lookupButton(btnReplaceAndSearch).setDisable(true);
			dialog.getDialogPane().lookupButton(btnReplaceAll).setDisable(true);
		}
		final TextField replacement = new TextField();
		replacement.setPrefWidth(225);
		if (this.ta.getSelectedText() != null && !this.ta.getSelectedText().isEmpty()) {
			// Initial replacement text?
			// replacement.setText("???");
		} else {
			btnReplace.setDisable(true);
			dialog.getDialogPane().lookupButton(btnReplaceAndSearch).setDisable(true);
			dialog.getDialogPane().lookupButton(btnReplaceAll).setDisable(true);
		}
		final ChangeListener<String> listener = new ChangeListener<>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (SearchAndReplaceBox.this.needle.getText() != null
						&& !SearchAndReplaceBox.this.needle.getText().isEmpty()) {
					btnSearch.setDisable(false);
					if (replacement.getText() != null && !replacement.getText().isEmpty()) {
						btnReplace.setDisable(false);
						dialog.getDialogPane().lookupButton(btnReplaceAndSearch).setDisable(false);
						dialog.getDialogPane().lookupButton(btnReplaceAll).setDisable(false);
					} else {
						btnReplace.setDisable(true);
						dialog.getDialogPane().lookupButton(btnReplaceAndSearch).setDisable(true);
						dialog.getDialogPane().lookupButton(btnReplaceAll).setDisable(true);
					}
				} else {
					btnSearch.setDisable(true);
					btnReplace.setDisable(true);
					dialog.getDialogPane().lookupButton(btnReplaceAndSearch).setDisable(true);
					dialog.getDialogPane().lookupButton(btnReplaceAll).setDisable(true);
				}
			}
		};
		this.needle.textProperty().addListener(listener);
		replacement.textProperty().addListener(listener);

		this.needle.setOnKeyPressed((e) -> {
			if (e.getCode() == KeyCode.ENTER) {
				// Search
				search();
			}
		});

		final GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.add(new Label("Search:"), 0, 0);
		grid.add(this.needle, 1, 0);
		grid.add(btnSearch, 2, 0);
		btnSearch.setPrefWidth(109);
		grid.add(new Label("Replace by:"), 0, 1);
		grid.add(replacement, 1, 1);
		grid.add(btnReplace, 2, 1);
		btnReplace.setPrefWidth(109);

		dialog.getDialogPane().setContent(grid);

		btnSearch.setOnAction((e) -> {
			// Search
			search();
			toFront(btnSearch);
		});
		btnReplace.setOnAction((e) -> {
			// Replace
			this.ta.replaceSelection(replacement.getText());
			toFront(btnReplace);
		});
		((Button) dialog.getDialogPane().lookupButton(btnReplaceAndSearch)).setOnAction((e) -> {
			// Replace and Search
			this.ta.replaceSelection(replacement.getText());
			search();
			e.consume();
		});
		((Button) dialog.getDialogPane().lookupButton(btnReplaceAll)).setOnAction((e) -> {
			// Replace all
			final int caretPos = this.ta.getCaretPosition();
			while (search()) {
				this.ta.replaceSelection(replacement.getText());
			}
			this.ta.moveTo(caretPos);
			this.offset = caretPos;
			toFront(dialog.getDialogPane().lookupButton(btnReplaceAll));
			e.consume();
		});
		((Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL)).setOnAction((e) -> {
			this.close = true;
			dialog.hide();
		});
		dialog.setOnCloseRequest((e) -> {
			if (!this.close) {
				e.consume();
			} else {
				this.close = false;
			}
		});
		return dialog;
	}

	public boolean search() {
		if (this.offset == -1) {
			this.ta.moveTo(this.caret);
		}
		this.offset = this.ta.getCaretPosition();
		int start = this.ta.getText().indexOf(this.needle.getText(), this.offset);
		if (start < 0) {
			this.offset = 0;
			start = this.ta.getText().indexOf(this.needle.getText(), this.offset);
		}
		if (start >= 0) {
			this.ta.requestFollowCaret();
			this.ta.selectRange(start, start + this.needle.getText().length());
			return true;
		} else {
			return false;
		}
	}
}