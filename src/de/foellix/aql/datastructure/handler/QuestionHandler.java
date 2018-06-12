package de.foellix.aql.datastructure.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.PreviousQuestion;
import de.foellix.aql.datastructure.Question;
import de.foellix.aql.datastructure.QuestionFilter;
import de.foellix.aql.datastructure.QuestionPart;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.Statement;
import de.foellix.aql.helper.Helper;

public class QuestionHandler {
	private Stack<Question> stack;
	private List<String> features;

	private QuestionPart currentQuestion;
	private Reference currentReference;
	private List<String> currentPreprocessors;

	// Name-Value-Pair
	private String currentName = null, currentValue = null;
	private int currentSOI = KeywordsAndConstants.QUESTION_TYPE_UNKNOWN;

	public QuestionHandler() {

	}

	public void startQuery() {
		this.stack = new Stack<>();
		this.stack.push(new Question(KeywordsAndConstants.OPERATOR_COLLECTION));
	}

	public void startOperator(final String operator) {
		// Assign type
		final Question temp;
		if (operator.equals(KeywordsAndConstants.OPERATOR_FILTER)) {
			temp = new QuestionFilter(operator);
		} else {
			temp = new Question(operator);
		}
		this.stack.peek().addChild(temp);
		this.stack.push(temp);
	}

	public void setFilterNameValuePair(final String name, final String value) {
		this.currentName = name;
		this.currentValue = value;
	}

	public void setFilterSOI(final String soi) {
		this.currentSOI = soiToType(soi);
	}

	public void endOperator() {
		if (this.stack.peek() instanceof QuestionFilter) {
			((QuestionFilter) this.stack.peek()).setName(this.currentName);
			((QuestionFilter) this.stack.peek()).setValue(this.currentValue);
			((QuestionFilter) this.stack.peek()).setSoi(this.currentSOI);
			this.currentName = null;
			this.currentValue = null;
			this.currentSOI = KeywordsAndConstants.QUESTION_TYPE_UNKNOWN;
		}

		this.stack.pop();
	}

	public void startQuestion() {
		this.currentQuestion = new QuestionPart();
		this.currentPreprocessors = new ArrayList<>();
	}

	public void endQuestion() {
		this.stack.peek().addChild(this.currentQuestion);
	}

	public void setMode(final String soi) {
		// Convert subject of interest to question type
		final int type = soiToType(soi);

		// Assign type
		this.currentQuestion.setMode(type);
	}

	public void addReference() {
		this.currentQuestion.addPreprocessor(this.currentReference, this.currentPreprocessors);
		this.currentPreprocessors = new ArrayList<>();
		this.currentQuestion.addReference(this.currentReference);
		this.currentReference = null;
	}

	public void setReferenceType(final String type) {
		this.currentReference.setType(type);
	}

	public void setStatement(String value) {
		value = init(value);

		final Statement statement = Helper.fromStatementString(value);

		this.currentReference.setStatement(statement);
	}

	public void setMethod(String value) {
		value = init(value);

		this.currentReference.setMethod(value);
	}

	public void setClass(String value) {
		value = init(value);

		this.currentReference.setClassname(value);
	}

	public void setApp(String value) {
		if (value.equals("''") || value.equals("'/'") || value.equals("'\'") || value.equals("'*'")) {
			value = "'.'";
		} else if (value.endsWith("*'")) {
			value = value.substring(0, value.length() - 2) + "'";
		}

		value = init(value);

		this.currentReference.setApp(Helper.createApp(value));
	}

	public void addPreprocessor(final String preprocessor) {
		this.currentPreprocessors.add(preprocessor);
	}

	private String init(final String value) {
		if (this.currentReference == null) {
			this.currentReference = new Reference();
		}
		return value.substring(1, value.length() - 1);
	}

	public Question getCollection() {
		return this.stack.firstElement();
	}

	public void addAnswer(final String file) {
		final PreviousQuestion pq = new PreviousQuestion(file);
		this.stack.peek().addChild(pq);
	}

	private int soiToType(final String soi) {
		switch (soi) {
		case KeywordsAndConstants.SOI_FLOWS:
			return KeywordsAndConstants.QUESTION_TYPE_FLOWS;
		case KeywordsAndConstants.SOI_INTENTFILTERS:
			return KeywordsAndConstants.QUESTION_TYPE_INTENTFILTER;
		case KeywordsAndConstants.SOI_INTENTS:
			return KeywordsAndConstants.QUESTION_TYPE_INTENTS;
		case KeywordsAndConstants.SOI_INTENTSINKS:
			return KeywordsAndConstants.QUESTION_TYPE_INTENTSINKS;
		case KeywordsAndConstants.SOI_INTENTSOURCES:
			return KeywordsAndConstants.QUESTION_TYPE_INTENTSOURCES;
		case KeywordsAndConstants.SOI_PERMISSIONS:
			return KeywordsAndConstants.QUESTION_TYPE_PERMISSIONS;
		default:
			return KeywordsAndConstants.QUESTION_TYPE_UNKNOWN;
		}
	}

	public void addFeature(String feature) {
		this.currentQuestion.getFeatures().add(feature.substring(1, feature.length() - 1));
	}
}
