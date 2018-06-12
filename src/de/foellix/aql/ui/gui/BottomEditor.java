package de.foellix.aql.ui.gui;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;

public class BottomEditor extends TitledPane {
	private static TextArea logArea = new TextArea();

	BottomEditor() {
		super("Log", logArea);

		logArea.setEditable(false);

		this.setExpanded(false);
	}

	public static void log(final String newLine) {
		Platform.runLater(() -> logArea.appendText(newLine + "\n"));
	}
}
