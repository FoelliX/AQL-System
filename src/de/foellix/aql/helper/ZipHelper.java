package de.foellix.aql.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
			for (final File childFile : children) {
				zipFile(childFile, fileName + "/" + childFile.getName(), zipOut, zipFile);
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
}
