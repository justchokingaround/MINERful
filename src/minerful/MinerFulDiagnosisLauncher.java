package minerful;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.processmining.plugins.declareminer.visualizing.AssignmentModel;

import minerful.checking.ProcessSpecificationDiagnoser;
import minerful.checking.diagnosis.ProcessSpecificationDiagnosis;
import minerful.checking.params.DiagnosisCmdParameters;
import minerful.concept.ProcessSpecification;
import minerful.io.ProcessSpecificationLoader;
import minerful.io.encdec.dto.ProcSpecDiagnCmprsDto;
import minerful.io.params.InputSpecificationParameters;
import minerful.logparser.LogParser;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.SystemCmdParameters.DebugLevel;
import minerful.utils.MessagePrinter;

public class MinerFulDiagnosisLauncher {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulDiagnosisLauncher.class);
			
	private ProcessSpecification processSpecification;
	private LogParser eventLogParser;
	private DiagnosisCmdParameters diagnoParams;
	
	public MinerFulDiagnosisLauncher(DiagnosisCmdParameters diagnoParams) {
		this.diagnoParams = diagnoParams;
	}
	
	public MinerFulDiagnosisLauncher(AssignmentModel declareMapModel, LogParser inputLog, DiagnosisCmdParameters diagnoParams) {
		this(diagnoParams);
		this.processSpecification = new ProcessSpecificationLoader().loadProcessSpecification(declareMapModel);
		this.eventLogParser = inputLog;
	}

	public MinerFulDiagnosisLauncher(ProcessSpecification minerFulProcessSpecification, LogParser inputLog, DiagnosisCmdParameters diagnoParams) {
		this(diagnoParams);
		this.processSpecification = minerFulProcessSpecification;
		this.eventLogParser = inputLog;
	}

	public MinerFulDiagnosisLauncher(InputSpecificationParameters inputSpecParams,
			InputLogCmdParameters inputLogParams, DiagnosisCmdParameters diagnoParams, SystemCmdParameters systemParams) {
		this(diagnoParams);

		if (inputSpecParams.inputFile == null) {
			systemParams.printHelpForWrongUsage("Input process specification file missing!");
			System.exit(1);
		}
		// Load the process specification from the file
		this.processSpecification = 
				new ProcessSpecificationLoader().loadProcessSpecification(inputSpecParams.inputLanguage, inputSpecParams.inputFile);

		this.eventLogParser = MinerFulMinerLauncher.deriveLogParserFromLogFile(inputLogParams);

		// Notice that the merging of event log codification of TaskChars with the given specification’s one happens only late (at checking time)
		MessagePrinter.configureLogging(systemParams.debugLevel);
	}

	public ProcessSpecification getProcessSpecification() {
		return processSpecification;
	}

	public LogParser getEventLogParser() {
		return eventLogParser;
	}
	
	public ProcessSpecificationDiagnosis run() {
		ProcessSpecificationDiagnoser diagnoser = new ProcessSpecificationDiagnoser(processSpecification);
		
		ProcessSpecificationDiagnosis diagnosis = diagnoser.runOnLog(eventLogParser);
		
		reportOnDiagnosis(diagnosis);
		
		return diagnosis;
	}

	private void reportOnDiagnosis(ProcessSpecificationDiagnosis diagnosis) {
		// Print the result
		MessagePrinter.printlnOut(new ProcSpecDiagnCmprsDto(diagnosis).toJsonString());
		
		if (diagnoParams.fileToSaveResultsAsJSON != null) {
			logger.info("Saving results in a JSON format as " + diagnoParams.fileToSaveResultsAsJSON + "...");
			PrintWriter outWriter = null;
        	try {
    				outWriter = new PrintWriter(diagnoParams.fileToSaveResultsAsJSON);
    	        	outWriter.print(new ProcSpecDiagnCmprsDto(diagnosis).toJsonString());
    	        	outWriter.flush();
    	        	outWriter.close();
    			} catch (FileNotFoundException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
		}
	}
	
	public static void main(String[] args) {
		DiagnosisCmdParameters diagParams = new DiagnosisCmdParameters();
		diagParams.fileToSaveResultsAsJSON = new File("/home/cdc08x/Code/MINERful-dev/logs/Example-logs/diagnosis/ABC-diagnosis.json");
		InputLogCmdParameters logParams = new InputLogCmdParameters();
		logParams.inputLanguage = InputLogCmdParameters.InputEncoding.strings;
		logParams.inputLogFile = new File("/home/cdc08x/Code/MINERful-dev/logs/Example-logs/diagnosis/ABC-log.txt");
		logParams.inputLanguage = InputLogCmdParameters.InputEncoding.xes;
		logParams.inputLogFile = new File("/home/cdc08x/Code/MINERful-dev/logs/Example-logs/repairExampleSample2.xes");
		InputSpecificationParameters specParams = new InputSpecificationParameters();
		specParams.inputLanguage = InputSpecificationParameters.InputEncoding.JSON;
		specParams.inputFile = new File("/home/cdc08x/Code/MINERful-dev/logs/Example-logs/diagnosis/ABC-specification.json");
		SystemCmdParameters cmdParams = new SystemCmdParameters();
//		cmdParams.debugLevel = DebugLevel.all;
		
		MinerFulDiagnosisLauncher launch = new MinerFulDiagnosisLauncher(specParams, logParams, diagParams, cmdParams);
		
		launch.run();
	}

}