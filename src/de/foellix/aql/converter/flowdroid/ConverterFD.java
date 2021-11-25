package de.foellix.aql.converter.flowdroid;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.foellix.aql.Log;
import de.foellix.aql.converter.IConverter;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.helper.KeywordsAndConstantsHelper;
import de.foellix.aql.system.task.ConverterTask;
import de.foellix.aql.system.task.ConverterTaskInfo;

public class ConverterFD implements IConverter {
	private static final String EMPTY_RESULT_FILE_CONTENT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><DataFlowResults TerminationState=\"Success\"><Results></Results></DataFlowResults>";

	private App app;

	@Override
	public Answer parse(ConverterTask task) {
		final File resultFile = new File(task.getTaskInfo().getData(ConverterTaskInfo.RESULT_FILE));
		this.app = Helper.createApp(Helper.getAppFromData(task.getTaskInfo()));

		final Answer answer = new Answer();
		if (this.app != null) {
			answer.setFlows(new Flows());
			try {
				final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
				final DocumentBuilder domBuilder = domFactory.newDocumentBuilder();
				final Document resultDocument = domBuilder.parse(resultFile);
				resultDocument.getDocumentElement().normalize();

				// Results
				if (resultDocument.getElementsByTagName("Result") != null) {
					final NodeList results = resultDocument.getElementsByTagName("Result");
					for (int i = 0; i < results.getLength(); i++) {
						final Element resultNode = (Element) results.item(i);

						// Sink
						final Node sink = resultNode.getElementsByTagName("Sink").item(0);
						final Reference sinkRef = getReference(sink, false);

						// Sources
						final NodeList sources = ((Element) resultNode.getElementsByTagName("Sources").item(0))
								.getElementsByTagName("Source");
						for (int j = 0; j < sources.getLength(); j++) {
							final Reference sourceRef = getReference(sources.item(j), true);

							// Flows
							final Flow newFlow = new Flow();
							newFlow.getReference().add(sourceRef);
							newFlow.getReference().add(sinkRef);
							answer.getFlows().getFlow().add(newFlow);
						}
					}
				}
			} catch (final Exception e) {
				Log.error("Error while reading file: " + resultFile.getAbsolutePath() + Log.getExceptionAppendix(e));
			}
		} else {
			Log.error("Error while converting result: App object was not created!");
		}

		return answer;
	}

	private Reference getReference(Node sourceOrSinkNode, boolean isSource) {
		// Parse node
		final Element node = (Element) sourceOrSinkNode;
		final String statement = node.getAttribute("Statement");
		final String method = node.getAttribute("Method");
		final String classname = Helper.cut(method, "<", ": ");
		int linenumber;
		try {
			linenumber = (node.hasAttribute("LineNumber") && node.getAttribute("LineNumber") != null
					&& !node.getAttribute("LineNumber").isEmpty() ? Integer.parseInt(node.getAttribute("LineNumber"))
							: -1);
		} catch (final NumberFormatException e) {
			linenumber = -1;
		}

		// Create and return reference
		final Reference ref = new Reference();
		if (isSource) {
			ref.setType(KeywordsAndConstantsHelper.REFERENCE_TYPE_FROM);
		} else {
			ref.setType(KeywordsAndConstantsHelper.REFERENCE_TYPE_TO);
		}
		ref.setApp(this.app);
		ref.setClassname(classname);
		ref.setMethod(method);
		ref.setStatement(Helper.createStatement(statement));
		if (linenumber > 0) {
			ref.getStatement().setLinenumber(linenumber);
		}

		return ref;
	}

	@Override
	public File recoverResultFromOutput(List<String> output, File expectedResultFile) {
		if (output.contains("[main] WARN soot.jimple.infoflow.android.SetupApplication - No entry points")) {
			expectedResultFile = new File(expectedResultFile.getAbsolutePath().replace('*', '_'));
			try {
				Files.write(expectedResultFile.toPath(), EMPTY_RESULT_FILE_CONTENT.getBytes());
				Log.msg("Recovered answer file (\"" + expectedResultFile.getAbsolutePath()
						+ "\") from FlowDroid's output.", Log.DEBUG_DETAILED);
				return expectedResultFile;
			} catch (final IOException e) {
				Log.msg("Could not recover answer file (\"" + expectedResultFile.getAbsolutePath()
						+ "\") from FlowDroid's output.", Log.DEBUG_DETAILED);
			}
		}
		return IConverter.super.recoverResultFromOutput(output, expectedResultFile);
	}
}