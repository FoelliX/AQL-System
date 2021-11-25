package de.foellix.aql.config.wizard;

import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.system.IAnswerAvailable;
import de.foellix.aql.ui.gui.ViewerXML;
import javafx.application.Platform;

public class EditorXML extends ViewerXML implements IAnswerAvailable {
	private String content;

	public EditorXML(final ConfigWizard parent) {
		super();

		setContent(ConfigHandler.toXML(parent.getConfig()));
	}

	public void setContent(final String content) {
		if (content != null) {
			this.content = content;
			Platform.runLater(() -> {
				super.codeArea.clear();
				super.codeArea.insertText(0, content);
				this.content = null;
				Platform.runLater(() -> {
					super.codeArea.scrollYToPixel(0);
				});
			});
		}
	}

	@Override
	public String getContent() {
		if (this.content != null) {
			return this.content;
		} else {
			return super.getContent();
		}
	}
}
