package de.foellix.aql.helper;

public class ManifestInfo {
	private final String manifest;
	private final String pkgName;
	private final String appName;

	public ManifestInfo(final String manifest) {
		this.manifest = manifest;
		this.pkgName = Helper.cut(manifest, "package=\"", "\"");
		this.appName = Helper.cut(this.pkgName, ".", Helper.OCCURENCE_LAST);
	}

	public String getManifest() {
		return this.manifest;
	}

	public String getPkgName() {
		return this.pkgName;
	}

	public String getAppName() {
		return this.appName;
	}
}
