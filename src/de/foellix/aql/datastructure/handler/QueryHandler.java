package de.foellix.aql.datastructure.handler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.query.Query;
import de.foellix.aql.ui.gui.GUI;

public class QueryHandler {
	public static Query parseQuery(String query) {
		return parseQuery(query, false);
	}

	public static Query parseQuery(String query, boolean ignoreExceptions) {
		try {
			final InputStream input = new ByteArrayInputStream(query.getBytes());
			final QuestionParser parser = new QuestionParser(input);
			parser.queries();
			final QuestionHandler questionHandler = parser.getQuestionHandler();
			final Query currentQuery = questionHandler.getQuery();
			return currentQuery;
		} catch (final ParseException e) {
			if (!ignoreExceptions) {
				Log.error("Query is not valid! Syntax error at line " + e.currentToken.beginLine + " near symbol \""
						+ e.currentToken.image + "\".");
				if (GUI.started) {
					GUI.alert(true, "Error", "Query is not valid!", "Syntax error at line " + e.currentToken.beginLine
							+ " near symbol \"" + e.currentToken.image + "\".");
				}
				if (Log.logIt(Log.DEBUG_DETAILED)) {
					e.printStackTrace();
				}
			}
			return null;
		}
	}
}
