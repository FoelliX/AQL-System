package de.foellix.aql.ui.gui;

import java.util.Optional;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ExitDialog {
	public ExitDialog(String title, String header, String content) {
		final Alert alert = new Alert(AlertType.CONFIRMATION);
		final Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
		alertStage.getIcons().add(new Image("file:data/gui/images/icon_16.png", 16, 16, false, true));
		alertStage.getIcons().add(new Image("file:data/gui/images/icon_32.png", 32, 32, false, true));
		alertStage.getIcons().add(new Image("file:data/gui/images/icon_64.png", 64, 64, false, true));
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);

		final Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
			Platform.exit();
		} else {
			alert.hide();
		}
	}
}
