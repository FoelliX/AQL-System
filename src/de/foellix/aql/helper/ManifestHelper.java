package de.foellix.aql.helper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.foellix.aql.Log;
import net.dongliu.apk.parser.ApkFile;

public class ManifestHelper {
	private final Map<String, ApkFile> data;

	private static ManifestHelper instance = new ManifestHelper();

	private ManifestHelper() {
		this.data = new HashMap<>();
	}

	public static ManifestHelper getInstance() {
		return instance;
	}

	synchronized public String getManifestRAW(File apkFile) {
		ApkFile apkParserFile = null;
		try {
			if (apkFile.exists()) {
				final String hash = HashHelper.sha256Hash(apkFile);
				apkParserFile = this.data.get(hash);
				if (apkParserFile == null) {
					// Instantiate ApkFile from ApkParser
					try {
						apkParserFile = new ApkFile(apkFile);
						this.data.put(hash, apkParserFile);
					} catch (final Exception e) {
						Log.error("apk-parser failed to parse: \"" + apkFile.getAbsolutePath() + "\""
								+ Log.getExceptionAppendix(e));
						return null;
					}
				}
				try {
					return apkParserFile.getManifestXml();
				} catch (final IOException e) {
					Log.error("No valid manifest could be found in: \"" + apkFile.getAbsolutePath() + "\""
							+ Log.getExceptionAppendix(e));
					return null;
				}
			} else {
				Log.error("Apk file does not exist: \"" + apkFile.getAbsolutePath() + "\"");
				return null;
			}
		} finally {
			if (apkParserFile != null) {
				try {
					apkParserFile.close();
				} catch (final IOException e) {
					Log.error("Read APK information from \"" + apkFile.getAbsolutePath()
							+ "\" but could not close access!" + Log.getExceptionAppendix(e));
				}
			}
		}
	}

	public ApkFile getApkParserFile(File apkFile) {
		final String hash = HashHelper.sha256Hash(apkFile);
		if (!this.data.containsKey(hash)) {
			getManifestRAW(apkFile);
		}
		return this.data.get(hash);
	}

	public ManifestInfo getManifest(File apkFile) {
		return new ManifestInfo(getManifestRAW(apkFile));
	}
}