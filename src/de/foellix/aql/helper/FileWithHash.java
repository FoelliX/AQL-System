package de.foellix.aql.helper;

import java.io.File;

import de.foellix.aql.datastructure.Hash;

public class FileWithHash {
	private File file;
	private Hash hash;

	public FileWithHash(String file) {
		this(new File(file));
	}

	public FileWithHash(File file) {
		this(file, HashHelper.HASH_TYPE_SHA256);
	}

	public FileWithHash(File file, String hashAlgorithm) {
		this.file = file;
		this.hash = new Hash();
		this.hash.setType(hashAlgorithm);
		if (file.exists()) {
			this.hash.setValue(HashHelper.hash(file, hashAlgorithm));
		} else {
			this.hash.setValue(HashHelper.hash(file.getAbsolutePath(), hashAlgorithm));
		}
	}

	public FileWithHash(File file, Hash hash) {
		this.file = file;
		this.hash = hash;
	}

	public File getFile() {
		return this.file;
	}

	public Hash getHash() {
		return this.hash;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setHash(Hash hash) {
		this.hash = hash;
	}

	@Override
	public String toString() {
		return this.file.toString() + " (" + this.hash.getType() + ": " + this.hash.getValue() + ")";
	}
}