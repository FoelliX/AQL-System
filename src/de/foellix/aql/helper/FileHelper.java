package de.foellix.aql.helper;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.ws.rs.core.MediaType;

import de.foellix.aql.Log;
import de.foellix.aql.system.task.Task;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestException;

public class FileHelper {
	public static final String FILE_ENDING_TXT = ".txt";
	public static final String FILE_ENDING_XML = ".xml";
	public static final String FILE_ENDING_AQL = FILE_ENDING_XML;
	public static final String FILE_ENDING_APK = ".apk";

	private static final String FILE_ENDING_NULL = "###null###";
	private static final String CLASSES_DEX_FILENAME = "classes.dex";
	private static final String ANDROID_MANIFEST_FILENAME = "AndroidManifest.xml";

	private static final int TEMP_FILE_ID_LENGTH = 5;
	private static final int UNIQUE_FILE_ID_LENGTH = 3;

	private static Map<String, Integer> tempFileID = new HashMap<>();
	private static Lock lock = new ReentrantLock();

	private static File tempDirectory = new File("data", "temp");
	private static File converterDirectory = new File("data", "converter");
	private static File answersDirectory = new File("answers");
	private static final File queriesDirectory = new File("queries");

	public static void initializeFileSystem() {
		setTempDirectory(tempDirectory);
		setConverterDirectory(converterDirectory);
		setAnswersDirectory(answersDirectory);
	}

	public static File getTempDirectory() {
		return tempDirectory;
	}

	public static File getConverterDirectory() {
		return converterDirectory;
	}

	public static File getAnswersDirectory() {
		return answersDirectory;
	}

	public static void setTempDirectory(File tempDirectory) {
		FileHelper.tempDirectory = tempDirectory;
		if (!tempDirectory.exists()) {
			tempDirectory.mkdirs();
		}
	}

	public static void setConverterDirectory(File converterDirectory) {
		FileHelper.converterDirectory = converterDirectory;
		if (!converterDirectory.exists()) {
			converterDirectory.mkdirs();
		}
	}

	public static void setAnswersDirectory(File answersDirectory) {
		FileHelper.answersDirectory = answersDirectory;
		if (!answersDirectory.exists()) {
			answersDirectory.mkdirs();
		}

		// Also create Queries directory if it does not exist
		if (!queriesDirectory.exists()) {
			queriesDirectory.mkdirs();
		}
	}

	/**
	 * Empties a directory and deletes it
	 *
	 * @param directory
	 *            the directory to be deleted
	 * @return
	 */
	public static boolean deleteDir(File directory) {
		if (!directory.exists()) {
			return true;
		}
		if (directory.isDirectory()) {
			for (final File temp : directory.listFiles()) {
				if (!deleteDir(temp)) {
					Log.warning("Failed to delete \"" + temp.getAbsolutePath() + "\"!");
				}
			}
		}
		return directory.delete();
	}

	/**
	 * Returns a unique file in an AQL-Systems's temporary data folder with no file-ending.
	 *
	 * @return unique temporary file
	 */
	public static File getTempFile() {
		return getTempFile(null);
	}

	/**
	 * Returns a unique file in an AQL-Systems's temporary data folder.
	 *
	 * @param fileEnding
	 *            Must follow the format: *.ending (E.g.: .xml) - Might be null for no ending.
	 * @return unique temporary file
	 */
	public static File getTempFile(String fileEnding) {
		setTempDirectory(tempDirectory); // Initialize directory if used without AQL-System

		if (fileEnding == null) {
			fileEnding = FILE_ENDING_NULL;
		}
		lock.lock();
		int id = (tempFileID.containsKey(fileEnding) ? tempFileID.get(fileEnding) + 1 : 0);

		File uniqueFile;
		do {
			uniqueFile = new File(tempDirectory, "temp_" + Helper.addZeroDigits(++id, TEMP_FILE_ID_LENGTH)
					+ (fileEnding != FILE_ENDING_NULL ? fileEnding : ""));
		} while (uniqueFile.exists());

		tempFileID.put(fileEnding, id);
		lock.unlock();

		return uniqueFile;
	}

	/**
	 * Unifies filenames
	 *
	 * @param file
	 *            - File with filename such as randomFile-0.txt
	 * @return randomFile-X.txt with X such that the file does not exists, yet.
	 */
	public static File makeUnique(File file) {
		String extension = "";
		if (file.getName().contains(".")) {
			extension = file.getName().substring(file.getName().lastIndexOf("."));
		}
		final String filename = file.getAbsolutePath().substring(0,
				file.getAbsolutePath().length() - (extension.length() + 1));

		int id = 0;
		do {
			file = new File(filename + Helper.addZeroDigits(++id, UNIQUE_FILE_ID_LENGTH) + extension);
		} while (file.exists());

		return file;
	}

	/**
	 * Waits 10 seconds for the file to become available.
	 *
	 * @param resultWithAsteriskFile
	 *            The file to become available with placeholders ('*').
	 * @return The final file available (or null)
	 */
	public static File waitForResult(File resultWithAsteriskFile, Task task) {
		for (int i = 0; i <= 10; i++) {
			final File result = FileHelper.findFileWithAsterisk(resultWithAsteriskFile);
			if (result.exists()) {
				Log.msg("Result available: " + result.getAbsolutePath(), Log.DEBUG_DETAILED);
				return result;
			} else {
				try {
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					Log.warning("Interrupted while waiting for result. Trying to continue.");
				}
			}
		}
		return null;
	}

	/**
	 * Find any file whose filename matches the input file but with replacements for any occurrence of '*'.
	 *
	 * @param file
	 *            The file to match
	 * @return The first matching file
	 */
	public static File findFileWithAsterisk(File file) {
		if (!file.getAbsolutePath().contains("*")) {
			return file;
		}

		final String[] needles = file.getName().split("\\*");

		file = new File(file.getAbsolutePath().replace("*", "_"));

		boolean didNotExist = false;
		if (!file.exists()) {
			didNotExist = true;
			try {
				file.createNewFile();
			} catch (final IOException e) {
				Log.error("Analysis result could not be found or written: " + file.getAbsolutePath());
			}
		}

		if (file.getParentFile().listFiles() != null) {
			for (final File candidate : file.getParentFile().listFiles()) {
				if (didNotExist && candidate.equals(file)) {
					continue;
				}
				boolean valid = true;
				for (final String needle : needles) {
					if (!candidate.getName().contains(needle)) {
						valid = false;
					}
				}
				if (valid) {
					if (didNotExist) {
						file.delete();
					}
					return candidate;
				}
			}
		}
		if (didNotExist) {
			file.delete();
		}
		return file;
	}

	public static List<File> findFilesWithAsteriskInParent(File directory) {
		final List<File> filesFound = new ArrayList<>();
		File parent = directory.getParentFile();
		if (parent == null) {
			parent = new File(".");
			parent = new File(parent.getAbsolutePath().substring(0, parent.getAbsolutePath().length() - 1));
		}

		final String needle = directory.getName().replace("*", ".*");
		for (final File file : parent.listFiles()) {
			if (file.getName().matches(needle)) {
				filesFound.add(file);
			}
		}

		if (filesFound.isEmpty()) {
			Log.warning("No files found when replacing asterisk in: " + directory.getAbsolutePath());
		}
		return filesFound;
	}

	public static List<File> searchRecursively(File resultFolder, String extension) {
		final List<File> returnList = new ArrayList<>();
		for (final File file : resultFolder.listFiles()) {
			if (file.isDirectory()) {
				returnList.addAll(FileHelper.searchRecursively(file, extension));
			} else {
				if (file.getAbsolutePath().endsWith(extension)) {
					returnList.add(file);
				}
			}
		}
		return returnList;
	}

	public static String getRawContent(File answerFile) throws IOException {
		return Helper.replaceAllWhiteSpaceChars(new String(Files.readAllBytes(answerFile.toPath())));
	}

	public static String getApkFileName(File apkFile) {
		final String name = apkFile.getName();
		final String ending = "." + Helper.cut(name, ".", Helper.OCCURENCE_LAST);
		if (ending.equalsIgnoreCase(FILE_ENDING_APK)) {
			return Helper.cutFromStart(name, ".", Helper.OCCURENCE_LAST);
		} else {
			return name;
		}
	}

	public static boolean downloadFile(String URL, File destination) {
		return downloadFile(URL, destination, null);
	}

	public static boolean downloadFile(String url, File destination, String filename) {
		if (filename == null) {
			filename = destination.getName();
		}
		try {
			Log.msg("Downloading " + filename + " from \"" + url + "\".", Log.NORMAL);
			final InputStream is = new URL(url).openStream();
			Files.copy(is, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
			Log.msg("Downloaded " + filename + " saved as \"" + destination.getAbsolutePath() + "\".", Log.NORMAL);
			return true;
		} catch (final IOException e) {
			Log.warning("Could not download " + filename + " from \"" + url + "\" to \"" + destination + "\"."
					+ Log.getExceptionAppendix(e));
			return false;
		}
	}

	public static final boolean getConfigFromWebService(String url, String username, String password,
			File destination) {
		try {
			Log.msg("Getting config from: " + url, Log.NORMAL);
			Unirest.config().connectTimeout(10000).socketTimeout(30000);
			final HttpRequestWithBody request = Unirest.post(url).header("Accept", MediaType.TEXT_XML)
					.queryString("username", username).queryString("password", password);
			final HttpResponse<String> responseString = request.asString();
			try (FileWriter writer = new FileWriter(destination)) {
				final String body = responseString.getBody();
				if (body.contains("<config />") || body.contains("<config/>")) {
					Log.warning("Configuration accuired is empty! Please check \"" + destination.getAbsolutePath()
							+ "\" for more information.");
				}
				writer.append(body);
			}
			Unirest.shutDown();
			return true;
		} catch (final UnirestException | IOException e) {
			Log.warning("Could not download configuration from \"" + url + "\"." + Log.getExceptionAppendix(e));
			return false;
		}
	}

	/**
	 * Checks whether the given file contains a Android manifest and a classes.dex file.
	 *
	 * @param file
	 * @return true if both are contained
	 */
	public static boolean isAPK(File file) {
		try (ZipInputStream zipStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)))) {
			boolean dexFound = false;
			boolean manifestFound = false;
			ZipEntry zipItem;
			while ((zipItem = zipStream.getNextEntry()) != null) {
				if (!dexFound && zipItem.getName().equalsIgnoreCase(CLASSES_DEX_FILENAME)) {
					dexFound = true;
				} else if (!manifestFound && zipItem.getName().equalsIgnoreCase(ANDROID_MANIFEST_FILENAME)) {
					manifestFound = true;
				}
				if (dexFound && manifestFound) {
					return true;
				}
			}
		} catch (final IOException e) {
			Log.warning("Could not check if \"" + file + "\" is a valid APK file." + Log.getExceptionAppendix(e));
		}
		return false;
	}

	/**
	 * Gets properties of a file, e.g. its creation time.
	 *
	 * @param file
	 * @return BasicFileAttributes - The object containing the file properties
	 */
	public static BasicFileAttributes getFileProperties(File file) {
		if (file.exists()) {
			try {
				return Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			} catch (final IOException e) {
				Log.error("Could not get file properties: " + file.getAbsolutePath() + Log.getExceptionAppendix(e));
			}
		}
		return null;
	}
}