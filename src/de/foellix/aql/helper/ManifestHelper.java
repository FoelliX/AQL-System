package de.foellix.aql.helper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import de.foellix.aql.Log;
import net.dongliu.apk.parser.ApkFile;

public class ManifestHelper {
	private final Map<String, ManifestInfo> data;

	private static ManifestHelper instance = new ManifestHelper();

	private ManifestHelper() {
		this.data = new HashMap<>();
	}

	public static ManifestHelper getInstance() {
		return instance;
	}

	synchronized public ManifestInfo getManifest(File apkFile) {
		ManifestInfo manifestInfo = this.data.get(HashHelper.md5Hash(apkFile) + HashHelper.sha1Hash(apkFile));
		if (manifestInfo == null) {
			final String manifest;

			// Instantiate Apk-Parser
			try (ApkFile apkParserFile = new ApkFile(apkFile)) {
				manifest = apkParserFile.getManifestXml();
			} catch (final Exception e) {
				Log.error("No valid manifest could be found in: \"" + apkFile.getAbsolutePath() + "\"");
				return null;
			}

			manifestInfo = new ManifestInfo(manifest);
			this.data.put(HashHelper.md5Hash(apkFile) + HashHelper.sha1Hash(apkFile), manifestInfo);
		}
		return manifestInfo;
	}
}