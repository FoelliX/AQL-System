package de.foellix.aql.datastructure.query;

public interface IStringOrQuestion {
	/**
	 * Returns if this question is complete.
	 *
	 * @param takeUnansweredIntoAccount
	 *            true: Incomplete if at least one unanswered question is contained. false: Ignores unanswered parts.
	 * @return true if question is complete
	 */
	public boolean isComplete(boolean takeUnansweredIntoAccount);

	@Override
	public String toString();

	public String toStringInAnswer();

	public default String toStringInAnswer(boolean withQuotes) {
		return toStringInAnswer();
	}
}
