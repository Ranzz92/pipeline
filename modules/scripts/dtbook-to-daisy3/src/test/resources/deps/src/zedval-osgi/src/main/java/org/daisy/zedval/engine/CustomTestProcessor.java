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

import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A <code>CustomTestProcessor</code> object executes tests via a custom Java
 * class 
 * @author James Pritchett
 */
public class CustomTestProcessor extends ZedTestProcessor {

    /**
     * @param id
     *            ID of this ZedTestProcessor (from processor map)
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
     * @param testerName
     *            The name of a class that implements the ZedCustomTest
     *            interface that will actually execute the tests
     */
	public CustomTestProcessor(String id, String l, Map<String,ZedTest> tests,
			Map<String,ZedFile> files, ZedContext c, String testerName) {
        super(id, l, tests, files, c);
        this.testerName = testerName;
        /*
         * Instantiate the named ZedCustomTest [TestProcessorErrMsg and abort on
         * failure]
         */
        try {
            tester = (ZedCustomTest) Class.forName(testerName).newInstance();
        } catch (ClassNotFoundException e) {
            try {
                c
                        .getReporter()
                        .addMessage(
                                new TestProcessorErrMsg(
                                        "Cannot find ZedCustomTest class "
                                                + testerName, "en", true, this));
            } catch (ZedReporterException zre) {
                System.err.println("Exception while writing to reporter: "
                        + zre.getMessage());
            }
            tester = null;
            return;
        } catch (InstantiationException e) {
            try {
                c.getReporter().addMessage(
                        new TestProcessorErrMsg(
                                "Cannot instantiate ZedCustomTest class "
                                        + testerName, "en", true, this));
            } catch (ZedReporterException zre) {
                System.err.println("Exception while writing to reporter: "
                        + zre.getMessage());
            }
            tester = null;
            return;
        } catch (IllegalAccessException e) {
            try {
                c.getReporter().addMessage(
                        new TestProcessorErrMsg(
                                "Illegal access exception when instantiating ZedCustomTest class "
                                        + testerName, "en", true, this));
            } catch (ZedReporterException zre) {
                System.err.println("Exception while writing to reporter: "
                        + zre.getMessage());
            }
            tester = null;
            return;
        } catch (Exception testEx) {

        }

    }

    /**
     * Performs the tests on all the files
     */
	public boolean performTests() {
        boolean retVal = false; // Return value
        NodeList nl;
        int j;
        ZedTest failedTest;
        Element failNode;
        Node detailTextNode;
        long line = -1;
        long column = -1;

        // If we have no tester, files, or tests, then bail now
        if (this.tester == null || this.getFilesTested() == null
                || this.getTestsImplemented() == null)
            return retVal;

        /*
         * Iterate over the files collection and evaluate the test(s) for each
         */
        for (Iterator<ZedFile> i = this.getFilesTested().values().iterator(); i.hasNext();) {
            // Save this in case tester calls an error handler below
            this.curFile = (ZedFile) i.next(); 

            // First, check to be sure we can actually run the test on this file
            if (this.curFile.exists() == false) {
                try {
                    this.getContext().getReporter().addMessage(
                            new TestProcessorErrMsg(
                                    "Cannot run testProcessor ["
                                            + this.getLabel() + "] on file "
                                            + this.curFile.getName()
                                            + ": File does not exist", "en",
                                    false, this.curFile, this));
                } catch (ZedReporterException zre) {
                    System.err.println("Exception while writing to reporter: "
                            + zre.getMessage());
                }
            } else if (this.curFile.canRead() == false) {
                try {
                    this.getContext().getReporter().addMessage(
                            new TestProcessorErrMsg(
                                    "Cannot run testProcessor ["
                                            + this.getLabel() + "] on file "
                                            + this.curFile.getName()
                                            + ": File is not readable", "en",
                                    false, this.curFile, this));
                } catch (ZedReporterException zre) {
                    System.err.println("Exception while writing to reporter: "
                            + zre.getMessage());
                }
            } else if (this.curFile instanceof XmlFile
                    && ((XmlFile) (this.curFile)).isWellFormed() == false) {
                try {
                    this.getContext().getReporter().addMessage(
                            new TestProcessorErrMsg(
                                    "Cannot run testProcessor ["
                                            + this.getLabel()
                                            + "] on XML file "
                                            + this.curFile.getName()
                                            + ": File is not well-formed",
                                    "en", false, this.curFile, this));
                } catch (ZedReporterException zre) {
                    System.err.println("Exception while writing to reporter: "
                            + zre.getMessage());
                }
            }

            // If file is testable, tell the custom class to do it
            else {
                try {
                    this.tester.performTest(this.curFile);
                } catch (ZedCustomTestException e) {
                    try {
                        this.getContext().getReporter().addMessage(
                                new TestProcessorErrMsg(
                                        "Cannot run testProcessor ["
                                                + this.getLabel()
                                                + "] on file "
                                                + this.curFile.getName() + ": "
                                                + e.getMessage(), "en", false,
                                        this.curFile, this));
                    } catch (ZedReporterException zre) {
                        System.err
                                .println("Exception while writing to reporter: "
                                        + zre.getMessage());
                    }
                }
            }
            retVal = true;

            // Query the resulting DOM document and format messages for any
            // testFailure nodes found
            // QUESTION: What do we do with @badRef on <detail>?
            if (tester.getResultDoc() != null) {
                nl = tester.getResultDoc().getElementsByTagName("testFailure");
                for (j = 0; j < nl.getLength(); j++) {
                    failNode = (Element) nl.item(j); // <testFailure
                                                        // testRef="testId">

                    failedTest = (ZedTest) this.getTestsImplemented().get(
                            failNode.getAttribute("testRef"));
                    if (failedTest != null) {
                        failNode.normalize(); // Make sure detail text is all
                                                // in one text node
                        detailTextNode = ((Element) failNode.getFirstChild())
                                .getFirstChild(); // <detail>Text</detail>
                                                
                        if (failNode.getAttribute("line").equals("") == false) {
                            line = Long
                                    .parseLong(failNode.getAttribute("line"));
                        } else {
                            line = -1; // This means no data
                        }
                        if (failNode.getAttribute("col").equals("") == false) {
                            column = Long.parseLong(failNode
                                    .getAttribute("col"));
                        } else {
                            column = -1; // This means no data
                        }
                        try {
                            this.getContext().getReporter().addMessage(
                                    new FailureMessage(detailTextNode
                                            .getNodeValue().trim(), "en", line,
                                            column, failedTest, this.curFile,
                                            this));
                        } catch (ZedReporterException zre) {
                            System.err
                                    .println("Exception while writing to reporter: "
                                            + zre.getMessage());
                        }
                    }else{
                    	//Since tests are filtered after perform (some dont apply to the instance) this
                    	//state is sometimes ok:
                    	//System.err.println("Could not allocate a ZedTest for IDREF " + failNode.getAttribute("testRef"));	
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * Returns the object being used to test files
     * 
     * @return The ZedCustomTest object
     */
    public ZedCustomTest getTester() {
        return this.tester;
    }

    /**
     * Returns the class name of the object being used to test files
     * 
     * @return The ZedCustomTest implementation class name
     */
    public String getTesterName() {
        return this.testerName;
    }

    public String toString() {
        return super.toString() + "\t[uses=" + this.testerName + "]\n";
    }

    private String testerName;

    private ZedCustomTest tester;

    private ZedFile curFile;

}