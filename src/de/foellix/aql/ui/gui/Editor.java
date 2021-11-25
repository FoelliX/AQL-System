package de.foellix.aql.ui.gui;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.flowless.ScaledVirtualized;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.handler.QueryHandler;
import de.foellix.aql.datastructure.query.Query;
import de.foellix.aql.helper.EqualSymbolsComparator;
import de.foellix.aql.helper.GUIHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.helper.KeywordsAndConstantsHelper;
import de.foellix.aql.helper.LevenshteinComparator;
import de.foellix.aql.system.defaulttools.operators.DefaultOperator;
import de.foellix.aql.system.task.Task;
import de.foellix.aql.system.task.gui.TaskTreeViewer;
import de.foellix.aql.transformations.QueryTransformer;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Popup;

public class Editor extends BorderPane {
	public static final double ZOOM_FACTOR = 0.9d;
	public static final double ZOOM_LOWER_BOUND = 0.7d;
	public static final double ZOOM_UPPER_BOUND = 6d;

	private GUI parent;

	private boolean ready;
	private boolean stop;

	private FileChooser openDialog;
	private File currentDir;
	private CodeArea codeArea;
	private ScaledVirtualized<CodeArea> codeZoomPane;
	private StackPane codePane;
	private Popup popup;
	private ListView<AutoCompletionEntry> popupList;
	private int[] popupPart = new int[2];
	private boolean popupReady = true;
	private int popupCounter = 0;
	private int lastCaretPos = -1;
	private boolean topSelected;

	private static final String[] KEYWORDS1 = new String[] { "IN", "FROM", "TO", "FEATURES", "FEATURING", "USES",
			"USING", "WITH" };
	private static final String[] KEYWORDS3 = new String[] { KeywordsAndConstantsHelper.SOI_ARGUMENTS,
			KeywordsAndConstantsHelper.SOI_FLOWS, KeywordsAndConstantsHelper.SOI_INTENTFILTERS,
			KeywordsAndConstantsHelper.SOI_INTENTS, KeywordsAndConstantsHelper.SOI_INTENTSINKS,
			KeywordsAndConstantsHelper.SOI_INTENTSOURCES, KeywordsAndConstantsHelper.SOI_PERMISSIONS,
			KeywordsAndConstantsHelper.SOI_SINKS, KeywordsAndConstantsHelper.SOI_SLICE,
			KeywordsAndConstantsHelper.SOI_SOURCES, "Statement", "Class", "Method", "App" };
	private static final String[] AUTOCOMPLETION_START_SYMBOLS = new String[] { " ", "\t", "\n", "'", "(", "[", "{",
			"-", ">", ",", ".", "!", "?" };
	private static final String[] AUTOCOMPLETION_END_SYMBOLS = new String[] { " ", "\t", "\n", "'", ")", "]", "}", ".",
			"!", "?" };
	private static final String SUBJECT_OF_INTEREST = "Subject of interest";
	private static final String FOLLOWING_REFERENCE = "following reference";
	private static final String PART_OF_A_REFERENCE = "part of a Reference";
	private static AutoCompletionEntry[] AUTOCOMPLETION_KEYWORDS = null;
	private static AutoCompletionEntry AUTOCOMPLETION_INSERT_FILE = null;
	private static AutoCompletionEntry AUTOCOMPLETION_ENDING_SYMBOL_AQL = null;
	private static AutoCompletionEntry AUTOCOMPLETION_ENDING_SYMBOL_FILE = null;
	private static AutoCompletionEntry AUTOCOMPLETION_ENDING_SYMBOL_RAW = null;
	private static List<String> AUTOCOMPLETION_LAST_INSERTED_FILES = new LinkedList<>();

	private static final String KEYWORD1_PATTERN = "\\b(" + String.join("|", KEYWORDS1) + ")\\b";
	private static final String KEYWORD2_PATTERN = "([A-Za-z0-9~]+\s*\\[)"; // "\\b(" + String.join("|", KEYWORDS2) + ")\\b";
	private static final String KEYWORD3_PATTERN = "\\b(" + String.join("|", KEYWORDS3) + ")\\b";
	private static final String PAREN1_PATTERN = "\\(|\\)";
	private static final String PAREN2_PATTERN = "\\[|\\]";
	private static final String PAREN3_PATTERN = "\\{|\\}";
	private static final String QUESTION_PATTERN = "\\?|\\!|\\.";
	private static final String ARROW_PATTERN = "\\->";
	private static final String COMMA_PATTERN = "\\,|\\||=";
	private static final String STRING_PATTERN = "'([^'\\\\]|\\\\.)*'";
	private static final String VAR1_PATTERN = "([A-Za-z0-9]+\s*=)";
	private static final String VAR2_PATTERN = "\\$([^ \\)\\]]*)"; // "$([^)\\\\]|\\\\.)*'";
	// private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";

	private static final Pattern PATTERN = Pattern
			.compile("(?<KEYWORD1>" + KEYWORD1_PATTERN + ")" + "|(?<KEYWORD2>" + KEYWORD2_PATTERN + ")"
					+ "|(?<KEYWORD3>" + KEYWORD3_PATTERN + ")" + "|(?<PAREN1>" + PAREN1_PATTERN + ")" + "|(?<PAREN2>"
					+ PAREN2_PATTERN + ")" + "|(?<PAREN3>" + PAREN3_PATTERN + ")" + "|(?<QUESTION>" + QUESTION_PATTERN
					+ ")" + "|(?<ARROW>" + ARROW_PATTERN + ")" + "|(?<COMMA>" + COMMA_PATTERN + ")" + "|(?<STRING>"
					+ STRING_PATTERN + ")" + "|(?<VAR1>" + VAR1_PATTERN + ")" + "|(?<VAR2>" + VAR2_PATTERN + ")");

	private MenubarEditor menuBar;

	public Editor() {
		this.ready = false;
	}

	public Editor(final GUI parent) {
		this();
		init(parent);
	}

	public void init(final GUI parent) {
		this.parent = parent;

		if (AUTOCOMPLETION_KEYWORDS == null) {
			AUTOCOMPLETION_KEYWORDS = new AutoCompletionEntry[] {
					new AutoCompletionEntry(DefaultOperator.OPERATOR_CONNECT, "connects the given answers", " [  ] ?"),
					new AutoCompletionEntry(DefaultOperator.OPERATOR_FILTER, "filter the given answer", " [  ] ?"),
					new AutoCompletionEntry(DefaultOperator.OPERATOR_INTERSECT, "intersects the given answers",
							" [  ] ?"),
					new AutoCompletionEntry(DefaultOperator.OPERATOR_MINUS, "answer 1 without answer 2", " [  ] ?"),
					new AutoCompletionEntry(DefaultOperator.OPERATOR_UNIFY, "unifies the given answers", " [  ] ?"),
					new AutoCompletionEntry(DefaultOperator.OPERATOR_SIGN, "signs the given APK file", " [  ] !"),
					new AutoCompletionEntry(DefaultOperator.OPERATOR_TOAD,
							"transforms Sources&Sinks to Amandroid's format", " [  ] !"),
					new AutoCompletionEntry(DefaultOperator.OPERATOR_TOFD,
							"transforms Sources&Sinks to FlowDroid's format", " [  ] !"),
					new AutoCompletionEntry(KeywordsAndConstantsHelper.SOI_ARGUMENTS, "Subject of interest", " "),
					new AutoCompletionEntry(KeywordsAndConstantsHelper.SOI_FLOWS, "Subject of interest", " "),
					new AutoCompletionEntry(KeywordsAndConstantsHelper.SOI_INTENTFILTERS, "Subject of interest", " "),
					new AutoCompletionEntry(KeywordsAndConstantsHelper.SOI_INTENTS, "Subject of interest", " "),
					new AutoCompletionEntry(KeywordsAndConstantsHelper.SOI_INTENTSINKS, "Subject of interest", " "),
					new AutoCompletionEntry(KeywordsAndConstantsHelper.SOI_INTENTSOURCES, "Subject of interest", " "),
					new AutoCompletionEntry(KeywordsAndConstantsHelper.SOI_PERMISSIONS, "Subject of interest", " "),
					new AutoCompletionEntry(KeywordsAndConstantsHelper.SOI_SINKS, "Subject of interest", " "),
					new AutoCompletionEntry(KeywordsAndConstantsHelper.SOI_SLICE, "Subject of interest", " "),
					new AutoCompletionEntry(KeywordsAndConstantsHelper.SOI_SOURCES, "Subject of interest", " "),
					new AutoCompletionEntry("Statement", "Optional part of a Reference", "('')"),
					new AutoCompletionEntry("Class", "Optional part of a Reference", "('')"),
					new AutoCompletionEntry("Method", "Optional part of a Reference", "('')"),
					new AutoCompletionEntry("App", "Mandatory part of a Reference", "('')"),
					new AutoCompletionEntry("IN", "in the following reference", " "),
					new AutoCompletionEntry("FROM", "from the following reference", " "),
					new AutoCompletionEntry("TO", "to the following reference", " "),
					new AutoCompletionEntry("FEATURES", "to attach features", " ''"),
					new AutoCompletionEntry("FEATURING", "to attach features - same as FEATURES", " ''"),
					new AutoCompletionEntry("USES", "force using a certain tool", " ''"),
					new AutoCompletionEntry("USING", "force using a certain tool - same as USES", " ''"),
					new AutoCompletionEntry("WITH", "to assign variables", " 'VARIABLE' = ") };
			AUTOCOMPLETION_INSERT_FILE = new AutoCompletionEntry("Insert path to file", "Opens file browser..", null);
			AUTOCOMPLETION_ENDING_SYMBOL_AQL = new AutoCompletionEntry("?", "Expect AQL-Answer ending symbol", null);
			AUTOCOMPLETION_ENDING_SYMBOL_FILE = new AutoCompletionEntry("!", "Expect File ending symbol", null);
			AUTOCOMPLETION_ENDING_SYMBOL_RAW = new AutoCompletionEntry(".", "Expect RAW data ending symbol", null);
			for (final File file : Storage.getInstance().getRecentFiles(Storage.TYPE_FILES)) {
				AUTOCOMPLETION_LAST_INSERTED_FILES.add(file.getAbsolutePath());
			}
		}

		this.openDialog = new FileChooser();
		this.openDialog.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("*.* All files", "*.*"));

		this.codeArea = new CodeArea();
		this.codeArea.setWrapText(true);
		this.codeArea.setParagraphGraphicFactory(LineNumberFactory.get(this.codeArea));
		this.codeArea.setStyle("-fx-font-family: Consolas;");
		this.codeArea.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton() == MouseButton.PRIMARY || event.getButton() == MouseButton.SECONDARY) {
					Editor.this.popup.hide();
				}
			}
		});
		this.codeArea.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				final Bounds bounds = Editor.this.codeArea.localToScene(Editor.this.codeArea.getBoundsInLocal());
				if (event.getSceneX() < bounds.getMinX() || event.getSceneX() > bounds.getMinX() + bounds.getWidth() - 5
						|| event.getSceneY() < bounds.getMinY()
						|| event.getSceneY() > bounds.getMinY() + bounds.getHeight() - 5) {
					Editor.this.popup.hide();
				}
			}
		});
		GUIHelper.addFinder(this.codeArea, new SearchAndReplaceBox(this.codeArea, "Editor"));
		this.codeZoomPane = new ScaledVirtualized<>(this.codeArea);
		this.codePane = new StackPane(new VirtualizedScrollPane<>(this.codeZoomPane));
		this.codePane.setPrefWidth(Integer.MAX_VALUE);

		this.popup = new Popup();
		this.popup.setAutoHide(true);
		this.popupList = new ListView<>();
		this.popupList.setPrefSize(350, 200);
		this.popupList.setOnMouseClicked((e) -> {
			if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() >= 2) {
				doAutoCompletion();
			}
		});
		this.popupList.setOnKeyPressed((e) -> {
			final KeyCode key = e.getCode();
			if (key == KeyCode.ENTER) {
				doAutoCompletion();
			} else if (key != KeyCode.UP && key != KeyCode.DOWN) {
				Editor.this.popup.hide();
				if (key == KeyCode.LEFT) {
					if (Editor.this.codeArea.getCaretPosition() > 0) {
						Editor.this.codeArea.moveTo(Editor.this.codeArea.getCaretPosition() - 1);
					}
				} else if (key == KeyCode.RIGHT) {
					if (Editor.this.codeArea.getCaretPosition() < Editor.this.codeArea.getText().length()) {
						Editor.this.codeArea.moveTo(Editor.this.codeArea.getCaretPosition() + 1);
					}
				}
			} else if (key == KeyCode.UP) {
				if (Editor.this.topSelected) {
					Editor.this.popupList.getSelectionModel().clearSelection();
				} else if (Editor.this.popupList.getSelectionModel().getSelectedIndex() == 0) {
					Editor.this.topSelected = true;
				}
			} else if (key == KeyCode.DOWN) {
				if (Editor.this.popupList.getSelectionModel().getSelectedIndex() > 0) {
					Editor.this.topSelected = false;
				}
			}
		});
		this.popup.getContent().add(this.popupList);

		this.codeArea.richChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())).subscribe(change -> {
			this.codeArea.setStyleSpans(0, computeHighlighting(this.codeArea.getText()));
		});
		this.codeArea.addEventFilter(ScrollEvent.ANY, e -> {
			// Zoom with Mouse
			if (e.isControlDown()) {
				if (e.getDeltaY() > 0) {
					zoomIn();
				} else if (e.getDeltaY() < 0) {
					zoomOut();
				}
			}
		});
		this.codeArea.setOnKeyPressed((event) -> {
			// Auto-Popup
			this.popupCounter++;
			final int localCounter = this.popupCounter;
			if (this.popupReady && !this.popup.isShowing()) {
				new Thread(() -> {
					try {
						Thread.sleep(1500);
						if (this.popupCounter == localCounter) {
							Platform.runLater(() -> {
								if (this.codeArea.isFocused()) {
									if (!this.popup.isShowing()) {
										autocomplete(false);
									}
								}
							});
						}
					} catch (final Exception e) {
						// do nothing
					}
				}).start();
			}
		});

		this.menuBar = new MenubarEditor(this);
		parent.getSystem().getProgressListener().add(this.menuBar);
		this.setTop(this.menuBar);
		this.setBottom(new LogViewer());

		final BorderPane egBox = new BorderPane();
		final SplitPane splitPane = new SplitPane(this.codePane, egBox);
		final ScrollPane graphPane = new ScrollPane(TaskTreeViewer.getPane());
		graphPane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		final Button hideButton = new Button(FontAwesome.ICON_FORWARD);
		FontAwesome.applyFontAwesome(hideButton);
		hideButton.setMinWidth(50);
		hideButton.setOnAction((event) -> {
			if (graphPane.isVisible()) {
				hideExecutionGraphs(graphPane, hideButton, splitPane);
			} else {
				showExecutionGraphs(graphPane, hideButton, splitPane, true);
			}
		});
		egBox.setCenter(graphPane);
		egBox.setBottom(hideButton);
		this.setCenter(splitPane);
		if (this.parent.getSystem().getOptions().getDrawGraphs()) {
			showExecutionGraphs(graphPane, hideButton, splitPane, true);
		} else {
			hideExecutionGraphs(graphPane, hideButton, splitPane);
		}
		splitPane.getDividers().get(0).positionProperty().addListener((obj, oldValue, newValue) -> {
			if (1f - newValue.floatValue() > 60 / splitPane.getWidth() && newValue.intValue() != 1) {
				showExecutionGraphs(graphPane, hideButton, splitPane, false);
			} else {
				hideExecutionGraphs(graphPane, hideButton, splitPane);
			}
		});

		this.ready = true;
	}

	public boolean isReady() {
		return this.ready;
	}

	private void showExecutionGraphs(ScrollPane pane, Button hideButton, SplitPane splitPane,
			boolean changeDividerPosition) {
		if (changeDividerPosition) {
			splitPane.setDividerPosition(0, 0.6);
		}
		pane.setMaxWidth(Double.MAX_VALUE);
		pane.setVisible(true);
		hideButton.setMaxWidth(Double.MAX_VALUE);
		hideButton.setText(FontAwesome.ICON_FORWARD);
		this.parent.getSystem().getOptions().setDrawGraphs(true);
	}

	private void hideExecutionGraphs(ScrollPane pane, Button hideButton, SplitPane splitPane) {
		pane.setMaxWidth(50);
		pane.setVisible(false);
		hideButton.setMaxWidth(50);
		hideButton.setText(FontAwesome.ICON_BACKWARD);
		splitPane.setDividerPosition(0, 1);
		this.parent.getSystem().getOptions().setDrawGraphs(false);
	}

	private static StyleSpans<Collection<String>> computeHighlighting(final String text) {
		final Matcher matcher = PATTERN.matcher(text);
		int lastKwEnd = 0;
		final StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		while (matcher.find()) {
			final String styleClass = matcher.group("KEYWORD1") != null ? "keyword1"
					: matcher.group("KEYWORD2") != null ? "keyword2"
							: matcher.group("KEYWORD3") != null ? "keyword3"
									: matcher.group("PAREN1") != null ? "paren1"
											: matcher.group("PAREN2") != null ? "paren2"
													: matcher.group("PAREN3") != null ? "paren3"
															: matcher.group("QUESTION") != null ? "question"
																	: matcher.group("ARROW") != null ? "arrow"
																			: matcher.group("VAR1") != null ? "var"
																					: matcher.group("VAR2") != null
																							? "var"
																							: matcher.group(
																									"COMMA") != null
																											? "comma"
																											: matcher
																													.group("STRING") != null
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

	public void zoomReset() {
		this.codeZoomPane.getZoom().setX(1d);
		this.codeZoomPane.getZoom().setY(1d);
	}

	public void zoomIn() {
		if (this.codeZoomPane.getZoom().getX() < ZOOM_UPPER_BOUND) {
			this.codeZoomPane.getZoom().setX(this.codeZoomPane.getZoom().getX() / ZOOM_FACTOR);
			this.codeZoomPane.getZoom().setY(this.codeZoomPane.getZoom().getY() / ZOOM_FACTOR);
		}
	}

	public void zoomOut() {
		if (this.codeZoomPane.getZoom().getX() > ZOOM_LOWER_BOUND) {
			this.codeZoomPane.getZoom().setX(this.codeZoomPane.getZoom().getX() * ZOOM_FACTOR);
			this.codeZoomPane.getZoom().setY(this.codeZoomPane.getZoom().getY() * ZOOM_FACTOR);
		}
	}

	public void autoformat() {
		Platform.runLater(() -> {
			this.popupReady = false;
			String content = this.codeArea.getText();
			content = Helper.autoformat(content);
			this.codeArea.replaceText(content);
			this.popupReady = true;
		});
	}

	public void ask() {
		if (!this.stop) {
			this.parent.getSystem().query(this.codeArea.getText());
		} else {
			this.parent.getSystem().queryCanceled(Task.STATUS_EXECUTION_INTERRUPTED, "User canceled query execution!");
		}
	}

	public void setContent(final String content) {
		Platform.runLater(() -> {
			this.codeArea.replaceText(content);
		});
		autoformat();
	}

	public void openFile(final File file) throws IOException {
		final byte[] encoded = Files.readAllBytes(file.toPath());
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
			final File file = this.openDialog.showOpenDialog(this.getParentGUI().getStage());
			if (file != null) {
				try {
					filename = file.getAbsolutePath();
					this.currentDir = file.getParentFile();

					// Paste
					insert(filename);
				} catch (final Exception e) {
					Log.msg("File not found: " + file.toString(), Log.ERROR);
				}
			}
		});
	}

	public boolean isStop() {
		return this.stop;
	}

	public void setStop(final boolean stop) {
		this.stop = stop;
	}

	public void insert(String string) {
		if (!AUTOCOMPLETION_LAST_INSERTED_FILES.contains(string)) {
			AUTOCOMPLETION_LAST_INSERTED_FILES.add(0, string);
			Storage.getInstance().store(new File(string), Storage.TYPE_FILES);
		}
		final int caret = this.codeArea.getCaretPosition();

		final String before = this.codeArea.getText();
		final String after;
		if (caret >= before.length()) {
			after = before.substring(0) + string;
		} else {
			after = before.substring(0, caret) + string + before.substring(caret);
		}

		this.codeArea.replaceText(after);
		this.codeArea.requestFocus();
		this.codeArea.moveTo(caret + string.length());

		Editor.this.popupReady = true;
	}

	public void transform() {
		final Query queryObj = QueryHandler.parseQuery(this.codeArea.getText());
		if (QueryTransformer.transform(queryObj, queryObj.getQuestions().iterator().next())) {
			this.codeArea.replaceText(queryObj.toString());
		}
	}

	public void autocomplete(boolean force) {
		try {
			// Cancel if empty
			if (!force && this.codeArea.getText().isBlank()) {
				return;
			}

			// Cancel on same caret position as before
			final int currentCaretPos = this.codeArea.getCaretPosition();
			if (!force && this.lastCaretPos == currentCaretPos) {
				return;
			} else {
				this.lastCaretPos = currentCaretPos;
			}

			// Cancel on last symbol exclusions
			if (!force && this.codeArea.getCaretPosition() > 0) {
				final String key = this.codeArea.getText().substring(this.codeArea.getCaretPosition() - 1,
						this.codeArea.getCaretPosition());
				if (key.equals("?") || key.equals("!") || key.equals(".")) {
					return;
				}
			}

			// Get key for sorting
			final int caret = this.codeArea.getCaretPosition();
			int from = 0;
			for (final String symbol : AUTOCOMPLETION_START_SYMBOLS) {
				final int newFrom = this.codeArea.getText().substring(0, caret).lastIndexOf(symbol);
				if ((newFrom + 1) > from) {
					from = 1 + newFrom;
				}
			}

			final String afterCaret = this.codeArea.getText().substring(caret);
			int to = Integer.MAX_VALUE;
			for (final String symbol : AUTOCOMPLETION_END_SYMBOLS) {
				if (to > afterCaret.indexOf(symbol) && afterCaret.contains(symbol)) {
					to = this.codeArea.getText().substring(caret).indexOf(symbol);
				}
			}
			if (to == Integer.MAX_VALUE) {
				to = caret;
			} else {
				to += caret;
			}
			String key = this.codeArea.getText().substring(from, to);
			this.popupPart[0] = from;
			this.popupPart[1] = to;

			// Word before
			String wordBefore;
			try {
				int fromBefore = 0;
				for (final String symbol : AUTOCOMPLETION_START_SYMBOLS) {
					final int newFrom = this.codeArea.getText().substring(0, from - 1).lastIndexOf(symbol);
					if ((newFrom + 1) > fromBefore) {
						fromBefore = 1 + newFrom;
					}
				}
				wordBefore = this.codeArea.getText().substring(fromBefore, from - 1);
			} catch (final StringIndexOutOfBoundsException e) {
				wordBefore = null;
			}
			final String key2 = getAutoCompletionEntryDescription(wordBefore);

			// Adjust key
			String cutterSymbol = " ";
			boolean offerQueryEnd = false;
			boolean lookForFile;
			try {
				lookForFile = (from > 0 && to > 0 && "'".equals(String.valueOf(this.codeArea.getText().charAt(from)))
						&& "'".equals(String.valueOf(this.codeArea.getText().charAt(to))));
			} catch (final IndexOutOfBoundsException e) {
				lookForFile = false;
			}
			if (from - 2 >= 0 && this.codeArea.getText().substring(from - 2, from).equals("->")) {
				key = "App";
			} else if (key2 != null) {
				if (key2.contains(SUBJECT_OF_INTEREST)) {
					key = "IN " + FOLLOWING_REFERENCE;
					cutterSymbol = null;
				} else if (key2.contains(FOLLOWING_REFERENCE)) {
					key = "App " + PART_OF_A_REFERENCE;
					cutterSymbol = null;
				}
			} else if (key.isBlank() && !lookForFile) {
				key = KeywordsAndConstantsHelper.SOI_FLOWS + " " + SUBJECT_OF_INTEREST;
				cutterSymbol = null;
			} else if (key.equalsIgnoreCase("apk") || key.equals(")")) {
				offerQueryEnd = true;
			}

			// Show popup
			this.popup.show(this.codeArea, this.codeArea.getCaretBounds().get().getMaxX(),
					this.codeArea.getCaretBounds().get().getMaxY());

			// Sort items and populate popup
			this.popupList.getItems().clear();
			final List<AutoCompletionEntry> items1 = new LinkedList<>();
			if (!lookForFile) {
				for (final AutoCompletionEntry item : AUTOCOMPLETION_KEYWORDS) {
					items1.add(item);
				}
			}
			items1.add(AUTOCOMPLETION_INSERT_FILE);
			if (offerQueryEnd) {
				this.popupList.getItems().addAll(AUTOCOMPLETION_ENDING_SYMBOL_AQL, AUTOCOMPLETION_ENDING_SYMBOL_FILE,
						AUTOCOMPLETION_ENDING_SYMBOL_RAW);
			}
			if (lookForFile) {
				this.popupList.getItems().addAll(items1);
			} else {
				final List<AutoCompletionEntry> items2 = new LinkedList<>(items1);
				items1.sort(new EqualSymbolsComparator(key, cutterSymbol));
				items2.sort(new LevenshteinComparator(items1.get(0).toString()));
				items2.remove(items1.get(0));
				if (key.length() <= 2) {
					items2.remove(items1.get(1));
					items2.remove(items1.get(2));
				}
				this.popupList.getItems().add(items1.get(0));
				if (key.length() <= 2) {
					this.popupList.getItems().add(items1.get(1));
					this.popupList.getItems().add(items1.get(2));
				}
				this.popupList.getItems().addAll(items2);
			}
			for (final String file : AUTOCOMPLETION_LAST_INSERTED_FILES) {
				this.popupList.getItems().add(new AutoCompletionEntry(file, "lately inserted file", ""));
			}
			this.popupList.getSelectionModel().select(0);
			this.topSelected = true;
			this.popupList.requestFocus();
		} catch (final Exception e) {
			Log.msg("Something went wrong during auto-completion." + Log.getExceptionAppendix(e), Log.DEBUG_DETAILED);
		}
	}

	private void doAutoCompletion() {
		if (!Editor.this.popupList.getSelectionModel().isEmpty()) {
			Editor.this.popupReady = false;
			final AutoCompletionEntry entry = Editor.this.popupList.getSelectionModel().getSelectedItem();
			final String replacement;
			if (entry == AUTOCOMPLETION_ENDING_SYMBOL_AQL || entry == AUTOCOMPLETION_ENDING_SYMBOL_FILE
					|| entry == AUTOCOMPLETION_ENDING_SYMBOL_RAW) {
				this.popupPart[0] = this.codeArea.getText().indexOf("\n", this.codeArea.getCaretPosition());
				if (this.popupPart[0] < 0) {
					this.popupPart[0] = this.codeArea.getText().length();
				}
				if (this.codeArea.getText().charAt(this.popupPart[0] - 1) == '\n') {
					this.popupPart[0] = this.popupPart[0] - 1;
				}
				this.popupPart[1] = this.popupPart[0];
				if (this.popupPart[0] - 1 > 0 && this.codeArea.getText().charAt(this.popupPart[0] - 1) == ' ') {
					replacement = entry.getKeyword();
				} else {
					replacement = " " + entry.getKeyword();
				}
			} else {
				replacement = entry.getOutput();
				if (AUTOCOMPLETION_LAST_INSERTED_FILES.contains(replacement)) {
					AUTOCOMPLETION_LAST_INSERTED_FILES.remove(replacement);
					AUTOCOMPLETION_LAST_INSERTED_FILES.add(0, replacement);
					Storage.getInstance().store(new File(replacement), Storage.TYPE_FILES);
				}
			}
			if (replacement == null) {
				insertFilename();
			} else {
				final String before = Editor.this.codeArea.getText().substring(0, Editor.this.popupPart[0]);
				final String after = Editor.this.codeArea.getText().substring(Editor.this.popupPart[1]);
				Editor.this.codeArea.replaceText(before + replacement + after);
				Editor.this.codeArea.requestFocus();
				Editor.this.codeArea.moveTo(Editor.this.popupPart[0] + replacement.length()
						- Editor.this.popupList.getSelectionModel().getSelectedItem().getOffset());
				Editor.this.popupReady = true;
			}
		}
		Editor.this.popup.hide();
	}

	private String getAutoCompletionEntryDescription(String key) {
		if (key != null) {
			for (final AutoCompletionEntry entry : AUTOCOMPLETION_KEYWORDS) {
				if (entry.getKeyword().equalsIgnoreCase(key)) {
					return entry.toString();
				}
			}
		}
		return null;
	}

	private class AutoCompletionEntry {
		private String keyword;
		private String description;
		private String output;
		private int offset;

		public AutoCompletionEntry(String keyword, String description, String output) {
			this.keyword = keyword;
			this.description = keyword + " (" + description + ")";
			if (output != null) {
				this.output = keyword + output;
			} else {
				output = null;
			}
			this.offset = 0;
			if (output != null) {
				if (output.endsWith("''")) {
					this.offset = 1;
				} else if (output.endsWith("('')")) {
					this.offset = 2;
				} else if (output.endsWith("[  ] ?") || output.endsWith("[  ] !")) {
					this.offset = 4;
				}
			}
		}

		public String getKeyword() {
			return this.keyword;
		}

		public String getOutput() {
			return this.output;
		}

		public int getOffset() {
			return this.offset;
		}

		@Override
		public String toString() {
			return this.description;
		}
	}
}