package de.foellix.aql.ui.gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class LoadingDialog extends Stage {
	private boolean done;

	private String text;

	public LoadingDialog(String title) {
		this.setTitle(title);
		this.getIcons().add(new Image("file:data/gui/images/icon_16.png", 16, 16, false, true));
		this.getIcons().add(new Image("file:data/gui/images/icon_32.png", 32, 32, false, true));
		this.getIcons().add(new Image("file:data/gui/images/icon_64.png", 64, 64, false, true));
		this.resizableProperty().setValue(false);
		this.setOnCloseRequest(e -> e.consume());

		final GridPane mainPane = new GridPane();
		mainPane.setHgap(10);
		mainPane.setVgap(10);
		mainPane.setPadding(new Insets(10, 10, 10, 10));
		final ProgressIndicator circleOfDoom = new ProgressIndicator();
		final Label labelStatus1 = new Label("Loading.");
		mainPane.add(circleOfDoom, 0, 0);
		mainPane.add(labelStatus1, 1, 0);

		this.done = false;
		new Thread(() -> {
			try {
				int i = 1;
				while (!this.done) {
					if (i == 3) {
						i = 1;
					} else {
						i++;
					}
					this.text = "Loading";
					for (int o = 0; o < i; o++) {
						this.text += ".";
					}
					Platform.runLater(() -> {
						labelStatus1.setText(this.text);
					});
					Thread.sleep(400);
				}
			} catch (final InterruptedException e) {
				// do nothing
			}
		}).start();

		final Scene scene = new Scene(mainPane, 400, 70);
		scene.getStylesheets().add("file:data/gui/style.css");
		this.setScene(scene);
		this.show();
	}

	public boolean isDone() {
		return this.done;
	}

	public void setDone(boolean done) {
		this.done = done;
		if (done) {
			this.hide();
		}
	}
}