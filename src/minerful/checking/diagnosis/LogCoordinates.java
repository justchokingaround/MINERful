package minerful.checking.diagnosis;

public class LogCoordinates implements Comparable<LogCoordinates> {
	public final Integer caseNumber;
	public final Integer eventNumber;
	/**
	 * Log coordinates start the count at 1
	 */
	public static final int START_NUMBER = 1;
	public static final int START_OF_TRACE_EVENT_NUMBER = Integer.MIN_VALUE;
	public static final int END_OF_TRACE_EVENT_NUMBER = Integer.MAX_VALUE;
	public static final String START_OF_TRACE_EVENT_NUMBER_PRINTOUT = "\"start\"";
	public static final String END_OF_TRACE_EVENT_PRINTOUT = "\"end\"";
	
	public LogCoordinates(Integer caseNumber, Integer eventNumber) {
		this.caseNumber = ((caseNumber == START_OF_TRACE_EVENT_NUMBER || caseNumber == END_OF_TRACE_EVENT_NUMBER)? caseNumber : START_NUMBER + caseNumber);
		this.eventNumber = ((eventNumber == START_OF_TRACE_EVENT_NUMBER || eventNumber == END_OF_TRACE_EVENT_NUMBER)? eventNumber : START_NUMBER + eventNumber);
	}
	@Override
	public String toString() {
		return "LogCoordinates [caseNumber=" + caseNumber + ", eventNumber="
				+ eventNumberToString(eventNumber) + "]";
	}
	public static String eventNumberToString(Integer eventNumber) {
		return eventNumber == START_OF_TRACE_EVENT_NUMBER ? START_OF_TRACE_EVENT_NUMBER_PRINTOUT : (
				eventNumber == END_OF_TRACE_EVENT_NUMBER ? END_OF_TRACE_EVENT_PRINTOUT : eventNumber.toString() );
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof LogCoordinates)) {
			return false;
		}
		LogCoordinates other = (LogCoordinates) obj;
		return this.caseNumber == other.caseNumber && this.eventNumber == other.eventNumber;
	}
	
	@Override
	public int compareTo(LogCoordinates other) {
		if (other.caseNumber.equals(this.caseNumber)) {
			return this.eventNumber.compareTo(other.eventNumber);
		}
		return this.caseNumber.compareTo(other.caseNumber);
	}
	
	
}
