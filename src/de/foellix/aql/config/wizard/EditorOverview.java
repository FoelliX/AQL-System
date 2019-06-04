package de.foellix.aql.config.wizard;

import de.foellix.aql.config.Execute;
import de.foellix.aql.config.Priority;
import de.foellix.aql.config.Tool;
import de.foellix.aql.ui.gui.FileChooserUIElement;
import de.foellix.aql.ui.gui.FontAwesome;
import de.foellix.aql.ui.gui.StringConstants;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class EditorOverview extends BorderPane {
	// GENERAL
	final private static int ITEM_NAME = 0;
	final private static int ITEM_VERSION = 1;
	final private static int ITEM_QUESTIONS = 2;
	final private static int ITEM_PRIORITY = 3;
	final private static int ITEM_PATH = 4;

	// INTERNAL
	final private static int ITEM_RUN = 5;
	final private static int ITEM_RESULT = 6;
	final private static int ITEM_INSTANCES = 7;
	final private static int ITEM_MEMORY_PER_INSTANCE = 8;

	// EXTERNAL
	final private static int ITEM_URL = 9;
	final private static int ITEM_USERNAME = 10;
	final private static int ITEM_PASSWORD = 11;

	// EXTRA
	final private static int ITEM_RUN_ON_ABORT = 12;
	final private static int ITEM_RUN_ON_FAIL = 13;
	final private static int ITEM_RUN_ON_SUCCESS = 14;
	final private static int ITEM_RUN_ON_EXIT = 15;
	final private static int ITEM_RUN_ON_ENTRY = 15;

	private final Overview parent;

	private final Label[] labels = new Label[16];
	private final HelpButton[] helpBtn = new HelpButton[16];
	private final FlowPane[] helpPane = new FlowPane[16];
	private final FileChooserUIElement[] texts = new FileChooserUIElement[16];
	private ScrollPane priorityScroll;
	private TableView<Priority> priorityTable;
	private Button addBtn;
	final RadioButton radioBtnInternal, radioBtnExternal;

	private Tool currentTool;

	private static final String TOOLTIP_TOOL = "Available variables: \r\n" + "\r\n" + "%APP_APK%: .apk file\r\n"
			+ "%APP_APK_FILENAME%: .apk file without path and \".apk\"\r\n" + "%APP_NAME%: App name\r\n"
			+ "%APP_PACKAGE%: App package\r\n" + "%ANDROID_PLATFORMS%: Android platforms folder\r\n"
			+ "%MEMORY%: Memory\r\n" + "%PID%: Process ID";
	private static final String TOOLTIP_PREPROCESSOR = TOOLTIP_TOOL;
	private static final String TOOLTIP_OPERATOR = "Available variables: \r\n" + "\r\n"
			+ "%ANSWERS%: Input AQL-Answers for operators\r\n" + "%ANSWERSHASH%: SHA-256-hash of %ANSWERS%-String\r\n"
			+ "%ANDROID_PLATFORMS%: Android platforms folder\r\n" + "%MEMORY%: Memory\r\n" + "%PID%: Process ID";
	private static final String TOOLTIP_CONVERTER_1 = "Available variables: \r\n" + "\r\n"
			+ "%RESULT_FILE%: Result file of a tool\r\n" + "%APP_APK%: .apk file\r\n"
			+ "%APP_APK_FILENAME%: .apk file without path and \".apk\"\r\n" + "%APP_NAME%: App name\r\n"
			+ "%APP_PACKAGE%: App package\r\n" + "%ANDROID_PLATFORMS%: Android platforms folder\r\n"
			+ "%MEMORY%: Memory\r\n" + "%PID%: Process ID";
	private static final String TOOLTIP_CONVERTER_2 = "Available variables: \r\n" + "\r\n" + "%APP_APK%: .apk file\r\n"
			+ "%APP_APK_FILENAME%: .apk file without path and \".apk\"\r\n" + "%APP_NAME%: App name\r\n"
			+ "%APP_PACKAGE%: App package\r\n" + "%ANDROID_PLATFORMS%: Android platforms folder\r\n"
			+ "%MEMORY%: Memory\r\n" + "%PID%: Process ID";
	private static final String TOOLTIP_EVENT = "Available variables: \r\n" + "\r\n" + "%MEMORY%: Memory\r\n"
			+ "%PID%: Process ID";

	EditorOverview(final Overview parent) {
		super();

		this.parent = parent;

		final ScrollPane scrollBox = new ScrollPane();
		scrollBox.setFitToWidth(true);
		final VBox editorBox = new VBox(5);
		editorBox.setPadding(new Insets(10));

		final VBox executeBox = new VBox(5);
		executeBox.setPadding(new Insets(10));
		executeBox.setBorder(new Border(new BorderStroke(Color.rgb(200, 200, 200), BorderStrokeStyle.SOLID,
				new CornerRadii(3), BorderWidths.DEFAULT)));
		final ToggleGroup toggleGroup = new ToggleGroup();
		this.radioBtnInternal = new RadioButton("Internal");
		this.radioBtnInternal.setToggleGroup(toggleGroup);
		this.radioBtnInternal.setSelected(true);
		this.radioBtnExternal = new RadioButton("External");
		this.radioBtnExternal.setToggleGroup(toggleGroup);
		this.radioBtnExternal.setSelected(false);
		final HBox radioBox = new HBox(20);
		radioBox.getChildren().addAll(this.radioBtnInternal, this.radioBtnExternal);
		toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				if (toggleGroup.getSelectedToggle() == EditorOverview.this.radioBtnInternal) {
					for (int i = EditorOverview.ITEM_RUN; i <= EditorOverview.ITEM_MEMORY_PER_INSTANCE; i++) {
						show(i);
					}
					for (int i = EditorOverview.ITEM_URL; i <= EditorOverview.ITEM_PASSWORD; i++) {
						hide(i);
					}
				} else {
					for (int i = EditorOverview.ITEM_URL; i <= EditorOverview.ITEM_PASSWORD; i++) {
						show(i);
					}
					for (int i = EditorOverview.ITEM_RUN; i <= EditorOverview.ITEM_MEMORY_PER_INSTANCE; i++) {
						hide(i);
					}
				}
			}
		});
		executeBox.getChildren().addAll(radioBox, new Separator());

		final VBox runOnEventBox = new VBox(5);
		final TitledPane runOnEventRoot = new TitledPane("Run on Event", runOnEventBox);
		runOnEventRoot.setExpanded(false);

		this.labels[ITEM_NAME] = new Label("Name:");
		this.labels[ITEM_VERSION] = new Label("Version:");
		this.labels[ITEM_QUESTIONS] = new Label();
		this.labels[ITEM_PRIORITY] = new Label("Priority:");
		this.labels[ITEM_PATH] = new Label("Path: ");
		this.helpBtn[ITEM_PATH] = new HelpButton(TOOLTIP_TOOL);

		this.labels[ITEM_RUN] = new Label("Run: ");
		this.helpBtn[ITEM_RUN] = new HelpButton(TOOLTIP_TOOL);
		this.labels[ITEM_RESULT] = new Label("Result: ");
		this.helpBtn[ITEM_RESULT] = new HelpButton(TOOLTIP_TOOL);
		this.labels[ITEM_INSTANCES] = new Label("Instances (0 = \u221e):");
		this.labels[ITEM_MEMORY_PER_INSTANCE] = new Label("Memory per instance (in GB):");

		this.labels[ITEM_URL] = new Label("URL: ");
		this.labels[ITEM_USERNAME] = new Label("Username: ");
		this.labels[ITEM_PASSWORD] = new Label("Password: ");

		this.labels[ITEM_RUN_ON_ENTRY] = new Label("Run on Entry: ");
		this.helpBtn[ITEM_RUN_ON_ENTRY] = new HelpButton(TOOLTIP_EVENT);
		this.labels[ITEM_RUN_ON_ABORT] = new Label("Run on Abort: ");
		this.helpBtn[ITEM_RUN_ON_ABORT] = new HelpButton(TOOLTIP_EVENT);
		this.labels[ITEM_RUN_ON_FAIL] = new Label("Run on Fail: ");
		this.helpBtn[ITEM_RUN_ON_FAIL] = new HelpButton(TOOLTIP_EVENT);
		this.labels[ITEM_RUN_ON_SUCCESS] = new Label("Run on Success: ");
		this.helpBtn[ITEM_RUN_ON_SUCCESS] = new HelpButton(TOOLTIP_EVENT);
		this.labels[ITEM_RUN_ON_EXIT] = new Label("Run on Exit: ");
		this.helpBtn[ITEM_RUN_ON_EXIT] = new HelpButton(TOOLTIP_EVENT);

		for (int i = 0; i <= 11; i++) {
			if (i != ITEM_PRIORITY) {
				if (i == ITEM_PATH || i == ITEM_RUN || i == ITEM_RESULT || i == ITEM_RUN_ON_ENTRY
						|| i == ITEM_RUN_ON_ABORT || i == ITEM_RUN_ON_FAIL || i == ITEM_RUN_ON_SUCCESS
						|| i == ITEM_RUN_ON_EXIT) {
					this.texts[i] = new FileChooserUIElement(parent.getParentGUI().getStage(),
							StringConstants.STR_BROWSE);
				} else {
					this.texts[i] = new FileChooserUIElement(parent.getParentGUI().getStage(), null);
				}
				if (i == ITEM_INSTANCES || i == ITEM_MEMORY_PER_INSTANCE) {
					this.texts[i].setNumeric();
				}
				if (i == ITEM_PATH) {
					this.texts[i].setFolder();
				}
				Node toAdd;
				if (this.helpBtn[i] != null) {
					this.helpPane[i] = new FlowPane();
					this.helpPane[i].getChildren().addAll(this.labels[i], this.helpBtn[i]);
					toAdd = this.helpPane[i];
				} else {
					toAdd = this.labels[i];
				}
				if (i == ITEM_RUN || i == ITEM_RESULT || i == ITEM_INSTANCES || i == ITEM_MEMORY_PER_INSTANCE
						|| i == ITEM_URL || i == ITEM_USERNAME || i == ITEM_PASSWORD) {
					executeBox.getChildren().addAll(toAdd, this.texts[i]);
					if (!editorBox.getChildren().contains(executeBox)) {
						final Separator separator = new Separator();
						separator.setVisible(false);
						editorBox.getChildren().addAll(separator, executeBox);
					}
				} else {
					editorBox.getChildren().addAll(toAdd, this.texts[i]);
				}
				if (i >= EditorOverview.ITEM_URL && i <= EditorOverview.ITEM_PASSWORD) {
					hide(i);
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
		for (int i = ITEM_RUN_ON_ABORT; i <= ITEM_RUN_ON_ENTRY; i++) {
			this.texts[i] = new FileChooserUIElement(parent.getParentGUI().getStage(), StringConstants.STR_BROWSE);
			if (this.helpBtn[i] != null) {
				final FlowPane helpPane = new FlowPane();
				helpPane.getChildren().addAll(this.labels[i], this.helpBtn[i]);
				runOnEventBox.getChildren().addAll(helpPane, this.texts[i]);
			} else {
				runOnEventBox.getChildren().addAll(this.labels[i], this.texts[i]);
			}
		}
		final Separator separator = new Separator();
		separator.setVisible(false);
		editorBox.getChildren().addAll(separator, runOnEventRoot);
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
			final boolean toggle = (type == Overview.TYPE_CONVERTER);

			this.labels[ITEM_QUESTIONS].setText(Overview.typeToString(type) + " (separated by \",\")");
			this.helpBtn[ITEM_PATH].setToolTip(typeToToolTip(type, 1));
			this.helpBtn[ITEM_RUN].setToolTip(typeToToolTip(type, 1));
			this.helpBtn[ITEM_RESULT].setToolTip(typeToToolTip(type, 2));

			this.texts[ITEM_NAME].getTextField().setText(tool.getName());
			this.texts[ITEM_VERSION].getTextField().setText(tool.getVersion());
			this.texts[ITEM_QUESTIONS].getTextField().setText(tool.getQuestions());
			this.priorityScroll.setDisable(toggle);
			this.addBtn.setDisable(toggle);
			this.priorityTable.setDisable(toggle);
			this.priorityTable.getItems().setAll(tool.getPriority());
			this.texts[ITEM_PATH].getTextField().setText(tool.getPath());

			if (!tool.isExternal()) {
				this.radioBtnExternal.setSelected(false);
				this.radioBtnInternal.setSelected(true);

				if (tool.getExecute() != null) {
					this.texts[ITEM_RUN].getTextField().setText(tool.getExecute().getRun());
					this.texts[ITEM_RESULT].getTextField().setText(tool.getExecute().getResult());
					this.texts[ITEM_INSTANCES].getTextField().setText(String.valueOf(tool.getExecute().getInstances()));
					this.texts[ITEM_MEMORY_PER_INSTANCE].getTextField()
							.setText(String.valueOf(tool.getExecute().getMemoryPerInstance()));
				}
				this.texts[ITEM_INSTANCES].setDisable(toggle);

				this.texts[ITEM_URL].getTextField().clear();
				this.texts[ITEM_USERNAME].getTextField().clear();
				this.texts[ITEM_PASSWORD].getTextField().clear();
			} else {
				this.radioBtnExternal.setSelected(true);
				this.radioBtnInternal.setSelected(false);

				if (tool.getExecute() != null) {
					this.texts[ITEM_URL].getTextField().setText(tool.getExecute().getUrl());
					this.texts[ITEM_USERNAME].getTextField().setText(tool.getExecute().getUsername());
					this.texts[ITEM_PASSWORD].getTextField().setText(tool.getExecute().getPassword());
				}

				this.texts[ITEM_RUN].getTextField().clear();
				this.texts[ITEM_RESULT].getTextField().clear();
				this.texts[ITEM_INSTANCES].getTextField().clear();
				this.texts[ITEM_MEMORY_PER_INSTANCE].getTextField().clear();
			}

			this.texts[ITEM_RUN_ON_ENTRY].setDisable(toggle);
			this.texts[ITEM_RUN_ON_ABORT].setDisable(toggle);
			this.texts[ITEM_RUN_ON_FAIL].setDisable(toggle);
			this.texts[ITEM_RUN_ON_SUCCESS].setDisable(toggle);
			this.texts[ITEM_RUN_ON_EXIT].setDisable(toggle);
			this.texts[ITEM_RUN_ON_ENTRY].getTextField().setText(tool.getRunOnEntry());
			this.texts[ITEM_RUN_ON_ABORT].getTextField().setText(tool.getRunOnAbort());
			this.texts[ITEM_RUN_ON_FAIL].getTextField().setText(tool.getRunOnFail());
			this.texts[ITEM_RUN_ON_SUCCESS].getTextField().setText(tool.getRunOnSuccess());
			this.texts[ITEM_RUN_ON_EXIT].getTextField().setText(tool.getRunOnExit());
		}
	}

	private void apply() {
		this.currentTool.setName(this.texts[ITEM_NAME].getTextField().getText());
		this.currentTool.setVersion(this.texts[ITEM_VERSION].getTextField().getText());
		this.currentTool.setQuestions(this.texts[ITEM_QUESTIONS].getTextField().getText());
		this.currentTool.getPriority().clear();
		this.currentTool.getPriority().addAll(this.priorityTable.getItems());
		this.currentTool.setPath(this.texts[ITEM_PATH].getTextField().getText());

		this.currentTool.setExecute(new Execute());
		if (this.radioBtnExternal.isSelected()) {
			this.currentTool.setExternal(true);
		} else {
			this.currentTool.setExternal(false);
		}
		if (this.currentTool.isExternal()) {
			this.currentTool.getExecute().setUrl(this.texts[ITEM_URL].getTextField().getText());
			this.currentTool.getExecute().setUsername(this.texts[ITEM_USERNAME].getTextField().getText());
			this.currentTool.getExecute().setPassword(this.texts[ITEM_PASSWORD].getTextField().getText());
		} else {
			this.currentTool.getExecute().setRun(this.texts[ITEM_RUN].getTextField().getText());
			this.currentTool.getExecute().setResult(this.texts[ITEM_RESULT].getTextField().getText());
			this.currentTool.getExecute()
					.setInstances(Integer.parseInt(this.texts[ITEM_INSTANCES].getTextField().getText()));
			this.currentTool.getExecute().setMemoryPerInstance(
					Integer.parseInt(this.texts[ITEM_MEMORY_PER_INSTANCE].getTextField().getText()));
		}

		this.currentTool.setRunOnEntry(this.texts[ITEM_RUN_ON_ENTRY].getTextField().getText());
		this.currentTool.setRunOnAbort(this.texts[ITEM_RUN_ON_ABORT].getTextField().getText());
		this.currentTool.setRunOnFail(this.texts[ITEM_RUN_ON_FAIL].getTextField().getText());
		this.currentTool.setRunOnSuccess(this.texts[ITEM_RUN_ON_SUCCESS].getTextField().getText());
		this.currentTool.setRunOnExit(this.texts[ITEM_RUN_ON_EXIT].getTextField().getText());

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

	private static String typeToToolTip(int type, int number) {
		if (type == Overview.TYPE_TOOL) {
			return TOOLTIP_TOOL;
		} else if (type == Overview.TYPE_PREPROCESSOR) {
			return TOOLTIP_PREPROCESSOR;
		} else if (type == Overview.TYPE_OPERATOR) {
			return TOOLTIP_OPERATOR;
		} else if (type == Overview.TYPE_CONVERTER) {
			if (number == 1) {
				return TOOLTIP_CONVERTER_1;
			} else {
				return TOOLTIP_CONVERTER_2;
			}
		} else {
			return "UNKNOWN TYPE";
		}
	}

	private void hide(int id) {
		if (this.labels[id] != null) {
			this.labels[id].setVisible(false);
			this.labels[id].setManaged(false);
		}
		if (this.texts[id] != null) {
			this.texts[id].setVisible(false);
			this.texts[id].getTextField().clear();
			this.texts[id].setManaged(false);
		}
		if (this.helpPane[id] != null) {
			this.helpPane[id].setVisible(false);
			this.helpPane[id].setManaged(false);
		}
	}

	private void show(int id) {
		if (this.labels[id] != null) {
			this.labels[id].setVisible(true);
			this.labels[id].setManaged(true);
		}
		if (this.texts[id] != null) {
			this.texts[id].setVisible(true);
			this.texts[id].setManaged(true);
		}
		if (this.helpPane[id] != null) {
			this.helpPane[id].setVisible(true);
			this.helpPane[id].setManaged(true);
		}
	}
}
