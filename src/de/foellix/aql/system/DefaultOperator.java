package de.foellix.aql.system;

import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.helper.EqualsOptions;
import de.foellix.aql.system.task.OperatorTaskInfo;

public class DefaultOperator extends Tool {
	private static final DefaultOperatorInstance instance = DefaultOperatorInstance.getInstance();

	public static Answer applyOperator(OperatorTaskInfo taskinfo) {
		return instance.applyOperator(taskinfo);
	}

	public static Answer unify(final Answer answer, final Answer unifyWithAnswer) {
		return instance.unify(answer, unifyWithAnswer);
	}

	public static Answer connect(final Answer answer, final Answer connectWithAnswer) {
		return instance.connect(answer, connectWithAnswer);
	}

	public static Answer connect(final Answer answer, final Answer connectWithAnswer, int mode) {
		return instance.connect(answer, connectWithAnswer, mode);
	}

	public static Answer minus(final Answer answer, final Answer minusAnswer) {
		return instance.minus(answer, minusAnswer);
	}

	public static Answer minus(final Answer answer, final Answer minusAnswer, EqualsOptions options) {
		return instance.minus(answer, minusAnswer, options);
	}

	public static Answer intersect(final Answer answer, final Answer intersectWithAnswer) {
		return instance.intersect(answer, intersectWithAnswer);
	}

	public static Answer intersect(final Answer answer, final Answer intersectWithAnswer, EqualsOptions options) {
		return instance.intersect(answer, intersectWithAnswer, options);
	}

	public static Answer filter1(Answer answer) {
		return instance.filter1(answer);
	}

	public static Answer filter2(final Answer answer, String name, String value, final int soi) {
		return instance.filter2(answer, name, value, soi);
	}

	public static Answer filter3(final Answer answer, final int soi) {
		return instance.filter3(answer, soi);
	}
}