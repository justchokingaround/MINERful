package minerful.checking.diagnosis;

import java.util.Set;
import java.util.TreeSet;

import minerful.concept.ProcessSpecification;
import minerful.concept.constraint.Constraint;

public class ProcessSpecificationDiagnosis {
	public final Set<ConstraintDiagnosis> cnsDiagnoses;
	
	public ProcessSpecificationDiagnosis() {
		this.cnsDiagnoses = new TreeSet<ConstraintDiagnosis>();
	}
}