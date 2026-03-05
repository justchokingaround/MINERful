package minerful.checking.diagnosis;

public class DiagnosisCell implements Comparable<DiagnosisCell> {
	
	public DiagnosisStatus status = DiagnosisStatus.IS_PENDING;
	public LogCoordinates coordinates;

	public DiagnosisCell(DiagnosisStatus status, Integer caseNumber, Integer eventNumber) {
		this.status = status;
		this.coordinates = new LogCoordinates(caseNumber, eventNumber);
	}

	public DiagnosisCell(DiagnosisStatus status, LogCoordinates coordinates) {
		this.status = status;
		this.coordinates = coordinates;
	}
	
	public DiagnosisCell duplicate() {
		return new DiagnosisCell(status, coordinates.caseNumber, coordinates.eventNumber);
	}

	@Override
	public String toString() {
		return "DiagnosisCell [status=" + status + ", coordinates=" + coordinates
				+ "]";
	}

	@Override
	public int compareTo(DiagnosisCell other) {
		if (this.coordinates.equals(other.coordinates)) {
			return this.status.compareTo(other.status);
		}
		return (this.coordinates.compareTo(other.coordinates));
	}
	
	@Override public boolean equals(Object other) {
		try { return (this.compareTo((DiagnosisCell) other) == 0); } catch (ClassCastException e) { return false; } 
	}
}