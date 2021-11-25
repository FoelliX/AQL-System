package de.foellix.aql.converter.amandroid;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.Attribute;
import de.foellix.aql.datastructure.Attributes;
import de.foellix.aql.datastructure.Data;
import de.foellix.aql.datastructure.Intentfilter;
import de.foellix.aql.datastructure.Intentfilters;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsinks;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.Target;
import de.foellix.aql.helper.Helper;

public class ConverterAmandroidBase {
	protected static final String SEPARATOR = "###SEPARATOR###";

	protected File resultFile;
	protected List<File> sourceFiles;
	protected App app;

	protected Set<String> needles;
	protected Map<String, Reference> mapReferences;
	protected List<String> intentLines;

	protected String cleanUpIntentString(String original) {
		original = Helper.replaceDoubleSpaces(original);
		return original;
	}

	protected void computeIntents(Answer answer) {
		final Intentfilters intentfilters = new Intentfilters();
		final Intentsinks intentsinks = new Intentsinks();
		Reference ref = null;

		for (String line : this.intentLines) {
			if (line.startsWith("Component ")) {
				ref = new Reference();
				ref.setClassname(line.substring(10));
				ref.setApp(this.app);
			} else if (line.startsWith("      Caller Context: ")) {
				final String needle = line.substring(line.indexOf(",") + 1, line.indexOf(")"));
				ref = this.mapReferences.get(needle);
			} else if (line.startsWith(" IntentFilter:")) {
				final Intentfilter intentfilter = new Intentfilter();
				intentfilter.setReference(ref);
				Data data = null;

				line = line.substring(14);
				line = line.replace(",", SEPARATOR).replace("\"" + SEPARATOR + "\"", "\",\"");
				for (String part : line.split(SEPARATOR)) {
					if (part.contains("Data:[")) {
						part = part.replace("Data:[", "");
						data = new Data();
						intentfilter.getData().add(data);
					}
					if (part.contains(":")) {
						final String name = cleanUpIntentPart(part.substring(0, part.indexOf(":")));
						final String[] values = cleanUpIntentPart(Helper.cutFromFirstToLast(part, "\"", "\""))
								.split("\",\"");
						if (name.equals("Actions")) {
							for (final String value : values) {
								intentfilter.getAction().add(value);
							}
						} else if (name.equals("Categories")) {
							for (final String value : values) {
								intentfilter.getCategory().add(value);
							}
						} else if (!values[0].isEmpty()) {
							if (name.equals("Hosts")) {
								data.setHost(values[0]);
							} else if (name.equals("MimeTypes")) {
								data.setType(values[0]);
							} else if (name.equals("PathPatterns")) {
								data.setPathPattern(values[0]);
							} else if (name.equals("PathPrefixs")) {
								data.setPathPrefix(values[0]);
							} else if (name.equals("Paths")) {
								data.setPath(values[0]);
							} else if (name.equals("Ports")) {
								data.setPort(values[0]);
							} else if (name.equals("Schemes")) {
								data.setScheme(values[0]);
							}
						}
					}
				}
				intentfilters.getIntentfilter().add(intentfilter);
			} else if (line.startsWith(" Intent:")) {
				final Intentsink intentsink = new Intentsink();
				intentsink.setReference(ref);
				final Target target = new Target();
				intentsink.setTarget(target);
				final Data data = new Data();
				final Attributes attributes = new Attributes();
				intentsink.setAttributes(attributes);

				line = line.substring(9);
				line = line.replace("Component Names:", "Component-Names:").replace("ICC Targets:", "ICC-Targets:")
						.replace(": ", ":").replace("= ", "=").replace(" ", SEPARATOR);
				if (line.contains("ICC-Targets:")) {
					final String lineICC = line.substring(line.indexOf("ICC-Targets:")).replace(SEPARATOR, ",");
					line = line.substring(0, line.indexOf("ICC-Targets:")) + lineICC;
				}
				String name = null;
				for (String part : line.split(SEPARATOR)) {
					final String value;
					if (part.contains("=")) {
						if (part.startsWith("Data:")) {
							part = part.substring(5);
						}
						name = cleanUpIntentPart(part.substring(0, part.indexOf("=")));
						value = cleanUpIntentPart(part.substring(part.indexOf("=") + 1));
					} else if (part.contains(":")) {
						name = cleanUpIntentPart(part.substring(0, part.indexOf(":")));
						value = cleanUpIntentPart(part.substring(part.indexOf(":") + 1));
					} else {
						value = cleanUpIntentPart(part);
					}
					if (name != null && !value.equals("null")) {
						if (name.equals("Component-Names")) {
							final Reference targetComponent = new Reference();
							targetComponent.setClassname(value);
							targetComponent.setApp(this.app);
							intentsink.getTarget().setReference(targetComponent);
						} else if (name.equals("Actions")) {
							intentsink.getTarget().getAction().add(value);
						} else if (name.equals("Categories")) {
							intentsink.getTarget().getCategory().add(value);
						} else if (name.equals("host")) {
							data.setHost(value);
							if (!target.getData().contains(data)) {
								target.getData().add(data);
							}
						} else if (name.equals("Types")) {
							data.setType(value);
							if (!target.getData().contains(data)) {
								target.getData().add(data);
							}
						} else if (name.equals("pathPattern")) {
							data.setPathPattern(value);
							if (!target.getData().contains(data)) {
								target.getData().add(data);
							}
						} else if (name.equals("pathPrefix")) {
							data.setPathPrefix(value);
							if (!target.getData().contains(data)) {
								target.getData().add(data);
							}
						} else if (name.equals("path")) {
							data.setPath(value);
							if (!target.getData().contains(data)) {
								target.getData().add(data);
							}
						} else if (name.equals("port")) {
							data.setPort(value);
							if (!target.getData().contains(data)) {
								target.getData().add(data);
							}
						} else if (name.equals("schemes")) {
							data.setScheme(value);
							if (!target.getData().contains(data)) {
								target.getData().add(data);
							}
						} else if (name != null && !name.isBlank() && !name.equals("null") && value != null
								&& !value.isBlank() && !value.equals("null")) {
							final Attribute attribute = new Attribute();
							attribute.setName(name);
							attribute.setValue(value);
							attributes.getAttribute().add(attribute);
						}
					}
				}
				if (!Helper.isEmpty(intentsink.getTarget())) {
					intentsinks.getIntentsink().add(intentsink);
				}
			}
		}

		if (intentfilters.getIntentfilter().size() > 0) {
			answer.setIntentfilters(intentfilters);
		}
		if (intentsinks.getIntentsink().size() > 0) {
			answer.setIntentsinks(intentsinks);
		}
	}

	protected String cleanUpIntentPart(String part) {
		return part.replaceAll("[\\[\\]\\(\\)]", "");
	}
}