package minerful.checking.params;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import minerful.params.ParamsManager;


public class DiagnosisCmdParameters extends ParamsManager {
	public static final String SAVE_AS_JSON_PARAM_NAME = "oDF";
	
	/** File in which the checking output is printed in a CSV format. Keep it equal to <code>null</code> for avoiding such print-out. */
	public File fileToSaveResultsAsJSON;
	
	public DiagnosisCmdParameters() {
		super();
		this.fileToSaveResultsAsJSON = null;
	}
    
    public DiagnosisCmdParameters(Options options, String[] args) {
    	this();
        // parse the command line arguments
    	this.parseAndSetup(options, args);
	}

	public DiagnosisCmdParameters(String[] args) {
		this();
        // parse the command line arguments
    	this.parseAndSetup(new Options(), args);
	}

	@Override
	protected void setup(CommandLine line) {
		this.fileToSaveResultsAsJSON = openOutputFile(line, SAVE_AS_JSON_PARAM_NAME);
	}

	@Override
    public Options addParseableOptions(Options options) {
		Options myOptions = listParseableOptions();
		for (Object myOpt: myOptions.getOptions())
			options.addOption((Option)myOpt);
        return options;
	}
	
	@Override
    public Options listParseableOptions() {
    	return parseableOptions();
    }
	@SuppressWarnings("static-access")
	public static Options parseableOptions() {
		Options options = new Options();
        options.addOption(
				Option.builder(SAVE_AS_JSON_PARAM_NAME)
        		.hasArg().argName("path")
        		.longOpt("save-diagnosis-as-json")
        		.desc("print the diagnosis in a JSON format into the specified file path")
        		.type(String.class)
				.build()
//        		.create(SAVE_AS_JSON_PARAM_NAME)
        		);
        return options;
	}
}