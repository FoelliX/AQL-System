package de.foellix.aql.system;

import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.config.ConfigHandler;
import de.foellix.aql.config.Priority;
import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.Question;
import de.foellix.aql.datastructure.QuestionPart;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.EqualsHelper;

public class ToolSelector {
	private static ToolSelector instance = new ToolSelector();

	private ToolSelector() {
	}

	public static ToolSelector getInstance() {
		return instance;
	}

	public boolean detectInterApp(final QuestionPart question) {
		boolean interApp = false;
		Reference firstReference = null;
		for (final Reference reference : question.getReferences()) {
			if (firstReference == null) {
				firstReference = reference;
			} else if (!EqualsHelper.equals(firstReference.getApp().getHashes(), reference.getApp().getHashes())) {
				interApp = true;
				break;
			}
		}
		return interApp;
	}

	public Tool selectTool(final QuestionPart question) {
		return selectTool(question, 0);
	}

	public Tool selectTool(final QuestionPart question, final int priority) {
		final List<Tool> choices = new ArrayList<>();

		if (!question.getReferences().isEmpty()) {
			for (final Tool tool : ConfigHandler.getInstance().getConfig().getTools().getTool()) {
				if (question.getMode() == KeywordsAndConstants.QUESTION_TYPE_FLOWS) {
					// Detect inter-app flow
					final boolean interApp = detectInterApp(question);

					// Select tool
					if (interApp) {
						if (tool.getQuestions().contains(KeywordsAndConstants.MODE_INTER_FLOWS)) {
							choices.add(tool);
						}
					} else {
						if (tool.getQuestions().contains(KeywordsAndConstants.MODE_INTRA_FLOWS)) {
							choices.add(tool);
						}
					}
				} else if (question.getMode() == KeywordsAndConstants.QUESTION_TYPE_PERMISSIONS) {
					if (tool.getQuestions().contains(KeywordsAndConstants.MODE_PERMISSIONS)) {
						choices.add(tool);
					}
				} else if (question.getMode() == KeywordsAndConstants.QUESTION_TYPE_INTENTFILTER) {
					if (tool.getQuestions().contains(KeywordsAndConstants.MODE_INTENTFILTER)) {
						choices.add(tool);
					}
				} else if (question.getMode() == KeywordsAndConstants.QUESTION_TYPE_INTENTS) {
					if (tool.getQuestions().contains(KeywordsAndConstants.MODE_INTENTS)) {
						choices.add(tool);
					}
				} else if (question.getMode() == KeywordsAndConstants.QUESTION_TYPE_INTENTSINKS) {
					if (tool.getQuestions().contains(KeywordsAndConstants.MODE_INTENTSINKS)) {
						choices.add(tool);
					}
				} else if (question.getMode() == KeywordsAndConstants.QUESTION_TYPE_INTENTSOURCES) {
					if (tool.getQuestions().contains(KeywordsAndConstants.MODE_INTENTSOURCES)) {
						choices.add(tool);
					}
				}
			}
		}

		return selectChoice(choices, priority, question);
	}

	public Tool selectPreprocessor(final QuestionPart question, final String keyword) {
		return selectPreprocessor(question, keyword, 0);
	}

	public Tool selectPreprocessor(final QuestionPart question, String keyword, final int priority) {
		keyword = keyword.substring(1, keyword.length() - 1);

		final List<Tool> choices = new ArrayList<>();
		if (ConfigHandler.getInstance().getConfig().getPreprocessors() != null) {
			for (final Tool preprocessor : ConfigHandler.getInstance().getConfig().getPreprocessors().getTool()) {
				if (preprocessor.getQuestions().equals(keyword)) {
					choices.add(preprocessor);
				}
			}
		}

		return selectChoice(choices, priority, question);
	}

	public Tool selectOperator(Question question) {
		return selectOperator(question, 0);
	}

	public Tool selectOperator(Question question, final int priority) {
		final List<Tool> choices = new ArrayList<>();
		if (ConfigHandler.getInstance().getConfig().getOperators() != null) {
			for (final Tool operator : ConfigHandler.getInstance().getConfig().getOperators().getTool()) {
				for (final String splitStr : operator.getQuestions().replaceAll(" ", "").split(",")) {
					if (splitStr.equals(question.getOperator()) || (splitStr.contains("(")
							&& splitStr.substring(0, splitStr.indexOf("(")).equals(question.getOperator()))) {
						choices.add(operator);
					}
				}
			}
		}

		return selectChoice(choices, priority, null);
	}

	private Tool selectChoice(final List<Tool> choices, final int priority, QuestionPart question) {
		if (choices.size() > 0) {
			Tool selectedChoice = null;
			int selectedPriority = -1;
			for (final Tool choice : choices) {
				final int newPriority = getPriority(choice, question.getFeatures());
				if ((newPriority > priority) && (selectedChoice == null || selectedPriority < newPriority)) {
					selectedChoice = choice;
					selectedPriority = newPriority;
				}
			}
			return selectedChoice;
		} else {
			return null;
		}
	}

	public int getPriority(Tool tool, List<String> features) {
		if (features != null && !features.isEmpty()) {
			for (final Priority priority : tool.getPriority()) {
				if (features.contains(priority.getFeature())) {
					return priority.getValue();
				}
			}
		}
		return getPriority(tool);
	}

	public int getPriority(Tool tool) {
		for (final Priority priority : tool.getPriority()) {
			if (priority.getFeature() == null) {
				return priority.getValue();
			}
		}
		return 0;
	}
}
