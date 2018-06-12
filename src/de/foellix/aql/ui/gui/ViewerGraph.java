package de.foellix.aql.ui.gui;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Attribute;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.Permission;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.EqualsHelper;
import de.foellix.aql.system.IAnswerAvailable;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class ViewerGraph extends HBox implements IAnswerAvailable {
	private final Viewer parent;

	private Answer answer;
	private final GraphicsContext gc;
	private final Canvas canvas;
	private Tooltip tp;

	private final int horizontalOffset = 10;
	private final int verticalOffset = 10;
	private final int edgeOffset = 30;
	private float y1, y2, y3, y4, x1, x2, x3;
	private final float py1 = 10, py2 = 25, py3 = 40, py4 = 71, px1 = 25, px2 = 50, px3 = 75;
	private int currentSizeY, currentSizeX;
	private boolean rotated = false;
	private final float zoomFactor = 1.15f;

	private List<Node> intentSinks, intentSources, permissions, references;
	private List<Edge> intentSourceToRef, intentSinkToRef, permissionToRef, refToRef;
	private Map<Object, Node> nodeMap;

	private final File tempGraphFile;

	public ViewerGraph(final Viewer parent) {
		this.parent = parent;

		this.currentSizeX = Math.max(1024, Double.valueOf(this.parent.getParentGUI().getStage().getWidth()).intValue());
		this.currentSizeY = Math.max(768, Double.valueOf(this.parent.getParentGUI().getStage().getHeight()).intValue());

		this.canvas = new Canvas(this.currentSizeX, this.currentSizeY);
		this.canvas.addEventHandler(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent t) {
				if (checkForToolTip(t)) {
					parent.getParentGUI().getStage().getScene().setCursor(Cursor.HAND);
				} else {
					parent.getParentGUI().getStage().getScene().setCursor(Cursor.DEFAULT);
					if (ViewerGraph.this.tp != null && ViewerGraph.this.tp.isShowing()) {
						ViewerGraph.this.tp.hide();
					}
				}
			}
		});
		this.canvas.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
			@Override
			public void handle(final MouseEvent t) {
				showToolTip(t);
			}
		});
		this.gc = this.canvas.getGraphicsContext2D();

		this.scrollPane = new ScrollPane(this.canvas);
		this.scrollPane.setPrefSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
		this.scrollPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		this.scrollPane.setStyle("-fx-focus-color: transparent;");

		this.getChildren().add(this.scrollPane);

		this.tempGraphFile = new File("data/gui/temp.png");
	}

	final ScrollPane scrollPane;

	public void refresh() {
		refreshSizes();
		if (this.answer != null) {
			initShapes();
		}

		if (!this.rotated) {
			this.scrollPane.setContent(this.canvas);
		} else {
			this.canvas.setRotate(270);
			try {
				final WritableImage writableImage = new WritableImage(this.currentSizeY, this.currentSizeX);
				this.canvas.snapshot(new SnapshotParameters(), writableImage);
				final RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
				ImageIO.write(renderedImage, "png", this.tempGraphFile);

				final ImageView iv = new ImageView(this.tempGraphFile.toURI().toURL().toString());
				this.scrollPane.setContent(iv);
			} catch (final IOException e) {
				Log.error("Could not rotate graph.");
				e.printStackTrace();
			}
			this.canvas.setRotate(0);
		}
	}

	private void refreshSizes() {
		if (this.rotated) {
			this.currentSizeX = Math.max(768,
					Double.valueOf(this.parent.getParentGUI().getStage().getHeight()).intValue()) - 23;
			this.currentSizeY = Math.max(1024,
					Double.valueOf(this.parent.getParentGUI().getStage().getWidth()).intValue()) - 23;
		} else {
			this.currentSizeX = Math.max(1024,
					Double.valueOf(this.parent.getParentGUI().getStage().getWidth()).intValue()) - 23;
			this.currentSizeY = Math.max(768,
					Double.valueOf(this.parent.getParentGUI().getStage().getHeight()).intValue()) - 23;
		}
		if (this.currentSizeX % 2 == 1) {
			this.currentSizeX--;
		}
		if (this.currentSizeY % 2 == 1) {
			this.currentSizeY--;
		}

		this.canvas.setWidth(this.currentSizeX);
		this.canvas.setHeight(this.currentSizeY);

		this.y1 = (this.py1 / 100f) * this.currentSizeY;
		this.y2 = (this.py2 / 100f) * this.currentSizeY;
		this.y3 = (this.py3 / 100f) * this.currentSizeY;
		this.y4 = (this.py4 / 100f) * this.currentSizeY;

		this.x1 = (this.px1 / 100f) * this.currentSizeX;
		this.x2 = (this.px2 / 100f) * this.currentSizeX;
		this.x3 = (this.px3 / 100f) * this.currentSizeX;
	}

	private void initShapes() {
		this.nodeMap = new HashMap<>();

		// Nodes
		this.intentSinks = new ArrayList<>();
		if (this.answer.getIntentsinks() != null) {
			final float count = this.answer.getIntentsinks().getIntentsink().size();
			for (int i = 0; i < count; i++) {
				final Intentsink item = this.answer.getIntentsinks().getIntentsink().get(i);

				final int maxWidth = 500;
				final int maxHeight = 50;
				final int minWidth = 150;
				final int minHeight = 30;
				final float width = this.x1 - 0 - (2 * this.horizontalOffset);
				final float x = 0 + this.horizontalOffset;
				final float height = ((1 / count) * (this.y2 - this.y1 - (count - 1) * this.verticalOffset));
				final float y = this.y2
						- ((i + 1) * ((Math.max(Math.min(height, maxHeight), minHeight) + this.verticalOffset)))
						+ this.verticalOffset;

				final Node temp = new Node(x, y, width, height, maxWidth, maxHeight, minWidth, minHeight, item);
				this.intentSinks.add(temp);
				this.nodeMap.put(item, temp);
			}
		}

		this.intentSources = new ArrayList<>();
		if (this.answer.getIntentsources() != null) {
			final float count = this.answer.getIntentsources().getIntentsource().size();
			for (int i = 0; i < count; i++) {
				final Intentsource item = this.answer.getIntentsources().getIntentsource().get(i);
				Node temp = null;
				for (final Node existingNode : this.intentSources) {
					if (existingNode.getReference() instanceof Intentsource) {
						final Intentsource existingItem = (Intentsource) existingNode.getReference();
						if (EqualsHelper.equals(existingItem.getTarget(), item.getTarget(), true)) {
							temp = existingNode;
							break;
						}
					}
				}
				if (temp == null) {
					final int maxWidth = 500;
					final int maxHeight = 50;
					final int minWidth = 150;
					final int minHeight = 30;
					final float width = this.currentSizeX - this.x3 - (2 * this.horizontalOffset);
					final float x = this.currentSizeX - Math.max(Math.min(width, maxWidth), minWidth)
							- this.horizontalOffset;
					final float height = ((1 / count) * (this.y2 - this.y1 - (count - 1) * this.verticalOffset));
					final float y = this.y2
							+ (i * ((Math.max(Math.min(height, maxHeight), minHeight) + this.verticalOffset)));

					temp = new Node(x, y, width, height, maxWidth, maxHeight, minWidth, minHeight, item);
					this.intentSources.add(temp);
				}
				this.nodeMap.put(item, temp);
			}
		}

		this.permissions = new ArrayList<>();
		if (this.answer.getPermissions() != null) {
			final float count = this.answer.getPermissions().getPermission().size();
			for (int i = 0; i < count; i++) {
				final Permission item = this.answer.getPermissions().getPermission().get(i);
				final int maxWidth = 500;
				final int maxHeight = 30;
				final int minWidth = 150;
				final int minHeight = 15;
				final float width = ((1 / count) * (this.x3 - this.x1 - (count - 1) * this.horizontalOffset));
				final float x = this.x1 + (i * (Math.max(Math.min(width, maxWidth), minWidth) + this.horizontalOffset));
				final float height = this.y1 - 0 - this.verticalOffset;
				final float y = 0 + this.verticalOffset;

				final Node temp = new Node(x, y, width, height, maxWidth, maxHeight, minWidth, minHeight, item);
				this.permissions.add(temp);
				this.nodeMap.put(item, temp);
			}
		}

		this.references = new ArrayList<>();
		final List<Reference> uniqueReferences = new ArrayList<>();
		if (this.answer.getFlows() != null) {
			for (final Flow path : this.answer.getFlows().getFlow()) {
				for (final Reference reference : path.getReference()) {
					boolean alreadyIn = false;
					for (final Reference inReference : uniqueReferences) {
						if (EqualsHelper.equals(reference, inReference)) {
							alreadyIn = true;
							break;
						}
					}

					if (!alreadyIn) {
						uniqueReferences.add(reference);
					}
				}
			}
			final float count = uniqueReferences.size();
			int i = 0;
			for (final Reference item : uniqueReferences) {
				final int maxWidth = 300;
				final int maxHeight = 300;
				final int minWidth = 60;
				final int minHeight = 250;
				final float width = ((1 / count) * (this.x3 - this.x1 - (count - 1) * this.horizontalOffset));
				final float x = this.x1 + (i * (Math.max(Math.min(width, maxWidth), minWidth) + this.horizontalOffset));
				final float height = this.y4 - this.y3;
				final float y = this.y3;

				final Node temp = new Node(x, y, width, height, maxWidth, maxHeight, minWidth, minHeight, item);
				this.references.add(temp);
				this.nodeMap.put(item, temp);

				i++;
			}
		}

		// Edges
		this.intentSourceToRef = new ArrayList<>();
		if (this.answer.getIntentsources() != null) {
			for (int i = 0; i < this.answer.getIntentsources().getIntentsource().size(); i++) {
				final Intentsource item = this.answer.getIntentsources().getIntentsource().get(i);
				final Reference r1 = item.getReference();
				for (final Reference r2 : uniqueReferences) {
					if (EqualsHelper.equals(r1, r2)) {
						final Node start = this.nodeMap.get(item);
						final Node end = this.nodeMap.get(r2);

						this.intentSourceToRef.add(new Edge(start, end, -1));
						break;
					}
				}
			}
		}

		this.intentSinkToRef = new ArrayList<>();
		if (this.answer.getIntentsinks() != null) {
			for (int i = 0; i < this.answer.getIntentsinks().getIntentsink().size(); i++) {
				final Intentsink item = this.answer.getIntentsinks().getIntentsink().get(i);
				final Reference r1 = item.getReference();
				for (final Reference r2 : uniqueReferences) {
					if (EqualsHelper.equals(r1, r2)) {
						final Node start = this.nodeMap.get(item);
						final Node end = this.nodeMap.get(r2);

						this.intentSinkToRef.add(new Edge(start, end, -1));
						break;
					}
				}
			}
		}

		this.permissionToRef = new ArrayList<>();
		if (this.answer.getPermissions() != null) {
			for (final Permission item : this.answer.getPermissions().getPermission()) {
				final Reference r1 = item.getReference();
				for (final Reference r2 : uniqueReferences) {
					if (EqualsHelper.equals(r1, r2)) {
						final Node start = this.nodeMap.get(item);
						final Node end = this.nodeMap.get(r2);

						this.permissionToRef.add(new Edge(start, end));
						break;
					}
				}
			}
		}

		this.refToRef = new ArrayList<>();
		if (this.answer.getFlows() != null) {
			for (final Flow p : this.answer.getFlows().getFlow()) {
				Reference r1;
				Reference r2;
				if (p.getReference().get(0).getType().equals("from")) {
					r1 = p.getReference().get(0);
					r2 = p.getReference().get(1);
				} else {
					r1 = p.getReference().get(1);
					r2 = p.getReference().get(0);
				}

				for (final Reference rt : uniqueReferences) {
					if (EqualsHelper.equals(rt, r1)) {
						r1 = rt;
						break;
					}
				}

				for (final Reference rt : uniqueReferences) {
					if (EqualsHelper.equals(rt, r2)) {
						r2 = rt;
						break;
					}
				}

				final Node start = this.nodeMap.get(r1);
				final Node end = this.nodeMap.get(r2);

				final Edge temp = new Edge(start, end);
				if (p.getAttributes() != null) {
					for (final Attribute attr : p.getAttributes().getAttribute()) {
						if (attr.getName().equals("complete") && attr.getValue().equals("true")) {
							temp.setSize(4);
							break;
						}
					}
				}
				temp.setLength(Math.abs(end.getPosX() - start.getPosX()));
				this.refToRef.add(temp);
			}

			Collections.sort(this.refToRef, new Comparator<Edge>() {
				@Override
				public int compare(final Edge e1, final Edge e2) {
					if (e1.getLength() == e2.getLength()) {
						if (e1.getStart().getPosX() + e1.getEnd().getPosX() > e2.getStart().getPosX()
								+ e2.getEnd().getPosX()) {
							return 1;
						} else {
							return -1;
						}
					} else if (e1.getLength() > e2.getLength()) {
						return 1;
					} else {
						return -1;
					}
				}
			});

			int i = 0;
			int j = 0;
			float lastLength = 0;
			for (final Edge edge : this.refToRef) {
				if (edge.getLength() > lastLength) {
					i++;
					j--;
					lastLength = edge.getLength();
				} else {
					j++;
				}
				edge.setCornerY(this.y4 + i * this.edgeOffset + j * (this.edgeOffset / 5));
			}
		}

		drawShapes();
	}

	private void drawShapes() {
		// Background
		this.gc.setFill(Color.WHITE);
		this.gc.fillRect(0, 0, this.currentSizeX, this.currentSizeY);
		this.gc.setStroke(Color.BLACK);
		if (Log.logIt(Log.DEBUG_DETAILED)) {
			// Grid
			this.gc.setLineWidth(1);
			this.gc.strokeLine(0, this.y1, this.currentSizeX, this.y1);
			this.gc.strokeLine(0, this.y2, this.currentSizeX, this.y2);
			this.gc.strokeLine(0, this.y3, this.currentSizeX, this.y3);
			this.gc.strokeLine(0, this.y4, this.currentSizeX, this.y4);
			this.gc.strokeLine(this.x1, 0, this.x1, this.currentSizeY);
			this.gc.strokeLine(this.x2, 0, this.x2, this.currentSizeY);
			this.gc.strokeLine(this.x3, 0, this.x3, this.currentSizeY);
		}
		this.gc.setLineWidth(2);

		// Counter
		final Map<Node, Integer> countDocksFromTop = new HashMap<>();
		final Map<Node, Integer> countDocksFromBottom = new HashMap<>();

		// Edges
		for (final Edge edge : this.permissionToRef) {
			if (countDocksFromTop.get(edge.getEnd()) == null) {
				countDocksFromTop.put(edge.getEnd(), new Integer(2));
			} else {
				countDocksFromTop.replace(edge.getEnd(),
						new Integer(countDocksFromTop.get(edge.getEnd()).intValue() + 1));
			}
		}
		for (final Edge edge : this.intentSourceToRef) {
			if (countDocksFromTop.get(edge.getEnd()) == null) {
				countDocksFromTop.put(edge.getEnd(), new Integer(2));
			} else {
				countDocksFromTop.replace(edge.getEnd(),
						new Integer(countDocksFromTop.get(edge.getEnd()).intValue() + 1));
			}
		}
		for (final Edge edge : this.intentSinkToRef) {
			if (countDocksFromTop.get(edge.getEnd()) == null) {
				countDocksFromTop.put(edge.getEnd(), new Integer(2));
			} else {
				countDocksFromTop.replace(edge.getEnd(),
						new Integer(countDocksFromTop.get(edge.getEnd()).intValue() + 1));
			}
		}
		for (final Edge edge : this.refToRef) {
			if (countDocksFromBottom.get(edge.getStart()) == null) {
				countDocksFromBottom.put(edge.getStart(), new Integer(2));
			} else {
				countDocksFromBottom.replace(edge.getStart(),
						new Integer(countDocksFromBottom.get(edge.getStart()).intValue() + 1));
			}
			if (countDocksFromBottom.get(edge.getEnd()) == null) {
				countDocksFromBottom.put(edge.getEnd(), new Integer(2));
			} else {
				countDocksFromBottom.replace(edge.getEnd(),
						new Integer(countDocksFromBottom.get(edge.getEnd()).intValue() + 1));
			}
		}

		this.gc.setFill(Color.web("#d6ace4"));
		this.gc.setStroke(Color.web("#d6ace4"));
		for (final Edge edge : this.permissionToRef) {
			edge.draw(this.gc, countDocksFromTop);
		}

		this.gc.setFill(Color.web("#f29c9c"));
		this.gc.setStroke(Color.web("#f29c9c"));
		for (final Edge edge : this.intentSourceToRef) {
			edge.draw(this.gc, countDocksFromTop);
		}

		this.gc.setFill(Color.web("#c0d6a3"));
		this.gc.setStroke(Color.web("#c0d6a3"));
		for (final Edge edge : this.intentSinkToRef) {
			edge.draw(this.gc, countDocksFromTop);
		}

		this.gc.setFill(Color.web("#a3c3d6"));
		this.gc.setStroke(Color.web("#a3c3d6"));
		for (final Edge edge : this.refToRef) {
			edge.draw(this.gc, countDocksFromBottom);
		}

		// Nodes
		this.gc.setFill(Color.web("#c0d6a3"));
		this.gc.setStroke(Color.web("#c0d6a3"));
		for (final Node node : this.intentSinks) {
			node.draw(this.gc);
		}

		this.gc.setFill(Color.web("#f29c9c"));
		this.gc.setStroke(Color.web("#f29c9c"));
		for (final Node node : this.intentSources) {
			node.draw(this.gc);
		}

		this.gc.setFill(Color.web("#d6ace4"));
		this.gc.setStroke(Color.web("#d6ace4"));
		for (final Node node : this.permissions) {
			node.draw(this.gc);
		}

		this.gc.setFill(Color.web("#a3c3d6"));
		this.gc.setStroke(Color.web("#a3c3d6"));
		for (final Node node : this.references) {
			node.draw(this.gc, true);
		}
	}

	private boolean checkForToolTip(final MouseEvent t) {
		try {
			for (final Node node : this.intentSinks) {
				if (checkForToolTip(t, node)) {
					return true;
				}
			}
			for (final Node node : this.intentSources) {
				if (checkForToolTip(t, node)) {
					return true;
				}
			}
			for (final Node node : this.permissions) {
				if (checkForToolTip(t, node)) {
					return true;
				}
			}
			for (final Node node : this.references) {
				if (checkForToolTip(t, node)) {
					return true;
				}
			}
			return false;
		} catch (final Exception e) {
			return false;
		}
	}

	private boolean checkForToolTip(final MouseEvent t, final Node node) {
		final double x = t.getX();
		final double y = t.getY();

		if (x >= node.getPosX() && x <= node.getPosX() + node.getWidth()) {
			if (y >= node.getPosY() && y <= node.getPosY() + node.getHeight()) {
				return true;
			}
		}
		return false;
	}

	private void showToolTip(final MouseEvent t) {
		try {
			if (this.tp != null) {
				this.tp.hide();
			}

			for (final Node node : this.intentSinks) {
				showToolTip(t, node);
			}
			for (final Node node : this.intentSources) {
				showToolTip(t, node);
			}
			for (final Node node : this.permissions) {
				showToolTip(t, node);
			}
			for (final Node node : this.references) {
				showToolTip(t, node);
			}
		} catch (final Exception e) {
			// do nothing
		}
	}

	private void showToolTip(final MouseEvent t, final Node node) {
		final double x = t.getX();
		final double y = t.getY();

		if (x >= node.getPosX() && x <= node.getPosX() + node.getWidth()) {
			if (y >= node.getPosY() && y <= node.getPosY() + node.getHeight()) {
				if (t.getButton() == MouseButton.PRIMARY) {
					this.tp = new Tooltip(node.tooltip());
				} else {
					this.tp = new Tooltip(node.tooltip2());
				}
				this.tp.show((javafx.scene.Node) t.getSource(),
						this.parent.getParentGUI().getStage().getX() + t.getSceneX() + 20,
						this.parent.getParentGUI().getStage().getY() + t.getSceneY());
			}
		}
	}

	@Override
	public void answerAvailable(final Answer answer, int status) {
		this.answer = answer;

		Platform.runLater(() -> {
			refresh();
			initShapes();
		});
	}

	public void exportGraph(final File file) {
		try {
			final WritableImage writableImage = new WritableImage(this.currentSizeX, this.currentSizeY);
			this.canvas.snapshot(new SnapshotParameters(), writableImage);
			final RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
			ImageIO.write(renderedImage, "png", file);
		} catch (final IOException ex) {
			Log.error("Could not export image.");
		}
	}

	public void rotate() {
		this.rotated = !this.rotated;
		refresh();
	}

	public void zoomReset() {
		this.canvas.setScaleX(1);
		this.canvas.setScaleY(1);
		refresh();
	}

	public void zoomIn() {
		this.canvas.setScaleX(this.canvas.getScaleX() * this.zoomFactor);
		this.canvas.setScaleY(this.canvas.getScaleY() * this.zoomFactor);
		refresh();
	}

	public void zoomOut() {
		this.canvas.setScaleX(this.canvas.getScaleX() / this.zoomFactor);
		this.canvas.setScaleY(this.canvas.getScaleY() / this.zoomFactor);
		refresh();
	}
}
