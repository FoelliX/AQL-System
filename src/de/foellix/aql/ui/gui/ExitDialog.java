package de.foellix.aql.ui.gui;

import java.io.File;
import java.util.Optional;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ExitDialog {
	public ExitDialog(String title, String header, String content) {
		if (!GUI.started
				|| !Boolean.parseBoolean(Storage.getInstance().getGuiConfigProperty(Storage.PROPERTY_CONFIRM_EXIT))) {
			final Alert alert = new Alert(AlertType.CONFIRMATION);
			final Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
			alertStage.getIcons()
					.add(new Image(new File("data/gui/images/icon_16.png").toURI().toString(), 16, 16, false, true));
			alertStage.getIcons()
					.add(new Image(new File("data/gui/images/icon_32.png").toURI().toString(), 32, 32, false, true));
			alertStage.getIcons()
					.add(new Image(new File("data/gui/images/icon_64.png").toURI().toString(), 64, 64, false, true));
			alert.setTitle(title);
			alert.setHeaderText(header);

			CheckBox checkBox = null;
			if (GUI.started) {
				final VBox box = new VBox(10);
				box.getChildren().add(new Label(content));
				checkBox = new CheckBox("Do not ask again");
				box.getChildren().add(checkBox);
				alert.getDialogPane().setContent(box);
			} else {
				alert.setContentText(content);
			}

			final Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK) {
				if (checkBox != null) {
					Storage.getInstance().setGuiConfigProperty(Storage.PROPERTY_CONFIRM_EXIT,
							String.valueOf(checkBox.isSelected()));
				}
				Platform.exit();
			} else {
				alert.hide();
			}
		} else {
			Platform.exit();
		}
	}
}
