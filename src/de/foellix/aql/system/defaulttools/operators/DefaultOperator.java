package de.foellix.aql.system.defaulttools.operators;

import java.io.File;

import de.foellix.aql.Properties;
import de.foellix.aql.config.Tool;
import de.foellix.aql.helper.EqualsOptions;
import de.foellix.aql.system.defaulttools.DefaultTool;
import de.foellix.aql.system.task.OperatorTask;
import de.foellix.aql.system.task.OperatorTaskInfo;

public abstract class DefaultOperator extends DefaultTool {
	public static final String OPERATOR_UNIFY = "UNIFY";
	public static final String OPERATOR_CONNECT = "CONNECT";
	public static final String OPERATOR_CONNECT_APPROX = "CONNECT~";
	public static final String OPERATOR_MINUS = "MINUS";
	public static final String OPERATOR_INTERSECT = "INTERSECT";
	public static final String OPERATOR_FILTER = "FILTER";
	public static final String OPERATOR_SIGN = "SIGN";
	public static final String OPERATOR_TOFD = "TOFD";
	public static final String OPERATOR_TOAD = "TOAD";

	protected static EqualsOptions equalsOptions;

	public DefaultOperator() {
		super();
		this.execute.setRun(
				this.getClass().getSimpleName() + " (" + Properties.info().VERSION + ") " + OperatorTaskInfo.ANSWERS
						+ ", " + OperatorTaskInfo.ANSWERSHASH + ", " + OperatorTaskInfo.ANSWERSHASH_MD5 + ", "
						+ OperatorTaskInfo.ANSWERSHASH_SHA1 + ", " + OperatorTaskInfo.ANSWERSHASH_SHA256);
		if (equalsOptions == null) {
			equalsOptions = EqualsOptions.DEFAULT;
		}
	}

	public static EqualsOptions getEqualsOptions() {
		return equalsOptions;
	}

	public static void setEqualsOptions(EqualsOptions options) {
		equalsOptions = options;
	}

	public static void resetEqualsOptions() {
		equalsOptions = EqualsOptions.DEFAULT;
	}

	public abstract File applyOperator(OperatorTask task);

	public static Tool getOperator(String operator) {
		if (operator.equals(DefaultOperator.OPERATOR_UNIFY)) {
			return new DefaultUnifyOperator();
		} else if (operator.equals(DefaultOperator.OPERATOR_INTERSECT)) {
			return new DefaultIntersectOperator();
		} else if (operator.equals(DefaultOperator.OPERATOR_MINUS)) {
			return new DefaultMinusOperator();
		} else if (operator.equals(DefaultOperator.OPERATOR_CONNECT)) {
			return new DefaultConnectOperator();
		} else if (operator.equals(DefaultOperator.OPERATOR_CONNECT_APPROX)) {
			return new DefaultConnectOperator().setApproximation(true);
		} else if (operator.equals(DefaultOperator.OPERATOR_FILTER)) {
			return new DefaultFilterOperator();
		} else if (operator.equals(DefaultOperator.OPERATOR_SIGN)) {
			return new DefaultSignOperator();
		} else if (operator.equals(DefaultOperator.OPERATOR_TOFD)) {
			return new DefaultToFDOperator();
		} else if (operator.equals(DefaultOperator.OPERATOR_TOAD)) {
			return new DefaultToADOperator();
		} else {
			return null;
		}
	}
}