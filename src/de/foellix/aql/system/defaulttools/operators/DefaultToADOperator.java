package de.foellix.aql.system.defaulttools.operators;

import de.foellix.aql.datastructure.Statement;
import de.foellix.aql.helper.JawaHelper;
import de.foellix.aql.helper.KeywordsAndConstantsHelper;

public class DefaultToADOperator extends DefaultToToolOperator {
	@Override
	public String toSource(Statement statement) {
		return JawaHelper.toJawa(statement) + " SENSITIVE_INFO -> " + KeywordsAndConstantsHelper.SOURCE;
	}

	@Override
	public String toSink(Statement statement) {
		final StringBuilder attachment = new StringBuilder();
		if (statement.getParameters() != null && !statement.getParameters().getParameter().isEmpty()) {
			attachment.append(" ");
			for (int i = 1; i <= statement.getParameters().getParameter().size(); i++) {
				attachment.append((attachment.length() == 1 ? "" : "|") + i);
			}
		}
		return JawaHelper.toJawa(statement) + " -> " + KeywordsAndConstantsHelper.SINK + attachment.toString();
	}
}