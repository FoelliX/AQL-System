package de.foellix.aql.helper.tools;

import java.io.File;
import java.io.IOException;

import de.foellix.aql.Log;
import de.foellix.aql.system.ProcessWrapper;

public class APKSigner {
	private static final int SYSTEM_UNKNOWN = -1;
	private static final int SYSTEM_WINDOWS = 0;
	private static final int SYSTEM_LINUX_BASED = 1;
	private static final String SCRIPT = "data/sign/sign";
	private static final String TEMP_APK = "data/temp/tempApkForSigning.apk";
	private static final String DEFAULT_KEYSTORE_PASSWORD = "AQL123";
	private static final String DEFAULT_KEYSTORE = "data/sign/keyForSigning.jks";

	private File outputApk = null;
	private File script = null;
	private File zipAligner = null;
	private File apkSigner = null;
	private int system = SYSTEM_UNKNOWN;
	private File directory = null;

	public APKSigner(File sdkBuildTools) {
		this.system = getSystem();
		if (this.system == SYSTEM_WINDOWS) {
			init(new File(sdkBuildTools, "zipalign.exe"), new File(sdkBuildTools, "apksigner.bat"));
		} else if (this.system == SYSTEM_WINDOWS) {
			init(new File(sdkBuildTools, "zipalign"), new File(sdkBuildTools, "apksigner"));
		} else {
			Log.error("Cannot sign APK since the operating system could not be identified.");
		}
	}

	public APKSigner(File zipAligner, File apkSigner) {
		init(zipAligner, apkSigner);
	}

	private void init(File zipAligner, File apkSigner) {
		this.zipAligner = zipAligner;
		this.apkSigner = apkSigner;

		this.system = getSystem();
		if (this.system == SYSTEM_UNKNOWN) {
			if (zipAligner.getName().endsWith(".exe")) {
				Log.warning("Could not detext operating system. Assuming a Windows system.");
				this.system = SYSTEM_WINDOWS;
			} else {
				Log.warning("Could not detext operating system. Assuming a Linux based system.");
				this.system = SYSTEM_LINUX_BASED;
			}
		}
	}

	private int getSystem() {
		if (this.system != SYSTEM_UNKNOWN) {
			return this.system;
		} else {
			final String system = System.getProperty("os.name").toLowerCase();
			if (system.contains("win")) {
				return SYSTEM_WINDOWS;
			} else if (system.contains("mac") || system.contains("nix") || system.contains("nux")
					|| system.contains("aix")) {
				return SYSTEM_LINUX_BASED;
			}
			return SYSTEM_UNKNOWN;
		}
	}

	public boolean sign(File apkToSign) {
		return sign(apkToSign, DEFAULT_KEYSTORE_PASSWORD);
	}

	public boolean sign(File apkToSign, String keyStorePassword) {
		File keyStore;
		if (this.directory == null) {
			keyStore = new File(DEFAULT_KEYSTORE);
		} else {
			keyStore = new File(this.directory, DEFAULT_KEYSTORE);
		}
		return sign(apkToSign, keyStorePassword, keyStore);
	}

	public boolean sign(File apkToSign, String keyStorePassword, File keyStore) {
		if (this.directory == null) {
			if (this.system == SYSTEM_WINDOWS) {
				this.script = new File(SCRIPT + ".bat");
			} else {
				this.script = new File(SCRIPT + ".sh");
			}
		} else {
			if (this.system == SYSTEM_WINDOWS) {
				this.script = new File(this.directory, SCRIPT + ".bat");
			} else {
				this.script = new File(this.directory, SCRIPT + ".sh");
			}
		}

		final String[] cmd = new String[8];
		cmd[0] = this.script.getAbsolutePath();
		cmd[1] = this.zipAligner.getAbsolutePath();
		cmd[2] = this.apkSigner.getAbsolutePath();
		cmd[3] = keyStore.getAbsolutePath();
		cmd[4] = keyStorePassword;
		if (this.directory == null) {
			cmd[5] = new File(TEMP_APK).getAbsolutePath();
		} else {
			cmd[5] = new File(this.directory, TEMP_APK).getAbsolutePath();
		}
		cmd[6] = apkToSign.getAbsolutePath();
		if (this.outputApk == null) {
			cmd[7] = cmd[6];
		} else {
			cmd[7] = this.outputApk.getAbsolutePath();
		}

		try {
			final ProcessBuilder pb = new ProcessBuilder(cmd);
			if (this.directory != null) {
				pb.directory(this.directory);
			}
			if (Log.logIt(Log.DEBUG)) {
				final StringBuilder sb = new StringBuilder();
				for (final String part : cmd) {
					if (sb.length() > 0) {
						sb.append(" ");
					}
					sb.append(part);
				}
				Log.msg("Start signing \"" + apkToSign + "\" by running: " + sb.toString() + " (in: "
						+ (this.directory != null ? this.directory.getAbsolutePath() : "local directory") + ")",
						Log.DEBUG);
			}
			final ProcessWrapper pw = new ProcessWrapper(pb.start());
			if (pw.waitFor() != 0) {
				Log.error("Something went wrong while signing \"" + apkToSign + "\". \"" + this.script
						+ "\" did not terminate successfully!");
				return false;
			}
			Log.msg("Finished signing \"" + apkToSign + "\".", Log.DEBUG);
			return true;
		} catch (final IOException e) {
			Log.error("Something went wrong while signing \"" + apkToSign + "\"." + Log.getExceptionAppendix(e));
			return false;
		}
	}

	/**
	 * If the script is located in "/path/to/AQL-System/data/signing" and the program is not run within "/path/to/AQL-System" the directory must manually be set to "/path/to/AQL-System".
	 *
	 * @param directory
	 *            must be set to "/path/to/AQL-System"
	 */
	public void setDirectory(File directory) {
		this.directory = directory;
	}

	public void setOutputApk(File outputApk) {
		this.outputApk = outputApk;
	}
}