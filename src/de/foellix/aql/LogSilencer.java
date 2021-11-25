package de.foellix.aql;

public class LogSilencer implements AutoCloseable {
	int before;

	/**
	 * The area is completely silenced.
	 */
	public LogSilencer() {
		this(Log.SILENCE_LEVEL_ALL);
	}

	/**
	 * The area is silenced.
	 *
	 * @param silenceLevel
	 *            log level to be silenced
	 */
	public LogSilencer(int silenceLevel) {
		this.before = Log.getSilenceLevel();
		Log.setSilence(silenceLevel);
	}

	@Override
	public void close() {
		Log.setSilence(this.before);
	}
}