/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package minerful;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import minerful.checking.diagnosis.ProcessSpecificationDiagnosis;
import minerful.checking.params.CheckingCmdParameters;
import minerful.checking.params.DiagnosisCmdParameters;
import minerful.checking.relevance.dao.SpecificationFitnessEvaluation;
import minerful.concept.ProcessSpecification;
import minerful.io.params.InputSpecificationParameters;
import minerful.io.params.OutputSpecificationParameters;
import minerful.params.InputLogCmdParameters;
import minerful.params.SystemCmdParameters;
import minerful.params.SystemCmdParameters.DebugLevel;
import minerful.postprocessing.params.PostProcessingCmdParameters;
import minerful.params.ViewCmdParameters;
import minerful.utils.MessagePrinter;

public class MinerFulDiagnosisStarter extends MinerFulMinerStarter {
	public static MessagePrinter logger = MessagePrinter.getInstance(MinerFulDiagnosisStarter.class);

	@Override
	public Options setupOptions() {
		Options cmdLineOptions = new Options();
		
		Options systemOptions = SystemCmdParameters.parseableOptions(),
				inputLogOptions = InputLogCmdParameters.parseableOptions(),
				diagnOptions = DiagnosisCmdParameters.parseableOptions(),
				inpuSpecOptions = InputSpecificationParameters.parseableOptions();
		
    	for (Object opt: systemOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: diagnOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: inputLogOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	for (Object opt: inpuSpecOptions.getOptions()) {
    		cmdLineOptions.addOption((Option)opt);
    	}
    	
    	return cmdLineOptions;
	}
	
    public static void main(String[] args) {
    	MinerFulDiagnosisStarter checkStarter = new MinerFulDiagnosisStarter();
    	Options cmdLineOptions = checkStarter.setupOptions();
    	
        SystemCmdParameters systemParams =
        		new SystemCmdParameters(
        				cmdLineOptions,
    					args);
		DiagnosisCmdParameters diaParams =
				new DiagnosisCmdParameters(
						cmdLineOptions,
						args);
		InputLogCmdParameters inputLogParams =
				new InputLogCmdParameters(
						cmdLineOptions,
						args);
		InputSpecificationParameters inpuSpecParams =
				new InputSpecificationParameters(
						cmdLineOptions,
						args);

		MessagePrinter.configureLogging(systemParams.debugLevel);

		if (systemParams.help) {
        	systemParams.printHelp(cmdLineOptions);
        	System.exit(0);
        }
        MinerFulDiagnosisLauncher miFuDiLa = new MinerFulDiagnosisLauncher(inpuSpecParams, inputLogParams, diaParams, systemParams);
        ProcessSpecificationDiagnosis diagnosis = miFuDiLa.run();
    }
 }