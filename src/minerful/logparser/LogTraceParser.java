package minerful.logparser;

public interface LogTraceParser {
	enum ReadingSense {
		ONWARDS,
		BACKWARDS;
		
		public ReadingSense switchSenseOfReading() {
			return (this.equals(ONWARDS) ? BACKWARDS : ONWARDS);
		}
	}

	/**
	 * Reverts the reading sense along the trace
	 * @return The new reading sense
	 */
	ReadingSense reverse();
	/**
	 * Returns the reading sense along the trace
	 * @return The current reading sense
	 */
	ReadingSense getSenseOfReading();
	/**
	 * Returns the number of events in the trace
	 * @return The number of events in the trace
	 */
	int length();
	/**
	 * Returns the log parser for the log that this trace belongs to
	 * @return The log parser for the log that this trace belongs to
	 */
	LogParser getLogParser();
	/**
	 * Indicates whether the parser is currently reading a trace.
	 * @return <code>true</code> if the parser is currently reading a trace. <code>false</code> otherwise.
	 */
	boolean isParsing();
	/**
	 * Steps to the next event and reads it
	 * @return The event parser dealing with the newly parsed event
	 */
	LogEventParser parseSubsequent();
	/**
	 * Steps to the next event and returns a Character identifier for it.
	 * @return A Character identifier of the newly parsed event
	 */
	Character parseSubsequentAndEncode();
	/**
	 * Indicates whether the reading is over.
	 * @return <code>true</code> if there are no new events left to parse.
	 */
	boolean isParsingOver();
	/**
	 * Moves to the following event, if any is readable.
	 * @return <code>true</code> if a new event is reached.
	 */
	boolean stepToSubsequent();
	/**
	 * Initializes the internal data structures.
	 */
	void init();
	/**
	 * Returns a string of characters (see {@link #parseSubsequentAndEncode() parseSubsequentAndEncode}), each identifying an event in the trace.
	 * @return A string of identifiers.
	 */
	String encodeTrace();
	/**
	 * Returns a readable string with the sequence of event classifiers.
	 * @return A readable string representing the trace.
	 */
	String printStringTrace();
	/**
	 * Returns the name of the trace.
	 * @return The name of the trace.
	 */
	String getName();
}