package de.foellix.aql.datastructure.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.Stack;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.query.DefaultQuestion;
import de.foellix.aql.datastructure.query.FilterQuestion;
import de.foellix.aql.datastructure.query.LoadingQuestion;
import de.foellix.aql.datastructure.query.OperatorQuestion;
import de.foellix.aql.datastructure.query.Query;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.datastructure.query.QuestionReference;
import de.foellix.aql.datastructure.query.QuestionString;
import de.foellix.aql.datastructure.query.StringOrQuestionPair;

public class QuestionHandler {
	private static final int ARGUMENT_FEATURES = 1;
	private static final int ARGUMENT_USES = 2;
	private static final int ARGUMENT_WITHS_KEY = 3;
	private static final int ARGUMENT_WITHS_VALUE = 4;
	private static final int ARGUMENT_REFERENCE_STATEMENT = 5;
	private static final int ARGUMENT_REFERENCE_METHOD = 6;
	private static final int ARGUMENT_REFERENCE_CLASS = 7;
	private static final int ARGUMENT_REFERENCE_APP = 8;
	private static final int ARGUMENT_FILTER_KEY = 9;
	private static final int ARGUMENT_FILTER_VALUE = 10;
	private static final int ARGUMENT_PREPROCESSOR_KEYWORD = 15;
	private static final int OPERATOR_QUESTIONS = 16;
	private static final int FILTER_QUESTION = 17;

	private Query completeQuery;
	private Stack<QuestionReference> referenceStack;
	private boolean bracketsActive;
	private Stack<Integer> innerQueryMode;
	private Stack<StringOrQuestionPair> filterKeyValuePairs;
	private String variable;

	public void startQueries() {
		this.completeQuery = new Query();
		this.referenceStack = new Stack<>();
		this.bracketsActive = false;
		this.innerQueryMode = new Stack<>();
		this.filterKeyValuePairs = new Stack<>();
		this.variable = null;
	}

	public void endQueries() {
		// Cleanup
		if (this.completeQuery.getQuestionStack().isEmpty()) {
			this.completeQuery.destroy();
		} else {
			Log.error("Query corrupted: Question stack was not empty after reading the complete query.");
		}
		if (this.referenceStack.isEmpty()) {
			this.referenceStack = null;
		} else {
			Log.error("Query corrupted: Reference stack was not empty after reading the complete query.");
		}
	}

	public void startQuery() {
		// Kept for completeness
	}

	public void activateBrackets() {
		this.bracketsActive = true;
	}

	public void deactivateBrackets() {
		this.bracketsActive = false;
	}

	public void endQuery() {
		// Kept for completeness
	}

	public void startQuestion() {
		// Kept for completeness
	}

	public void endQuestion(String endingSymbol) {
		getCurrentQuestion().setEndingSymbol(endingSymbol.charAt(0));
		this.completeQuery.getQuestionStack().pop();
	}

	public void startDefaultQuestion() {
		addQuestion(new DefaultQuestion());
	}

	public void startOperatorQuestion(String operator) {
		addQuestion(new OperatorQuestion(operator));
	}

	public void startInnerOperatorQuestions() {
		this.innerQueryMode.push(OPERATOR_QUESTIONS);
	}

	public void endInnerOperatorQuestions() {
		this.innerQueryMode.pop();
	}

	public void startFilterQuestion() {
		addQuestion(new FilterQuestion());
	}

	public void startInnerFilterQuestion() {
		this.innerQueryMode.push(FILTER_QUESTION);
	}

	public void endInnerFilterQuestion() {
		this.innerQueryMode.pop();
	}

	public void startLoadingQuestion() {
		addQuestion(new LoadingQuestion());
	}

	public void endDefaultQuestion() {
		// Kept for completeness
	}

	public void endOperatorQuestion() {
		// Kept for completeness
	}

	public void endFilterQuestion() {
		// Kept for completeness
	}

	public void endLoadingQuestion() {
		// Kept for completeness
	}

	public void setSubjectOfInterest(String subjectOfInterest) {
		getCurrentDefaultQuestion().setSubjectOfInterest(subjectOfInterest);
	}

	public void startFeatures() {
		getCurrentDefaultQuestion().setFeatures(new ArrayList<>());
	}

	public void addFeature(String feature) {
		getCurrentDefaultQuestion().getFeatures().add(new QuestionString(removeQuotes(feature)));
	}

	public void startFeaturesQuery() {
		this.innerQueryMode.push(ARGUMENT_FEATURES);
	}

	public void endFeaturesQuery() {
		this.innerQueryMode.pop();
	}

	public void endFeatures() {
		// Kept for completeness
	}

	public void startUses() {
		getCurrentDefaultQuestion().setUses(new ArrayList<>());
	}

	public void addUse(String use) {
		getCurrentDefaultQuestion().getUses().add(new QuestionString(removeQuotes(use)));
	}

	public void startUsesQuery() {
		this.innerQueryMode.push(ARGUMENT_USES);
	}

	public void endUsesQuery() {
		this.innerQueryMode.pop();
	}

	public void endUses() {
		// Kept for completeness
	}

	public void startWiths() {
		getCurrentDefaultQuestion().setWiths(new Stack<>());
	}

	public void startWith() {
		getCurrentDefaultQuestion().getWiths().push(new StringOrQuestionPair());
	}

	public void setWithKey(String key) {
		getCurrentDefaultQuestion().getWiths().peek().setKey(new QuestionString(removeQuotes(key)));
	}

	public void startWithKeyQuery() {
		this.innerQueryMode.push(ARGUMENT_WITHS_KEY);
	}

	public void endWithKeyQuery() {
		this.innerQueryMode.pop();
	}

	public void setWithValue(String value) {
		getCurrentDefaultQuestion().getWiths().peek().setValue(new QuestionString(removeQuotes(value)));
	}

	public void startWithValueQuery() {
		this.innerQueryMode.push(ARGUMENT_WITHS_VALUE);
	}

	public void endWithValueQuery() {
		this.innerQueryMode.pop();
	}

	public void startWithsQuery() {
		// Kept for completeness
	}

	public void endWithsQuery() {
		// Kept for completeness
	}

	public void endWith() {
		// Kept for completeness
	}

	public void endWiths() {
		// Kept for completeness
	}

	public void startFilterKeyValuePair() {
		this.filterKeyValuePairs.push(new StringOrQuestionPair());
	}

	public void startFilterKeyQuery() {
		this.innerQueryMode.push(ARGUMENT_FILTER_KEY);
	}

	public void endFilterKeyQuery() {
		this.innerQueryMode.pop();
	}

	public void setFilterKey(String key) {
		this.filterKeyValuePairs.peek().setKey(new QuestionString(removeQuotes(key)));
	}

	public void startFilterValueQuery() {
		this.innerQueryMode.push(ARGUMENT_FILTER_VALUE);
	}

	public void endFilterValueQuery() {
		this.innerQueryMode.pop();
	}

	public void setFilterValue(String value) {
		this.filterKeyValuePairs.peek().setValue(new QuestionString(removeQuotes(value)));
	}

	public void endFilterKeyValuePair() {
		getCurrentFilterQuestion().setFilterPair(this.filterKeyValuePairs.pop());
	}

	public void startFilterReference() {
		this.referenceStack.push(new QuestionReference());
		getCurrentFilterQuestion().setFilterReference(this.referenceStack.peek());
	}

	public void endFilterReference() {
		// Kept for completeness
	}

	public void setFilterSubjectOfInterest(String subjectOfInterest) {
		getCurrentFilterQuestion().setFilterSubjectOfInterest(subjectOfInterest);
	}

	public void startFrom() {
		this.referenceStack.push(new QuestionReference());
		getCurrentDefaultQuestion().setFrom(this.referenceStack.peek());
	}

	public void endFrom() {
		// Kept for completeness
	}

	public void startTo() {
		this.referenceStack.push(new QuestionReference());
		getCurrentDefaultQuestion().setTo(this.referenceStack.peek());
	}

	public void endTo() {
		// Kept for completeness
	}

	public void startIn() {
		this.referenceStack.push(new QuestionReference());
		getCurrentDefaultQuestion().setIn(this.referenceStack.peek());
	}

	public void endIn() {
		// Kept for completeness
	}

	public void startReference() {
		// Kept for completeness
	}

	public void setStatement(String statement) {
		this.referenceStack.peek().setStatement(new QuestionString(removeQuotes(statement)));
	}

	public void setLineNumber(String lineNumber) {
		this.referenceStack.peek().setLineNumber(Integer.parseInt(lineNumber));
	}

	public void startStatementQuery() {
		this.innerQueryMode.push(ARGUMENT_REFERENCE_STATEMENT);
	}

	public void endStatementQuery() {
		this.innerQueryMode.pop();
	}

	public void setMethod(String method) {
		this.referenceStack.peek().setMethod(new QuestionString(removeQuotes(method)));
	}

	public void startMethodQuery() {
		this.innerQueryMode.push(ARGUMENT_REFERENCE_METHOD);
	}

	public void endMethodQuery() {
		this.innerQueryMode.pop();
	}

	public void setClass(String classname) {
		this.referenceStack.peek().setClassname(new QuestionString(removeQuotes(classname)));
	}

	public void startClassQuery() {
		this.innerQueryMode.push(ARGUMENT_REFERENCE_CLASS);
	}

	public void endClassQuery() {
		this.innerQueryMode.pop();
	}

	public void setApp(String app) {
		this.referenceStack.peek().setApp(new QuestionString(sanitizeFile(removeQuotes(app))));
	}

	public void startAppQuery() {
		this.innerQueryMode.push(ARGUMENT_REFERENCE_APP);
	}

	public void endAppQuery() {
		this.innerQueryMode.pop();
	}

	public void endReference() {
		this.referenceStack.pop();
	}

	public void setKeyword(String keyword) {
		this.referenceStack.peek().getPreprocessorKeywords().add(new QuestionString(removeQuotes(keyword)));
	}

	public void startKeywordQuery() {
		this.innerQueryMode.push(ARGUMENT_PREPROCESSOR_KEYWORD);
	}

	public void endKeywordQuery() {
		this.innerQueryMode.pop();
	}

	public void addAnswer(String file) {
		getCurrentLoadingQuestion().setFile(removeQuotes(file));
	}

	// Helper methods
	private void addQuestion(Question question) {
		question.setParent(getCurrentQuestion());
		question.setWithBrackets(this.bracketsActive);
		if (this.completeQuery.getQuestionStack() != null && !this.completeQuery.getQuestionStack().isEmpty()) {
			switch (this.innerQueryMode.peek()) {
				case OPERATOR_QUESTIONS:
					if (getCurrentQuestion() instanceof FilterQuestion) {
						getCurrentFilterQuestion().setQuestion(question);
					} else if (getCurrentQuestion() instanceof OperatorQuestion) {
						getCurrentOperatorQuestion().getQuestions().add(question);
					} else {
						addQuestionToListOrVariable(question);
					}
					break;
				case FILTER_QUESTION:
					if (getCurrentQuestion() instanceof FilterQuestion) {
						getCurrentFilterQuestion().setQuestion(question);
					} else if (getCurrentQuestion() instanceof OperatorQuestion) {
						getCurrentOperatorQuestion().getQuestions().add(question);
					} else {
						addQuestionToListOrVariable(question);
					}
					break;
				case ARGUMENT_FEATURES:
					getCurrentDefaultQuestion().getFeatures().add(question);
					break;
				case ARGUMENT_USES:
					getCurrentDefaultQuestion().getUses().add(question);
					break;
				case ARGUMENT_WITHS_KEY:
					getCurrentDefaultQuestion().getWiths().peek().setKey(question);
					break;
				case ARGUMENT_WITHS_VALUE:
					getCurrentDefaultQuestion().getWiths().peek().setValue(question);
					break;
				case ARGUMENT_REFERENCE_STATEMENT:
					this.referenceStack.peek().setStatement(question);
					break;
				case ARGUMENT_REFERENCE_METHOD:
					this.referenceStack.peek().setMethod(question);
					break;
				case ARGUMENT_REFERENCE_CLASS:
					this.referenceStack.peek().setClassname(question);
					break;
				case ARGUMENT_REFERENCE_APP:
					this.referenceStack.peek().setApp(question);
					break;
				case ARGUMENT_FILTER_KEY:
					this.filterKeyValuePairs.peek().setKey(question);
					break;
				case ARGUMENT_FILTER_VALUE:
					this.filterKeyValuePairs.peek().setValue(question);
					break;
				case ARGUMENT_PREPROCESSOR_KEYWORD:
					this.referenceStack.peek().getPreprocessorKeywords().add(question);
					break;
				default:
					Log.error("Query corrupted: Inner query placed at invalid position.");
					break;
			}
		} else {
			addQuestionToListOrVariable(question);
		}
		this.completeQuery.getQuestionStack().push(question);
	}

	private void addQuestionToListOrVariable(Question question) {
		if (this.variable == null) {
			this.completeQuery.getQuestions().add(question);
		} else {
			this.completeQuery.getVariableMap().put(this.variable, question);
			this.variable = null;
		}
	}

	// Easy access methods
	private Question getCurrentQuestion() {
		if (this.completeQuery.getQuestionStack().isEmpty()) {
			return null;
		} else {
			return this.completeQuery.getQuestionStack().peek();
		}
	}

	private DefaultQuestion getCurrentDefaultQuestion() {
		if (getCurrentQuestion() instanceof DefaultQuestion) {
			return (DefaultQuestion) getCurrentQuestion();
		} else {
			Log.error("Query corrupted: A different question type (default) was expected.");
			return null;
		}
	}

	private OperatorQuestion getCurrentOperatorQuestion() {
		if (getCurrentQuestion() instanceof OperatorQuestion) {
			return (OperatorQuestion) getCurrentQuestion();
		} else {
			Log.error("Query corrupted: A different question type (operator) was expected.");
			return null;
		}
	}

	private FilterQuestion getCurrentFilterQuestion() {
		if (getCurrentQuestion() instanceof FilterQuestion) {
			return (FilterQuestion) getCurrentQuestion();
		} else {
			Log.error("Query corrupted: A different question type (filter) was expected.");
			return null;
		}
	}

	private LoadingQuestion getCurrentLoadingQuestion() {
		if (getCurrentQuestion() instanceof LoadingQuestion) {
			return (LoadingQuestion) getCurrentQuestion();
		} else {
			Log.error("Query corrupted: A different question type (loading) was expected.");
			return null;
		}
	}

	private String removeQuotes(String value) {
		return value.substring(1, value.length() - 1);
	}

	private String sanitizeFile(String file) {
		if (file.startsWith("%") && file.endsWith("%")) {
			return file;
		} else {
			return new File(file.replace("\\", "/")).getAbsolutePath();
		}
	}

	public void setVariable(String variable) {
		// Acknowledge variable
		this.variable = variable;
	}

	public void getVariable(String variable) {
		// Replace variable usage
		if (this.completeQuery.getVariableMap().containsKey(variable)) {
			final Question variableQuestion = this.completeQuery.getVariableMap().get(variable);
			if (this.completeQuery.getQuestionStack().isEmpty()) {
				this.completeQuery.getQuestions().add(variableQuestion);
			} else {
				if (variableQuestion.isWithBrackets()) {
					activateBrackets();
				}
				addQuestion(variableQuestion);
				endQuestion(String.valueOf(variableQuestion.getEndingSymbol()).toString());
				deactivateBrackets();
			}
		} else {
			Log.error("Query corrupted: Variable \"" + variable + "\" was not declared before usage.");
		}
	}

	// Getter for final object
	public Query getQuery() {
		return this.completeQuery;
	}
}