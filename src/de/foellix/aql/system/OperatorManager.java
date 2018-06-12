package de.foellix.aql.system;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.IQuestionNode;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.Question;
import de.foellix.aql.datastructure.WaitingAnswer;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.OperatorTaskInfo;

public class OperatorManager {
	private final System parent;

	private List<OperatorTaskInfo> listToSchedule;
	private Collection<Answer> collection;

	OperatorManager(System parent) {
		this.parent = parent;

		this.collection = null;
	}

	void apply() {
		this.listToSchedule = new ArrayList<>();
		this.collection = this.parent.buildCompleteAnswer(this.parent.getCurrentQuery());

		// Execute operators
		this.parent.getScheduler().setWaiting(this.listToSchedule.size());
		this.parent.setMax(this.listToSchedule.size());
		this.parent.progress("Step 3 of 3: Applying operators");
		if (this.parent.getMax() != 0) {
			for (final OperatorTaskInfo task : this.listToSchedule) {
				this.parent.getScheduler().scheduleOperator(task);
			}
			this.parent.getScheduler().runSchedule();
		} else {
			this.parent.operatorExecuted(null, null);
		}
	}

	public Answer applyOperator(Question question) {
		Tool operator = ToolSelector.getInstance().selectOperator(question);
		if (operator == null && (question.getOperator().equals(KeywordsAndConstants.OPERATOR_FILTER)
				|| question.getOperator().equals(KeywordsAndConstants.OPERATOR_UNIFY)
				|| question.getOperator().equals(KeywordsAndConstants.OPERATOR_CONNECT))) {
			operator = new DefaultOperator();
		}

		int cardinality = Helper.getCardinality(operator, question.getOperator());
		if (cardinality == 0) {
			cardinality = question.getChildren().size();
		}
		if (question.getChildren().size() != cardinality) {
			if (!(operator instanceof DefaultOperator)) {
				Log.warning("Wrong number of parameters for " + question.getOperator()
						+ " operator. Trying to apply it multiple times with two parameters");
			}
			cardinality = 2;
		}

		Answer answer = null;
		if (cardinality == 2) {
			for (int i = 0; i < question.getChildren().size(); i++) {
				if (question.getChildren().get(i) instanceof Question && ((Question) question.getChildren().get(i))
						.getOperator().equals(KeywordsAndConstants.OPERATOR_COLLECTION)) {
					for (final IQuestionNode child : question.getChildren().get(i).getChildren()) {
						if (answer == null) {
							answer = this.parent.buildCompleteAnswerNotCollection(child);
						} else {
							answer = applyOperator(question, operator, answer,
									this.parent.buildCompleteAnswerNotCollection(child));
						}
					}
				} else {
					if (answer == null) {
						answer = this.parent.buildCompleteAnswerNotCollection(question.getChildren().get(i));
					} else {
						answer = applyOperator(question, operator, answer,
								this.parent.buildCompleteAnswerNotCollection(question.getChildren().get(i)));
					}
				}
			}
		} else {
			final Answer[] answers = new Answer[cardinality];
			for (int i = 0; i < cardinality; i++) {
				answers[i] = this.parent.buildCompleteAnswerNotCollection(question.getChildren().get(i));
			}
			answer = applyOperator(question, operator, answers);
		}
		return answer;
	}

	private Answer applyOperator(Question question, Tool operator, Answer... answers) {
		final WaitingAnswer answer = new WaitingAnswer(operator, answers);
		this.listToSchedule.add(new OperatorTaskInfo(question, operator, answer));
		return answer;
	}

	public Collection<Answer> getAnswerCollection() {
		return this.collection;
	}
}
