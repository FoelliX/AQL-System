package de.foellix.aql.converter.iccta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import de.foellix.aql.Log;
import de.foellix.aql.converter.IConverter;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.Statement;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.helper.KeywordsAndConstantsHelper;
import de.foellix.aql.system.task.ConverterTask;
import de.foellix.aql.system.task.ConverterTaskInfo;

public class ConverterIccTA implements IConverter {
	@Override
	public Answer parse(ConverterTask task) {
		final File resultFile = new File(task.getTaskInfo().getData(ConverterTaskInfo.RESULT_FILE));
		final App app = Helper.createApp(Helper.getAppFromData(task.getTaskInfo()));

		final Answer answer = new Answer();
		answer.setFlows(new Flows());

		try {
			Flow flow = new Flow();
			Reference to = null;

			final FileReader fr = new FileReader(resultFile);
			final BufferedReader br = new BufferedReader(fr);

			String zeile = "";
			while ((zeile = br.readLine()) != null) {
				if (zeile.contains("The sink")
						&& zeile.contains("was called with values from the following sources:")) {
					final String statement = Helper.cut(zeile, "The sink ", " in method ");
					final String method = Helper.cut(zeile, " in method ",
							" was called with values from the following sources:");
					final String classname = Helper.cut(method, "<", ": ");

					to = new Reference();
					to.setType(KeywordsAndConstantsHelper.REFERENCE_TYPE_TO);
					to.setApp(app);
					to.setClassname(classname);
					to.setMethod(method);
					to.setStatement(createStatement(statement));

					flow = new Flow();
					flow.getReference().add(to);
				} else if (zeile.contains("[main] INFO soot.jimple.infoflow.Infoflow - - ")
						&& zeile.contains(" in method ")) {
					final String statement = Helper.cut(zeile, "[main] INFO soot.jimple.infoflow.Infoflow - - ",
							" in method ");
					final String method = Helper.cut(zeile, " in method ");
					final String classname = Helper.cut(method, "<", ": ");

					final Reference from = new Reference();
					from.setType(KeywordsAndConstantsHelper.REFERENCE_TYPE_FROM);
					from.setApp(app);
					from.setClassname(classname);
					from.setMethod(method);
					from.setStatement(createStatement(statement));

					flow.getReference().add(from);

					if (to != null) {
						answer.getFlows().getFlow().add(flow);
						flow = new Flow();
						flow.getReference().add(to);
					}
				}
			}

			br.close();
		} catch (final IOException e) {
			e.printStackTrace();
			Log.error("Error while reading file: " + resultFile.getAbsolutePath());
			return null;
		}

		return answer;
	}

	private Statement createStatement(String statement) {
		int lineNumber;
		try {
			if (statement.contains(" on line ")) {
				final String[] parts = statement.split(" on line ");
				lineNumber = Integer.parseInt(parts[1]);
				statement = parts[0];
			} else {
				throw new StringIndexOutOfBoundsException("\" on line \" not found in: " + statement);
			}
		} catch (final Exception e) {
			lineNumber = -1;
		}
		return Helper.createStatement(statement, lineNumber);
	}
}