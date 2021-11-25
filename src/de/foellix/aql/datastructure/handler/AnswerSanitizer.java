package de.foellix.aql.datastructure.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;

public class AnswerSanitizer {
	private static final String[] ELEMTNS_WITH_ATTRIBUTES = { "permission", "intentsource", "intentsink", "intent",
			"intentfilter", "flow", "source", "sink" };
	private static final List<String> ALL_KNOWN_ELEMENTS = Arrays.asList(new String[] { "hash", "file", "hashes",
			"method", "classname", "app", "name", "value", "attribute", "reference", "attributes", "permission", "type",
			"scheme", "ssp", "sspPattern", "sspPrefix", "host", "port", "path", "pathPattern", "pathPrefix", "action",
			"category", "data", "parameter", "statementfull", "statementgeneric", "linenumber", "parameters",
			"statement", "target", "intentsource", "intentsink", "intent", "intentfilter", "flow", "source", "sink",
			"permissions", "intentsources", "intentsinks", "intents", "intentfilters", "flows", "sources", "sinks",
			"answer" });

	/**
	 * Attempts to sanitize a corrupted AQL-Answer by adding a root node if missing and converting unknown elements into attributes if possible
	 *
	 * @param xmlFile
	 * @return sanitized content of input xmlFile
	 */
	public static String sanitize(File xmlFile) {
		try {
			// Parse
			final InputStream is = new FileInputStream(xmlFile);
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			final DocumentBuilder builder = factory.newDocumentBuilder();

			// Check Root
			final Document oldDoc = builder.parse(is);
			final Element oldRoot = oldDoc.getDocumentElement();
			final Document newDoc;
			if (oldRoot.getNodeName().equals("answer")) {
				newDoc = oldDoc;
			} else {
				newDoc = builder.newDocument();
				final Element newRoot = newDoc.createElement("answer");
				newDoc.appendChild(newRoot);
				newRoot.appendChild(newDoc.importNode(oldRoot, true));
			}

			// Convert unknown elements
			for (final String listElement : ELEMTNS_WITH_ATTRIBUTES) {
				final NodeList nl = newDoc.getElementsByTagName(listElement);
				for (int i = 0; i < nl.getLength(); i++) {
					final Node nlE = nl.item(i);
					final NodeList childNl = nlE.getChildNodes();

					// Get attributes element
					Node attributes = null;
					for (int j = 0; j < childNl.getLength(); j++) {
						final Node childNlE = childNl.item(j);
						if (childNlE.getNodeName().equals("attributes")) {
							attributes = childNlE;
						}
					}
					if (attributes == null) {
						final Node newAttributes = newDoc.createElement("attributes");
						attributes = newAttributes;
						nlE.appendChild(attributes);
					}

					for (int j = 0; j < childNl.getLength(); j++) {
						final Node childNlE = childNl.item(j);
						if (childNlE.getNodeName().startsWith("#") || childNlE.getNodeName().equals("attributes")) {
							continue;
						}
						if (!ALL_KNOWN_ELEMENTS.contains(childNlE.getNodeName())) {
							// Create converted attribute
							final Node newAttribute = newDoc.createElement("attribute");
							final Node newName = newDoc.createElement("name");
							newName.setTextContent(childNlE.getNodeName());
							final Node newValue = newDoc.createElement("value");
							newValue.setTextContent(childNlE.getTextContent());
							newAttribute.appendChild(newName);
							newAttribute.appendChild(newValue);

							attributes.appendChild(newAttribute);
						}
					}
				}
			}

			// Write to string
			final DOMSource domSource = new DOMSource(newDoc);
			final StringWriter writer = new StringWriter();
			final StreamResult result = new StreamResult(writer);
			final TransformerFactory tf = TransformerFactory.newInstance();
			final Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);

			return writer.toString();
		} catch (final Exception e) {
			Log.error("Could not parse AQL-Answer: " + xmlFile.getAbsolutePath() + Log.getExceptionAppendix(e));
			return AnswerHandler.createXMLString(new Answer());
		}
	}
}