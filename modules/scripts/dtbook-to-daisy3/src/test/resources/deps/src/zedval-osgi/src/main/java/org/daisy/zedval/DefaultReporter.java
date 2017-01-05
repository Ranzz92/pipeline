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

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.daisy.util.xml.SmilClock;
import org.daisy.zedval.engine.ApplicationErrMsg;
import org.daisy.zedval.engine.CustomTestProcessor;
import org.daisy.zedval.engine.DtdTestProcessor;
import org.daisy.zedval.engine.FailureMessage;
import org.daisy.zedval.engine.ManualTestProcessor;
import org.daisy.zedval.engine.RngSchematronTestProcessor;
import org.daisy.zedval.engine.TestProcessorErrMsg;
import org.daisy.zedval.engine.XslTestProcessor;
import org.daisy.zedval.engine.ZedContext;
import org.daisy.zedval.engine.ZedMessage;
import org.daisy.zedval.engine.ZedReporter;
import org.daisy.zedval.engine.ZedReporterException;
import org.daisy.zedval.engine.ZedTest;
import org.daisy.zedval.engine.ZedTestProcessor;
import org.xml.sax.XMLReader;

/**
 * The default <code>ZedReporter</code> for the ZedVal application
 * 
 * @author James Pritchett
 * @author Daniel Carlsson
 * @author Tor Ghai
 * @author Piotr Kiernicki
 */

public class DefaultReporter implements ZedReporter {

    public DefaultReporter() {
        this.initialized = false;
        this.closed = false;
    }

    /**
     * Associates a ZedContext with this report
     * 
     * @param c
     *            The ZedContext associated with this report
     */

    public void setContext(ZedContext c) {
        this.myContext = c;
    }

    /**
     * Associates a DefaultContext with this report (used only by the reporter,
     * not TestProcessors)
     * 
     * @param c
     *            the DefaultContext object associated with this report
     */
    public void setContext(DefaultContext c) {
        this.myContext = c;
    }

    /**
     * Initializes the report
     */
    public void initialize() throws ZedReporterException {

        String stylesheetName;

        if (this.initialized == false) {

            // Prevents run-time error if the opf is missing or invalid
            // if (!this.myContext.getPackageFile().isValid()){
            // System.err.println("Package file not found or invalid");
            // return;}

            if (this.myContext == null) {
                throw new ZedReporterException(
                        "Error in DefaultReporter.initialize():  no context");
            }

            // jp: Revised this to use stdout instead of a file. This can be
            // redirected on the command line
            // to give identical results.
            // PK: added encoding to the OutputStreamWriter
            String _reportEncoding = "utf-8";
            try {
                OutputStream _out = new FileOutputStream(FileDescriptor.out);
                this.reportWriter = new PrintWriter(new OutputStreamWriter(
                        _out, _reportEncoding));
            } catch (IOException e) {
                throw new ZedReporterException(
                        "Could not open file for writing");
            }

            reportWriter.println("<?xml version=\"1.0\" encoding=\""
                    + _reportEncoding + "\" ?>");
            // If the stylesheet option is used, put that here
            stylesheetName = ((DefaultContext) this.myContext)
                    .getStylesheetName();
            if (stylesheetName != null) {
                if (stylesheetName.matches(".*\\.[cC][sS][sS]")) {
                    reportWriter.println("<?xml-stylesheet href=\""
                            + stylesheetName + "\" type=\"text/css\"?>");
                } else if (stylesheetName.matches(".*\\.[xX][sS][lL]")) {
                    reportWriter.println("<?xml-stylesheet href=\""
                            + stylesheetName + "\" type=\"text/xsl\"?>");
                }
            }

            reportWriter.println("<zedValReport engineVersion=\""
                    + ZedContext.ENGINE_VERSION + "\">");
            reportWriter.println("  <head>");
            if (this.myContext.getPackageFile() != null) {
                try {
                    reportWriter.println("    <book uid=\""
                            + this.myContext.getPackageFile().getIdentifier()
                            + "\" href=\""
                            + this.myContext.getPackageFile()
                                    .getCanonicalPath() + "\">");
                } catch (IOException e) {
                    // TODO: Something more useful here?
                    reportWriter.println("    <book uid=\""
                            + this.myContext.getPackageFile().getIdentifier()
                            + "\" href=\"IOException\">");
                }

                // manifestcount
                if (this.myContext.getPackageFile().getManifest() != null) {
                    reportWriter.println("      <fileCount>"
                            + this.myContext.getPackageFile().getManifest()
                                    .size() + "</fileCount>");
                }

                reportWriter.println("      <title>"
                        + this.myContext.getPackageFile().getTitle()
                        + "</title>");

                if (this.myContext.getPackageFile().getCreators() != null) {
                    for (int i = 0; i < this.myContext.getPackageFile()
                            .getCreators().size(); i++) {
                        reportWriter.println("      <creator>"
                                + this.myContext.getPackageFile().getCreators()
                                        .get(i) + "</creator>");
                    }
                }
                reportWriter.println("    </book>");
            } else {
                reportWriter.println("    <book uid=\"null\" href=\"null\" />");
            }

            reportWriter.println("    <program appName=\""
                    + this.myContext.getAppName() + "\" appVersion=\""
                    + this.myContext.getAppVersion() + "\" >");
            if (this.myContext.getTestMap() != null) {
                try {
                    reportWriter.println("      <testMap href=\""
                            + this.myContext.getTestMap().getName()
                            + "\" />");
                } catch (Exception e) {
                    // TODO: Something more useful here?
                    reportWriter
                            .println("      <testMap href=\"Exception\" />");
                }
            }

            if (this.myContext.getProcessorMap() != null) {
                try {
                    reportWriter.println("      <procMap href=\""
                            + this.myContext.getProcessorMap().getName() + "\" />");
                } catch (Exception e) {
                    reportWriter
                            .println("      <procMap href=\"Exception\" />");
                }
            }

            
            
            reportWriter
                    .println("      <cmdLineArg name=\"-verbosity\" value=\""
                            + ((DefaultContext) (this.myContext))
                                    .getVerbosity() + "\" />");
            if (((DefaultContext) (this.myContext)).getDebugState()) {
                reportWriter
                        .println("      <cmdLineArg name=\"-debug\" value=\""
                                + "true" + "\" />");
            }
            String[] strArrArgsTemp = ((DefaultContext) (this.myContext))
                    .getCommandLineArguments();
            // strArrArgsTemp.length -1 because there has to be one more
            // argument for every
            // option in the loop
            for (int x = 0; (strArrArgsTemp.length - 1) > x; x += 2) {
                reportWriter.println("      <cmdLineArg name=\""
                        + strArrArgsTemp[x] + "\" value=\""
                        + strArrArgsTemp[x + 1] + "\" />");
            }
                        
            reportWriter.println("      <startTime>"
                    + toDateTimeString(((DefaultContext) (this.myContext))
                            .getStartTime()) + "</startTime>");
            reportWriter.println("    </program>");
            try{
            	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            	DocumentBuilder db = dbf.newDocumentBuilder();
            	SAXParserFactory spf = SAXParserFactory.newInstance();
            	SAXParser sp = spf.newSAXParser();
            	XMLReader xr = sp.getXMLReader(); 
            	TransformerFactory tf = TransformerFactory.newInstance();
            	Transformer t = tf.newTransformer();
            	            
            reportWriter.println("    <runTime>");
            reportWriter.println("       <jre version=\""+ System.getProperty("java.version") +"\" vendor=\"" + System.getProperty("java.vendor") + "\" />");
            reportWriter.println("       <os name=\""+ System.getProperty("os.name") +"\" version=\"" + System.getProperty("os.version") + "\" />");
            
            reportWriter.println("       <documentbuilderfactory\n" 
            		+ "          class=\""+ dbf.getClass().getName()+"\""
            		+"/>");
            reportWriter.println("       <documentbuilder\n"          		 
            		+ "          class=\""+ db.getClass().getName()+"\""
            		+"/>");
            reportWriter.println("       <saxparserfactory\n" 
            		+ "          class=\""+spf.getClass().getName()+"\""
            		+"/>");
            reportWriter.println("       <saxparser\n"             		 
            		+ "          class=\""+ sp.getClass().getName()+"\""
            		+"/>");            
            reportWriter.println("       <xmlreader\n"             		 
            		+ "          class=\""+ xr.getClass().getName()+"\""
            		+"/>");    
//XMLParserConfiguration; is it possible access it via another object?
// direct access is not possible for both java 1.4 and 1.5
//            reportWriter.println("       <xmlparserconfiguration\n "
//            		+ "          class=\""+XMLParserConfiguration.class.getName()+"\""
//            		+"/>");
            reportWriter.println("       <transformerfactory\n"
            		+ "          class=\""+tf.getClass().getName()+"\""
            		+"/>");                                             
            reportWriter.println("       <transformer\n"        		 
            		+ "          class=\""+ t.getClass().getName()+"\""
            		+"/>");                
            reportWriter.println("    </runTime>");        
            }catch(Exception e) {
            	System.out.println(e.getMessage());
            }
            reportWriter.println("  </head>");
            reportWriter.println("  <body>");
            this.initialized = true;
        } else {
            throw new ZedReporterException(
                    "Error in DefaultReporter.initialize():  reporter already initialized");
        }
    } // public void initialize

    /**
     * Adds a message for the report output
     */

	public void addMessage(ZedMessage m) throws ZedReporterException {
        // These are convenience variables (saves us a lot of casting)
        FailureMessage fm;
        ApplicationErrMsg aem;
        TestProcessorErrMsg tpem;
        ZedTest zt;
        ZedTestProcessor ztp;
        String s;

        /*
         * if verbose = 0 sett messagecount++ and exit verbose = 1, if message
         * comes from testtype = Recomendation then set messcount++ and exit
         * verbose = 2, nothing needs to be done in following if-clause.
         */
        if (((DefaultContext) myContext).getVerbosity() == 0) {
            if (m instanceof FailureMessage)
                failureCount++;
            else if (m instanceof ApplicationErrMsg)
                applicationErrCount++;
            else if (m instanceof TestProcessorErrMsg)
                processorErrCount++;
            else
                // This is the assumption if none of the others are satisfied.
                // maybe debug info?
                applicationErrCount++;
            return;
        } else if (((DefaultContext) myContext).getVerbosity() == 1) {
            if (m instanceof FailureMessage) { // If Failure is a Recomendation
                // and verbose is 1 (error only)
                // don't write annything.
                fm = (FailureMessage) m;
                if (fm.getTest() != null) {
                    if (fm.getTest().getType() == ZedTest.RECOMMENDATION) {
                        failureCount++;
                        return;
                    }
                }
            }
        }// if verbosity = 2(error and warnings), then all messages are
        // written

        if (reportWriter == null) {
            throw new ZedReporterException(
                    "Error in DefaultReporter.addMessage():  No report file specified");
        }
        if (m == null) {
            throw new ZedReporterException(
                    "Error in DefaultReporter.addMessage():  ZedMessage is null");
        }

        if (this.initialized && (this.closed == false)) {

            // Validation error
            if (m instanceof FailureMessage) {
                reportWriter.println("    <message type=\"failure\">");
                failureCount++;
                fm = (FailureMessage) m; // This saves us a bunch o' casting
                // later on
                if (fm.getFile() != null) {
                    try {
                        s = "      <file name=\""
                                + fm.getFile().getCanonicalPath() + "\"";
                    } catch (IOException e) {
                        // TODO: Something more useful here?
                        s = "      <file name=\"IOException\"";
                    }

                    if (fm.getLine() >= 0)
                        s = s + " line=\"" + fm.getLine() + "\"";
                    if (fm.getColumn() >= 0)
                        s = s + " column=\"" + fm.getColumn() + "\"";
                    s = s + " />";
                    reportWriter.println(s);
                }
                if (fm.getTest() != null) {
                    zt = fm.getTest();
                    s = "      <test id=\"" + zt.getId() + "\" ";
                    switch (zt.getType()) {
                    case ZedTest.REQUIREMENT:
                        s = s + "type=\"requirement\">";
                        break;
                    case ZedTest.RECOMMENDATION:
                        s = s + "type=\"recommendation\">";
                        break;
                    default:
                        s = s + "type=\"unknown\">";
                        break;
                    }
                    reportWriter.println(s);
                    reportWriter.println("        <testDesc>"
                            + trim(zt.getDescription()) + "</testDesc>");
                    reportWriter.println("        <specRef href=\""
                            + zt.getSpecRef() + "\" />");
                    reportWriter.println("        <onFalseMsg>");

                    for (ZedMessage falseMsg : zt.getOnFalseMsgs().values()) {
                        switch (falseMsg.getType()) {
                        case ZedMessage.LONG:
                            reportWriter
                                    .println("          <msg class=\"long\">"
                                            + trim(falseMsg.getText()) + "</msg>");
                            break;
                        case ZedMessage.SHORT:
                            reportWriter
                                    .println("          <msg class=\"short\">"
                                            + falseMsg.getText() + "</msg>");
                            break;
                        case ZedMessage.DETAIL:
                            reportWriter
                                    .println("          <msg class=\"detail\">"
                                            + falseMsg.getText() + "</msg>");
                            break;
                        default:
                            reportWriter
                                    .println("          <msg class=\"unknown\">"
                                            + falseMsg.getText() + "</msg>");
                            break;
                        }
                    }
                    reportWriter.println("         </onFalseMsg>");
                    reportWriter.println("       </test>");
                }
                reportWriter.println("      <detail>" + fm.getText()
                        + "</detail>");

                if (fm.getProcessor() != null) {
                    ztp = fm.getProcessor();
                    if (ztp instanceof CustomTestProcessor) {
                        reportWriter.println("      <testProcessor id=\""
                                + ztp.getId() + "\" type=\"CUSTOM\" uses=\""
                                + ((CustomTestProcessor) ztp).getTesterName()
                                + "\">");
                    } else if (ztp instanceof DtdTestProcessor) {
                        reportWriter
                                .println("      <testProcessor id=\""
                                        + ztp.getId()
                                        + "\" type=\"DTD\" uses=\""
                                        + ((DtdTestProcessor) ztp).getDtdName()
                                        + "\">");
                    } else if (ztp instanceof RngSchematronTestProcessor) {
                        reportWriter.println("      <testProcessor id=\""
                                + ztp.getId()
                                + "\" type=\"RNG|SCH\" uses=\""
                                + ((RngSchematronTestProcessor) ztp)
                                        .getSchemaUrl() + "\">");
                    } else if (ztp instanceof XslTestProcessor) {
                        reportWriter.println("      <testProcessor id=\""
                                + ztp.getId()
                                + "\" type=\"XSL\" uses=\""
                                + ((XslTestProcessor) ztp).getSheetUrl()
                                + "\">");
                    } else if (ztp instanceof ManualTestProcessor) {
                        reportWriter.println("      <testProcessor id=\""
                                + ztp.getId()
                                + "\" type=\"MANUAL\" uses=\""
                                + ((ManualTestProcessor) ztp).getReportFile()
                                        .getName() + "\">");
                    } else {
                        reportWriter.println("      <testProcessor id=\""
                                + ztp.getId() + "\" type=\"unknown\">");
                    }
                    reportWriter.println("        <label>" + ztp.getLabel()
                            + "</label>");
                    reportWriter.println("      </testProcessor>");
                }
            }
            // Application error
            else if (m instanceof ApplicationErrMsg) {
                reportWriter
                        .println("     <message type=\"applicationError\">");
                aem = (ApplicationErrMsg) m;
                applicationErrCount++;
                reportWriter.println("       <detail>" + aem.getText()
                        + "</detail>");
                reportWriter.println("       <context>" + aem.getContext()
                        + "</context>");
            }
            // Testprocessor error
            else if (m instanceof TestProcessorErrMsg) {
                reportWriter
                        .println("    <message type=\"testProcessorError\">");
                tpem = (TestProcessorErrMsg) m;
                processorErrCount++;
                if (tpem.getFile() != null) {
                    try {
                        reportWriter.println("      <file name=\""
                                + tpem.getFile().getCanonicalPath() + "\" />");
                    } catch (IOException e) {
                        // TODO: Something more useful here?
                        reportWriter
                                .println("      <file name=\"IOException\" />");
                    }
                }

                reportWriter.println("      <detail>" + tpem.getText()
                        + "</detail>");
                if (tpem.getProcessor() != null) {
                    ztp = tpem.getProcessor();
                    if (ztp instanceof CustomTestProcessor) {
                        reportWriter.println("      <testProcessor id=\""
                                + ztp.getId() + "\" type=\"CUSTOM\" uses=\""
                                + ((CustomTestProcessor) ztp).getTesterName()
                                + "\">");
                    } else if (ztp instanceof DtdTestProcessor) {
                        reportWriter
                                .println("      <testProcessor id=\""
                                        + ztp.getId()
                                        + "\" type=\"DTD\" uses=\""
                                        + ((DtdTestProcessor) ztp).getDtdName()
                                        + "\">");
                    } else if (ztp instanceof RngSchematronTestProcessor) {
                        reportWriter.println("      <testProcessor id=\""
                                + ztp.getId()
                                + "\" type=\"RNG\" uses=\""
                                + ((RngSchematronTestProcessor) ztp)
                                        .getSchemaUrl() + "\">");
                    } else if (ztp instanceof XslTestProcessor) {
                        reportWriter.println("      <testProcessor id=\""
                                + ztp.getId()
                                + "\" type=\"XSL\" uses=\""
                                + ((XslTestProcessor) ztp).getSheetUrl()
                                + "\">");
                    } else if (ztp instanceof ManualTestProcessor) {
                        reportWriter.println("      <testProcessor id=\""
                                + ztp.getId()
                                + "\" type=\"MANUAL\" uses=\""
                                + ((ManualTestProcessor) ztp).getReportFile()
                                        .getName() + "\">");
                    } else {
                        reportWriter.println("      <testProcessor id=\""
                                + "\" type=\"unknown\">");
                    }
                    reportWriter.println("        <label>" + ztp.getLabel()
                            + "</label>");
                    reportWriter.println("      </testProcessor>");
                }
            }
            // Error message
            else if (m.getClass().getName().equals(
                    "org.daisy.zedval.ErrorMessage")) {
                // KNOWN BUG: This is an assumption. Actually, ErrorMessages
                // shouldn't appear at all (only the subclasses)
                // FIX: Remove this?
                reportWriter.println("    <message type=\"applicationError\">");
                applicationErrCount++;
                reportWriter.println("      <detail>" + m.getText()
                        + "</detail>");
            }
            // ZedMessage
            else {
                // KNOWN BUG: This is an assumption. Actually, ZedMessages
                // shouldn't appear at all (only the subclasses)
                // FIX: Remove this?
                reportWriter.println("    <message type=\"applicationError\">");
                applicationErrCount++;
                reportWriter.println("      <detail>" + m.getText()
                        + "</detail>");
            }

            reportWriter.println("    </message>");
        } else {
            throw new ZedReporterException(
                    "Error in DefaultReporter.addMessage():  reporter not initialized or already closed");
        }
    }

    /**
     * Trim leading and trailing whitespace, truncate all excessive (>1) whitespace into single space 
     */
    private String trim(String text) {
    	    
		try{
			text = text.trim();
			StringBuilder sb = new StringBuilder();
			char prevChar = 'a' ;
			for (int i = 0; i < text.length(); i++) {
				char ch = text.charAt(i);
				
				if(Character.isSpaceChar(ch)) {
					if(!Character.isSpaceChar(prevChar)) {
						sb.append(" ");
					}					
				}else{
					sb.append(ch);
				}
				prevChar = ch;
			}
			return sb.toString();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return text;
	}

	/**
     * Closes the report
     */

    public void close() throws ZedReporterException {
        if (reportWriter == null) {
            throw new ZedReporterException(
                    "Error in DefaultReporter.close():  No report file specified");
        }
        if (this.initialized && (this.closed == false)) {

            if (((DefaultContext) myContext).getVerbosity() == 0) {

                if (failureCount + processorErrCount + applicationErrCount > 0) {
                    reportWriter.println("\t<result>Fail</result>");
                } else {
                    reportWriter.println("\t<result>Pass</result>");
                }
            }

            reportWriter.println("  </body>");
            reportWriter.println("  <foot>");
            endTime = new GregorianCalendar();
            reportWriter.println("    <endTime>" + toDateTimeString(endTime)
                    + "</endTime>");
            reportWriter
                    .println("    <elapsedTime>"
                            + new SmilClock(
                                    (endTime.getTimeInMillis() - ((DefaultContext) (this.myContext))
                                            .getStartTime().getTimeInMillis()))
                                    .toString() + "</elapsedTime>");
            reportWriter.println("    <failureCount>" + failureCount
                    + "</failureCount>");
            reportWriter.println("    <procErrCount>" + processorErrCount
                    + "</procErrCount>");
            reportWriter.println("    <appErrCount>" + applicationErrCount
                    + "</appErrCount>");
            reportWriter.println("  </foot>");
            reportWriter.println("</zedValReport>");
            reportWriter.close();
            this.closed = true;
        } else {
            throw new ZedReporterException(
                    "Error in DefaultReporter.close():  reporter not initialized or already closed");
        }
    }

    // Converts date/time to format as defined by XML Schema dateTime datatype:
    // Format is YYYY-MM-DDThh:mm:ss.sss, followed by time offset from UTC
    static String toDateTimeString(GregorianCalendar gc) {
        DecimalFormat TwoDigNf;
        int timeZoneOffset;

        gc = new GregorianCalendar();
        TwoDigNf = new DecimalFormat();
        TwoDigNf.setMinimumIntegerDigits(2);
        timeZoneOffset = gc.get(Calendar.ZONE_OFFSET)
                + gc.get(Calendar.DST_OFFSET);

        return gc.get(Calendar.YEAR) + "-"
                + TwoDigNf.format(gc.get(Calendar.MONTH) + 1) + "-"
                + TwoDigNf.format(gc.get(Calendar.DATE)) + "T"
                + TwoDigNf.format(gc.get(Calendar.HOUR_OF_DAY)) + ":"
                + TwoDigNf.format(gc.get(Calendar.MINUTE)) + ":"
                + TwoDigNf.format(gc.get(Calendar.SECOND)) + "."
                + gc.get(Calendar.MILLISECOND)
                + TwoDigNf.format(timeZoneOffset / 3600000) + ":"
                + TwoDigNf.format(timeZoneOffset % 3600000);
    }

    private boolean initialized;

    private boolean closed;

    private PrintWriter reportWriter;

    private long failureCount = 0; // Total number of FailureMessages received

    private long processorErrCount = 0; // Total number of TestProcessorErrMsgs

    // received
    private long applicationErrCount = 0; // Total number of
                                            // ApplicationErrMsgs

    // received
    private GregorianCalendar endTime;

    private ZedContext myContext;
}
