package de.foellix.aql.datastructure;

public class KeywordsAndConstants {
	public static final String OPERATOR_COLLECTION = "COLLECTION";
	public static String OPERATOR_UNIFY_ORIGINAL = "UNIFY";
	public static String OPERATOR_CONNECT_ORIGINAL = "CONNECT";
	public static String OPERATOR_MINUS_ORIGINAL = "MINUS";
	public static String OPERATOR_INTERSECT_ORIGINAL = "INTERSECT";
	public static String OPERATOR_FILTER_ORIGINAL = "FILTER";
	private static String OPERATOR_UNIFY = "UNIFY";
	private static String OPERATOR_CONNECT = "CONNECT";
	private static String OPERATOR_MINUS = "MINUS";
	private static String OPERATOR_INTERSECT = "INTERSECT";
	private static String OPERATOR_FILTER = "FILTER";

	public static final String SOI_UNKNOWN = "Unknown";
	public static final String SOI_FLOWS = "Flows";
	public static final String SOI_PERMISSIONS = "Permissions";
	public static final String SOI_INTENTS = "Intents";
	public static final String SOI_INTENTFILTERS = "IntentFilters";
	public static final String SOI_INTENTSOURCES = "IntentSources";
	public static final String SOI_INTENTSINKS = "IntentSinks";

	public static final String MODE_INTRA_FLOWS = "IntraAppFlows";
	public static final String MODE_INTER_FLOWS = "InterAppFlows";
	public static final String MODE_PERMISSIONS = SOI_PERMISSIONS;
	public static final String MODE_INTENTS = SOI_INTENTS;
	public static final String MODE_INTENTFILTER = SOI_INTENTFILTERS;
	public static final String MODE_INTENTSOURCES = SOI_INTENTSOURCES;
	public static final String MODE_INTENTSINKS = SOI_INTENTSINKS;

	public static final int QUESTION_TYPE_UNKNOWN = -1;
	public static final int QUESTION_TYPE_FLOWS = 0;
	public static final int QUESTION_TYPE_PERMISSIONS = 1;
	public static final int QUESTION_TYPE_INTENTS = 2;
	public static final int QUESTION_TYPE_INTENTFILTER = 3;
	public static final int QUESTION_TYPE_INTENTSOURCES = 4;
	public static final int QUESTION_TYPE_INTENTSINKS = 5;

	public static final String REFERENCE_TYPE_FROM = "from";
	public static final String REFERENCE_TYPE_TO = "to";

	public static final String HASH_TYPE_MD5 = "MD5";
	public static final String HASH_TYPE_SHA1 = "SHA-1";
	public static final String HASH_TYPE_SHA256 = "SHA-256";

	public static final int ANSWER_STATUS_FAILED = -1;
	public static final int ANSWER_STATUS_UNKNOWN = 0;
	public static final int ANSWER_STATUS_SUCCESSFUL = 1;

	public static final String CATEGORY_DEFAULT = "android.intent.category.DEFAULT";

	public static final int DEFAULT_CONNECT_ALL = 0;
	public static final int DEFAULT_CONNECT_INTRA_APP = 1;
	public static final int DEFAULT_CONNECT_INTER_APP = 2;

	public static String getUnifyOperator() {
		return OPERATOR_UNIFY;
	}

	public static String getConnectOperator() {
		return OPERATOR_CONNECT;
	}

	public static String getMinusOperator() {
		return OPERATOR_MINUS;
	}

	public static String getIntersectOperator() {
		return OPERATOR_INTERSECT;
	}

	public static String getFilterOperator() {
		return OPERATOR_FILTER;
	}

	public static void overwriteUnifyOperator(String operator) {
		OPERATOR_UNIFY = operator;
	}

	public static void overwriteConnectOperator(String operator) {
		OPERATOR_CONNECT = operator;
	}

	public static void overwriteMinusOperator(String operator) {
		OPERATOR_MINUS = operator;
	}

	public static void overwriteIntersectOperator(String operator) {
		OPERATOR_INTERSECT = operator;
	}

	public static void overwriteFilterOperator(String operator) {
		OPERATOR_FILTER = operator;
	}
}