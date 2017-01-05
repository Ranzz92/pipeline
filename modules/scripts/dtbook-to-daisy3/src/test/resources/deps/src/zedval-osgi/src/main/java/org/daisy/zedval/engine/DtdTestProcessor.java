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

// DtdTestProcessor.java
/* Versions:
 0.1.0
 - Filled in performTests()
 0.1.1
 - Added toString() for debugging
 0.1.2
 - Added id to constructor
 - Added getDtdFile() method
 0.1.4 (22/02/2003)
 - Added some detail to the parse exception error messages
 - Changed parse call to use a file:/// URI
 - Made this an implementation of EntityResolver interface (removed need for ZedResolver)
 - When doing entity resolution, this processor now uses the same DTDs that XmlFile does,
 using the XmlFile dtdPath field.
 - Since DTD is no longer loaded as a file, we only store the name
 - Since DTD is no longer loaded as a file, we no longer test existence/readability
 - When doing entity resolution, will substitute whatever external DTD subset is defined
 in the input file with the canonical DTD
 0.2.0 (22/03/2003)
 - Fixed bug that caused OPF validation to fail because it couldn't find oeb1.ent
 - Now issues TestProcessorError if document has no DOCTYPE (temporary fix)
 0.3.0 (28/05/2003)
 - performTests() now returns boolean (FALSE if processor could not be run on
 any file)
 - Fixed bug in resolveEntity() where it used a bogus version of oeb1.ent public ID
 1.2 (2005)
 -mg 20050707: complete remake, now only reads XmlFile properties
 -jp 20050708: Updated to support ZedReporterExceptions
 */
package org.daisy.zedval.engine;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXParseException;

/**
 * A <code>DtdTestProcessor</code> object executes tests via DTD validation
 * 
 * @author James Pritchett
 * @author Daniel Carlsson
 * @author Markus Gylling
 */
public class DtdTestProcessor extends ZedTestProcessor {
    private String dtdName;

    private ZedFile curFile;

    /**
     * @param id
     *            Id of this ZedTestProcessor (from processor map)
     * @param l
     *            Name for this ZedTestProcessor
     * @param tests
     *            LinkedHashMap of ZedTests that this ZedTestProcessor
     *            implements (key = id)
     * @param files
     *            LinkedHashMap of ZedFiles upon which this ZedTestProcessor is
     *            to be invoked (key = absolute full path)
     * @param c
     *            ZedContext for this run
     * @param dtdName
     *            Full name/path of DTD against which to validate
     */

	public DtdTestProcessor(String id, String l, Map<String,ZedTest> tests,
			Map<String,ZedFile> files, ZedContext c, String dtdName) {
        super(id, l, tests, files, c);
        this.dtdName = dtdName;
    }

	public boolean performTests() {
        boolean retVal = true;
        for (Iterator<ZedFile> i = this.getFilesTested().values().iterator(); i
                .hasNext();) {
            this.curFile = i.next();
            // Check to be sure we can actually run the test on this file
            try {
                if (!this.curFile.exists()) {
                    try {
                        this.getContext().getReporter().addMessage(
                                new TestProcessorErrMsg(
                                        "Cannot run testProcessor ["
                                                + this.getLabel()
                                                + "] on file "
                                                + this.curFile
                                                        .getCanonicalPath()
                                                + ": File does not exist",
                                        "en", false, this.curFile, this));
                    } catch (ZedReporterException zre) {
                        System.err
                                .println("Exception while writing to reporter: "
                                        + zre.getMessage());
                    }
                } else if (!this.curFile.canRead()) {
                    try {
                        this.getContext().getReporter().addMessage(
                                new TestProcessorErrMsg(
                                        "Cannot run testProcessor ["
                                                + this.getLabel()
                                                + "] on file "
                                                + this.curFile
                                                        .getCanonicalPath()
                                                + ": File is not readable",
                                        "en", false, this.curFile, this));
                    } catch (ZedReporterException zre) {
                        System.err
                                .println("Exception while writing to reporter: "
                                        + zre.getMessage());
                    }
                } else if (!(this.curFile instanceof XmlFile)) {
                    try {
                        this.getContext().getReporter().addMessage(
                                new TestProcessorErrMsg(
                                        "Cannot run testProcessor ["
                                                + this.getLabel()
                                                + "] on file "
                                                + this.curFile
                                                        .getCanonicalPath()
                                                + ": File is not an XML file",
                                        "en", false, this.curFile, this));
                    } catch (ZedReporterException zre) {
                        System.err
                                .println("Exception while writing to reporter: "
                                        + zre.getMessage());
                    }
                    // } else if (!(((XmlFile) (this.curFile)).isWellFormed()))
                    // {
                    // try {
                    // this.getContext().getReporter().addMessage(new
                    // TestProcessorErrMsg("Cannot run testProcessor ["+
                    // this.getLabel()+ "] on XML file "+
                    // this.curFile.getCanonicalPath()+ ": File is not
                    // well-formed", "en", false, this.curFile, this));
                    // } catch (ZedReporterException zre) {
                    // System.err.println("Exception while writing to reporter:
                    // " + zre.getMessage());
                    // }
                } else if (((XmlFile) (this.curFile)).getDoctypeName() == null) { // No
                                                                                    // DOCTYPE!
                    try {
                        this.getContext().getReporter().addMessage(
                                new TestProcessorErrMsg(
                                        "Cannot run testProcessor ["
                                                + this.getLabel()
                                                + "] on XML file "
                                                + this.curFile
                                                        .getCanonicalPath()
                                                + ": File has no DOCTYPE",
                                        "en", false, this.curFile, this));
                    } catch (ZedReporterException zre) {
                        System.err
                                .println("Exception while writing to reporter: "
                                        + zre.getMessage());
                    }
                } else if (!((XmlFile) (this.curFile)).isValidated()) {
                    try {
                        this
                                .getContext()
                                .getReporter()
                                .addMessage(
                                        new TestProcessorErrMsg(
                                                "Cannot run testProcessor ["
                                                        + this.getLabel()
                                                        + "] on XML file "
                                                        + this.curFile
                                                                .getCanonicalPath()
                                                        + ": File has not been parsed with validation turned on",
                                                "en", false, this.curFile, this));
                    } catch (ZedReporterException zre) {
                        System.err
                                .println("Exception while writing to reporter: "
                                        + zre.getMessage());
                    }
                } else {
                    // we can run the test on this file
                    XmlFile xmlfile = (XmlFile) this.curFile;
                    try {
                        // if (!xmlfile.isValid()) {
                        // get fatal errors (although this with 99.99% certainty
                        // means that isWellformed==false so we wont get here)
                        List<SAXParseException> errors = xmlfile.getValidationFatalErrors();
                        for (SAXParseException e : errors) {
                        	this.getContext().getReporter().addMessage(
                        			new FailureMessage(
                        					"A fatal validation error occurred: "
                        							+ e.getMessage(), "en",
                        							e.getLineNumber(), e
                        							.getColumnNumber(),
                        							this.curFile, this));
						}
                        // get non-fatal errors
                        errors = xmlfile.getValidationErrors();
                        for (SAXParseException e : errors) {
                        	this.getContext().getReporter().addMessage(
                        			new FailureMessage(
                        					"A validation error occurred: "
                        							+ e.getMessage(), "en",
                        							e.getLineNumber(), e
                        							.getColumnNumber(),
                        							this.curFile, this));
						}
                        // get warnings
                        errors = xmlfile.getValidationWarnings();
                        for (SAXParseException e : errors) {
                            this.getContext().getReporter().addMessage(
                                    new FailureMessage(
                                            "A validation warning occurred: "
                                                    + e.getMessage(), "en",
                                             e.getLineNumber(), e
                                                    .getColumnNumber(),
                                            this.curFile, this));
                        }
                        // }
                    } catch (ZedReporterException zre) {
                        System.err
                                .println("Exception while writing to reporter: "
                                        + zre.getMessage());
                    }
                } // else
            } catch (IOException e) {
                try {
                    this.getContext().getReporter().addMessage(
                            new TestProcessorErrMsg(
                                    "IOException file integrity tests: "
                                            + e.getMessage(), "en", false,
                                    this.curFile, this));
                } catch (ZedReporterException zre) {
                    System.err.println("Exception while writing to reporter: "
                            + zre.getMessage());
                }
                retVal = false;
                continue;
            }
        }// for (Iterator i = this.getFilesTested()
        return retVal;
    }// performTests()

    /**
     * @return name (String) of the DTD being used by this processor
     */
    public String getDtdName() {
        return this.dtdName;
    }
}