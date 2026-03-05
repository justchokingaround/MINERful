package minerful.automaton.concept.symbolic;

import dk.brics.automaton.State;
import minerful.automaton.concept.AnnotatedAutomaton;
import minerful.concept.AbstractTaskClass;
import minerful.concept.constraint.Constraint;
import minerful.io.encdec.TaskCharEncoderDecoder;
import minerful.utils.MessagePrinter;

public class SymbolicConstraintAnnotatedAutomatonWalker {
	public static enum EvaluationStatus {
		INACTIVE,
		TEMPORARILY_VIOLATED,
		VIOLATED,
		SATISFIED,
		PERMANENTLY_SATISFIED
	}
	
	public static MessagePrinter logger = MessagePrinter.getInstance(SymbolicConstraintAnnotatedAutomatonWalker.class);
	public final Constraint cns;
	private State initialState;
	private State currentState;
	private AnnotatedAutomaton annAut;
	private EvaluationStatus evaluation;
	private SymbolicConstraintTaskClassTranslator symbolTranslator;
	
	public SymbolicConstraintAnnotatedAutomatonWalker(Constraint cns) {
		this.cns = cns;
		this.symbolTranslator = new SymbolicConstraintTaskClassTranslator(cns);
		this.annAut = SymbolicConstraintAnnotatedAutomatonFactory.getSymbolicConstraintAnnotatedAutomaton(cns);
		this.initialState = annAut.automaton.getInitialState();
		this.resetRun();
	}
	
	/**
	 * Steps according to the read TaskClass.
	 * @param taskClass
	 * @return
	 */
	public EvaluationStatus step(AbstractTaskClass taskClass) {
		logger.debug(this.cns.toString() + " reads " + taskClass);
		
		State necState = null;
		Character stepChar = null;
		
		if (this.symbolTranslator.contains(taskClass)) { // Is this TaskClass in the scope of the constraint?
			stepChar = this.symbolTranslator.getSymbol(taskClass);
			necState = this.currentState.step(stepChar);
			logger.debug("currentStateInTheWalk.getTransitions(): " + this.currentState.getTransitions());
			logger.debug(taskClass + " => " + stepChar + " is involved in this constraint (" + this.cns + ") and leads to " + (necState!=null?necState:null));
		} else {
			logger.debug(taskClass + " is not involved in this constraint (" + this.cns + ")");
			necState = this.currentState.step(TaskCharEncoderDecoder.WILDCARD_CHAR);
		}
		
		if (necState != null) { // If the requested transition was not available, due to automata minimisation, it means there was a violation
			this.currentState = necState;
			if (annAut.isPermanentlySatisfying(necState)) {
				this.evaluation = EvaluationStatus.PERMANENTLY_SATISFIED;
			} else if (currentState.isAccept()) {
				this.evaluation = EvaluationStatus.SATISFIED;
			} else {
				this.evaluation = EvaluationStatus.TEMPORARILY_VIOLATED;
			}
		} else {
			this.evaluation = EvaluationStatus.VIOLATED;
		}
		return this.evaluation;
	}
	
	public boolean currentStateAnnotationContains(AbstractTaskClass taskClass) {
		if (annAut.getStateAnnotation(currentState) != null) {
			return annAut.getStateAnnotation(currentState).contains(this.symbolTranslator.getSymbol(taskClass));
		}
		return false;
	}
	
	public void resetRun() {
		this.evaluation = EvaluationStatus.INACTIVE;
		this.currentState = this.initialState;
	}
	
	/**
	 * Returns <code>true</code> if the current state is non-accepting, and with the latest move the run ends in a virtual sink node.
	 * @return
	 */
	public boolean isInTrapState() {
		return evaluation == EvaluationStatus.VIOLATED && !currentState.isAccept();
	}

}
