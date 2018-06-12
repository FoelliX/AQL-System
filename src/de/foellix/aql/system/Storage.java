package de.foellix.aql.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.foellix.aql.Log;
import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.IQuestionNode;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.QuestionPart;
import de.foellix.aql.datastructure.handler.AnswerHandler;
import de.foellix.aql.helper.HashHelper;
import de.foellix.aql.helper.Helper;

public class Storage implements Serializable {
	private static final long serialVersionUID = -8412816715518012412L;

	private final String storageFolder = "data/storage/";
	private final String storageFileParts = "storageParts.ser";
	private final String storageFilePreprocessors = "storagePreprocessors.ser";

	private Data data;
	private Map<String, App> dataPreprocessors;

	private static Storage instance = new Storage();

	private Storage() {
		loadData();

		if (this.data == null) {
			this.data = new Data();
		}
		if (this.dataPreprocessors == null) {
			this.dataPreprocessors = new HashMap<>();
		}
	}

	public static Storage getInstance() {
		return instance;
	}

	public void store(final Tool tool, final IQuestionNode question, final Answer answer) {
		final String hash = HashHelper.createHash(tool, question);
		Log.msg("Storing part with associated hash: " + hash, Log.DEBUG_DETAILED);

		final File xmlFile = new File("data/storage/" + hash + ".xml");
		AnswerHandler.createXML(answer, xmlFile);

		List<IQuestionNode> tempList = this.data.getData().get(hash);
		if (tempList == null) {
			tempList = new ArrayList<>();
			if (question instanceof QuestionPart) {
				if (tool.getQuestions().contains(KeywordsAndConstants.MODE_INTRA_FLOWS)
						|| tool.getQuestions().contains(KeywordsAndConstants.MODE_INTER_FLOWS)) {
					final QuestionPart tempQuestionPart = Helper.copy((QuestionPart) question);
					tempQuestionPart.setMode(KeywordsAndConstants.QUESTION_TYPE_FLOWS);
					tempList.add(tempQuestionPart);
				}
				if (tool.getQuestions().contains(KeywordsAndConstants.MODE_PERMISSIONS)) {
					final QuestionPart tempQuestionPart = Helper.copy((QuestionPart) question);
					tempQuestionPart.setMode(KeywordsAndConstants.QUESTION_TYPE_PERMISSIONS);
					tempList.add(tempQuestionPart);
				}
				if (tool.getQuestions().contains(KeywordsAndConstants.MODE_INTENTFILTER)) {
					final QuestionPart tempQuestionPart = Helper.copy((QuestionPart) question);
					tempQuestionPart.setMode(KeywordsAndConstants.QUESTION_TYPE_INTENTFILTER);
					tempList.add(tempQuestionPart);
				}
				if (tool.getQuestions().contains(KeywordsAndConstants.MODE_INTENTS)) {
					final QuestionPart tempQuestionPart = Helper.copy((QuestionPart) question);
					tempQuestionPart.setMode(KeywordsAndConstants.QUESTION_TYPE_INTENTS);
					tempList.add(tempQuestionPart);
				}
				if (tool.getQuestions().contains(KeywordsAndConstants.MODE_INTENTSOURCES)) {
					final QuestionPart tempQuestionPart = Helper.copy((QuestionPart) question);
					tempQuestionPart.setMode(KeywordsAndConstants.QUESTION_TYPE_INTENTSOURCES);
					tempList.add(tempQuestionPart);
				}
				if (tool.getQuestions().contains(KeywordsAndConstants.MODE_INTENTSINKS)) {
					final QuestionPart tempQuestionPart = Helper.copy((QuestionPart) question);
					tempQuestionPart.setMode(KeywordsAndConstants.QUESTION_TYPE_INTENTSINKS);
					tempList.add(tempQuestionPart);
				}
			} else {
				if (!tempList.contains(question)) {
					tempList.add(question);
				}
			}
			this.data.getData().put(hash, tempList);
			this.data.getMayFitHashes().put(HashHelper.createGenericHash(question), hash);
			this.data.getMayFitTool().put(HashHelper.createGenericHash(question), tool.getQuestions());
		} else {
			if (!tempList.contains(question)) {
				tempList.add(question);
			}
		}

		saveData();
	}

	public void store(final Tool preprocessor, final App app, final App preprocessedApp) {
		final String hash = HashHelper.createHash(preprocessor, app);
		Log.msg("Storing preprocessor with associated hash: " + hash, Log.DEBUG_DETAILED);

		this.dataPreprocessors.put(hash, preprocessedApp);

		saveData();
	}

	public Answer load(final Tool tool, final IQuestionNode question) {
		return load(tool, question, false);
	}

	public Answer load(final Tool tool, final IQuestionNode question, final boolean loadSimilar) {
		if (tool != null) {
			final String hash = HashHelper.createHash(tool, question);
			final File file = getFile(hash);
			if (file.exists()) {
				return AnswerHandler.parseXML(getFile(hash));
			}
		}
		if (question instanceof QuestionPart) {
			final QuestionPart p1 = (QuestionPart) question;
			for (final List<IQuestionNode> list : this.data.getData().values()) {
				for (final IQuestionNode storedQuestion : list) {
					if (storedQuestion instanceof QuestionPart) {
						final QuestionPart p2 = (QuestionPart) storedQuestion;
						if (p1.getMode() == p2.getMode() && p1.toRAW().equals(p2.toRAW())) {
							return AnswerHandler.parseXML(getFile(this.data.getData().getKey(list)));
						}
					}
				}
			}
		}
		if (loadSimilar) {
			final String hash = this.data.getMayFitHashes().get(HashHelper.createGenericHash(question));
			final File file = getFile(hash);
			if (file.exists()) {
				if (question instanceof QuestionPart
						&& this.data.getMayFitTool().get(HashHelper.createGenericHash(question))
								.contains(Helper.modeToString(((QuestionPart) question).getMode()))) {
					Log.warning(
							"Previously computed answer may not be accurate, because it only fits on App detail-level.");
					return AnswerHandler.parseXML(getFile(hash));
				}
			}
		}

		return null;
	}

	public App load(final Tool preprocessor, final App app) {
		if (preprocessor != null) {
			final String hash = HashHelper.createHash(preprocessor, app);
			final App preprocessedApp = this.dataPreprocessors.get(hash);
			if (preprocessedApp != null && getFile(preprocessedApp).exists()) {
				return preprocessedApp;
			}
		}
		return null;
	}

	private void loadData() {
		try {
			final FileInputStream fileIn = new FileInputStream(this.storageFolder + this.storageFileParts);
			final ObjectInputStream in = new ObjectInputStream(fileIn);
			this.data = (Data) in.readObject();
			in.close();
			fileIn.close();
		} catch (final IOException | ClassNotFoundException e) {
			Log.warning("Could not load part-storage.");
		}

		try {
			final FileInputStream fileIn = new FileInputStream(this.storageFolder + this.storageFilePreprocessors);
			final ObjectInputStream in = new ObjectInputStream(fileIn);
			this.dataPreprocessors = (HashMap<String, App>) in.readObject();
			in.close();
			fileIn.close();
		} catch (final IOException | ClassNotFoundException e) {
			Log.warning("Could not load preprocessor-storage.");
		}

		if (this.data != null) {
			boolean changed = false;
			final List<String> removeKeys = new ArrayList<>();
			for (final String hash : this.data.getData().keySet()) {
				if (!getFile(hash).exists()) {
					removeKeys.add(hash);
					changed = true;
				}
			}
			if (changed) {
				for (final String removeKey : removeKeys) {
					this.data.getData().remove(removeKey);
				}
				saveData();
			}
		}

		if (this.dataPreprocessors != null) {
			boolean changed = false;
			final List<String> removeKeys = new ArrayList<>();
			for (final String hash : this.dataPreprocessors.keySet()) {
				if (!getFile(this.dataPreprocessors.get(hash)).exists()) {
					removeKeys.add(hash);
					changed = true;
				}
			}
			if (changed) {
				for (final String removeKey : removeKeys) {
					this.dataPreprocessors.remove(removeKey);
				}
				saveData();
			}
		}
	}

	private void saveData() {
		try {
			final FileOutputStream fileOut = new FileOutputStream(this.storageFolder + this.storageFileParts);
			final ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this.data);
			out.close();
			fileOut.close();
		} catch (final IOException e) {
			Log.error("Could not save part-storage.");
		}

		try {
			final FileOutputStream fileOut = new FileOutputStream(this.storageFolder + this.storageFilePreprocessors);
			final ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this.dataPreprocessors);
			out.close();
			fileOut.close();
		} catch (final IOException e) {
			Log.error("Could not save preprocessor-storage.");
		}
	}

	private File getFile(final String hash) {
		return new File(this.storageFolder + hash + ".xml");
	}

	private File getFile(final App app) {
		return new File(app.getFile());
	}

	public Data getData() {
		return this.data;
	}

	public void reset() {
		instance = new Storage();
	}
}
