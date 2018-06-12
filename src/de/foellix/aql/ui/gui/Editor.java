package de.foellix.aql.ui.gui;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import de.foellix.aql.Log;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

public class Editor extends BorderPane {
	private final GUI parent;

	private boolean stop;

	private final FileChooser openDialog;
	private File currentDir;
	private final CodeArea codeArea;

	private static final String[] KEYWORDS1 = new String[] { "IN", "FROM", "TO", "FEATURING" };
	private static final String[] KEYWORDS2 = new String[] { "UNIFY", "CONNECT", "FILTER" };
	private static final String[] KEYWORDS3 = new String[] { "Permissions", "Flows", "IntentSources", "IntentSinks",
			"Intents", "IntentFilters", "Statement", "Class", "Method", "App" };

	private static final String KEYWORD1_PATTERN = "\\b(" + String.join("|", KEYWORDS1) + ")\\b";
	private static final String KEYWORD2_PATTERN = "\\b(" + String.join("|", KEYWORDS2) + ")\\b";
	private static final String KEYWORD3_PATTERN = "\\b(" + String.join("|", KEYWORDS3) + ")\\b";
	private static final String PAREN1_PATTERN = "\\(|\\)";
	private static final String PAREN2_PATTERN = "\\[|\\]";
	private static final String QUESTION_PATTERN = "\\?|\\!";
	private static final String ARROW_PATTERN = "\\->";
	private static final String COMMA_PATTERN = "\\,|\\|";
	private static final String STRING_PATTERN = "'([^'\\\\]|\\\\.)*'";
	// private static final String COMMENT_PATTERN = "//[^\n]*" + "|" +
	// "/\\*(.|\\R)*?\\*/";

	private static final Pattern PATTERN = Pattern.compile("(?<KEYWORD1>" + KEYWORD1_PATTERN + ")" + "|(?<KEYWORD2>"
			+ KEYWORD2_PATTERN + ")" + "|(?<KEYWORD3>" + KEYWORD3_PATTERN + ")" + "|(?<PAREN1>" + PAREN1_PATTERN + ")"
			+ "|(?<PAREN2>" + PAREN2_PATTERN + ")" + "|(?<QUESTION>" + QUESTION_PATTERN + ")" + "|(?<ARROW>"
			+ ARROW_PATTERN + ")" + "|(?<COMMA>" + COMMA_PATTERN + ")" + "|(?<STRING>" + STRING_PATTERN + ")");

	private final MenubarEditor menuBar;

	public Editor(final GUI parent) {
		this.parent = parent;

		this.openDialog = new FileChooser();
		this.openDialog.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("*.* All files", "*.*"));

		this.codeArea = new CodeArea();
		this.codeArea.setWrapText(true);
		this.codeArea.setParagraphGraphicFactory(LineNumberFactory.get(this.codeArea));
		this.codeArea.richChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())).subscribe(change -> {
			this.codeArea.setStyleSpans(0, computeHighlighting(this.codeArea.getText()));
		});
		this.codeArea.setStyle("-fx-font-family: Consolas;");
		final StackPane codePane = new StackPane(new VirtualizedScrollPane<>(this.codeArea));
		codePane.setPrefWidth(Integer.MAX_VALUE);

		this.menuBar = new MenubarEditor(this);
		parent.system.getProgressListener().add(this.menuBar);
		this.setTop(this.menuBar);
		this.setBottom(new BottomEditor());
		this.setCenter(codePane);
	}

	private static StyleSpans<Collection<String>> computeHighlighting(final String text) {
		final Matcher matcher = PATTERN.matcher(text);
		int lastKwEnd = 0;
		final StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		while (matcher.find()) {
			final String styleClass = matcher.group("KEYWORD1") != null ? "keyword1"
					: matcher
							.group("KEYWORD2") != null
									? "keyword2"
									: matcher.group("KEYWORD3") != null ? "keyword3"
											: matcher.group("PAREN1") != null ? "paren1"
													: matcher.group("PAREN2") != null ? "paren2"
															: matcher.group("QUESTION") != null ? "question"
																	: matcher.group("ARROW") != null ? "arrow"
																			: matcher.group("COMMA") != null ? "comma"
																					: matcher.group("STRING") != null
																							? "string"
																							: null;
			/* never happens */ assert styleClass != null;
			spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
			spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
			lastKwEnd = matcher.end();
		}
		spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
		return spansBuilder.create();
	}

	public void undo() {
		this.codeArea.undo();
	}

	public void redo() {
		this.codeArea.redo();
	}

	public void autoformat() {
		Platform.runLater(() -> {
			String content = this.codeArea.getText();

			content = content.replaceAll("\n", "");
			content = content.replaceAll("\t", "");
			content = content.replaceAll("\\[", "%PLACEHOLDER_OPENING_BRACKET%");
			content = content.replaceAll("\\]", "%PLACEHOLDER_CLOSING_BRACKET%");
			content = content.replaceAll(", ", "%PLACEHOLDER_COMMA%");
			content = content.replaceAll(",", "%PLACEHOLDER_COMMA%");

			int tab = 0;
			while (content.contains("%PLACEHOLDER_OPENING_BRACKET%")
					|| content.contains("%PLACEHOLDER_CLOSING_BRACKET%") || content.contains("%PLACEHOLDER_COMMA%")) {
				final int i1 = content.indexOf("%PLACEHOLDER_OPENING_BRACKET%", 0);
				final int i2 = content.indexOf("%PLACEHOLDER_CLOSING_BRACKET%", 0);
				final int i3 = content.indexOf("%PLACEHOLDER_COMMA%", 0);

				if (i1 >= 0 && (i2 < 0 || i1 < i2) && (i3 < 0 || i1 < i3)) {
					tab++;
					String tabStr = "";
					for (int i = 0; i < tab; i++) {
						tabStr += "\t";
					}
					content = content.replaceFirst("%PLACEHOLDER_OPENING_BRACKET%", "[\n" + tabStr);
				} else if (i2 >= 0 && (i1 < 0 || i2 < i1) && (i3 < 0 || i2 < i3)) {
					tab--;
					String tabStr = "";
					for (int i = 0; i < tab; i++) {
						tabStr += "\t";
					}
					content = content.replaceFirst("%PLACEHOLDER_CLOSING_BRACKET%", "\n" + tabStr + "]");
				} else if (i3 >= 0 && (i1 < 0 || i3 < i1) && (i2 < 0 || i3 < i2)) {
					String tabStr = "";
					for (int i = 0; i < tab; i++) {
						tabStr += "\t";
					}
					content = content.replaceFirst("%PLACEHOLDER_COMMA%", ",\n" + tabStr);
				}
			}

			this.codeArea.replaceText(content);
		});
	}

	public void ask() {
		if (!this.stop) {
			this.parent.system.query(this.codeArea.getText());
		} else {
			this.parent.system.cancel();
		}
	}

	public void setContent(final String content) {
		Platform.runLater(() -> {
			this.codeArea.replaceText(content);
		});
		autoformat();
	}

	public void openFile(final File file) throws IOException {
		final byte[] encoded = Files.readAllBytes(Paths.get(file.toURI()));
		this.setContent(new String(encoded, StandardCharsets.UTF_8));
	}

	public GUI getParentGUI() {
		return this.parent;
	}

	public String getContent() {
		return this.codeArea.getText();
	}

	public void resetContent() {
		Platform.runLater(() -> {
			this.codeArea.clear();
		});
	}

	public void insertFilename() {
		Platform.runLater(() -> {
			String filename = "";

			// Get Filename
			if (this.currentDir != null) {
				this.openDialog.setInitialDirectory(this.currentDir);
			} else {
				this.openDialog.setInitialDirectory(new File("."));
			}
			final File file = this.openDialog.showOpenDialog(this.getParentGUI().stage);
			if (file != null) {
				try {
					filename = file.getAbsolutePath();
					this.currentDir = file.getParentFile();
				} catch (final Exception e) {
					Log.msg("File not found: " + file.toString(), Log.ERROR);
				}
			}

			// Paste
			final int caret = this.codeArea.getCaretPosition();

			final String before = this.codeArea.getText();
			final String after;
			if (caret >= before.length()) {
				after = before.substring(0) + filename;
			} else {
				after = before.substring(0, caret) + filename + before.substring(caret);
			}

			this.codeArea.replaceText(after);
			this.codeArea.requestFocus();
		});
	}

	public boolean isStop() {
		return this.stop;
	}

	public void setStop(final boolean stop) {
		this.stop = stop;
	}
}
