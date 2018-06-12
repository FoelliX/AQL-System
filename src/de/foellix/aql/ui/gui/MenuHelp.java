package de.foellix.aql.ui.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import de.foellix.aql.Log;
import de.foellix.aql.Properties;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.config.wizard.ConfigWizard;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MenuHelp extends Menu {
	private final MenuItem menuItemConfigWizard;
	private final SeparatorMenuItem separator;

	public MenuHelp(Stage stage) {
		super(StringConstants.STR_HELP);

		this.menuItemConfigWizard = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_MAGIC,
				StringConstants.STR_CONFIGWIZARD);
		this.menuItemConfigWizard.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				new ConfigWizard(stage, ConfigHandler.getInstance().getConfigFile()).show();
			}
		});

		this.separator = new SeparatorMenuItem();

		final MenuItem menuItemManual = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_PDF,
				StringConstants.STR_MANUAL);
		menuItemManual.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				if (Desktop.isDesktopSupported()) {
					try {
						final File myFile = new File("manual.pdf");
						Desktop.getDesktop().open(myFile);
					} catch (final IOException ex) {
						Log.error("Could not access manual.pdf or PDF Viewer.");
					}
				}
			}
		});
		final MenuItem menuItemInfo = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_INFO_SIGN,
				StringConstants.STR_INFO);
		menuItemInfo.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				final Alert alert = new Alert(AlertType.INFORMATION);
				final Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
				alertStage.getIcons().add(new Image("file:data/gui/images/icon_16.png", 16, 16, false, true));
				alertStage.getIcons().add(new Image("file:data/gui/images/icon_32.png", 32, 32, false, true));
				alertStage.getIcons().add(new Image("file:data/gui/images/icon_64.png", 64, 64, false, true));
				alert.setTitle("Information");
				alert.setHeaderText(Properties.info().ABBRRVIATION + "\n" + Properties.info().NAME);
				alert.setContentText("Version: " + Properties.info().VERSION + " (" + Properties.info().BUILDNUMBER
						+ ")\nDeveloped by: " + Properties.info().AUTHOR + " (" + Properties.info().AUTHOR_EMAIL
						+ ")\n\nGitHub: " + Properties.info().GITHUB_LINK);

				alert.showAndWait();

				alert.hide();
			}
		});
		this.getItems().addAll(this.menuItemConfigWizard, this.separator, menuItemManual, menuItemInfo);
	}

	public Menu removeConfigWizard() {
		this.getItems().removeAll(this.menuItemConfigWizard, this.separator);
		return this;
	}
}
