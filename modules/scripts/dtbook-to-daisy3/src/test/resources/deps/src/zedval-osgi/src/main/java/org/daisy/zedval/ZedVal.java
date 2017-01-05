package org.daisy.zedval;
/*
 * ZedVal - ANSI/NISO Z39.86-2002/Z39.86-2005 DTB Validator
 * Copyright (C) 2003,2004,2005,2006,2007,2008 Daisy Consortium
 *
 * This library is free software; you can
 * redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation;
 * either version 2.1 of the License,
 * or (at your option) any later version.
 *
 * This library is distributed in the hope that it
 * will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the
 * GNU Lesser General Public License along with
 * this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 *
 * For information about the Daisy Consortium,
 * visit www.daisy.org or contact info@mail.daisy.org.
 * For development issues, contact
 * markus.gylling@tpb.se.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.xerces.impl.Version;
import org.daisy.zedval.engine.ApplicationErrMsg;
import org.daisy.zedval.engine.ZedConstants;
import org.daisy.zedval.engine.ZedContextException;
import org.daisy.zedval.engine.ZedFileInitializationException;
import org.daisy.zedval.engine.ZedReporter;
import org.daisy.zedval.engine.ZedReporterException;
import org.daisy.zedval.engine.ZedTest;
import org.daisy.zedval.engine.ZedTestProcessor;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Main application class.
 * <h2>Command line usage</h2>
 * <p>To run ZedVal using default (provided) test maps:
 * <code>zedval D:/myDtb/package.opf</code></p>
 * <p>For more information on options, run zedval -help</p>
 * 
 * <h2>Component usage</h2>
 * <p>Use of the instantiation convenience methods:</p>
 * <pre><code>
 *  public class ZedComponent implements ZedReporter {
 * 		//....
 * 		Zedval zv = new ZedVal();
 *		try {
 *			zv.setReporter(this);
 *			zv.validate(opfFile);
 *		} catch (ZedContextException e) {
 *			//...				
 *		} catch (ZedFileInitializationException e) {
 *			//...				
 *	    }	
 * 	   	   
 *    //implement the ZedReporter interface	
 *    public void addMessage(ZedMessage m) throws ZedReporterException {
 *      if (m instanceof FailureMessage) {
 *      	//...
 *      }else{
 *        System.out.println(m.getText());
 *  	}
 *    }
 *  
 *  }
 * </code></pre>
 *  
 * @author mgylling 
 */

public class ZedVal implements ErrorHandler {

    Options options = new Options();
    CommandLine cmd;
    BufferedWriter bwTestfile = null;
    private static final String VERSION = "2.1";
    private DefaultContext myContext;

	public static void main(String[] args) throws Exception {
        ZedVal zv = null;

        try {

            zv = new ZedVal(); 

            zv.myContext = new DefaultContext("ZedVal", ZedVal.VERSION);
            
            zv.setJaxpProperties();
                        
            System.err.println("ZedVal v" + ZedVal.VERSION + "\n");
            
            //mg20061001: adressing zero argument bug 1567005
            if(null==args||args.length==0) {
            	zv.printHelp();
            	System.exit(1);
            }
            
            // parse the args
            try {
                zv.parseArgs(args);                        
            } catch (ParseException e) {            
                zv.sendApplicationErrMsg(
                        "commandline argument parse exception: "
                                + e.getMessage(), true,
                        "org.daisy.zedval.ZedVal.main");
                zv.printHelp();
                throw e;
            }

            if (zv.hasCommandLineOption("help")) {
                zv.printHelp();
                return;
            }

            if (zv.hasCommandLineOption("debug")) {
                zv.getContext().setDebugState(true);
                //System.setProperty("org.daisy.debug", "true");
            }

            //create a shortcut to context
            DefaultContext c = zv.getContext();

            // park the args string in context for backwards compatibility
            c.setCommandLineArguments(args);
            
            //prep a local initexception object
            ZedFileInitializationException myInitExceptions = null;

            // load packagefile and as a consequence of that the entire input fileset  
            // we do not know if returned exceptions are fatal or not
            try {            	
                c.loadPackageFile(zv.getCommandLinePackageFile());                
            } catch (ZedFileInitializationException zfe) {
            	//zfe is exceptions that were collected during
            	//fileset instantiation.
                //store locally to try to add to reporter later 
            	//when its instantiated
                myInitExceptions = zfe;

            }

            //check the minimal reqs for even trying to continue
            if(c.getPackageFile() == null){
            	throw new ZedContextException("Package file does not exist");
            }
            if(!c.getPackageFile().exists()){
            	throw new ZedContextException("Package file "+ c.getPackageFile().getName() + " does not exist");
            }else if(!c.getPackageFile().isWellFormed()){
            	throw new ZedContextException("Package file "+ c.getPackageFile().getName() + " is not wellformed");
            }
            
                        
            // load procMap: a returned exception is terminating
            if (zv.hasCommandLineOption("procMap")) {
                c.loadProcessorMap(zv.getCommandLineOption("procMap"));
            } else {
            	//the user didnt specify a map, so use the default provided map.
            	//This requires specVersion from packagefile
            	//in order to know what map (2002,2005) to pick
            	String specVersion = c.getPackageFile().getSpecVersion();
            	if (specVersion!=null && (specVersion.equals(ZedConstants.Z3986_VERSION_2002)||specVersion.equals(ZedConstants.Z3986_VERSION_2005))) {
                    c.loadProcessorMap(
                    		c.getDefaultProcMap(specVersion));            		
            	}else{
            		throw new ZedContextException("Package file " + c.getPackageFile().getName() + " does not contain a recognizable specification version.");
            	}
            }

            // load testMap: a returned exception is terminating
            if (zv.hasCommandLineOption("testMap")) {
                c.loadTestMap(zv.getCommandLineOption("testMap"));
            } else {
                c.loadTestMap(c.getDefaultTestMap(c.getPackageFile()
                        .getSpecVersion()));
            }
            
            // load and init reporter: a returned exception is terminating
            if (zv.hasCommandLineOption("reporter")) {
                c.loadReporter(zv.getCommandLineOption("reporter"));
            } else {
                c.loadReporter("org.daisy.zedval.DefaultReporter");
            }
            c.getReporter().initialize();

            //add any init exceptions from above now that reporter is live
            if (myInitExceptions!=null) {
            	for (String error : myInitExceptions.getErrors()) {
            		zv.sendApplicationErrMsg(error, false,
            				"org.daisy.zedval.ZedVal.main");
				}
            }

            //zv.getContext().setDebugState(true);
            if (zv.getContext().getDebugState()) {
                zv.getContext().printMapsValidityInfo();
            }

            // set verbosity: a returned exception is nonterminating
            if (zv.hasCommandLineOption("verbosity")) {
                try {
                    zv.getContext().setVerbosity(
                            zv.getCommandLineOption("verbosity"));
                } catch (NumberFormatException nfe) {
                    zv.sendApplicationErrMsg(
                            "numberformatexception in -verbosity: "
                                    + nfe.getMessage(), false,
                            "org.daisy.zedval.ZedVal.main");
                    zv.getContext().setVerbosity(2);
                }
            } else {
                zv.getContext().setVerbosity(2);
            }

            // set timeTolerance: a returned exception is nonterminating
            if (zv.hasCommandLineOption("timeTolerance")) {
                try {
                    zv.getContext().setTimeTolerance(
                            zv.getCommandLineOption("timeTolerance"));
                } catch (NumberFormatException nfe) {
                    zv.sendApplicationErrMsg(
                            "numberformatexception in -timetolerance: "
                                    + nfe.getMessage(), false,
                            "org.daisy.zedval.ZedVal.main");
                    zv.getContext().setTimeTolerance(0);
                }
            } else {
                zv.getContext().setTimeTolerance(0);
            }

            // set stylesheet: does not return exception
            if (zv.hasCommandLineOption("stylesheet")) {
                zv.getContext().setStylesheetName(
                        zv.getCommandLineOption("stylesheet"));
            }

            // set manualReport: does not return exception
            if (zv.hasCommandLineOption("manualTests")) {
                zv.getContext().setManualReportPath(
                        zv.getCommandLineOption("manualTests"));
            }

            // set outputList: a returned exception is terminating
            if (zv.hasCommandLineOption("outputList")) {
                zv.getContext().setOutputList(
                        zv.getCommandLineOption("outputList"));
            }

        } catch (Exception e) {
            // only terminating exceptions should end up here
            zv.sendApplicationErrMsg(e.getMessage().trim(), true,
                    "org.daisy.zedval.ZedVal.main");
            zv.resetJaxpProperties();
            System.exit(1);
        }

        // setup phase was successful
        // proceed to validate

        try {
            if (zv.getContext().isValid()) {
                zv.openTestFile();
                zv.getContext().loadTests();
                zv.getContext().loadTestProcessors();
                zv.runTestProcessors();
                zv.closeTestFile();
            }

        } catch (Exception e) {
            if (e instanceof ZedContextException) {
                zv.sendApplicationErrMsg("Exception during validation "
                        + e.getMessage(), true, "org.daisy.zedval.ZedVal.main");
            } else {
                zv.sendApplicationErrMsg("Unknown exception during validation "
                        + e.getMessage(), true, "org.daisy.zedval.ZedVal.main");
            }
            zv.resetJaxpProperties();
            throw e;
        }

        try {
            zv.getContext().getReporter().close();
        } catch (ZedReporterException zre) {
            zv.sendApplicationErrMsg("Exception during reporter close"
                    + zre.getMessage(), true, "org.daisy.zedval.ZedVal.main");
        }
        zv.resetJaxpProperties();
        
//        if(zv.getContext().getDebugState()) {
//        	System.clearProperty("org.daisy.debug");
//        }
        
        System.exit(0);
    }

    /**
     * Constructor when using ZedVal as a component.
     */
    public ZedVal() {    	
    	myContext = new DefaultContext("ZedVal", ZedVal.VERSION);    	
    }

	public void error(SAXParseException e)throws SAXException {
		System.err.println(e.getMessage());
	}

	public void fatalError(SAXParseException e)throws SAXException {
		System.err.println(e.getMessage());
	}

	public void warning(SAXParseException e)throws SAXException {
		System.err.println(e.getMessage());
	}
    
	/**
     * Convenience method to register a reporter when using ZedVal as a component.
     */
    public void setReporter(ZedReporter zr) throws ZedContextException {
    	if (null==myContext) throw new ZedContextException("context is null");
    	myContext.setReporter(zr);
    }
    
    
    /**
     * <p>Convenience method when using ZedVal as a component.</p>
     * <p>This method is reentrant: several validate() calls can be made on the same ZedVal instance.</p>
     * <p>If the user has not specified custom test- or processor maps to use prior to calling this method, the default
     * (provided) maps will be used.</p>
     * <p>If this method does not throw an Exception, validation was completed successfully.</p>
     */
            
	public void validate(File opf) throws ZedContextException, ZedFileInitializationException {

    	    boolean mapsChanged = false;
    	    
    	    setJaxpProperties();
    	        					
			// load packagefile
    	    try{
    	    	myContext.loadPackageFile(opf.getAbsolutePath());
    	    }catch (ZedFileInitializationException zfe) {
            	//zfe is exceptions that were collected during
            	//fileset instantiation.
    	    	//print each zfe to reporter
    	    	for (String error : zfe.getErrors()) {
    	    		sendApplicationErrMsg(error, false,
    	    				"org.daisy.zedval.ZedVal.main");

    	    	}
    	    	
    	    	//check the minimal req for even trying to continue
                if(myContext.getPackageFile() == null 
                		|| !myContext.getPackageFile().exists()
                		||!myContext.getPackageFile().isWellFormed()){
                    throw zfe;
                }    	    	    	    	    	    	
    	    }
			
			// get a shortcut to opf specversion
			String opfSpecVersion = myContext.getPackageFile().getSpecVersion();			
			//get a shortcut to opf multimediatype
			String opfMultiMediaType = myContext.getPackageFile().getDtbMultimediaTypeAsDeclared();
			
			if (opfSpecVersion==null
					||(!opfSpecVersion.equals(ZedConstants.Z3986_VERSION_2002)
					&&!opfSpecVersion.equals(ZedConstants.Z3986_VERSION_2005))) {
				throw new ZedContextException("Opf file does not contain a recognizable specversion");
			}
			
			// set the testMap if it doesnt exist yet, or if current opf has other specversion than previous
			if(myContext.getTestMap()==null
					||!myContext.getTestMap().getSpecVersion().equalsIgnoreCase(opfSpecVersion)) {
				mapsChanged = true;
				myContext.loadTestMap(myContext.getDefaultTestMap(opfSpecVersion));
			}
										
			// set the procMap if it doesnt exist yet, or if current opf has other specversion than previous
			if(myContext.getProcessorMap()==null
					||!myContext.getProcessorMap().getSpecVersion().equalsIgnoreCase(opfSpecVersion)) {
				mapsChanged = true;
				myContext.loadProcessorMap(myContext.getDefaultProcMap(opfSpecVersion));
			}
									
			// run
			if (myContext.isValid()) {
				//load tests if not loaded, or if theres a diff between specversion or multimediatype
				if(myContext.getTests()==null
						||myContext.getTests().isEmpty()
						||mapsChanged
						||previousMultiMediaType.equalsIgnoreCase(opfMultiMediaType)){
					myContext.loadTests();
					myContext.loadTestProcessors();					
				}
				
				for (ZedTestProcessor ztp : myContext.getTestProcessors().values()) {
					ztp.performTests();
				}
			} else {
				// the isValid method will throw an exception
			}

			resetJaxpProperties();
			
    	    	    	
    }

    /**
     * @return the ZedContext object used in this run
     */
    public DefaultContext getContext() {
        return myContext;
    }

    /**
     * Constructs and sends an ApplicationErrMsg to the Reporter, if available.
     * Otherwise, sends to error output
     * 
     * @param msg
     *            The message text
     * @param fatal
     *            Is the error fatal?
     * @param context
     *            String expressing application context
     */
    public void sendApplicationErrMsg(String msg, boolean fatal, String context) {
        if (myContext.getReporter() != null) {
            ApplicationErrMsg m = new ApplicationErrMsg(msg, "en", fatal,
                    context);
            try {
                myContext.getReporter().addMessage(m);
            } catch (ZedReporterException zre) {
                // If reporter fails, just send the message to the console
                System.err.println("ZedVal application ERROR:  " + msg);
            }
            // If the error was fatal, close the reporter
            if (fatal == true) {
                try {
                    myContext.getReporter().close();
                } catch (ZedReporterException zre) {
                    System.err
                            .println("ZedVal application ERROR:  Couldn't close reporter: "
                                    + zre.getMessage());
                }
            }
        } else { // No reporter available; send to console
            System.err.println("ZedVal application ERROR:  " + msg);
        }
        if (fatal == true) {
            System.err.println("ZedVal had fatal errors -- exiting");
        }
    }

    private void openTestFile() {
        if (myContext.getOutputListFlag()) {
            try {
                File fTestListFile = new File(myContext.getTestListName());
                FileWriter frTestListFile = new FileWriter(fTestListFile);
                bwTestfile = new BufferedWriter(frTestListFile);
                bwTestfile.write("<outputList>");
                bwTestfile.newLine();
            } catch (IOException e) {
                sendApplicationErrMsg(
                        "ERROR:  Can't create file otputlistfile "
                                + myContext.getTestListName() + ": "
                                + e.getMessage(), false,
                        "org.daisy.zedval.ZedVal.main");
            }
        }

    }

    private void closeTestFile() {
        try {
            if (bwTestfile != null) {
                bwTestfile.write("</outputList>");
                bwTestfile.close();
            }
        } catch (IOException e) {
            sendApplicationErrMsg("Can't close test list file "
                    + myContext.getTestListName() + ": " + e.getMessage(),
                    false, "org.daisy.zedval.ZedVal.main");
        }
    }

	private void runTestProcessors() {
        System.err.println("Executing test processors");
        
        for (ZedTestProcessor ztp : myContext.getTestProcessors().values()) {
            System.err.println("\tExecuting: " + ztp.getLabel());
            addElementToOutputFile(ztp, ztp.performTests(), bwTestfile);
        }
    }

    /**
     * Creates an element and adds it to the Outputlistfile
     * 
     * @param aZTD
     *            The test processor that has been executed
     * @param abolPerformedTest
     *            Was the test processor executed successfully?
     * @param aBuffWriter
     *            BufferedWriter to send output to
     */
	private void addElementToOutputFile(ZedTestProcessor aZTD,
            boolean aBolPerformedTest, BufferedWriter aBuffWriter) {
        if (aBolPerformedTest && aBuffWriter != null) {
            try {
                aBuffWriter.write("<processor>");
                aBuffWriter.newLine();
                aBuffWriter.write("  <label>");
                aBuffWriter.write(aZTD.getLabel());
                aBuffWriter.write("</label>");
                aBuffWriter.newLine();
                aBuffWriter.write("  <tests_performed>");
                aBuffWriter.newLine();

                for (ZedTest z : aZTD.getTestsImplemented().values()) {
                    aBuffWriter.write("    <test id=\"" + z.getId() + "\"/>");
                    aBuffWriter.newLine();
                }

                aBuffWriter.write("  </tests_performed>");
                aBuffWriter.newLine();

                aBuffWriter.write("</processor>");
                aBuffWriter.newLine();
            } catch (IOException e) {
                sendApplicationErrMsg("Error when trying to write to file:"
                        + myContext.getTestListName(), false,
                        "org.daisy.zedval.ZedVal.main");
            }
        }
    }

    private void parseArgs(String[] args) throws ParseException {

        OptionBuilder.hasArg(false);
        OptionBuilder.isRequired(false);
        OptionBuilder.withDescription("print help message");
        Option help = OptionBuilder.create("help");
        options.addOption(help);

        OptionBuilder.hasArg(false);
        OptionBuilder.isRequired(false);
        OptionBuilder.withDescription("print debug messages");
        Option debug = OptionBuilder.create("debug");
        options.addOption(debug);

        OptionBuilder.hasArg();
        OptionBuilder.isRequired(false);
        OptionBuilder.withArgName("path");
        OptionBuilder.withDescription("Path to processor map");
        Option procMap = OptionBuilder.create("procMap");
        options.addOption(procMap);

        OptionBuilder.hasArg();
        OptionBuilder.isRequired(false);
        OptionBuilder.withArgName("path");
        OptionBuilder.withDescription("Path to test map");
        Option testMap = OptionBuilder.create("testMap");
        options.addOption(testMap);

        OptionBuilder.hasArg();
        OptionBuilder.isRequired(false);
        OptionBuilder.withArgName("value");
        OptionBuilder.withDescription("set verbosity level 1-3 (default: 2)");
        Option verbosity = OptionBuilder.create("verbosity");
        options.addOption(verbosity);

        OptionBuilder.hasArg();
        OptionBuilder.isRequired(false);
        OptionBuilder.withArgName("classname");
        OptionBuilder
                .withDescription("Use this class as the output reporter (default: DefaultReporter)");
        Option reporter = OptionBuilder.create("reporter");
        options.addOption(reporter);

        OptionBuilder.hasArg();
        OptionBuilder.isRequired(false);
        OptionBuilder.withArgName("filename");
        OptionBuilder
                .withDescription("Generate a list of tests actually run to this file");
        Option outputList = OptionBuilder.create("outputList");
        options.addOption(outputList);

        OptionBuilder.hasArg();
        OptionBuilder.isRequired(false);
        OptionBuilder.withArgName("filename");
        OptionBuilder
                .withDescription("Generate lists of manual tests to be performed, saving reports in path given here");
        Option manualTests = OptionBuilder.create("manualTests");
        options.addOption(manualTests);

        OptionBuilder.hasArg();
        OptionBuilder.isRequired(false);
        OptionBuilder.withArgName("filename");
        OptionBuilder
                .withDescription("Include the stylesheet mentioned here in the output report");
        Option stylesheet = OptionBuilder.create("stylesheet");
        options.addOption(stylesheet);

        OptionBuilder.hasArg();
        OptionBuilder.isRequired(false);
        OptionBuilder.withArgName("milliseconds");
        OptionBuilder
                .withDescription("All time comparisons will be made plus/minus this value");
        Option timeTolerance = OptionBuilder.create("timeTolerance");
        options.addOption(timeTolerance);

        GnuParser parser = new GnuParser();

        cmd = parser.parse(options, args);

    }

    private String getCommandLineOption(String optionName) {
        if (cmd.hasOption(optionName)) {
            return cmd.getOptionValue(optionName);
        }
        return null;
    }

    private boolean hasCommandLineOption(String optionName) {
        if (cmd.hasOption(optionName)) {
            return true;
        }
        return false;
    }

    private String getCommandLinePackageFile() {
        //assumes that OptionParser has already been run
        //what remains are options not captured by Options     
        if (cmd.getArgs().length == 1) {
            return cmd.getArgs()[0];
        }

        //if more than one remaining, loop
        for (int i = 0; i < cmd.getArgs().length; i++) {
            //TODO if there are spaces in opf filename, we get >1 arg
            if (cmd.getArgs()[i].indexOf("opf")>=0) {
                return cmd.getArgs()[i];
            }
        }
        return null;
    }

    /**
     * Resets the JAXP properties to what they were before ZedVal was started
     */
    private void resetJaxpProperties() {
        if (_propDBF != null) System.setProperty(_DocumentBuilderFactory, _propDBF);
        if (_propSAXPF != null) System.setProperty(_SAXParserFactory, _propSAXPF);
        if (_propXMLPPConf != null) System.setProperty(_XMLParserConfiguration,_propXMLPPConf);        
        if (_propXTF != null)System.setProperty(_TransformerFactory,_propXTF);
        if (_propSSRF != null)System.setProperty(_SchematronSchemaReaderFactory,_propSSRF); //mg20071218
    }

    /**
     * Sets the system JAXP properties to specific implementations required by ZedVal
     * And performs a test that we really have access to what we want
     */
    private void setJaxpProperties() {
    	if(!jaxpVisited){ //not to redo the same thing on repeated calls
    		//store system properties
			if(_propDBF==null)_propDBF = System.getProperty(_DocumentBuilderFactory);
			if(_propSAXPF==null)_propSAXPF = System.getProperty(_SAXParserFactory);
			if(_propXMLPPConf==null)_propXMLPPConf = System.getProperty(_XMLParserConfiguration);
			if(_propXTF==null)_propXTF = System.getProperty(_TransformerFactory);
			if(_propXTF==null)_propSSRF = System.getProperty(_SchematronSchemaReaderFactory); //mg20071218
			jaxpVisited = true;			
			//test xerces version
			try {
				String versionString = Version.getVersion();
				versionString = versionString.substring(9, 12);
				double xercesVersion = Double.valueOf(versionString).doubleValue();
				if (xercesVersion < 2.8) {
					this.sendApplicationErrMsg("WARNING: Xerces version is less than 2.8", false, "org.daisy.zedval.ZedVal.setJaxpProperties");
				}
			} catch (Exception ex) {
				xercesException = true;
				this.sendApplicationErrMsg("WARNING: Could not determine Xerces version", false, "org.daisy.zedval.ZedVal.setJaxpProperties");
			} catch (Error err) {
				xercesException = true;
				this.sendApplicationErrMsg("WARNING: Xerces does not seem to be installed", false, "org.daisy.zedval.ZedVal.setJaxpProperties");
			}
			
    	}//if(!_jaxpPropertiesSet)
	
    	// we use Apache Xerces 2.8 or later, ignore any xerces bundled with JREs etc
    	//do this for each visit
    	if(!xercesException){
			System.setProperty(_DocumentBuilderFactory, "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
			System.setProperty(_SAXParserFactory, "org.apache.xerces.jaxp.SAXParserFactoryImpl");
			System.setProperty(_XMLParserConfiguration, "org.apache.xerces.parsers.XML11Configuration");
    	}
		// Note: XSLT tests in 2002 books needs Xalan
		// The XSLT used for schematron in 2005 books does not require a specific impl
		System.setProperty(_TransformerFactory, "net.sf.saxon.TransformerFactoryImpl");
		System.setProperty(_SchematronSchemaReaderFactory, "org.daisy.util.xml.validation.jaxp.SaxonSchematronSchemaReaderFactory"); //mg20071218
    }

    private void printHelp() {
        HelpFormatter hf = new HelpFormatter();
        hf.printHelp("zedval [options] packageFile", options);
    }
    
    private final String _DocumentBuilderFactory = "javax.xml.parsers.DocumentBuilderFactory";
    private final String _SAXParserFactory = "javax.xml.parsers.SAXParserFactory";
    private final String _XMLParserConfiguration = "org.apache.xerces.xni.parser.XMLParserConfiguration";
    private final String _TransformerFactory = "javax.xml.transform.TransformerFactory";
    private final String _SchematronSchemaReaderFactory = "org.daisy.util.xml.validation.jaxp.SchematronValidator.SchemaReaderFactory"; //mg20071218
    private static String _propDBF = null;
    private static String _propSAXPF = null;
    private static String _propXMLPPConf = null;
    private static String _propXTF = null;
    private static String _propSSRF = null; //mg20071218
    private boolean jaxpVisited = false; //marks first visit to this method
    private boolean xercesException = false; //marks whether we could get an parse xerces version info
    
    private String previousMultiMediaType = "";

	public static String getVersion() {
		return VERSION;
	}


}
