package de.foellix.aql.system.defaulttools.operators;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.Intentfilter;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.Target;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.ConnectHelper;
import de.foellix.aql.helper.EqualsHelper;
import de.foellix.aql.helper.EqualsOptions;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.helper.KeywordsAndConstantsHelper;
import de.foellix.aql.system.task.OperatorTask;

public class DefaultConnectOperator extends DefaultUnifyOperator {
	public static final int CONNECT_MODE_ALL = 0;
	public static final int CONNECT_MODE_INTRA_APP = 1;
	public static final int CONNECT_MODE_INTER_APP = 2;
	public static final int CONNECT_MODE_FILTER_SOURCE = 3;

	private static final String NEEDLE_SET_RESULT = "void setResult(int,android.content.Intent)";
	private static final String NEEDLE_ON_ACTIVITY_RESULT = "void onActivityResult(int,int,android.content.Intent)";

	private int connectMode;
	private boolean approximate;

	public DefaultConnectOperator() {
		this(CONNECT_MODE_ALL);
	}

	public DefaultConnectOperator(int connectMode) {
		super();
		this.connectMode = connectMode;
		this.approximate = false;
	}

	@Override
	public String getName() {
		return super.getName() + (this.approximate ? "*" : "");
	}

	/**
	 * Disabled by default! Use with caution! Intents and intent filters are not accurately matched when enabled. Instead an over-approximation based on the action attribute ONLY is used.
	 *
	 * @param enabled
	 *            Default: false
	 * @return The same operator with adapted approximation
	 */
	public DefaultConnectOperator setApproximation(boolean enabled) {
		this.approximate = enabled;
		return this;
	}

	@Override
	public File applyOperator(OperatorTask task) {
		final File answerFile = FileHelper.getTempFile(FileHelper.FILE_ENDING_XML);
		AnswerHandler.createXML(applyOperatorInner(task), answerFile);
		return answerFile;
	}

	@Override
	public Answer applyOperatorInner(OperatorTask task) {
		Answer temp = null;
		final Set<Answer> children = task.getAvailableAnswersOfChildren();
		for (final Answer childAnswer : children) {
			if (temp == null) {
				if (children.size() > 1) {
					temp = childAnswer;
				} else {
					temp = connect(childAnswer, childAnswer);
				}
			} else {
				temp = connect(temp, childAnswer);
			}
		}
		return temp;
	}

	public Answer connect(final Answer answer, final Answer connectWithAnswer) {
		final EqualsOptions localEqualsOptions = equalsOptions.copy().setOption(EqualsOptions.PRECISELY_TARGET, true)
				.setOption(EqualsOptions.PRECISELY_REFERENCE, true);

		Answer returnAnswer = unify(answer, connectWithAnswer);

		// Attach intent-filters to intent-sources
		if (this.connectMode == CONNECT_MODE_ALL || this.connectMode == CONNECT_MODE_FILTER_SOURCE) {
			if (returnAnswer.getIntentfilters() != null && !returnAnswer.getIntentfilters().getIntentfilter().isEmpty()
					&& returnAnswer.getIntentsources() != null
					&& !returnAnswer.getIntentsources().getIntentsource().isEmpty()) {
				final List<Intentsource> newSources = new LinkedList<>();
				for (final Intentfilter filter : returnAnswer.getIntentfilters().getIntentfilter()) {
					final Target target = new Target();
					target.getAction().addAll(filter.getAction());
					target.getCategory().addAll(filter.getCategory());
					target.getData().addAll(filter.getData());
					if (filter.getReference() != null && filter.getReference().getClassname() != null
							&& !filter.getReference().getClassname().isBlank()) {
						for (final Intentsource source : returnAnswer.getIntentsources().getIntentsource()) {
							if (filter.getReference().getClassname().equals(source.getReference().getClassname())) {
								final Intentsource newSource = new Intentsource();
								newSource.setReference(source.getReference());
								newSource.setTarget(target);
								boolean add = true;
								for (final Intentsource existentSource : returnAnswer.getIntentsources()
										.getIntentsource()) {
									if (EqualsHelper.equals(existentSource, newSource, localEqualsOptions)) {
										add = false;
										break;
									}
								}
								if (add) {
									newSources.add(newSource);
								}
							}
						}
					}
				}
				returnAnswer.getIntentsources().getIntentsource().addAll(newSources);
			}
		}

		// New Flow for intent-sinks and -sources
		if (this.connectMode == CONNECT_MODE_ALL || this.connectMode == CONNECT_MODE_INTER_APP) {
			if (returnAnswer.getIntentsinks() != null && returnAnswer.getIntentsources() != null) {
				final List<Flow> addedFlows = new LinkedList<>();
				for (final Intentsink from : returnAnswer.getIntentsinks().getIntentsink()) {
					if (from.getTarget() != null) {
						boolean addDefault = false;
						if (from.getTarget() == null) {
							continue;
						}
						if ((from.getTarget().getCategory() == null || !from.getTarget().getCategory()
								.contains(KeywordsAndConstantsHelper.CATEGORY_DEFAULT))
								&& (from.getTarget().getAction() != null && !from.getTarget().getAction().isEmpty())) {
							addDefault = true;
							from.getTarget().getCategory().add(KeywordsAndConstantsHelper.CATEGORY_DEFAULT);
						}
						for (final Intentsource to : returnAnswer.getIntentsources().getIntentsource()) {
							if (to.getTarget() != null
									&& (EqualsHelper.equals(to.getTarget(), from.getTarget(), localEqualsOptions)
											|| (this.approximate
													&& EqualsHelper.equalsCategoryAndAction(to.getTarget().getAction(),
															from.getTarget().getAction(), localEqualsOptions)))) {
								if (returnAnswer.getFlows() == null) {
									returnAnswer.setFlows(new Flows());
								}
								final Flow flowToAdd = ConnectHelper.connect(from, to);
								addedFlows.add(flowToAdd);
								returnAnswer.getFlows().getFlow().add(flowToAdd);
							}
						}
						if (addDefault) {
							from.getTarget().getCategory().remove(KeywordsAndConstantsHelper.CATEGORY_DEFAULT);
						}
					}
				}

				// Additional accumulated flows (setResult -> onActivityResult)
				if (returnAnswer.getFlows() != null) {
					final List<Flow> toAdd = new LinkedList<>();
					for (final Flow onActivityResultFlow : returnAnswer.getFlows().getFlow()) {
						final Reference fromOnActivityResultFlow = Helper.getFrom(onActivityResultFlow); // TO
						if (fromOnActivityResultFlow.getMethod().contains(NEEDLE_ON_ACTIVITY_RESULT)) {
							for (final Flow addedFlow : addedFlows) {
								final Reference fromAddedFlow = Helper.getFrom(addedFlow);
								if (EqualsHelper.equals(fromAddedFlow.getApp(), fromOnActivityResultFlow.getApp(),
										localEqualsOptions)
										&& EqualsHelper.equalsClassString(fromAddedFlow.getClassname(),
												fromOnActivityResultFlow.getClassname())) {
									final Reference toAddedFlow = Helper.getTo(addedFlow);
									for (final Flow setResultFlow : returnAnswer.getFlows().getFlow()) {
										final Reference toSetResultFlow = Helper.getTo(setResultFlow); // FROM
										if (Helper.getStatementgenericSafe(toSetResultFlow.getStatement())
												.contains(NEEDLE_SET_RESULT)) {
											final Reference fromSetResultFlow = Helper.getFrom(setResultFlow);
											if (EqualsHelper.equals(toAddedFlow, fromSetResultFlow,
													localEqualsOptions)) {
												toAdd.add(ConnectHelper.connect(toSetResultFlow,
														fromOnActivityResultFlow));
											}
										}
									}
								}
							}
						}
					}
					returnAnswer.getFlows().getFlow().addAll(toAdd);
				}
			}
		}

		// Connect existing paths
		if (this.connectMode == CONNECT_MODE_ALL || this.connectMode == CONNECT_MODE_INTRA_APP) {
			if (returnAnswer.getFlows() != null) {
				ConnectHelper.computeTransitiveHull(returnAnswer.getFlows(), equalsOptions);
			}
		}

		// Remove redundant items
		returnAnswer = Helper.removeRedundant(returnAnswer, equalsOptions);

		return returnAnswer;
	}
}