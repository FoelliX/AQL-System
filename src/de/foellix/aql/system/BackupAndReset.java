package de.foellix.aql.system;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.foellix.aql.Log;
import de.foellix.aql.helper.FileHelper;
import de.foellix.aql.helper.ZipHelper;
import de.foellix.aql.system.storage.Storage;
import de.foellix.aql.ui.gui.viewer.web.ViewerWeb;

public class BackupAndReset {
	public static boolean backup() {
		return backup(Storage.DEFAULT_STORAGE_DIRECTORY);
	}

	public static boolean backup(File storageDirectory) {
		final SimpleDateFormat formatter = new SimpleDateFormat("dd_MM_yyyy-HH_mm_ss");
		final Date currentTime = new Date();
		try {
			final File zipFile = new File("data/storage_backup_" + formatter.format(currentTime) + ".zip");
			ZipHelper.zip(storageDirectory, zipFile);
			Log.msg("Successfully backuped storage to: " + zipFile.getAbsolutePath(), Log.NORMAL);
			return true;
		} catch (final IOException e) {
			Log.error("Something went wrong while backing up the storage: " + e.getMessage());
			return false;
		}
	}

	public static void reset() {
		reset(Storage.DEFAULT_STORAGE_DIRECTORY);
	}

	public static void reset(File storageDirectory) {
		boolean failed = false;

		// Reset (delete) storage directory
		if (storageDirectory.exists() && !FileHelper.deleteDir(storageDirectory)) {
			failed = true;
		}

		// Reset storage object
		Storage.getInstance().reset();

		// Re-create storage directory
		storageDirectory.mkdir();

		// Log
		if (failed) {
			Log.warning("Could not completely reset storage!");
		} else {
			Log.msg("Successfully reset storage!", Log.NORMAL);
		}
	}

	public static void resetOutputDirectories() {
		// data/temp
		if (FileHelper.getTempDirectory().exists()) {
			FileHelper.deleteDir(FileHelper.getTempDirectory());
		}
		FileHelper.getTempDirectory().mkdir();

		// data/gui/web/temp
		if (ViewerWeb.TEMP_DIRECTORY.exists()) {
			FileHelper.deleteDir(ViewerWeb.TEMP_DIRECTORY);
		}
		ViewerWeb.TEMP_DIRECTORY.mkdir();

		// answers
		if (FileHelper.getAnswersDirectory().exists()) {
			FileHelper.deleteDir(FileHelper.getAnswersDirectory());
		}
		FileHelper.getAnswersDirectory().mkdir();
	}
}