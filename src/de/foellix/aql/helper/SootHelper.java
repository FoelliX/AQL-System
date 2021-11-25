package de.foellix.aql.helper;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import de.foellix.aql.ConditionalLogSilencer;
import de.foellix.aql.Log;
import de.foellix.aql.LogSilencer;
import de.foellix.aql.config.ConfigHandler;
import soot.Scene;
import soot.options.Options;

public class SootHelper {
	public static final File ANDROID_JAR_FORCED_FILE = new File("data/android.jar");
	private static final String[] DEFAULT_EXCLUDES = new String[] { "android.support.*", "android.arch.*",
			"com.google.*", "java.*" }; // Removed "com.android.*" on 06.07.2021
	private static final String ANDROID_JAR_URL = "https://github.com/Sable/android-platforms/raw/master/android-30/android.jar";

	private static String[] excludes = DEFAULT_EXCLUDES;
	private static boolean noConfig = false;

	public static boolean isNoConfig() {
		return noConfig;
	}

	public static void setNoConfig(boolean noConfig) {
		SootHelper.noConfig = noConfig;
	}

	public static String[] getExcludes() {
		return excludes;
	}

	public static void setExcludes(String[] excludes) {
		SootHelper.excludes = excludes;
	}

	public static void resetExcludes() {
		SootHelper.excludes = DEFAULT_EXCLUDES;
	}

	public static Scene getScene(File apkFile) {
		return getScene(apkFile, null);
	}

	public static Scene getScene(File apkFile, File androidPlatforms) {
		// Reinitialize
		soot.G.reset();

		// Setup Soot
		Options.v().set_src_prec(Options.src_prec_apk);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_include_all(true);
		Options.v().set_process_multiple_dex(true);
		Options.v().set_process_dir(Collections.singletonList(apkFile.getAbsolutePath()));
		Options.v().set_exclude(Arrays.asList(excludes));

		// Android Platform
		if (androidPlatforms != null) {
			if (!androidPlatforms.exists()) {
				Log.warning("Given Android platforms directory does not exist: " + androidPlatforms.getAbsolutePath());
			}
			Options.v().set_android_jars(androidPlatforms.getAbsolutePath());
		} else if (!noConfig && ConfigHandler.getInstance().getConfig() != null
				&& ConfigHandler.getInstance().getConfig().getAndroidPlatforms() != null
				&& !ConfigHandler.getInstance().getConfig().getAndroidPlatforms().isEmpty()) {
			Options.v().set_android_jars(ConfigHandler.getInstance().getConfig().getAndroidPlatforms());
		} else {
			if (!ANDROID_JAR_FORCED_FILE.exists()) {
				FileHelper.downloadFile(ANDROID_JAR_URL, ANDROID_JAR_FORCED_FILE, "android.jar");
			}
			Options.v().set_force_android_jar(ANDROID_JAR_FORCED_FILE.getAbsolutePath());
		}

		// Run Soot
		try (LogSilencer s = new ConditionalLogSilencer(Log.DEBUG)) {
			Scene.v().loadNecessaryClasses();
		}

		// Return scene
		return Scene.v();
	}
}