package de.foellix.aql.ui.gui.viewer.graph;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Attribute;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.Permission;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.Helper;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;

public class Node {
	private final int fontSize = 11;
	private int lines = 1;

	private float posX, posY;
	private float width, height;
	private Object reference;
	private int dockX1 = 0, dockX2 = 0;

	public Node(final float posX, final float posY) {
		create(posX, posY, 1, 1, 1, 1, 0, 0, null);
	}

	public Node(final float posX, final float posY, final float width, final float height, final float maxWidth,
			final int maxHeight) {
		create(posX, posY, width, height, maxWidth, maxHeight, 0, 0, null);
	}

	public Node(final float posX, final float posY, final float width, final float height, final float maxWidth,
			final int maxHeight, final float minWidth, final int minHeight, final Object reference) {
		create(posX, posY, width, height, maxWidth, maxHeight, minWidth, minHeight, reference);
	}

	private void create(final float posX, final float posY, final float width, final float height, final float maxWidth,
			final int maxHeight, final float minWidth, final int minHeight, final Object reference) {
		this.posX = posX;
		this.posY = posY;
		this.width = Math.max(Math.min(width, maxWidth), minWidth);
		this.height = Math.max(Math.min(height, maxHeight), minHeight);
		this.reference = reference;
	}

	public void draw(final GraphicsContext gc) {
		draw(gc, false);
	}

	public void draw(final GraphicsContext gc, final boolean rotate) {
		gc.fillRoundRect(this.posX, this.posY, this.width, this.height, 10, 10);
		gc.strokeRoundRect(this.posX, this.posY, this.width, this.height, 10, 10);

		final Paint p = gc.getFill();
		gc.setFill(Color.BLACK);
		gc.setFont(Font.font("Tahoma", FontWeight.BOLD, this.fontSize));

		if (rotate) {
			gc.save();
			final Rotate r = new Rotate(90, this.posX, this.posY);
			gc.setTransform(r.getMxx(), r.getMyx(), r.getMxy(), r.getMyy(), r.getTx(), r.getTy());

			gc.fillText(this.toString(), this.posX + 10, this.posY - (this.width / 2) + (this.fontSize / 2)
					- (this.fontSize / 2 * (this.lines - 1)) - (1 * this.lines));
			gc.restore();
		} else {
			gc.fillText(this.toString(), this.posX + 10, this.posY + (this.height / 2) + (this.fontSize / 2)
					- (this.fontSize / 2 * (this.lines - 1)) - (1 * this.lines));
		}

		gc.setFill(p);
	}

	public int getDockX(final boolean fromTop) {
		if (fromTop) {
			this.dockX1++;
			return this.dockX1;
		} else {
			this.dockX2++;
			return this.dockX2;
		}
	}

	@Override
	public String toString() {
		if (this.reference instanceof Intentsource) {
			if (((Intentsource) this.reference).getTarget().getAction() != null
					&& !((Intentsource) this.reference).getTarget().getAction().isEmpty()) {
				this.lines = 2;
				return ((Intentsource) this.reference).getTarget().getAction() + "\n"
						+ (((Intentsource) this.reference).getTarget().getCategory() == null ? "-"
								: ((Intentsource) this.reference).getTarget().getCategory());
			} else {
				this.lines = 1;
				if (((Intentsource) this.reference).getTarget().getReference() != null
						&& ((Intentsource) this.reference).getTarget().getReference().getClassname() != null) {
					return ((Intentsource) this.reference).getTarget().getReference().getClassname();
				} else {
					this.lines = 2;
					return "No short info except:\nImplicit without action";
				}
			}
		} else if (this.reference instanceof Intentsink) {
			if (((Intentsink) this.reference).getTarget().getAction() != null
					&& !((Intentsink) this.reference).getTarget().getAction().isEmpty()) {
				this.lines = 2;
				return ((Intentsink) this.reference).getTarget().getAction() + "\n"
						+ (((Intentsink) this.reference).getTarget().getCategory() == null ? "-"
								: ((Intentsink) this.reference).getTarget().getCategory());
			} else {
				this.lines = 1;
				if (((Intentsink) this.reference).getTarget().getReference() != null
						&& ((Intentsink) this.reference).getTarget().getReference().getClassname() != null) {
					return ((Intentsink) this.reference).getTarget().getReference().getClassname();
				} else {
					this.lines = 2;
					return "No short info except:\nImplicit without action";
				}
			}
		} else if (this.reference instanceof Permission) {
			return ((Permission) this.reference).getName().replace("android.permission", "... ");
		} else if (this.reference instanceof Reference) {
			this.lines = 4;
			String statement = Helper.cut(
					Helper.cut(((Reference) this.reference).getStatement().getStatementfull(), "<", ">"), " ",
					Helper.OCCURENCE_LAST);
			if (Helper.cut(statement, "(", ")").length() > 22) {
				statement = Helper.cutFromStart(statement, "(") + "(..)";
			}
			String app = Helper.cut(
					Helper.cut(((Reference) this.reference).getApp().getFile(), "/", Helper.OCCURENCE_LAST), "\\",
					Helper.OCCURENCE_LAST);
			if (app.length() >= 34) {
				app = app.substring(0, 28) + "...apk";
			}
			return statement + "\n"
					+ Helper.cut(((Reference) this.reference).getMethod(), " ", ">", Helper.OCCURENCE_LAST) + "\n"
					+ Helper.cut(((Reference) this.reference).getClassname(), ".", Helper.OCCURENCE_LAST) + "\n" + app;
		} else {
			return super.toString();
		}
	}

	public String tooltip() {
		if (this.reference instanceof Intentsource) {
			final Intentsource reference = ((Intentsource) this.reference);

			String attrStr = "";
			if (reference.getAttributes() != null) {
				for (final Attribute attr : reference.getAttributes().getAttribute()) {
					if (attrStr.equals("")) {
						attrStr = "\n\nAttributes:\n";
					}
					attrStr += attr.getName() + ": " + attr.getValue() + "\n";
				}
			}

			return "IntentSource\n\n" + Helper.toString(reference.getTarget()) + attrStr;
		} else if (this.reference instanceof Intentsink) {
			final Intentsink reference = ((Intentsink) this.reference);

			String attrStr = "";
			if (reference.getAttributes() != null) {
				for (final Attribute attr : reference.getAttributes().getAttribute()) {
					if (attrStr.equals("")) {
						attrStr = "\n\nAttributes:\n";
					}
					attrStr += attr.getName() + ": " + attr.getValue() + "\n";
				}
			}

			return "IntentSink\n\n" + Helper.toString(reference.getTarget()) + attrStr;
		} else if (this.reference instanceof Permission) {
			final Permission reference = ((Permission) this.reference);

			String attrStr = "";
			if (reference.getAttributes() != null) {
				for (final Attribute attr : reference.getAttributes().getAttribute()) {
					if (attrStr.equals("")) {
						attrStr = "\n\nAttributes:\n";
					}
					attrStr += attr.getName() + ": " + attr.getValue() + "\n";
				}
			}

			return "Permission\n\nName: " + reference.getName() + attrStr;
		} else if (this.reference instanceof Reference) {
			final Reference reference = ((Reference) this.reference);

			return "Reference\n\nStatement: " + reference.getStatement().getStatementfull() + "\n" + "Method: "
					+ reference.getMethod() + "\n" + "Class: " + reference.getClassname() + "\n" + "App: "
					+ reference.getApp().getFile();
		} else {
			return super.toString();
		}
	}

	public String tooltip2() {
		final String tooltip = AnswerHandler.createXMLString(this.reference);
		Log.msg("XML:\n" + tooltip, Log.DEBUG_DETAILED);
		return tooltip;
	}

	// Generated Getters & Setters
	public float getPosX() {
		return this.posX;
	}

	public float getPosY() {
		return this.posY;
	}

	public float getWidth() {
		return this.width;
	}

	public float getHeight() {
		return this.height;
	}

	public Object getReference() {
		return this.reference;
	}

	public void setPosX(final int posX) {
		this.posX = posX;
	}

	public void setPosY(final int posY) {
		this.posY = posY;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	public void setHeight(final int height) {
		this.height = height;
	}

	public void setReference(final Object reference) {
		this.reference = reference;
	}
}
