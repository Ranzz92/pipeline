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

import java.net.URL;
import java.util.Map;

/**
 * Encapsulates run-time context information
 * <p>
 * This can be subclassed to provide more context information
 * </p>
 * 
 * @author James Pritchett
 * @author Daniel Carlsson
 */
public class ZedContext {

    /**
     * @param appName
     *            The name of the application being run
     * @param appVersion
     *            The version number of the application being run
     */
    public ZedContext(String appName, String appVersion) {
        this.appName = appName;
        this.appVersion = appVersion;
    }

    /**
     * Creates and initializes a PackageFile object for this run
     * 
     * @param fullPath
     *            The full path (including name) of the package file to load
     */
    public void loadPackageFile(String fullPath) throws ZedFileInitializationException {

        this.myPackage = new PackageFile(fullPath);
        //PackageFile.initialize() populates the whole input fileset.
        //At the end, it will throw any exceptions collected
        //during fileset population. These are simply rethrown here.
        //it is up to the reciever to decide what to do
        try {
        	this.myPackage.initialize();
        } catch (ZedFileInitializationException zfe) {
        	//zfe is exceptions that were collected during
        	//fileset instantiation.
        	throw zfe;
        }

        
        
        //String s;
        // Initialize the package file; exceptions here are rethrown
//        try {
//            this.myPackage.initialize();
//            //check the minimal requirements for continuing without throwing
//            if(!this.myPackage.exists()){
//            	throw new ZedContextException("packagefile " + this.myPackage.getAbsolutePath() +  "does not exist");
//            }
//            if(!this.myPackage.canRead()){
//            	throw new ZedContextException("packagefile " + this.myPackage.getAbsolutePath() + "is not readable");
//            }
//            if(!this.myPackage.isWellFormed()){
//            	throw new ZedContextException("packagefile " + this.myPackage.getAbsolutePath() + "is not wellformed");
//            }              	            
//        } catch (ZedFileInitializationException zfe) {
//            //check the minimal requirements for continuing without throwing
//            if(!this.myPackage.exists()){
//            	throw new ZedContextException("packagefile " + this.myPackage.getAbsolutePath() +  "does not exist");
//            }
//            if(!this.myPackage.canRead()){
//            	throw new ZedContextException("packagefile " + this.myPackage.getAbsolutePath() + "is not readable");
//            }
//            if(!this.myPackage.isWellFormed()){
//            	throw new ZedContextException("packagefile " + this.myPackage.getAbsolutePath() + "is not wellformed");
//            } 
//            //else dont throw
//            //throw zfe;
//        }
    }

    /**
     * Sets the package file for this run
     * 
     * @param p
     *            The PackageFile object for this run
     */
    public void setPackageFile(PackageFile p) {
        this.myPackage = p;
    }

    /**
     * @return The PackageFile object for this run
     */
    public PackageFile getPackageFile() {
        return this.myPackage;
    }

    /**
     * Creates and initializes a test map MapFile object for this run
     * @param fullPath
     *            The full path (including name) of the test map file to load
     */
    public void loadTestMap(String fullPath)
            throws ZedFileInitializationException {

        this.testMap = new ZedMap(fullPath);

    }

    /**
     * Creates and initializes a test map MapFile object for this run
     * @param url
     *            URL of the test map file to load
     */
    public void loadTestMap(URL url) throws ZedFileInitializationException {

        this.testMap = new ZedMap(url);

    }

    /**
     * Sets the test map file for this run
     * 
     * @param tm
     *            The test map ZedMao object for this run
     */
    public void setTestMap(ZedMap tm) {
        this.testMap = tm;
    }

    /**
     * Returns the test map file used in this run
     * 
     * @return the test map MapFile object used in this run
     */
    public ZedMap getTestMap() {
        return this.testMap;
    }

    /**
     * Creates and initializes a processor map MapFile object for this run
     * 
     * @param fullPath
     *            The full path (including name) of the processor map file to
     *            load
     */
    public void loadProcessorMap(String fullPath) throws ZedFileInitializationException {

        this.procMap = new ZedMap(fullPath);
    }

    public void loadProcessorMap(URL url) throws ZedFileInitializationException {

        this.procMap = new ZedMap(url);
    }

    /**
     * Sets the processor map file for this run
     * 
     * @param pm
     *            The processor map MapFile object for this run
     */
    public void setProcessorMap(ZedMap pm) {
        this.procMap = pm;
    }

    /**
     * Returns the processor map file used in this run
     * 
     * @return the processor map MapFile used in this run
     */
    public ZedMap getProcessorMap() {
        return this.procMap;
    }

    /**
     * Creates a ZedReporter object for this run
     * 
     * @param className
     *            The name of the ZedReporter implementation class to use
     */
    public void loadReporter(String className) throws ZedContextException {
        ZedReporter r;
        try {
            r = (ZedReporter) Class.forName(className).newInstance();
        } catch (ClassNotFoundException e) {
            throw new ZedContextException("Can't locate reporter class "
                    + className);
        } catch (InstantiationException e) {
            throw new ZedContextException("Can't instantiate reporter "
                    + className);
        } catch (IllegalAccessException e) {
            throw new ZedContextException(
                    "Illegal access exception when instantiating " + className);
        }
        this.setReporter(r);
    }

    /**
     * Sets the reporter for this run
     * 
     * @param r
     *            The ZedReporter object for this run
     */
    public void setReporter(ZedReporter r) {
        this.reporter = r;
        this.reporter.setContext(this); // Be sure to set self as context!
    }

    /**
     * Returns the ZedReporter instance for this run
     * @return The ZedReporter instance for this run
     */
    public ZedReporter getReporter() {
        return this.reporter;
    }

    /**
     * Instantiates all applicable test processors based on list of tests and
     * processor map
     * <p>
     * <em>Note:</em> setTests() or loadTests() must be called before this
     * method
     * </p>
     * 
     * @return LinkedHashMap of ZedTestProcessors created (key = id)
     */
	public Map<String,ZedTestProcessor> loadTestProcessors() throws ZedContextException {
        // This will contain whatever default processor-test map coordination
        // logic
        // we decide upon.
        return this.processorList;
    }

    /**
     * Sets the list of test processors to use in this run
     * 
     * @param tp
     *            LinkedHashMap of ZedTestProcessor objects to use in this run
     *            (key = id)
     */
    public void setTestProcessors(Map<String,ZedTestProcessor> tp) {
        this.processorList = tp;
    }

    /**
     * Gets the list of test processors to use in this run
     * 
     * @return LinkedHashMap of ZedTestProcessor objects to use in this run (key =
     *         id)
     */
	public Map<String,ZedTestProcessor> getTestProcessors() {
        return this.processorList;
    }

    /**
     * Instantiates all applicable tests based on contents of package file and
     * test map
     * 
     * @return LinkedHashMap of ZedTests created (key = id)
     */
	public Map<String,ZedTest> loadTests() throws ZedContextException {
        // Use XPath here ..
        return this.testList;
    }

    /**
     * Sets the list of tests to use in this run
     * 
     * @param t
     *            LinkedHashMap of ZedTest objects to use in this run (key = id)
     */
	public void setTests(Map<String,ZedTest> t) {
        this.testList = t;
    }

    /**
     * Gets the list of tests to use in this run
     * 
     * @return LinkedHashMap of ZedTest objects to use in this run (key = id)
     */
	public Map<String,ZedTest> getTests() {
        return this.testList;
    }

    /**
     * Prints the entire list of tests to stdout
     * 
     */

	public void printTests() {
		for (String testId : this.testList.keySet()) {
			System.out.println(testId);
		}
    }

    /**
     * Returns the application name
     * 
     * @return the application name
     */
    public String getAppName() {
        return this.appName;
    }
    
    /**
     * Returns the application version number
     * 
     * @return the application version number
     */
    public String getAppVersion() {
        return this.appVersion;
    }

    public String getSpecYear() {
        if (null != this.getPackageFile()) {
            if (this.getPackageFile().getSpecVersion().equals(
                    ZedConstants.Z3986_VERSION_2002)) {
                return "2002";
            } else if (this.getPackageFile().getSpecVersion().equals(
                    ZedConstants.Z3986_VERSION_2005)) {
                return "2005";
            }
        }
        return null;
    }

	public String toString() {
        String s;

        s = getClass().getName() + " [appName=" + this.appName + " appVersion="
                + this.appVersion;

        if (this.myPackage != null) {
            s = s + " myPackage=" + this.myPackage.getName();
        } else {
            s = s + " myPackage=null";
        }
        if (this.testMap != null) {
            s = s + " testMap=" + this.testMap.getName();
        } else {
            s = s + " testMap=null";
        }
        if (this.procMap != null) {
            s = s + " procMap=" + this.procMap.getName();
        } else {
            s = s + " procMap=null";
        }
        if (this.reporter != null) {
            s = s + " reporter=" + this.reporter.getClass().getName();
        } else {
            s = s + " reporter=null";
        }
        s = s + "]\n";

        if (this.testList != null) {
            s = s + "\t[testList=\n";
            for (ZedTest test : testList.values()) {
            	s = s + "\t\t" + test;
			}
            s = s + "\t]\n";
        } else {
            s = s + "\t[testList=null]\n";
        }
        if (this.processorList != null) {
            s = s + "\t[processorList=\n";
            for (ZedTestProcessor testProc : processorList.values()) {
            	s = s + "\t\t" + testProc;
			}
            s = s + "\t]\n";
        } else {
            s = s + "\t[processorList=null]\n";
        }

        return s;
    }

    public static final String ENGINE_VERSION = "2.1";

    private String appName;

    private String appVersion;

    private PackageFile myPackage;

    private ZedMap testMap;

    private ZedMap procMap;

    private ZedReporter reporter;

	private Map<String,ZedTest> testList;

	private Map<String,ZedTestProcessor> processorList;
}
