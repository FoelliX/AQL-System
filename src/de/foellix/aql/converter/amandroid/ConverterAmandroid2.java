package de.foellix.aql.converter.amandroid;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.converter.IConverter;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.Statement;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.helper.JawaHelper;
import de.foellix.aql.helper.KeywordsAndConstantsHelper;
import de.foellix.aql.system.task.ConverterTask;
import de.foellix.aql.system.task.ConverterTaskInfo;

public class ConverterAmandroid2 extends ConverterAmandroidBase implements IConverter {
	@Override
	public Answer parse(ConverterTask task) throws Exception {
		this.resultFile = new File(task.getTaskInfo().getData(ConverterTaskInfo.RESULT_FILE));
		this.app = Helper.createApp(Helper.getAppFromData(task.getTaskInfo()));

		final Answer answer = new Answer();

		// Step 1: Gather required needles
		this.needles = new HashSet<>();
		this.intentLines = new LinkedList<>();
		extractNeedles();

		// Step 2: Find required files
		final File srcDir = new File(this.resultFile.getParentFile().getParentFile(), "src");
		if (!srcDir.exists()) {
			Log.warning("Result indicated by \"" + this.resultFile.getAbsolutePath() + "\" is incomplete!");
			return answer;
		}
		this.sourceFiles = searchRecursively(srcDir);

		// Step 3: Map labels to references
		this.mapReferences = new HashMap<>();
		createReferences();

		// Step 4: Find connected sources and sinks
		answer.setFlows(computeFlows());

		// Step 5: Read Intents and IntentFilter
		computeIntents(answer);

		return answer;
	}

	private void extractNeedles() {
		try {
			final FileReader fr = new FileReader(this.resultFile);
			final BufferedReader br = new BufferedReader(fr);

			final StringBuilder sb = new StringBuilder();
			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.startsWith("        List(")) {
					final List<String> parts = getParts(line);
					if (parts.get(0).contains("#") && parts.get(0).contains(".  ")) {
						final String fromStr = Helper.cut(parts.get(0), "#", ".  ");
						this.needles.add(fromStr);
					}
					if (parts.get(parts.size() - 1).contains("#") && parts.get(parts.size() - 1).contains(".  ")) {
						final String toStr = Helper.cut(parts.get(parts.size() - 1), "#", ".  ");
						this.needles.add(toStr);
					}
				} else if (line.startsWith("Component ")) {
					this.intentLines.add(line);
				} else if (line.startsWith("      Caller Context: ")) {
					this.needles.add(Helper.cut(line, ",", ")"));
					this.intentLines.add(line);
				} else if (line.startsWith("    IntentFilter:(")) {
					this.intentLines.add(cleanUpIntentString(line));
				} else if (line.startsWith("        Intent:")) {
					sb.append(line);
				} else if (line.startsWith("          ") && !sb.isEmpty()) {
					sb.append(line);
				} else if (!sb.isEmpty()) {
					for (final String part : sb.toString().split(" Intent:")) {
						if (part.isBlank()) {
							continue;
						}
						this.intentLines.add(cleanUpIntentString(" Intent:" + part));
					}
					sb.setLength(0);
				}
			}

			br.close();
			fr.close();
		} catch (final IOException e) {
			e.printStackTrace();
			Log.error("Error while reading file: " + this.resultFile.getAbsolutePath());
		}
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
				String method = null;

				while ((line = br.readLine()) != null) {
					if (line.startsWith("procedure `")) {
						method = Helper.cut(line, "@signature `", "` @");
					} else if (line.startsWith("  #") && line.contains(".  ") && method != null) {
						final String key = Helper.cut(line, "  #", ".  ");
						if (this.needles.contains(key)) {
							try {
								this.mapReferences.put(key, createReference(line, classname, method));
							} catch (final IndexOutOfBoundsException e) {
								// do nothing
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

	private Reference createReference(String line, String classname, String method) {
		final Reference reference = new Reference();
		reference.setStatement(toStatement(line));
		reference.setMethod(toMethod(method));
		reference.setClassname(classname);
		reference.setApp(this.app);

		return reference;
	}

	private Statement toStatement(String amandroidString) {
		// Class
		String stmClass = Helper.cut(amandroidString, " @signature `", ";.");
		stmClass = JawaHelper.toJavaType(stmClass);

		// ReturnType
		String stmReturntype = Helper.cut(amandroidString, ")", "` @kind ", Helper.OCCURENCE_LAST);
		stmReturntype = JawaHelper.toJavaType(stmReturntype);

		// Parameters
		String stmParameters = Helper.cut(amandroidString, "(", ")", Helper.OCCURENCE_LAST);
		if (!stmParameters.isEmpty()) {
			final StringBuilder sb = new StringBuilder();
			for (final String parameter : stmParameters.split(";")) {
				if (sb.length() != 0) {
					sb.append(",");
				}
				sb.append(JawaHelper.toJavaType(parameter));
			}
			stmParameters = sb.toString();
		}

		// Function name
		final String stmName = Helper.cut(amandroidString, ";.", ":(");

		// Compose statement
		final String jimpleString = stmClass + ": " + stmReturntype + " " + stmName + "(" + stmParameters + ")";
		final Statement statement = Helper.createStatement("<" + jimpleString + ">", false);
		return statement;
	}

	private String toMethod(String amandroidString) {
		// Class
		String stmClass = Helper.cutFromStart(amandroidString, ";.");
		stmClass = JawaHelper.toJavaType(stmClass);

		// ReturnType
		String stmReturntype = Helper.cut(amandroidString, ")", Helper.OCCURENCE_LAST);
		stmReturntype = JawaHelper.toJavaType(stmReturntype);

		// Parameters
		String stmParameters = Helper.cut(amandroidString, "(", ")");
		if (!stmParameters.isEmpty()) {
			final StringBuilder sb = new StringBuilder();
			for (final String parameter : stmParameters.split(";")) {
				if (sb.length() != 0) {
					sb.append(",");
				}
				sb.append(JawaHelper.toJavaType(parameter));
			}
			stmParameters = sb.toString();
		}

		// Function name
		final String stmName = Helper.cut(amandroidString, ";.", ":(");

		// Compose method
		return "<" + stmClass + ": " + stmReturntype + " " + stmName + "(" + stmParameters + ")>";
	}

	private Flows computeFlows() {
		final Flows flows = new Flows();

		try {
			final FileReader fr = new FileReader(this.resultFile);
			final BufferedReader br = new BufferedReader(fr);

			Flow flow = null;

			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.startsWith(
						"      The path consists of the following edges (\"->\"). The nodes have the context information (p1 to pn means which parameter). The source is at the top :")) {
					flow = new Flow();
				} else if (flow != null) {
					if (line.startsWith("        List(")) {
						final List<String> parts = getParts(line);

						final String fromStr = Helper.cut(parts.get(0), "#", ".  ");
						final String toStr = Helper.cut(parts.get(parts.size() - 1), "#", ".  ");

						final Reference from = this.mapReferences.get(fromStr);
						final Reference to = this.mapReferences.get(toStr);
						if (from != null && to != null) {
							from.setType(KeywordsAndConstantsHelper.REFERENCE_TYPE_FROM);
							to.setType(KeywordsAndConstantsHelper.REFERENCE_TYPE_TO);

							flow.getReference().add(from);
							flow.getReference().add(to);

							flows.getFlow().add(flow);
						} else {
							Log.msg("Could not convert the following flow in Amandroid's result: " + line,
									Log.DEBUG_DETAILED);
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

	private List<String> getParts(String line) {
		final List<String> parts = new ArrayList<>(Arrays.asList(line.substring(13, line.length() - 1).split(";, ")));
		final List<String> toRemove = new ArrayList<>();
		for (final String part : parts) {
			if (!part.contains("  call ")) {
				toRemove.add(part);
			}
		}
		parts.removeAll(toRemove);
		return parts;
	}
}