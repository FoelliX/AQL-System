package de.foellix.aql.datastructure;

import java.util.List;

public interface IQuestionNode {
	public String toString(final int level);

	public String toRAW();

	public List<IQuestionNode> getChildren();

	public List<QuestionPart> getAllQuestionParts();

	public List<PreviousQuestion> getAllPreviousQuestions();
}
