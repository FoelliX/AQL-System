package de.foellix.aql.system.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.system.task.TaskAnswer;

public class Storage {
	public static final File DEFAULT_STORAGE_DIRECTORY = new File("data/storage");
	public static final String STORAGE_FILE = "storage.ser";

	private File storageDirectory;
	private Data data;

	private static Storage instance = new Storage();

	private Storage() {
		this.storageDirectory = DEFAULT_STORAGE_DIRECTORY;
		this.storageDirectory.mkdir();
		init(true);
	}

	private void init(boolean loadData) {
		if (loadData) {
			loadData();
		}
		if (this.data == null) {
			this.data = new Data();
		}
	}

	public static Storage getInstance() {
		return instance;
	}

	public File getStorageDirectory() {
		return this.storageDirectory;
	}

	public void setStorageDirectory(File newStorageDirectory) {
		this.storageDirectory = newStorageDirectory;
		this.storageDirectory.mkdir();
		Log.msg("Relocating storage to " + this.storageDirectory, Log.NORMAL);
		reset();
	}

	public Data getData() {
		return this.data;
	}

	private void loadData() {
		final File storageFile = new File(this.storageDirectory, STORAGE_FILE);
		if (storageFile.exists()) {
			try {
				final FileInputStream fileIn = new FileInputStream(storageFile);
				final ObjectInputStream in = new ObjectInputStream(fileIn);
				final Object temp = in.readObject();
				if (temp instanceof Data) {
					this.data = (Data) temp;
				} else {
					Log.error("Loaded storage file is corrupted or not valid for this version of AQL-System!");
				}
				in.close();
				fileIn.close();
			} catch (final IOException | ClassNotFoundException e) {
				Log.warning(
						"Could not load storage file: " + storageFile.getAbsolutePath() + Log.getExceptionAppendix(e));
			}
		} else {
			Log.msg("No storage file (" + storageFile.getAbsolutePath() + ") found. Continuing with fresh one.",
					Log.NORMAL);
		}

		// CleanUp answers which do not exist on local file-system and save
		if (this.data != null) {
			if (cleanUp()) {
				saveData();
			}
		}
	}

	public void saveData() {
		final File storageFile = new File(this.storageDirectory, STORAGE_FILE);
		if ((storageFile.getParentFile().exists() && storageFile.getParentFile().isDirectory())
				|| storageFile.getParentFile().mkdirs()) {
			try {
				final FileOutputStream fileOut = new FileOutputStream(storageFile);
				final ObjectOutputStream out = new ObjectOutputStream(fileOut);
				out.writeObject(this.data);
				out.close();
				fileOut.close();
			} catch (final IOException e) {
				Log.error("Could not save storage: " + storageFile + Log.getExceptionAppendix(e));
			}
		} else {
			Log.error("Could not access or create storage directory: " + storageFile.getParentFile().getAbsolutePath());
		}
	}

	private boolean cleanUp() {
		final List<String> toRemove = new ArrayList<>();
		for (final String key : this.data.getRunAnswerMap().keySet()) {
			final TaskAnswer taskAnswer = this.data.getRunAnswerMap().get(key);
			if (!taskAnswer.getAnswerFile().exists()) {
				toRemove.add(key);
			}
		}
		for (final String key : toRemove) {
			this.data.getRunAnswerMap().remove(key);
		}
		return !toRemove.isEmpty();
	}

	public void reset() {
		this.data = new Data();
	}
}