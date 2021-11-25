package de.foellix.aql.ui.gui.viewer.web;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import com.sun.javafx.webkit.WebConsoleListener;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.HashHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.IAnswerAvailable;
import de.foellix.aql.ui.gui.MenubarViewer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import kong.unirest.json.JSONException;
import kong.unirest.json.JSONObject;

public class ViewerWeb extends BorderPane implements IAnswerAvailable {
	public static final File TEMP_DIRECTORY = new File("data/gui/web/temp");
	private static final File INDEX_FILE = new File("data/gui/web/index.html");
	private static final File EMPTY_FILE = new File(TEMP_DIRECTORY, "empty.html");

	public static List<ViewerWeb> allViewerWeb = new ArrayList<>();

	private static String TEMPLATE;
	private static final String NEEDLE = "%JSON_DATA%";
	private static String CANVAS_NOT_SUPPORTED = "TypeError: canvas.getContext is not a function. (In 'canvas.getContext('2d')', 'canvas.getContext' is undefined)";

	private MenubarViewer statsbar;

	private File tempGraphFile;
	private WebView wv;
	private WebRepresentation webAnswer;
	private boolean considerLinenumbers;

	public ViewerWeb() {
		this.statsbar = null;
		init();
	}

	public ViewerWeb(MenubarViewer statsbar) {
		this.statsbar = statsbar;
		init();
	}

	private void init() {
		this.considerLinenumbers = true;

		// Load template
		try {
			TEMPLATE = new String(Files.readAllBytes(INDEX_FILE.toPath()));
		} catch (final IOException e) {
			Log.error("Could not read template file: " + INDEX_FILE.getAbsolutePath() + Log.getExceptionAppendix(e));
		}

		// Initialize
		if (!TEMP_DIRECTORY.exists()) {
			TEMP_DIRECTORY.mkdirs();
		}

		allViewerWeb.add(this);
		this.wv = new WebView();
		this.wv.getEngine().setJavaScriptEnabled(true);
		this.setCenter(this.wv);

		WebConsoleListener.setDefaultListener((webView1, message, lineNumber, sourceId) -> {
			Log.msg("WebView-Debugger: [" + sourceId + ":" + lineNumber + "] " + message, Log.DEBUG_DETAILED);
			if (message.equals(CANVAS_NOT_SUPPORTED)) {
				for (final ViewerWeb viewer : allViewerWeb) {
					viewer.showWarning();
				}
			}
		});

		this.tempGraphFile = EMPTY_FILE;
		refresh(null);

		this.webAnswer = new WebRepresentation();
	}

	public void showWarning() {
		final VBox box = new VBox(10);
		box.setAlignment(Pos.CENTER);
		final ImageView iv = new ImageView("/com/sun/javafx/scene/control/skin/modena/dialog-warning.png");
		final Label warning = new Label(
				"The web representation seems to be incompatible with your OS or Java installation.");
		final Button btnOpenInBrowser = new Button("Open in Browser");
		btnOpenInBrowser.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(final ActionEvent event) {
				inBrowser();
			}
		});
		box.getChildren().addAll(iv, warning, btnOpenInBrowser);
		this.setCenter(box);
	}

	private void refresh(Answer answer) {
		try {
			final FileWriter fw = new FileWriter(this.tempGraphFile);
			final BufferedWriter writer = new BufferedWriter(fw);
			try {
				if (answer != null && !Helper.isEmpty(answer)) {
					final String jsonContent = new JSONObject(this.webAnswer.toJson(answer, this.considerLinenumbers))
							.toString(4);
					writer.write(TEMPLATE.replace(NEEDLE, jsonContent));
				}
			} catch (final JSONException e) {
				// Do nothing
			}
			writer.close();
			fw.close();
		} catch (final IOException e) {
			Log.warning("Cannot write graph file: " + this.tempGraphFile.getAbsolutePath());
		}

		this.wv.getEngine().load(this.tempGraphFile.toURI().toString());
	}

	public WebView getWebView() {
		return this.wv;
	}

	@Override
	public void answerAvailable(Object answer, int status) {
		if (answer instanceof Answer) {
			final Answer castedAnswer = (Answer) answer;
			Platform.runLater(() -> {
				if (castedAnswer != null && !Helper.isEmpty(castedAnswer)) {
					if (this.tempGraphFile != null && this.tempGraphFile.exists()) {
						this.tempGraphFile.delete();
					}
					this.tempGraphFile = new File(TEMP_DIRECTORY,
							HashHelper.sha1Hash(AnswerHandler.createXMLString(castedAnswer)) + ".html");
				} else {
					this.tempGraphFile = EMPTY_FILE;
				}
				refresh(castedAnswer);
				if (this.statsbar != null) {
					this.statsbar.refresh(castedAnswer);
				}
			});
		}
	}

	public void inBrowser() {
		final Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				if (this.tempGraphFile == EMPTY_FILE && !EMPTY_FILE.exists()) {
					refresh(null);
				}
				final URI uri = this.tempGraphFile.toURI();
				Log.msg("Opening in browser: " + uri + (Log.logIt(Log.DEBUG)
						? " (On some Linux implementations this does not work - the program will hang - see: https://stackoverflow.com/questions/27879854/desktop-getdesktop-browse-hangs)"
						: ""), Log.NORMAL);
				desktop.browse(uri);
			} catch (final Exception e) {
				Log.msg("Could not open answer in browser." + Log.getExceptionAppendix(e), Log.NORMAL);
			}
		} else {
			Log.error("Could not find any supported browser.");
		}
	}

	public void setConsiderLinenumbers(boolean considerLinenumbers) {
		this.considerLinenumbers = considerLinenumbers;
	}
}