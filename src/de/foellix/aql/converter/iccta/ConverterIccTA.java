package de.foellix.aql.converter.iccta;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import de.foellix.aql.Log;
import de.foellix.aql.converter.IConverter;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.ToolTaskInfo;

public class ConverterIccTA implements IConverter {
	@Override
	public Answer parse(final File resultFile, final ToolTaskInfo taskInfo) {
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
					to.setType(KeywordsAndConstants.REFERENCE_TYPE_TO);
					to.setApp(taskInfo.getQuestion().getAllReferences().get(0).getApp());
					to.setClassname(classname);
					to.setMethod(method);
					to.setStatement(Helper.createStatement(statement));

					flow = new Flow();
					flow.getReference().add(to);
				} else if (zeile.contains("[main] INFO soot.jimple.infoflow.Infoflow - - ")
						&& zeile.contains(" in method ")) {
					final String statement = Helper.cut(zeile, "[main] INFO soot.jimple.infoflow.Infoflow - - ",
							" in method ");
					final String method = Helper.cut(zeile, " in method ");
					final String classname = Helper.cut(method, "<", ": ");

					final Reference from = new Reference();
					from.setType(KeywordsAndConstants.REFERENCE_TYPE_FROM);
					from.setApp(taskInfo.getQuestion().getAllReferences().get(0).getApp());
					from.setClassname(classname);
					from.setMethod(method);
					from.setStatement(Helper.createStatement(statement));

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
}
