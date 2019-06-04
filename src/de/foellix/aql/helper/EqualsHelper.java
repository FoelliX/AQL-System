package de.foellix.aql.helper;

import java.io.File;
import java.util.List;

import de.foellix.aql.Log;
import de.foellix.aql.datastructure.Answer;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.Data;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Flows;
import de.foellix.aql.datastructure.Hash;
import de.foellix.aql.datastructure.Hashes;
import de.foellix.aql.datastructure.Intent;
import de.foellix.aql.datastructure.Intentfilter;
import de.foellix.aql.datastructure.Intentfilters;
import de.foellix.aql.datastructure.Intents;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsinks;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.Intentsources;
import de.foellix.aql.datastructure.Permission;
import de.foellix.aql.datastructure.Permissions;
import de.foellix.aql.datastructure.Reference;
import de.foellix.aql.datastructure.Statement;
import de.foellix.aql.datastructure.Target;

public class EqualsHelper {
	// Answer
	public static boolean equals(Answer answer1, Answer answer2) {
		return equals(answer1, answer2, EqualsOptions.DEFAULT);
	}

	public static boolean equals(Answer answer1, Answer answer2, EqualsOptions options) {
		if (equals(answer1.getFlows(), answer2.getFlows(), options)
				&& equals(answer1.getIntentfilters(), answer2.getIntentfilters(), options)
				&& equals(answer1.getIntents(), answer2.getIntents(), options)
				&& equals(answer1.getIntentsinks(), answer2.getIntentsinks(), options)
				&& equals(answer1.getIntentsources(), answer2.getIntentsources(), options)
				&& equals(answer1.getPermissions(), answer2.getPermissions(), options)) {
			return true;
		} else {
			return false;
		}
	}

	// Flows
	public static boolean equals(Flows flows1, Flows flows2) {
		return equals(flows1, flows2, EqualsOptions.DEFAULT);
	}

	public static boolean equals(Flows flows1, Flows flows2, EqualsOptions options) {
		if ((flows1 == null && flows2 != null) || (flows1 != null && flows2 == null)) {
			if ((flows1 == null && flows2.getFlow().isEmpty()) || (flows2 == null && flows1.getFlow().isEmpty())) {
				return true;
			} else {
				return false;
			}
		} else if (flows1 == null && flows2 == null) {
			return true;
		} else if (flows1.getFlow().size() != flows2.getFlow().size()) {
			return false;
		}
		for (final Flow item1 : flows1.getFlow()) {
			boolean found = false;
			for (final Flow item2 : flows2.getFlow()) {
				if (equals(item1, item2, options)) {
					found = true;
					break;
				}
			}
			if (found == false) {
				return false;
			}
		}
		return true;
	}

	public static boolean equals(Flow flow1, Flow flow2) {
		return equals(flow1, flow2, EqualsOptions.DEFAULT);
	}

	public static boolean equals(Flow flow1, Flow flow2, EqualsOptions options) {
		if (equals(Helper.getFrom(flow1), Helper.getFrom(flow2), options)
				&& equals(Helper.getTo(flow1), Helper.getTo(flow2), options)) {
			return true;
		} else {
			return false;
		}
	}

	// Permissions
	public static boolean equals(Permissions permissions1, Permissions permissions2) {
		return equals(permissions1, permissions2, EqualsOptions.DEFAULT);
	}

	public static boolean equals(Permissions permissions1, Permissions permissions2, EqualsOptions options) {
		if ((permissions1 == null && permissions2 != null) || (permissions1 != null && permissions2 == null)) {
			if ((permissions1 == null && permissions2.getPermission().isEmpty())
					|| (permissions2 == null && permissions1.getPermission().isEmpty())) {
				return true;
			} else {
				return false;
			}
		} else if (permissions1 == null && permissions2 == null) {
			return true;
		} else if (permissions1.getPermission().size() != permissions2.getPermission().size()) {
			return false;
		}
		for (final Permission item1 : permissions1.getPermission()) {
			boolean found = false;
			for (final Permission item2 : permissions2.getPermission()) {
				if (equals(item1, item2, options)) {
					found = true;
					break;
				}
			}
			if (found == false) {
				return false;
			}
		}
		return true;
	}

	public static boolean equals(Permission permission1, Permission permission2) {
		return equals(permission1, permission2, EqualsOptions.DEFAULT);
	}

	public static boolean equals(Permission permission1, Permission permission2, EqualsOptions options) {
		if (permission1.getName().equals(permission2.getName())) {
			if (equals(permission1.getReference(), permission2.getReference(), options)) {
				return true;
			}
		}
		return false;
	}

	// Intents
	public static boolean equals(Intents intents1, Intents intents2) {
		return equals(intents1, intents2, EqualsOptions.DEFAULT);
	}

	public static boolean equals(Intents intents1, Intents intents2, EqualsOptions options) {
		if ((intents1 == null && intents2 != null) || (intents1 != null && intents2 == null)) {
			if ((intents1 == null && intents2.getIntent().isEmpty())
					|| (intents2 == null && intents1.getIntent().isEmpty())) {
				return true;
			} else {
				return false;
			}
		} else if (intents1 == null && intents2 == null) {
			return true;
		} else if (intents1.getIntent().size() != intents2.getIntent().size()) {
			return false;
		}
		for (final Intent item1 : intents1.getIntent()) {
			boolean found = false;
			for (final Intent item2 : intents2.getIntent()) {
				if (equals(item1, item2, options)) {
					found = true;
					break;
				}
			}
			if (found == false) {
				return false;
			}
		}
		return true;
	}

	public static boolean equals(Intent intent1, Intent intent2) {
		return equals(intent1, intent2, EqualsOptions.DEFAULT);
	}

	public static boolean equals(Intent intent1, Intent intent2, EqualsOptions options) {
		return equals(intent1.getReference(), intent2.getReference(), options)
				&& equals(intent1.getTarget(), intent2.getTarget(), options);
	}

	// Intentfilters
	public static boolean equals(Intentfilters intentfilters1, Intentfilters intentfilters2) {
		return equals(intentfilters1, intentfilters2, EqualsOptions.DEFAULT);
	}

	public static boolean equals(Intentfilters intentfilters1, Intentfilters intentfilters2, EqualsOptions options) {
		if ((intentfilters1 == null && intentfilters2 != null) || (intentfilters1 != null && intentfilters2 == null)) {
			if ((intentfilters1 == null && intentfilters2.getIntentfilter().isEmpty())
					|| (intentfilters2 == null && intentfilters1.getIntentfilter().isEmpty())) {
				return true;
			} else {
				return false;
			}
		} else if (intentfilters1 == null && intentfilters2 == null) {
			return true;
		} else if (intentfilters1.getIntentfilter().size() != intentfilters2.getIntentfilter().size()) {
			return false;
		}
		for (final Intentfilter item1 : intentfilters1.getIntentfilter()) {
			boolean found = false;
			for (final Intentfilter item2 : intentfilters2.getIntentfilter()) {
				if (equals(item1, item2, options)) {
					found = true;
					break;
				}
			}
			if (found == false) {
				return false;
			}
		}
		return true;
	}

	public static boolean equals(Intentfilter intentfilter1, Intentfilter intentfilter2) {
		return equals(intentfilter1, intentfilter2,
				EqualsOptions.DEFAULT.setOption(EqualsOptions.NULL_ALLOWED_ON_LEFT_HAND_SIDE, true));
	}

	public static boolean equals(Intentfilter filter1, Intentfilter filter2, EqualsOptions options) {
		final Target temp1 = new Target();
		temp1.getAction().addAll(filter1.getAction());
		temp1.getCategory().addAll(filter1.getCategory());
		temp1.getData().addAll(filter1.getData());
		final Target temp2 = new Target();
		temp2.getAction().addAll(filter2.getAction());
		temp2.getCategory().addAll(filter2.getCategory());
		temp2.getData().addAll(filter2.getData());

		return equals(filter1.getReference(), filter2.getReference(), options) && equals(temp1, temp2, options);
	}

	// Intentsinks
	public static boolean equals(Intentsinks intentsinks1, Intentsinks intentsinks2) {
		return equals(intentsinks1, intentsinks2, EqualsOptions.DEFAULT);
	}

	public static boolean equals(Intentsinks intentsinks1, Intentsinks intentsinks2, EqualsOptions options) {
		if ((intentsinks1 == null && intentsinks2 != null) || (intentsinks1 != null && intentsinks2 == null)) {
			if ((intentsinks1 == null && intentsinks2.getIntentsink().isEmpty())
					|| (intentsinks2 == null && intentsinks1.getIntentsink().isEmpty())) {
				return true;
			} else {
				return false;
			}
		} else if (intentsinks1 == null && intentsinks2 == null) {
			return true;
		} else if (intentsinks1.getIntentsink().size() != intentsinks2.getIntentsink().size()) {
			return false;
		}
		for (final Intentsink item1 : intentsinks1.getIntentsink()) {
			boolean found = false;
			for (final Intentsink item2 : intentsinks2.getIntentsink()) {
				if (equals(item1, item2, options)) {
					found = true;
					break;
				}
			}
			if (found == false) {
				return false;
			}
		}
		return true;
	}

	public static boolean equals(Intentsink intentsink1, Intentsink intentsink2) {
		return equals(intentsink1, intentsink2, EqualsOptions.DEFAULT);
	}

	public static boolean equals(Intentsink sink1, Intentsink sink2, EqualsOptions options) {
		return equals(sink1.getReference(), sink2.getReference(), options)
				&& equals(sink1.getTarget(), sink2.getTarget(), options);
	}

	// Intentsources
	public static boolean equals(Intentsources intentsources1, Intentsources intentsources2) {
		return equals(intentsources1, intentsources2, EqualsOptions.DEFAULT);
	}

	public static boolean equals(Intentsources intentsources1, Intentsources intentsources2, EqualsOptions options) {
		if ((intentsources1 == null && intentsources2 != null) || (intentsources1 != null && intentsources2 == null)) {
			if ((intentsources1 == null && intentsources2.getIntentsource().isEmpty())
					|| (intentsources2 == null && intentsources1.getIntentsource().isEmpty())) {
				return true;
			} else {
				return false;
			}
		} else if (intentsources1 == null && intentsources2 == null) {
			return true;
		} else if (intentsources1.getIntentsource().size() != intentsources2.getIntentsource().size()) {
			return false;
		}
		for (final Intentsource item1 : intentsources1.getIntentsource()) {
			boolean found = false;
			for (final Intentsource item2 : intentsources2.getIntentsource()) {
				if (equals(item1, item2, options)) {
					found = true;
					break;
				}
			}
			if (found == false) {
				return false;
			}
		}
		return true;
	}

	public static boolean equals(Intentsource intentsource1, Intentsource intentsource2) {
		return equals(intentsource1, intentsource2, EqualsOptions.DEFAULT);
	}

	public static boolean equals(Intentsource source1, Intentsource source2, EqualsOptions options) {
		return equals(source1.getReference(), source2.getReference(), options)
				&& equals(source1.getTarget(), source2.getTarget(), options);
	}

	// References
	public static boolean equals(Reference reference1, Reference reference2) {
		return equals(reference1, reference2, EqualsOptions.DEFAULT);
	}

	public static boolean equals(Reference reference1, Reference reference2, EqualsOptions options) {
		boolean returnValue = false;
		if (options.getOption(EqualsOptions.PRECISELY_REFERENCE)) {
			if (reference1 == null && reference2 == null) {
				returnValue = true;
			} else if (reference1 == null || reference2 == null) {
				returnValue = false;
			} else {
				if (options.getOption(EqualsOptions.IGNORE_APP)
						|| equals(reference1.getApp(), reference2.getApp(), options)) {
					if ((reference1.getClassname() == null && reference2.getClassname() == null)
							|| (reference1.getClassname() != null && reference2.getClassname() != null
									&& reference1.getClassname().equals(reference2.getClassname()))) {
						if ((reference1.getMethod() == null && reference2.getMethod() == null)
								|| (reference1.getMethod() != null && reference2.getMethod() != null
										&& reference1.getMethod().equals(reference2.getMethod()))) {
							if (equals(reference1.getStatement(), reference2.getStatement(), options)) {
								returnValue = true;
							}
						}
					}
				}
			}
		} else {
			if (options.getOption(EqualsOptions.IGNORE_APP)
					|| equals(reference1.getApp(), reference2.getApp(), options)) {
				if ((options.getOption(EqualsOptions.NULL_ALLOWED_ON_LEFT_HAND_SIDE)
						&& reference1.getClassname() == null)
						|| reference1.getClassname().equals(reference2.getClassname())) {
					if ((options.getOption(EqualsOptions.NULL_ALLOWED_ON_LEFT_HAND_SIDE)
							&& reference1.getMethod() == null)
							|| reference1.getMethod().equals(reference2.getMethod())) {
						if ((options.getOption(EqualsOptions.NULL_ALLOWED_ON_LEFT_HAND_SIDE)
								&& reference1.getStatement() == null)
								|| equals(reference1.getStatement(), reference2.getStatement(), options)) {
							returnValue = true;
						}
					}
				}
			}
		}

		if (returnValue && Log.logIt(Log.DEBUG_DETAILED)) {
			if (options.getOption(EqualsOptions.IGNORE_APP)
					&& !equals(reference1.getApp(), reference2.getApp(), options)) {
				Log.warning(Helper.toString(reference1) + "\nis only equal to\n" + Helper.toString(reference2)
						+ "\nif the app is ignored. Reason might be an app-merge for example.");
			}
		}
		return returnValue;
	}

	// Statements
	public static boolean equals(Statement statement1, Statement statement2) {
		return equals(statement1, statement2, EqualsOptions.DEFAULT.setOption(EqualsOptions.PRECISELY_REFERENCE, true));
	}

	public static boolean equals(Statement statement1, Statement statement2, EqualsOptions options) {
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

			if (ref1String.equals(ref2String)) {
				return true;
			} else if (!options.getOption(EqualsOptions.PRECISELY_REFERENCE)) {
				if (statement1.getStatementgeneric() == null || statement1.getStatementgeneric().equals("")
						|| statement2.getStatementgeneric() == null || statement2.getStatementgeneric().equals("")) {
					if (Helper.cut(statement1.getStatementfull(), "<", ">")
							.equals(Helper.cut(statement2.getStatementfull(), "<", ">"))) {
						Log.warning("Statements are equal only on generic level:\n" + statement1.getStatementfull()
								+ "\n" + statement2.getStatementfull());
						return true;
					}
				} else if (statement1.getStatementgeneric().equals(statement2.getStatementgeneric())) {
					Log.warning("Statements are equal only on generic level:\n" + statement1.getStatementfull() + "\n"
							+ statement2.getStatementfull());
					return true;
				}
			}
		}
		return false;
	}

	// Methods and Classnames (String only) - Not required

	// Apps
	public static boolean equals(App app1, App app2) {
		return equals(app1, app2, EqualsOptions.DEFAULT);
	}

	public static boolean equals(App app1, App app2, EqualsOptions options) {
		final boolean check = equals(app1.getHashes(), app2.getHashes(), options);
		if (options.getOption(EqualsOptions.GENERATE_HASH_IF_NOT_AVAILABLE)) {
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

	// Hashes
	public static boolean equals(Hashes hashes1, Hashes hashes2) {
		return equals(hashes1, hashes2, EqualsOptions.DEFAULT);
	}

	public static boolean equals(Hashes hashes1, Hashes hashes2, EqualsOptions options) {
		if ((hashes1 == null && hashes2 != null) || (hashes1 != null && hashes2 == null)) {
			return false;
		} else if (hashes1 == null && hashes2 == null) {
			return true;
		}
		for (final Hash ref1Hash : hashes1.getHash()) {
			for (final Hash ref2Hash : hashes2.getHash()) {
				if (equals(ref1Hash, ref2Hash, options)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean equals(Hash hash1, Hash hash2) {
		return equals(hash1, hash2, EqualsOptions.DEFAULT);
	}

	public static boolean equals(Hash hash1, Hash hash2, EqualsOptions options) {
		if ((hash1 == null && hash2 != null) || (hash1 != null && hash2 == null)) {
			return false;
		} else if (hash1 == null && hash2 == null) {
			return true;
		}
		if (hash1.getValue() != null && hash2.getValue() != null && hash1.getType() != null
				&& hash2.getType() != null) {
			if (hash1.getType().equals(hash2.getType()) && hash1.getValue().equals(hash2.getValue())) {
				return true;
			}
		}
		return false;
	}

	// Targets
	public static boolean equals(Target target1, Target target2) {
		return equals(target1, target2, EqualsOptions.DEFAULT.setOption(EqualsOptions.PRECISELY_TARGET, true)
				.setOption(EqualsOptions.PRECISELY_REFERENCE, true));
	}

	public static boolean equals(Target target1, Target target2, EqualsOptions options) {
		if (options.getOption(EqualsOptions.PRECISELY_TARGET)) {
			if (target1 == null && target2 == null) {
				return true;
			} else if (target1 == null || target2 == null) {
				return false;
			} else {
				if (equalsCategoryAndAction(target1.getAction(), target2.getAction(), options)) {
					if (equalsCategoryAndAction(target1.getCategory(), target2.getCategory(), options)) {
						if (equalsData(target1.getData(), target2.getData(), options)) {
							if ((target1.getReference() == null && target2.getReference() == null)
									|| (target1.getReference() != null && target2.getReference() != null
											&& equals(target1.getReference(), target2.getReference(), options))) {
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
					if (equalsCategoryAndAction(target1.getAction(), target2.getAction(), options)) {
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
						if (equalsCategoryAndAction(target1.getAction(), target2.getAction(), options)
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

	public static boolean equalsCategoryAndAction(List<String> categoryOrAction1, List<String> categoryOrAction2) {
		return equalsCategoryAndAction(categoryOrAction1, categoryOrAction2, EqualsOptions.DEFAULT);
	}

	public static boolean equalsCategoryAndAction(List<String> categoryOrAction1, List<String> categoryOrAction2,
			EqualsOptions options) {
		// Precisely! Not Android related.
		if (categoryOrAction1 == null && categoryOrAction2 == null) {
			return true;
		} else if (categoryOrAction1 != null && categoryOrAction2 == null) {
			return false;
		} else if (categoryOrAction1 == null && categoryOrAction2 != null) {
			return false;
		} else if (categoryOrAction1.size() != categoryOrAction2.size()) {
			return false;
		} else if (categoryOrAction1.isEmpty() && categoryOrAction2.isEmpty()) {
			return true;
		} else {
			for (final String d1 : categoryOrAction1) {
				boolean found = false;
				for (final String d2 : categoryOrAction2) {
					if (d1.equals(d2)) {
						found = true;
						break;
					}
				}
				if (!found) {
					return false;
				}
			}
			return true;
		}
	}

	public static boolean equalsData(List<Data> data1, List<Data> data2) {
		return equalsData(data1, data2, EqualsOptions.DEFAULT);
	}

	public static boolean equalsData(List<Data> data1, List<Data> data2, EqualsOptions options) {
		// Precisely! Not Android related.
		if (data1 == null && data2 == null) {
			return true;
		} else if (data1 != null && data2 == null) {
			return false;
		} else if (data1 == null && data2 != null) {
			return false;
		} else if (data1.size() != data2.size()) {
			return false;
		} else if (data1.isEmpty() && data2.isEmpty()) {
			return true;
		} else {
			for (final Data d1 : data1) {
				boolean found = false;
				for (final Data d2 : data2) {
					if (equals(d1, d2, options)) {
						found = true;
						break;
					}
				}
				if (!found) {
					return false;
				}
			}
			return true;
		}
	}

	public static boolean equals(Data data1, Data data2) {
		return equals(data1, data2, EqualsOptions.DEFAULT);
	}

	public static boolean equals(Data data1, Data data2, EqualsOptions options) {
		// Precisely! Not Android related.
		final boolean[] returnValue = new boolean[6];

		// Check elements
		if (data1.getHost() == null && data2.getHost() == null) {
			returnValue[0] = true;
		} else if (data1.getHost() == null || data2.getHost() == null) {
			return false;
		} else {
			returnValue[0] = data1.getHost().equals(data2.getHost());
		}
		if (data1.getPath() == null && data2.getPath() == null) {
			returnValue[0] = true;
		} else if (data1.getPath() == null || data2.getPath() == null) {
			return false;
		} else {
			returnValue[1] = data1.getPath().equals(data2.getPath());
		}
		if (data1.getPort() == null && data2.getPort() == null) {
			returnValue[0] = true;
		} else if (data1.getPort() == null || data2.getPort() == null) {
			return false;
		} else {
			returnValue[2] = data1.getPort().equals(data2.getPort());
		}
		if (data1.getScheme() == null && data2.getScheme() == null) {
			returnValue[0] = true;
		} else if (data1.getScheme() == null || data2.getScheme() == null) {
			return false;
		} else {
			returnValue[3] = data1.getScheme().equals(data2.getScheme());
		}
		if (data1.getSsp() == null && data2.getSsp() == null) {
			returnValue[0] = true;
		} else if (data1.getSsp() == null || data2.getSsp() == null) {
			return false;
		} else {
			returnValue[4] = data1.getSsp().equals(data2.getSsp());
		}
		if (data1.getType() == null && data2.getType() == null) {
			returnValue[0] = true;
		} else if (data1.getType() == null || data2.getType() == null) {
			return false;
		} else {
			returnValue[5] = data1.getType().equals(data2.getType());
		}

		// Check complete
		if (!returnValue[0] || !returnValue[1] || !returnValue[2] || !returnValue[3] || !returnValue[4]
				|| !returnValue[5]) {
			return false;
		} else {
			return true;
		}
	}

	// Equals used in Default-Operators
	@Deprecated
	public static boolean equalsConnect(Intentsource source, Intentsink sink) {
		return equalsConnect(source, sink, EqualsOptions.DEFAULT);
	}

	@Deprecated
	public static boolean equalsConnect(Intentsource source, Intentsink sink, EqualsOptions options) {
		return equals(sink.getTarget(), source.getTarget(), options);
	}

	@Deprecated
	public static boolean equalsConnect(Intentsink sink, Intentsource source) {
		return equalsConnect(sink, source, EqualsOptions.DEFAULT);
	}

	@Deprecated
	public static boolean equalsConnect(Intentsink sink, Intentsource source, EqualsOptions options) {
		return equals(sink.getTarget(), source.getTarget(), options);
	}

	// Downward compatibility
	@Deprecated
	public static boolean equals(Reference reference1, Reference reference2, boolean nullAllowedOnLeftHandSide) {
		return equals(reference1, reference2, EqualsOptions.DEFAULT
				.setOption(EqualsOptions.NULL_ALLOWED_ON_LEFT_HAND_SIDE, nullAllowedOnLeftHandSide));
	}

	@Deprecated
	public static boolean equals(Reference reference1, Reference reference2, boolean nullAllowedOnLeftHandSide,
			boolean precisely) {
		return equals(reference1, reference2,
				EqualsOptions.DEFAULT.setOption(EqualsOptions.NULL_ALLOWED_ON_LEFT_HAND_SIDE, nullAllowedOnLeftHandSide)
						.setOption(EqualsOptions.PRECISELY_REFERENCE, precisely));
	}

	@Deprecated
	public static boolean equals(App app1, App app2, boolean generateHashIfNotAvailable) {
		return equals(app1, app2, EqualsOptions.DEFAULT.setOption(EqualsOptions.GENERATE_HASH_IF_NOT_AVAILABLE,
				generateHashIfNotAvailable));
	}

	@Deprecated
	public static boolean equals(Target target1, Target target2, boolean precisely) {
		return equals(target1, target2, EqualsOptions.DEFAULT.setOption(EqualsOptions.PRECISELY_TARGET, precisely)
				.setOption(EqualsOptions.PRECISELY_REFERENCE, true));
	}
}
