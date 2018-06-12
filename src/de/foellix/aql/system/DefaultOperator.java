package de.foellix.aql.system;

import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Attribute;
import de.foellix.aql.datastructure.Attributes;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.Intentfilters;
import de.foellix.aql.datastructure.Intents;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsinks;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.Intentsources;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.Permissions;
import de.foellix.aql.datastructure.QuestionFilter;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.WaitingAnswer;
import de.foellix.aql.helper.EqualsHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.OperatorTaskInfo;

public class DefaultOperator extends Tool {
	public static Answer applyOperator(OperatorTaskInfo taskinfo) {
		Answer answer = null;
		if (taskinfo.getQuestion().getOperator().equals(KeywordsAndConstants.OPERATOR_FILTER)) {
			answer = taskinfo.getWaitingAnswer().getAnswers()[0];
			if (answer instanceof WaitingAnswer) {
				answer = ((WaitingAnswer) answer).getAnswer();
			}
			if (taskinfo.getQuestion() instanceof QuestionFilter) {
				final QuestionFilter qf = (QuestionFilter) taskinfo.getQuestion();

				if (qf.getName() != null || qf.getValue() != null) {
					answer = filter2(answer, qf.getName(), qf.getValue(), qf.getSoi());
				} else if (qf.getSoi() != KeywordsAndConstants.QUESTION_TYPE_UNKNOWN) {
					answer = filter3(answer, qf.getSoi());
				} else {
					answer = filter1(answer);
				}
			} else {
				answer = filter1(answer);
			}
		} else {
			Answer a1 = taskinfo.getWaitingAnswer().getAnswers()[0];
			if (a1 instanceof WaitingAnswer) {
				a1 = ((WaitingAnswer) a1).getAnswer();
			}
			Answer a2 = taskinfo.getWaitingAnswer().getAnswers()[1];
			if (a2 instanceof WaitingAnswer) {
				a2 = ((WaitingAnswer) a2).getAnswer();
			}

			if (taskinfo.getQuestion().getOperator().equals(KeywordsAndConstants.OPERATOR_UNIFY)) {
				answer = unify(a1, a2);
			} else if (taskinfo.getQuestion().getOperator().equals(KeywordsAndConstants.OPERATOR_CONNECT)) {
				answer = connect(a1, a2);
			}
		}
		return answer;
	}

	public static Answer unify(final Answer answer, final Answer unifyWithAnswer) {
		Answer returnAnswer = new Answer();

		// Permissions
		if (answer.getPermissions() != null || unifyWithAnswer.getPermissions() != null) {
			final Permissions permissions = new Permissions();
			if (answer.getPermissions() != null) {
				permissions.getPermission().addAll(answer.getPermissions().getPermission());
			}
			if (unifyWithAnswer.getPermissions() != null) {
				permissions.getPermission().addAll(unifyWithAnswer.getPermissions().getPermission());
			}
			returnAnswer.setPermissions(permissions);
		}

		// Intents
		if (answer.getIntents() != null || unifyWithAnswer.getIntents() != null) {
			final Intents intents = new Intents();
			if (answer.getIntents() != null) {
				intents.getIntent().addAll(answer.getIntents().getIntent());
			}
			if (unifyWithAnswer.getIntents() != null) {
				intents.getIntent().addAll(unifyWithAnswer.getIntents().getIntent());
			}
			returnAnswer.setIntents(intents);
		}

		// Intent-filters
		if (answer.getIntentfilters() != null || unifyWithAnswer.getIntentfilters() != null) {
			final Intentfilters intentfilters = new Intentfilters();
			if (answer.getIntentfilters() != null) {
				intentfilters.getIntentfilter().addAll(answer.getIntentfilters().getIntentfilter());
			}
			if (unifyWithAnswer.getIntentfilters() != null) {
				intentfilters.getIntentfilter().addAll(unifyWithAnswer.getIntentfilters().getIntentfilter());
			}
			returnAnswer.setIntentfilters(intentfilters);
		}

		// Intent-sinks
		if (answer.getIntentsinks() != null || unifyWithAnswer.getIntentsinks() != null) {
			final Intentsinks intentsinks = new Intentsinks();
			if (answer.getIntentsinks() != null) {
				intentsinks.getIntentsink().addAll(answer.getIntentsinks().getIntentsink());
			}
			if (unifyWithAnswer.getIntentsinks() != null) {
				intentsinks.getIntentsink().addAll(unifyWithAnswer.getIntentsinks().getIntentsink());
			}
			returnAnswer.setIntentsinks(intentsinks);
		}

		// Intent-sources
		if (answer.getIntentsources() != null || unifyWithAnswer.getIntentsources() != null) {
			final Intentsources intentsources = new Intentsources();
			if (answer.getIntentsources() != null) {
				intentsources.getIntentsource().addAll(answer.getIntentsources().getIntentsource());
			}
			if (unifyWithAnswer.getIntentsources() != null) {
				intentsources.getIntentsource().addAll(unifyWithAnswer.getIntentsources().getIntentsource());
			}
			returnAnswer.setIntentsources(intentsources);
		}

		// Flow
		if (answer.getFlows() != null || unifyWithAnswer.getFlows() != null) {
			final Flows paths = new Flows();
			if (answer.getFlows() != null) {
				paths.getFlow().addAll(answer.getFlows().getFlow());
			}
			if (unifyWithAnswer.getFlows() != null) {
				paths.getFlow().addAll(unifyWithAnswer.getFlows().getFlow());
			}
			returnAnswer.setFlows(paths);
		}

		// Remove redundant items
		returnAnswer = Helper.removeRedundant(returnAnswer);

		return returnAnswer;
	}

	public static Answer connect(final Answer answer, final Answer connectWithAnswer) {
		return connect(answer, connectWithAnswer, KeywordsAndConstants.DEFAULT_CONNECT_ALL);
	}

	public static Answer connect(final Answer answer, final Answer connectWithAnswer, int mode) {
		Answer returnAnswer = unify(answer, connectWithAnswer);

		// New Flow for intent-sinks and -sources
		if (mode == KeywordsAndConstants.DEFAULT_CONNECT_ALL
				|| mode == KeywordsAndConstants.DEFAULT_CONNECT_INTER_APP) {
			if (returnAnswer.getIntentsinks() != null && returnAnswer.getIntentsources() != null) {
				for (final Intentsink from : returnAnswer.getIntentsinks().getIntentsink()) {
					boolean addDefault = false;
					if (from.getTarget().getCategory() == null
							|| !from.getTarget().getCategory().contains(KeywordsAndConstants.CATEGORY_DEFAULT)) {
						addDefault = true;
						from.getTarget().getCategory().add(KeywordsAndConstants.CATEGORY_DEFAULT);
					}
					for (final Intentsource to : returnAnswer.getIntentsources().getIntentsource()) {
						if (EqualsHelper.equalsConnect(from, to)) {
							if (returnAnswer.getFlows() == null) {
								returnAnswer.setFlows(new Flows());
							}
							returnAnswer.getFlows().getFlow().add(connect(from, to));
						}
					}
					if (addDefault) {
						from.getTarget().getCategory().remove(KeywordsAndConstants.CATEGORY_DEFAULT);
					}
				}
			}
		}

		// Connect existing paths
		if (mode == KeywordsAndConstants.DEFAULT_CONNECT_ALL
				|| mode == KeywordsAndConstants.DEFAULT_CONNECT_INTRA_APP) {
			if (returnAnswer.getFlows() != null) {
				boolean fixpoint = false;
				while (!fixpoint) {
					fixpoint = true;
					final List<Flow> addPaths = new ArrayList<>();
					for (final Flow path1 : returnAnswer.getFlows().getFlow()) {
						for (final Flow path2 : returnAnswer.getFlows().getFlow()) {
							if (path1 != path2) {
								Reference path1From = null;
								Reference path1To = null;
								Reference path2From = null;
								Reference path2To = null;
								for (final Reference refPath1 : path1.getReference()) {
									if (refPath1.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_FROM)) {
										path1From = refPath1;
									} else if (refPath1.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_TO)) {
										path1To = refPath1;
									}
								}
								for (final Reference refPath2 : path2.getReference()) {
									if (refPath2.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_FROM)) {
										path2From = refPath2;
									} else if (refPath2.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_TO)) {
										path2To = refPath2;
									}
								}

								if (path1From != null && path1To != null && path2From != null && path2To != null) {
									if (EqualsHelper.equals(path1To, path2From)) {
										boolean exists = false;

										for (final Flow checkPath : returnAnswer.getFlows().getFlow()) {
											Reference checkFrom = null;
											Reference checkTo = null;
											for (final Reference checkRef : checkPath.getReference()) {
												if (checkRef.getType()
														.equals(KeywordsAndConstants.REFERENCE_TYPE_FROM)) {
													checkFrom = checkRef;
												} else if (checkRef.getType()
														.equals(KeywordsAndConstants.REFERENCE_TYPE_TO)) {
													checkTo = checkRef;
												}
											}

											if (checkFrom != null && checkTo != null) {
												if (EqualsHelper.equals(path1From, checkFrom)
														&& EqualsHelper.equals(path2To, checkTo)) {
													exists = true;
												}
											} else {
												exists = true;
											}
										}

										if (!exists) {
											fixpoint = false;
											addPaths.add(connect(path1From, path2To));
										}
									}
								}
							}
						}
					}
					returnAnswer.getFlows().getFlow().addAll(addPaths);
				}
			}
		}

		// Remove redundant items
		returnAnswer = Helper.removeRedundant(returnAnswer);

		return returnAnswer;
	}

	private static Flow connect(final Reference from, final Reference to) {
		final Flow newPath = new Flow();

		final Reference newFrom = new Reference();
		newFrom.setType(KeywordsAndConstants.REFERENCE_TYPE_FROM);
		newFrom.setApp(from.getApp());
		newFrom.setClassname(from.getClassname());
		newFrom.setMethod(from.getMethod());
		newFrom.setStatement(from.getStatement());

		final Reference newTo = new Reference();
		newTo.setType(KeywordsAndConstants.REFERENCE_TYPE_TO);
		newTo.setApp(to.getApp());
		newTo.setClassname(to.getClassname());
		newTo.setMethod(to.getMethod());
		newTo.setStatement(to.getStatement());

		newPath.getReference().add(newFrom);
		newPath.getReference().add(newTo);

		return newPath;
	}

	private static Flow connect(final Intentsink from, final Intentsource to) {
		final Flow newPath = new Flow();

		final Reference newFrom = new Reference();
		newFrom.setType(KeywordsAndConstants.REFERENCE_TYPE_FROM);
		newFrom.setApp(from.getReference().getApp());
		newFrom.setClassname(from.getReference().getClassname());
		newFrom.setMethod(from.getReference().getMethod());
		newFrom.setStatement(from.getReference().getStatement());

		final Reference newTo = new Reference();
		newTo.setType(KeywordsAndConstants.REFERENCE_TYPE_TO);
		newTo.setApp(to.getReference().getApp());
		newTo.setClassname(to.getReference().getClassname());
		newTo.setMethod(to.getReference().getMethod());
		newTo.setStatement(to.getReference().getStatement());

		newPath.getReference().add(newFrom);
		newPath.getReference().add(newTo);

		return newPath;
	}

	public static Answer filter1(final Answer answer) {
		// Permissions
		if (answer.getPermissions() != null) {
			for (int i = 0; i < answer.getPermissions().getPermission().size(); i++) {
				final Reference ref1 = answer.getPermissions().getPermission().get(i).getReference();

				if (filter(ref1, answer)) {
					answer.getPermissions().getPermission().remove(i);
					i--;
				}
			}
			if (answer.getPermissions().getPermission().size() == 0) {
				answer.setPermissions(null);
			}
		}

		// Intent-sinks
		if (answer.getIntentsinks() != null) {
			for (int i = 0; i < answer.getIntentsinks().getIntentsink().size(); i++) {
				final Reference ref1 = answer.getIntentsinks().getIntentsink().get(i).getReference();

				if (filter(ref1, answer)) {
					answer.getIntentsinks().getIntentsink().remove(i);
					i--;
				}
			}
			if (answer.getIntentsinks().getIntentsink().size() == 0) {
				answer.setIntentsinks(null);
			}
		}

		// Intent-sources
		if (answer.getIntentsources() != null) {
			for (int i = 0; i < answer.getIntentsources().getIntentsource().size(); i++) {
				final Reference ref1 = answer.getIntentsources().getIntentsource().get(i).getReference();

				if (filter(ref1, answer)) {
					answer.getIntentsources().getIntentsource().remove(i);
					i--;
				}
			}
			if (answer.getIntentsources().getIntentsource().size() == 0) {
				answer.setIntentsources(null);
			}
		}

		// Flows (set flag only)
		if (answer.getFlows() != null) {
			for (final Flow path1 : answer.getFlows().getFlow()) {
				if (path1.getAttributes() != null) {
					Attribute removeAttr = null;
					for (final Attribute attr : path1.getAttributes().getAttribute()) {
						if (attr.getName().equals("complete") && attr.getValue().equals("true")) {
							removeAttr = attr;
							break;
						}
					}
					if (removeAttr != null) {
						path1.getAttributes().getAttribute().remove(removeAttr);
					}
				}
			}

			for (final Flow path1 : answer.getFlows().getFlow()) {
				boolean complete = true;

				Reference path1From = null;
				Reference path1To = null;
				for (final Reference refPath1 : path1.getReference()) {
					if (refPath1.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_FROM)) {
						path1From = refPath1;
					} else if (refPath1.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_TO)) {
						path1To = refPath1;
					}
				}

				for (final Flow path2 : answer.getFlows().getFlow()) {
					if (path1 != path2) {
						Reference path2From = null;
						Reference path2To = null;
						for (final Reference refPath2 : path2.getReference()) {
							if (refPath2.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_FROM)) {
								path2From = refPath2;
							} else if (refPath2.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_TO)) {
								path2To = refPath2;
							}
						}

						if (EqualsHelper.equals(path1From, path2To) || EqualsHelper.equals(path1To, path2From)) {
							complete = false;
						}

					}
				}
				if (complete) {
					boolean add = true;
					if (path1.getAttributes() == null) {
						path1.setAttributes(new Attributes());
					} else {
						for (final Attribute attr : path1.getAttributes().getAttribute()) {
							if (attr.getName().equals("complete") && attr.getValue().equals("true")) {
								add = false;
								break;
							}
						}
					}

					if (add) {
						final Attribute completeAttr = new Attribute();
						completeAttr.setName("complete");
						completeAttr.setValue("true");
						path1.getAttributes().getAttribute().add(completeAttr);
					}
				}
			}
		}

		return answer;
	}

	private static boolean filter(final Reference ref1, final Answer answer) {
		if (answer.getFlows() == null) {
			return true;
		}
		for (final Flow path : answer.getFlows().getFlow()) {
			for (final Reference ref2 : path.getReference()) {
				if (EqualsHelper.equals(ref1, ref2)) {
					return false;
				}
			}
		}
		return true;
	}

	public static Answer filter2(final Answer answer, String name, String value, final int soi) {
		name = name.substring(1, name.length() - 1);
		value = value.substring(1, value.length() - 1);

		// Permissions
		if (soi == KeywordsAndConstants.QUESTION_TYPE_UNKNOWN
				|| soi == KeywordsAndConstants.QUESTION_TYPE_PERMISSIONS) {
			if (answer.getPermissions() != null) {
				for (int i = 0; i < answer.getPermissions().getPermission().size(); i++) {
					boolean keep = false;
					if (answer.getPermissions().getPermission().get(i).getAttributes() != null) {
						for (final Attribute attr : answer.getPermissions().getPermission().get(i).getAttributes()
								.getAttribute()) {
							if (attr.getName().equals(name) && attr.getValue().equals(value)) {
								keep = true;
								break;
							}
						}
					}

					if (!keep) {
						answer.getPermissions().getPermission().remove(i);
						i--;
					}
				}
				if (answer.getPermissions().getPermission().size() == 0) {
					answer.setPermissions(null);
				}
			}
		}

		// Intents
		if (soi == KeywordsAndConstants.QUESTION_TYPE_UNKNOWN || soi == KeywordsAndConstants.QUESTION_TYPE_INTENTS) {
			if (answer.getIntents() != null) {
				for (int i = 0; i < answer.getIntents().getIntent().size(); i++) {
					boolean keep = false;
					if (answer.getIntents().getIntent().get(i).getAttributes() != null) {
						for (final Attribute attr : answer.getIntents().getIntent().get(i).getAttributes()
								.getAttribute()) {
							if (attr.getName().equals(name) && attr.getValue().equals(value)) {
								keep = true;
								break;
							}
						}
					}

					if (!keep) {
						answer.getIntents().getIntent().remove(i);
						i--;
					}
				}
				if (answer.getIntents().getIntent().size() == 0) {
					answer.setIntents(null);
				}
			}
		}

		// Intent-Filters
		if (soi == KeywordsAndConstants.QUESTION_TYPE_UNKNOWN
				|| soi == KeywordsAndConstants.QUESTION_TYPE_INTENTFILTER) {
			if (answer.getIntentfilters() != null) {
				for (int i = 0; i < answer.getIntentfilters().getIntentfilter().size(); i++) {
					boolean keep = false;
					if (answer.getIntentfilters().getIntentfilter().get(i).getAttributes() != null) {
						for (final Attribute attr : answer.getIntentfilters().getIntentfilter().get(i).getAttributes()
								.getAttribute()) {
							if (attr.getName().equals(name) && attr.getValue().equals(value)) {
								keep = true;
								break;
							}
						}
					}

					if (!keep) {
						answer.getIntentfilters().getIntentfilter().remove(i);
						i--;
					}
				}
				if (answer.getIntentfilters().getIntentfilter().size() == 0) {
					answer.setIntentfilters(null);
				}
			}
		}

		// Intent-Sinks
		if (soi == KeywordsAndConstants.QUESTION_TYPE_UNKNOWN
				|| soi == KeywordsAndConstants.QUESTION_TYPE_INTENTSINKS) {
			if (answer.getIntentsinks() != null) {
				for (int i = 0; i < answer.getIntentsinks().getIntentsink().size(); i++) {
					boolean keep = false;
					if (answer.getIntentsinks().getIntentsink().get(i).getAttributes() != null) {
						for (final Attribute attr : answer.getIntentsinks().getIntentsink().get(i).getAttributes()
								.getAttribute()) {
							if (attr.getName().equals(name) && attr.getValue().equals(value)) {
								keep = true;
								break;
							}
						}
					}

					if (!keep) {
						answer.getIntentsinks().getIntentsink().remove(i);
						i--;
					}
				}
				if (answer.getIntentsinks().getIntentsink().size() == 0) {
					answer.setIntentsinks(null);
				}
			}
		}

		// Intent-Sources
		if (soi == KeywordsAndConstants.QUESTION_TYPE_UNKNOWN
				|| soi == KeywordsAndConstants.QUESTION_TYPE_INTENTSOURCES) {
			if (answer.getIntentsources() != null) {
				for (int i = 0; i < answer.getIntentsources().getIntentsource().size(); i++) {
					boolean keep = false;
					if (answer.getIntentsources().getIntentsource().get(i).getAttributes() != null) {
						for (final Attribute attr : answer.getIntentsources().getIntentsource().get(i).getAttributes()
								.getAttribute()) {
							if (attr.getName().equals(name) && attr.getValue().equals(value)) {
								keep = true;
								break;
							}
						}
					}

					if (!keep) {
						answer.getIntentsources().getIntentsource().remove(i);
						i--;
					}
				}
				if (answer.getIntentsources().getIntentsource().size() == 0) {
					answer.setIntentsources(null);
				}
			}
		}

		// Flows
		if (soi == KeywordsAndConstants.QUESTION_TYPE_UNKNOWN || soi == KeywordsAndConstants.QUESTION_TYPE_FLOWS) {
			if (answer.getFlows() != null) {
				for (int i = 0; i < answer.getFlows().getFlow().size(); i++) {
					boolean keep = false;
					if (answer.getFlows().getFlow().get(i).getAttributes() != null) {
						for (final Attribute attr : answer.getFlows().getFlow().get(i).getAttributes().getAttribute()) {
							if (attr.getName().equals(name) && attr.getValue().equals(value)) {
								keep = true;
								break;
							}
						}
					}

					if (!keep) {
						answer.getFlows().getFlow().remove(i);
						i--;
					}
				}
				if (answer.getFlows().getFlow().size() == 0) {
					answer.setFlows(null);
				}
			}
		}

		return answer;
	}

	public static Answer filter3(final Answer answer, final int soi) {
		// Permissions
		if (soi == KeywordsAndConstants.QUESTION_TYPE_PERMISSIONS) {
			answer.setPermissions(null);
		}

		// Intents
		if (soi == KeywordsAndConstants.QUESTION_TYPE_INTENTS) {
			answer.setIntents(null);
		}

		// Intent-Filters
		if (soi == KeywordsAndConstants.QUESTION_TYPE_INTENTFILTER) {
			answer.setIntentfilters(null);
		}

		// Intent-Sinks
		if (soi == KeywordsAndConstants.QUESTION_TYPE_INTENTSINKS) {
			answer.setIntentsinks(null);
		}

		// Intent-Sources
		if (soi == KeywordsAndConstants.QUESTION_TYPE_INTENTSOURCES) {
			answer.setIntentsources(null);
		}

		// Flows
		if (soi == KeywordsAndConstants.QUESTION_TYPE_FLOWS) {
			answer.setFlows(null);
		}

		return answer;
	}
}
