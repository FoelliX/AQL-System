package de.foellix.aql.system.defaulttools.operators;

import de.foellix.aql.datastructure.Statement;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.helper.KeywordsAndConstantsHelper;

public class DefaultToFDOperator extends DefaultToToolOperator {
	@Override
	public String toSource(Statement statement) {
		return "<" + Helper.getStatementgenericSafe(statement) + "> -> " + KeywordsAndConstantsHelper.SOURCE;
	}

	@Override
	public String toSink(Statement statement) {
		return "<" + Helper.getStatementgenericSafe(statement) + "> -> " + KeywordsAndConstantsHelper.SINK;
	}
}