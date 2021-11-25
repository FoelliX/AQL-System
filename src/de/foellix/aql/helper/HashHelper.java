package de.foellix.aql.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Hash;
import de.foellix.aql.datastructure.Hashes;

public class HashHelper {
	public static final String HASH_TYPE_MD5 = "MD5";
	public static final String HASH_TYPE_SHA1 = "SHA-1";
	public static final String HASH_TYPE_SHA256 = "SHA-256";

	public static String md5Hash(final File file) {
		return hash(file, HASH_TYPE_MD5);
	}

	public static String md5Hash(final String string) {
		return md5Hash(string, false);
	}

	public static String md5Hash(final String string, boolean replaceSlashes) {
		return hash(string, HASH_TYPE_MD5, replaceSlashes);
	}

	public static String sha1Hash(final File file) {
		return hash(file, HASH_TYPE_SHA1);
	}

	public static String sha1Hash(final String string) {
		return sha1Hash(string, false);
	}

	public static String sha1Hash(final String string, boolean replaceSlashes) {
		return hash(string, HASH_TYPE_SHA1, replaceSlashes);
	}

	public static String sha256Hash(final File file) {
		return hash(file, HASH_TYPE_SHA256);
	}

	public static String sha256Hash(final String string) {
		return sha256Hash(string, false);
	}

	public static String sha256Hash(final String string, boolean replaceSlashes) {
		return hash(string, HASH_TYPE_SHA256, replaceSlashes);
	}

	public static String hash(final File file, final String algorithm) {
		if (file.isDirectory()) {
			Log.warning("File for hash creation is a directory (" + file.getAbsolutePath()
					+ "). Using only its name (\"" + file.getName() + "\") for hash creation.");
			return hash(file.getName(), algorithm);
		}
		try {
			final MessageDigest digest = MessageDigest.getInstance(algorithm);
			final InputStream is = new FileInputStream(file);

			final byte[] buffer = new byte[8192];
			int read;
			try {
				while ((read = is.read(buffer)) > 0) {
					digest.update(buffer, 0, read);
				}
				final byte[] hash = digest.digest();
				return makeOutput(hash);
			} catch (final IOException e) {
				throw new RuntimeException("Unable to process file.", e);
			} finally {
				try {
					is.close();
				} catch (final IOException e) {
					Log.warning("Could not close a file's input stream: " + file.getAbsolutePath());
					return null;
				}
			}
		} catch (final FileNotFoundException | NoSuchAlgorithmException e) {
			Log.error("Could not find file for hash creation: " + file.getAbsolutePath());
			return null;
		}
	}

	public static String hash(String string, final String algorithm) {
		return hash(string, algorithm, false);
	}

	public static String hash(String string, final String algorithm, boolean replaceSlashes) {
		if (replaceSlashes) {
			string = string.replace("\\", "/");
		}
		try {
			final MessageDigest digest = MessageDigest.getInstance(algorithm);
			final byte[] hash = digest.digest(string.getBytes());
			return makeOutput(hash);
		} catch (final NoSuchAlgorithmException e) {
			return null;
		}
	}

	private static String makeOutput(final byte[] byteStr) {
		final BigInteger bigInt = new BigInteger(1, byteStr);
		String output = bigInt.toString(16);
		output = String.format("%32s", output).replace(' ', '0');
		return output;
	}

	public static String getHash(Hashes hashes, String type) {
		for (final Hash hash : hashes.getHash()) {
			if (hash.getType().equals(type)) {
				return hash.getValue();
			}
		}
		return null;
	}
}
