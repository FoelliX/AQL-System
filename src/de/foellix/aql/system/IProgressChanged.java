package de.foellix.aql.system;

public interface IProgressChanged {
	public void onProgressChanged(String step, int inProgress, int done, int max);
}
