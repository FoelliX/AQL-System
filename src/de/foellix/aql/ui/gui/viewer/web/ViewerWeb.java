package de.foellix.aql.ui.gui.viewer.web;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONObject;

import com.sun.javafx.webkit.WebConsoleListener;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.HashHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.IAnswerAvailable;
import de.foellix.aql.ui.gui.MenubarViewer;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;

public class ViewerWeb extends BorderPane implements IAnswerAvailable {
	private static final File INDEX_FILE = new File("data/gui/web/index.html");
	private static final File JSON_TEMP_FOLDER = new File("data/gui/web/temp");

	private MenubarViewer statsbar;

	private File tempGraphFile;
	private WebView wv;
	private WebRepresentation webAnswer;

	public ViewerWeb() {
		this.statsbar = null;
		init();
	}

	public ViewerWeb(MenubarViewer statsbar) {
		this.statsbar = statsbar;
		init();
	}

	private void init() {
		if (!JSON_TEMP_FOLDER.exists()) {
			JSON_TEMP_FOLDER.mkdir();
		}

		this.wv = new WebView();
		this.wv.getEngine().setJavaScriptEnabled(true);
		this.setCenter(this.wv);

		if (Log.logIt(Log.DEBUG_DETAILED)) {
			WebConsoleListener.setDefaultListener((webView1, message, lineNumber, sourceId) -> Log
					.msg("WebView-Debugger: [" + sourceId + ":" + lineNumber + "] " + message, Log.DEBUG_DETAILED));
		}

		this.webAnswer = new WebRepresentation();
	}

	private void refresh(Answer answer) {
		final String json = this.webAnswer.toJson(answer);

		try {
			final FileWriter fw = new FileWriter(this.tempGraphFile);
			final BufferedWriter writer = new BufferedWriter(fw);
			if (answer != null && !Helper.isEmpty(answer)) {
				writer.write(new JSONObject(json).toString(4));
			} else {
				writer.write("{}");
			}
			writer.close();
			fw.close();

			this.wv.getEngine().load(INDEX_FILE.toURI().toURL().toExternalForm());
			this.wv.getEngine().getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {
				@Override
				public void changed(ObservableValue<? extends State> observable, State oldValue, State newValue) {
					if (newValue == State.SUCCEEDED) {
						ViewerWeb.this.wv.getEngine()
								.executeScript("load('" + ViewerWeb.this.tempGraphFile.getName() + "');");
					}
				}
			});
			this.wv.getEngine().reload();
		} catch (final IOException e) {
			Log.warning("Cannot write graph file: " + this.tempGraphFile.getAbsolutePath());
		}
	}

	public WebView getWebView() {
		return this.wv;
	}

	@Override
	public void answerAvailable(final Answer answer, int status) {
		Platform.runLater(() -> {
			if (answer != null && !Helper.isEmpty(answer)) {
				if (this.tempGraphFile != null && this.tempGraphFile.exists()) {
					this.tempGraphFile.delete();
				}
				this.tempGraphFile = new File(JSON_TEMP_FOLDER,
						HashHelper.sha1Hash(AnswerHandler.createXMLString(answer)) + ".json");
			} else {
				this.tempGraphFile = new File(JSON_TEMP_FOLDER, "empty.json");
			}
			refresh(answer);
			if (this.statsbar != null) {
				this.statsbar.refresh(answer);
			}
		});
	}
}
