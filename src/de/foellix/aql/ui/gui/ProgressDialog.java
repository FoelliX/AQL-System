package de.foellix.aql.ui.gui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ProgressDialog extends Stage {
	private final ProgressBar progressBar1, progressBar2;
	private final Label labelStatus1, labelStatus2;

	public ProgressDialog() {
		this.setTitle("Loading...");
		this.getIcons().add(new Image("file:data/gui/images/icon_16.png", 16, 16, false, true));
		this.getIcons().add(new Image("file:data/gui/images/icon_32.png", 32, 32, false, true));
		this.getIcons().add(new Image("file:data/gui/images/icon_64.png", 64, 64, false, true));
		this.resizableProperty().setValue(false);
		this.setOnCloseRequest(e -> e.consume());

		final GridPane mainPane = new GridPane();
		mainPane.setHgap(10);
		mainPane.setVgap(10);
		mainPane.setPadding(new Insets(10, 10, 10, 10));
		final Label label1 = new Label("Total: ");
		this.progressBar1 = new ProgressBar();
		this.progressBar1.setPrefWidth(175);
		this.labelStatus1 = new Label("0% (0/0)");
		mainPane.add(label1, 0, 0);
		mainPane.add(this.progressBar1, 1, 0);
		mainPane.add(this.labelStatus1, 2, 0);
		final Label label2 = new Label("Current: ");
		this.progressBar2 = new ProgressBar();
		this.progressBar2.setPrefWidth(175);
		this.labelStatus2 = new Label("0% (0/0)");
		mainPane.add(label2, 0, 1);
		mainPane.add(this.progressBar2, 1, 1);
		mainPane.add(this.labelStatus2, 2, 1);

		final Scene scene = new Scene(mainPane, 400, 70);
		scene.getStylesheets().add("file:data/gui/style.css");
		this.setScene(scene);
		this.show();
	}

	public void updateProgress(int currentDone, int currentMax, int totalDone, int totalMax) {
		Platform.runLater(() -> {
			this.progressBar1.setProgress((double) totalDone / (double) totalMax);
			this.labelStatus1.setText((int) Math.floor(((double) totalDone / (double) totalMax) * 100d) + "% ("
					+ totalDone + "/" + totalMax + ")");
			this.progressBar2.setProgress((double) currentDone / (double) currentMax);
			this.labelStatus2.setText(Math.floor(((double) currentDone / (double) currentMax) * 100d) + "% ("
					+ currentDone + "/" + currentMax + ")");
			if (totalDone >= totalMax) {
				this.hide();
			}
		});
	}
}