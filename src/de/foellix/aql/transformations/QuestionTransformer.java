package de.foellix.aql.transformations;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.handler.QueryHandler;
import de.foellix.aql.datastructure.query.DefaultQuestion;
import de.foellix.aql.datastructure.query.IStringOrQuestion;
import de.foellix.aql.datastructure.query.Query;
import de.foellix.aql.datastructure.query.Question;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.ToolTaskInfo;

public class QuestionTransformer {
	private static final String REPLACE_QUERY = "%QUERY%";
	private static final String REPLACE_FEATURE = "%FEATURE_#%";
	private static final String REPLACE_FEATURES = "%FEATURES%";

	private Question question;
	private Rule rule;
	private List<String> features;
	private Map<Rule, String> ruleQueryMap;

	public QuestionTransformer(Question question) {
		this.ruleQueryMap = new HashMap<>();
		this.question = question;
		this.features = null;
		if (question instanceof DefaultQuestion) {
			final List<IStringOrQuestion> features = ((DefaultQuestion) question).getFeatures();
			if (features != null) {
				this.features = new LinkedList<>();
				for (final IStringOrQuestion feature : features) {
					if (feature.isComplete(false)) {
						final String featuresStr = feature.toStringInAnswer(false);
						if (featuresStr != null) {
							if (featuresStr.contains(",")) {
								for (final String featureStr : featuresStr.replace(" ", "").split(",")) {
									this.features.add(featureStr);
								}
							} else {
								this.features.add(featuresStr);
							}
						}
					}
				}
			}
		}
		this.rule = getTransformationRule();

		// Sanitize map
		final String keep = this.ruleQueryMap.get(this.rule);
		this.ruleQueryMap.clear();
		this.ruleQueryMap.put(this.rule, keep);
	}

	/**
	 * Outputs whether the transformer is applicable or not
	 *
	 * @return true if applicable
	 */
	public boolean isApplicable() {
		return this.rule != null;
	}

	/**
	 * Transforms the question by applying the selected rule
	 *
	 * @return The transformed question
	 */
	public Question transform() {
		if (isApplicable()) {
			final Question transformedQuestion = applyRule();
			transformedQuestion.setParent(this.question.getParent());
			return transformedQuestion;
		}
		return this.question;
	}

	/**
	 * Applies the selected rule on the question
	 *
	 * @return The transformed question
	 */
	private Question applyRule() {
		final String query = getQueryString(this.rule);
		if (query != null) {
			final Query tempQuery = QueryHandler.parseQuery(query, true);
			if (tempQuery != null) {
				if (this.rule != null) {
					if (this.rule.getName() != null) {
						Log.msg("Transformation-rule applied: " + this.rule.getName(), Log.DEBUG_DETAILED);
					} else {
						Log.msg("Transformation-rule with no name applied.", Log.DEBUG_DETAILED);
					}
				}
				return tempQuery.getQuestions().iterator().next();
			} else {
				Log.msg("Rule (" + (this.rule.getName() != null ? this.rule.getName() : "no name given")
						+ ") could not be applied on:\n" + this.question + "\nsince it let to an invalid query:\n"
						+ Helper.autoformat(query).replace("\\\\", "\\"), Log.DEBUG_DETAILED);
			}
		}
		return this.question;
	}

	/**
	 * @param rule
	 * @param question
	 * @return returns true if rule is applicable otherwise returns false.
	 */
	private boolean isApplicable(Rule rule) {
		if (isSimpleRule(rule)) {
			return getRulePriority(rule) > 0 && isApplicableSimple(rule);
		} else {
			return getRulePriority(rule) > 0 && isApplicableInOut(rule);
		}
	}

	private String getQueryString(Rule rule) {
		String query = this.ruleQueryMap.get(rule);
		if (query == null) {
			if (isSimpleRule(rule)) {
				query = getQueryStringSimple(rule);
			} else {
				query = getQueryStringInOut(rule);
			}
			this.ruleQueryMap.put(rule, query);
		}
		return query;
	}

	private String cleanUpQuery(String query) {
		return Helper.replaceAllWhiteSpaceChars(query, true).replace(",,", ",").replace(", ,", ",");
	}

	private boolean isSimpleRule(Rule rule) {
		return rule.getQuery() != null;
	}

	/**
	 * Selects the applicable rule with highest priority for the given question query. Returns null if no rule is applicable.
	 *
	 * @return The rule to apply or null if no rule is applicable
	 */
	private Rule getTransformationRule() {
		Rule ruleToApply = null;
		int highestPriority = 0;
		for (final Rule rule : RulesHandler.getInstance().getRules()) {
			if (isApplicable(rule)) {
				final int prio = getRulePriority(rule);
				if (prio > highestPriority) {
					ruleToApply = rule;
					highestPriority = prio;
				}
			}
		}
		return ruleToApply;
	}

	private int getRulePriority(Rule rule) {
		int value = 0;
		for (final Priority prio : rule.getPriority()) {
			if (prio.getFeature() == null && prio.getValue() != null) {
				value += prio.getValue().intValue();
			}
			if (this.features != null) {
				if (prio.getFeature() != null && prio.getValue() != null) {
					if (this.features.contains(prio.getFeature())) {
						value += prio.getValue().intValue();
					}
				}
			}
		}
		return value;
	}

	/*
	 * In-Out rules
	 */
	private boolean isApplicableInOut(Rule rule) {
		return getQueryString(rule) != null;
	}

	private String getQueryStringInOut(Rule rule) {
		final String query = Helper.replaceAllWhiteSpaceChars(this.question.toString().replace("->", " -> "), true);
		final String ruleInputQuery = Helper.replaceAllWhiteSpaceChars(rule.getInputQuery().replace("->", " -> "),
				true);
		final String ruleOutputQuery = Helper.replaceAllWhiteSpaceChars(rule.getOutputQuery().replace("->", " -> "),
				true);

		final List<String> placeHolderList = new LinkedList<>();
		Pattern pattern = Pattern.compile("(\\*|%)([A-Za-z0-9_]+)(\\*|%)");
		Matcher matcher = pattern.matcher(ruleInputQuery);
		while (matcher.find()) {
			placeHolderList.add(matcher.group());
		}

		final List<String> pieces = new LinkedList<>();
		String temp = new String(ruleInputQuery);
		for (final String pc : placeHolderList) {
			final int i = temp.indexOf(pc);
			pieces.add(temp.substring(0, i));
			temp = temp.substring(i + pc.length());
		}
		pieces.add(temp);

		String constructedPattern = "";
		for (int i = 0; i < placeHolderList.size(); i++) {
			constructedPattern += preparePiece(pieces.get(i)) + "(.*)";
		}
		constructedPattern += preparePiece(pieces.get(pieces.size() - 1));
		constructedPattern = constructedPattern.replace(" (.*) ", "(.*) ");

		pattern = Pattern.compile(constructedPattern);
		matcher = pattern.matcher(query);
		final List<String> replacements = new LinkedList<>();
		if (matcher.find()) {
			for (int i = 1; i <= matcher.groupCount(); i++) {
				replacements.add(matcher.group(i));
			}
		}

		if (placeHolderList.size() == replacements.size()) {
			String finalQuery = new String(ruleOutputQuery);
			for (int i = 0; i < placeHolderList.size(); i++) {
				final String pc = placeHolderList.get(i);
				final String replacement = replacements.get(i);
				finalQuery = finalQuery.replace(pc, replacement);
			}
			finalQuery = cleanUpQuery(finalQuery);
			if (QueryHandler.parseQuery(finalQuery, true) != null) {
				return finalQuery;
			}
		}

		return null;
	}

	private String preparePiece(String piece) {
		return piece.replace("(", "\\(").replace(")", "\\)").replace("?", "\\?").replace(".", "\\.").replace("$",
				"\\$");
	}

	/*
	 * Simple rules
	 */
	private boolean isApplicableSimple(Rule rule) {
		if (getQueryString(rule) == null || getQueryString(rule).contains("%QUERY%")
				|| getQueryString(rule).contains("%FEATURE_") || getQueryString(rule).contains("%FEATURES%")
				|| getQueryString(rule).contains(ToolTaskInfo.APP_APK_IN)
				|| getQueryString(rule).contains(ToolTaskInfo.CLASS_IN)
				|| getQueryString(rule).contains(ToolTaskInfo.METHOD_IN)
				|| getQueryString(rule).contains(ToolTaskInfo.STATEMENT_IN)
				|| getQueryString(rule).contains(ToolTaskInfo.APP_APK_FROM)
				|| getQueryString(rule).contains(ToolTaskInfo.CLASS_FROM)
				|| getQueryString(rule).contains(ToolTaskInfo.METHOD_FROM)
				|| getQueryString(rule).contains(ToolTaskInfo.STATEMENT_FROM)
				|| getQueryString(rule).contains(ToolTaskInfo.APP_APK_TO)
				|| getQueryString(rule).contains(ToolTaskInfo.CLASS_TO)
				|| getQueryString(rule).contains(ToolTaskInfo.METHOD_TO)
				|| getQueryString(rule).contains(ToolTaskInfo.STATEMENT_TO)) {
			return false;
		} else {
			return true;
		}
	}

	private String getQueryStringSimple(Rule rule) {
		if (this.question instanceof DefaultQuestion) {
			final DefaultQuestion castedQuestion = (DefaultQuestion) this.question;
			final Map<String, String> customVariables = new HashMap<>();

			// Replace query
			String replacement = castedQuestion.toString();
			if (replacement.endsWith("?")) {
				replacement = replacement.substring(0, replacement.lastIndexOf("?"));
				while (replacement.endsWith(" ")) {
					replacement = replacement.substring(0, replacement.length() - 1);
				}
			}
			replacement = replacement.replace("\\", "\\\\");
			customVariables.put(REPLACE_QUERY, replacement);

			// Replace in
			if (castedQuestion.getIn() != null && castedQuestion.getIn().isComplete(true)) {
				final Reference ref = castedQuestion.getIn().toReference();
				if (ref.getApp() != null && ref.getApp().getFile() != null && !ref.getApp().getFile().isEmpty()) {
					final File apk = new File(ref.getApp().getFile());
					customVariables.put(ToolTaskInfo.APP_APK_IN, apk.getAbsolutePath());
				}
				if (ref.getClassname() != null && !ref.getClassname().isEmpty()) {
					customVariables.put(ToolTaskInfo.CLASS_IN, ref.getClassname());
				}
				if (ref.getMethod() != null && !ref.getMethod().isEmpty()) {
					customVariables.put(ToolTaskInfo.METHOD_IN, ref.getMethod());
				}
				if (ref.getStatement() != null) {
					if (ref.getStatement().getStatementfull() != null
							&& !ref.getStatement().getStatementfull().isEmpty()) {
						customVariables.put(ToolTaskInfo.STATEMENT_IN, ref.getStatement().getStatementfull());
					} else if (ref.getStatement().getStatementgeneric() != null
							&& !ref.getStatement().getStatementgeneric().isEmpty()) {
						customVariables.put(ToolTaskInfo.STATEMENT_IN, ref.getStatement().getStatementgeneric());
					}
					customVariables.put(ToolTaskInfo.LINENUMBER_IN,
							String.valueOf(Helper.getLineNumberSafe(ref.getStatement())));
				}
			}

			// Replace from
			if (castedQuestion.getFrom() != null && castedQuestion.getFrom().isComplete(true)) {
				final Reference ref = castedQuestion.getFrom().toReference();
				if (ref.getApp() != null && ref.getApp().getFile() != null && !ref.getApp().getFile().isEmpty()) {
					final File apk = new File(ref.getApp().getFile());
					customVariables.put(ToolTaskInfo.APP_APK_FROM, apk.getAbsolutePath());
				}
				if (ref.getClassname() != null && !ref.getClassname().isEmpty()) {
					customVariables.put(ToolTaskInfo.CLASS_FROM, ref.getClassname());
				}
				if (ref.getMethod() != null && !ref.getMethod().isEmpty()) {
					customVariables.put(ToolTaskInfo.METHOD_FROM, ref.getMethod());
				}
				if (ref.getStatement() != null) {
					if (ref.getStatement().getStatementfull() != null
							&& !ref.getStatement().getStatementfull().isEmpty()) {
						customVariables.put(ToolTaskInfo.STATEMENT_FROM, ref.getStatement().getStatementfull());
					} else if (ref.getStatement().getStatementgeneric() != null
							&& !ref.getStatement().getStatementgeneric().isEmpty()) {
						customVariables.put(ToolTaskInfo.STATEMENT_FROM, ref.getStatement().getStatementgeneric());
					}
					customVariables.put(ToolTaskInfo.LINENUMBER_FROM,
							String.valueOf(Helper.getLineNumberSafe(ref.getStatement())));
				}
			}

			// Replace to
			if (castedQuestion.getTo() != null && castedQuestion.getTo().isComplete(true)) {
				final Reference ref = castedQuestion.getTo().toReference();
				if (ref.getApp() != null && ref.getApp().getFile() != null && !ref.getApp().getFile().isEmpty()) {
					final File apk = new File(ref.getApp().getFile());
					customVariables.put(ToolTaskInfo.APP_APK_TO, apk.getAbsolutePath());
				}
				if (ref.getClassname() != null && !ref.getClassname().isEmpty()) {
					customVariables.put(ToolTaskInfo.CLASS_TO, ref.getClassname());
				}
				if (ref.getMethod() != null && !ref.getMethod().isEmpty()) {
					customVariables.put(ToolTaskInfo.METHOD_TO, ref.getMethod());
				}
				if (ref.getStatement() != null) {
					if (ref.getStatement().getStatementfull() != null
							&& !ref.getStatement().getStatementfull().isEmpty()) {
						customVariables.put(ToolTaskInfo.STATEMENT_TO, ref.getStatement().getStatementfull());
					} else if (ref.getStatement().getStatementgeneric() != null
							&& !ref.getStatement().getStatementgeneric().isEmpty()) {
						customVariables.put(ToolTaskInfo.STATEMENT_TO, ref.getStatement().getStatementgeneric());
					}
					customVariables.put(ToolTaskInfo.LINENUMBER_TO,
							String.valueOf(Helper.getLineNumberSafe(ref.getStatement())));
				}
			}

			// Replace features
			String allFeatures = "";
			if (this.features != null) {
				for (int i = 0; i < this.features.size(); i++) {
					customVariables.put(REPLACE_FEATURE.replace("#", Integer.valueOf(i + 1).toString()),
							"'" + this.features.get(i) + "'");
					allFeatures = allFeatures + (allFeatures.equals("") ? "'" : ", '") + this.features.get(i) + "'";
				}
			}
			customVariables.put(REPLACE_FEATURES, allFeatures);

			return cleanUpQuery(Helper.replaceCustomVariables(rule.getQuery(), customVariables));
		}
		return null;
	}
}