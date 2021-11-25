package de.foellix.aql.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import org.junit.jupiter.api.Test;

public class FileHelperTest {
	@Test
	void getApkFileNameTest1() {
		final File apkFile = new File("/path/to/file.apk");
		final String actual = FileHelper.getApkFileName(apkFile);
		assertEquals("file", actual);
	}

	@Test
	void getApkFileNameTest2() {
		final File apkFile = new File("/path/to/00000007");
		final String actual = FileHelper.getApkFileName(apkFile);
		assertEquals("00000007", actual);
	}
}
