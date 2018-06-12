package de.foellix.aql.converter.droidsafe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.foellix.aql.Log;
import de.foellix.aql.converter.IConverter;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.ToolTaskInfo;

public class ConverterDroidSafe implements IConverter {
	private JSONReader json;

	@Override
	public Answer parse(File resultFolder, ToolTaskInfo taskInfo) {
		final Answer answer = new Answer();
		final Flows flows = new Flows();

		resultFolder = new File(resultFolder, "droidsafe-gen");
		final File jsonFile = new File(resultFolder, "user_call_graph.json");
		final File resultFile = new File(resultFolder, "info-flow-results.txt");

		// Parse json file
		try {
			final ObjectMapper mapper = new ObjectMapper();
			this.json = mapper.readValue(jsonFile, JSONReader.class);
		} catch (final IOException e) {
			Log.error("Error reading DroidSafe's .json result file: " + jsonFile);
			return null;
		}

		// Parse result file
		try {
			final FileReader fr = new FileReader(resultFile);
			final BufferedReader br = new BufferedReader(fr);

			Reference to = null;
			Reference from = null;
			boolean sources = false;

			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.startsWith("Sink: ")) {
					to = new Reference();
					to.setStatement(Helper.fromStatementString(Helper.cutFromFirstToLast(line, "<", ">")));
					final String method = findMethodRecursively(to.getStatement().getStatementgeneric());
					if (method != null) {
						to.setMethod(method);
						to.setClassname(Helper.cut(to.getMethod(), "<", ":"));
						to.setApp(taskInfo.getQuestion().getReferences().get(0).getApp());
						to.setType(KeywordsAndConstants.REFERENCE_TYPE_TO);
					} else {
						Log.error("Could not find method for the following statement: "
								+ to.getStatement().getStatementgeneric());
						to = null;
					}
				} else if (line.contains("Sources:") && to != null) {
					sources = true;
				} else if (line.equals("")) {
					to = null;
					sources = false;
				} else if (sources) {
					from = new Reference();
					from.setStatement(Helper.fromStatementString(Helper.cut(line, "\t", " (UNIQUE_IDENTIFIER)")));
					final String method = findMethodRecursively(from.getStatement().getStatementgeneric());
					if (method != null) {
						from.setMethod(method);
						from.setClassname(Helper.cut(from.getMethod(), "<", ":"));
						from.setApp(taskInfo.getQuestion().getReferences().get(0).getApp());
						from.setType(KeywordsAndConstants.REFERENCE_TYPE_FROM);

						final Flow flow = new Flow();
						flow.getReference().add(from);
						flow.getReference().add(to);
						flows.getFlow().add(flow);
					}
				}
			}

			fr.close();
			br.close();
		} catch (final IOException e) {
			Log.error("Error reading DroidSafe's result file: " + resultFolder);
			return null;
		}
		if (!flows.getFlow().isEmpty()) {
			answer.setFlows(flows);
		}

		return answer;
	}

	private String findMethodRecursively(String statement) {
		for (final Content c : this.json.getContents()) {
			if (c.getContents() != null && !c.getContents().isEmpty()) {
				final Content temp = findMethodRecursively(statement, c);
				if (temp != null) {
					return temp.getSignature();
				}
			}
		}
		return null;
	}

	private Content findMethodRecursively(String statement, Content parent) {
		for (final Content c : parent.getContents()) {
			if (c.getSignature().substring(1, c.getSignature().length() - 1).equals(statement)) {
				return parent;
			}
			if (c.getContents() != null && !c.getContents().isEmpty()) {
				final Content temp = findMethodRecursively(statement, c);
				if (temp != null) {
					return temp;
				}
			}
		}
		return null;
	}
}
