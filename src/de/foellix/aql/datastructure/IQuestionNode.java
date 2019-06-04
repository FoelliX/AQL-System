package de.foellix.aql.datastructure;

import java.util.List;

public interface IQuestionNode {
	public String toString(final int level);

	public String toRAW(boolean external);

	public List<IQuestionNode> getChildren();

	public List<QuestionPart> getAllQuestionParts();

	public List<PreviousQuestion> getAllPreviousQuestions();

	public List<Reference> getAllReferences();

	public List<App> getAllApps(boolean equalsOnObjectLevel);
}
