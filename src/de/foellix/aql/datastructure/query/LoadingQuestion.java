package de.foellix.aql.datastructure.query;

import java.io.File;
import java.util.Collection;

public class LoadingQuestion extends Question {
	private static final long serialVersionUID = -360428414607615579L;

	private File file;

	public LoadingQuestion() {
	}

	public File getFile() {
		return this.file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setFile(String file) {
		setFile(new File(file));
	}

	@Override
	public String toString(int level) {
		return getIndent(level) + (this.withBrackets ? "{ " : "") + "'" + this.file + "' " + this.endingSymbol
				+ (this.withBrackets ? " }" : "");
	}

	@Override
	public Collection<Question> getChildren(boolean recursively) {
		return CHILDREN_EMPTY;
	}

	@Override
	public boolean replaceChild(Question childToReplace, IStringOrQuestion replacement) {
		return false;
	}

	@Override
	public Collection<QuestionReference> getAllReferences(boolean recursively) {
		return REFERENCES_EMPTY;
	}
}