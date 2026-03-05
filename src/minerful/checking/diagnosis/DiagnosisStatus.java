package minerful.checking.diagnosis;

public enum DiagnosisStatus {
	VIOLATES(-1),
	IS_PENDING(0),
	SATISFIES(1);
	
	public final Integer code;
	private DiagnosisStatus(Integer value) {
		this.code = value;
	}
}