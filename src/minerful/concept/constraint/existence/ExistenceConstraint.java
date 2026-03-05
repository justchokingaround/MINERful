package minerful.concept.constraint.existence;

import java.lang.reflect.Constructor;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.ConstraintFamily;
import minerful.checking.ConstraintMonitor;
import minerful.concept.constraint.ConstraintFamily.ExistenceConstraintSubFamily;

public abstract class ExistenceConstraint extends Constraint {
	/**
	 * Existence constraints' activator is the start or end of a trace.
	 * Therefore, their consequent is expressed by the checking automaton's regular expression itself.
	 */
	@Override
	public ConstraintMonitor[] getMonitors() {
		return new ConstraintMonitor[] {new ConstraintMonitor(this, getRegularExpressionTemplate())};
	}

    protected ExistenceConstraint() {
    	super();
    }
	
    public ExistenceConstraint(TaskChar param1) {
        super(param1);
    }
	public ExistenceConstraint(TaskCharSet param1) {
		super(param1);
	}

	public static String toExistenceQuantifiersString(AtLeast1 least, AtMost1 atMost) {
        String min = "0",
                max = "*";
        if (least != null) {
            min = "1";
        }
        if (atMost != null) {
            max = "*";
        }
        return "[ " + min + " ... " + max + " ]";
    }

    @Override
    public int compareTo(Constraint t) {
        int result = super.compareTo(t);
        if (result == 0) {
            return this.getClass().getCanonicalName().compareTo(
                    t.getClass().getCanonicalName());
        }
        return result;
    }
    

    @Override
    public String toString() {
        return super.toString();
    }

	@Override
	public TaskCharSet getImplied() {
		return null;
	}

    @Override
    public ConstraintFamily getFamily() {
        return ConstraintFamily.EXISTENCE;
    }

    @Override
    public ExistenceConstraintSubFamily getSubFamily() {
        return ExistenceConstraintSubFamily.NONE;
    }

	@Override
	public boolean checkParams(TaskChar... taskChars) throws IllegalArgumentException {
		if (taskChars.length > 1)
			throw new IllegalArgumentException("Too many parameters");
		return true;
	}

	@Override
	public boolean checkParams(TaskCharSet... taskCharSets) throws IllegalArgumentException {
		if (taskCharSets.length > 1)
			throw new IllegalArgumentException("Too many parameters");
		return true;
	}
	
	@Override
	public Map<TaskChar, TaskChar> getSymbolicMap() {
		Map<TaskChar, TaskChar> map = new HashMap<TaskChar, TaskChar>(this.getInvolvedTaskChars().size(), 
				(float)1.0);
		for (TaskChar tCh : this.getParameters().getFirst().getTaskCharsArray()) {
			map.put(tCh, TaskChar.SYMBOLIC_TASKCHARS[0]);
		}
		return map;
	}	
	
	/**
	 * This method uses heavy Java reflection tricks to generate constraints that
	 *  symbolically abstracts from the actual parameter.
	 */
	public Constraint getSymbolic() {
		Constraint con = null;
		Constructor<? extends Constraint> constructor = null;
		Class<? extends Constraint> template = null;		
		try {
			template = getClass();
			constructor = template.getConstructor(TaskChar.class);
			con = constructor.newInstance(TaskChar.SYMBOLIC_TASKCHARS[0]);
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return con;
	}
}