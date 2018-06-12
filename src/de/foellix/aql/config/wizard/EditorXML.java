package de.foellix.aql.config.wizard;

import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.system.IAnswerAvailable;
import de.foellix.aql.ui.gui.ViewerXML;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public class EditorXML extends ViewerXML implements IAnswerAvailable {
	public EditorXML(final ConfigWizard parent) {
		super();

		setContent(ConfigHandler.toXML(parent.getConfig()));

		super.codeArea.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(final ObservableValue<? extends String> observable, final String oldValue,
					final String newValue) {
				parent.syncOverview();
			}
		});
	}

	public void setContent(final String content) {
		if (content != null) {
			Platform.runLater(() -> {
				super.codeArea.replaceText(content);
				super.codeArea.scrollYBy(0);
			});
		}
	}
}
