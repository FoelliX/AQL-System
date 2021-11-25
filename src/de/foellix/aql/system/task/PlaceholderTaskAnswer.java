package de.foellix.aql.system.task;

public class PlaceholderTaskAnswer extends TaskAnswer {
	private static final long serialVersionUID = 624151724616345745L;

	public PlaceholderTaskAnswer(Task parent) {
		super(parent, -1);
	}

	@Override
	public boolean isAnswered() {
		return false;
	}
}
