package de.foellix.aql.converter.didfail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.converter.IConverter;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.QuestionPart;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.helper.EqualsHelper;
import de.foellix.aql.helper.Helper;
import de.foellix.aql.system.task.ToolTaskInfo;

public class ConverterDidFail implements IConverter {
	private File resultFolder;
	private QuestionPart question;

	@Override
	public Answer parse(final File resultFolder, final ToolTaskInfo taskInfo) {
		this.resultFolder = resultFolder;
		this.question = taskInfo.getQuestion();

		final Answer answer = new Answer();
		answer.setFlows(new Flows());

		try {
			List<Reference> to = null;

			final File resultFile = new File(resultFolder, "flows.out");
			final FileReader fr = new FileReader(resultFile);
			final BufferedReader br = new BufferedReader(fr);

			String line = "";
			while ((line = br.readLine()) != null) {
				if (line.contains("### 'Sink: ") && line.contains("': ###")) {
					final String statement = Helper.cut(line, "### 'Sink: ", "': ###");
					to = generateCompleteReference(statement);
					if (to.size() > 1) {
						Log.warning(
								"Could not identify unique to-reference: " + statement + " (Taking all into account)");
					}
				} else if (to != null) {
					if (line.contains("'Src: ")) {
						final String statement = Helper.cut(line, "'Src: ", "'");
						final List<Reference> from = generateCompleteReference(statement);
						if (from.size() > 1) {
							Log.warning("Could not identify unique from-reference: " + statement
									+ " (Taking all into account)");
						}

						for (final Reference fromRef : from) {
							for (final Reference toRef : to) {
								final Flow flow = new Flow();

								fromRef.setType(KeywordsAndConstants.REFERENCE_TYPE_FROM);
								flow.getReference().add(fromRef);
								toRef.setType(KeywordsAndConstants.REFERENCE_TYPE_TO);
								flow.getReference().add(toRef);

								boolean add = true;
								for (final Flow temp : answer.getFlows().getFlow()) {
									if (EqualsHelper.equals(temp, flow)) {
										add = false;
										break;
									}
								}
								if (add) {
									answer.getFlows().getFlow().add(flow);
								}
							}
						}
					} else {
						break;
					}
				}
			}
			br.close();
			fr.close();
		} catch (final IOException e) {
			e.printStackTrace();
			Log.error("Error while reading files in directory: " + resultFolder.getAbsolutePath());
			return null;
		}

		return answer;
	}

	private List<Reference> generateCompleteReference(String statement) throws IOException {
		final List<Reference> returnList = new ArrayList<>();

		final File logFolder = new File(this.resultFolder, "log");
		for (final File file : logFolder.listFiles()) {
			if (file.getName().endsWith("flowdroid.log")) {
				final FileReader fr = new FileReader(file);
				final BufferedReader br = new BufferedReader(fr);
				String line = "";
				while ((line = br.readLine()) != null) {
					if (line.contains(statement) && line.contains(" in method ")) {
						final String method = Helper.cut(line, " in method ", ")>") + ")>";
						final String classname = Helper.cut(method, "<", ": ");
						final Reference reference = new Reference();
						for (final Reference appCandidate : this.question.getAllReferences()) {
							if (appCandidate.getApp().getFile().toLowerCase().contains(file.getName()
									.substring(0, file.getName().indexOf("flowdroid.log") - 1).toLowerCase())) {
								reference.setApp(appCandidate.getApp());
							}
						}
						if (reference.getApp() == null) {
							br.close();
							fr.close();
							throw new FileNotFoundException("Could not find file that matches an app with the name: "
									+ file.getName().substring(0, file.getName().indexOf("flowdroid.log") - 1));
						}
						reference.setClassname(classname);
						reference.setMethod(method);
						reference.setStatement(Helper.createStatement(statement));

						returnList.add(reference);
					}
				}
				br.close();
				fr.close();
			}
		}

		return returnList;
	}
}
