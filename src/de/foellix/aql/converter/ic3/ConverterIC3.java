package de.foellix.aql.converter.ic3;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import de.foellix.aql.Log;
import de.foellix.aql.converter.IConverter;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Data;
import de.foellix.aql.datastructure.Intentfilter;
import de.foellix.aql.datastructure.Intentfilters;
import de.foellix.aql.datastructure.Intents;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsinks;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.Intentsources;
import de.foellix.aql.datastructure.QuestionPart;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.Target;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.ToolTaskInfo;
import edu.psu.cse.siis.ic3.Ic3Data;
import edu.psu.cse.siis.ic3.Ic3Data.Application;
import edu.psu.cse.siis.ic3.Ic3Data.Application.Component;
import edu.psu.cse.siis.ic3.Ic3Data.Application.Component.ExitPoint;
import edu.psu.cse.siis.ic3.Ic3Data.Application.Component.ExitPoint.Intent;
import edu.psu.cse.siis.ic3.Ic3Data.Application.Component.Extra;
import edu.psu.cse.siis.ic3.Ic3Data.Application.Component.Instruction;
import edu.psu.cse.siis.ic3.Ic3Data.Application.Component.IntentFilter;
import edu.psu.cse.siis.ic3.Ic3Data.Attribute;
import edu.psu.cse.siis.ic3.Ic3Data.AttributeKind;

public class ConverterIC3 implements IConverter {
	@Override
	public Answer parse(final File resultFile, final ToolTaskInfo taskInfo) {
		final Application app;

		try {
			final InputStream input = new FileInputStream(resultFile);
			app = Ic3Data.Application.parseFrom(input);
		} catch (final IOException e) {
			e.printStackTrace();
			Log.error("Error while reading file: " + resultFile.getAbsolutePath());
			return null;
		}

		return convert(app, taskInfo.getQuestion());
	}

	private Answer convert(final Application data, final QuestionPart question) {
		final Answer answer = new Answer();

		Log.msg(data.toString(), Log.DEBUG_DETAILED);

		// Intent Information
		if (data.getComponentsList() != null && !data.getComponentsList().isEmpty()) {
			answer.setIntentfilters(new Intentfilters());
			for (final Component component : data.getComponentsList()) {
				// Intent-Filters
				for (final Extra ex : component.getExtrasList()) {
					final Instruction instruction = ex.getInstruction();

					// Origin
					final Reference originReduced = new Reference();
					originReduced.setClassname(instruction.getClassName());
					originReduced.setApp(question.getAllReferences().get(0).getApp());
					final Reference origin = new Reference();
					origin.setStatement(Helper.createStatement(instruction.getStatement()));
					origin.setMethod(instruction.getMethod());
					origin.setClassname(instruction.getClassName());
					origin.setApp(question.getAllReferences().get(0).getApp());

					if (!component.getIntentFiltersList().isEmpty()) {
						// Action
						for (final IntentFilter intentfilter : component.getIntentFiltersList()) {
							final Intentfilter intentfilterObj = new Intentfilter();
							intentfilterObj.setReference(originReduced);
							Data dataObj = null;
							for (final Attribute attribute : intentfilter.getAttributesList()) {
								// Implicit only, since explicit is set in
								// origin (Component Reference)
								if (attribute.getKind() == AttributeKind.CATEGORY) {
									for (final String value : attribute.getValueList()) {
										intentfilterObj.getCategory().add(value);
									}
								} else if (attribute.getKind() == AttributeKind.ACTION) {
									for (final String value : attribute.getValueList()) {
										intentfilterObj.getAction().add(value);
									}
								} else if (attribute.getKind() == AttributeKind.TYPE) {
									for (final String value : attribute.getValueList()) {
										dataObj = new Data();
										dataObj.setType(value);
										intentfilterObj.getData().add(dataObj);
									}
								} else if (attribute.getKind() == AttributeKind.SCHEME) {
									for (final String value : attribute.getValueList()) {
										dataObj = new Data();
										dataObj.setScheme(value);
										intentfilterObj.getData().add(dataObj);
									}
								} else if (attribute.getKind() == AttributeKind.SSP) {
									for (final String value : attribute.getValueList()) {
										dataObj = new Data();
										dataObj.setSsp(value);
										intentfilterObj.getData().add(dataObj);
									}
								} else if (attribute.getKind() == AttributeKind.HOST) {
									for (final String value : attribute.getValueList()) {
										dataObj = new Data();
										dataObj.setHost(value);
										intentfilterObj.getData().add(dataObj);
									}
								} else if (attribute.getKind() == AttributeKind.PORT) {
									for (final String value : attribute.getValueList()) {
										dataObj = new Data();
										dataObj.setPort(value);
										intentfilterObj.getData().add(dataObj);
									}
								} else if (attribute.getKind() == AttributeKind.PATH) {
									for (final String value : attribute.getValueList()) {
										dataObj = new Data();
										dataObj.setPath(value);
										intentfilterObj.getData().add(dataObj);
									}
								} else if (attribute.getKind() == AttributeKind.URI) {
									for (final String value : attribute.getValueList()) {
										dataObj = new Data();
										Helper.extractDataFromURI(value, dataObj);
										intentfilterObj.getData().add(dataObj);
									}
								} else if (attribute.getKind() == AttributeKind.AUTHORITY) {
									for (final String value : attribute.getValueList()) {
										dataObj = new Data();
										Helper.extractDataFromAuthority(value, dataObj);
										intentfilterObj.getData().add(dataObj);
									}
								}
								// MISSING: URI, AUTHORITY
							}
							answer.getIntentfilters().getIntentfilter().add(intentfilterObj);
						}
					}
				}

				// Intent-Sources
				if (!component.getExtrasList().isEmpty()) {
					if (answer.getIntentsources() == null) {
						answer.setIntentsources(new Intentsources());
					}
					for (final Extra ex : component.getExtrasList()) {
						final Instruction instruction = ex.getInstruction();

						// Origin
						final Reference originReduced = new Reference();
						originReduced.setClassname(instruction.getClassName());
						originReduced.setApp(question.getAllReferences().get(0).getApp());
						final Reference origin = new Reference();
						origin.setStatement(Helper.createStatement(instruction.getStatement()));
						origin.setMethod(instruction.getMethod());
						origin.setClassname(instruction.getClassName());
						origin.setApp(question.getAllReferences().get(0).getApp());

						// Explicit
						final Intentsource intentsourceExp = new Intentsource();
						intentsourceExp.setReference(origin);
						final Target targetExp = new Target();
						targetExp.setReference(originReduced);
						intentsourceExp.setTarget(targetExp);
						answer.getIntentsources().getIntentsource().add(intentsourceExp);

						// Implicit
						if (!component.getIntentFiltersList().isEmpty()) {
							// Action
							for (final IntentFilter intentfilter : component.getIntentFiltersList()) {
								final Intentsource intentsource = new Intentsource();
								intentsource.setReference(origin);
								final Target targetImp = new Target();
								Data dataObj = null;
								for (final Attribute attribute : intentfilter.getAttributesList()) {
									// Implicit only, since explicit is set in
									// origin (Component Reference)
									if (attribute.getKind() == AttributeKind.CATEGORY) {
										for (final String value : attribute.getValueList()) {
											targetImp.getCategory().add(value);
										}
									} else if (attribute.getKind() == AttributeKind.ACTION) {
										for (final String value : attribute.getValueList()) {
											targetImp.getAction().add(value);
										}
									} else if (attribute.getKind() == AttributeKind.TYPE) {
										for (final String value : attribute.getValueList()) {
											dataObj = new Data();
											dataObj.setType(value);
											targetImp.getData().add(dataObj);
										}
									} else if (attribute.getKind() == AttributeKind.SCHEME) {
										for (final String value : attribute.getValueList()) {
											dataObj = new Data();
											dataObj.setScheme(value);
											targetImp.getData().add(dataObj);
										}
									} else if (attribute.getKind() == AttributeKind.SSP) {
										for (final String value : attribute.getValueList()) {
											dataObj = new Data();
											dataObj.setSsp(value);
											targetImp.getData().add(dataObj);
										}
									} else if (attribute.getKind() == AttributeKind.HOST) {
										for (final String value : attribute.getValueList()) {
											dataObj = new Data();
											dataObj.setHost(value);
											targetImp.getData().add(dataObj);
										}
									} else if (attribute.getKind() == AttributeKind.PORT) {
										for (final String value : attribute.getValueList()) {
											dataObj = new Data();
											dataObj.setPort(value);
											targetImp.getData().add(dataObj);
										}
									} else if (attribute.getKind() == AttributeKind.PATH) {
										for (final String value : attribute.getValueList()) {
											dataObj = new Data();
											dataObj.setPath(value);
											targetImp.getData().add(dataObj);
										}
									} else if (attribute.getKind() == AttributeKind.URI) {
										for (final String value : attribute.getValueList()) {
											dataObj = new Data();
											Helper.extractDataFromURI(value, dataObj);
											targetImp.getData().add(dataObj);
										}
									} else if (attribute.getKind() == AttributeKind.AUTHORITY) {
										for (final String value : attribute.getValueList()) {
											dataObj = new Data();
											Helper.extractDataFromAuthority(value, dataObj);
											targetImp.getData().add(dataObj);
										}
									}
								}
								intentsource.setTarget(targetImp);
								answer.getIntentsources().getIntentsource().add(intentsource);
							}
						}
					}
				}

				// Intents & Intent-Sinks
				if (!component.getExitPointsList().isEmpty()) {
					if (answer.getIntents() == null) {
						answer.setIntents(new Intents());
					}
					if (answer.getIntentsinks() == null) {
						answer.setIntentsinks(new Intentsinks());
					}
					for (final ExitPoint exitpoint : component.getExitPointsList()) {
						final Instruction instruction = exitpoint.getInstruction();

						// Origin
						final Reference origin = new Reference();
						origin.setStatement(Helper.createStatement(instruction.getStatement()));
						origin.setMethod(instruction.getMethod());
						origin.setClassname(instruction.getClassName());
						origin.setApp(question.getAllReferences().get(0).getApp());

						// Action & Package + Class
						for (final Intent intent : exitpoint.getIntentsList()) {
							final de.foellix.aql.datastructure.Intent intentObj = new de.foellix.aql.datastructure.Intent();
							final Intentsink intentsink = new Intentsink();
							intentObj.setReference(origin);
							intentsink.setReference(origin);
							final Target target = new Target();
							Reference targetRef = null;
							Data dataObj = null;
							for (final Attribute attribute : intent.getAttributesList()) {
								// Implicit
								if (attribute.getKind() == AttributeKind.CATEGORY) {
									for (final String value : attribute.getValueList()) {
										target.getCategory().add(value);
									}
								} else if (attribute.getKind() == AttributeKind.ACTION) {
									for (final String value : attribute.getValueList()) {
										target.getAction().add(value);
									}
								} else if (attribute.getKind() == AttributeKind.TYPE) {
									for (final String value : attribute.getValueList()) {
										dataObj = new Data();
										dataObj.setType(value);
										target.getData().add(dataObj);
									}
								} else if (attribute.getKind() == AttributeKind.SCHEME) {
									for (final String value : attribute.getValueList()) {
										dataObj = new Data();
										dataObj.setScheme(value);
										target.getData().add(dataObj);
									}
								} else if (attribute.getKind() == AttributeKind.SSP) {
									for (final String value : attribute.getValueList()) {
										dataObj = new Data();
										dataObj.setSsp(value);
										target.getData().add(dataObj);
									}
								} else if (attribute.getKind() == AttributeKind.HOST) {
									for (final String value : attribute.getValueList()) {
										dataObj = new Data();
										dataObj.setHost(value);
										target.getData().add(dataObj);
									}
								} else if (attribute.getKind() == AttributeKind.PORT) {
									for (final String value : attribute.getValueList()) {
										dataObj = new Data();
										dataObj.setPort(value);
										target.getData().add(dataObj);
									}
								} else if (attribute.getKind() == AttributeKind.PATH) {
									for (final String value : attribute.getValueList()) {
										dataObj = new Data();
										dataObj.setPath(value);
										target.getData().add(dataObj);
									}
								} else if (attribute.getKind() == AttributeKind.URI) {
									for (final String value : attribute.getValueList()) {
										dataObj = new Data();
										Helper.extractDataFromURI(value, dataObj);
										target.getData().add(dataObj);
									}
								} else if (attribute.getKind() == AttributeKind.AUTHORITY) {
									for (final String value : attribute.getValueList()) {
										dataObj = new Data();
										Helper.extractDataFromAuthority(value, dataObj);
										target.getData().add(dataObj);
									}
								} else {
									// Explicit
									if (attribute.getKind() == AttributeKind.CLASS) {
										for (final String value : attribute.getValueList()) {
											if (targetRef == null) {
												targetRef = new Reference();
											}
											targetRef.setClassname(value.replace("/", "."));
										}
									} else if (attribute.getKind() == AttributeKind.PACKAGE) {
										for (final String value : attribute.getValueList()) {
											if (targetRef == null) {
												targetRef = new Reference();
											}
											if (targetRef.getClassname() == null
													|| targetRef.getClassname().isEmpty()) {
												targetRef.setClassname(value);
											}
										}
									}
								}
							}
							if (targetRef != null) {
								target.setReference(targetRef);
							}
							intentObj.setTarget(target);
							intentsink.setTarget(target);
							answer.getIntents().getIntent().add(intentObj);
							answer.getIntentsinks().getIntentsink().add(intentsink);
						}
					}
				}

			}
		}

		return answer;
	}
}
