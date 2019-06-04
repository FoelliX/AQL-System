package de.foellix.aql.ui.gui.viewer.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Attribute;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.Permission;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.EqualsHelper;
import de.foellix.aql.helper.EqualsOptions;
import de.foellix.aql.helper.Helper;

public class WebRepresentation {
	private int counter;
	private Map<String, Node> map;
	private Collection<Edge> edges;

	public WebRepresentation() {
		this.map = new HashMap<>();
		this.edges = new ArrayList<>();
	}

	public String toJson(Answer answer) {
		this.counter = 0;
		this.map.clear();
		this.edges.clear();

		if (answer.getFlows() != null && !answer.getFlows().getFlow().isEmpty()) {
			for (final Flow item : answer.getFlows().getFlow()) {
				// Create edge
				final Reference fromRef = Helper.getFrom(item.getReference());
				final Node from = addNode(fromRef, Helper.toRAW(fromRef, true));
				final Reference toRef = Helper.getTo(item.getReference());
				final Node to = addNode(toRef, Helper.toRAW(toRef, true));
				this.counter++;

				// Set edge style
				int style = Edge.STYLE_NORMAL;
				if (item.getAttributes() != null) {
					for (final Attribute attr : item.getAttributes().getAttribute()) {
						if (attr.getName().equals("complete") && attr.getValue().equals("true")) {
							style = Edge.STYLE_COMPLETE;
							break;
						}
					}
				}
				final List<Flow> toCandidates = new ArrayList<>();
				for (final Flow temp : answer.getFlows().getFlow()) {
					if (temp == item) {
						continue;
					}
					final Reference tempTo = Helper.getTo(temp);
					if (EqualsHelper.equals(toRef, tempTo,
							EqualsOptions.DEFAULT.setOption(EqualsOptions.PRECISELY_REFERENCE, true))) {
						toCandidates.add(temp);
					}
				}
				final List<Flow> fromCandidates = new ArrayList<>();
				for (final Flow temp : answer.getFlows().getFlow()) {
					if (temp == item) {
						continue;
					}
					final Reference tempFrom = Helper.getFrom(temp);
					if (EqualsHelper.equals(fromRef, tempFrom,
							EqualsOptions.DEFAULT.setOption(EqualsOptions.PRECISELY_REFERENCE, true))) {
						fromCandidates.add(temp);
					}
				}
				for (final Flow tempTo : toCandidates) {
					final Reference commonFrom = Helper.getFrom(tempTo);
					for (final Flow tempFrom : fromCandidates) {
						final Reference commonTo = Helper.getTo(tempFrom);
						if (EqualsHelper.equals(commonFrom, commonTo,
								EqualsOptions.DEFAULT.setOption(EqualsOptions.PRECISELY_REFERENCE, true))) {
							if (style == Edge.STYLE_NORMAL) {
								style = Edge.STYLE_TRANSITIVLY_REPLACEABLE;
							} else {
								style = Edge.STYLE_TRANSITIVLY_REPLACEABLE_BUT_COMPLETE;
							}
							break;
						}
					}
					if (style != Edge.STYLE_NORMAL) {
						break;
					}
				}

				// Add edge
				this.edges.add(new Edge(this.counter, from, to, style));
			}
		}
		if (answer.getPermissions() != null && !answer.getPermissions().getPermission().isEmpty()) {
			for (final Permission item : answer.getPermissions().getPermission()) {
				final Node ref = addNode(item.getReference(), Helper.toRAW(item.getReference(), true));
				final Node permission = addNode(item, Helper.toRAW(item));
				this.counter++;
				this.edges.add(new Edge(this.counter, permission, ref, Edge.STYLE_PERMISSION));
			}
		}
		if (answer.getIntentsinks() != null && !answer.getIntentsinks().getIntentsink().isEmpty()) {
			for (final Intentsink item : answer.getIntentsinks().getIntentsink()) {
				final Node ref = addNode(item.getReference(), Helper.toRAW(item.getReference(), true));

				final Intentsink temp = new Intentsink();
				temp.setTarget(item.getTarget());
				temp.setAttributes(item.getAttributes());
				final Node intentsink = addNode(temp, Helper.toRAW(temp));
				this.counter++;
				this.edges.add(new Edge(this.counter, intentsink, ref, Edge.STYLE_INTENT_SINK));
			}
		}
		if (answer.getIntentsources() != null && !answer.getIntentsources().getIntentsource().isEmpty()) {
			for (final Intentsource item : answer.getIntentsources().getIntentsource()) {
				final Node ref = addNode(item.getReference(), Helper.toRAW(item.getReference(), true));

				final Intentsource temp = new Intentsource();
				temp.setTarget(item.getTarget());
				temp.setAttributes(item.getAttributes());
				final Node intentsource = addNode(temp, Helper.toRAW(temp));
				this.counter++;
				this.edges.add(new Edge(this.counter, intentsource, ref, Edge.STYLE_INTENT_SOURCE));
			}
		}

		final StringBuilder sb = new StringBuilder("{ \"nodes\": [");
		for (final Node node : this.map.values()) {
			sb.append(node.toElement());
		}
		sb.setLength(sb.length() - 1);
		sb.append("], \"edges\": [");
		for (final Edge edge : this.edges) {
			sb.append(edge.toElement());
		}
		sb.setLength(sb.length() - 1);
		sb.append("] }");

		return sb.toString();
	}

	private Node addNode(Object obj, String identifier) {
		if (!this.map.containsKey(identifier)) {
			this.counter++;
			final Node node = new Node(this.counter, obj);
			this.map.put(identifier, node);
			return node;
		} else {
			final Node node = this.map.get(identifier);
			if (obj instanceof Reference) {
				node.addReference((Reference) obj);
			}
			return node;
		}
	}
}
