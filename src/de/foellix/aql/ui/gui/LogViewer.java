package de.foellix.aql.ui.gui;

import de.foellix.aql.Log;
import de.foellix.aql.helper.GUIHelper;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

public class LogViewer extends TitledPane {
	private static ListView<LogMsg> logArea = new ListView<>();

	public LogViewer() {
		super();
		setText("Log");
		setContent(logArea);
		logArea.setCellFactory(item1 -> new LogViewerListViewCell());
		logArea.setStyle("-fx-font-family: Consolas;");
		logArea.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		logArea.setOnKeyPressed((e) -> {
			if (e.isControlDown() && (e.getCode() == KeyCode.C || e.getCode() == KeyCode.X)) {
				copyToClipboard();
			}
		});
		logArea.setOnMouseClicked((e) -> {
			if (e.getClickCount() == 2 || e.getButton() == MouseButton.SECONDARY) {
				copyToClipboard();
			}
		});
		GUIHelper.makeResizableInHeight(logArea, 200);
		this.setExpanded(false);
	}

	private void copyToClipboard() {
		final StringBuilder sb = new StringBuilder();
		for (final LogMsg msg : logArea.getSelectionModel().getSelectedItems()) {
			if (!sb.isEmpty()) {
				sb.append("\n");
			}
			sb.append(msg.getMsg());
		}
		GUIHelper.copyToClipboard(sb.toString());
	}

	public static void log(final LogMsg msg) {
		if (GUI.started) {
			String temp = Log.stripAnsi(msg.getMsg());
			while (temp.endsWith(" ") || temp.endsWith("\n") || temp.endsWith("\r") || temp.endsWith("\t")) {
				temp = temp.substring(0, temp.length() - 1);
			}
			while (temp.startsWith("\n") || temp.startsWith("\r")) {
				temp = temp.substring(1);
			}
			msg.setMsg(temp);

			Platform.runLater(() -> {
				logArea.getItems().add(0, msg);
				// TODO: (After 2.0.0 release) Check if scrolling works in future javafx versions (last tested with 18-ea+4; Part 1/2)
				// logArea.getItems().add(msg);
				// logArea.scrollTo(logArea.getItems().size() - 1);
			});
		}
	}

	private class LogViewerListViewCell extends ListCell<LogMsg> {
		@Override
		protected void updateItem(LogMsg item, boolean empty) {
			super.updateItem(item, empty);
			if (item != null) {
				setText(item.toString());
				setGraphic(getIcon(item.getType()));
			}
		}

		private Node getIcon(int type) {
			final Label icon;
			switch (type) {
				case LogMsg.TYPE_WARNING: {
					icon = new Label(FontAwesome.ICON_WARNING_SIGN);
					FontAwesome.applyFontAwesome(icon, "-fx-text-fill: #ffdd21; -fx-font-size: 15px;");
					break;
				}
				case LogMsg.TYPE_ERROR: {
					icon = new Label(FontAwesome.ICON_EXCLAMATION_SIGN);
					FontAwesome.applyFontAwesome(icon, "-fx-text-fill: #a40000; -fx-font-size: 15px;");
					break;
				}
				case LogMsg.TYPE_NOTE: {
					icon = new Label(FontAwesome.ICON_INFO_SIGN);
					FontAwesome.applyFontAwesome(icon, "-fx-text-fill: #00a3db; -fx-font-size: 15px;");
					break;
				}
				default: {
					icon = new Label("");
				}
			}
			icon.setPrefWidth(25);
			return icon;
		}
	}
}