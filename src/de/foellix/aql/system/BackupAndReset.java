package de.foellix.aql.system;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.foellix.aql.Log;
import de.foellix.aql.helper.ZipHelper;

public class BackupAndReset {
	private static File storageFolder = new File("data/storage");

	public static boolean backup(File storageFolder) {
		BackupAndReset.storageFolder = storageFolder;
		return backup();
	}

	public static boolean backup() {
		final SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy-HH_mm_ss");
		final Date currentTime = new Date();
		try {
			final File zipFile = new File("data/storage_backup_" + formatter.format(currentTime) + ".zip");
			ZipHelper.zip(storageFolder, zipFile);
			Log.msg("Successfully backuped storage to: " + zipFile.getAbsolutePath(), Log.NORMAL);
			return true;
		} catch (final IOException e) {
			Log.error("Something went wrong while backing up the storage: " + e.getMessage());
			return false;
		}
	}

	public static void reset(File storageFolder) {
		BackupAndReset.storageFolder = storageFolder;
		reset();
	}

	public static void reset() {
		boolean failed = false;
		for (final File file : storageFolder.listFiles()) {
			if (file.getName().endsWith(".xml")) {
				if (file.exists() && !file.delete()) {
					failed = true;
				}
			}
		}
		File storageFile = new File(storageFolder, "storageParts.ser");
		if (storageFile.exists() && !storageFile.delete()) {
			failed = true;
		}
		storageFile = new File(storageFolder, "storagePreprocessors.ser");
		if (storageFile.exists() && !storageFile.delete()) {
			failed = true;
		}

		Storage.getInstance().reset();

		if (failed) {
			Log.warning("Could not completely reset storage!");
		} else {
			Log.msg("Successfully reset storage!", Log.NORMAL);
		}
	}
}
