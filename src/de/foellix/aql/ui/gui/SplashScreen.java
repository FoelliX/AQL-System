package de.foellix.aql.ui.gui;

import java.io.File;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SplashScreen extends Stage {
	private boolean done;

	public SplashScreen(String title, String version, Color color) {
		this.setTitle(title);
		this.getIcons().add(new Image("file:data/gui/images/icon_16.png", 16, 16, false, true));
		this.getIcons().add(new Image("file:data/gui/images/icon_32.png", 32, 32, false, true));
		this.getIcons().add(new Image("file:data/gui/images/icon_64.png", 64, 64, false, true));
		this.initStyle(StageStyle.TRANSPARENT);

		final BorderPane mainPane = new BorderPane();
		mainPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0);");
		mainPane.setPadding(new Insets(20, 20, 20, 20));
		final BorderPane splash = new BorderPane();
		splash.setPadding(new Insets(10, 10, 10, 10));
		final BackgroundImage bg = new BackgroundImage(
				new Image(new File("data/gui/images/splash.png").toURI().toString()), BackgroundRepeat.REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT);
		splash.setBackground(new Background(bg));
		final DropShadow shadow = new DropShadow();
		shadow.setRadius(17.0);
		shadow.setOffsetX(3.0);
		shadow.setOffsetY(3.0);
		splash.setEffect(shadow);
		final ProgressBar progressBar = new ProgressBar(-1);
		progressBar.setPrefWidth(Integer.MAX_VALUE);
		splash.setBottom(progressBar);
		final BorderPane titlePane = new BorderPane();
		final Text titleLabel = new Text(title);
		titleLabel.setFill(color);
		titlePane.setLeft(titleLabel);
		final Text versionLabel = new Text(version);
		versionLabel.setFill(color);
		titlePane.setRight(versionLabel);
		splash.setTop(titlePane);
		mainPane.setCenter(splash);

		final Scene scene = new Scene(mainPane, 840, 540);
		scene.getStylesheets().add("file:data/gui/style.css");
		scene.setFill(Color.TRANSPARENT);
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