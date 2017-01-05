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

import java.util.Map;

/**
 * A <code>ZedTestProcessor</code> object executes one or more Tests on one or
 * more ZedFiles
 * 
 * @author James Pritchett
 */

public class ZedTestProcessor {

    /**
     * @param id
     *            ID of this ZedTestProcessor (from processor map)
     * @param l
     *            Name for this ZedTestProcessor
     * @param tests
     *            Map of ZedTests that this ZedTestProcessor
     *            implements (key = id)
     * @param files
     *            Map of ZedFiles upon which this ZedTestProcessor is
     *            to be invoked (key = absolute full path)
     * @param c
     *            ZedContext for this run
     */
	public ZedTestProcessor(String id, String l, Map<String,ZedTest> tests,
            Map<String,ZedFile> files, ZedContext c) {
        this.id = id;
        this.myLabel = l;
        this.myContext = c;
        this.filesTested = files;
        this.testsImplemented = tests;        
    }
    
    /**
     * Performs all implemented tests on all given files
     * 
     * @return TRUE if tests were performed on one or more files
     */
    public boolean performTests() {
        return false;
    }

    /**
     * Returns the id of this ZedTestProcessor (from processor map)
     * 
     * @return the id of this ZedTestProcessor
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the name for this ZedTestProcessor
     * 
     * @return the name for this ZedTestProcessor
     */
    public String getLabel() {
        return this.myLabel;
    }

    /**
     * Returns a list of all tests implemented by this ZedTestProcessor
     * 
     * @return a Map of ZedTests (key = ZedTest.getId())
     */
	public Map<String,ZedTest> getTestsImplemented() {
        return this.testsImplemented;
    }

    /**
     * Returns a list of all files tested by this ZedTestProcessor
     * 
     * @return a Map of ZedFile objects (key = File.getName())
     */
	public Map<String,ZedFile> getFilesTested() {
        return this.filesTested;
    }

    /**
     * Returns ZedContext for this run
     * 
     * @return ZedContext object for this run
     */
    public ZedContext getContext() {
        return this.myContext;
    }

	public String toString() {
        String s;

        s = getClass().getName() + "[label=" + this.myLabel + "]\n";
        if (this.testsImplemented != null) {
            s = s + "\t[testsImplemented=\n";
            for (ZedTest test : testsImplemented.values()) {
            	s = s + "\t\t" + test + "\n";
			}
            s = s + "\t]\n";
        } else {
            s = s + "\t[testsImplemented=null]\n";
        }
        if (this.filesTested != null) {
            s = s + "\t[filesTested=\n";
            for (ZedFile file : filesTested.values()) {
            	s = s + "\t\t" + file.getName() + "\n";
			}
            s = s + "\t]\n";
        } else {
            s = s + "\t[filesTested=null]\n";
        }

        // Note: Can't print myContext here because myContext will print its
        // TestProcessors, which
        // would make an infinite loop!
        return s;
    }

    private String id;

    private ZedContext myContext;

    private String myLabel;
    
	private Map<String,ZedTest> testsImplemented;

	private Map<String,ZedFile> filesTested;
}
