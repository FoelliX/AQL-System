package de.foellix.aql.config.wizard;

import java.util.List;
import java.util.Optional;

import de.foellix.aql.config.Tool;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class ToolTableView extends ScrollPane {
	private final Overview parent;

	private final ObservableList<Tool> tools;

	public ToolTableView(final Overview parent, final int type) {
		super();
		this.setVbarPolicy(ScrollBarPolicy.NEVER);
		final TableView<Tool> root = new TableView<>();
		super.setContent(root);

		this.parent = parent;

		this.tools = FXCollections.observableArrayList();
		root.setItems(this.tools);

		final TableColumn<Tool, String> colName = new TableColumn<Tool, String>("Name");
		colName.setCellValueFactory(new PropertyValueFactory<Tool, String>("name"));
		colName.setPrefWidth(150);

		final TableColumn<Tool, String> colVersion = new TableColumn<Tool, String>("Version");
		colVersion.setCellValueFactory(new PropertyValueFactory<Tool, String>("version"));
		colVersion.setPrefWidth(150);

		final TableColumn<Tool, String> colQuestion = new TableColumn<Tool, String>(Overview.typeToString(type));
		colQuestion.setCellValueFactory(new PropertyValueFactory<Tool, String>("questions"));
		colQuestion.setPrefWidth(150);

		root.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		root.setEditable(true);
		root.getColumns().addAll(colName, colVersion, colQuestion);
		this.setOnKeyPressed((event) -> {
			if (event.getCode() == KeyCode.DELETE) {
				delete(root.getSelectionModel().getSelectedItem());
			}
			if (event.getCode() != KeyCode.UP && event.getCode() != KeyCode.DOWN) {
				event.consume();
			}
		});
		this.addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent event) {
				if (event.getButton() == MouseButton.SECONDARY) {
					event.consume();
					delete(root.getSelectionModel().getSelectedItem());
				} else if (event.getButton() == MouseButton.PRIMARY) {
					parent.edit(root.getSelectionModel().getSelectedItem(), type);
				}
			}
		});
		root.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				this.parent.edit(newSelection, type);
			}
		});
	}

	private void delete(final Tool tool) {
		final Alert alert = new Alert(AlertType.CONFIRMATION);

		final Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons().add(new Image("file:data/gui/images/icon_16.png", 16, 16, false, true));
		alertStage.getIcons().add(new Image("file:data/gui/images/icon_32.png", 32, 32, false, true));
		alertStage.getIcons().add(new Image("file:data/gui/images/icon_64.png", 64, 64, false, true));
		alert.setTitle("Remove");
		alert.setHeaderText("The following tool will be removed:\n" + tool.getName() + " (" + tool.getVersion() + ")");
		alert.setContentText("Proceed?");

		final Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			this.tools.remove(tool);
			if (this.parent.getParentGUI().getConfig().getTools() != null
					&& this.parent.getParentGUI().getConfig().getTools().getTool().contains(tool)) {
				this.parent.getParentGUI().getConfig().getTools().getTool().remove(tool);
			}
			if (this.parent.getParentGUI().getConfig().getPreprocessors() != null
					&& this.parent.getParentGUI().getConfig().getPreprocessors().getTool().contains(tool)) {
				this.parent.getParentGUI().getConfig().getPreprocessors().getTool().remove(tool);
			}
			if (this.parent.getParentGUI().getConfig().getOperators() != null
					&& this.parent.getParentGUI().getConfig().getOperators().getTool().contains(tool)) {
				this.parent.getParentGUI().getConfig().getOperators().getTool().remove(tool);
			}
			if (this.parent.getParentGUI().getConfig().getConverters() != null
					&& this.parent.getParentGUI().getConfig().getConverters().getTool().contains(tool)) {
				this.parent.getParentGUI().getConfig().getConverters().getTool().remove(tool);
			}
			this.parent.apply();
		} else {
			alert.hide();
		}
	}

	public void sync(final List<Tool> tools) {
		this.tools.clear();
		if (tools != null) {
			this.tools.addAll(tools);
		}
	}
}
