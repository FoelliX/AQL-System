package de.foellix.aql.ui.gui;

import java.util.ArrayList;

import de.foellix.aql.Log;
import de.foellix.aql.system.BackupAndReset;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class BackupAndResetMenu extends ArrayList<MenuItem> {
	private static final long serialVersionUID = -3960538885772359971L;

	public BackupAndResetMenu() {
		super();

		final MenuItem menuItemBackupStorage = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_STEP_BACKWARD,
				StringConstants.STR_BACKUP_STORAGE);
		menuItemBackupStorage.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				BackupAndReset.backup();
			}
		});
		final MenuItem menuItemResetStorage = FontAwesome.getInstance().createMenuItem(FontAwesome.ICON_BACKWARD,
				StringConstants.STR_RESET_STORAGE);
		menuItemResetStorage.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				BackupAndReset.reset();
			}
		});
		final MenuItem menuItemBackupAndResetStorage = FontAwesome.getInstance()
				.createMenuItem(FontAwesome.ICON_FAST_BACKWARD, StringConstants.STR_RESET_AND_BACKUP_STORAGE);
		menuItemBackupAndResetStorage.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				if (BackupAndReset.backup()) {
					BackupAndReset.reset();
				} else {
					Log.msg("Did not reset storage since backup was not successful.", Log.NORMAL);
				}
			}
		});

		this.addAll(menuItemBackupStorage, menuItemResetStorage, menuItemBackupAndResetStorage,
				new SeparatorMenuItem());
	}

	private void addAll(MenuItem... menuitems) {
		for (final MenuItem mi : menuitems) {
			this.add(mi);
		}
	}
}
