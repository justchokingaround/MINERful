package minerful.checking.diagnosis;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import minerful.concept.constraint.Constraint;

public class ConstraintDiagnosis implements Comparable<ConstraintDiagnosis> {
	public final Constraint cns;
	/**
	 * Retains all activators
	 */
	public final Queue<LogCoordinates> allActivators;
	/**
	 * Retains all targets
	 */
	public final Stack<LogCoordinates> allTargets;
	private final Set<DiagnosisRecord> diagnosisRecords;
	private final Set<DiagnosisRecord> satisfactions;
	private final Set<DiagnosisRecord> violations;
	
	public ConstraintDiagnosis(Constraint cns) {
		this.cns = cns;
		allActivators = new LinkedList<LogCoordinates>();
		allTargets = new Stack<LogCoordinates>();
		diagnosisRecords = new TreeSet<DiagnosisRecord>();
		satisfactions = new TreeSet<DiagnosisRecord>();
		violations = new TreeSet<DiagnosisRecord>();
	}
	
	public Set<DiagnosisRecord> getDiagnosisRecords() { return diagnosisRecords; }
	public Set<DiagnosisRecord> getSatisfactions() { return satisfactions; }
	public Set<DiagnosisRecord> getViolations() { return violations; }
	
	public boolean addDiagnosisRecord(DiagnosisRecord diagRec) {
		boolean added = this.diagnosisRecords.add(diagRec);
		if (added) {
			switch (diagRec.status) {
			case VIOLATES:
				this.violations.add(diagRec);
				break;
			case SATISFIES:
				this.satisfactions.add(diagRec);
				break;
			case IS_PENDING:
				default:
					break;
			}
		}
		return added;
	}

	@Override
	public int compareTo(ConstraintDiagnosis other) {
		return this.cns.compareTo(other.cns);
	}

	@Override
	public boolean equals(Object other) {
		try { return this.cns.equals(((ConstraintDiagnosis) other).cns); } catch (ClassCastException e) { return false; }
	}
}