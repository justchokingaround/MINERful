package minerful.checking;

import java.util.Iterator;

import minerful.checking.diagnosis.ConstraintDiagnosis;
import minerful.checking.diagnosis.ProcessSpecificationDiagnosis;
import minerful.concept.AbstractTaskClass;
import minerful.concept.ProcessSpecification;
import minerful.concept.constraint.Constraint;
import minerful.logparser.LogParser;
import minerful.logparser.LogTraceParser;
import minerful.utils.MessagePrinter;

public class ProcessSpecificationDiagnoser {
	private static MessagePrinter logger = MessagePrinter.getInstance(ProcessSpecificationDiagnoser.class);

	public final ProcessSpecification pSpec;

	private final IncorrectConstraintDiagnoser[] cnsDiagnosers;
	private ProcessSpecificationDiagnosis diagnosis;
	
	public ProcessSpecificationDiagnoser(ProcessSpecification pSpec) {
		int i = 0;
		this.pSpec = pSpec;
		this.cnsDiagnosers = new IncorrectConstraintDiagnoser[pSpec.size()];
		this.diagnosis = new ProcessSpecificationDiagnosis();
		for (Constraint cns: pSpec.getAllConstraints()) {
			this.cnsDiagnosers[i] = new IncorrectConstraintDiagnoser(cns);
			i++;
		}
	}
	
	/**
	 * Diagnoses the constraints on the passed event log.
	 * @param logParser Parser of the event log to replay
	 */
	public ProcessSpecificationDiagnosis runOnLog(LogParser logParser) {
		logger.debug("Running on the log");
		
		long from = 0, to = 0;
		
		Iterator<LogTraceParser> logParIter = logParser.traceIterator();
		LogTraceParser loTraParser = null;
		AbstractTaskClass tasCla = null;
		
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
		
		for (int i = 0; i < this.cnsDiagnosers.length; i++) {
			this.diagnosis.cnsDiagnoses.add(this.cnsDiagnosers[i].getDiagnosis());
		}
		
		return diagnosis;
	}
	
	public ProcessSpecificationDiagnosis runOnTrace(LogTraceParser loTraParser, int traceNum) {
		int eventNum = 0;
		ConstraintDiagnosis runtimeDiag;
		AbstractTaskClass currentRead;
		
		while (!loTraParser.isParsingOver()) {
			currentRead = loTraParser.parseSubsequent().getEvent().getTaskClass();
			// For every constraint diagnoser
			for (IncorrectConstraintDiagnoser diagnoser : this.cnsDiagnosers) {
				diagnoser.step(currentRead,
						traceNum,
						eventNum);
			}
			eventNum++;
		}
		for (IncorrectConstraintDiagnoser diagnoser : this.cnsDiagnosers) {
			diagnoser.finalize();
			diagnoser.reset();			
		}
		
		return diagnosis;
	}

	private static int displayAdvancementBars(int logParserLength, int barCount) {
		if (barCount > logParserLength / 80) {
			barCount = 0;
			MessagePrinter.printOut("|");
		}
		return barCount;
	}
}