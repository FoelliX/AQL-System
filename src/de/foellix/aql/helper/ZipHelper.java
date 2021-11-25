package de.foellix.aql.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import de.foellix.aql.Log;

public class ZipHelper {
	public static void zip(File fileToZip, File zipFile) throws IOException {
		final FileOutputStream fos = new FileOutputStream(zipFile);
		final ZipOutputStream zipOut = new ZipOutputStream(fos);

		zipFile(fileToZip, fileToZip.getName(), zipOut, zipFile);

		zipOut.close();
		fos.close();
	}

	private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut, File zipFile)
			throws IOException {
		if (fileToZip.equals(zipFile)) {
			return;
		}
		if (fileToZip.isHidden()) {
			return;
		}
		if (fileToZip.isDirectory()) {
			final File[] children = fileToZip.listFiles();
			if (children != null) {
				for (final File childFile : children) {
					zipFile(childFile, fileName + "/" + childFile.getName(), zipOut, zipFile);
				}
			}
			return;
		}
		final FileInputStream fis = new FileInputStream(fileToZip);
		final ZipEntry zipEntry = new ZipEntry(fileName);
		zipOut.putNextEntry(zipEntry);
		final byte[] bytes = new byte[1024];
		int length;
		while ((length = fis.read(bytes)) >= 0) {
			zipOut.write(bytes, 0, length);
		}
		fis.close();
	}

	public static void unzip(File zipFile, File destinationDirectory) {
		unzip(zipFile, destinationDirectory, true, null);
	}

	public static void unzip(File zipFile, File destinationDirectory, boolean mkdirs) {
		unzip(zipFile, destinationDirectory, mkdirs, null);
	}

	public static void unzip(File zipFile, File destinationDirectory, boolean mkdirs, String fileMatcher) {
		if (mkdirs) {
			destinationDirectory.mkdirs();
		}
		if (destinationDirectory.exists()) {
			final byte[] buffer = new byte[1024];
			try {
				final ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
				ZipEntry zipEntry = zis.getNextEntry();
				while (zipEntry != null) {
					final File newFile = getUnzipFile(destinationDirectory, zipEntry);
					Log.msg("Unzipping: " + newFile.getAbsolutePath(), Log.DEBUG_DETAILED);
					if (zipEntry.isDirectory()) {
						if (mkdirs) {
							newFile.mkdirs();
						}
					} else {
						if (fileMatcher == null || newFile.getName().matches(fileMatcher)) {
							if (newFile.getParentFile() != null && !newFile.getParentFile().exists()) {
								newFile.getParentFile().mkdirs();
							}
							if (newFile.exists()) {
								newFile.delete();
							}
							final FileOutputStream fos = new FileOutputStream(newFile);
							int len;
							while ((len = zis.read(buffer)) > 0) {
								fos.write(buffer, 0, len);
							}
							fos.close();
						}
					}
					zipEntry = zis.getNextEntry();
				}
				zis.closeEntry();
				zis.close();
			} catch (final IOException e) {
				Log.error("Could not unzip \"" + zipFile.getAbsolutePath() + "\" into \"" + destinationDirectory + "\"."
						+ Log.getExceptionAppendix(e));
			}
		} else {
			if (mkdirs) {
				Log.error("Could not unzip \"" + zipFile.getAbsolutePath()
						+ "\". Destination directory could not be created: " + destinationDirectory);
			} else {
				Log.error("Could not unzip \"" + zipFile.getAbsolutePath()
						+ "\". Destination directory does not exist: " + destinationDirectory);
			}
		}
	}

	private static File getUnzipFile(File destinationDir, ZipEntry zipEntry) throws IOException {
		final File destFile = new File(destinationDir, zipEntry.getName());

		final String destDirPath = destinationDir.getCanonicalPath();
		final String destFilePath = destFile.getCanonicalPath();

		if (!destFilePath.startsWith(destDirPath + File.separator)) {
			throw new IOException("Entry is outside of the destination directory: " + zipEntry.getName());
		}

		return destFile;
	}
}
