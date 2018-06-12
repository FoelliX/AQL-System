package de.foellix.aql.ui.gui;

import de.foellix.aql.config.wizard.ConfigWizard;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class MenuFile extends Menu {
	public MenuFile(final IGUI parent) {
		super(StringConstants.STR_FILE);

		if ((parent instanceof GUI) || (parent instanceof ConfigWizard)) {
			final MenuItem menuItemNew = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_FILE,
					StringConstants.STR_NEW);
			menuItemNew.setAccelerator(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN));
			menuItemNew.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent event) {
					parent.newFile();
				}
			});
			final SeparatorMenuItem seperator1 = new SeparatorMenuItem();
			final MenuItem menuItemOpen = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_FOLDER_OPEN_ALT,
					StringConstants.STR_OPEN);
			menuItemOpen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
			menuItemOpen.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent event) {
					parent.open();
				}
			});
			final SeparatorMenuItem seperator2 = new SeparatorMenuItem();
			final MenuItem menuItemSave = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_SAVE,
					StringConstants.STR_SAVE);
			menuItemSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
			menuItemSave.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent event) {
					parent.save();
				}
			});
			final MenuItem menuItemSaveAs = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_SAVE,
					StringConstants.STR_SAVE_AS);
			menuItemSaveAs.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent event) {
					parent.saveAs();
				}
			});
			final SeparatorMenuItem seperator3 = new SeparatorMenuItem();
			final MenuItem menuItemExit = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_OFF,
					StringConstants.STR_EXIT);
			menuItemExit.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent event) {
					parent.exit();
				}
			});
			this.getItems().addAll(menuItemNew, seperator1, menuItemOpen, seperator2, menuItemSave, menuItemSaveAs,
					seperator3);
			this.getItems().addAll(new BackupAndResetMenu());
			this.getItems().add(menuItemExit);
		}
	}
}
