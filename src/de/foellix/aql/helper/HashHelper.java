package de.foellix.aql.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.Hash;
import de.foellix.aql.datastructure.Hashes;
import de.foellix.aql.datastructure.IQuestionNode;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.QuestionPart;
import de.foellix.aql.datastructure.Reference;

public class HashHelper {
	// Hash-Creator
	public static String createHash(final Tool tool, final IQuestionNode question) {
		final String hash = Helper.toRAW(tool) + question.toRAW(tool.isExternal());
		return HashHelper.sha256Hash(hash);
	}

	public static String createHash(final Tool tool, final App app) {
		final String hash = Helper.toRAW(tool) + Helper.toRAW(app);
		return HashHelper.sha256Hash(hash);
	}

	public static String createGenericHash(final IQuestionNode question) {
		final IQuestionNode copy = Helper.copy(question);
		for (final QuestionPart part : copy.getAllQuestionParts()) {
			for (final Reference ref : part.getAllReferences()) {
				ref.setClassname(null);
				ref.setMethod(null);
				ref.setStatement(null);
			}
		}
		final String hash = copy.toRAW(false);
		return HashHelper.sha256Hash(hash);
	}

	// Hashes
	public static String md5Hash(final File file) {
		return hash(file, KeywordsAndConstants.HASH_TYPE_MD5);
	}

	public static String md5Hash(final String string) {
		return hash(string, KeywordsAndConstants.HASH_TYPE_MD5);
	}

	public static String sha1Hash(final File file) {
		return hash(file, KeywordsAndConstants.HASH_TYPE_SHA1);
	}

	public static String sha1Hash(final String string) {
		return hash(string, KeywordsAndConstants.HASH_TYPE_SHA1);
	}

	public static String sha256Hash(final File file) {
		return hash(file, KeywordsAndConstants.HASH_TYPE_SHA256);
	}

	public static String sha256Hash(final String string) {
		return hash(string, KeywordsAndConstants.HASH_TYPE_SHA256);
	}

	private static String hash(final File file, final String algorithm) {
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
					return null;
				}
			}
		} catch (final FileNotFoundException | NoSuchAlgorithmException e) {
			return null;
		}
	}

	private static String hash(final String string, final String algorithm) {
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
