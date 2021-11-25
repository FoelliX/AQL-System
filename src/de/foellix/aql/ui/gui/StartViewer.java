package de.foellix.aql.ui.gui;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.IAnswerAvailable;
import de.foellix.aql.system.Options;
import javafx.application.Application;
import javafx.application.Platform;

public class StartViewer implements IAnswerAvailable {
	private String[] args;
	private Options options;
	private boolean started;

	public StartViewer(String[] args, Options options) {
		this.args = args;
		this.options = options;
		this.started = false;
	}

	@Override
	public void answerAvailable(Object answer, int status) {
		if (!this.started) {
			if (answer instanceof Answer) {
				this.started = true;

				GUI.options = this.options;
				new Thread() {
					@Override
					public void run() {
						try {
							while (!GUI.started && GUI.viewer == null) {
								sleep(100);
							}
							Platform.runLater(() -> {
								GUI.switchToViewer();
								GUI.viewer.openAnswer((Answer) answer);
							});
						} catch (final InterruptedException e) {
							Log.error("Execution was interrupted when waiting for GUI to start.");
						}
					}
				}.start();
				if (!this.options.getDrawGraphs()) {
					Application.launch(GUI.class, this.args);
				}
			} else {
				Log.warning("Viewer not started since given answer is not an AQL-Answer: " + Helper.toString(answer));
			}
		}
	}
}
