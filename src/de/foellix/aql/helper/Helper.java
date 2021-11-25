package de.foellix.aql.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.foellix.aql.Log;
import de.foellix.aql.config.Priority;
import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.Attribute;
import de.foellix.aql.datastructure.Attributes;
import de.foellix.aql.datastructure.Data;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.Hash;
import de.foellix.aql.datastructure.Hashes;
import de.foellix.aql.datastructure.Intent;
import de.foellix.aql.datastructure.Intentfilter;
import de.foellix.aql.datastructure.Intentfilters;
import de.foellix.aql.datastructure.Intents;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsinks;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.Intentsources;
import de.foellix.aql.datastructure.Parameter;
import de.foellix.aql.datastructure.Parameters;
import de.foellix.aql.datastructure.Permission;
import de.foellix.aql.datastructure.Permissions;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.Sink;
import de.foellix.aql.datastructure.Sinks;
import de.foellix.aql.datastructure.Source;
import de.foellix.aql.datastructure.Sources;
import de.foellix.aql.datastructure.Statement;
import de.foellix.aql.datastructure.Target;
import de.foellix.aql.datastructure.query.IStringOrQuestion;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.faketool.FakeToolHelper;
import de.foellix.aql.system.defaulttools.analysistools.DefaultAnalysisTool;
import de.foellix.aql.system.defaulttools.operators.DefaultOperator;
import de.foellix.aql.system.storage.Storage;
import de.foellix.aql.system.task.ConverterTask;
import de.foellix.aql.system.task.FilterOperatorTaskInfo;
import de.foellix.aql.system.task.PreprocessorTask;
import de.foellix.aql.system.task.PreprocessorTaskInfo;
import de.foellix.aql.system.task.Task;
import de.foellix.aql.system.task.TaskAnswer;
import de.foellix.aql.system.task.TaskInfo;
import de.foellix.aql.system.task.ToolTaskInfo;

public class Helper {
	public static final int OCCURENCE_LAST = -1;

	private static final String SPACER = "###SPACE###";

	private static final RAWIdentifier RAW = new RAWIdentifier();

	public static String cut(final String input, final String from, final String to) {
		return cut(input, from, to, 1);
	}

	public static String cutFromFirstToLast(final String input, final String fromFirst, final String toLast) {
		try {
			return input.substring(input.indexOf(fromFirst) + fromFirst.length(), input.lastIndexOf(toLast));
		} catch (final StringIndexOutOfBoundsException e) {
			Log.msg("Input not valid! Input: \"" + input + "\", From: \"" + fromFirst + "\", To: \"" + toLast + "\""
					+ Log.getExceptionAppendix(e), Log.DEBUG_DETAILED);
			return input;
		}
	}

	public static String cut(final String input, final String from) {
		return cut(input, from, 1);
	}

	public static String cut(final String input, final String from, final String to, int fromOccurence) {
		try {
			int pos1 = 0;
			int pos2 = 0;

			if (fromOccurence == OCCURENCE_LAST) {
				fromOccurence = (input.length() - input.replace(from, "").length()) / from.length();
			}

			for (int i = 0; i < fromOccurence; i++) {
				if (from != null) {
					pos1 = input.indexOf(from, pos1) + from.length();
					if (to != null) {
						pos2 = input.indexOf(to, pos1);
					}
				} else {
					if (to != null) {
						pos2 = input.indexOf(to, pos2);
					}
				}
			}

			if (to != null) {
				return input.substring(pos1, pos2);
			} else {
				return input.substring(pos1);
			}
		} catch (final StringIndexOutOfBoundsException e) {
			Log.msg("Input not valid! Input: \"" + input + "\", From: \"" + from + "\", To: \"" + to + "\", Occurence: "
					+ (fromOccurence == OCCURENCE_LAST ? "last" : fromOccurence) + Log.getExceptionAppendix(e),
					Log.DEBUG_DETAILED);
			return input;
		}
	}

	public static String cut(final String input, final String from, int fromOccurence) {
		return cut(input, from, null, fromOccurence);
	}

	public static String cutFromStart(final String input, final String to) {
		return cutFromStart(input, to, 1);
	}

	public static String cutFromStart(final String input, final String to, int toOccurence) {
		try {
			int pos1 = 0;

			if (toOccurence == OCCURENCE_LAST) {
				toOccurence = (input.length() - input.replace(to, "").length()) / to.length();
			}

			for (int i = 0; i < toOccurence; i++) {
				if (to != null) {
					pos1 = input.indexOf(to, pos1);
				}
			}

			return input.substring(0, pos1);
		} catch (final StringIndexOutOfBoundsException e) {
			Log.msg("Input not valid! Input: \"" + input + "\", To: \"" + to + "\", Occurence: "
					+ (toOccurence == OCCURENCE_LAST ? "last" : toOccurence) + Log.getExceptionAppendix(e),
					Log.DEBUG_DETAILED);
			return input;
		}
	}

	public static int countStringOccurences(String haystack, String needle) {
		return (haystack.length() - haystack.replace(needle, "").length()) / needle.length();
	}

	/**
	 * Replace first no regEx!
	 *
	 * @param input
	 * @param needle
	 * @param replacement
	 * @return input with needle replaced by replacement
	 */
	public static String replaceFirst(String input, String needle, String replacement) {
		if (input.contains(needle)) {
			final int start = input.indexOf(needle);
			final int end = start + needle.length();
			input = input.substring(0, start) + replacement + input.substring(end);
		}
		return input;
	}

	/**
	 * Replace all no regEx!
	 *
	 * @param input
	 * @param needle
	 * @param replacement
	 * @return input with needle replaced by replacement
	 */
	public static String replaceAll(String input, String needle, String replacement) {
		while (input.contains(needle)) {
			input = replaceFirst(input, needle, replacement);
		}
		return input;
	}

	public static int soiToType(final String soi) {
		switch (soi) {
			case KeywordsAndConstantsHelper.SOI_FLOWS:
				return Question.QUESTION_TYPE_FLOWS;
			case KeywordsAndConstantsHelper.SOI_INTENTFILTERS:
				return Question.QUESTION_TYPE_INTENTFILTER;
			case KeywordsAndConstantsHelper.SOI_INTENTS:
				return Question.QUESTION_TYPE_INTENTS;
			case KeywordsAndConstantsHelper.SOI_INTENTSINKS:
				return Question.QUESTION_TYPE_INTENTSINKS;
			case KeywordsAndConstantsHelper.SOI_INTENTSOURCES:
				return Question.QUESTION_TYPE_INTENTSOURCES;
			case KeywordsAndConstantsHelper.SOI_PERMISSIONS:
				return Question.QUESTION_TYPE_PERMISSIONS;
			case KeywordsAndConstantsHelper.SOI_SLICE:
				return Question.QUESTION_TYPE_SLICE;
			case KeywordsAndConstantsHelper.SOI_SOURCES:
				return Question.QUESTION_TYPE_SOURCES;
			case KeywordsAndConstantsHelper.SOI_SINKS:
				return Question.QUESTION_TYPE_SINKS;
			case KeywordsAndConstantsHelper.SOI_ARGUMENTS:
				return Question.QUESTION_TYPE_ARGUMENTS;
			default:
				return Question.QUESTION_TYPE_UNKNOWN;
		}
	}

	public static String typeToSoi(final int type) {
		switch (type) {
			case Question.QUESTION_TYPE_FLOWS:
				return KeywordsAndConstantsHelper.SOI_FLOWS;
			case Question.QUESTION_TYPE_INTENTFILTER:
				return KeywordsAndConstantsHelper.SOI_INTENTFILTERS;
			case Question.QUESTION_TYPE_INTENTS:
				return KeywordsAndConstantsHelper.SOI_INTENTS;
			case Question.QUESTION_TYPE_INTENTSINKS:
				return KeywordsAndConstantsHelper.SOI_INTENTSINKS;
			case Question.QUESTION_TYPE_INTENTSOURCES:
				return KeywordsAndConstantsHelper.SOI_INTENTSOURCES;
			case Question.QUESTION_TYPE_PERMISSIONS:
				return KeywordsAndConstantsHelper.SOI_PERMISSIONS;
			case Question.QUESTION_TYPE_SLICE:
				return KeywordsAndConstantsHelper.SOI_SLICE;
			case Question.QUESTION_TYPE_SOURCES:
				return KeywordsAndConstantsHelper.SOI_SOURCES;
			case Question.QUESTION_TYPE_SINKS:
				return KeywordsAndConstantsHelper.SOI_SINKS;
			case Question.QUESTION_TYPE_ARGUMENTS:
				return KeywordsAndConstantsHelper.SOI_ARGUMENTS;
			default:
				return KeywordsAndConstantsHelper.SOI_UNKNOWN;
		}
	}

	public static Answer copy(Answer answer) {
		final Answer returnAnswer = new Answer();
		if (answer.getFlows() != null) {
			returnAnswer.setFlows(new Flows());
			if (!answer.getFlows().getFlow().isEmpty()) {
				returnAnswer.getFlows().getFlow().addAll(answer.getFlows().getFlow());
			}
		}
		if (answer.getIntentfilters() != null) {
			returnAnswer.setIntentfilters(new Intentfilters());
			if (!answer.getIntentfilters().getIntentfilter().isEmpty()) {
				returnAnswer.getIntentfilters().getIntentfilter().addAll(answer.getIntentfilters().getIntentfilter());
			}
		}
		if (answer.getIntents() != null) {
			returnAnswer.setIntents(new Intents());
			if (!answer.getIntents().getIntent().isEmpty()) {
				returnAnswer.getIntents().getIntent().addAll(answer.getIntents().getIntent());
			}
		}
		if (answer.getIntentsinks() != null) {
			returnAnswer.setIntentsinks(new Intentsinks());
			if (!answer.getIntentsinks().getIntentsink().isEmpty()) {
				returnAnswer.getIntentsinks().getIntentsink().addAll(answer.getIntentsinks().getIntentsink());
			}
		}
		if (answer.getIntentsources() != null) {
			returnAnswer.setIntentsources(new Intentsources());
			if (!answer.getIntentsources().getIntentsource().isEmpty()) {
				returnAnswer.getIntentsources().getIntentsource().addAll(answer.getIntentsources().getIntentsource());
			}
		}
		if (answer.getPermissions() != null) {
			returnAnswer.setPermissions(new Permissions());
			if (!answer.getPermissions().getPermission().isEmpty()) {
				returnAnswer.getPermissions().getPermission().addAll(answer.getPermissions().getPermission());
			}
		}
		return returnAnswer;
	}

	public static Reference copy(final Reference reference) {
		if (reference == null) {
			return null;
		}

		final Reference newReference = new Reference();
		if (reference.getApp() != null) {
			newReference.setApp(reference.getApp());
		}
		if (reference.getClassname() != null) {
			newReference.setClassname(reference.getClassname());
		}
		if (reference.getMethod() != null) {
			newReference.setMethod(reference.getMethod());
		}
		if (reference.getStatement() != null) {
			newReference.setStatement(reference.getStatement());
		}

		return newReference;
	}

	public static Reference createReference(String aqlQueryReference) {
		final Reference ref = new Reference();
		if (aqlQueryReference.contains("Statement('")) {
			if (aqlQueryReference.indexOf("')") > aqlQueryReference.indexOf("->")) {
				ref.setStatement(Helper.createStatement(Helper.cut(aqlQueryReference, "Statement('", "',")));
				ref.getStatement()
						.setLinenumber(Integer.parseInt(Helper.cut(aqlQueryReference, "',", ")").replace(" ", "")));
			} else {
				ref.setStatement(Helper.createStatement(Helper.cut(aqlQueryReference, "Statement('", "')")));
			}
		}
		if (aqlQueryReference.contains("Method('")) {
			ref.setMethod(Helper.cut(aqlQueryReference, "Method('", "')"));
		}
		if (aqlQueryReference.contains("Class('")) {
			ref.setClassname(Helper.cut(aqlQueryReference, "Class('", "')"));
		}
		if (aqlQueryReference.contains("App('")) {
			final File appFile = new File(Helper.cut(aqlQueryReference, "App('", "')"));
			ref.setApp(Helper.createApp(appFile));
		}
		return ref;
	}

	public static Statement createStatement(final String jimpleString) {
		return createStatement(jimpleString, true, -1);
	}

	public static Statement createStatement(final String jimpleString, int linenumber) {
		return createStatement(jimpleString, true, linenumber);
	}

	public static Statement createStatement(final String jimpleString, boolean assignValues) {
		return createStatement(jimpleString, assignValues, -1);
	}

	public static Statement createStatement(String jimpleString, boolean assignValues, int linenumber) {
		final Statement newstatement = new Statement();
		String statementfull = jimpleString;
		while (statementfull.startsWith("\t")) {
			statementfull = statementfull.substring(1);
		}
		newstatement.setStatementfull(statementfull);
		if (-1 < jimpleString.indexOf("<") && jimpleString.indexOf("<") < jimpleString.indexOf(">(")) {
			newstatement.setStatementgeneric(Helper.cutFromFirstToLast(jimpleString, "<", ">("));
		} else if (-1 < jimpleString.indexOf("<") && jimpleString.indexOf("<") < jimpleString.indexOf(">")) {
			newstatement.setStatementgeneric(Helper.cutFromFirstToLast(jimpleString, "<", ">"));
		} else {
			newstatement.setStatementgeneric(jimpleString);
		}

		if (linenumber > -1) {
			newstatement.setLinenumber(linenumber);
		}

		// Parameters
		final int openCount = Helper.countStringOccurences(jimpleString, "(");
		final int closeCount = Helper.countStringOccurences(jimpleString, ")");
		if (openCount >= 1 && closeCount >= 1) {
			final String classes = Helper.cut(jimpleString, "(", ")", 1);
			if (!classes.equals("")) {
				newstatement.setParameters(new Parameters());
				final String[] parameterClasses = classes.split(",");
				for (int i = 0; i < parameterClasses.length; i++) {
					final Parameter parameter = new Parameter();
					parameter.setType(parameterClasses[i]);
					newstatement.getParameters().getParameter().add(parameter);
				}
			}
			if (assignValues && openCount >= 2 && closeCount >= 2) {
				final String values = Helper.cut(jimpleString, "(", ")", 2);
				if (!values.equals("")) {
					final String[] parameterValues = values.split(", ");
					for (int i = 0; i < parameterValues.length
							&& i < newstatement.getParameters().getParameter().size(); i++) {
						newstatement.getParameters().getParameter().get(i).setValue(parameterValues[i]);
					}
				}
			}
		}

		return newstatement;
	}

	public static String getStatementfullSafe(Statement statement) {
		if (statement.getStatementfull() != null && !statement.getStatementfull().isEmpty()) {
			return statement.getStatementfull();
		} else if (statement.getStatementgeneric() != null && !statement.getStatementgeneric().isEmpty()) {
			return statement.getStatementgeneric();
		}
		return null;
	}

	public static String getStatementgenericSafe(Statement statement) {
		if (statement.getStatementgeneric() != null && !statement.getStatementgeneric().isEmpty()) {
			return statement.getStatementgeneric();
		} else if (statement.getStatementfull() != null && !statement.getStatementfull().isEmpty()) {
			if (statement.getStatementfull().contains("<")
					&& statement.getStatementfull().indexOf("<") < statement.getStatementfull().indexOf("<")) {
				return Helper.cut(statement.getStatementfull(), "<", ">");
			}
			return statement.getStatementfull();
		}
		return null;
	}

	public static String toString(Object item) {
		if (item instanceof Answer) {
			return toString((Answer) item);
		} else if (item instanceof Reference) {
			return toString((Reference) item);
		} else if (item instanceof Permissions) {
			return toString((Permissions) item);
		} else if (item instanceof Permission) {
			return toString((Permission) item);
		} else if (item instanceof Flows) {
			return toString(item);
		} else if (item instanceof Flow) {
			return toString((Flow) item);
		} else if (item instanceof Intents) {
			return toString((Intents) item);
		} else if (item instanceof Intent) {
			return toString((Intent) item);
		} else if (item instanceof Intentfilters) {
			return toString((Intentfilters) item);
		} else if (item instanceof Intentfilter) {
			return toString((Intentfilter) item);
		} else if (item instanceof Intentsources) {
			return toString((Intentsources) item);
		} else if (item instanceof Intentsource) {
			return toString((Intentsource) item);
		} else if (item instanceof Intentsinks) {
			return toString((Intentsinks) item);
		} else if (item instanceof Intentsink) {
			return toString((Intentsink) item);
		} else if (item instanceof Target) {
			return toString((Target) item);
		} else if (item instanceof Data) {
			return toString((Data) item);
		} else if (item instanceof Tool) {
			return toString((Tool) item);
		} else {
			return item.toString();
		}
	}

	public static String toString(final Answer answer) {
		final StringBuilder sb = new StringBuilder();

		if (answer.getPermissions() != null) {
			sb.append("*** Permissions ***\n" + toString(answer.getPermissions()) + "\n");
		}
		if (answer.getFlows() != null) {
			sb.append("*** Flows ***\n" + toString(answer.getFlows()) + "\n");
		}
		if (answer.getSources() != null) {
			sb.append("*** Sources ***\n" + toString(answer.getSources()) + "\n");
		}
		if (answer.getSinks() != null) {
			sb.append("*** Sinks ***\n" + toString(answer.getSinks()) + "\n");
		}
		if (answer.getIntents() != null) {
			sb.append("*** Intents ***\n" + toString(answer.getIntents()) + "\n");
		}
		if (answer.getIntentfilters() != null) {
			sb.append("*** IntentFilters ***\n" + toString(answer.getIntentfilters()) + "\n");
		}
		if (answer.getIntentsources() != null) {
			sb.append("*** IntentSources ***\n" + toString(answer.getIntentsources()) + "\n");
		}
		if (answer.getIntentsinks() != null) {
			sb.append("*** IntentSinks ***\n" + toString(answer.getIntentsinks()) + "\n");
		}

		return sb.toString();
	}

	public static String toString(final Reference reference) {
		return toString(reference, "->", false);
	}

	public static String toString(final Reference reference, String separator) {
		return toString(reference, separator, false);
	}

	public static String toString(final Reference reference, boolean replaceQuotes) {
		return toString(reference, "->", replaceQuotes);
	}

	public static String toString(final Reference reference, String separator, boolean replaceQuotes) {
		final StringBuilder sb = new StringBuilder();

		if (reference != null) {
			if (reference.getStatement() != null && reference.getStatement().getStatementfull() != null) {
				sb.append(toStringStatement(reference.getStatement(), replaceQuotes) + separator);
			}
			if (reference.getMethod() != null) {
				sb.append(toStringMethod(reference.getMethod()) + separator);
			}
			if (reference.getClassname() != null) {
				sb.append(toStringClass(reference.getClassname()) + separator);
			}
			if (reference.getApp() != null && reference.getApp().getFile() != null) {
				sb.append(toStringApp(reference.getApp()));
			} else {
				sb.append("No .apk defined (Not App specific)");
			}
		} else {
			sb.append("No Reference");
		}

		return sb.toString();
	}

	public static String toString(final Statement statement) {
		return toString(statement, false);
	}

	public static String toString(final Statement statement, boolean replaceQuotes) {
		return toStringStatement(statement, replaceQuotes);
	}

	public static String toStringStatement(final Statement statement) {
		return toStringStatement(statement, false);
	}

	public static String toStringStatement(final Statement statement, boolean replaceQuotes) {
		return "Statement('"
				+ (replaceQuotes ? CLIHelper.replaceQuotesWithNeedles(statement.getStatementfull())
						: statement.getStatementfull())
				+ "'" + (statement.getLinenumber() != null ? ", " + statement.getLinenumber().intValue() : "") + ")";
	}

	public static String toStringMethod(final String method) {
		return "Method('" + method + "')";
	}

	public static String toStringClass(final String classname) {
		return "Class('" + classname + "')";
	}

	public static String toStringApp(final App app) {
		return "App('" + app.getFile() + "')";
	}

	public static String toRAW(Object item) {
		return RAW.toRAW(item);
	}

	public static String toRAW(final Tool tool) {
		return RAW.toRAW(tool);
	}

	public static String toRAW(Flow flow) {
		return RAW.toRAW(flow);
	}

	public static String toRAW(Permission permission) {
		return RAW.toRAW(permission);
	}

	public static String toRAW(Intentsink intentsink) {
		return RAW.toRAW(intentsink);
	}

	public static String toRAW(Intentsource intentsource) {
		return RAW.toRAW(intentsource);
	}

	public static String toRAW(Reference from, Reference to) {
		return RAW.toRAW(from, to);
	}

	public static String toRAW(final Reference reference) {
		return RAW.toRAW(reference);
	}

	public static String toRAW(final App app) {
		return RAW.toRAW(app);
	}

	public static String toString(final Permissions permissions) {
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < permissions.getPermission().size(); i++) {
			sb.append("#" + (i + 1) + ":\n" + toString(permissions.getPermission().get(i)));
			if (i < permissions.getPermission().size() - 1) {
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	public static String toString(final Permission permission) {
		return "Name: " + permission.getName() + "\nReference: " + toString(permission.getReference())
				+ toString(permission.getAttributes());
	}

	public static String toString(final Flows flows) {
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < flows.getFlow().size(); i++) {
			sb.append("#" + (i + 1) + ":\n");
			sb.append(toString(flows.getFlow().get(i)));
			if (i < flows.getFlow().size() - 1) {
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	public static String toString(final Flow flow) {
		return "From:\n" + toString(Helper.getFrom(flow.getReference())) + "\nTo:\n"
				+ toString(Helper.getTo(flow.getReference())) + toString(flow.getAttributes());
	}

	public static String toString(final Sources sources) {
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < sources.getSource().size(); i++) {
			sb.append("#" + (i + 1) + ":\n");
			sb.append(toString(sources.getSource().get(i)));
			if (i < sources.getSource().size() - 1) {
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	public static String toString(final Source source) {
		final StringBuilder sb = new StringBuilder();

		sb.append("Reference:\n" + toString(source.getReference()));

		return sb.toString() + toString(source.getAttributes());
	}

	public static String toString(final Sinks sinks) {
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < sinks.getSink().size(); i++) {
			sb.append("#" + (i + 1) + ":\n");
			sb.append(toString(sinks.getSink().get(i)));
			if (i < sinks.getSink().size() - 1) {
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	public static String toString(final Sink sink) {
		final StringBuilder sb = new StringBuilder();

		sb.append("Reference:\n" + toString(sink.getReference()));

		return sb.toString() + toString(sink.getAttributes());
	}

	public static String toString(final Intents intents) {
		return toString(intents, false);
	}

	public static String toString(final Intents intents, boolean detailData) {
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < intents.getIntent().size(); i++) {
			sb.append("#" + (i + 1) + ":\n");
			sb.append(toString(intents.getIntent().get(i), detailData));
			if (i < intents.getIntent().size() - 1) {
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	public static String toString(final Intent intent) {
		return toString(intent, false);
	}

	public static String toString(final Intent intent, boolean detailData) {
		final StringBuilder sb = new StringBuilder();

		sb.append(toString(intent.getTarget(), detailData));
		sb.append("Reference:\n" + toString(intent.getReference()));

		return sb.toString() + toString(intent.getAttributes());
	}

	public static String toString(final Intentfilters intentfilters) {
		return toString(intentfilters, false);
	}

	public static String toString(final Intentfilters intentfilters, boolean detailData) {
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < intentfilters.getIntentfilter().size(); i++) {
			sb.append("#" + (i + 1) + ":\n");
			sb.append(toString(intentfilters.getIntentfilter().get(i), detailData));
			if (i < intentfilters.getIntentfilter().size() - 1) {
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	public static String toString(final Intentfilter intentfilter) {
		return toString(intentfilter, false);
	}

	public static String toString(final Intentfilter intentfilter, boolean detailData) {
		final StringBuilder sb = new StringBuilder();

		if (intentfilter.getAction() != null && !intentfilter.getAction().isEmpty()) {
			for (final String action : intentfilter.getAction()) {
				sb.append("Action: " + action + "\n");
			}
		}
		if (intentfilter.getCategory() != null) {
			for (final String category : intentfilter.getCategory()) {
				sb.append("Category: " + category + "\n");
			}
		}
		if (intentfilter.getData() != null) {
			for (final Data data : intentfilter.getData()) {
				if (detailData) {
					sb.append("Data: {\n" + toString(data) + "}\n");
				} else {
					sb.append(
							"Data: is set" + (data.getType() != null ? " (Type: " + data.getType() + ")" : "") + "\n");
				}
			}
		}

		sb.append("Reference:\n" + toString(intentfilter.getReference()));

		return sb.toString() + toString(intentfilter.getAttributes());
	}

	public static String toString(final Intentsinks intentsinks) {
		return toString(intentsinks, false);
	}

	public static String toString(final Intentsinks intentsinks, boolean detailData) {
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < intentsinks.getIntentsink().size(); i++) {
			sb.append("#" + (i + 1) + ":\n");
			sb.append(toString(intentsinks.getIntentsink().get(i), detailData));
			if (i < intentsinks.getIntentsink().size() - 1) {
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	public static String toString(final Intentsink intentsink) {
		return toString(intentsink, false);
	}

	public static String toString(final Intentsink intentsink, boolean detailData) {
		final StringBuilder sb = new StringBuilder();

		sb.append(toString(intentsink.getTarget(), detailData));
		sb.append("Reference:\n" + toString(intentsink.getReference()));

		return sb.toString() + toString(intentsink.getAttributes());
	}

	public static String toString(final Intentsources intentsources) {
		return toString(intentsources, false);
	}

	public static String toString(final Intentsources intentsources, boolean detailData) {
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < intentsources.getIntentsource().size(); i++) {
			sb.append("#" + (i + 1) + ":\n");
			sb.append(toString(intentsources.getIntentsource().get(i), detailData));
			if (i < intentsources.getIntentsource().size() - 1) {
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	public static String toString(final Intentsource intentsource) {
		return toString(intentsource, false);
	}

	public static String toString(final Intentsource intentsource, boolean detailData) {
		final StringBuilder sb = new StringBuilder();

		sb.append(toString(intentsource.getTarget(), detailData));
		sb.append("Reference:\n" + toString(intentsource.getReference()));

		return sb.toString() + toString(intentsource.getAttributes());
	}

	public static String toString(final Attributes attributes) {
		if (attributes != null && !attributes.getAttribute().isEmpty()) {
			final StringBuilder sb = new StringBuilder("\nAttributes:");
			for (final Attribute attribute : attributes.getAttribute()) {
				sb.append("\n- " + attribute.getName() + " = " + attribute.getValue());
			}
			return sb.toString();
		}
		return "";
	}

	public static String toString(final Target target) {
		return toString(target, false);
	}

	public static String toString(final Target target, boolean detailData) {
		if (target != null) {
			final StringBuilder sb = new StringBuilder();

			if (target.getAction() != null && !target.getAction().isEmpty()) {
				for (final String action : target.getAction()) {
					sb.append("Action: " + action + "\n");
				}
			}
			if (target.getCategory() != null) {
				for (final String category : target.getCategory()) {
					sb.append("Category: " + category + "\n");
				}
			}
			if (target.getData() != null) {
				for (final Data data : target.getData()) {
					if (detailData) {
						sb.append("Data: {\n" + toString(data) + "}\n");
					} else {
						sb.append("Data: is set" + (data.getType() != null ? " (Type: " + data.getType() + ")" : "")
								+ "\n");
					}
				}
			}
			if (target.getReference() != null) {
				sb.append("Class: " + target.getReference().getClassname() + "\n");
			}

			if (!sb.isEmpty()) {
				return sb.toString();
			}
		}

		return "No target specified (Action/Category/Data Information missing)\n";
	}

	public static String toString(final Data data) {
		final StringBuilder sb = new StringBuilder();

		if (data.getHost() != null && data.getPort() != null) {
			sb.append("\tHost+Port: " + data.getHost() + ":" + data.getPort() + "\n");
		} else if (data.getHost() != null) {
			sb.append("\tHost: " + data.getHost() + "\n");
		} else if (data.getPort() != null) {
			sb.append("\tPort: " + data.getPort() + "\n");
		}
		if (data.getPath() != null) {
			sb.append("\tPath: " + data.getPath() + "\n");
		}
		if (data.getPathPattern() != null) {
			sb.append("\tPathPattern: " + data.getPathPattern() + "\n");
		}
		if (data.getPathPrefix() != null) {
			sb.append("\tPathPrefix: " + data.getPathPrefix() + "\n");
		}
		if (data.getScheme() != null) {
			sb.append("\tScheme: " + data.getScheme() + "\n");
		}
		if (data.getSsp() != null) {
			sb.append("\tSsp: " + data.getSsp() + "\n");
		}
		if (data.getSspPattern() != null) {
			sb.append("\tSspPattern: " + data.getSspPattern() + "\n");
		}
		if (data.getSspPrefix() != null) {
			sb.append("\tSspPrefix: " + data.getSspPrefix() + "\n");
		}
		if (data.getType() != null) {
			sb.append("\tType: " + data.getType() + "\n");
		}

		return sb.toString();
	}

	public static String toString(final List<Tool> tools) {
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < tools.size(); i++) {
			sb.append("#" + (i + 1) + ":\n");
			sb.append(toString(tools.get(i)));
			if (i < tools.size() - 1) {
				sb.append("\n\n");
			}
		}

		return sb.toString();
	}

	public static String toString(final Tool tool) {
		final StringBuilder sb = new StringBuilder();
		sb.append("Name: " + Helper.getQualifiedName(tool) + "\n");
		if (tool.getPriority().size() <= 1) {
			sb.append("Priority: " + tool.getPriority().get(0).getValue()
					+ (tool.getPriority().get(0).getFeature() != null
							&& !tool.getPriority().get(0).getFeature().equals("")
									? " (" + tool.getPriority().get(0).getFeature() + ")"
									: "")
					+ "\n");
		} else {
			sb.append("Priorities:\n");
			for (final Priority priority : tool.getPriority()) {
				sb.append(priority.getValue() + (priority.getFeature() != null && !priority.getFeature().equals("")
						? " (" + priority.getFeature() + ")"
						: "") + "\n");
			}
		}
		sb.append("Questions: " + tool.getQuestions() + "\n");
		sb.append("Path: " + tool.getPath() + "\n");
		if (tool.isExternal()) {
			sb.append("URL: " + tool.getExecute().getUrl() + "\n");
			sb.append("Username: " + tool.getExecute().getUsername() + "\n");
			sb.append("Password: " + tool.getExecute().getPassword() + "\n");
		} else {
			sb.append("Run: " + tool.getExecute().getRun() + "\n");
			sb.append("Result: " + tool.getExecute().getResult() + "\n");
			sb.append("Instances: " + tool.getExecute().getInstances() + "\n");
			sb.append("MemoryPerInstance: " + tool.getExecute().getMemoryPerInstance() + "\n");
		}
		sb.append("\nRun on Event:\n\t- Entry: " + replaceNull(tool.getRunOnEntry(), "-") + "\n\t- Success: "
				+ replaceNull(tool.getRunOnSuccess(), "-") + "\n\t- Fail: " + replaceNull(tool.getRunOnFail(), "-")
				+ "\n\t- Abort: " + replaceNull(tool.getRunOnAbort(), "-") + "\n\t- Exit: "
				+ replaceNull(tool.getRunOnExit(), "-"));

		return sb.toString();
	}

	public static String getQualifiedName(Tool tool) {
		return tool.getName() + " (" + tool.getVersion() + ")";
	}

	public static List<File> getExternalAppFiles(Task task) {
		final List<File> files = new ArrayList<>();
		final Question query = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(task);
		final String extQuery = Helper.getExternalQuery(task);
		for (final App app : query.getAllApps(false, false)) {
			final String allFiles = app.getFile();
			for (final String fileStr : allFiles.replace(", ", ",").split(",")) {
				final File file = new File(fileStr);
				if (extQuery.contains(file.getAbsolutePath())) {
					files.add(file);
				}
			}
		}
		return files;
	}

	public static String[] getRunCommandAsArray(String runCmd) {
		if (runCmd != null && !runCmd.isEmpty() && runCmd.contains(" ") && runCmd.contains("\"")) {
			String runCmdCopy = new String(runCmd);

			final Matcher m = Pattern.compile("\"(.*?)\"").matcher(runCmd);
			while (m.find()) {
				runCmdCopy = runCmdCopy.replace(m.group(0), m.group(0).replace(" ", SPACER));
			}

			final String[] runCmdAsArray = runCmdCopy.split(" ");
			for (int i = 0; i < runCmdAsArray.length; i++) {
				runCmdAsArray[i] = runCmdAsArray[i].replace(SPACER, " ");
			}

			return runCmdAsArray;
		} else {
			return runCmd.split(" ");
		}
	}

	public static String getExternalQuery(Task task) {
		final Map<String, String> replacementsSoFar = new HashMap<>();
		String query = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(task).toString();
		for (final Task child : task.getChildren()) {
			if (child.getTaskAnswer().isAnswered()) {
				String needle;
				final String replacement;
				if (child instanceof PreprocessorTask) {
					needle = ((PreprocessorTask) child).getQuestionReference().toString();
					replacement = "App(" + child.getTaskAnswer().getAnswerForQuery(true) + ")";
				} else {
					needle = Storage.getInstance().getData().getQuestionFromQuestionTaskMap(child).toString();
					replacement = child.getTaskAnswer().getAnswerForQuery(true);
				}
				for (final String key : replacementsSoFar.keySet()) {
					needle = needle.replace(key, replacementsSoFar.get(key));
				}
				query = query.replace(needle, replacement);
				replacementsSoFar.put(needle, replacement);
			}
		}
		return query;
	}

	public static List<File> getExternalQueryFiles(Task task) {
		final List<File> files = new ArrayList<>();
		for (final Task child : task.getChildren()) {
			if (child.getTaskAnswer().isAnswered() && (child.getTaskAnswer().getType() == TaskAnswer.ANSWER_TYPE_AQL
					|| child.getTaskAnswer().getType() == TaskAnswer.ANSWER_TYPE_FILE)) {
				files.add(child.getTaskAnswer().getAnswerFile());
			}
		}
		return files;
	}

	public static List<File> getPreprocessorFiles(Task task) {
		final List<File> apps = new ArrayList<>();
		final String appsStr = task.getTaskInfo().getData(PreprocessorTaskInfo.APP_APK);
		for (final String appStr : appsStr.replace(", ", ",").split(",")) {
			apps.add(new File(appStr));
		}
		Log.msg("Files given to external preprocessor: " + apps, Log.DEBUG_DETAILED);
		return apps;
	}

	public static String replaceVariables(String string, Task task) {
		return replaceVariables(string, task, null);
	}

	public static String replaceVariables(String string, Task task, Map<String, String> customVariables) {
		return replaceVariables(string, task, customVariables, false);
	}

	public static String replaceVariables(String string, Task task, Map<String, String> customVariables,
			boolean useHashesAndNoDate) {
		return replaceVariables(string, task, customVariables, useHashesAndNoDate, useHashesAndNoDate);
	}

	public static String replaceVariables(String string, Task task, Map<String, String> customVariables,
			boolean useHashes, boolean noDate) {
		final Set<String> fileVariableNames = task.getTaskInfo().getAllFileVariableNames();
		for (final String variableName : task.getTaskInfo().getAllSetVariableNames()) {
			final String replacement = task.getTaskInfo().getData(variableName);
			if (replacement != null) {
				if (useHashes) {
					boolean isFileVariable = fileVariableNames.contains(variableName);

					// Variables can have arbitrary names, however, if they follow the naming convention %FILE_*% they will be treated as files. By default variables are treated like strings.
					if (!isFileVariable) {
						isFileVariable = variableName.startsWith("%FILE_") && variableName.endsWith("%");
					}

					// Variables will also be treated as files if they reference an existing file and their value has the following format: x.y (where x, y only include Aa0-Zz9, but x may also hold path separators as well as - or _ and y must have a length of 1-5)
					if (!isFileVariable) {
						if (new File(replacement).exists()) {
							if (replacement.contains(".")) {
								final String[] arr = replacement.split("\\.");
								isFileVariable = arr.length == 2
										&& arr[0].replaceFirst(":", "").matches("[a-zA-Z0-9/\\\\\\-_]+")
										&& arr[1].length() < 5 && arr[1].matches("[a-zA-Z0-9]+");
							}
						}
					}

					if (isFileVariable) {
						final List<FileWithHash> files = new LinkedList<>();
						final String[] parts = replacement.split(",");
						for (int i = 0; i < parts.length; i++) {
							while (parts[i].startsWith(" ")) {
								parts[i] = parts[i].substring(1);
							}
							files.add(new FileWithHash(parts[i]));
						}
						string = string.replace(variableName, getAnswerFilesAsHash(files));
						continue;
					}
				}
				if (variableName.equals(TaskInfo.DATE)) {
					if (noDate) {
						string = string.replace(variableName, "DATE-SKIPPED");
					} else {
						string = string.replace(variableName, replacement);
					}
				} else if (variableName.equals(ToolTaskInfo.STATEMENT_IN)
						|| variableName.equals(ToolTaskInfo.STATEMENT_FROM)
						|| variableName.equals(ToolTaskInfo.STATEMENT_TO)) {
					string = string.replace(variableName, CLIHelper.replaceQuotesWithNeedles(replacement));
				} else {
					string = string.replace(variableName, replacement);
				}
			}
		}
		if (string.contains(FakeToolHelper.FAKETOOL_VARIABLE)) {
			string = FakeToolHelper.replaceFakeToolVariable(string);
		}
		if (customVariables == null) {
			return string;
		} else {
			return replaceCustomVariables(string, customVariables);
		}
	}

	public static String replaceCustomVariables(String string, Map<String, String> customVariables) {
		for (String variableName : customVariables.keySet()) {
			final String replacement = customVariables.get(variableName);
			if (replacement != null) {
				variableName = toVariableName(variableName);
				string = string.replace(variableName, replacement);
			}
		}
		return string;
	}

	public static String toVariableName(String variableName) {
		if (!variableName.startsWith("%")) {
			variableName = "%" + variableName;
		}
		if (!variableName.endsWith("%")) {
			variableName = variableName + "%";
		}
		return variableName;
	}

	public static String reportMissingVariables(String runCmd, Task task) {
		final String unVars = reportMissingVariables(runCmd);
		if (unVars != null && !(task.getTool() instanceof DefaultAnalysisTool)) {
			Log.warning("Run command of tool " + Helper.getQualifiedName(task.getTool()) + " contains "
					+ (unVars.contains(",") ? "unresolved variables" : "an unresolved variable") + ": " + unVars);
		}
		return unVars;
	}

	public static String reportMissingVariables(String string) {
		final StringBuilder sb = new StringBuilder();
		final Matcher m = Pattern.compile("%(.*?)%").matcher(string);
		while (m.find()) {
			if (sb.length() > 0) {
				sb.append(", ");
			}
			sb.append(m.group(0));
		}
		if (sb.length() > 0) {
			return sb.toString();
		} else {
			return null;
		}
	}

	public static List<FileWithHash> getAnswerChildrenAsString(Task task) {
		final List<FileWithHash> answerFiles = new ArrayList<>();
		for (final Task taskChild : task.getChildren()) {
			if (taskChild.getTaskAnswer().isAnswered()) {
				answerFiles.add(new FileWithHash(taskChild.getTaskAnswer().getAnswerFile()));
			}
		}
		return answerFiles;
	}

	public static String getAnswerFilesAsString(List<FileWithHash> answerFiles) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < answerFiles.size(); i++) {
			sb.append(answerFiles.get(i).getFile().getAbsolutePath().replace("\\", "/"))
					.append(i != answerFiles.size() - 1 ? ", " : "");
		}
		return sb.toString();
	}

	/**
	 * Use this method to create %ANSWERSHASH% with SHA-256
	 *
	 * @param answerFile
	 *            the input file
	 * @return the hash
	 */
	public static String getAnswerFilesAsHash(FileWithHash answerFile) {
		return getAnswerFilesAsHash(answerFile, HashHelper.HASH_TYPE_SHA256);
	}

	/**
	 * Use this method to create %ANSWERSHASH% of any type
	 *
	 * @param answerFile
	 *            the input file
	 * @return the hash
	 */
	public static String getAnswerFilesAsHash(FileWithHash answerFile, String hashAlgorithm) {
		return getAnswerFilesAsHash(Collections.singletonList(answerFile), hashAlgorithm);
	}

	/**
	 * Use this method to create %ANSWERSHASH% with SHA-256
	 *
	 * @param answerFiles
	 *            the input files
	 * @return the hash
	 */
	public static String getAnswerFilesAsHash(List<FileWithHash> answerFiles) {
		return getAnswerFilesAsHash(answerFiles, HashHelper.HASH_TYPE_SHA256);
	}

	/**
	 * Use this method to create %ANSWERSHASH% of any type
	 *
	 * @param answerFiles
	 *            the input files
	 * @return the hash
	 */
	public static String getAnswerFilesAsHash(List<FileWithHash> answerFiles, String hashAlgorithm) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < answerFiles.size(); i++) {
			sb.append(answerFiles.get(i).getHash().getValue() + (i != answerFiles.size() - 1 ? ", " : ""));
		}
		return HashHelper.hash(sb.toString(), hashAlgorithm);
	}

	/**
	 * Create a hashed file list from ordinary file list
	 *
	 * @param answerFiles
	 * @return the hashed file list
	 */
	public static List<FileWithHash> toHashedFileList(List<File> answerFiles) {
		return toHashedFileList(answerFiles, HashHelper.HASH_TYPE_SHA256);
	}

	/**
	 * Create a hashed file list from ordinary file list
	 *
	 * @param answerFiles
	 * @return the hashed file list
	 */
	public static List<FileWithHash> toHashedFileList(List<File> answerFiles, String hashAlgorithm) {
		final List<FileWithHash> answerFilesHashed = new ArrayList<>();
		for (final File answerFile : answerFiles) {
			answerFilesHashed.add(new FileWithHash(answerFile, hashAlgorithm));
		}
		return answerFilesHashed;
	}

	public static String getMultipleApkName(String name) {
		final StringBuilder full = new StringBuilder("");
		for (String onePart : name.split(" ")) {
			onePart = onePart.replace("\\", "/");
			if (onePart.contains("/")) {
				if (full.length() != 0) {
					full.append(" ");
				}
				full.append(Helper.cut(onePart, "/", Helper.OCCURENCE_LAST));
			}
		}
		return full.toString().replace(",", "").replace(" ", "_").replace(FileHelper.FILE_ENDING_APK, "");
	}

	public static String getDate() {
		return getDate(System.currentTimeMillis());
	}

	public static String getDate(long timestamp) {
		return getDate(timestamp, "dd_MM_yyyy-HH_mm_ss");
	}

	public static String getDate(long timestamp, String format) {
		final Date date = new Date(timestamp);
		final DateFormat dateFormat = new SimpleDateFormat(format);
		return dateFormat.format(date);
	}

	public static long getTimestamp(String dateStr, String format) {
		try {
			return new SimpleDateFormat(format).parse(dateStr).getTime();
		} catch (final ParseException e) {
			return -1;
		}
	}

	public static App createApp(final File file) {
		return createApp(file.toString());
	}

	public static App createApp(String value) {
		final App app = new App();
		final File file = new File(value);
		app.setFile(getAppFileString(file));

		// Hashes
		final Hashes hashes = new Hashes();

		final Hash hashMD5 = new Hash();
		hashMD5.setType(HashHelper.HASH_TYPE_MD5);

		final Hash hashSHA1 = new Hash();
		hashSHA1.setType(HashHelper.HASH_TYPE_SHA1);

		final Hash hashSHA256 = new Hash();
		hashSHA256.setType(HashHelper.HASH_TYPE_SHA256);

		if (file.exists()) {
			hashMD5.setValue(HashHelper.md5Hash(file));
			hashSHA1.setValue(HashHelper.sha1Hash(file));
			hashSHA256.setValue(HashHelper.sha256Hash(file));
		} else {
			Log.msg("Could not find file for hash creation (may not be necesarry at this point): "
					+ file.getAbsolutePath(), Log.DEBUG);
			hashMD5.setValue(HashHelper.md5Hash(value));
			hashSHA1.setValue(HashHelper.sha1Hash(value));
			hashSHA256.setValue(HashHelper.sha256Hash(value));
		}

		hashes.getHash().add(hashMD5);
		hashes.getHash().add(hashSHA1);
		hashes.getHash().add(hashSHA256);

		app.setHashes(hashes);

		return app;
	}

	public static String getAppFileString(File appFile) {
		return appFile.getAbsolutePath().replace("\\", "/");
	}

	public static long getPid(Process p) {
		return p.toHandle().pid();

		// Old Java 8 version:
		// Field f;
		// if (Platform.isWindows()) {
		// try {
		// f = p.getClass().getDeclaredField("handle");
		// f.setAccessible(true);
		// final WinNT.HANDLE handle = new WinNT.HANDLE();
		// handle.setPointer(Pointer.createConstant(f.getLong(p)));
		// final int pid = Kernel32.INSTANCE.GetProcessId(handle);
		// return pid;
		// } catch (final Exception ex) {
		// Log.error("Could not identify process ID on this Windows operating system.");
		// }
		// } else if (Platform.isLinux()) {
		// try {
		// f = p.getClass().getDeclaredField("pid");
		// f.setAccessible(true);
		// final int pid = (Integer) f.get(p);
		// return pid;
		// } catch (final Exception ex) {
		// Log.error("Could not identify process ID on this Linux operating system.");
		// }
		// } else {
		// Log.error("Could not identify operating system.");
		// }
		// return -1;
	}

	public static Reference getFrom(Flow flow) {
		return getFrom(flow.getReference());
	}

	public static Reference getFrom(List<Reference> references) {
		for (final Reference ref : references) {
			if (ref.getType().equals(KeywordsAndConstantsHelper.REFERENCE_TYPE_FROM)) {
				return ref;
			}
		}
		return null;
	}

	public static Reference getTo(Flow flow) {
		return getTo(flow.getReference());
	}

	public static Reference getTo(List<Reference> references) {
		for (final Reference ref : references) {
			if (ref.getType().equals(KeywordsAndConstantsHelper.REFERENCE_TYPE_TO)) {
				return ref;
			}
		}
		return null;
	}

	public static Answer removeRedundant(final Answer answer, EqualsOptions options) {
		Helper.maximizeInformation(answer, options, false);
		final EqualsOptions localEqualsOptions = options.copy().setOption(EqualsOptions.PRECISELY_TARGET, true);

		// Permissions
		if (answer.getPermissions() != null) {
			for (int i = 0; i < answer.getPermissions().getPermission().size() - 1; i++) {
				final Permission obj1 = answer.getPermissions().getPermission().get(i);
				for (int j = i + 1; j < answer.getPermissions().getPermission().size(); j++) {
					final Permission obj2 = answer.getPermissions().getPermission().get(j);
					if (EqualsHelper.equals(obj1, obj2, localEqualsOptions)) {
						answer.getPermissions().getPermission().remove(j);
						j--;
					}
				}
			}
		}

		// Intents
		if (answer.getIntents() != null) {
			for (int i = 0; i < answer.getIntents().getIntent().size() - 1; i++) {
				final Intent obj1 = answer.getIntents().getIntent().get(i);
				for (int j = i + 1; j < answer.getIntents().getIntent().size(); j++) {
					final Intent obj2 = answer.getIntents().getIntent().get(j);
					if (EqualsHelper.equals(obj1, obj2, localEqualsOptions)) {
						answer.getIntents().getIntent().remove(j);
						j--;
					}
				}
			}
		}

		// Intent-filters
		if (answer.getIntentfilters() != null) {
			for (int i = 0; i < answer.getIntentfilters().getIntentfilter().size() - 1; i++) {
				final Intentfilter obj1 = answer.getIntentfilters().getIntentfilter().get(i);
				for (int j = i + 1; j < answer.getIntentfilters().getIntentfilter().size(); j++) {
					final Intentfilter obj2 = answer.getIntentfilters().getIntentfilter().get(j);
					if (EqualsHelper.equals(obj1, obj2, localEqualsOptions)) {
						answer.getIntentfilters().getIntentfilter().remove(j);
						j--;
					}
				}
			}
		}

		// Sources
		if (answer.getSources() != null) {
			for (int i = 0; i < answer.getSources().getSource().size() - 1; i++) {
				final Source obj1 = answer.getSources().getSource().get(i);
				for (int j = i + 1; j < answer.getSources().getSource().size(); j++) {
					final Source obj2 = answer.getSources().getSource().get(j);
					if (EqualsHelper.equals(obj1, obj2, localEqualsOptions)) {
						answer.getSources().getSource().remove(j);
						j--;
					}
				}
			}
		}

		// Sinks
		if (answer.getSinks() != null) {
			for (int i = 0; i < answer.getSinks().getSink().size() - 1; i++) {
				final Sink obj1 = answer.getSinks().getSink().get(i);
				for (int j = i + 1; j < answer.getSinks().getSink().size(); j++) {
					final Sink obj2 = answer.getSinks().getSink().get(j);
					if (EqualsHelper.equals(obj1, obj2, localEqualsOptions)) {
						answer.getSinks().getSink().remove(j);
						j--;
					}
				}
			}
		}

		// Intent-sinks
		if (answer.getIntentsinks() != null) {
			for (int i = 0; i < answer.getIntentsinks().getIntentsink().size() - 1; i++) {
				final Intentsink obj1 = answer.getIntentsinks().getIntentsink().get(i);
				for (int j = i + 1; j < answer.getIntentsinks().getIntentsink().size(); j++) {
					final Intentsink obj2 = answer.getIntentsinks().getIntentsink().get(j);
					if (EqualsHelper.equals(obj1, obj2, localEqualsOptions)) {
						answer.getIntentsinks().getIntentsink().remove(j);
						j--;
					}
				}
			}
		}

		// Intent-sources
		if (answer.getIntentsources() != null) {
			for (int i = 0; i < answer.getIntentsources().getIntentsource().size() - 1; i++) {
				final Intentsource obj1 = answer.getIntentsources().getIntentsource().get(i);
				for (int j = i + 1; j < answer.getIntentsources().getIntentsource().size(); j++) {
					final Intentsource obj2 = answer.getIntentsources().getIntentsource().get(j);
					if (EqualsHelper.equals(obj1, obj2, localEqualsOptions)) {
						answer.getIntentsources().getIntentsource().remove(j);
						j--;
					}
				}
			}
		}

		// Flow
		if (answer.getFlows() != null) {
			for (int i = 0; i < answer.getFlows().getFlow().size() - 1; i++) {
				final Flow obj1 = answer.getFlows().getFlow().get(i);
				for (int j = i + 1; j < answer.getFlows().getFlow().size(); j++) {
					final Flow obj2 = answer.getFlows().getFlow().get(j);
					if (EqualsHelper.equals(obj1, obj2, localEqualsOptions)) {
						answer.getFlows().getFlow().remove(j);
						j--;
					}
				}
			}
		}

		return answer;
	}

	public static void maximizeInformation(Answer answer, EqualsOptions options, boolean parametersAndLinenumbersOnly) {
		final Collection<Reference> collection1 = Helper.getAllReferences(answer, true);
		final Collection<Reference> collection2;
		if (parametersAndLinenumbersOnly) {
			// collection1 = all that have no parameters or no line number
			// collection2 = all that have either parameters or a line number
			collection2 = new LinkedList<>();
			final Collection<Reference> toRemove = new LinkedList<>();
			for (final Reference ref : collection1) {
				if (ref.getStatement() == null) {
					toRemove.add(ref);
				} else if ((ref.getStatement().getParameters() != null
						&& !ref.getStatement().getParameters().getParameter().isEmpty())
						|| (ref.getStatement().getLinenumber() != null && ref.getStatement().getLinenumber() != -1)) {
					toRemove.add(ref);
					collection2.add(ref);
				}
			}
			collection1.removeAll(toRemove);
		} else {
			collection2 = Helper.getAllReferences(answer, true);
		}
		keepAllInformation(collection1, collection2, options);
	}

	private static void keepAllInformation(Collection<Reference> collection1, Collection<Reference> collection2,
			EqualsOptions options) {
		if (!collection1.isEmpty() && !collection2.isEmpty()) {
			for (final Reference ref1 : collection1) {
				for (final Reference ref2 : collection2) {
					if (ref1 == ref2) {
						continue;
					}
					if (EqualsHelper.equals(ref1, ref2, options)) {
						keepAllInformation(ref1, ref2, options);
					}
				}
			}
		}
	}

	private static void keepAllInformation(Reference ref1, Reference ref2, EqualsOptions options) {
		// Statement
		if (ref1.getStatement() != null && ref2.getStatement() != null) {
			if (ref1.getStatement().getStatementfull() != null && ref2.getStatement().getStatementfull() != null) {
				if (ref1.getStatement().getStatementfull().length() < ref2.getStatement().getStatementfull().length()) {
					ref1.getStatement().setStatementfull(ref2.getStatement().getStatementfull());
				}
			}
			if (ref1.getStatement().getStatementgeneric() != null
					&& ref2.getStatement().getStatementgeneric() != null) {
				if (ref1.getStatement().getStatementgeneric().length() < ref2.getStatement().getStatementgeneric()
						.length()) {
					ref1.getStatement().setStatementgeneric(ref2.getStatement().getStatementgeneric());
				}
			}
			if (ref1.getStatement().getLinenumber() == null || ref1.getStatement().getLinenumber() == -1) {
				if (ref2.getStatement().getLinenumber() != null && ref2.getStatement().getLinenumber() > -1) {
					ref1.getStatement().setLinenumber(ref2.getStatement().getLinenumber());
				}
			}
			if (ref1.getStatement().getParameters() == null && ref2.getStatement().getParameters() != null) {
				ref1.getStatement().setParameters(ref2.getStatement().getParameters());
			} else if (ref1.getStatement().getParameters() != null && ref2.getStatement().getParameters() != null) {
				keepAllParameterInformation(ref1.getStatement().getParameters().getParameter(),
						ref2.getStatement().getParameters().getParameter());
			}
		}

		// Method
		if (ref1.getMethod() != null && ref2.getMethod() != null) {
			if (ref1.getMethod().length() < ref2.getMethod().length()) {
				ref1.setMethod(ref2.getMethod());
			}
		}

		// Class
		if (ref1.getClassname() != null && ref2.getClassname() != null) {
			if (ref1.getClassname().length() < ref2.getClassname().length()) {
				ref1.setClassname(ref2.getClassname());
			}
		}
	}

	private static void keepAllParameterInformation(List<Parameter> params1, List<Parameter> params2) {
		if (params1.isEmpty() && !params2.isEmpty()) {
			params1.addAll(params2);
		} else if (!params1.isEmpty() && !params2.isEmpty()) {
			for (final Parameter param1 : params1) {
				for (final Parameter param2 : params2) {
					if (param1 == param2) {
						continue;
					}
					if (EqualsHelper.equals(param1, param2)) {
						keepAllParameterInformation(param1, param2);
					}
				}
			}
		}
	}

	private static void keepAllParameterInformation(Parameter param1, Parameter param2) {
		if (param1.getValue() == null || param1.getValue().length() < param2.getValue().length()) {
			param1.setValue(param2.getValue());
		}
	}

	public static int getCardinality(Tool tool, String operator) {
		if (tool == null || operator == null) {
			return 2;
		}
		if (tool instanceof DefaultOperator) {
			if (operator.equals(DefaultOperator.OPERATOR_FILTER)) {
				return 1;
			} else {
				return 2;
			}
		} else {
			for (final String splitStr : tool.getQuestions().replace(" ", "").split(",")) {
				if (splitStr.equals(operator)) {
					return 0;
				} else if (splitStr.contains("(") && splitStr.substring(0, splitStr.indexOf("(")).equals(operator)) {
					final String cardinality = splitStr.substring(splitStr.indexOf("(") + 1, splitStr.indexOf(")"));
					if (cardinality.equals("*")) {
						return 0;
					} else {
						return Integer.parseInt(cardinality);
					}
				}
			}
			return -1;
		}
	}

	public static File lastFileModified(File directory) {
		final File fl = directory;
		final File[] files = fl.listFiles(new FileFilter() {
			@Override
			public boolean accept(final File file) {
				return file.isFile();
			}
		});
		long lastMod = Long.MIN_VALUE;
		File choice = null;
		if (files != null) {
			for (final File file : files) {
				if (file.lastModified() > lastMod) {
					choice = file;
					lastMod = file.lastModified();
				}
			}
		}
		return choice;
	}

	public static String replaceNull(String input, String replacement) {
		if (input == null) {
			return replacement;
		} else {
			return input;
		}
	}

	public static void extractDataFromURI(String uri, Data data) {
		// <scheme>://<host>:<port>[<path>|<pathPrefix>|<pathPattern>]
		String temp = uri;
		data.setScheme(Helper.cutFromStart(temp, "://"));
		temp = Helper.cut(temp, "://");
		if (temp.contains(":")) {
			data.setHost(Helper.cutFromStart(temp, ":"));
			temp = Helper.cut(temp, ":");
			if (temp.contains("/")) {
				data.setPort(Helper.cutFromStart(temp, "/"));
				temp = Helper.cut(temp, "/");
			} else {
				data.setPort(temp);
				temp = "";
			}
		} else if (temp.contains("/")) {
			data.setHost(Helper.cutFromStart(temp, "/"));
		} else {
			data.setHost(temp);
			temp = "";
		}
		if (!temp.equals("")) {
			data.setPath(temp);
		}
	}

	public static void extractDataFromAuthority(String authority, Data data) {
		// <host>:<port>
		final String temp = authority;
		if (temp.contains(":")) {
			data.setHost(Helper.cutFromStart(temp, ":"));
			data.setPort(Helper.cut(temp, ":"));
		} else {
			data.setHost(temp);
		}
	}

	public static boolean isEmpty(Answer answer) {
		if ((answer.getFlows() == null || answer.getFlows().getFlow().isEmpty())
				&& (answer.getIntentfilters() == null || answer.getIntentfilters().getIntentfilter().isEmpty())
				&& (answer.getIntents() == null || answer.getIntents().getIntent().isEmpty())
				&& (answer.getIntentsinks() == null || answer.getIntentsinks().getIntentsink().isEmpty())
				&& (answer.getIntentsources() == null || answer.getIntentsources().getIntentsource().isEmpty())
				&& (answer.getPermissions() == null || answer.getPermissions().getPermission().isEmpty())) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isEmpty(Target target) {
		if (target != null) {
			if (!isEmpty(target.getReference())) {
				return false;
			}
			if (target.getAction() != null && !target.getAction().isEmpty()) {
				return false;
			}
			if (target.getCategory() != null && !target.getCategory().isEmpty()) {
				return false;
			}
			if (target.getData() != null && !target.getData().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public static boolean isEmpty(Reference reference) {
		if (reference != null) {
			if (reference.getStatement() != null) {
				return false;
			}
			if (reference.getMethod() != null) {
				return false;
			}
			if (reference.getClassname() != null) {
				return false;
			}
			if (reference.getApp() != null) {
				return false;
			}
		}
		return true;
	}

	public static Collection<Reference> getAllReferences(Answer answer) {
		return getAllReferences(answer, false);
	}

	public static Collection<Reference> getAllReferences(Answer answer, boolean doubleEntriesAllowed) {
		final Collection<Reference> references = new LinkedList<>();

		if (answer.getFlows() != null && !answer.getFlows().getFlow().isEmpty()) {
			for (final Flow item : answer.getFlows().getFlow()) {
				for (final Reference ref : item.getReference()) {
					if (!doubleEntriesAllowed) {
						boolean skip = false;
						for (final Reference temp : references) {
							if (EqualsHelper.equals(ref, temp)) {
								skip = true;
								break;
							}
						}
						if (skip) {
							continue;
						}
					}
					references.add(ref);
				}
			}
		}

		if (answer.getSources() != null && !answer.getSources().getSource().isEmpty()) {
			for (final Source item : answer.getSources().getSource()) {
				if (!doubleEntriesAllowed) {
					boolean skip = false;
					for (final Reference temp : references) {
						if (EqualsHelper.equals(item.getReference(), temp)) {
							skip = true;
							break;
						}
					}
					if (skip) {
						continue;
					}
				}
				references.add(item.getReference());
			}
		}

		if (answer.getSinks() != null && !answer.getSinks().getSink().isEmpty()) {
			for (final Sink item : answer.getSinks().getSink()) {
				if (!doubleEntriesAllowed) {
					boolean skip = false;
					for (final Reference temp : references) {
						if (EqualsHelper.equals(item.getReference(), temp)) {
							skip = true;
							break;
						}
					}
					if (skip) {
						continue;
					}
				}
				references.add(item.getReference());
			}
		}

		if (answer.getIntentfilters() != null && !answer.getIntentfilters().getIntentfilter().isEmpty()) {
			for (final Intentfilter item : answer.getIntentfilters().getIntentfilter()) {
				if (!doubleEntriesAllowed) {
					boolean skip = false;
					for (final Reference temp : references) {
						if (EqualsHelper.equals(item.getReference(), temp)) {
							skip = true;
							break;
						}
					}
					if (skip) {
						continue;
					}
				}
				references.add(item.getReference());
			}
		}

		if (answer.getIntents() != null && !answer.getIntents().getIntent().isEmpty()) {
			for (final Intent item : answer.getIntents().getIntent()) {
				if (!doubleEntriesAllowed) {
					boolean skip = false;
					for (final Reference temp : references) {
						if (EqualsHelper.equals(item.getReference(), temp)) {
							skip = true;
							break;
						}
					}
					if (skip) {
						continue;
					}
				}
				references.add(item.getReference());
			}
		}

		if (answer.getIntentsinks() != null && !answer.getIntentsinks().getIntentsink().isEmpty()) {
			for (final Intentsink item : answer.getIntentsinks().getIntentsink()) {
				if (!doubleEntriesAllowed) {
					boolean skip = false;
					for (final Reference temp : references) {
						if (EqualsHelper.equals(item.getReference(), temp)) {
							skip = true;
							break;
						}
					}
					if (skip) {
						continue;
					}
				}
				references.add(item.getReference());
			}
		}

		if (answer.getIntentsources() != null && !answer.getIntentsources().getIntentsource().isEmpty()) {
			for (final Intentsource item : answer.getIntentsources().getIntentsource()) {
				if (!doubleEntriesAllowed) {
					boolean skip = false;
					for (final Reference temp : references) {
						if (EqualsHelper.equals(item.getReference(), temp)) {
							skip = true;
							break;
						}
					}
					if (skip) {
						continue;
					}
				}
				references.add(item.getReference());
			}
		}

		if (answer.getPermissions() != null && !answer.getPermissions().getPermission().isEmpty()) {
			for (final Permission item : answer.getPermissions().getPermission()) {
				if (!doubleEntriesAllowed) {
					boolean skip = false;
					for (final Reference temp : references) {
						if (EqualsHelper.equals(item.getReference(), temp)) {
							skip = true;
							break;
						}
					}
					if (skip) {
						continue;
					}
				}
				references.add(item.getReference());
			}
		}

		return references;
	}

	public static String replaceAllWhiteSpaceChars(String string) {
		return replaceAllWhiteSpaceChars(string, false);
	}

	public static String replaceAllWhiteSpaceChars(String string, boolean removeTrailingWhiteSpace) {
		String s = string.replaceAll("\\s+", " ");
		if (removeTrailingWhiteSpace && s.endsWith(" ")) {
			s = s.substring(0, s.length() - 1);
		}
		return s;
	}

	public static String replaceDoubleSpaces(String input) {
		while (input.contains("  ")) {
			input = input.replace("  ", " ");
		}
		return input;
	}

	public static ConverterTask getConverterParent(Task task) {
		if (task.getParents().size() == 1) {
			final Task parentTask = task.getParents().iterator().next();
			if (parentTask instanceof ConverterTask) {
				return (ConverterTask) parentTask;
			}
		}
		return null;
	}

	public static String autoformat(String query) {
		return autoformat(query, true);
	}

	public static String autoformat(String query, boolean breaklines) {
		query = Helper.replaceAllWhiteSpaceChars(query);

		final StringBuilder sb = new StringBuilder();
		int indent = 0;

		boolean inString = false;
		final int len = query.length();
		String lastC = "";
		for (int i = 0; i < len; i++) {
			final String c = String.valueOf(query.charAt(i));
			if (!inString) {
				if (lastC.equals(")") && !c.equals(" ") && !c.equals("-") && !c.equals("?") && !c.equals("!")
						&& !c.equals(".")) {
					sb.append(" ");
				}
				if (c.equals("[") || c.equals("{")) {
					sb.append(c + (breaklines ? "\n" : " "));
					indent++;
					if (breaklines) {
						for (int o = 0; o < indent; o++) {
							sb.append("\t");
						}
					}
				} else if (c.equals("]") || c.equals("}")) {
					sb.append((breaklines ? "\n" : " "));
					indent--;
					if (breaklines) {
						for (int o = 0; o < indent; o++) {
							sb.append("\t");
						}
					}
					sb.append(c);
				} else if ((c.equals("?") || c.equals("!") || c.equals("."))) {
					if (!lastC.equals(" ")) {
						sb.append(" ");
					}
					sb.append(c + (breaklines ? "\n" : " "));
					if (breaklines) {
						for (int o = 0; o < indent; o++) {
							sb.append("\t");
						}
					}
				} else if ((lastC.equals("?") || lastC.equals("!") || lastC.equals(".")) && c.equals(" ")) {
					// do not append
				} else {
					sb.append(c);
				}
			} else {
				sb.append(c);
			}
			lastC = c;
			if (c.equals("'")) {
				inString = !inString;
			}
		}

		String newContent = sb.toString();
		if (breaklines) {
			String tab = "\t";
			while (newContent.contains(tab)) {
				newContent = newContent.replace("\n" + tab + ",", ",\n" + tab);
				tab = tab + "\t";
			}
			newContent = newContent.replace("\t ", "\t").replaceAll("\n(\t)*\n", "\n").replaceAll("\n(\t)*\\)", ")");
		}
		newContent = newContent.replace(" -> ", "->").replace(" ->", "->").replace("-> ", "->");
		while (newContent.endsWith(" ")) {
			newContent = newContent.substring(0, newContent.length() - 1);
		}

		return newContent;
	}

	public static String getAppFromData(TaskInfo taskInfo) {
		if (taskInfo.getData(ToolTaskInfo.APP_APK_IN) != null && !taskInfo.getData(ToolTaskInfo.APP_APK_IN).isEmpty()) {
			return taskInfo.getData(ToolTaskInfo.APP_APK_IN);
		} else if (taskInfo.getData(ToolTaskInfo.APP_APK_FROM) != null
				&& !taskInfo.getData(ToolTaskInfo.APP_APK_FROM).isEmpty()) {
			return taskInfo.getData(ToolTaskInfo.APP_APK_FROM);
		} else if (taskInfo.getData(ToolTaskInfo.APP_APK_TO) != null
				&& !taskInfo.getData(ToolTaskInfo.APP_APK_TO).isEmpty()) {
			return taskInfo.getData(ToolTaskInfo.APP_APK_TO);
		} else if (taskInfo.getData(ToolTaskInfo.APP_APK) != null
				&& !taskInfo.getData(ToolTaskInfo.APP_APK).isEmpty()) {
			return taskInfo.getData(ToolTaskInfo.APP_APK);
		}
		return null;
	}

	public static int getLineNumberSafe(Reference reference) {
		return (reference == null || reference.getStatement() == null ? -1
				: getLineNumberSafe(reference.getStatement()));
	}

	public static int getLineNumberSafe(Statement statement) {
		return (statement.getLinenumber() == null ? -1 : statement.getLinenumber().intValue());
	}

	/**
	 * Checks if needle ist contained in the given list.
	 *
	 * @param list
	 * @param needle
	 *            Provide needle without "'" at start and end of the string.
	 * @return true if contained.
	 */
	public static boolean contains(List<IStringOrQuestion> list, String needle) {
		if (list != null) {
			for (final IStringOrQuestion item : list) {
				if (item.isComplete(true) && item.toStringInAnswer(false).equals(needle)) {
					return true;
				}
			}
		}
		return false;
	}

	public static Attribute getAttributeByName(Attributes attributes, String name) {
		if (attributes != null && !attributes.getAttribute().isEmpty()) {
			return getAttributeByName(attributes.getAttribute(), name);
		} else {
			return null;
		}
	}

	public static Attribute getAttributeByName(List<Attribute> attributes, String name) {
		for (final Attribute attr : attributes) {
			if (attr.getName().equals(name)) {
				return attr;
			}
		}
		return null;
	}

	public static String cleanupParameters(String jimpleInvokeString) {
		final String start = jimpleInvokeString.substring(0, jimpleInvokeString.indexOf('('));
		String parameters = jimpleInvokeString.substring(jimpleInvokeString.indexOf('('),
				jimpleInvokeString.indexOf(')'));
		while (parameters.contains(", ")) {
			parameters = parameters.replace(", ", ",");
		}
		final String end = jimpleInvokeString.substring(jimpleInvokeString.indexOf(')'));
		return start + parameters + end;
	}

	public static URL getURL(String url) {
		try {
			return new URL(url);
		} catch (final MalformedURLException e) {
			return null;
		}
	}

	public static Set<String> getAllDefaultVariableNames() {
		final Set<String> allVars = new HashSet<>();
		// Tools etc.
		final ToolTaskInfo tti = new ToolTaskInfo();
		allVars.addAll(tti.getAllVariableNames());

		// Operators etc.
		final FilterOperatorTaskInfo foti = new FilterOperatorTaskInfo();
		allVars.addAll(foti.getAllVariableNames());

		// Always
		allVars.add(TaskInfo.ANDROID_PLATFORMS);
		allVars.add(TaskInfo.ANDROID_BUILDTOOLS);
		allVars.add(TaskInfo.MEMORY);
		allVars.add(TaskInfo.PID);
		allVars.add(TaskInfo.DATE);

		return allVars;
	}

	/**
	 * Appends zeros to the start of the id.
	 *
	 * @param id
	 *            given id
	 * @return the required sequence of '0's and the given id
	 */
	public static String addZeroDigits(int id) {
		return addZeroDigits((long) id);
	}

	/**
	 * Appends zeros to the start of the id.
	 *
	 * @param id
	 *            given id
	 * @return the required sequence of '0's and the given id
	 */
	public static String addZeroDigits(long id) {
		if (id < 50000) {
			return Helper.addZeroDigits(id, 5);
		} else {
			return Helper.addZeroDigits(id, 10);
		}
	}

	/**
	 * Appends zeros to the start of the id until the given string length is reached.
	 *
	 * @param id
	 *            given id
	 * @param length
	 *            targeted sting length
	 * @return the required sequence of '0's and the given id
	 */
	public static String addZeroDigits(int id, int length) {
		return addZeroDigits((long) id, length);
	}

	/**
	 * Appends zeros to the start of the id until the given string length is reached.
	 *
	 * @param id
	 *            given id
	 * @param length
	 *            targeted sting length
	 * @return the required sequence of '0's and the given id
	 */
	public static String addZeroDigits(long id, int length) {
		final StringBuilder sb = new StringBuilder();
		for (int i = String.valueOf(id).length(); i < length; i++) {
			sb.append(0);
		}
		sb.append(id);
		return sb.toString();
	}

	public static String shorten(String string, int numberOrLines) {
		int index = 0;
		for (int i = 0; i < numberOrLines; i++) {
			index = string.indexOf("\n", index);
			if (index < 0 || index >= string.length()) {
				return string;
			} else {
				index++;
			}
		}
		return string.substring(0, index) + "...";
	}

	/**
	 * Returns feedback message if given file is a feedback file. Otherwise return null.
	 *
	 * @param file
	 *            Potential feedback file
	 * @return feedback as String or null
	 */
	public static String getFeedbackFromFile(File file) {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			boolean isFeedback = false;
			final StringBuilder sb = new StringBuilder();
			String line;
			for (int i = 0; (i < 3 || isFeedback) && (line = br.readLine()) != null; i++) {
				if (!isFeedback && line.contains(KeywordsAndConstantsHelper.FEEDBACK_ANSWER_STRING)) {
					isFeedback = true;
				}
				sb.append('\n').append(line);
			}
			if (isFeedback) {
				return sb.toString();
			}
		} catch (final IOException e) {
			Log.msg("Could not check received file for feedback information: " + file.getAbsolutePath()
					+ Log.getExceptionAppendix(e), Log.DEBUG_DETAILED);
		}
		return null;
	}
}