package de.foellix.aql.config.wizard;

import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.system.IAnswerAvailable;
import de.foellix.aql.ui.gui.ViewerXML;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class EditorXML extends ViewerXML implements IAnswerAvailable {
	private boolean syncActive;

	public EditorXML(final ConfigWizard parent) {
		super();

		this.syncActive = true;

		setContent(ConfigHandler.toXML(parent.getConfig()));

		super.codeArea.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(final ObservableValue<? extends String> observable, final String oldValue,
					final String newValue) {
				if (EditorXML.this.syncActive) {
					parent.syncOverview();
				}
			}
		});
	}

	public void setContent(final String content) {
		if (content != null) {
			Platform.runLater(() -> {
				this.syncActive = false;
				super.codeArea.clear();
				this.syncActive = true;
				super.codeArea.insertText(0, content);
				// super.codeArea.replaceText(content);
				super.codeArea.scrollYBy(0);
			});
		}
	}
}
