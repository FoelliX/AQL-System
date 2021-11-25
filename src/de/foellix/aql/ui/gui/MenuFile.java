package de.foellix.aql.ui.gui;

import java.io.File;
import java.util.List;

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
	private static final int FILENAME_MAX_LENGTH = 75;

	private IGUI parent;
	private boolean isEditor;
	private Menu menuItemOpenRecent;

	public MenuFile(final IGUI parent) {
		this(parent, false);
	}

	public MenuFile(final IGUI parent, boolean isEditor) {
		super(StringConstants.STR_FILE);

		this.parent = parent;
		this.isEditor = isEditor;

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
			final MenuItem menuItemOpen = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_FOLDER_OPEN_ALT,
					StringConstants.STR_OPEN);
			menuItemOpen.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
			menuItemOpen.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent event) {
					parent.open();
					refreshRecentFiles();
				}
			});
			this.menuItemOpenRecent = FontAwesome.getInstance().createInnerMenu(FontAwesome.ICON_FOLDER_OPEN_ALT,
					StringConstants.STR_OPEN_RECENT);
			refreshRecentFiles();
			final MenuItem menuItemSave = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_SAVE,
					StringConstants.STR_SAVE);
			menuItemSave.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
			menuItemSave.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent event) {
					parent.save();
					refreshRecentFiles();
				}
			});
			final MenuItem menuItemSaveAs = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_SAVE,
					StringConstants.STR_SAVE_AS);
			menuItemSaveAs.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent event) {
					parent.saveAs();
					refreshRecentFiles();
				}
			});
			final MenuItem menuItemResetGuiConfig = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_TRASH,
					StringConstants.STR_RESET_GUI_CONFIGURATION);
			menuItemResetGuiConfig.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent event) {
					Storage.getInstance().resetGuiConfig();
				}
			});
			final MenuItem menuItemExit = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_OFF,
					StringConstants.STR_EXIT);
			menuItemExit.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent event) {
					parent.exit();
				}
			});
			this.getItems().addAll(menuItemNew, new SeparatorMenuItem(), menuItemOpen);
			if (this.parent instanceof GUI) {
				this.getItems().add(this.menuItemOpenRecent);
			}
			this.getItems().addAll(new SeparatorMenuItem(), menuItemSave, menuItemSaveAs, new SeparatorMenuItem());
			this.getItems().addAll(new BackupAndResetMenu());
			this.getItems().addAll(menuItemResetGuiConfig, new SeparatorMenuItem(), menuItemExit);
		}
	}

	public void refreshRecentFiles() {
		if (this.parent instanceof GUI) {
			this.menuItemOpenRecent.getItems().clear();
			List<File> fileList;
			if (this.isEditor) {
				fileList = Storage.getInstance().getRecentFiles(Storage.TYPE_QUERIES);
			} else {
				fileList = Storage.getInstance().getRecentFiles(Storage.TYPE_ANSWERS);
			}
			for (final File file : fileList) {
				String text = file.getAbsolutePath();
				if (text.length() > FILENAME_MAX_LENGTH + 3 + 5) { // +3 for "...", +5 to actually save anything
					text = text.substring(0, 20) + "..." + text.substring(text.length() - (FILENAME_MAX_LENGTH - 20));
				}
				final MenuItem newMenuItem = FontAwesome.getInstance().createMenuItem(null, text);
				newMenuItem.setOnAction(new EventHandler<ActionEvent>() {
					@Override
					public void handle(final ActionEvent event) {
						((GUI) MenuFile.this.parent).open(file);
						refreshRecentFiles();
					}
				});
				this.menuItemOpenRecent.getItems().add(newMenuItem);
			}
			if (this.menuItemOpenRecent.getItems().isEmpty()) {
				this.menuItemOpenRecent.setDisable(true);
			} else {
				this.menuItemOpenRecent.setDisable(false);
			}
		}
	}
}
