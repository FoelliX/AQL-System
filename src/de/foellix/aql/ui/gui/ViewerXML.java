package de.foellix.aql.ui.gui;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.system.IAnswerAvailable;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class ViewerXML extends BorderPane implements IAnswerAvailable {
	protected CodeArea codeArea;

	private MenubarViewer statsbar;

	private static final Pattern XML_TAG = Pattern
			.compile("(?<ELEMENT>(</?\\h*)(\\w+)([^<>]*)(\\h*/?>))" + "|(?<COMMENT><!--[^<>]+-->)");
	// private static final Pattern XML_TAG =
	// Pattern.compile("(?<ELEMENT>(</?\\h*)(\\w+)([^<>]*)(\\h*[|/]>))" +
	// "|(?<COMMENT><!--[^<>]+-->)");

	private static final Pattern ATTRIBUTES = Pattern.compile("(\\w+\\h*)(=)(\\h*\"[^\"]+\")");

	private static final int GROUP_OPEN_BRACKET = 2;
	private static final int GROUP_ELEMENT_NAME = 3;
	private static final int GROUP_ATTRIBUTES_SECTION = 4;
	private static final int GROUP_CLOSE_BRACKET = 5;
	private static final int GROUP_ATTRIBUTE_NAME = 1;
	private static final int GROUP_EQUAL_SYMBOL = 2;
	private static final int GROUP_ATTRIBUTE_VALUE = 3;

	public ViewerXML() {
		this.statsbar = null;
		init();
	}

	public ViewerXML(MenubarViewer statsbar) {
		this.statsbar = statsbar;
		init();
	}

	private void init() {
		this.codeArea = new CodeArea();
		this.codeArea.setWrapText(true);
		this.codeArea.setParagraphGraphicFactory(LineNumberFactory.get(this.codeArea));
		this.codeArea.textProperty().addListener((obs, oldText, newText) -> {
			this.codeArea.setStyleSpans(0, computeHighlighting(newText));
		});
		this.codeArea.setStyle("-fx-font-family: Consolas;");
		final StackPane codePane = new StackPane(new VirtualizedScrollPane<>(this.codeArea));
		codePane.setPrefWidth(Integer.MAX_VALUE);

		this.setCenter(codePane);
	}

	private static StyleSpans<Collection<String>> computeHighlighting(final String text) {
		final Matcher matcher = XML_TAG.matcher(text);
		int lastKwEnd = 0;
		final StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		while (matcher.find()) {

			spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd);
			if (matcher.group("COMMENT") != null) {
				spansBuilder.add(Collections.singleton("comment"), matcher.end() - matcher.start());
			} else {
				if (matcher.group("ELEMENT") != null) {
					final String attributesText = matcher.group(GROUP_ATTRIBUTES_SECTION);

					spansBuilder.add(Collections.singleton("tagmark"),
							matcher.end(GROUP_OPEN_BRACKET) - matcher.start(GROUP_OPEN_BRACKET));
					spansBuilder.add(Collections.singleton("anytag"),
							matcher.end(GROUP_ELEMENT_NAME) - matcher.end(GROUP_OPEN_BRACKET));

					if (!attributesText.isEmpty()) {

						lastKwEnd = 0;

						final Matcher amatcher = ATTRIBUTES.matcher(attributesText);
						while (amatcher.find()) {
							spansBuilder.add(Collections.emptyList(), amatcher.start() - lastKwEnd);
							spansBuilder.add(Collections.singleton("attribute"),
									amatcher.end(GROUP_ATTRIBUTE_NAME) - amatcher.start(GROUP_ATTRIBUTE_NAME));
							spansBuilder.add(Collections.singleton("tagmark"),
									amatcher.end(GROUP_EQUAL_SYMBOL) - amatcher.end(GROUP_ATTRIBUTE_NAME));
							spansBuilder.add(Collections.singleton("avalue"),
									amatcher.end(GROUP_ATTRIBUTE_VALUE) - amatcher.end(GROUP_EQUAL_SYMBOL));
							lastKwEnd = amatcher.end();
						}
						if (attributesText.length() > lastKwEnd) {
							spansBuilder.add(Collections.emptyList(), attributesText.length() - lastKwEnd);
						}
					}

					lastKwEnd = matcher.end(GROUP_ATTRIBUTES_SECTION);

					spansBuilder.add(Collections.singleton("tagmark"), matcher.end(GROUP_CLOSE_BRACKET) - lastKwEnd);
				}
			}
			lastKwEnd = matcher.end();
		}
		spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
		return spansBuilder.create();
	}

	@Override
	public void answerAvailable(final Answer answer, final int status) {
		Platform.runLater(() -> {
			this.codeArea.replaceText(AnswerHandler.createXMLString(answer));
			this.codeArea.scrollYBy(0);
			if (this.statsbar != null) {
				this.statsbar.refresh(answer);
			}
		});
	}

	public String getContent() {
		return this.codeArea.getText();
	}

	public void resetContent() {
		this.codeArea.clear();
	}

	public void undo() {
		this.codeArea.undo();
	}

	public void redo() {
		this.codeArea.redo();
	}
}
