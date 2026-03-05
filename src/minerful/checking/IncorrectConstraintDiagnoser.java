package minerful.checking;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.apache.log4j.BasicConfigurator;

import minerful.automaton.concept.symbolic.SymbolicConstraintAnnotatedAutomatonWalker.EvaluationStatus;
import minerful.checking.diagnosis.ConstraintDiagnosis;
import minerful.checking.diagnosis.DiagnosisCell;
import minerful.checking.diagnosis.DiagnosisRecord;
import minerful.checking.diagnosis.DiagnosisStatus;
import minerful.checking.diagnosis.LogCoordinates;
import minerful.checking.diagnosis.ProcessSpecificationDiagnosis;
import minerful.concept.AbstractTaskClass;
import minerful.concept.TaskChar;
import minerful.concept.TaskCharSet;
import minerful.concept.constraint.Constraint;
import minerful.concept.constraint.existence.AtLeast3;
import minerful.concept.constraint.existence.AtMost2;
import minerful.concept.constraint.relation.AlternatePrecedence;
import minerful.concept.constraint.relation.AlternateSuccession;
import minerful.concept.constraint.relation.ChainResponse;
import minerful.concept.constraint.relation.ChainSuccession;
import minerful.concept.constraint.relation.CoExistence;
import minerful.concept.constraint.relation.NotChainPrecedence;
import minerful.concept.constraint.relation.NotChainResponse;
import minerful.concept.constraint.relation.NotResponse;
import minerful.concept.constraint.relation.Precedence;
import minerful.concept.constraint.relation.Succession;
import minerful.io.encdec.dto.ProcSpecDiagnCmprsDto;
import minerful.logparser.CharTaskClass;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.utils.MessagePrinter;

public class IncorrectConstraintDiagnoser {
	private static MessagePrinter logger = MessagePrinter.getInstance(IncorrectConstraintDiagnoser.class);
	
	private IncorrectConstraintMonitorWalker coWal;
	public final Constraint cns;

	private boolean verdictGiven;
	
	private ConstraintDiagnosis diagnosis;
	/**
	 * Retains the activations for which the evaluation (satisfaction or violation) is still unknown
	 */
	public final Queue<DiagnosisCell> pendingActivations;
	/**
	 * The latest target occurred
	 */
	public LogCoordinates latestTarget;

	private Integer currentCaseNumber = 0;
	private Integer currentEventNumber = 0;
	
	
	public IncorrectConstraintDiagnoser(Constraint cns) {
		this.cns = cns;
		coWal = new IncorrectConstraintMonitorWalker(cns);
		diagnosis = new ConstraintDiagnosis(cns);
		pendingActivations = new LinkedList<DiagnosisCell>();
	}
	
	public ConstraintDiagnosis step(AbstractTaskClass taskClass, Integer caseNo, Integer eventNo) {
		coWal.step(taskClass);
		currentCaseNumber = caseNo;
		currentEventNumber = eventNo;
		
		boolean evaluationNotified = false;
		
		/*
		   For a binary cns to record a sat right on the spot, we need that  
		    the coWal hasTraversedActivations() and hasTraversedTargets()
		     and that the current coWal reads that isCurrentStepActivation().
		   For a binary cns to record a violation right on the spot, we need that
		    the current coWal reads that isCurrentStepActivation().		    
		 */
		if (cns.hasRuntimeActivators()) {
			if (coWal.isCurrentStepTarget()) { // If the current step is a target of a binary cns,
				if (coWal.hasTraversedActivations() 
						&& coWal.hasTraversedTargets() 				 
						&& coWal.isCurrentStateSatisfying()) { // If it leads to a satisfaction, save it for all pending activations
					evaluationNotified = flushPendingActivations(DiagnosisStatus.SATISFIES, caseNo, eventNo);	
				} else if (coWal.getStatus() == EvaluationStatus.VIOLATED) {
					evaluationNotified = flushPendingActivations(DiagnosisStatus.VIOLATES, caseNo, eventNo);	
				}
			}
			if (coWal.isCurrentStepActivation()) {
				if (coWal.hasTraversedActivations() 
						&& coWal.hasTraversedTargets()
						&& coWal.isCurrentStateSatisfying()) {
					// An activation acting also as target may work twice as a satisfying event (for the previous act, and for now).
					storeSatisfactionByActivation(new LogCoordinates(currentCaseNumber, currentEventNumber));
				} else if (coWal.getStatus() == EvaluationStatus.VIOLATED 
						&& !evaluationNotified) {
					// An activation acting also as target should not violate the constraint twice
					storeViolationByActivation(new LogCoordinates(currentCaseNumber, currentEventNumber));
					evaluationNotified = flushPendingActivations(DiagnosisStatus.VIOLATES, caseNo, eventNo);	
				} else {
					pendingActivations.add(new DiagnosisCell(DiagnosisStatus.IS_PENDING, currentCaseNumber, currentEventNumber));					
				}
			} 
			if (!coWal.isCurrentStepActivation() 
					&& !coWal.isCurrentStepTarget() 
					&& coWal.hasTraversedActivations()) {
				if (coWal.isCurrentStateSatisfying()) {
					evaluationNotified = flushPendingActivations(DiagnosisStatus.SATISFIES, caseNo, eventNo);
				} else if (coWal.getStatus() == EvaluationStatus.VIOLATED) {
					evaluationNotified = flushPendingActivations(DiagnosisStatus.VIOLATES, caseNo, eventNo);
				}
			}
		} else {
			/*
			   For a unary cns, we only have targets.
			   We save a sat right on the spot only if we reach a permanent satisfaction, or we violate the constraint.
			   Thereafter, no more checks should be performed.
			*/
			if (!verdictGiven) {
				if (coWal.getStatus() == EvaluationStatus.VIOLATED) {
					storeViolationByTarget(new LogCoordinates(currentCaseNumber, currentEventNumber));
					verdictGiven = true;
				} else if (coWal.getStatus() == EvaluationStatus.PERMANENTLY_SATISFIED) {
					storeSatisfactionByTarget(new LogCoordinates(currentCaseNumber, currentEventNumber));
					verdictGiven = true;
				}
			}
		}

		if (coWal.isCurrentStepActivation()) {
			logger.debug("Recording the occurrence of an activation");
			// Store the activation in the stack
			diagnosis.allActivators.add(new LogCoordinates(currentCaseNumber, currentEventNumber));
		}
		if (coWal.isCurrentStepTarget()) {
			logger.debug("Recording the occurrence of a target");
			// Store the latest target
			latestTarget = new LogCoordinates(currentCaseNumber, currentEventNumber);
			diagnosis.allTargets.add(new LogCoordinates(currentCaseNumber, currentEventNumber));
		}

		// At every violation, should we reset the run? No, otherwise you make a mistake with, say, AtMost2(A) on <A, A, A, A> (adding one satisfaction after 3 A's)
		if (coWal.getStatus() == EvaluationStatus.VIOLATED && !verdictGiven) {
			logger.debug("Resetting the run and restarting with the last action observed");
			restart(taskClass);
		}

		return diagnosis;
	}

	private void storeSatisfactionByTarget(LogCoordinates currentTarget) {
		logger.debug("storeSatisfactionByTarget " + currentTarget);
		diagnosis.addDiagnosisRecord(
				new DiagnosisRecord(DiagnosisStatus.SATISFIES, 
						new DiagnosisCell(DiagnosisStatus.SATISFIES, 
								new LogCoordinates(currentCaseNumber, LogCoordinates.START_OF_TRACE_EVENT_NUMBER)),
						new DiagnosisCell(DiagnosisStatus.SATISFIES, currentTarget)));
	}

	private void storeViolationByTarget(LogCoordinates currentTarget) {
		logger.debug("storeViolationByTarget " + currentTarget);
		diagnosis.addDiagnosisRecord(
				new DiagnosisRecord(DiagnosisStatus.VIOLATES, 
						new DiagnosisCell(DiagnosisStatus.VIOLATES, 
								new LogCoordinates(currentCaseNumber, LogCoordinates.START_OF_TRACE_EVENT_NUMBER)),
						new DiagnosisCell(DiagnosisStatus.VIOLATES, currentTarget)));
	}

	public void finalize() {
		/* 
		 	There could have been activators or targets traversed but still with pending evaluations at the end of a run over a trace.
		 	It is the case of, e.g., NotResponse(A, B) over <A,C,A,D>
		 	and AtMost2(A) over the same above trace (with satisfactions)
		 	or Response(A, B) over the same above trace (with violations).
		 	TODO: Still to be solved! At present this one fails with AtMost2(A) over <A,C,A,C,A,D> since it records both VIO (correct) and SAT (incorrect).
		 	How can we make sure to distinguish this case? For the other templates, the mechanism seems to work.
		 */
		logger.debug("Finalising the pending evaluations");
		if (!verdictGiven) {
			if (!cns.hasRuntimeActivators()) { // This happens with existence constraints: The activation is the start (or end) meta-event of a trace.
				pendingActivations.add(new DiagnosisCell(DiagnosisStatus.IS_PENDING, currentCaseNumber, LogCoordinates.START_OF_TRACE_EVENT_NUMBER));
			}
			switch(coWal.getStatus()) {
			case TEMPORARILY_VIOLATED:
			case VIOLATED:
				flushPendingActivations(DiagnosisStatus.VIOLATES, currentCaseNumber, LogCoordinates.END_OF_TRACE_EVENT_NUMBER);
				break;
			case SATISFIED:
			case PERMANENTLY_SATISFIED:
				flushPendingActivations(DiagnosisStatus.SATISFIES, currentCaseNumber, LogCoordinates.END_OF_TRACE_EVENT_NUMBER);
				break;
			default:
				break;
			}
		}
	}
	
	private boolean flushPendingActivations(DiagnosisStatus status, Integer validatorCaseNo, Integer validatorEventNo) {
		DiagnosisCell pendingActivation;
		DiagnosisCell validationEvent = new DiagnosisCell(status, validatorCaseNo, validatorEventNo);
		DiagnosisCell triggerEvent = null;
		logger.debug(String.format("Flushing pending activations (%d) with %s", pendingActivations.size(), status));
		boolean doFlushPendingActivations = (pendingActivations.size() > 0);
		// For all activations, 
		while (pendingActivations.size() > 0) {
			pendingActivation = pendingActivations.poll(); // Remove the satisfying activation from the stack
			pendingActivation.status = status; // Change its status
			if (pendingActivation.coordinates.equals(validationEvent.coordinates) && latestTarget != null) { // Show where the target is, if available
				triggerEvent = new DiagnosisCell(status, latestTarget);
				validationEvent = pendingActivation;
			} else {
				triggerEvent = pendingActivation;
				validationEvent = new DiagnosisCell(status, validatorCaseNo, validatorEventNo);
			}
			logger.debug("Recording " + status + " with trigger " + triggerEvent + " and validator " + validationEvent);
			diagnosis.addDiagnosisRecord(new DiagnosisRecord(status, triggerEvent, validationEvent)); // Store the satisfying pair
		}
		return doFlushPendingActivations;
	}
	
	private void storeSatisfactionByActivation(LogCoordinates currentActivation) {
		logger.debug("storeSatisfactionByActivation " + currentActivation);
		if (latestTarget != null) {
			diagnosis.addDiagnosisRecord(
					new DiagnosisRecord(DiagnosisStatus.SATISFIES,
							new DiagnosisCell(DiagnosisStatus.SATISFIES, currentActivation),
							new DiagnosisCell(DiagnosisStatus.SATISFIES, latestTarget)));
		} else {
			diagnosis.addDiagnosisRecord(
					new DiagnosisRecord(DiagnosisStatus.SATISFIES,
							new DiagnosisCell(DiagnosisStatus.SATISFIES, currentActivation),
							new DiagnosisCell(DiagnosisStatus.SATISFIES, currentActivation)));
		}
	}
	
	private void storeViolationByActivation(LogCoordinates currentActivation) {
		logger.debug("storeViolationByActivation " + currentActivation);
		if (latestTarget != null) {
			diagnosis.addDiagnosisRecord(
					new DiagnosisRecord(DiagnosisStatus.VIOLATES,
							new DiagnosisCell(DiagnosisStatus.VIOLATES, currentActivation),
							new DiagnosisCell(DiagnosisStatus.VIOLATES, latestTarget)));
		} else {
			diagnosis.addDiagnosisRecord(
					new DiagnosisRecord(DiagnosisStatus.VIOLATES,
							new DiagnosisCell(DiagnosisStatus.VIOLATES, currentActivation),
							new DiagnosisCell(DiagnosisStatus.VIOLATES, currentActivation)));
		}
	}
	
	public void restart(AbstractTaskClass taskClass) {
		coWal.resetRun();
		coWal.step(taskClass); // We try to repeat the latest action. Reason: take, e.g., <A,A> for ChainResponse(A,B) (the violation gets us back to the starting state but also to the next, expecting a B)
	}
	
	public void reset() {
		coWal.resetRun();
		currentCaseNumber = 0;
		currentEventNumber = 0;
		verdictGiven = false;
		latestTarget = null;
		pendingActivations.clear();
	}
	
	/**
	 * Diagnoses the constraint on the passed event log.
	 * @param logParser Parser of the event log to replay
	 */
	public ConstraintDiagnosis runOnLog(LogParser logParser) {
		logger.debug("Running on the log");
		
		long from = 0, to = 0;
		
		Iterator<LogTraceParser> logParIter = logParser.traceIterator();
		LogTraceParser loTraParser = null;
		AbstractTaskClass tasCla = null;
		
		ConstraintDiagnosis diagnosis = new ConstraintDiagnosis(cns);

		int
			traceCount = 0,
			barCount = 0,
			traceNum = 0;
		MessagePrinter.printOut("Parsing log: ");
		
		from = System.currentTimeMillis();
		
		// For every trace
		while (logParIter.hasNext()) {
			loTraParser = logParIter.next();
			
			runOnTrace(loTraParser, traceNum);
			
			barCount = displayAdvancementBars(logParser.length(), barCount);
			traceNum++;
			traceCount++;
			barCount++;
		}
		
		to = System.currentTimeMillis();

		MessagePrinter.printlnOut("\nDone.");
		logger.debug(traceCount + " traces evaluated on the log.");
		logger.debug("Evaluation done. Time in msec: " + (to - from));

		return diagnosis;
	}
	
	public ConstraintDiagnosis runOnTrace(LogTraceParser loTraParser, int traceNum) {
		int eventNum = 0;
		
		while (!loTraParser.isParsingOver()) {
			this.step(loTraParser.parseSubsequent().getEvent().getTaskClass(),
					traceNum,
					eventNum);
			eventNum++;
		}
		finalize();
		reset();
		
		return diagnosis;
	}
	
	public ConstraintDiagnosis getDiagnosis() {
		return diagnosis;
	}

	private static int displayAdvancementBars(int logParserLength, int barCount) {
		if (barCount > logParserLength / 80) {
			barCount = 0;
			MessagePrinter.printOut("|");
		}
		return barCount;
	}

	
	
	public static void main(String[] args) {
		BasicConfigurator.configure();
		Constraint
		suc = new Succession(
				new TaskChar('A'),new TaskChar('B')),
		sucBra = new Succession(
				new TaskCharSet(new TaskChar('A')),new TaskCharSet(new TaskChar('B'),new TaskChar('C'))),
		altSuc = new AlternateSuccession(
				new TaskChar('A'),new TaskChar('B')), // OK but remember: With <B,A,A,B>, the third and fourth events should be SAT, whereas the third is VIO here
		coExi = new CoExistence(
				new TaskChar('A'),new TaskChar('B')), // OK
		chRes = new ChainResponse(
				new TaskChar('A'),new TaskChar('B')),
		atMoTwo = new AtMost2(new TaskChar('A')),
		atLeThree = new AtLeast3(new TaskChar('A')),
		notRe = new NotResponse(
				new TaskChar('A'),new TaskChar('B')), // OK
		chSuc = new ChainSuccession(
				new TaskChar('A'),new TaskChar('B')),
		prec = new Precedence(
				new TaskChar('A'),new TaskChar('B')), // OK
		altPrec = new AlternatePrecedence(
				new TaskChar('A'),new TaskChar('B')),
		notChRe = new NotChainResponse(
				new TaskChar('A'),new TaskChar('B')), 
		notChPre = new NotChainPrecedence(
				new TaskChar('A'),new TaskChar('B')),
		cns = suc; 
		
		IncorrectConstraintDiagnoser evalu = new IncorrectConstraintDiagnoser(cns);
		CharTaskClass[][] steps = { 
				{
					new CharTaskClass('B'),
					new CharTaskClass('A'),
					new CharTaskClass('A'),
					new CharTaskClass('B'),
					new CharTaskClass('B'),
					new CharTaskClass('C'),
					new CharTaskClass('A'),
					new CharTaskClass('C'),
					new CharTaskClass('A'),
					new CharTaskClass('D')
				},
				{
					new CharTaskClass('B'),
					new CharTaskClass('A'),
					new CharTaskClass('B'),
					new CharTaskClass('B'),
					new CharTaskClass('B'),
					new CharTaskClass('A'),
					new CharTaskClass('A'),
					new CharTaskClass('B'),
				},				
		}; 
		for (int i = 0; i < steps.length; i++) {
			System.out.println("\nNEW TRACE!\n");
			for (int j = 0; j < steps[i].length; j++) {
				evalu.step(steps[i][j], i , j);
			}
			evalu.finalize();
			evalu.reset();
		}
		
		ProcessSpecificationDiagnosis proSpecDiag = new ProcessSpecificationDiagnosis();
		proSpecDiag.cnsDiagnoses.add(evalu.diagnosis);
		
		for (DiagnosisRecord record : evalu.diagnosis.getDiagnosisRecords()) {
			System.out.println(record);
		}
		System.out.print("Satisfactions: \n");
		for (DiagnosisRecord record : evalu.diagnosis.getSatisfactions()) {
			System.out.println(record);
		}
		System.out.print("Violations: \n");
		for (DiagnosisRecord record : evalu.diagnosis.getViolations()) {
			System.out.println(record);
		}
		for (LogCoordinates actiCoord : evalu.diagnosis.allActivators) {
			System.out.print("Activator: ");
			System.out.println(actiCoord);
		}
		for (LogCoordinates targeCoord : evalu.diagnosis.allTargets) {
			System.out.print("Target: ");
			System.out.println(targeCoord);
		}
		
		System.out.println("Diagnosis JSON: \n");
		System.out.println(new ProcSpecDiagnCmprsDto(proSpecDiag).toJsonString());
	}
}