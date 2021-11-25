package de.foellix.aql.system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.config.Priority;
import de.foellix.aql.config.Tool;
import de.foellix.aql.converter.DefaultConverterRegistry;
import de.foellix.aql.datastructure.query.DefaultQuestion;
import de.foellix.aql.datastructure.query.IStringOrQuestion;
import de.foellix.aql.datastructure.query.OperatorQuestion;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.datastructure.query.StringOrQuestionPair;
import de.foellix.aql.helper.EqualsHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.helper.KeywordsAndConstantsHelper;
import de.foellix.aql.system.defaulttools.DefaultTool;
import de.foellix.aql.system.defaulttools.analysistools.DefaultFeatureFinder;
import de.foellix.aql.system.defaulttools.analysistools.DefaultIntentInformationFinder;
import de.foellix.aql.system.defaulttools.analysistools.DefaultPermissionFinder;
import de.foellix.aql.system.defaulttools.analysistools.DefaultSourceSinkFinder;
import de.foellix.aql.system.defaulttools.operators.DefaultOperator;

public class ToolSelector {
	public static final String MODE_INTRA_FLOWS = "IntraAppFlows";
	public static final String MODE_INTER_FLOWS = "InterAppFlows";
	public static final String MODE_PERMISSIONS = KeywordsAndConstantsHelper.SOI_PERMISSIONS;
	public static final String MODE_INTENTS = KeywordsAndConstantsHelper.SOI_INTENTS;
	public static final String MODE_INTENTFILTER = KeywordsAndConstantsHelper.SOI_INTENTFILTERS;
	public static final String MODE_INTENTSOURCES = KeywordsAndConstantsHelper.SOI_INTENTSOURCES;
	public static final String MODE_INTENTSINKS = KeywordsAndConstantsHelper.SOI_INTENTSINKS;
	public static final String MODE_SLICE = KeywordsAndConstantsHelper.SOI_SLICE;
	public static final String MODE_SOURCES = KeywordsAndConstantsHelper.SOI_SOURCES;
	public static final String MODE_SINKS = KeywordsAndConstantsHelper.SOI_SINKS;
	public static final String MODE_ARGUMENTS = KeywordsAndConstantsHelper.SOI_ARGUMENTS;

	public static Tool selectAnalysisTool(final DefaultQuestion question) {
		return selectAnalysisTool(question, null);
	}

	public static Tool selectAnalysisTool(final DefaultQuestion question, Tool[] exceptions) {
		final List<Tool> choices = new ArrayList<>();

		final String mode = soiToMode(question);
		if (ConfigHandler.getInstance().getConfig() != null
				&& ConfigHandler.getInstance().getConfig().getTools() != null
				&& !ConfigHandler.getInstance().getConfig().getTools().getTool().isEmpty()) {
			for (final Tool tool : ConfigHandler.getInstance().getConfig().getTools().getTool()) {
				if (modeAvailable(tool, mode)) {
					choices.add(tool);
				}
			}
		}

		// Consider default tools
		final Tool defaultTool = selectDefaultAnalysisTool(question);
		if (defaultTool != null) {
			choices.add(defaultTool);
		}

		return selectTool(choices, -1, exceptions, question);
	}

	private static Tool selectDefaultAnalysisTool(DefaultQuestion question) {
		if (question.getSubjectOfInterest() == Question.QUESTION_TYPE_INTENTS
				|| question.getSubjectOfInterest() == Question.QUESTION_TYPE_INTENTFILTER
				|| question.getSubjectOfInterest() == Question.QUESTION_TYPE_INTENTSINKS
				|| question.getSubjectOfInterest() == Question.QUESTION_TYPE_INTENTSOURCES) {
			return new DefaultIntentInformationFinder();
		} else if (question.getSubjectOfInterest() == Question.QUESTION_TYPE_SOURCES
				|| question.getSubjectOfInterest() == Question.QUESTION_TYPE_SINKS) {
			return new DefaultSourceSinkFinder();
		} else if (question.getSubjectOfInterest() == Question.QUESTION_TYPE_PERMISSIONS) {
			return new DefaultPermissionFinder();
		} else if (question.getSubjectOfInterest() == Question.QUESTION_TYPE_ARGUMENTS) {
			return new DefaultFeatureFinder();
		} else {
			return null;
		}
	}

	private static boolean modeAvailable(Tool tool, String mode) {
		return Arrays.asList(tool.getQuestions().replace(" ", "").split(",")).contains(mode);
	}

	public static Tool selectConverter(final Tool analysisTool) {
		return selectConverter(analysisTool, null);
	}

	public static Tool selectConverter(final Tool analysisTool, Tool[] exceptions) {
		final List<Tool> choices = new ArrayList<>();

		if (ConfigHandler.getInstance().getConfig() != null
				&& ConfigHandler.getInstance().getConfig().getConverters() != null
				&& !ConfigHandler.getInstance().getConfig().getConverters().getTool().isEmpty()) {
			// Search for converter w.r.t. the tool's version
			String needle = analysisTool.getName().toLowerCase() + "(" + analysisTool.getVersion().toLowerCase() + ")";
			for (final Tool tool : ConfigHandler.getInstance().getConfig().getConverters().getTool()) {
				final List<String> targetTools = Arrays
						.asList(tool.getQuestions().toLowerCase().replace(" ", "").split(","));
				if (targetTools.contains(needle)) {
					choices.add(tool);
				}
			}

			// Search ignoring the tool's version
			if (choices.isEmpty()) {
				needle = analysisTool.getName().toLowerCase();
				for (final Tool tool : ConfigHandler.getInstance().getConfig().getConverters().getTool()) {
					final List<String> targetTools = Arrays
							.asList(tool.getQuestions().toLowerCase().replace(" ", "").split(","));
					if (targetTools.contains(needle)) {
						choices.add(tool);
					}
				}
			}
		}

		// If no converter was selected try to find a default one
		if (choices.isEmpty()) {
			return DefaultConverterRegistry.getInstance().getConverter(analysisTool);
		}

		return selectTool(choices, -1, exceptions);
	}

	public static Tool selectPreprocessor(String keyword) {
		return selectPreprocessor(keyword, null);
	}

	public static Tool selectPreprocessor(String keyword, Tool[] exceptions) {
		final List<Tool> choices = new ArrayList<>();
		if (ConfigHandler.getInstance().getConfig().getPreprocessors() != null
				&& !ConfigHandler.getInstance().getConfig().getPreprocessors().getTool().isEmpty()) {
			for (final Tool preprocessor : ConfigHandler.getInstance().getConfig().getPreprocessors().getTool()) {
				if (preprocessor.getQuestions().equals(keyword) || keyword.equals(preprocessor.getName())
						|| keyword.equals(preprocessor.getName() + "-" + preprocessor.getVersion())) {
					choices.add(preprocessor);
				}
			}
		}
		return selectTool(choices, -1, exceptions);
	}

	public static Tool selectOperator(OperatorQuestion question) {
		return selectOperator(question, null);
	}

	public static Tool selectOperator(OperatorQuestion question, Tool[] exceptions) {
		final List<Tool> choices = new ArrayList<>();
		if (ConfigHandler.getInstance().getConfig().getOperators() != null
				&& !ConfigHandler.getInstance().getConfig().getOperators().getTool().isEmpty()) {
			for (final Tool operator : ConfigHandler.getInstance().getConfig().getOperators().getTool()) {
				for (final String splitStr : operator.getQuestions().replace(" ", "").split(",")) {
					if (splitStr.equals(question.getOperator()) || (splitStr.contains("(")
							&& splitStr.substring(0, splitStr.indexOf("(")).equals(question.getOperator()))) {
						choices.add(operator);
					}
				}
				if (!choices.contains(operator)) {
					if (question.getOperator().equals(operator.getName())
							|| question.getOperator().equals(operator.getName() + "-" + operator.getVersion())) {
						choices.add(operator);
					}
				}
			}
		}

		// Consider default operators
		final Tool defaultOperator = selectDefaultOperator(question);
		if (defaultOperator != null) {
			choices.add(defaultOperator);
		}

		return selectTool(choices, -1, exceptions);
	}

	private static Tool selectDefaultOperator(OperatorQuestion question) {
		return DefaultOperator.getOperator(question.getOperator());
	}

	// Best choice finder
	private static Tool selectTool(final List<Tool> choices, final int minPriority, Tool[] exceptions) {
		return selectTool(choices, minPriority, exceptions, null);
	}

	private static Tool selectTool(List<Tool> choices, final int minPriority, Tool[] exceptions,
			DefaultQuestion question) {
		if (exceptions != null) {
			choices = removeExceptions(choices, exceptions, question);
		}
		if (!choices.isEmpty()) {
			Tool selectedChoice = null;
			int selectedPriority = -1;
			for (final Tool choice : choices) {
				final int newPriority = (question != null ? getPriority(choice, question) : getPriority(choice));
				if ((newPriority > minPriority || choice instanceof DefaultTool)
						&& (selectedChoice == null || selectedPriority <= newPriority)) {
					if (selectedChoice != null && selectedPriority == newPriority) {
						selectedChoice = breakTie(selectedChoice, choice, question);
					} else {
						selectedChoice = choice;
						selectedPriority = newPriority;
					}
				}
			}
			return selectedChoice;
		} else {
			return null;
		}
	}

	private static Tool breakTie(Tool tool1, Tool tool2, DefaultQuestion question) {
		if (tool1.isExternal() && !tool2.isExternal()) {
			// Prefer local
			return tool2;
		} else if (!tool1.isExternal() && !tool2.isExternal()) {
			int tool1Priority = 0;
			int tool2Priority = 0;
			if (question.getWiths() != null && !question.getWiths().isEmpty()) {
				// Prefer tool that seems to resolve more variables
				final Set<String> variablesUsed = new HashSet<>();
				for (final StringOrQuestionPair pair : question.getWiths()) {
					if (pair.getKey().isComplete(true)) {
						variablesUsed.add("%" + pair.getKey().toStringInAnswer(false) + "%");
					}
				}
				for (final String variableUsed : variablesUsed) {
					if (tool1.getExecute().getRun().contains(variableUsed)) {
						tool1Priority++;
					}
					if (tool2.getExecute().getRun().contains(variableUsed)) {
						tool2Priority++;
					}
				}
				if (tool2Priority > tool1Priority) {
					return tool2;
				}
			}
			if (tool1Priority == tool2Priority) {
				// Prefer tool that requires less custom variables
				String runTool1 = tool1.getExecute().getRun();
				String runTool2 = tool2.getExecute().getRun();
				int tool1Priority2 = 0;
				int tool2Priority2 = 0;
				for (final String var : Helper.getAllDefaultVariableNames()) {
					tool1Priority2 += Helper.countStringOccurences(runTool1, var);
					tool2Priority2 += Helper.countStringOccurences(runTool2, var);
					runTool1 = runTool1.replace(var, "");
					runTool2 = runTool2.replace(var, "");
				}
				tool1Priority = Helper.countStringOccurences(runTool1, "%");
				tool2Priority = Helper.countStringOccurences(runTool2, "%");
				if (tool2Priority < tool1Priority) {
					// "<" in this case - lower is better
					return tool2;
				} else if (tool2Priority == tool1Priority && tool2Priority2 > tool1Priority2) {
					// On another tie: take tool that uses more default variables
					return tool2;
				}
			}
		}
		// Any other case: Prefer first
		return tool1;
	}

	private static List<Tool> removeExceptions(final List<Tool> choices, Tool[] exceptions, DefaultQuestion question) {
		final Collection<Tool> toRemove = new ArrayList<>(Arrays.asList(exceptions));
		choices.removeAll(toRemove);
		if (!choices.isEmpty()) {
			int lowestPriority = Integer.MAX_VALUE;
			for (final Tool tool : exceptions) {
				final int toolPriority = getPriority(tool, question);
				if (toolPriority < lowestPriority) {
					lowestPriority = toolPriority;
				}
			}
			toRemove.clear();
			for (final Tool tool : choices) {
				if (getPriority(tool, question) > lowestPriority) {
					toRemove.add(tool);
				}
			}
			choices.removeAll(toRemove);
		}
		return choices;
	}

	// Priority handler
	public static int getPriority(Tool tool, DefaultQuestion question) {
		if (question != null) {
			if (question.getUses() != null && !question.getUses().isEmpty()) {
				if (Helper.contains(question.getUses(), tool.getName())
						|| Helper.contains(question.getUses(), tool.getName() + "-" + tool.getVersion())
						|| (tool instanceof DefaultTool
								&& Helper.contains(question.getUses(), DefaultTool.DEFAULT_TOOL_USES))) {
					return ConfigHandler.getInstance().getMaxConfiguredPriority()
							+ Math.max(0, getPriorityByFeatures(tool, question)) + 1;
				}
			} else if (question.getFeatures() != null && !question.getFeatures().isEmpty()) {
				return getPriorityByFeatures(tool, question);
			}
		}
		return getPriority(tool);
	}

	public static int getPriorityByFeatures(Tool tool, DefaultQuestion question) {
		int priorityValue = getPriority(tool);
		for (final Priority priority : tool.getPriority()) {
			if (priority.getFeature() != null && question.getFeatures() != null) {
				final String feature = priority.getFeature();
				boolean found = false;
				for (final IStringOrQuestion candidate : question.getFeatures()) {
					if (candidate.toStringInAnswer(false).equals(feature)) {
						found = true;
						break;
					}
				}
				if (found) {
					priorityValue += priority.getValue();
				}
			}
		}
		return priorityValue;
	}

	public static int getPriority(Tool tool) {
		for (final Priority priority : tool.getPriority()) {
			if (priority.getFeature() == null) {
				return priority.getValue();
			}
		}
		return 0;
	}

	// Helper methods
	private static String soiToMode(DefaultQuestion question) {
		switch (question.getSubjectOfInterest()) {
			case Question.QUESTION_TYPE_FLOWS:
				if (detectInterApp(question)) {
					return MODE_INTER_FLOWS;
				} else {
					return MODE_INTRA_FLOWS;
				}
			case Question.QUESTION_TYPE_PERMISSIONS:
				return MODE_PERMISSIONS;
			case Question.QUESTION_TYPE_INTENTFILTER:
				return MODE_INTENTFILTER;
			case Question.QUESTION_TYPE_INTENTS:
				return MODE_INTENTS;
			case Question.QUESTION_TYPE_INTENTSINKS:
				return MODE_INTENTSINKS;
			case Question.QUESTION_TYPE_INTENTSOURCES:
				return MODE_INTENTSOURCES;
			case Question.QUESTION_TYPE_SLICE:
				return MODE_SLICE;
			case Question.QUESTION_TYPE_SOURCES:
				return MODE_SOURCES;
			case Question.QUESTION_TYPE_SINKS:
				return MODE_SINKS;
			case Question.QUESTION_TYPE_ARGUMENTS:
				return MODE_ARGUMENTS;
			default:
				return null;
		}
	}

	private static boolean detectInterApp(final Question question) {
		if (question instanceof DefaultQuestion) {
			final DefaultQuestion castedQuestion = (DefaultQuestion) question;
			if (castedQuestion.getFrom() != null && castedQuestion.getTo() != null) {
				if (castedQuestion.getFrom().toReference() != null && castedQuestion.getTo().toReference() != null) {
					if (castedQuestion.getFrom().toReference().getApp() != null
							&& castedQuestion.getTo().toReference().getApp() != null) {
						if (!EqualsHelper.equals(castedQuestion.getFrom().toReference().getApp(),
								castedQuestion.getTo().toReference().getApp())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
}
