package de.foellix.aql.helper;

import org.fxmisc.richtext.CodeArea;

import de.foellix.aql.ui.gui.GUI;
import de.foellix.aql.ui.gui.SearchAndReplaceBox;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class GUIHelper {
	private static final GUIHelper INSTANCE = new GUIHelper();

	public static void addFinder(CodeArea ta, SearchAndReplaceBox searchAndReplaceBox) {
		ta.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
			if (e.isControlDown() && !e.isShiftDown() && !e.isAltDown() && e.getCode() == KeyCode.F) {
				searchAndReplaceBox.show();
			} else if (e.getCode() == KeyCode.F3) {
				searchAndReplaceBox.search();
			}
		});
	}

	public static void copyToClipboard(String string) {
		if (Platform.isFxApplicationThread()) {
			copyToClipboardOnFXthread(string);
		} else {
			Platform.runLater(() -> copyToClipboardOnFXthread(string));
		}
	}

	private static void copyToClipboardOnFXthread(String string) {
		final ClipboardContent content = new ClipboardContent();
		content.putString(string);
		Clipboard.getSystemClipboard().setContent(content);

		notifyUser("Copied to clipboard!");
	}

	/**
	 * Shows a short notification to the user.
	 */
	public static void notifyUser(String toastMsg) {
		notifyUser(toastMsg, 1000, 500, 500);
	}

	/**
	 * Shows a short notification to the user.
	 *
	 * @param delay
	 *            in milliseconds
	 * @param fadeInDelay
	 *            in milliseconds
	 * @param fadeOutDelay
	 *            in milliseconds
	 */
	public static void notifyUser(String msg, int delay, int fadeInDelay, int fadeOutDelay) {
		final Stage toastStage = new Stage();
		toastStage.initOwner(GUI.stage);
		toastStage.setResizable(false);
		toastStage.initStyle(StageStyle.TRANSPARENT);

		final Text text = new Text(msg);
		text.setFill(Color.WHITE);

		final StackPane root = new StackPane(text);
		root.setStyle("-fx-background-radius: 10; -fx-background-color: rgba(0, 0, 0, 0.75); -fx-padding: 10px;");
		root.setOpacity(0);

		final Scene scene = new Scene(root);
		scene.setFill(Color.TRANSPARENT);
		toastStage.setScene(scene);
		toastStage.show();
		toastStage.setY(GUI.stage.getY() + GUI.stage.getHeight() - 100);

		final Timeline fadeInTimeline = new Timeline();
		final KeyFrame fadeInKey1 = new KeyFrame(Duration.millis(fadeInDelay),
				new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 1));
		fadeInTimeline.getKeyFrames().add(fadeInKey1);
		fadeInTimeline.setOnFinished((ae) -> {
			new Thread(() -> {
				try {
					Thread.sleep(delay);
				} catch (final InterruptedException e) {
					// do nothing
				}
				final Timeline fadeOutTimeline = new Timeline();
				final KeyFrame fadeOutKey1 = new KeyFrame(Duration.millis(fadeOutDelay),
						new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 0));
				fadeOutTimeline.getKeyFrames().add(fadeOutKey1);
				fadeOutTimeline.setOnFinished((aeb) -> toastStage.close());
				fadeOutTimeline.play();
			}).start();
		});
		fadeInTimeline.play();
		GUI.stage.requestFocus();
	}

	public static void makeResizableInHeight(Region region, int initialHeight) {
		final HeightDragResizer resizer = INSTANCE.new HeightDragResizer(region);
		region.setMinHeight(initialHeight);
		region.setPrefHeight(initialHeight);

		region.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				resizer.mousePressed(event);
			}
		});
		region.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				resizer.mouseDragged(event);
			}
		});
		region.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				resizer.mouseOver(event);
			}
		});
		region.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				resizer.mouseReleased(event);
			}
		});
	}

	private class HeightDragResizer {
		private static final int RESIZE_MARGIN = 5;

		private final Region region;

		private boolean dragging;

		private HeightDragResizer(Region region) {
			this.region = region;
		}

		protected void mouseReleased(MouseEvent event) {
			this.dragging = false;
			this.region.setCursor(Cursor.DEFAULT);
		}

		protected void mouseOver(MouseEvent event) {
			if (isInDraggableZone(event) || this.dragging) {
				this.region.setCursor(Cursor.S_RESIZE);
			} else {
				this.region.setCursor(Cursor.DEFAULT);
			}
		}

		protected boolean isInDraggableZone(MouseEvent event) {
			return event.getY() > 0 && event.getY() < RESIZE_MARGIN;
		}

		protected void mouseDragged(MouseEvent event) {
			if (!this.dragging) {
				return;
			}
			final double newHeight = this.region.getMinHeight() - event.getY();
			new Timeline(new KeyFrame(Duration.millis(1),
					new KeyValue(this.region.minHeightProperty(), newHeight, Interpolator.EASE_BOTH))).play();
		}

		protected void mousePressed(MouseEvent event) {
			if (!isInDraggableZone(event)) {
				return;
			}
			this.dragging = true;
		}
	}
}