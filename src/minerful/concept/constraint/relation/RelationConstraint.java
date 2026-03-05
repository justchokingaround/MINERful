/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful.concept.constraint.relation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import minerful.automaton.AutomatonFactory;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily;
import minerful.concept.constraint.ConstraintFamily.ConstraintImplicationVerse;
import minerful.concept.constraint.ConstraintFamily.RelationConstraintSubFamily;

public abstract class RelationConstraint extends Constraint {
	public static enum ImplicationVerse {
		FORWARD,
		BACKWARD,
		BOTH
	}

	protected TaskCharSet implied;
	
	protected RelationConstraint() {
		super();
	}

    public RelationConstraint(TaskCharSet param1, TaskCharSet param2) {
        super(param1, param2);
        super.setSilentToObservers(true);
        this.implied = param2;
        super.setSilentToObservers(false);
    }
    public RelationConstraint(TaskChar param1, TaskChar param2) {
    	super(param1, param2);
        super.setSilentToObservers(true);
        this.implied = new TaskCharSet(param2);
        super.setSilentToObservers(false);
    }

    @Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((implied == null) ? 0 : implied.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		RelationConstraint other = (RelationConstraint) obj;
		if (implied == null) {
			if (other.implied != null)
				return false;
		} else if (!implied.equals(other.implied))
			return false;
		return true;
	}

    @Override
    public int compareTo(Constraint o) {
        int result = super.compareTo(o);
        if (result == 0) {
            if (o instanceof RelationConstraint) {
                RelationConstraint other = (RelationConstraint) o;
            	result = this.getFamily().compareTo(other.getFamily());
            	if (result == 0) {
            		result = this.getSubFamily().compareTo(other.getSubFamily());
            		if (result == 0) {
            			result = this.getImplicationVerse().compareTo(other.getImplicationVerse());
	            		if (result == 0) {
	            			result = this.getTemplateName().compareTo(other.getTemplateName());
	            			if (result != 0) {
	                            if (this.getClass().isInstance(o)) {
	                            	result = -1;
	                            } else if (o.getClass().isInstance(this)) {
	                            	result = +1;
	                            } else {
	                            	result = 0;
	                            }
	            			}
	            		}
            		}
            	}
            } else {
                result = 1;
            }
        }
    	return result;
    }
    
    @Override
    public ConstraintFamily getFamily() {
        return ConstraintFamily.RELATION;
    }

    @SuppressWarnings("unchecked")
    @Override
    public RelationConstraintSubFamily getSubFamily() {
        return RelationConstraintSubFamily.NONE;
    }
    
    @Override
    public int getHierarchyLevel() {
        return super.getHierarchyLevel()+1;
    }

    public boolean regardsTheSameChars(RelationConstraint relCon) {
        return
                    this.base.equals(relCon.base)
                &&  this.implied.equals(relCon.implied);
    }
    
    @Override
	public TaskCharSet getImplied() {
    	return this.implied;
    }

	@Override
	public String getRegularExpression() {
		return String.format(this.getRegularExpressionTemplate(), base.toPatternString(), implied.toPatternString());
	}

	@Override
	public String getLTLpfExpression() {
		return String.format(this.getLTLpfExpressionTemplate(), base.toLTLpfString(), implied.toLTLpfString());
	}
	
	@Override
	public String getViolatingRegularExpression() {
		return String.format(this.getViolatingRegularExpressionTemplate(), base.toPatternString(), implied.toPatternString());
	}

	@Override
	public String getViolatingLTLpfExpression() {
		return String.format(this.getViolatingLTLpfExpressionTemplate(), base.toLTLpfString(), implied.toLTLpfString());
	}
	
	public abstract ConstraintImplicationVerse getImplicationVerse();
	
	public boolean isActivationBranched() {
		return this.base.size() > 1 && this.implied.size() < 2;
	}

	public boolean isTargetBranched() {
		return this.implied.size() > 1 && this.base.size() < 2;
	}
	
	public boolean isBranchedBothWays() {
		return this.isActivationBranched() && this.isTargetBranched();
	}
	
	public boolean hasActivationSetStrictlyIncludingTheOneOf(Constraint c) {
		return
				this.isActivationBranched()
			&&	this.base.strictlyIncludes(c.getBase());
	}
	
	public boolean hasTargetSetStrictlyIncludingTheOneOf(Constraint c) {
		return
				this.isTargetBranched()
			&&	this.implied.strictlyIncludes(c.getImplied());
	}

	@Override
	public boolean isDescendantAlongSameBranchOf(Constraint c) {
		if (super.isDescendantAlongSameBranchOf(c) == false) {
			return false;
		}
		if (!(c instanceof RelationConstraint)) {
			return false;
		}
		RelationConstraint relaCon = ((RelationConstraint)c);
		return
				this.getImplicationVerse() == relaCon.getImplicationVerse()
			// FIXME This is a trick which could be inconsistent with possible specification extensions
			||	relaCon.getClass().equals(RespondedExistence.class);
	}

	@Override
	public boolean isTemplateDescendantAlongSameBranchOf(Constraint c) {
		if (super.isTemplateDescendantAlongSameBranchOf(c) == false) {
			return false;
		}
		if (!(c instanceof RelationConstraint)) {
			return false;
		}
		RelationConstraint relaCon = ((RelationConstraint)c);
		return
				this.getImplicationVerse() == relaCon.getImplicationVerse()
			// FIXME This is a trick which could be inconsistent with possible specification extensions
			||	relaCon.getClass().equals(RespondedExistence.class);
	}
	
	// @Override
	// protected void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
	// 	if (this.getFamily().equals(ConstraintFamily.RELATION)) {
	// 			this.base = this.getParameters().get(0);
	// 			this.implied = this.getParameters().get(1);
	// 	}
	// }

	@Override
	public String getRegularExpressionTemplate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getViolatingRegularExpressionTemplate() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public Constraint suggestConstraintWhichThisShouldBeBasedUpon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean checkParams(TaskChar... taskChars)
			throws IllegalArgumentException {
		if (taskChars.length != 2)
			throw new IllegalArgumentException("Too many parameters");
		return true;
	}

	@Override
	public boolean checkParams(TaskCharSet... taskCharSets)
			throws IllegalArgumentException {
		if (taskCharSets.length != 2)
			throw new IllegalArgumentException("Too many parameters");
		return true;
	}
	
	
	@Override
	public TaskCharSet[] getTargets() {
		return new TaskCharSet[] { getImplied() };
	}

	@Override
	public TaskCharSet[] getActivators() {
		return new TaskCharSet[] { getBase() };
	}
	
	/**
	 * Returns <code>true</code> if and only if there is an overlap between TaskChars in the parameters.
	 * @return <code>true</code> if there is an overlap between TaskChars in the parameters.
	 * 	<code>false</code> otherwise.
	 */
	public boolean doActualParametersOverlap() {
		return this.getOverlappingParameterTaskChars().size() > 0;
	}
	
	/**
	 * Returns the set of TaskChars shared between the parameters.
	 * @return The set of TaskChars shared between the parameters. The set is empty if there is no such TaskChar.
	 */
	public Set<TaskChar> getOverlappingParameterTaskChars() {
		Set<TaskChar> overlap = new TreeSet<TaskChar>();
		TaskChar[] taskChars1 = this.getParameters().getFirst().getTaskCharsArray();
		Set<TaskChar> taskChars2 = this.getParameters().getLast().getSetOfTaskChars();
		for (TaskChar tCh : taskChars1) {
			if (taskChars2.contains(tCh)) {
				overlap.add(tCh);
			}
		}
		return overlap;
	}

	/**
	 * Notice that a separate TaskChar is to be used
	 * 	for actual parameters that share some taskChars (e.g., 
	 * 	Response({A,B,C}{B,C,D})
	 *  share {B,C} among parameters, thus introducing potential nondeterminism
	 *  if we are to run it to replay a trace on a symbolic automaton
	 *  (when reading B in <A, B, ...>, which of the two symbols are
	 *  considered for the transition?).
	 * @return A map from the actual TaskChar to the symbolic TaskChar
	 */
	@Override
	public Map<TaskChar, TaskChar> getSymbolicMap() {
		Map<TaskChar, TaskChar> map = new HashMap<TaskChar, TaskChar>(this.getInvolvedTaskChars().size(), 
				(float)1.0);
//		Set<TaskChar> overlap = getOverlappingParameterTaskChars();
		for (TaskChar tCh : this.getParameters().getFirst().getTaskCharsArray()) {
//			if (!overlap.contains(tCh)) {
				map.put(tCh, TaskChar.SYMBOLIC_TASKCHARS[0]);
//			} else {
//				map.put(tCh, TaskChar.SYMBOLIC_TASKCHARS[2]);
//			}
		}
		for (TaskChar tCh : this.getParameters().getLast().getTaskCharsArray()) {
//			if (!overlap.contains(tCh)) {
				map.put(tCh, TaskChar.SYMBOLIC_TASKCHARS[1]);
//			}
		}
		return map;
	}
	/* OLD IDEA, which does not work because it introduces a misinterpretation of parameters and formulae.
	 * Take Response({A,B},{B}) as a clarifying example: It is totally inconsistent, yet the symbolic automaton
	 * would make room for it.
	 * Notice that a separate TaskChar ({@link TaskChar#SYMBOLIC_TASKCHARS TaskChar#SYMBOLIC_TASKCHARS[2]})
	 * 	is used for actual parameters that are shared among parameters. For instance, 
	 * 	Response({A,B,C}{B,C,D})
	 *  shares {B,C} among parameters. In this case, {A} is mapped to 
     *  {@link TaskChar#SYMBOLIC_TASKCHARS TaskChar#SYMBOLIC_TASKCHARS[0]},
     *  {D} is mapped to 
     *  {@link TaskChar#SYMBOLIC_TASKCHARS TaskChar#SYMBOLIC_TASKCHARS[1]}
     *  and {B,C} are mapped to
     *  {@link TaskChar#SYMBOLIC_TASKCHARS TaskChar#SYMBOLIC_TASKCHARS[2]}.
     *  This strategy is meant to avoid introducing potential nondeterminism
	 *  if we are to run it to replay a trace on a symbolic automaton while mapping
	 *  a TaskChar to two symbolic characters. This would be in the case, in our example,
	 *  when reading B in <A, B, ...>: which of the two symbols should be
	 *  considered for the transition?
	 */
	
	/**
	 * This method uses heavy Java reflection tricks to generate constraints that
	 *  symbolically abstracts from the actual parameter.
	 *  For instance, given the constraint
	 * 	Response({A,B}{C,D})
	 *  {A,B} are mapped to 
     *  {@link TaskChar#SYMBOLIC_TASKCHARS TaskChar#SYMBOLIC_TASKCHARS[0]},
     *  and {C,D} are mapped to
     *  {@link TaskChar#SYMBOLIC_TASKCHARS TaskChar#SYMBOLIC_TASKCHARS[2]}.
	 */
	public Constraint getSymbolic() {
		Constraint con = null;
		Constructor<? extends Constraint> constructor = null;
		Class<? extends Constraint> template = null;
		try {
			template = getClass();
			constructor = template.getConstructor(TaskChar.class, TaskChar.class);
			con = constructor.newInstance(TaskChar.SYMBOLIC_TASKCHARS[0], TaskChar.SYMBOLIC_TASKCHARS[1]);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return con;
	}
	
//	public static void main(String[] params) {
//		System.out.println(new AlternatePrecedence(new TaskChar('A'), new TaskChar('B')).getSymbolic().getRegularExpression());
//		System.out.println(new AlternatePrecedence(new TaskChar('A'), new TaskChar('B')).getSymbolic());
//		System.out.println(AutomatonFactory.buildAutomatonWithWildcard(new AlternatePrecedence(new TaskChar('A'), new TaskChar('B')).getSymbolic()).toDot());
//		System.out.println(new Response(new TaskCharSet(new TaskChar('A'), new TaskChar('B')), new TaskCharSet(new TaskChar('B'))).getSymbolic());
//		System.out.println(AutomatonFactory.buildAutomatonWithWildcard(new Response(new TaskCharSet(new TaskChar('A'), new TaskChar('B')), new TaskCharSet(new TaskChar('B'))).getSymbolic()).toDot());
//	}
}