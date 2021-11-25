package de.foellix.aql.converter.horndroid;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

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

public class ConverterHD implements IConverter {
	@Override
	public Answer parse(ConverterTask task) {
		final File resultFile = new File(task.getTaskInfo().getData(ConverterTaskInfo.RESULT_FILE));
		final App app = Helper.createApp(Helper.getAppFromData(task.getTaskInfo()));

		final Answer answer = new Answer();

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
				sink.setType(KeywordsAndConstantsHelper.REFERENCE_TYPE_TO);

				sink.setApp(app);
				sink.setClassname(
						Helper.cut(re.getDescription(), " of the class L", "; to the sink ").replace("/", "."));
				sink.setMethod(Helper.cut(re.getDescription(), " in method ", "V of the class ").replace("/", ".")
						.replace("(L", "(").replace(";)", ")").replace(";", ","));
				String statement = Helper.cut(re.getDescription(), "; to the sink ", ")V") + ")";
				statement = "<unknown.pkg.and.Class: unknown.return.Type "
						+ statement.replace("(L", "(").replace(";L", ",").replace(";", "").replace("/", ".") + ">()";
				sink.setStatement(Helper.createStatement(statement, false));

				// Create flow
				final Flow flow = new Flow();
				flow.getReference().add(sink);
				if (answer.getFlows() == null) {
					answer.setFlows(new Flows());
				}
				answer.getFlows().getFlow().add(flow);
			}
		}

		return answer;
	}
}