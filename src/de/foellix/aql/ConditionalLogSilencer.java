package de.foellix.aql;

public class ConditionalLogSilencer extends LogSilencer {
	/**
	 * The area is completely silenced until a specific level.
	 *
	 * @param silenceIncludingLogLevel
	 *            the level (e.g. when set to DEBUG, the area will be silenced unless the current log level is higher than DEBUG.)
	 */
	public ConditionalLogSilencer(int silenceIncludingLogLevel) {
		this(silenceIncludingLogLevel, Log.SILENCE_LEVEL_ALL);
	}

	/**
	 * The area is silenced until a specific level.
	 *
	 * @param silenceIncludingLogLevel
	 *            the level (e.g. when set to DEBUG, the area will be silenced unless the current log level is higher than DEBUG.)
	 * @param silenceLevel
	 *            log level to be silenced
	 */
	public ConditionalLogSilencer(int silenceIncludingLogLevel, int silenceLevel) {
		if (!Log.logIt(silenceIncludingLogLevel)) {
			Log.setSilence(silenceLevel);
		}
	}
}