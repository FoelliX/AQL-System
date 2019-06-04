package de.foellix.aql.ui.gui.viewer.web;

public class Edge {
	public static final int STYLE_NORMAL = 0;
	public static final int STYLE_TRANSITIVLY_REPLACEABLE = 1;
	public static final int STYLE_TRANSITIVLY_REPLACEABLE_BUT_COMPLETE = 2;
	public static final int STYLE_COMPLETE = 3;
	public static final int STYLE_INTENT_SINK = 4;
	public static final int STYLE_INTENT_SOURCE = 5;
	public static final int STYLE_PERMISSION = 6;

	private int id;
	private Node from;
	private Node to;
	private int style;

	public Edge(int id, Node from, Node to, int style) {
		super();
		this.id = id;
		this.from = from;
		this.to = to;
		this.style = style;
	}

	public int getId() {
		return this.id;
	}

	public Node getFrom() {
		return this.from;
	}

	public Node getTo() {
		return this.to;
	}

	public int getStyle() {
		return this.style;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setFrom(Node from) {
		this.from = from;
	}

	public void setTo(Node to) {
		this.to = to;
	}

	public void setStyle(int style) {
		this.style = style;
	}

	public String toElement() {
		String color;
		int width = 1;
		switch (this.style) {
		case Edge.STYLE_COMPLETE:
			color = "#000000";
			width = 5;
			break;
		case Edge.STYLE_INTENT_SINK:
			color = "#c0d6a3";
			break;
		case Edge.STYLE_INTENT_SOURCE:
			color = "#f29c9c";
			break;
		case Edge.STYLE_PERMISSION:
			color = "#d6ace4";
			break;
		case Edge.STYLE_TRANSITIVLY_REPLACEABLE:
			color = "#CCCCCC";
			break;
		case Edge.STYLE_TRANSITIVLY_REPLACEABLE_BUT_COMPLETE:
			color = "#CCCCCC";
			width = 5;
			break;
		default:
			color = "#000000";
			width = 3;
			break;
		}

		return "{ \"data\": { \"id\": \"" + this.id + "\", \"source\": \"" + this.from.getId() + "\", \"target\": \""
				+ this.to.getId() + "\", \"color\": \"" + color + "\", \"width\": \"" + width + "\" }},";
	}
}