package de.foellix.aql.config.wizard;

import de.foellix.aql.config.Priority;
import de.foellix.aql.config.Tool;
import de.foellix.aql.ui.gui.FileChooserUIElement;
import de.foellix.aql.ui.gui.FontAwesome;
import de.foellix.aql.ui.gui.StringConstants;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class EditorOverview extends BorderPane {
	private final Overview parent;

	private final Label[] labels = new Label[13];
	private final HelpButton[] helpBtn = new HelpButton[13];
	private final FileChooserUIElement[] texts = new FileChooserUIElement[13];
	private ScrollPane priorityScroll;
	private TableView<Priority> priorityTable;
	private Button addBtn;

	private Tool currentTool;

	private static final String TOOLTIP_TOOL = "Available variables: \r\n" + "\r\n" + "%APP_APK%: .apk file\r\n"
			+ "%APP_APK_FILENAME%: .apk file without path and \".apk\"\r\n" + "%APP_NAME%: App name\r\n"
			+ "%APP_PACKAGE%: App package\r\n" + "%ANDROID_PLATFORMS%: Android platforms folder\r\n"
			+ "%MEMORY%: Memory\r\n" + "%PID%: Process ID";
	private static final String TOOLTIP_PREPROCESSOR = TOOLTIP_TOOL;
	private static final String TOOLTIP_OPERATOR = "Available variables: \r\n" + "\r\n"
			+ "%ANSWERS%: Input AQL-Answers for operators\r\n" + "%ANSWERSHASH%: SHA-256-hash of %ANSWERS%-String\r\n"
			+ "%MEMORY%: Memory\r\n" + "%PID%: Process ID";
	private static final String TOOLTIP_CONVERTER = "Available variables: \r\n" + "\r\n"
			+ "%RESULT_FILE%: Result file of a tool\r\n" + "%MEMORY%: Memory\r\n" + "%PID%: Process ID";
	private static final String TOOLTIP_EVENT = "Available variables: \r\n" + "\r\n" + "%MEMORY%: Memory\r\n"
			+ "%PID%: Process ID";

	EditorOverview(final Overview parent) {
		super();

		this.parent = parent;

		final ScrollPane scrollBox = new ScrollPane();
		scrollBox.setFitToWidth(true);
		final VBox editorBox = new VBox(5);
		editorBox.setPadding(new Insets(10));
		this.labels[0] = new Label("Name:");
		this.labels[1] = new Label("Version:");
		this.labels[2] = new Label();
		this.labels[3] = new Label("Priority:");
		this.labels[4] = new Label("Instances (0 = \u221e):");
		this.labels[5] = new Label("Memory per instance (in GB):");
		this.labels[6] = new Label("Path: ");
		this.helpBtn[6] = new HelpButton(TOOLTIP_TOOL);
		this.labels[7] = new Label("Run: ");
		this.helpBtn[7] = new HelpButton(TOOLTIP_TOOL);
		this.labels[8] = new Label("Result: ");
		this.helpBtn[8] = new HelpButton(TOOLTIP_TOOL);
		this.labels[9] = new Label("Run on Abort: ");
		this.helpBtn[9] = new HelpButton(TOOLTIP_EVENT);
		this.labels[10] = new Label("Run on Fail: ");
		this.helpBtn[10] = new HelpButton(TOOLTIP_EVENT);
		this.labels[11] = new Label("Run on Success: ");
		this.helpBtn[11] = new HelpButton(TOOLTIP_EVENT);
		this.labels[12] = new Label("Run on Exit: ");
		this.helpBtn[12] = new HelpButton(TOOLTIP_EVENT);
		for (int i = 0; i <= 8; i++) {
			if (i != 3) {
				if (i >= 6) {
					this.texts[i] = new FileChooserUIElement(parent.getParentGUI().getStage(),
							StringConstants.STR_BROWSE);
				} else {
					this.texts[i] = new FileChooserUIElement(parent.getParentGUI().getStage(), null);
				}
				if (i >= 4 && i <= 5) {
					this.texts[i].setNumeric();
				}
				if (i == 6) {
					this.texts[i].setFolder();
				}
				if (this.helpBtn[i] != null) {
					final FlowPane helpPane = new FlowPane();
					helpPane.getChildren().addAll(this.labels[i], this.helpBtn[i]);
					editorBox.getChildren().addAll(helpPane, this.texts[i]);
				} else {
					editorBox.getChildren().addAll(this.labels[i], this.texts[i]);
				}
			} else {
				final BorderPane priorityBox = new BorderPane();
				this.priorityTable = new TableView<>();
				this.priorityTable.setEditable(true);

				final TableColumn<Priority, Integer> colValue = new TableColumn<Priority, Integer>("Value");
				colValue.setCellValueFactory(new PropertyValueFactory<Priority, Integer>("value"));
				colValue.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Integer>() {
					@Override
					public String toString(Integer object) {
						return object.toString();
					}

					@Override
					public Integer fromString(String string) {
						return Integer.valueOf(string);
					}
				}));
				colValue.setOnEditCommit(new EventHandler<CellEditEvent<Priority, Integer>>() {
					@Override
					public void handle(CellEditEvent<Priority, Integer> t) {
						t.getTableView().getItems().get(t.getTablePosition().getRow()).setValue(t.getNewValue());
					}
				});
				final TableColumn<Priority, String> colFeature = new TableColumn<Priority, String>("Feature");
				colFeature.setCellValueFactory(new PropertyValueFactory<Priority, String>("feature"));
				colFeature.setCellFactory(TextFieldTableCell.forTableColumn());
				colFeature.setOnEditCommit(new EventHandler<CellEditEvent<Priority, String>>() {
					@Override
					public void handle(CellEditEvent<Priority, String> t) {
						t.getTableView().getItems().get(t.getTablePosition().getRow()).setFeature(t.getNewValue());
					}
				});
				this.priorityTable.getColumns().addAll(colValue, colFeature);

				this.priorityScroll = new ScrollPane(this.priorityTable);
				this.priorityScroll.setFitToWidth(true);
				this.priorityScroll.setMaxHeight(95d);

				this.addBtn = new Button("Add");
				this.addBtn.setOnAction(eh -> this.priorityTable.getItems().add(new Priority()));
				this.addBtn.setMaxHeight(95d);

				priorityBox.setCenter(this.priorityScroll);
				priorityBox.setRight(this.addBtn);

				editorBox.getChildren().addAll(this.labels[i], priorityBox);
			}
		}
		final VBox runOnEventBox = new VBox(5);
		final TitledPane runOnEventRoot = new TitledPane("Run on Event", runOnEventBox);
		runOnEventRoot.setExpanded(false);
		for (int i = 9; i <= 12; i++) {
			this.texts[i] = new FileChooserUIElement(parent.getParentGUI().getStage(), StringConstants.STR_BROWSE);
			if (this.helpBtn[i] != null) {
				final FlowPane helpPane = new FlowPane();
				helpPane.getChildren().addAll(this.labels[i], this.helpBtn[i]);
				runOnEventBox.getChildren().addAll(helpPane, this.texts[i]);
			} else {
				runOnEventBox.getChildren().addAll(this.labels[i], this.texts[i]);
			}
		}
		editorBox.getChildren().addAll(new Label(), runOnEventRoot);
		scrollBox.setContent(editorBox);

		final Button applyBtn = new Button("Apply");
		final BorderPane applyPane = new BorderPane();
		applyPane.setRight(applyBtn);
		applyPane.setPadding(new Insets(10));
		applyBtn.setOnAction(eh -> apply());

		this.setBottom(applyPane);
		this.setCenter(scrollBox);
	}

	public void load(final Tool tool, final int type) {
		if (tool != null && tool.getName() != null) {
			this.currentTool = tool;
			final boolean toggle = type == Overview.TYPE_CONVERTER;

			this.labels[2].setText(Overview.typeToString(type) + " (separated by \",\")");
			this.helpBtn[6].setToolTip(typeToToolTip(type));
			this.helpBtn[7].setToolTip(typeToToolTip(type));
			this.helpBtn[8].setToolTip(typeToToolTip(type));

			this.texts[0].getTextField().setText(tool.getName());
			this.texts[1].getTextField().setText(tool.getVersion());
			this.texts[2].getTextField().setText(tool.getQuestions());
			this.priorityScroll.setDisable(toggle);
			this.addBtn.setDisable(toggle);
			this.priorityTable.setDisable(toggle);
			this.priorityTable.getItems().setAll(tool.getPriority());

			this.texts[4].setDisable(toggle);
			this.texts[4].getTextField().setText(String.valueOf(tool.getInstances()));
			this.texts[5].getTextField().setText(String.valueOf(tool.getMemoryPerInstance()));

			this.texts[6].getTextField().setText(tool.getPath());
			this.texts[7].getTextField().setText(tool.getRun());
			this.texts[8].getTextField().setText(tool.getResult());

			this.texts[9].setDisable(toggle);
			this.texts[10].setDisable(toggle);
			this.texts[11].setDisable(toggle);
			this.texts[12].setDisable(toggle);
			this.texts[9].getTextField().setText(tool.getRunOnAbort());
			this.texts[10].getTextField().setText(tool.getRunOnFail());
			this.texts[11].getTextField().setText(tool.getRunOnSuccess());
			this.texts[12].getTextField().setText(tool.getRunOnExit());
		}
	}

	private void apply() {
		this.currentTool.setName(this.texts[0].getTextField().getText());
		this.currentTool.setVersion(this.texts[1].getTextField().getText());
		this.currentTool.setQuestions(this.texts[2].getTextField().getText());
		this.currentTool.getPriority().clear();
		this.currentTool.getPriority().addAll(this.priorityTable.getItems());

		this.currentTool.setInstances(Integer.parseInt(this.texts[4].getTextField().getText()));
		this.currentTool.setMemoryPerInstance(Integer.parseInt(this.texts[5].getTextField().getText()));

		this.currentTool.setPath(this.texts[6].getTextField().getText());
		this.currentTool.setRun(this.texts[7].getTextField().getText());
		this.currentTool.setResult(this.texts[8].getTextField().getText());

		this.currentTool.setRunOnAbort(this.texts[9].getTextField().getText());
		this.currentTool.setRunOnFail(this.texts[10].getTextField().getText());
		this.currentTool.setRunOnSuccess(this.texts[11].getTextField().getText());
		this.currentTool.setRunOnExit(this.texts[12].getTextField().getText());

		this.parent.apply();
	}

	private class HelpButton extends Button {
		String toolTip;

		HelpButton(String toolTip) {
			super("?");

			this.toolTip = toolTip;

			this.setTooltip(new Tooltip(toolTip));
			FontAwesome.getInstance().setBlue(this);
			this.setOnAction(eh -> showToolTip());
		}

		private void showToolTip() {

			final Alert alert = new Alert(AlertType.INFORMATION);
			final Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
			alertStage.getIcons().add(new Image("file:data/gui/images/icon_16.png", 16, 16, false, true));
			alertStage.getIcons().add(new Image("file:data/gui/images/icon_32.png", 32, 32, false, true));
			alertStage.getIcons().add(new Image("file:data/gui/images/icon_64.png", 64, 64, false, true));
			alert.setTitle("Help");
			alert.setHeaderText(this.toolTip.substring(0, this.toolTip.indexOf("\r\n")));
			final String text = this.toolTip.substring(this.toolTip.indexOf("\r\n") + 4);
			final TextArea textArea = new TextArea(text);
			textArea.setEditable(false);
			textArea.setPrefHeight(text.split("\r\n").length * 17d + 10d);
			textArea.setWrapText(true);
			final BorderPane content = new BorderPane();
			content.setCenter(textArea);
			alert.getDialogPane().setContent(content);

			alert.showAndWait();
		}

		public void setToolTip(String toolTip) {
			this.toolTip = toolTip;
		}
	}

	private static String typeToToolTip(int type) {
		if (type == Overview.TYPE_TOOL) {
			return TOOLTIP_TOOL;
		} else if (type == Overview.TYPE_PREPROCESSOR) {
			return TOOLTIP_PREPROCESSOR;
		} else if (type == Overview.TYPE_OPERATOR) {
			return TOOLTIP_OPERATOR;
		} else if (type == Overview.TYPE_CONVERTER) {
			return TOOLTIP_CONVERTER;
		} else {
			return "UNKNOWN TYPE";
		}
	}
}
