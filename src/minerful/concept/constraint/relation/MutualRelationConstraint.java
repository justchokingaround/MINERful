package minerful.concept.constraint.relation;

import minerful.checking.ConstraintMonitor;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily.ConstraintImplicationVerse;
import minerful.concept.constraint.ConstraintFamily.RelationConstraintSubFamily;

public abstract class MutualRelationConstraint extends RelationConstraint {
	protected RelationConstraint forwardConstraint;
	protected RelationConstraint backwardConstraint;
	
	/**
	 * Mutual relation constraints are the smallest non-trivial specifications.
	 * They consist of a pair: the {@link #forwardConstraint forwardConstraint} and the {@link #backwardConstraint backwardConstraint}.
	 * Therefore, their monitors are directly taken from there.
	 */
	@Override
	public ConstraintMonitor[] getMonitors() {
		ConstraintMonitor[] monitors = new ConstraintMonitor[forwardConstraint.getMonitors().length + backwardConstraint.getMonitors().length];
		int i = 0;
		for (ConstraintMonitor cMon : forwardConstraint.getMonitors()) {
			monitors[i++] = cMon;
		}
		for (ConstraintMonitor cMon : backwardConstraint.getMonitors()) {
			monitors[i++] = cMon;
		}
		return monitors;
	}

	public MutualRelationConstraint() {
		super();
	}
	
	public MutualRelationConstraint(TaskCharSet param1, TaskCharSet param2) {
		super(param1, param2);
	}

	public MutualRelationConstraint(TaskChar param1, TaskChar param2) {
		super(param1, param2);
	}

	@Override
	public ConstraintImplicationVerse getImplicationVerse() {
		return ConstraintImplicationVerse.BOTH;
	}

	@Override
	public RelationConstraintSubFamily getSubFamily() {
	    return RelationConstraintSubFamily.POSITIVE_MUTUAL;
	}

	@Override
	public int getHierarchyLevel() {
	    return super.getHierarchyLevel() + 1;
	}

	public RelationConstraint getForwardConstraint() {
	    return forwardConstraint;
	}

	public RelationConstraint getBackwardConstraint() {
	    return backwardConstraint;
	}
	
	public boolean hasForwardConstraint() {
	    return forwardConstraint != null;
	}
	
	public boolean hasBackwardConstraint() {
	    return backwardConstraint != null;
	}
	
	@Override
	public TaskCharSet[] getTargets() {
		return new TaskCharSet[] { getBase(), getImplied() };
	}

	@Override
	public TaskCharSet[] getActivators() {
		return new TaskCharSet[] { getImplied(), getBase() };
	}

	public void setImplyingConstraints(RelationConstraint forwardConstraint, RelationConstraint backwardConstraint) {
		this.forwardConstraint = forwardConstraint;
		this.backwardConstraint = backwardConstraint;
	}

	public boolean isAsInformativeAsTheImplyingConstraints() {
	    return	this.evtBasedMeasures.confidence >= forwardConstraint.getEventBasedMeasures().getConfidence() &&
	            this.evtBasedMeasures.confidence >= backwardConstraint.getEventBasedMeasures().getConfidence();
	}

	public boolean isMoreInformativeThanAnyOfImplyingConstraints() {
	    return	this.evtBasedMeasures.confidence >= forwardConstraint.getEventBasedMeasures().getConfidence() ||
	            this.evtBasedMeasures.confidence >= backwardConstraint.getEventBasedMeasures().getConfidence();
	}

	public boolean isMoreInformativeThanForwardConstraint() {
	    return	this.evtBasedMeasures.confidence >= forwardConstraint.getEventBasedMeasures().getConfidence();
	}

	public boolean isMoreInformativeThanBackwardConstraints() {
	    return	this.evtBasedMeasures.confidence >= backwardConstraint.getEventBasedMeasures().getConfidence();
	}

	protected boolean ckeckConsistency(RelationConstraint forwardConstraint, RelationConstraint backwardConstraint) {
	    return     forwardConstraint.getParameters().containsAll(backwardConstraint.getParameters())
	    		&&	backwardConstraint.getParameters().containsAll(forwardConstraint.getParameters())
	            &&  this.getHierarchyLevel() == forwardConstraint.getHierarchyLevel()
	            &&  this.getHierarchyLevel() == backwardConstraint.getHierarchyLevel();
	}

	public boolean hasImplyingConstraints() {
		return	this.forwardConstraint != null &&
				this.backwardConstraint != null;
	}

	public void setForwardConstraint(RelationConstraint forwardConstraint) {
		this.forwardConstraint = forwardConstraint;
	}

	public void setBackwardConstraint(RelationConstraint backwardConstraint) {
		this.backwardConstraint = backwardConstraint;
	}

	public RelationConstraint getPossibleForwardConstraint() {
		if (this.hasForwardConstraint())
			return this.getForwardConstraint();
		return null;
	}

	public RelationConstraint getPossibleBackwardConstraint() {
		if (this.hasBackwardConstraint())
			return this.getBackwardConstraint();
		return null;
	}

	@Override
	public Constraint[] suggestImpliedConstraints() {
		Constraint[] impliCons = null;
		Constraint[] inheritedImpliCons = super.suggestImpliedConstraints();
		int i = 0;

		if (inheritedImpliCons != null) {
			impliCons = new Constraint[inheritedImpliCons.length + 2];
			for (Constraint impliCon : inheritedImpliCons) {
				impliCons[i++] = impliCon;
			}
		} else {
			impliCons = new Constraint[2];
		}
		impliCons[i++] = getPossibleForwardConstraint();
		impliCons[i++] = getPossibleBackwardConstraint();
		
		return impliCons;
	}
}