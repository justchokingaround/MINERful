package minerful.checking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.BasicConfigurator;

import minerful.automaton.concept.symbolic.SymbolicConstraintAnnotatedAutomatonWalker;
import minerful.automaton.concept.symbolic.SymbolicConstraintAnnotatedAutomatonWalker.EvaluationStatus;
import minerful.concept.AbstractTaskClass;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.relation.Succession;
import minerful.logparser.CharTaskClass;
import minerful.utils.MessagePrinter;

public class IncorrectConstraintMonitorWalker {
	private static MessagePrinter logger = MessagePrinter.getInstance(IncorrectConstraintMonitorWalker.class);
		
	public final Constraint cns;
	public final SymbolicConstraintAnnotatedAutomatonWalker autoWalk;
	/**
	 * An array of sets of activators' TaskClasses.
	 * The array is due to the fact that
	 * Succession(A,B), e.g., has A and B both as activators and targets.
	 * Thus it has two activators: A and B, indeed.
	 * The activators are sets to cater for branched constraints.
	 * Succession({A,B}, C), e.g., has {A,B} as an activator
	 * (and as a target, too).
	 */
	private ArrayList<Set<AbstractTaskClass>> activatorTaskClasses;
	/**
	 * An array of sets of targets' TaskClasses.
	 * The array is due to the fact that
	 * Succession(A,B), e.g., has A and B both as activators and targets.
	 * Thus it has two targets: A and B, indeed.
	 * The activators are sets to cater for branched constraints.
	 * Succession(C, {A,B}), e.g., has {A,B} as a target
	 * (and as an activator, too).
	 */
	private ArrayList<Set<AbstractTaskClass>> targetTaskClasses;
	
	/**
	 * The union of all 
	 * {@link #activatorTaskClasses activatorTaskClasses}.
	 */
	private HashSet<AbstractTaskClass> allActivatorTaskClasses;
	/**
	 * The union of all 
	 * {@link #targetTaskClasses targetTaskClasses}.
	 */
	private HashSet<AbstractTaskClass> allTargetTaskClasses;
	
	private Integer traversedActivatorSetIndex;
	private Integer traversedTargetSetIndex;
	private AbstractTaskClass traversedActivatorTaskClass;
	private AbstractTaskClass traversedTargetTaskClass;
	private EvaluationStatus status;
	private boolean currentStepActivation;
	private boolean currentStepTarget;
	private StepTypeInterpretation stepTypeInterpretation;
	
	public enum StepTypeInterpretation {
		ACTIVATION,
		TARGET,
		DONT_CARE
	}
	
	public IncorrectConstraintMonitorWalker(Constraint cns) {
		this.cns = cns;
		this.autoWalk = new SymbolicConstraintAnnotatedAutomatonWalker(cns);
		if (cns.getActivators() != null) {
			this.activatorTaskClasses = new ArrayList<Set<AbstractTaskClass>>(cns.getActivators().length);
		} else {
			this.activatorTaskClasses = new ArrayList<Set<AbstractTaskClass>>();
		}
		if (cns.getTargets() != null) {
			this.targetTaskClasses = new ArrayList<Set<AbstractTaskClass>>(cns.getTargets().length);
		} else {
			this.targetTaskClasses = new ArrayList<Set<AbstractTaskClass>>();
		}
		
		// Prepare the sets of activator and target AbstractTaskClasses
		int i = 0, k = 0;
		if (cns.getActivators() != null) {
			for (TaskCharSet activatorSet : cns.getActivators()) {
				activatorTaskClasses.add((Set<AbstractTaskClass>) new HashSet<AbstractTaskClass>(activatorSet.getTaskCharsArray().length, 1));
				for (TaskChar activatorTaskCh : activatorSet.getTaskCharsArray()) {
					activatorTaskClasses.get(i).add(activatorTaskCh.taskClass);
				}
				i++;
				k++;
			}
		}
		allActivatorTaskClasses = new HashSet<AbstractTaskClass>(k, 1);
		for (; i > 0; i--) {
			allActivatorTaskClasses.addAll(activatorTaskClasses.get(i-1));
		}
		k = 0;
		if (cns.getTargets() != null) {
			for (TaskCharSet targetSet : cns.getTargets()) {
				targetTaskClasses.add((Set<AbstractTaskClass>) new HashSet<AbstractTaskClass>(targetSet.getTaskCharsArray().length, 1));
				for (TaskChar targetTaskCh : targetSet.getTaskCharsArray()) {
					targetTaskClasses.get(i).add(targetTaskCh.taskClass);
				}
				i++;
				k++;
			}
		}
		allTargetTaskClasses = new HashSet<AbstractTaskClass>(k, 1);
		for (; i > 0; i--) {
			allTargetTaskClasses.addAll(targetTaskClasses.get(i-1));
		}
	}
	
	public void step(AbstractTaskClass taskClass) {
		/*
		 	Decorate all states with the set of activities that led to it EXCLUDING cycles and loops
			Replay the trace
			CYCLE_START:
			Read symbol
			Read the new state's decoration and forget to have met an activation or a target if they miss from the decoration
			If an activation is met, append the location (trace#, evt#) and remember you met an activation
				If the state is accepting, check that also the target was met (see the decoration on the state and your memory)
					If so, store acceptance for all activations in the stack and EMPTY the stack
					Otherwise, store ?
			If a target is met, store the latest location (trace#, evt#)
				If the state is accepting, check that also the activation was met (see the decoration on the state and your memory)
					If so, store acceptance for all activations in the stack and EMPTY the stack
					Otherwise, ciccia
			GOTO CYCLE_START
		 */
		
		// Notice that with mutual constraints like Succession(a,b)
		//  both a and b mark activations and targets.
		//  However, they should not count as both at every single occurrence.
		//  Hence the following IF-nesting.
		//  We give priority to the activation, signing it first as traversed.
		//
		//	By default, the step type is that of a don't-care move
		stepTypeInterpretation = StepTypeInterpretation.DONT_CARE;
		Integer i = null, j = null;

		maskPastActivatorsAndTargets();
		
		// Is activationTraversed false?
		if (!hasTraversedActivations()) {
			i = searchActivator(taskClass); // Is it an activator?
			if (i != null) {
				logger.debug("rememberTraversedActivator number " + (i+1) + " by " + taskClass);
				rememberTraversedActivator(i, taskClass); // Remember the interpretation as an activator
			} else {
				j = searchTarget(taskClass); // Is it a target?
				if (j != null) {
					rememberTraversedTarget(j, taskClass);  // Remember the interpretation as a target
				}
			}
		} else { // Is activationTraversed true?
			i = searchActivator(taskClass); // Is it an activator?
			if (i != null && i == traversedActivatorSetIndex) { // Is it the same activator as before?
				rememberTraversedActivator(i, taskClass);
			} else { // Is activationTraversed true but it is not the same activator
				j = searchTarget(taskClass); // Is it a target?
				if (j != null) {
					rememberTraversedTarget(j, taskClass);
				} else {
					if (i != null) { // If it is not a target, but another activator (not the same as before, which is difficult)
						rememberTraversedActivator(i, taskClass);
					}
				}
			}
		}
		currentStepActivation = this.allActivatorTaskClasses.contains(taskClass);
		currentStepTarget = this.allTargetTaskClasses.contains(taskClass);

		status = autoWalk.step(taskClass);
		logger.debug(String.format("Activated? %b. Target traversed? %b. Is this step an activation? %b. Is this step a target? %b. Just interpreted as: %s. Status: %s",
				hasTraversedActivations(), hasTraversedTargets(), isCurrentStepActivation(), isCurrentStepTarget(), getStepTypeInterpretation(), getStatus()));
	}

	private void maskPastActivatorsAndTargets() {
		// In the new state, check if the info that the activation was traversed can be retained
		if (hasTraversedActivations() && !autoWalk.currentStateAnnotationContains(traversedActivatorTaskClass)) {
			forgetTraversedActivator();
		}
		if (hasTraversedTargets() && !autoWalk.currentStateAnnotationContains(traversedTargetTaskClass)) {
			forgetTraversedTarget();
		}
	}

	public boolean isCurrentStepActivation() {
		return currentStepActivation;
	}

	public boolean isCurrentStepTarget() {
		return currentStepTarget;
	}

	private void forgetTraversedActivator() {
		logger.debug("Forgetting the traversed activator");
		traversedActivatorSetIndex = null;
		traversedActivatorTaskClass = null;
	}

	private void forgetTraversedTarget() {
		logger.debug("Forgetting the traversed target");
		traversedTargetSetIndex = null;
		traversedTargetTaskClass = null;
	}

	private void rememberTraversedActivator(Integer i, AbstractTaskClass taskClass) {
		traversedActivatorSetIndex = i;
		traversedActivatorTaskClass = taskClass;
		stepTypeInterpretation = StepTypeInterpretation.ACTIVATION;
	}

	private void rememberTraversedTarget(Integer j, AbstractTaskClass taskClass) {
		traversedTargetSetIndex = j;
		logger.debug("traversedTargetSetIndex " + traversedTargetSetIndex);
		traversedTargetTaskClass = taskClass;
		stepTypeInterpretation = StepTypeInterpretation.TARGET;
	}

	private Integer searchActivator(AbstractTaskClass taskClass) {
		for (int i = 0; i < activatorTaskClasses.size(); i++) {
			logger.debug("Looking for " + taskClass + " in activator " + (i+1) + ": " + activatorTaskClasses.get(i));
			if (activatorTaskClasses.get(i).contains(taskClass)) {
				return i;
			}
		}
		return null;
	}

	private Integer searchTarget(AbstractTaskClass taskClass) {
		for (int j = 0; j < targetTaskClasses.size(); j++) {
			if (targetTaskClasses.get(j).contains(taskClass)) {
				return j;
			}
		}
		return null;
	}
	

	public boolean isCurrentStateSatisfying() {
		return getStatus() == EvaluationStatus.SATISFIED || getStatus() == EvaluationStatus.PERMANENTLY_SATISFIED;
	}

	public void resetRun() {
		autoWalk.resetRun();
		traversedActivatorSetIndex = null;
		traversedTargetSetIndex = null;
	}

	public boolean hasTraversedActivations() {
		return traversedActivatorSetIndex != null;
	}
	public boolean hasTraversedTargets() {
		return traversedTargetSetIndex != null;
	}
	public EvaluationStatus getStatus() {
		return status;
	}
	/**
	 * Returns the interpretation of the given step type. 
	 * The idea is that if we have a mutual constraint like Succession(A,B) and a trace like <A,A,B>
	 * the second A should be a reiteration of the same activation, not a target, despite A being both activation and target for that constraint.
	 * @return 
	 */
	public StepTypeInterpretation getStepTypeInterpretation() {
		return stepTypeInterpretation;
	}
	
	public static void main(String[] args) {
		BasicConfigurator.configure();
		IncorrectConstraintMonitorWalker evalu = new IncorrectConstraintMonitorWalker(
				new Succession(
						new TaskChar('A'),new TaskChar('B')));
		CharTaskClass[] steps = {
				new CharTaskClass('B'),
				new CharTaskClass('A'),
				new CharTaskClass('A'),
				new CharTaskClass('C'),
				new CharTaskClass('B'),
				new CharTaskClass('B'),
				new CharTaskClass('A'),
				new CharTaskClass('C'),
				new CharTaskClass('B'),
				new CharTaskClass('B')
		}; 
		for (CharTaskClass step: steps) {
			evalu.step(step);
		}
	}
}