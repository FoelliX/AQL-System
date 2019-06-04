package de.foellix.aql.datastructure.handler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.IQuestionNode;

public class QueryHandler {
	public static IQuestionNode parseQuery(String query) {
		try {
			final InputStream input = new ByteArrayInputStream(query.getBytes());
			final QuestionParser parser = new QuestionParser(input);
			parser.query();
			final QuestionHandler questionHandler = parser.getQuestionHandler();
			IQuestionNode currentQuery = questionHandler.getCollection();

			if (currentQuery.getChildren() != null && currentQuery.getChildren().size() == 1) {
				currentQuery = currentQuery.getChildren().get(0);
			}

			return currentQuery;
		} catch (final ParseException e) {
			Log.error("The query is not valid.");
			e.printStackTrace();
			return null;
		}
	}

	public static IQuestionNode replaceWithAbsolutePaths(IQuestionNode query) {
		try {
			for (final App app : query.getAllApps(true)) {
				final File file = new File(app.getFile());
				app.setFile(file.getAbsolutePath().replaceAll("\\\\", "/"));
			}
		} catch (final Exception e) {
			Log.error("Could not convert to absolute paths. Maybe one or more files do not exists.");
		}
		return query;
	}
}
