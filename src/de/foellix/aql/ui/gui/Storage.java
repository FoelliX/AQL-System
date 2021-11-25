package de.foellix.aql.ui.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import de.foellix.aql.Log;

public class Storage {
	public static final int TYPE_QUERIES = 0;
	public static final int TYPE_ANSWERS = 1;
	public static final int TYPE_FILES = 2;

	public static final String PROPERTY_FULLSCREEN = "fullScreen";
	public static final String PROPERTY_POSITION = "windowPosition";
	public static final String PROPERTY_SIZE = "windowSize";
	public static final String PROPERTY_CONFIRM_EXIT = "confirmExit";
	public static final String PROPERTY_CONFIRM_QUERY_ENDED = "confirmQueryEnded";

	private final String storageFolder = "data/gui/";
	private final String storageFileQ = "lastQueries.ser";
	private final String storageFileA = "lastAnswers.ser";
	private final String storageFileF = "lastFiles.ser";
	private final String storageFileC = "config.properties";

	private RecentFileList queries;
	private RecentFileList answers;
	private RecentFileList files;
	private Properties guiConfig;

	private static Storage instance = new Storage();

	private Storage() {
		// Data
		loadData();
		if (this.queries == null) {
			this.queries = new RecentFileList();
		}
		if (this.answers == null) {
			this.answers = new RecentFileList();
		}
		if (this.files == null) {
			this.files = new RecentFileList();
		}

		// GUI Config
		resetGuiConfig(false);
		loadGuiConfig();
	}

	public static Storage getInstance() {
		return instance;
	}

	public void store(final File file, final int type) {
		switch (type) {
			case TYPE_ANSWERS: {
				this.answers.add(file);
				break;
			}
			case TYPE_QUERIES: {
				this.queries.add(file);
				break;
			}
			default: {
				this.files.add(file);
			}
		}
		saveData();
	}

	private void loadData() {
		try {
			final FileInputStream fileIn = new FileInputStream(this.storageFolder + this.storageFileQ);
			final ObjectInputStream in = new ObjectInputStream(fileIn);
			this.queries = (RecentFileList) in.readObject();
			in.close();
			fileIn.close();
		} catch (final IOException | ClassNotFoundException e) {
			Log.msg("Could not load last queries.", Log.DEBUG_DETAILED);
		}

		try {
			final FileInputStream fileIn = new FileInputStream(this.storageFolder + this.storageFileA);
			final ObjectInputStream in = new ObjectInputStream(fileIn);
			this.answers = (RecentFileList) in.readObject();
			in.close();
			fileIn.close();
		} catch (final IOException | ClassNotFoundException e) {
			Log.msg("Could not load last answers.", Log.DEBUG_DETAILED);
		}

		try {
			final FileInputStream fileIn = new FileInputStream(this.storageFolder + this.storageFileF);
			final ObjectInputStream in = new ObjectInputStream(fileIn);
			this.files = (RecentFileList) in.readObject();
			in.close();
			fileIn.close();
		} catch (final IOException | ClassNotFoundException e) {
			Log.msg("Could not load last used files.", Log.DEBUG_DETAILED);
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
		if (this.files != null) {
			for (int i = 0; i < this.files.size(); i++) {
				if (!this.files.get(i).exists()) {
					this.files.remove(i);
					i--;
					changed = true;
				}
			}
		}
		if (changed) {
			saveData();
		}
	}

	private void loadGuiConfig() {
		try (FileInputStream fis = new FileInputStream(this.storageFolder + this.storageFileC)) {
			this.guiConfig.load(fis);
		} catch (final IOException e) {
			Log.msg("Could not load GUI config file.", Log.DEBUG_DETAILED);
		}
	}

	private void saveData() {
		// Remove duplicates
		final List<Integer> toRemove = new LinkedList<>();
		for (int i = 0; i < this.queries.size(); i++) {
			final File q1 = this.queries.get(i);
			for (int j = i + 1; j < this.queries.size(); j++) {
				final File q2 = this.queries.get(j);
				if (q1.getAbsolutePath().equals(q2.getAbsolutePath())) {
					toRemove.add(i);
				}
			}
		}
		for (final int i : toRemove) {
			this.queries.remove(i);
		}

		toRemove.clear();
		for (int i = 0; i < this.answers.size(); i++) {
			final File a1 = this.answers.get(i);
			for (int j = i + 1; j < this.answers.size(); j++) {
				final File a2 = this.answers.get(j);
				if (a1.getAbsolutePath().equals(a2.getAbsolutePath())) {
					toRemove.add(i);
				}
			}
		}
		for (final int i : toRemove) {
			this.answers.remove(i);
		}

		toRemove.clear();
		for (int i = 0; i < this.files.size(); i++) {
			final File f1 = this.files.get(i);
			for (int j = i + 1; j < this.files.size(); j++) {
				final File f2 = this.files.get(j);
				if (f1.getAbsolutePath().equals(f2.getAbsolutePath())) {
					toRemove.add(i);
				}
			}
		}
		for (final int i : toRemove) {
			this.files.remove(i);
		}

		// Save
		try {
			final FileOutputStream fileOut = new FileOutputStream(this.storageFolder + this.storageFileQ);
			final ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this.queries);
			out.close();
			fileOut.close();
		} catch (final IOException e) {
			Log.msg("Could not save last queries." + Log.getExceptionAppendix(e), Log.DEBUG);
		}

		try {
			final FileOutputStream fileOut = new FileOutputStream(this.storageFolder + this.storageFileA);
			final ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this.answers);
			out.close();
			fileOut.close();
		} catch (final IOException e) {
			Log.msg("Could not save last answers." + Log.getExceptionAppendix(e), Log.DEBUG);
		}

		try {
			final FileOutputStream fileOut = new FileOutputStream(this.storageFolder + this.storageFileF);
			final ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(this.files);
			out.close();
			fileOut.close();
		} catch (final IOException e) {
			Log.msg("Could not save last used files." + Log.getExceptionAppendix(e), Log.DEBUG);
		}
	}

	private void saveGuiConfig() {
		try (FileWriter fw = new FileWriter(this.storageFolder + this.storageFileC)) {
			this.guiConfig.store(fw, null);
		} catch (final IOException e) {
			Log.msg("Could not save GUI config file." + Log.getExceptionAppendix(e), Log.DEBUG);
		}
	}

	public List<File> getRecentFiles(final int type) {
		switch (type) {
			case TYPE_ANSWERS:
				return this.answers.getRecentFiles();
			case TYPE_QUERIES:
				return this.queries.getRecentFiles();
			default:
				return this.files.getRecentFiles();
		}
	}

	public void setGuiConfigProperty(String property, String value) {
		this.guiConfig.setProperty(property, value);
		saveGuiConfig();
	}

	public String getGuiConfigProperty(String property) {
		return this.guiConfig.getProperty(property);
	}

	public void resetGuiConfig() {
		resetGuiConfig(true);
	}

	public void resetGuiConfig(boolean deleteFile) {
		this.guiConfig = new Properties();
		this.guiConfig.setProperty(PROPERTY_CONFIRM_QUERY_ENDED, Boolean.TRUE.toString());
		if (deleteFile) {
			final File guiConfigFile = new File(this.storageFolder + this.storageFileC);
			try {
				Files.delete(guiConfigFile.toPath());
			} catch (final IOException e) {
				Log.msg("Could not reset GUI configuration file." + Log.getExceptionAppendix(e), Log.DEBUG);
			}
		}
	}
}