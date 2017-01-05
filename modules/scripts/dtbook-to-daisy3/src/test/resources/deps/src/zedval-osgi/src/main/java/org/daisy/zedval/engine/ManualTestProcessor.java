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

package org.daisy.zedval.engine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * A <code>ManualTestProcessor</code> object creates a report of tests that
 * will need to be done manually
 * 
 * @author James Pritchett
 */
public class ManualTestProcessor extends ZedTestProcessor {

    /**
     * @param id
     *            ID of this ManualTestProcessor (from processor map)
     * @param l
     *            Name for this ManualTestProcessor
     * @param tests
     *            LinkedHashMap of ZedTests that this ManualTestProcessor
     *            reports (key = id)
     * @param files
     *            LinkedHashMap of ZedFiles upon which this ManualTestProcessor
     *            is to be invoked (key = absolute full path)
     * @param c
     *            ZedContext for this run
     * @param reportFile
     *            A File object defining the report output file
     */
	public ManualTestProcessor(String id, String l, Map<String,ZedTest> tests,
			Map<String,ZedFile> files, ZedContext c, File reportFile) {
        super(id, l, tests, files, c);
        this.reportFile = reportFile;
    }

    /**
     * Performs the tests on all the files
     */
	public boolean performTests() {
        BufferedWriter bw;
        boolean retVal = false; // Return value
        //int j;

        // If we have no report, files, or tests, then bail now
        if (this.reportFile == null || this.getFilesTested() == null
                || this.getTestsImplemented() == null)
            return retVal;

        // Create/initialize report file
        try {
            bw = new BufferedWriter(new FileWriter(this.reportFile));

            bw.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>");
            bw.newLine();
            bw
                    .write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\" >");
            bw.newLine();
            bw.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
            bw.newLine();
            bw.write("<head>");
            bw.newLine();
            bw.write("<title>Manual test checklist: " + this.getLabel()
                    + "</title>");
            bw.newLine();
            bw.write("</head>");
            bw.newLine();
            bw.write("<body>");
            bw.newLine();
            bw.write("<h1>Manual test checklist: " + this.getLabel() + "</h1>");
            bw.newLine();
        } catch (IOException e) {
            try {
                this.getContext().getReporter().addMessage(
                        new TestProcessorErrMsg("Cannot run testProcessor ["
                                + this.getLabel() + "]"
                                + ": IOException writing output file "
                                + this.reportFile.getName() + ": "
                                + e.getMessage(), "en", false, this));
            } catch (ZedReporterException zre) {
                System.err.println("Exception while writing to reporter: "
                        + zre.getMessage());
            }
            return retVal;
        }

        // Output list of files
        try {
            bw.write("<h2>Files</h2>");
            bw.newLine();
            bw.write("<p>Perform tests on the following files:</p>");
            bw.newLine();
            bw.write("<ul>");
            bw.newLine();
            for (ZedFile zf : this.getFilesTested().values()) {
            	bw.write("<li>" + zf.getName() + "</li>");
            	bw.newLine();
            }
            bw.write("</ul>");
            bw.newLine();
        } catch (IOException e) {
            try {
                this.getContext().getReporter().addMessage(
                        new TestProcessorErrMsg("Cannot run testProcessor ["
                                + this.getLabel() + "]"
                                + ": IOException writing output file "
                                + this.reportFile.getName() + ": "
                                + e.getMessage(), "en", false, this));
            } catch (ZedReporterException zre) {
                System.err.println("Exception while writing to reporter: "
                        + zre.getMessage());
            }
            return retVal;
        }

        // Output list of tests
        try {
            bw.write("<h2>Tests</h2>");
            bw.newLine();
            bw.write("<p>Perform the following tests:</p>");
            bw.newLine();
            bw.write("<table><thead><tr>");
            bw.newLine();
            bw.write("<td>Test ID</td>");
            bw.newLine();
            bw.write("<td>Test description</td>");
            bw.newLine();
            bw.write("<td>Type</td>");
            bw.newLine();
            bw.write("</tr></thead><tbody>");
            bw.newLine();
            for (ZedTest zt : this.getTestsImplemented().values()) {
            	bw.write("<tr><td>" + zt.getId() + "</td>");
            	bw.newLine();
            	bw.write("<td>" + zt.getDescription() + "</td>");
            	bw.newLine();
            	bw.write("<td>");
            	if (zt.getType() == ZedTest.REQUIREMENT) {
            		bw.write("Requirement");
            	} else if (zt.getType() == ZedTest.RECOMMENDATION) {
            		bw.write("Recommendation");
            	} else {
            		bw.write("&lt;unknown&gt;");
            	}
            	bw.write("</td></tr>");
            	bw.newLine();
			}
            bw.write("</tbody></table>");
            bw.newLine();
        } catch (IOException e) {
            try {
                this.getContext().getReporter().addMessage(
                        new TestProcessorErrMsg("Cannot run testProcessor ["
                                + this.getLabel() + "]"
                                + ": IOException writing output file "
                                + this.reportFile.getName() + ": "
                                + e.getMessage(), "en", false, this));
            } catch (ZedReporterException zre) {
                System.err.println("Exception while writing to reporter: "
                        + zre.getMessage());
            }
            return retVal;
        }

        // Close the report
        try {
            bw.write("</body>");
            bw.newLine();
            bw.write("</html>");
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            try {
                this.getContext().getReporter().addMessage(
                        new TestProcessorErrMsg("Cannot run testProcessor ["
                                + this.getLabel() + "]"
                                + ": IOException writing output file "
                                + this.reportFile.getName() + ": "
                                + e.getMessage(), "en", false, this));
            } catch (ZedReporterException zre) {
                System.err.println("Exception while writing to reporter: "
                        + zre.getMessage());
            }
            return retVal;
        }

        retVal = true;

        return retVal;
    }

    /**
     * Returns the File object for the output report
     * 
     * @return The File object
     */
    public File getReportFile() {
        return this.reportFile;
    }

    public String toString() {
        return super.toString() + "\t[uses=" + this.reportFile + "]\n";
    }

    private File reportFile;

}