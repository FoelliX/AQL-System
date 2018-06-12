package de.foellix.aql.datastructure;

import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.helper.Helper;

public class PreviousQuestion implements IQuestionNode {
	private final String file;

	public PreviousQuestion(final String file) {
		this.file = Helper.cut(file, "'", "'");
	}

	@Override
	public String toString() {
		return toString(0);
	}

	@Override
	public String toString(final int level) {
		String indent = "";
		for (int i = 0; i < level; i++) {
			indent += "\t";
		}

		return indent + "ANSWER(" + this.file + ")\n";
	}

	@Override
	public String toRAW() {
		return "PreviousQuestion (" + this.file + ")";
	}

	@Override
	public List<IQuestionNode> getChildren() {
		return null;
	}

	@Override
	public List<QuestionPart> getAllQuestionParts() {
		return null;
	}

	@Override
	public List<PreviousQuestion> getAllPreviousQuestions() {
		final List<PreviousQuestion> temp = new ArrayList<>();
		temp.add(this);
		return temp;
	}

	public String getFile() {
		return this.file;
	}
}
