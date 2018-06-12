package de.foellix.aql.ui.gui;

import java.util.Map;

import de.foellix.aql.datastructure.Flow;
import javafx.scene.canvas.GraphicsContext;

public class Edge {
	private Node start, end;
	private float cornerY;
	private Flow reference;
	private float length;

	private int size = 2;

	public Edge(final Node start, final Node end) {
		create(start, end, 0, null);
	}

	public Edge(final Node start, final Node end, final float cornerY) {
		create(start, end, cornerY, null);
	}

	public Edge(final Node start, final Node end, final float cornerY, final Flow reference) {
		create(start, end, cornerY, reference);
	}

	private void create(final Node start, final Node end, final float cornerY, final Flow reference) {
		this.start = start;
		this.end = end;
		this.cornerY = cornerY;
		this.reference = reference;
	}

	public void draw(final GraphicsContext gc, final Map<Node, Integer> countDocks) {
		gc.setLineWidth(this.size);

		final float startX;
		final float endX;

		boolean fromTop = false;
		if (this.cornerY > 0) {
			fromTop = true;
		}
		if (countDocks.get(this.start) != null) {
			startX = this.start.getPosX()
					+ (this.start.getDockX(fromTop) * (this.start.getWidth() / countDocks.get(this.start)));
		} else {
			startX = this.start.getPosX() + (this.start.getDockX(fromTop) * (this.start.getWidth() / 2));
		}
		if (countDocks.get(this.end) != null) {
			endX = this.end.getPosX() + (this.end.getDockX(fromTop) * (this.end.getWidth() / countDocks.get(this.end)));
		} else {
			endX = this.end.getPosX() + (this.end.getDockX(fromTop) * (this.end.getWidth() / 2));
		}

		final float startY = this.start.getPosY() + (1f / 2f * this.start.getHeight());
		final float endY = this.end.getPosY();

		if (this.cornerY == 0) {
			gc.strokeLine(startX, startY, endX, endY);
			gc.fillPolygon(new double[] { endX - 5, endX, endX + 5 }, new double[] { endY - 10, endY, endY - 10 }, 3);
		} else if (this.cornerY < 0) {
			gc.strokeLine(startX, startY, endX, startY);
			gc.strokeLine(endX, startY, endX, endY);
			gc.fillPolygon(new double[] { endX - 5, endX, endX + 5 }, new double[] { endY - 10, endY, endY - 10 }, 3);
		} else {
			gc.strokeLine(startX, startY, startX, this.cornerY);
			gc.strokeLine(startX, this.cornerY, endX, this.cornerY);
			gc.strokeLine(endX, this.cornerY, endX, endY);
			gc.fillPolygon(new double[] { endX - 5, endX, endX + 5 }, new double[] { endY + 10 + this.end.getHeight(),
					endY + this.end.getHeight(), endY + 10 + this.end.getHeight() }, 3);
		}

	}

	public String tooltip() {
		return "Test";
	}

	// Generated Getters & Setters
	public Node getStart() {
		return this.start;
	}

	public Node getEnd() {
		return this.end;
	}

	public float getCornerY() {
		return this.cornerY;
	}

	public Flow getReference() {
		return this.reference;
	}

	public float getLength() {
		return this.length;
	}

	public int getSize() {
		return this.size;
	}

	public void setStart(final Node start) {
		this.start = start;
	}

	public void setEnd(final Node end) {
		this.end = end;
	}

	public void setCornerY(final float cornerY) {
		this.cornerY = cornerY;
	}

	public void setReference(final Flow reference) {
		this.reference = reference;
	}

	public void setLength(final float length) {
		this.length = length;
	}

	public void setSize(final int size) {
		this.size = size;
	}
}
