package de.foellix.aql.ui.gui;

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
			if (((Intentsource) this.reference).getTarget().getAction() != null) {
				this.lines = 2;
				return ((Intentsource) this.reference).getTarget().getAction() + "\n"
						+ (((Intentsource) this.reference).getTarget().getCategory() == null ? "-"
								: ((Intentsource) this.reference).getTarget().getCategory());
			} else {
				this.lines = 1;
				return ((Intentsource) this.reference).getTarget().getReference().getClassname();
			}
		} else if (this.reference instanceof Intentsink) {
			if (((Intentsink) this.reference).getTarget().getAction() != null) {
				this.lines = 2;
				return ((Intentsink) this.reference).getTarget().getAction() + "\n"
						+ (((Intentsink) this.reference).getTarget().getCategory() == null ? "-"
								: ((Intentsink) this.reference).getTarget().getCategory());
			} else {
				this.lines = 1;
				return ((Intentsink) this.reference).getTarget().getReference().getClassname();
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
			return statement + "\n"
					+ Helper.cut(((Reference) this.reference).getMethod(), " ", ">", Helper.OCCURENCE_LAST) + "\n"
					+ Helper.cut(((Reference) this.reference).getClassname(), ".", Helper.OCCURENCE_LAST) + "\n"
					+ Helper.cut(
							Helper.cut(((Reference) this.reference).getApp().getFile(), "/", Helper.OCCURENCE_LAST),
							"\\", Helper.OCCURENCE_LAST);
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

			String returnStr = "";
			if (reference.getTarget().getAction() != null) {
				returnStr += "Action: " + reference.getTarget().getAction();
			}
			if (reference.getTarget().getCategory() != null) {
				returnStr += "\n" + "Category: " + reference.getTarget().getCategory();
			}
			if (reference.getTarget().getData() != null) {
				if (reference.getTarget().getData().getHost() != null) {
					returnStr += "\n" + "Host: " + reference.getTarget().getData().getHost();
				}
				if (reference.getTarget().getData().getPath() != null) {
					returnStr += "\n" + "Path: " + reference.getTarget().getData().getPath();
				}
				if (reference.getTarget().getData().getPort() != null) {
					returnStr += "\n" + "Port: " + reference.getTarget().getData().getPort();
				}
				if (reference.getTarget().getData().getScheme() != null) {
					returnStr += "\n" + "Scheme: " + reference.getTarget().getData().getScheme();
				}
				if (reference.getTarget().getData().getSsp() != null) {
					returnStr += "\n" + "SSP: " + reference.getTarget().getData().getSsp();
				}
				if (reference.getTarget().getData().getType() != null) {
					returnStr += "\n" + "Type: " + reference.getTarget().getData().getType();
				}
			}
			if (reference.getTarget().getReference() != null) {
				returnStr += "\n" + "Class: " + reference.getTarget().getReference().getClassname();
			}

			return "IntentSource\n\n" + returnStr + attrStr;
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

			String returnStr = "";
			if (reference.getTarget() != null) {
				if (reference.getTarget().getAction() != null) {
					returnStr += "Action: " + reference.getTarget().getAction();
				}
				if (reference.getTarget().getCategory() != null) {
					returnStr += "\n" + "Category: " + reference.getTarget().getCategory();
				}
				if (reference.getTarget().getData() != null) {
					if (reference.getTarget().getData().getHost() != null) {
						returnStr += "\n" + "Host: " + reference.getTarget().getData().getHost();
					}
					if (reference.getTarget().getData().getPath() != null) {
						returnStr += "\n" + "Path: " + reference.getTarget().getData().getPath();
					}
					if (reference.getTarget().getData().getPort() != null) {
						returnStr += "\n" + "Port: " + reference.getTarget().getData().getPort();
					}
					if (reference.getTarget().getData().getScheme() != null) {
						returnStr += "\n" + "Scheme: " + reference.getTarget().getData().getScheme();
					}
					if (reference.getTarget().getData().getSsp() != null) {
						returnStr += "\n" + "SSP: " + reference.getTarget().getData().getSsp();
					}
					if (reference.getTarget().getData().getType() != null) {
						returnStr += "\n" + "Type: " + reference.getTarget().getData().getType();
					}
				}
			}
			if (reference.getTarget().getReference() != null) {
				returnStr += "\n" + "Class: " + reference.getTarget().getReference().getClassname();
			}

			return "IntentSink\n\n" + returnStr + attrStr;
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
