package de.foellix.aql.helper;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

import de.foellix.aql.Log;
import de.foellix.aql.config.Config;
import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.config.Priority;
import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.Attribute;
import de.foellix.aql.datastructure.Data;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.Hash;
import de.foellix.aql.datastructure.Hashes;
import de.foellix.aql.datastructure.IQuestionNode;
import de.foellix.aql.datastructure.Intent;
import de.foellix.aql.datastructure.Intentfilter;
import de.foellix.aql.datastructure.Intentfilters;
import de.foellix.aql.datastructure.Intents;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsinks;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.Intentsources;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.Parameter;
import de.foellix.aql.datastructure.Parameters;
import de.foellix.aql.datastructure.Permission;
import de.foellix.aql.datastructure.Permissions;
import de.foellix.aql.datastructure.PreviousQuestion;
import de.foellix.aql.datastructure.Question;
import de.foellix.aql.datastructure.QuestionFilter;
import de.foellix.aql.datastructure.QuestionPart;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.Statement;
import de.foellix.aql.datastructure.Target;
import de.foellix.aql.system.DefaultOperator;
import de.foellix.aql.system.task.OperatorTaskInfo;
import de.foellix.aql.system.task.PreprocessorTaskInfo;
import de.foellix.aql.system.task.TaskInfo;
import de.foellix.aql.system.task.ToolTaskInfo;

public class Helper {
	public static final int OCCURENCE_LAST = -1;

	public static String cut(final String input, final String from, final String to) {
		return cut(input, from, to, 1);
	}

	public static String cutFromFirstToLast(final String input, final String fromFirst, final String toLast) {
		try {
			return input.substring(input.indexOf(fromFirst) + fromFirst.length(), input.lastIndexOf(toLast));
		} catch (final StringIndexOutOfBoundsException e) {
			Log.msg("Non valid input: " + input, Log.DEBUG_DETAILED);
			return input;
		}
	}

	public static String cut(final String input, final String from) {
		return cut(input, from, 1);
	}

	public static String cut(final String input, final String from, final String to, int occurence) {
		try {
			int pos1 = 0;
			int pos2 = 0;

			if (occurence == OCCURENCE_LAST) {
				occurence = (input.length() - input.replace(from, "").length()) / from.length();
			}

			for (int i = 0; i < occurence; i++) {
				if (from != null) {
					pos1 = input.indexOf(from, pos1) + from.length();
					pos2 = input.indexOf(to, pos1);
				} else {
					pos2 = input.indexOf(to, pos2);
				}
			}

			return input.substring(pos1, pos2);
		} catch (final StringIndexOutOfBoundsException e) {
			Log.msg("Non valid input: " + input, Log.DEBUG_DETAILED);
			return input;
		}
	}

	public static String cut(final String input, final String from, int occurence) {
		try {
			int pos1 = 0;

			if (occurence == OCCURENCE_LAST) {
				occurence = (input.length() - input.replace(from, "").length()) / from.length();
			}

			for (int i = 0; i < occurence; i++) {
				pos1 = input.indexOf(from, pos1) + from.length();
			}

			return input.substring(pos1);
		} catch (final StringIndexOutOfBoundsException e) {
			Log.msg("Non valid input: " + input, Log.DEBUG_DETAILED);
			return input;
		}
	}

	public static String cutFromStart(final String input, final String to) {
		return cutFromStart(input, to, 1);
	}

	public static String cutFromStart(final String input, final String to, int occurence) {
		return cut(input, null, to, occurence);
	}

	public static int soiToType(final String soi) {
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

	public static String typeToSoi(final int type) {
		switch (type) {
		case KeywordsAndConstants.QUESTION_TYPE_FLOWS:
			return KeywordsAndConstants.SOI_FLOWS;
		case KeywordsAndConstants.QUESTION_TYPE_INTENTFILTER:
			return KeywordsAndConstants.SOI_INTENTFILTERS;
		case KeywordsAndConstants.QUESTION_TYPE_INTENTS:
			return KeywordsAndConstants.SOI_INTENTS;
		case KeywordsAndConstants.QUESTION_TYPE_INTENTSINKS:
			return KeywordsAndConstants.SOI_INTENTSINKS;
		case KeywordsAndConstants.QUESTION_TYPE_INTENTSOURCES:
			return KeywordsAndConstants.SOI_INTENTSOURCES;
		case KeywordsAndConstants.QUESTION_TYPE_PERMISSIONS:
			return KeywordsAndConstants.SOI_PERMISSIONS;
		default:
			return KeywordsAndConstants.SOI_UNKNOWN;
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

	public static IQuestionNode copy(final IQuestionNode question) {
		if (question instanceof Question || question instanceof QuestionFilter) {
			return copy((Question) question);
		} else if (question instanceof PreviousQuestion) {
			return copy((PreviousQuestion) question);
		} else {
			return copy((QuestionPart) question);
		}
	}

	public static Question copy(final Question question) {
		final Question newQuestion;
		if (question instanceof QuestionFilter) {
			newQuestion = new QuestionFilter(question.getOperator());
			((QuestionFilter) newQuestion).setName(((QuestionFilter) question).getName());
			((QuestionFilter) newQuestion).setValue(((QuestionFilter) question).getValue());
			((QuestionFilter) newQuestion).setSoi(((QuestionFilter) question).getSoi());
		} else {
			newQuestion = new Question(question.getOperator());
		}
		for (final IQuestionNode child : question.getChildren()) {
			newQuestion.getChildren().add(copy(child));
		}
		return newQuestion;
	}

	public static QuestionPart copy(final QuestionPart questionPart) {
		final QuestionPart newQuestionPart = new QuestionPart();
		newQuestionPart.setMode(questionPart.getMode());
		for (final Reference ref : questionPart.getAllReferences()) {
			newQuestionPart.addReference(copy(ref));
		}

		return newQuestionPart;
	}

	public static PreviousQuestion copy(final PreviousQuestion previousQuestion) {
		return new PreviousQuestion(previousQuestion.getFile());
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

	public static Statement createStatement(final String jimpleString) {
		return createStatement(jimpleString, true);
	}

	public static Statement createStatement(final String jimpleString, boolean assignValues) {
		final Statement newstatement = new Statement();
		newstatement.setStatementfull(jimpleString);
		newstatement.setStatementgeneric(cutFromFirstToLast(jimpleString, "<", ">"));

		// Parameters
		final String classes = Helper.cut(jimpleString, "(", ")", 1);
		if (!classes.equals("")) {
			final String values = Helper.cut(jimpleString, "(", ")", 2);

			newstatement.setParameters(new Parameters());

			final String[] parameterClasses = classes.split(",");
			final String[] parameterValues = values.split(", ");

			for (int i = 0; i < parameterClasses.length && (i < parameterValues.length || !assignValues); i++) {
				final Parameter parameter = new Parameter();
				parameter.setType(parameterClasses[i]);
				if (assignValues) {
					parameter.setValue(parameterValues[i]);
				}
				newstatement.getParameters().getParameter().add(parameter);
			}
		}

		return newstatement;
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
			return toString((Flows) item);
		} else if (item instanceof Flow) {
			return toString((Flow) item);
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
		if (answer.getIntentsources() != null) {
			sb.append("*** IntentSources ***\n" + toString(answer.getIntentsources()) + "\n");
		}
		if (answer.getIntentsinks() != null) {
			sb.append("*** IntentSinks ***\n" + toString(answer.getIntentsinks()) + "\n");
		}

		return sb.toString();
	}

	public static String toString(final Reference reference) {
		return toString(reference, "->");
	}

	public static String toString(final Reference reference, String separator) {
		final StringBuilder sb = new StringBuilder();

		if (reference != null) {
			if (reference.getStatement() != null && reference.getStatement().getStatementfull() != null) {
				sb.append("Statement('" + reference.getStatement().getStatementfull() + "')" + separator);
			}
			if (reference.getMethod() != null) {
				sb.append("Method('" + reference.getMethod() + "')" + separator);
			}
			if (reference.getClassname() != null) {
				sb.append("Class('" + reference.getClassname() + "')" + separator);
			}
			if (reference.getApp() != null && reference.getApp().getFile() != null) {
				sb.append("App('" + reference.getApp().getFile() + "')");
			} else {
				sb.append("No .apk defined (Not App specific)");
			}
		} else {
			sb.append("No Reference");
		}

		return sb.toString();
	}

	public static String toRAW(final Reference reference) {
		return toRAW(reference, false);
	}

	public static String toRAW(final Reference reference, boolean genericStatementOnly) {
		final StringBuilder sb = new StringBuilder();
		if (reference.getStatement() != null) {
			if (!genericStatementOnly) {
				sb.append(reference.getStatement().getStatementfull());
			} else {
				sb.append(reference.getStatement().getStatementgeneric());
			}
		}
		if (reference.getMethod() != null) {
			sb.append(reference.getMethod());
		}
		if (reference.getClassname() != null) {
			sb.append(reference.getClassname());
		}
		if (reference.getApp() != null) {
			sb.append(toRAW(reference.getApp()));
		}

		return sb.toString();
	}

	public static String toRAW(Object item) {
		if (item instanceof Permission) {
			return toRAW((Permission) item);
		} else if (item instanceof Intentsink) {
			return toRAW((Intentsink) item);
		} else if (item instanceof Intentsource) {
			return toRAW((Intentsource) item);
		} else if (item instanceof Reference) {
			return toRAW((Reference) item);
		} else {
			return null;
		}
	}

	public static String toRAW(final App app) {
		final StringBuilder sb = new StringBuilder();
		if (app.getHashes() != null && !app.getHashes().getHash().isEmpty()) {
			for (final Hash hash : app.getHashes().getHash()) {
				sb.append(hash.getType() + ": " + hash.getValue());
			}
		}
		return sb.toString();
	}

	public static String toRAW(final Tool tool) {
		return tool.getName() + "-" + tool.getVersion();
	}

	public static String toRAW(Permission permission) {
		final String temp = "Permission:" + permission.getName() + toRAW(permission.getReference());
		return temp.replaceAll("\\\n", "").replaceAll("\\\t", "");
	}

	public static String toRAW(Intentsink intentsink) {
		final StringBuilder sb = new StringBuilder("Intentsink:");
		if (intentsink.getTarget() != null) {
			sb.append(toString(intentsink.getTarget(), true));
		}
		if (intentsink.getReference() != null) {
			sb.append(toRAW(intentsink.getReference()));
		}
		if (sb.length() <= 0) {
			sb.append(intentsink.hashCode());
		}

		return sb.toString().replaceAll("\\\n", "").replaceAll("\\\t", "");
	}

	public static String toRAW(Intentsource intentsource) {
		final StringBuilder sb = new StringBuilder("Intentsource:");
		if (intentsource.getTarget() != null) {
			sb.append(toString(intentsource.getTarget(), true));
		}
		if (intentsource.getReference() != null) {
			sb.append(toRAW(intentsource.getReference()));
		}
		if (sb.length() <= 0) {
			sb.append(intentsource.hashCode());
		}

		return sb.toString().replaceAll("\\\n", "").replaceAll("\\\t", "");
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
		return permission.getName() + "\n-> " + toString(permission.getReference());
	}

	public static String toString(final Flows flows) {
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < flows.getFlow().size(); i++) {
			sb.append("#" + (i + 1) + ":\n");

			if (flows.getFlow().get(i).getAttributes() != null) {
				for (final Attribute attr : flows.getFlow().get(i).getAttributes().getAttribute()) {
					sb.append("(" + attr.getName() + " = " + attr.getValue() + ")\n");
				}
			}

			for (int j = 0; j < flows.getFlow().get(i).getReference().size(); j++) {
				final Reference reference = flows.getFlow().get(i).getReference().get(j);
				if (reference.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_FROM)) {
					sb.append("From:\n" + toString(reference));
				} else if (reference.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_TO)) {
					sb.append("To:\n" + toString(reference));
				}
				if (j != flows.getFlow().get(i).getReference().size() - 1) {
					sb.append("\n");
				}
			}
			if (i != flows.getFlow().size() - 1) {
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	public static String toString(final Flow flow) {
		return "From:\n" + toString(Helper.getFrom(flow.getReference())) + "\nTo:\n"
				+ toString(Helper.getTo(flow.getReference()));
	}

	public static String toString(final Intentsources intentsources) {
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < intentsources.getIntentsource().size(); i++) {
			sb.append("#" + (i + 1) + ":\n");
			sb.append(toString(intentsources.getIntentsource().get(i)));
			if (i < intentsources.getIntentsource().size() - 1) {
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	public static String toString(final Intentsource intentsource) {
		final StringBuilder sb = new StringBuilder();

		sb.append(toString(intentsource.getTarget()));
		sb.append("Reference:\n" + toString(intentsource.getReference()));

		return sb.toString();
	}

	public static String toString(final Intentsinks intentsinks) {
		final StringBuilder sb = new StringBuilder();

		for (int i = 0; i < intentsinks.getIntentsink().size(); i++) {
			sb.append("#" + (i + 1) + ":\n");
			sb.append(toString(intentsinks.getIntentsink().get(i)));
			if (i < intentsinks.getIntentsink().size() - 1) {
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	public static String toString(final Intentsink intentsink) {
		final StringBuilder sb = new StringBuilder();

		sb.append(toString(intentsink.getTarget()));
		sb.append("Reference:\n" + toString(intentsink.getReference()));

		return sb.toString();
	}

	public static String toString(final Target target) {
		return toString(target, false);
	}

	public static String toString(final Target target, boolean detailData) {
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
					sb.append("Data: {\n\t" + toString(data) + "}\n");
				} else {
					sb.append(
							"Data: is set" + (data.getType() != null ? " (Type: " + data.getType() + ")" : "") + "\n");
				}
			}
		}
		if (target.getReference() != null) {
			sb.append("Class: " + target.getReference().getClassname() + "\n");
		}

		return sb.toString();
	}

	public static String toString(final Data data) {
		final StringBuilder sb = new StringBuilder();

		if (data.getHost() != null && data.getPort() != null) {
			sb.append("Host+Port: " + data.getHost() + ":" + data.getPort() + "\n");
		} else if (data.getHost() != null) {
			sb.append("Host: " + data.getHost() + "\n");
		} else if (data.getPort() != null) {
			sb.append("Port: " + data.getPort() + "\n");
		}
		if (data.getPath() != null) {
			sb.append("Path: " + data.getPath() + "\n");
		}
		if (data.getScheme() != null) {
			sb.append("Scheme: " + data.getScheme() + "\n");
		}
		if (data.getSsp() != null) {
			sb.append("SSP: " + data.getSsp() + "\n");
		}
		if (data.getType() != null) {
			sb.append("Type: " + data.getType() + "\n");
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
		sb.append("Name: " + tool.getName() + " (" + tool.getVersion() + ")" + "\n");
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

	public static String replaceVariables(String str, OperatorTaskInfo taskinfo, List<File> tempAnswerFiles) {
		if ((str.contains("%ANSWERS%") || str.contains("%ANSWERSHASH%")) && tempAnswerFiles != null
				&& !tempAnswerFiles.isEmpty()) {
			final String tempAnswerFilesStr = answerFilesAsString(tempAnswerFiles);
			str = str.replaceAll("%ANSWERS%", tempAnswerFilesStr);
			str = str.replaceAll("%ANSWERSHASH%", HashHelper.sha256Hash(tempAnswerFilesStr));
		} else {
			str = str.replaceAll("%ANSWERS%", "NOT_AVAILABLE");
			str = str.replaceAll("%ANSWERSHASH%", "NOT_AVAILABLE");
		}
		str = str.replaceAll("%ANDROID_PLATFORMS%", ConfigHandler.getInstance().getConfig().getAndroidPlatforms());
		str = replaceVariables(str, taskinfo);
		return str;
	}

	public static String answerFilesAsString(List<File> answerFiles) {
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < answerFiles.size(); i++) {
			sb.append(answerFiles.get(i).getAbsolutePath().replaceAll("\\\\", "/")
					+ (i != answerFiles.size() - 1 ? ", " : ""));
		}
		return sb.toString();
	}

	public static String replaceVariables(String str, TaskInfo taskinfo) {
		if (!taskinfo.getTool().isExternal()) {
			str = str.replaceAll("%MEMORY%", Integer.toString(taskinfo.getTool().getExecute().getMemoryPerInstance()));
			str = str.replaceAll("%PID%", Integer.toString(taskinfo.getPID()));
		}
		return str;
	}

	public static String replaceVariables(final String str, final TaskInfo taskinfo, final App app) {
		final Config cfg = ConfigHandler.getInstance().getConfig();
		final File apkFile = new File(app.getFile());
		if (apkFile.exists()) {
			final ManifestInfo manifestInfo = ManifestHelper.getInstance().getManifest(apkFile);
			return Helper.replaceVariables(str, taskinfo, cfg, apkFile, manifestInfo);
		} else {
			return Helper.replaceVariables(str, taskinfo, cfg, app.getFile());
		}
	}

	public static String replaceVariables(final String str, final TaskInfo taskinfo, final QuestionPart question) {
		final Config cfg = ConfigHandler.getInstance().getConfig();
		if (question.getAllReferences().size() == 2) {
			return Helper.replaceVariables(str, taskinfo, cfg, question.getAllReferences().get(0).getApp().getFile()
					+ " " + question.getAllReferences().get(1).getApp().getFile());
		} else {
			final File apkFile = new File(question.getAllReferences().get(0).getApp().getFile());
			if (apkFile.exists()) {
				final ManifestInfo manifestInfo = ManifestHelper.getInstance().getManifest(apkFile);
				return Helper.replaceVariables(str, taskinfo, cfg, apkFile, manifestInfo);
			} else {
				return Helper.replaceVariables(str, taskinfo, cfg,
						question.getAllReferences().get(0).getApp().getFile());
			}
		}
	}

	private static String replaceVariables(String str, final TaskInfo taskinfo, final Config cfg, final File apkFile,
			final ManifestInfo manifestInfo) {
		str = str.replaceAll("%APP_APK_FILENAME%", apkFile.getName().substring(0, apkFile.getName().length() - 4));
		str = str.replaceAll("%APP_APK%", apkFile.getAbsolutePath().replaceAll("\\\\", "/"));
		str = str.replaceAll("%APP_NAME%", manifestInfo.getAppName());
		str = str.replaceAll("%APP_PACKAGE%", manifestInfo.getPkgName());
		str = str.replaceAll("%ANDROID_PLATFORMS%", cfg.getAndroidPlatforms());
		str = replaceVariables(str, taskinfo);

		return str;
	}

	private static String replaceVariables(String str, final TaskInfo taskinfo, final Config cfg, final String name) {
		final String editedName = getMultipleApkName(name);
		str = str.replaceAll("%APP_APK_FILENAME%", editedName);
		String files = "";
		for (final String oneFile : name.split(" ")) {
			final File apkFile = new File(oneFile);
			if (!files.equals("")) {
				files += " ";
			}
			files += apkFile.getAbsolutePath().replaceAll("\\\\", "/");
		}
		str = str.replaceAll("%APP_APK%", files);
		str = str.replaceAll("%APP_NAME%", editedName + "_name");
		str = str.replaceAll("%APP_PACKAGE%", editedName + "_pkg");
		str = str.replaceAll("%ANDROID_PLATFORMS%", cfg.getAndroidPlatforms());
		str = replaceVariables(str, taskinfo);

		return str;
	}

	public static String getMultipleApkName(String name) {
		final StringBuilder full = new StringBuilder("");
		for (String onePart : name.split(" ")) {
			onePart = onePart.replaceAll("\\\\", "/");
			if (onePart.contains("/")) {
				if (full.length() != 0) {
					full.append(" ");
				}
				full.append(Helper.cut(onePart, "/", Helper.OCCURENCE_LAST));
			}
		}
		return full.toString().replaceAll(",", "").replaceAll(" ", "_").replaceAll(".apk", "");
	}

	public static String replaceVariables(String str, ToolTaskInfo taskinfo, File resultFile) {
		str = replaceVariables(str, taskinfo, taskinfo.getQuestion());
		return str.replaceAll("%RESULT_FILE%", resultFile.getAbsolutePath().replaceAll("\\\\", "/"));
	}

	public static boolean replaceQuestionPart(final IQuestionNode question, final QuestionPart needle,
			final IQuestionNode replacement) {
		for (int i = 0; i < question.getChildren().size(); i++) {
			if (question.getChildren().get(i) instanceof Question) {
				if (replaceQuestionPart(question.getChildren().get(i), needle, replacement)) {
					return true;
				}
			} else {
				if (question.getChildren().get(i) == needle) {
					question.getChildren().add(i, replacement);
					question.getChildren().remove(needle);
					return true;
				}
			}
		}
		return false;
	}

	public static boolean replaceAllQuestionPart(final IQuestionNode question, final QuestionPart needle,
			final IQuestionNode replacement) {
		boolean goOn = true;
		while (goOn) {
			goOn = replaceQuestionPart(question, needle, replacement);
		}
		return true;
	}

	public static String getDate() {
		final DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy-HH_mm");
		final Date date = new Date();
		return dateFormat.format(date);
	}

	public static App createApp(final File file) {
		return createApp(file.toString());
	}

	public static App createApp(final String value) {
		final App app = new App();
		app.setFile(value);

		// Hashes
		final Hashes hashes = new Hashes();

		final File file = new File(value);

		final Hash hashMD5 = new Hash();
		hashMD5.setType(KeywordsAndConstants.HASH_TYPE_MD5);

		final Hash hashSHA1 = new Hash();
		hashSHA1.setType(KeywordsAndConstants.HASH_TYPE_SHA1);

		final Hash hashSHA256 = new Hash();
		hashSHA256.setType(KeywordsAndConstants.HASH_TYPE_SHA256);

		if (file.exists()) {
			hashMD5.setValue(HashHelper.md5Hash(file));
			hashSHA1.setValue(HashHelper.sha1Hash(file));
			hashSHA256.setValue(HashHelper.sha256Hash(file));
		} else {
			Log.msg("Could not find file for hash creation: " + value, Log.DEBUG);
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

	/**
	 * Unifies filenames
	 *
	 * @param file
	 *            - File with filename such as randomFile-0.txt
	 * @return randomFile-X.txt with X such that the file does not exists, yet.
	 */
	public static File makeUnique(File file) {
		String extension = "";
		if (file.getName().contains(".")) {
			extension = file.getName().substring(file.getName().lastIndexOf("."));
		}

		int i = 0;
		while (file.exists()) {
			i++;
			file = new File(file.getAbsolutePath().substring(0,
					file.getAbsolutePath().length() - (4 + String.valueOf(i).length())) + i + extension);
		}
		return file;
	}

	public static File findFileWithAsterisk(File file) {
		if (!file.getAbsolutePath().contains("*")) {
			return file;
		}

		final String[] needles = file.getName().split("\\*");

		file = new File(file.getAbsolutePath().replaceAll("\\*", "_"));

		boolean didNotExist = false;
		if (!file.exists()) {
			didNotExist = true;
			try {
				file.createNewFile();
			} catch (final IOException e) {
				Log.error("Analysis result could not be found or written: " + file.getAbsolutePath());
			}
		}

		for (final File candidate : file.getParentFile().listFiles()) {
			if (didNotExist && candidate.equals(file)) {
				continue;
			}
			boolean valid = true;
			for (final String needle : needles) {
				if (!candidate.getName().contains(needle)) {
					valid = false;
				}
			}
			if (valid) {
				if (didNotExist) {
					file.delete();
				}
				return candidate;
			}
		}
		if (didNotExist) {
			file.delete();
		}
		return file;
	}

	public static int getPid(Process p) {
		Field f;
		if (Platform.isWindows()) {
			try {
				f = p.getClass().getDeclaredField("handle");
				f.setAccessible(true);
				final WinNT.HANDLE handle = new WinNT.HANDLE();
				handle.setPointer(Pointer.createConstant(f.getLong(p)));
				final int pid = Kernel32.INSTANCE.GetProcessId(handle);
				return pid;
			} catch (final Exception ex) {
				Log.error("Could not identify process ID on this Windows operating system.");
			}
		} else if (Platform.isLinux()) {
			try {
				f = p.getClass().getDeclaredField("pid");
				f.setAccessible(true);
				final int pid = (Integer) f.get(p);
				return pid;
			} catch (final Exception ex) {
				Log.error("Could not identify process ID on this Linux operating system.");
			}
		} else {
			Log.error("Could not identify operating system.");
		}
		return -1;
	}

	public static Reference getFrom(Flow flow) {
		return getFrom(flow.getReference());
	}

	public static Reference getFrom(List<Reference> references) {
		for (final Reference ref : references) {
			if (ref.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_FROM)) {
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
			if (ref.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_TO)) {
				return ref;
			}
		}
		return null;
	}

	public static Answer removeRedundant(final Answer answer, EqualsOptions options) {
		// Permissions
		if (answer.getPermissions() != null) {
			for (int i = 0; i < answer.getPermissions().getPermission().size() - 1; i++) {
				final Permission obj1 = answer.getPermissions().getPermission().get(i);
				for (int j = i + 1; j < answer.getPermissions().getPermission().size(); j++) {
					final Permission obj2 = answer.getPermissions().getPermission().get(j);
					if (EqualsHelper.equals(obj1, obj2, options)) {
						answer.getPermissions().getPermission().remove(j);
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
					if (EqualsHelper.equals(obj1, obj2, options)) {
						answer.getIntents().getIntent().remove(j);
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
					if (EqualsHelper.equals(obj1, obj2, options)) {
						answer.getIntentfilters().getIntentfilter().remove(j);
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
					if (EqualsHelper.equals(obj1, obj2, options)) {
						answer.getIntentsinks().getIntentsink().remove(j);
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
					if (EqualsHelper.equals(obj1, obj2, options)) {
						answer.getIntentsources().getIntentsource().remove(j);
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
					if (EqualsHelper.equals(obj1, obj2, options)) {
						answer.getFlows().getFlow().remove(j);
					}
				}
			}
		}

		return answer;
	}

	public static int getCardinality(Tool tool, String operator) {
		if (tool == null || operator == null) {
			return 2;
		}
		if (tool instanceof DefaultOperator) {
			if (operator.equals(KeywordsAndConstants.getFilterOperator())
					|| operator.equals(KeywordsAndConstants.OPERATOR_FILTER_ORIGINAL)) {
				return 1;
			} else {
				return 2;
			}
		} else {
			for (final String splitStr : tool.getQuestions().replaceAll(" ", "").split(",")) {
				if (splitStr.equals(operator)) {
					return 0;
				} else if (splitStr.contains("(") && splitStr.substring(0, splitStr.indexOf("(")).equals(operator)) {
					final String cardinality = splitStr.substring(splitStr.indexOf("(") + 1, splitStr.indexOf(")"));
					if (cardinality.equals("*")) {
						return 0;
					} else {
						return Integer.valueOf(cardinality).intValue();
					}
				}
			}
			return -1;
		}
	}

	public static void waitForResult(String msg, File result) throws FileNotFoundException {
		for (int i = 0; i <= 10; i++) {
			if (result.exists()) {
				Log.msg("Result available: " + result.getAbsolutePath(), Log.DEBUG_DETAILED);
				break;
			} else {
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					Log.warning("Interrupted while waiting for result. Trying to continue.");
				}
			}
		}
		if (!result.exists()) {
			throw new FileNotFoundException(msg + "\n(" + result.getAbsolutePath() + ")");
		}
	}

	public static String getExecuteCommand(ToolTaskInfo taskinfo) {
		return (taskinfo.getTool().isExternal()
				? Helper.replaceVariables(taskinfo.getTool().getExecute().getUrl(), taskinfo, taskinfo.getQuestion())
				: Helper.replaceVariables(taskinfo.getTool().getExecute().getRun(), taskinfo, taskinfo.getQuestion()));
	}

	public static String getExecuteCommand(PreprocessorTaskInfo taskinfo) {
		return (taskinfo.getTool().isExternal()
				? Helper.replaceVariables(taskinfo.getTool().getExecute().getUrl(), taskinfo, taskinfo.getApp())
				: Helper.replaceVariables(taskinfo.getTool().getExecute().getRun(), taskinfo, taskinfo.getApp()));
	}

	public static String getExecuteCommand(OperatorTaskInfo taskinfo, List<File> tempAnswerFiles) {
		return (taskinfo.getTool().isExternal()
				? Helper.replaceVariables(taskinfo.getTool().getExecute().getUrl(), taskinfo, tempAnswerFiles)
				: Helper.replaceVariables(taskinfo.getTool().getExecute().getRun(), taskinfo, tempAnswerFiles));
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
		for (final File file : files) {
			if (file.lastModified() > lastMod) {
				choice = file;
				lastMod = file.lastModified();
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

	public static Collection<Reference> getAllReferences(Answer answer) {
		return getAllReferences(answer, false);
	}

	public static Collection<Reference> getAllReferences(Answer answer, boolean doubleEntriesAllowed) {
		final Collection<Reference> references = new ArrayList<>();

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
}
