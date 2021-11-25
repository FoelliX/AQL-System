package de.foellix.aql.ui.gui;

import de.foellix.aql.config.wizard.ConfigWizard;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

public class ToolsetFile extends HBox {
	public ToolsetFile(final IGUI parent) {
		this(parent, null);
	}

	public ToolsetFile(final IGUI parent, MenuFile menuFile) {
		super();

		if ((parent instanceof GUI) || (parent instanceof ConfigWizard)) {
			this.setSpacing(4);

			final Button btnNew = FontAwesome.getInstance().createButton(FontAwesome.ICON_FILE);
			btnNew.setTooltip(new Tooltip(StringConstants.STR_NEW));
			btnNew.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent event) {
					parent.newFile();
				}
			});
			final Button btnOpen = FontAwesome.getInstance().createButton(FontAwesome.ICON_FOLDER_OPEN_ALT);
			btnOpen.setTooltip(new Tooltip(StringConstants.STR_OPEN));
			btnOpen.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent event) {
					parent.open();
					if (menuFile != null) {
						menuFile.refreshRecentFiles();
					}
				}
			});
			final Button btnSave = FontAwesome.getInstance().createButton(FontAwesome.ICON_SAVE);
			btnSave.setTooltip(new Tooltip(StringConstants.STR_SAVE));
			btnSave.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(final ActionEvent event) {
					parent.save();
					if (menuFile != null) {
						menuFile.refreshRecentFiles();
					}
				}
			});

			this.getChildren().addAll(btnNew, btnOpen, btnSave);
		}
	}
}
