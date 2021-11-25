package de.foellix.aql.ui.gui.viewer.web;

import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.Permission;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.EqualsHelper;
import de.foellix.aql.helper.EqualsOptions;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.helper.KeywordsAndConstantsHelper;

public class Node {
	private int id;
	private Object item;
	private List<Reference> refs;

	public Node(int id, Object item) {
		super();
		this.id = id;
		this.item = item;
		this.refs = new ArrayList<>();
	}

	public int getId() {
		return this.id;
	}

	public Object getItem() {
		return this.item;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setItem(Object item) {
		this.item = item;
	}

	public String toElement() {
		String label;
		String color = "#ededed";

		if (this.item instanceof Intentsource) {
			color = "#f29c9c";
			if (((Intentsource) this.item).getTarget() != null) {
				if (((Intentsource) this.item).getTarget().getAction() != null
						&& !((Intentsource) this.item).getTarget().getAction().isEmpty()) {
					label = ((Intentsource) this.item).getTarget().getAction() + "\n"
							+ (((Intentsource) this.item).getTarget().getCategory() == null ? "-"
									: ((Intentsource) this.item).getTarget().getCategory());
				} else {
					if (((Intentsource) this.item).getTarget().getReference() != null
							&& ((Intentsource) this.item).getTarget().getReference().getClassname() != null) {
						label = ((Intentsource) this.item).getTarget().getReference().getClassname();
					} else {
						label = "No short info except:\nImplicit without action";
					}
				}
			} else {
				label = "No target info!";
			}
		} else if (this.item instanceof Intentsink) {
			color = "#c0d6a3";
			if (((Intentsink) this.item).getTarget() != null) {
				if (((Intentsink) this.item).getTarget().getAction() != null
						&& !((Intentsink) this.item).getTarget().getAction().isEmpty()) {
					label = ((Intentsink) this.item).getTarget().getAction() + "\n"
							+ (((Intentsink) this.item).getTarget().getCategory() == null ? "-"
									: ((Intentsink) this.item).getTarget().getCategory());
				} else {
					if (((Intentsink) this.item).getTarget().getReference() != null
							&& ((Intentsink) this.item).getTarget().getReference().getClassname() != null) {
						label = ((Intentsink) this.item).getTarget().getReference().getClassname();
					} else {
						label = "No short info except:\nImplicit without action";
					}
				}
			} else {
				label = "No target info!";
			}
		} else if (this.item instanceof Permission) {
			color = "#d6ace4";
			label = ((Permission) this.item).getName().replace("android.permission", "... ");
		} else if (this.item instanceof Reference) {
			if (this.refs.isEmpty()) {
				color = "#a3c3d6";
			} else {
				color = "#a3d6d6";
			}
			String statement = ((Reference) this.item).getStatement().getStatementfull();
			if (statement.contains(KeywordsAndConstantsHelper.CONSTRUCTOR_NAME)) {
				statement = statement.substring(0, statement.indexOf(':'));
				statement = statement.substring(statement.lastIndexOf('.') + 1) + " "
						+ KeywordsAndConstantsHelper.CONSTRUCTOR_NAME;
			} else if (statement.contains(KeywordsAndConstantsHelper.STATIC_CONSTRUCTOR_NAME)) {
				statement = statement.substring(0, statement.indexOf(':'));
				statement = statement.substring(statement.lastIndexOf('.') + 1) + " "
						+ KeywordsAndConstantsHelper.STATIC_CONSTRUCTOR_NAME;
			} else {
				statement = Helper.cut(Helper.cut(statement, "<", ">"), " ", Helper.OCCURENCE_LAST);
				if (Helper.cut(statement, "(", ")").length() > 22) {
					statement = Helper.cutFromStart(statement, "(") + "(..)";
				}
			}
			String app = Helper.cut(Helper.cut(((Reference) this.item).getApp().getFile(), "/", Helper.OCCURENCE_LAST),
					"\\", Helper.OCCURENCE_LAST);
			if (app.length() >= 34) {
				app = app.substring(0, 28) + "...apk";
			}
			label = statement + "\n" + Helper.cut(((Reference) this.item).getMethod(), " ", ">", Helper.OCCURENCE_LAST)
					+ "\n" + Helper.cut(((Reference) this.item).getClassname(), ".", Helper.OCCURENCE_LAST) + "\n"
					+ app;
		} else {
			label = super.toString();
		}

		final int height = height(label);
		final int width = width(label);
		label = escape(label);

		int counter = 1;
		final StringBuilder verbose = new StringBuilder(counter + ") " + escape(Helper.toString(this.item)));
		final String xml;
		if (!this.refs.isEmpty() && this.item instanceof Reference) {
			final Flow flow = new Flow();
			flow.getReference().add((Reference) this.item);
			for (final Reference ref : this.refs) {
				flow.getReference().add(ref);
				counter++;
				verbose.append(escape("\n\n" + counter + ") " + Helper.toString(ref)));
			}
			final Answer answerXML = new Answer();
			answerXML.setFlows(new Flows());
			answerXML.getFlows().getFlow().add(flow);
			xml = escape(AnswerHandler.createXMLString(answerXML));
		} else {
			xml = escape(AnswerHandler.createXMLString(this.item));
		}

		return "{ \"data\": { \"id\": \"" + this.id + "\", \"label\": \"" + label + "\", \"width\": \"" + width
				+ "px\", \"height\": \"" + height + "px\", \"color\": \"" + color + "\", \"verbose\": \"" + verbose
				+ "\", \"xml\": \"" + xml + "\" }},";
	}

	private int width(String label) {
		int max = 0;
		for (final String line : label.split("\\n")) {
			if (line.length() > max) {
				max = line.length();
			}
		}

		return max * 7;
	}

	private int height(String label) {
		return Math.max(1, Helper.countStringOccurences(label, "\n")) * 17;
	}

	private String escape(String input) {
		return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
	}

	public void addReference(Reference ref1) {
		final EqualsOptions options = new EqualsOptions();
		options.setOption(EqualsOptions.PRECISELY_REFERENCE, true);

		if (EqualsHelper.equals(ref1, (Reference) this.item, options)) {
			return;
		}
		for (final Reference ref2 : this.refs) {
			if (EqualsHelper.equals(ref1, ref2, options)) {
				return;
			}
		}
		this.refs.add(ref1);
	}
}