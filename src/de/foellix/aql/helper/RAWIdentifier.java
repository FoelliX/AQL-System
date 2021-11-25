package de.foellix.aql.helper;

import de.foellix.aql.config.Tool;
import de.foellix.aql.datastructure.App;
import de.foellix.aql.datastructure.Flow;
import de.foellix.aql.datastructure.Hash;
import de.foellix.aql.datastructure.Intentsink;
import de.foellix.aql.datastructure.Intentsource;
import de.foellix.aql.datastructure.Permission;
import de.foellix.aql.datastructure.Reference;

public class RAWIdentifier {
	private boolean genericStatementOnly;
	private boolean considerLineNumbers;

	public RAWIdentifier() {
		this.genericStatementOnly = false;
		this.considerLineNumbers = true;
	}

	public RAWIdentifier(boolean genericStatementOnly, boolean considerLineNumbers) {
		this.genericStatementOnly = genericStatementOnly;
		this.considerLineNumbers = considerLineNumbers;
	}

	public String toRAW(Object item) {
		if (item instanceof Permission) {
			return toRAW((Permission) item);
		} else if (item instanceof Intentsink) {
			return toRAW((Intentsink) item);
		} else if (item instanceof Intentsource) {
			return toRAW((Intentsource) item);
		} else if (item instanceof Reference) {
			return toRAW((Reference) item);
		} else if (item instanceof Tool) {
			return toRAW((Tool) item);
		} else {
			return null;
		}
	}

	public String toRAW(final Tool tool) {
		return tool.getName() + "-" + tool.getVersion();
	}

	public String toRAW(Flow flow) {
		return Helper.toRAW(Helper.getFrom(flow), Helper.getTo(flow));
	}

	public String toRAW(Permission permission) {
		final String temp = "Permission:" + permission.getName() + toRAW(permission.getReference());
		return Helper.replaceAllWhiteSpaceChars(temp);
	}

	public String toRAW(Intentsink intentsink) {
		final StringBuilder sb = new StringBuilder("Intentsink:");
		if (intentsink.getTarget() != null) {
			sb.append(Helper.toString(intentsink.getTarget(), true));
		}
		if (intentsink.getReference() != null) {
			sb.append(toRAW(intentsink.getReference()));
		}
		if (sb.length() <= 0) {
			sb.append(intentsink.hashCode());
		}

		return Helper.replaceAllWhiteSpaceChars(sb.toString());
	}

	public String toRAW(Intentsource intentsource) {
		final StringBuilder sb = new StringBuilder("Intentsource:");
		if (intentsource.getTarget() != null) {
			sb.append(Helper.toString(intentsource.getTarget(), true));
		}
		if (intentsource.getReference() != null) {
			sb.append(toRAW(intentsource.getReference()));
		}
		if (sb.length() <= 0) {
			sb.append(intentsource.hashCode());
		}

		return Helper.replaceAllWhiteSpaceChars(sb.toString());
	}

	public String toRAW(Reference from, Reference to) {
		return Helper.toRAW(from) + " -> " + Helper.toRAW(to);
	}

	public String toRAW(final Reference reference) {
		final StringBuilder sb = new StringBuilder();
		if (reference.getStatement() != null) {
			if (!this.genericStatementOnly) {
				sb.append(reference.getStatement().getStatementfull());
			} else {
				sb.append(reference.getStatement().getStatementgeneric());
			}
			if (this.considerLineNumbers) {
				sb.append(":" + (reference.getStatement().getLinenumber() == null ? -1
						: reference.getStatement().getLinenumber().intValue()));
			}
		}
		if (reference.getMethod() != null) {
			sb.append(reference.getMethod());
		}
		if (reference.getClassname() != null) {
			sb.append(reference.getClassname());
		}
		if (reference.getApp() != null) {
			sb.append(toRAW(reference.getApp()));
		}

		return sb.toString();
	}

	public String toRAW(final App app) {
		final StringBuilder sb = new StringBuilder();
		if (app.getHashes() != null && !app.getHashes().getHash().isEmpty()) {
			for (final Hash hash : app.getHashes().getHash()) {
				sb.append(hash.getType() + ": " + hash.getValue());
			}
		}
		return sb.toString();
	}
}