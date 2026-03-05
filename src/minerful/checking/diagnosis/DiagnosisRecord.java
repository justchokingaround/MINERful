package minerful.checking.diagnosis;

public class DiagnosisRecord implements Comparable<DiagnosisRecord> {
	
	public final DiagnosisStatus status;
	public final DiagnosisCell trigger;
	public final DiagnosisCell validator;
	
	public DiagnosisRecord(DiagnosisStatus status, DiagnosisCell trigger, DiagnosisCell validator) {
		this.status = status;
		this.trigger = trigger;
		// We do not necessarily pair the activation and the target.
		// Take ChainResponse(a,b). If we read a segment <a,c>, c is not the target, yet it violates the constraint.
		// Take Precedence(a,b). If we read a segment <a,b>, a is not the activator, yet it contributes to the satisfaction of the constraint.
		this.validator = validator;
	}

	@Override
	public String toString() {
		return "DiagnosisPair [status=" + status + ", trigger=" + trigger + ", validator=" + validator + "]";
	}

	@Override
	public int compareTo(DiagnosisRecord other) {
		if (this.status.equals(other.status)) {
			if (this.trigger.equals(other.trigger)) {
				return this.validator.compareTo(other.validator);
			}
			return this.trigger.compareTo(other.trigger);
		}
		return this.status.compareTo(other.status);
	}
}
