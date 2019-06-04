package de.foellix.aql.converter.amandroid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.foellix.aql.Log;
import de.foellix.aql.converter.IConverter;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.QuestionPart;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.Statement;
import de.foellix.aql.helper.EqualsHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.ToolTaskInfo;

public class ConverterAmandroid implements IConverter {
	private File resultFile;
	private List<File> sourceFiles;
	private QuestionPart question;

	private List<String> sourceNeedles;
	private List<String> sinkNeedles;

	private Map<String, Reference> mapSources;
	private Map<String, Reference> mapSinks;

	@Override
	public Answer parse(final File resultFile, final ToolTaskInfo taskInfo) {
		this.question = taskInfo.getQuestion();

		// Step 1: Find required files
		this.resultFile = resultFile;
		this.sourceFiles = searchRecursively(new File(resultFile.getParentFile().getParentFile(), "src"));

		// Step 2: Identify sources and sinks
		this.sourceNeedles = new ArrayList<>();
		this.sinkNeedles = new ArrayList<>();
		extractNeedles();

		// Step 3: Map labels to sources and sinks
		this.mapSources = new HashMap<>();
		this.mapSinks = new HashMap<>();
		createReferences();

		// Step 4: Find connected sources and sinks
		final Answer answer = new Answer();
		answer.setFlows(computeFlows());

		return answer;
	}

	private List<File> searchRecursively(File resultFolder) {
		final List<File> returnList = new ArrayList<>();
		for (final File file : resultFolder.listFiles()) {
			if (file.isDirectory()) {
				returnList.addAll(searchRecursively(file));
			} else {
				if (file.getAbsolutePath().endsWith(".jawa")) {
					returnList.add(file);
				}
			}
		}
		return returnList;
	}

	private void extractNeedles() {
		try {
			final FileReader fr = new FileReader(this.resultFile);
			final BufferedReader br = new BufferedReader(fr);

			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.contains("<Descriptors: api_source: L")) {
					this.sourceNeedles
							.add(Helper.cutFromFirstToLast(line, "<Descriptors: api_source: L", ">").replace("?", ""));
				} else if (line.contains("<Descriptors: api_sink: L")) {
					this.sinkNeedles
							.add(Helper.cutFromFirstToLast(line, "<Descriptors: api_sink: L", " ").replace("?", ""));
				}
			}
			br.close();
			fr.close();
		} catch (final IOException e) {
			e.printStackTrace();
			Log.error("Error while reading file: " + this.resultFile.getAbsolutePath());
		}
	}

	private void createReferences() {
		for (final File sourceFile : this.sourceFiles) {
			try {
				final FileReader fr = new FileReader(sourceFile);
				final BufferedReader br = new BufferedReader(fr);

				String line = br.readLine();
				if (line == null) {
					br.close();
					throw new NullPointerException();
				}
				final String classname = Helper.cut(line, "record `", "` @kind ");
				String method = "";

				while ((line = br.readLine()) != null) {
					if (line.startsWith("procedure `")) {
						method = Helper.cut(line, "@signature `L", "` @");
					} else if (line.startsWith("  #")) {
						for (final String needle : this.sourceNeedles) {
							if (line.contains(needle)) {
								this.mapSources.put(Helper.cut(line, "  #", ".  "),
										createReference(needle, method, classname));
							}
						}
						for (final String needle : this.sinkNeedles) {
							if (line.contains(needle)) {
								this.mapSinks.put(Helper.cut(line, "  #", ".  "),
										createReference(needle, method, classname));
							}
						}
					}
				}
				br.close();
				fr.close();
			} catch (final IOException e) {
				Log.error("Error while reading file: " + this.resultFile.getAbsolutePath());
			}
		}
	}

	private Reference createReference(String needle, String method, String classname) {
		final Reference reference = new Reference();
		reference.setStatement(toStatement(needle));
		reference.setMethod(toMethod(method));
		reference.setClassname(classname);
		reference.setApp(this.question.getAllReferences().get(0).getApp());
		return reference;
	}

	private Statement toStatement(String amandroidString) {
		String stmClass = amandroidString.substring(0, amandroidString.indexOf(";"));
		stmClass = stmClass.replace("/", ".");
		String stmReturntype = amandroidString.substring(amandroidString.indexOf(")") + 1);
		if (stmReturntype.contains(";")) {
			stmReturntype = stmReturntype.substring(0, stmReturntype.indexOf(";"));
		}
		stmReturntype = stmReturntype.replace("/", ".");
		if (stmReturntype.startsWith("L")) {
			stmReturntype = stmReturntype.substring(1);
		} else if (stmReturntype.startsWith("I")) {
			stmReturntype = "int";
		} else if (stmReturntype.startsWith("V")) {
			stmReturntype = "void";
		}
		String stmParameters = Helper.cut(amandroidString, "(", ")");
		if (!stmParameters.equals("")) {
			String temp = "";
			for (final String parameter : stmParameters.split(";")) {
				if (!temp.equals("")) {
					temp += ",";
				}
				if (parameter.startsWith("L")) {
					temp += parameter.substring(1);
				} else if (stmReturntype.startsWith("I")) {
					temp += "int";
				} else if (stmReturntype.startsWith("V")) {
					temp += "void";
				}
			}
			stmParameters = temp.replace("/", ".");
		}
		final String stmName = Helper.cut(amandroidString, ";.", ":(");

		final String jimpleString = stmClass + ": " + stmReturntype + " " + stmName + "(" + stmParameters + ")";
		final Statement statement = Helper.createStatement(jimpleString);

		return statement;
	}

	private String toMethod(String amandroidString) {
		final String jimpleString = "<" + toStatement(amandroidString).getStatementgeneric() + ">";

		return jimpleString;
	}

	private Flows computeFlows() {
		final Flows flows = new Flows();

		try {
			final FileReader fr = new FileReader(this.resultFile);
			final BufferedReader br = new BufferedReader(fr);

			Flow flow = null;
			Reference from = null;
			Reference to = null;

			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.startsWith(
						"      The path consists of the following edges (\"->\"). The nodes have the context information (p1 to pn means which parameter). The source is at the top :")) {
					flow = new Flow();
					from = null;
					to = null;
				} else if (flow != null) {
					boolean newOne = false;
					for (final String label : this.mapSources.keySet()) {
						if (line.contains(label)) {
							from = this.mapSources.get(label);
							from.setType(KeywordsAndConstants.REFERENCE_TYPE_FROM);
							newOne = true;
						}
					}
					for (final String label : this.mapSinks.keySet()) {
						if (line.contains(label)) {
							to = this.mapSinks.get(label);
							to.setType(KeywordsAndConstants.REFERENCE_TYPE_TO);
							newOne = true;
						}
					}
					if (newOne && from != null && to != null) {
						flow.getReference().add(from);
						flow.getReference().add(to);
						boolean add = true;
						for (final Flow temp : flows.getFlow()) {
							if (EqualsHelper.equals(temp, flow)) {
								add = false;
								break;
							}
						}
						if (add) {
							flows.getFlow().add(flow);
						}
					}
				}
			}
			br.close();
			fr.close();
		} catch (final IOException e) {
			e.printStackTrace();
			Log.error("Error while reading file: " + this.resultFile.getAbsolutePath());
		}

		return flows;
	}
}
