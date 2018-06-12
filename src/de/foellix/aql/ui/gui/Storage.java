package de.foellix.aql.ui.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.Log;

public class Storage {
	private final String storageFolder = "data/gui/";
	private final String storageFileQ = "lastQueries.ser";
	private final String storageFileA = "lastAnswers.ser";

	List<File> queries;
	List<File> answers;

	private static Storage instance = new Storage();

	private Storage() {
		loadData();
		if (this.queries == null) {
			this.queries = new ArrayList<>();
		}
		if (this.answers == null) {
			this.answers = new ArrayList<>();
		}
	}

	public static Storage getInstance() {
		return instance;
	}

	public void store(final File file, final boolean isAnswer) {
		if (!isAnswer) {
			if (!this.queries.contains(file)) {
				this.queries.add(file);
			}
		} else {
			if (!this.answers.contains(file)) {
				this.answers.add(file);
			}
		}
		saveData();
	}

	private void loadData() {
		try {
			final FileInputStream fileIn = new FileInputStream(this.storageFolder + this.storageFileQ);
			final ObjectInputStream in = new ObjectInputStream(fileIn);
			this.queries = (List<File>) in.readObject();
			in.close();
			fileIn.close();
		} catch (final IOException | ClassNotFoundException e) {
			Log.msg("Could not load last queries.", Log.DEBUG_DETAILED);
		}

		try {
			final FileInputStream fileIn = new FileInputStream(this.storageFolder + this.storageFileA);
			final ObjectInputStream in = new ObjectInputStream(fileIn);
			this.answers = (List<File>) in.readObject();
			in.close();
			fileIn.close();
		} catch (final IOException | ClassNotFoundException e) {
			Log.msg("Could not load last answers.", Log.DEBUG_DETAILED);
		}

		boolean changed = false;
		if (this.queries != null) {
			for (int i = 0; i < this.queries.size(); i++) {
				if (!this.queries.get(i).exists()) {
					this.queries.remove(i);
					i--;
					changed = true;
				}
			}
		}
		if (this.answers != null) {
			for (int i = 0; i < this.answers.size(); i++) {
				if (!this.answers.get(i).exists()) {
					this.answers.remove(i);
					i--;
					changed = true;
				}
			}
		}
		if (changed) {
			saveData();
		}
	}

	private void saveData() {
		try {
			final FileOutputStream fileOut = new FileOutputStream(this.storageFolder + this.storageFileQ);
			final ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this.queries);
			out.close();
			fileOut.close();
		} catch (final IOException e) {
			Log.msg("Could not save last queries.", Log.DEBUG);
		}

		try {
			final FileOutputStream fileOut = new FileOutputStream(this.storageFolder + this.storageFileA);
			final ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this.answers);
			out.close();
			fileOut.close();
		} catch (final IOException e) {
			Log.msg("Could not save last answers.", Log.DEBUG);
		}
	}

	public List<File> getLastFiles(final boolean isAnswer) {
		if (!isAnswer) {
			return this.queries;
		} else {
			return this.answers;
		}
	}
}