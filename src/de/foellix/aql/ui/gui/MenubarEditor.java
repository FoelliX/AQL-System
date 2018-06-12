package de.foellix.aql.ui.gui;

import de.foellix.aql.system.IProgressChanged;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
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
import javafx.scene.text.Text;

public class MenubarEditor extends VBox implements IProgressChanged {
	private final Editor parent;

	private final ProgressIndicator progressIndicator;
	private final Text progressText;
	private final ProgressBar progressBar;
	private final Button btnAsk;
	private final MenuItem menuItemAsk;

	MenubarEditor(final Editor parent) {
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
		final MenuItem menuItemInsertFilename = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_FILE_ALT,
				StringConstants.STR_INSERT_FILENAME);
		menuItemInsertFilename.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.insertFilename();
			}
		});
		final SeparatorMenuItem sep2 = new SeparatorMenuItem();
		this.menuItemAsk = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_PLAY,
				StringConstants.STR_ASK_QUERY);
		this.menuItemAsk.setAccelerator(new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN));
		this.menuItemAsk.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.ask();
			}
		});
		menuEdit.getItems().addAll(menuItemUndo, menuItemRedo, sep1, menuItemAutoFormat, menuItemInsertFilename, sep2,
				this.menuItemAsk);

		menuBar.getMenus().addAll(new MenuFile(parent.getParentGUI()), menuEdit,
				new MenuHelp(parent.getParentGUI().getStage()));

		// Toolbar
		final ToolBar toolBar = new ToolBar();

		final Separator sep3 = new Separator();
		final Button btnAutoFormat = FontAwesome.getInstance().createButton(FontAwesome.ICON_INDENT_RIGHT);
		btnAutoFormat.setTooltip(new Tooltip(StringConstants.STR_AUTO_FORMAT));
		btnAutoFormat.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.autoformat();
			}
		});
		final Button btnInsertFilename = FontAwesome.getInstance().createButton(FontAwesome.ICON_FILE_ALT);
		btnInsertFilename.setTooltip(new Tooltip(StringConstants.STR_INSERT_FILENAME));
		btnInsertFilename.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.insertFilename();
			}
		});
		final Separator sep4 = new Separator();
		final HBox progressBox = new HBox(10);
		HBox.setHgrow(progressBox, Priority.ALWAYS);
		final HBox indicatorBox = new HBox(0);
		indicatorBox.setAlignment(Pos.CENTER_LEFT);
		this.progressIndicator = new ProgressIndicator(-1);
		this.progressIndicator.setMaxWidth(25);
		this.progressIndicator.setMaxHeight(25);
		this.progressIndicator.setVisible(false);
		indicatorBox.getChildren().add(this.progressIndicator);
		final VBox innerProgressBox = new VBox(0);
		this.progressText = new Text("0% (0 of 0)");
		this.progressBar = new ProgressBar(0);
		this.progressBar.setPrefWidth(300);
		innerProgressBox.getChildren().addAll(this.progressText, this.progressBar);
		progressBox.getChildren().addAll(innerProgressBox);
		this.btnAsk = FontAwesome.getInstance().createButton(FontAwesome.ICON_PLAY);
		this.btnAsk.setTooltip(new Tooltip(StringConstants.STR_ASK_QUERY));
		FontAwesome.getInstance().setGreen(this.btnAsk);
		this.btnAsk.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				parent.ask();
			}
		});

		toolBar.getItems().addAll(new ToolsetFile(parent.getParentGUI()), sep3, btnAutoFormat, btnInsertFilename, sep4,
				progressBox, indicatorBox, this.btnAsk);

		this.getChildren().addAll(menuBar, toolBar);
	}

	@Override
	public void onProgressChanged(final String step, final int inProgress, final int done, final int max) {
		Platform.runLater(() -> {
			final float percentage = (max == 0 ? 0 : (float) done / (float) max);
			this.progressText.setText(step + " " + Math.round(percentage * 100) + "% (In Progress: " + inProgress
					+ ", Finished: " + done + " of " + max + ")");
			this.progressBar.setProgress(percentage);
			if (done < max) {
				this.menuItemAsk.setDisable(true);
				FontAwesome.getInstance().setRed(this.btnAsk);
				this.btnAsk.setText(FontAwesome.ICON_STOP);
				this.parent.setStop(true);
				this.progressIndicator.setVisible(true);
			} else {
				this.menuItemAsk.setDisable(false);
				FontAwesome.getInstance().setGreen(this.btnAsk);
				this.btnAsk.setText(FontAwesome.ICON_PLAY);
				this.parent.setStop(false);
				this.progressIndicator.setVisible(false);
			}
		});
	}
}
