package de.foellix.aql.helper;

import java.io.File;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.Data;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Hash;
import de.foellix.aql.datastructure.Hashes;
import de.foellix.aql.datastructure.Intent;
import de.foellix.aql.datastructure.Intentfilter;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.KeywordsAndConstants;
import de.foellix.aql.datastructure.Permission;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.Statement;
import de.foellix.aql.datastructure.Target;

public class EqualsHelper {
	public static boolean equals(final Reference reference1, final Reference reference2) {
		return equals(reference1, reference2, false);
	}

	public static boolean equals(final Reference reference1, final Reference reference2,
			final boolean nullAllowedOnLeftHandSide) {
		return equals(reference1, reference2, nullAllowedOnLeftHandSide, false);
	}

	public static boolean equals(final Reference reference1, final Reference reference2,
			final boolean nullAllowedOnLeftHandSide, boolean precisely) {
		if (precisely) {
			if (reference1 == null && reference2 == null) {
				return true;
			} else if (reference1 == null || reference2 == null) {
				return false;
			} else {
				if (equals(reference1.getApp(), reference2.getApp())) {
					if ((reference1.getClassname() == null && reference2.getClassname() == null)
							|| (reference1.getClassname() != null && reference2.getClassname() != null
									&& reference1.getClassname().equals(reference2.getClassname()))) {
						if ((reference1.getMethod() == null && reference2.getMethod() == null)
								|| (reference1.getMethod() != null && reference2.getMethod() != null
										&& reference1.getMethod().equals(reference2.getMethod()))) {
							if (equals(reference1.getStatement(), reference2.getStatement())) {
								return true;
							}
						}
					}
				}
			}
			return false;
		} else {
			if (equals(reference1.getApp(), reference2.getApp())) {
				if ((nullAllowedOnLeftHandSide && reference1.getClassname() == null)
						|| reference1.getClassname().equals(reference2.getClassname())) {
					if ((nullAllowedOnLeftHandSide && reference1.getMethod() == null)
							|| reference1.getMethod().equals(reference2.getMethod())) {
						if ((nullAllowedOnLeftHandSide && reference1.getStatement() == null)
								|| equals(reference1.getStatement(), reference2.getStatement())) {
							return true;
						} else {
							if (reference1.getStatement().getStatementgeneric() == null
									|| reference1.getStatement().getStatementgeneric().equals("")
									|| reference2.getStatement().getStatementgeneric() == null
									|| reference2.getStatement().getStatementgeneric().equals("")) {
								if (Helper.cut(reference1.getStatement().getStatementfull(), "<", ">")
										.equals(Helper.cut(reference2.getStatement().getStatementfull(), "<", ">"))) {
									Log.warning("Statements are equal only on generic level:\n"
											+ reference1.getStatement().getStatementfull() + "\n"
											+ reference2.getStatement().getStatementfull());
									return true;
								}
							} else if (reference1.getStatement().getStatementgeneric()
									.equals(reference2.getStatement().getStatementgeneric())) {
								Log.warning("Statements are equal only on generic level:\n"
										+ reference1.getStatement().getStatementfull() + "\n"
										+ reference2.getStatement().getStatementfull());
								return true;
							}
						}
					}
				}
			}
		}
		return false;
	}

	public static boolean equals(final App app1, final App app2) {
		return equals(app1, app2, false);
	}

	public static boolean equals(final App app1, final App app2, boolean generateHashIfNotAvailable) {
		final boolean check = equals(app1.getHashes(), app2.getHashes());
		if (generateHashIfNotAvailable) {
			if (!check && app1.getFile() != null && app2.getFile() != null && !app1.getFile().equals("")
					&& !app2.getFile().equals("")) {
				final File file1 = new File(app1.getFile());
				final File file2 = new File(app2.getFile());
				if (file1.exists() && file2.exists()) {
					final String hash1 = HashHelper.sha256Hash(file1);
					final String hash2 = HashHelper.sha256Hash(file2);
					if (hash1.equals(hash2)) {
						return true;
					}
				}
			}
		}
		return check;
	}

	public static boolean equals(final Hashes hashes1, final Hashes hashes2) {
		for (final Hash ref1Hash : hashes1.getHash()) {
			for (final Hash ref2Hash : hashes2.getHash()) {
				if (ref1Hash.getValue() != null && ref2Hash.getValue() != null && ref1Hash.getType() != null
						&& ref2Hash.getType() != null) {
					if (ref1Hash.getType().equals(ref2Hash.getType())
							&& ref1Hash.getValue().equals(ref2Hash.getValue())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean equals(final Statement statement1, final Statement statement2) {
		if ((statement1 == null && statement2 != null) || (statement1 != null && statement2 == null)) {
			String warning = "Could not compare statements. One statement is null the other one is:\n";
			if (statement1 == null) {
				warning += statement2.getStatementfull();
			} else {
				warning += statement1.getStatementfull();
			}
			Log.warning(warning);

			return true;
		} else if (statement1 == null && statement2 == null) {
			return true;
		} else {
			final String ref1String = statement1.getStatementfull().replaceAll("\\$", "");
			final String ref2String = statement2.getStatementfull().replaceAll("\\$", "");

			return ref1String.equals(ref2String);
		}
	}

	public static boolean equals(final Permission permission1, final Permission permission2) {
		if (permission1.getName().equals(permission2.getName())) {
			if (equals(permission1.getReference(), permission2.getReference())) {
				return true;
			}
		}
		return false;
	}

	public static boolean equals(final Intent intent1, final Intent intent2) {
		return equals(intent1.getReference(), intent2.getReference())
				&& equals(intent1.getTarget(), intent2.getTarget());
	}

	public static boolean equals(final Intentfilter filter1, final Intentfilter filter2) {
		final Target temp1 = new Target();
		temp1.setAction(filter1.getAction());
		temp1.getCategory().addAll(filter1.getCategory());
		temp1.setData(filter1.getData());
		final Target temp2 = new Target();
		temp2.setAction(filter2.getAction());
		temp2.getCategory().addAll(filter2.getCategory());
		temp2.setData(filter2.getData());

		return equals(filter1.getReference(), filter2.getReference(), true) && equals(temp1, temp2);
	}

	public static boolean equals(final Intentsink sink1, final Intentsink sink2) {
		return equals(sink1.getReference(), sink2.getReference()) && equals(sink1.getTarget(), sink2.getTarget());
	}

	public static boolean equals(final Intentsource source1, final Intentsource source2) {
		return equals(source1.getReference(), source2.getReference())
				&& equals(source1.getTarget(), source2.getTarget());
	}

	public static boolean equals(final Flow path1, final Flow path2) {
		Reference path1From = null;
		Reference path1To = null;
		Reference path2From = null;
		Reference path2To = null;
		for (final Reference ref : path1.getReference()) {
			if (ref.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_FROM)) {
				path1From = ref;
			} else if (ref.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_TO)) {
				path1To = ref;
			}
		}
		for (final Reference ref : path2.getReference()) {
			if (ref.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_FROM)) {
				path2From = ref;
			} else if (ref.getType().equals(KeywordsAndConstants.REFERENCE_TYPE_TO)) {
				path2To = ref;
			}
		}

		if (equals(path1From, path2From) && equals(path1To, path2To)) {
			return true;
		} else {
			return false;
		}
	}

	// Merging-Equals
	public static boolean equalsConnect(final Intentsource source, final Intentsink sink) {
		return equals(sink.getTarget(), source.getTarget());
	}

	public static boolean equalsConnect(final Intentsink sink, final Intentsource source) {
		return equals(sink.getTarget(), source.getTarget());
	}

	public static boolean equals(final Target target1, final Target target2) {
		return equals(target1, target2, false);
	}

	public static boolean equals(final Target target1, final Target target2, boolean precisely) {
		if (precisely) {
			if (target1 == null && target2 == null) {
				return true;
			} else if (target1 == null || target2 == null) {
				return false;
			} else {
				if ((target1.getAction() == null && target2.getAction() == null) || (target1.getAction() != null
						&& target2.getAction() != null && target1.getAction().equals(target2.getAction()))) {
					if ((target1.getCategory() == null && target2.getCategory() == null)
							|| (target1.getCategory() != null && target2.getCategory() != null
									&& equals(target1.getCategory(), target2.getCategory()))) {
						if ((target1.getData() == null && target2.getData() == null) || (target1.getData() != null
								&& target2.getData() != null && equals(target1.getData(), target2.getData()))) {
							if ((target1.getReference() == null && target2.getReference() == null)
									|| (target1.getReference() != null && target2.getReference() != null
											&& equals(target1.getReference(), target2.getReference(), false, true))) {
								return true;
							}
						}
					}
				}
			}
			return false;
		} else {
			if (target1 != null && target2 != null) {
				// Implicit
				try {
					if ((target1.getAction() == null && target2.getAction() == null)
							|| target1.getAction().equals(target2.getAction())) {
						if (target1.getCategory() != null && target2.getCategory() != null) {
							for (final String category1 : target1.getCategory()) {
								for (final String category2 : target2.getCategory()) {
									if (category1.equals(category2)) {
										return true;
									}
								}
							}
						}
					}
				} catch (final NullPointerException e1) {
					try {
						if ((target1.getAction() != null && target2.getAction() != null
								&& target1.getAction().equals(target2.getAction()))
								&& ((target1.getCategory() == null && target2.getCategory() != null)
										|| (target1.getCategory() != null && target2.getCategory() == null))) {
							Log.warning("Action (" + target1.getAction() + ") matches, but category ("
									+ target1.getCategory() + ", " + target2.getCategory() + ") does not.");
							return true;
						}
					} catch (final NullPointerException e2) {
						// do nothing
					}
				}

				// Explicit
				if (target1.getReference() != null && target2.getReference() != null) {
					if (target1.getReference().getClassname() != null
							&& target2.getReference().getClassname() != null) {
						if (target1.getReference().getClassname().equals(target2.getReference().getClassname())) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	public static boolean equals(List<String> category1, List<String> category2) {
		// Precisely! Not Android related.
		boolean catEquals = true;
		for (final String cat1 : category1) {
			boolean found = false;
			for (final String cat2 : category2) {
				if (cat1.equals(cat2)) {
					found = true;
					break;
				}
			}
			if (!found) {
				catEquals = false;
				break;
			}
		}
		return catEquals;
	}

	public static boolean equals(final Data data1, final Data data2) {
		// Precisely! Not Android related.
		if (data1.getHost().equals(data2.getHost()) && data1.getPath().equals(data2.getPath())
				&& data1.getPort().equals(data2.getPort()) && data1.getScheme().equals(data2.getScheme())
				&& data1.getSsp().equals(data2.getSsp()) && data1.getType().equals(data2.getType())) {
			return true;
		}
		return false;
	}
}
