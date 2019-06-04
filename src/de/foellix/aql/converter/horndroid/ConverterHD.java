package de.foellix.aql.converter.horndroid;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.foellix.aql.Log;
import de.foellix.aql.converter.IConverter;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.Permission;
import de.foellix.aql.datastructure.Permissions;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.ToolTaskInfo;

public class ConverterHD implements IConverter {
	@Override
	public Answer parse(final File resultFile, final ToolTaskInfo taskInfo) {
		final Answer answer = new Answer();
		answer.setFlows(new Flows());

		// Add permission to mark HornDroid execution as successful
		answer.setPermissions(new Permissions());
		final Permission p = new Permission();
		p.setName("HORNDROID_SUCCESSFULLY_EXECUTED");
		answer.getPermissions().getPermission().add(p);

		// Parse json file
		final JSONReader json;
		try {
			final ObjectMapper mapper = new ObjectMapper();
			json = mapper.readValue(resultFile, JSONReader.class);
		} catch (final IOException e) {
			Log.error("Error reading HornDroid's .json result file: " + resultFile);
			return null;
		}

		for (final ReportEntry re : json.getReportEntries()) {
			if (!re.getResult().equals("NO LEAK")) {
				// Create Reference
				final Reference sink = new Reference();
				sink.setType(KeywordsAndConstants.REFERENCE_TYPE_TO);

				sink.setApp(taskInfo.getQuestion().getAllReferences().iterator().next().getApp());
				sink.setClassname(
						Helper.cut(re.getDescription(), " of the class L", "; to the sink ").replaceAll("/", "."));
				sink.setMethod(Helper.cut(re.getDescription(), " in method ", "V of the class ").replaceAll("/", ".")
						.replaceAll("\\(L", "(").replaceAll(";\\)", ")").replaceAll(";", ","));
				String statement = Helper.cut(re.getDescription(), "; to the sink ", ")V") + ")";
				statement = "<unknown.pkg.and.Class: unknown.return.Type " + statement.replaceAll("\\(L", "(")
						.replaceAll(";L", ",").replaceAll(";", "").replaceAll("/", ".") + ">";
				sink.setStatement(Helper.createStatement(statement, false));

				// Create flow
				final Flow flow = new Flow();
				flow.getReference().add(sink);
				answer.getFlows().getFlow().add(flow);
			}
		}

		return answer;
	}
}
