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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.daisy.util.xml.SmilClock;
import org.daisy.util.xml.XPathUtils;
import org.daisy.zedval.engine.CustomTestProcessor;
import org.daisy.zedval.engine.DtdTestProcessor;
import org.daisy.zedval.engine.ManualTestProcessor;
import org.daisy.zedval.engine.RngSchematronTestProcessor;
import org.daisy.zedval.engine.SmilFile;
import org.daisy.zedval.engine.TextFile;
import org.daisy.zedval.engine.XslTestProcessor;
import org.daisy.zedval.engine.ZedConstants;
import org.daisy.zedval.engine.ZedContext;
import org.daisy.zedval.engine.ZedContextException;
import org.daisy.zedval.engine.ZedFile;
import org.daisy.zedval.engine.ZedMessage;
import org.daisy.zedval.engine.ZedTest;
import org.daisy.zedval.engine.ZedTestProcessor;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * 
 * @author Markus Gylling
 *
 */
public class DefaultContext extends ZedContext {
    private int verbosity = 2;
    private boolean outputTestList;
    private boolean debugState = false;
    private String testListName;
    private String stylesheetName;
    private boolean manualTests = false;
    private String manualReportPath;
    private String[] commandLineArguments;
    private GregorianCalendar startTime;

    /**
     * @param appName
     *            The name of the application being run
     * @param appVersion
     *            The version number of the application being run
     */
    public DefaultContext(String appName, String appVersion) {

        super(appName, appVersion);

        // Set default values here, to be overridden by arguments later
        startTime = new GregorianCalendar(); // Grab start time here
    }

    /**
     * <p>
     * Runs integrity checks on the current state of the context. Are we ready
     * to build a testList and validate a DTB against it?
     * </p>
     * <p>
     * This methods returns either boolean.TRUE or throws an Exception
     * </p>
     * 
     * @throws ZedContextException
     */
    boolean isValid() throws ZedContextException {

        // do we have a reporter?
        if (this.getReporter() == null) {
            throw new ZedContextException("No reporter available for output");
        }

        // do we have a packagefile?
        if (this.getPackageFile() == null) {
            throw new ZedContextException("Packagefile not available");
        }

        // is the packagefile in good physical shape?
        // or is she old and torn by too much parsing?
        if ((!this.getPackageFile().exists())
                || (!this.getPackageFile().canRead())
                || (!this.getPackageFile().isWellFormed())) {
            throw new ZedContextException(
                    "Packagefile not found or not readable");
        }

        // does the packagefile contain specVersion?
        if (this.getPackageFile().getSpecVersion() == null) {
            throw new ZedContextException(
                    "Packagefile does not contain information "
                            + "on Z3986 specification version");
        }

        // is there a testmap? is it ok?
        if (this.getTestMap() == null) {
            throw new ZedContextException("Testmap not set");
        }
        if (!this.getTestMap().exists()) {
            throw new ZedContextException("Testmap doesnt exist");
        }
        if (!this.getTestMap().isValid()) {
            throw new ZedContextException("Testmap is not valid");
        }

        // is there a procmap? is it ok?
        if (this.getProcessorMap() == null) {
            throw new ZedContextException("Processormap not set");
        }
        if (!(this.getProcessorMap().exists())) {
            throw new ZedContextException("Processormap doesnt exist");
        }
        if (!this.getProcessorMap().isValid()) {
            throw new ZedContextException("Processormap is not valid");
        }

        // does mapversion match packagefile specversion?
        if (!this.getPackageFile().getSpecVersion().equals(
                this.getProcessorMap().getSpecVersion())) {
            throw new ZedContextException(
                    "Packagefile specification version does not match "
                            + "processor map specification version");
        }
                
        return true;
    }

    public URL getDefaultProcMap(String specVersion)
            throws ZedContextException {        
        try {
            if (specVersion.equals(ZedConstants.Z3986_VERSION_2002)) {
                URL url = this.getClass().getResource(
                "/org/daisy/zedval/zedsuite/v2002/maps/procMap2002.xml");                
                return url;
            } else if (specVersion.equals(ZedConstants.Z3986_VERSION_2005)) {
                URL url = this.getClass().getResource(
                "/org/daisy/zedval/zedsuite/v2005/maps/procMap2005.xml");                
                return url;
            } else {
                throw new ZedContextException("Unrecognizable spec version");
            }
        } catch (Exception e) {
            throw new ZedContextException(e.getClass().getName()
                    + " in DefaultContext.getDefaultProcMap(): "
                    + e.getMessage());
        }
    }

    public URL getDefaultTestMap(String specVersion)
            throws ZedContextException {

        try {
            if (specVersion.equals(ZedConstants.Z3986_VERSION_2002)) {
                URL url = this.getClass().getResource(
                "/org/daisy/zedval/zedsuite/v2002/maps/testMap2002.xml");                
                return url;
            } else if (specVersion.equals(ZedConstants.Z3986_VERSION_2005)) {
                URL url = this.getClass().getResource(
                "/org/daisy/zedval/zedsuite/v2005/maps/testMap2005.xml");
                return url;
            } else {
                throw new ZedContextException("Unrecognizable spec version");
            }
        } catch (Exception e) {
            throw new ZedContextException(e.getClass().getName()
                    + " in DefaultContext.getDefaultTestMap(): "
                    + e.getMessage());
        }

    }

    /**
     * Instantiates all applicable tests based on contents of package file, test
     * map, and options
     * 
     * @return LinkedHashMap of ZedTests created (key = id)
     */
	public Map<String,ZedTest> loadTests() throws ZedContextException {
        String xpath;
        if (this.getTestMap() != null && this.getPackageFile() != null) {

            // Build the XPath string - user filters not implemented
            String xPathUsedFiles = "";
            if (this.getPackageFile().exists()) {
                xPathUsedFiles = xPathUsedFiles + "child::appliesToFile='opf'";
            }
            if (this.getPackageFile().getNcx() != null) {
                xPathUsedFiles = xPathUsedFiles
                        + "or child::appliesToFile='ncx'";
            }
            if (this.getPackageFile().getResource() != null) {
                xPathUsedFiles = xPathUsedFiles
                        + "or child::appliesToFile='resource'";
            }
            if (this.getPackageFile().getTextFiles() != null) {
                xPathUsedFiles = xPathUsedFiles
                        + "or child::appliesToFile='dtbook'";
            }
            if (this.getPackageFile().getAudioFiles() != null) {
                xPathUsedFiles = xPathUsedFiles
                        + "or child::appliesToFile='audio'";
            }
            if (this.getPackageFile().getAacFiles() != null) {
                xPathUsedFiles = xPathUsedFiles
                        + "or child::appliesToFile='aac'";
            }
            if (this.getPackageFile().getMp3Files() != null) {
                xPathUsedFiles = xPathUsedFiles
                        + "or child::appliesToFile='mp3'";
            }
            if (this.getPackageFile().getWavFiles() != null) {
                xPathUsedFiles = xPathUsedFiles
                        + "or child::appliesToFile='wav'";
            }
            if (this.getPackageFile().getImageFiles() != null) {
                xPathUsedFiles = xPathUsedFiles
                        + "or child::appliesToFile='image'";
            }
            if (this.getPackageFile().getJpegFiles() != null) {
                xPathUsedFiles = xPathUsedFiles
                        + "or child::appliesToFile='jpeg'";
            }
            if (this.getPackageFile().getPngFiles() != null) {
                xPathUsedFiles = xPathUsedFiles
                        + "or child::appliesToFile='png'";
            }
            if (this.getPackageFile().getSvgFiles() != null) {
                xPathUsedFiles = xPathUsedFiles
                        + "or child::appliesToFile='svg'";
            }
            if (this.getPackageFile().getSmilFiles() != null) {
                xPathUsedFiles = xPathUsedFiles
                        + "or child::appliesToFile='smil'";
            }

            if (xPathUsedFiles.startsWith("or "))
                xPathUsedFiles = xPathUsedFiles.substring(3);
            if (this.getPackageFile().getDtbMultimediaTypeAsDeclared() != null) {
                xpath = "//test[(child::appliesToDtbType='all' or child::appliesToDtbType='"
                        + this.getPackageFile()
                                .getDtbMultimediaTypeAsDeclared()
                        + "') and ("
                        + xPathUsedFiles + ")]";
            } else {
                xpath = "//test[child::appliesToDtbType='all' and "
                        + xPathUsedFiles + "]";
                // TODO some kind of warning here
            }

            // Use the simple XPath API to select a nodeIterator.
            NodeList nl = XPathUtils.selectNodes(this.getTestMap()
            		.getMapDocument(), xpath);

            // populate hashmap with zedtest objects key = test id
            final Map<String,ZedTest> testList = new LinkedHashMap<String,ZedTest>();
            ZedTest zTest;
            String testId;
            // To avoid a constructor call that I can't read I added
            // variables for the constructor parameters
            String testDescription = "";
            String testSpecificationReference = "";
            int testType;
            Map<String,ZedMessage> onFalseMessages = new HashMap<String,ZedMessage>();

            // Iterate through all tests that matched the XPath statement
            for (int j = 0; j < nl.getLength(); j++) {
            	Node n = nl.item(j);
            	// Set id variable for ztest
            	testId = n.getAttributes().getNamedItem("id").getNodeValue();

            	// Set type variable for ztest
            	if (n.getAttributes().getNamedItem("type").getNodeValue()
            			.matches("recommendation|strongRecommendation")) {
            		testType = ZedTest.RECOMMENDATION;
            	} else {
            		testType = ZedTest.REQUIREMENT;
            	}

            	// We iterate through the childnodes of the selection to set
            	// the remaining variables needed for the zedtest
            	NodeList nList = n.getChildNodes();
            	int messType;
            	if (nList != null) {
            		for (int i = 0; i < nList.getLength(); i++) {
            			// Set description variable for zedtest
            			if (nList.item(i).getNodeName().equals("testDesc")) {
            				testDescription = nList.item(i).getFirstChild()
            						.getNodeValue();
            			}
            			// Set specification reference variable for zedtest
            			else if (nList.item(i).getNodeName().equals(
            					"specRef")) {
            				testSpecificationReference = nList.item(i)
            						.getAttributes().getNamedItem("href")
            						.getNodeValue();
            			}
            			// Set error message variable for zedtest, the <msg
            			// /> elements class is used as key
            			else if (nList.item(i).getNodeName().equals(
            					"onFalseMsg")) {
            				onFalseMessages = new HashMap<String,ZedMessage>();
            				NodeList nFalseList = nList.item(i).getChildNodes();
            				if (nFalseList != null) {
            					for (int k = 0; k < nFalseList.getLength(); k++) {
            						if (nFalseList.item(k).getNodeName()
            								.equals("msg")) {
            							if (nFalseList.item(k)
            									.getAttributes()
            									.getNamedItem("class")
            									.getNodeValue()
            									.equalsIgnoreCase("long")) {
            								messType = ZedMessage.LONG;
            							} else if (nFalseList.item(k)
            									.getAttributes()
            									.getNamedItem("class")
            									.getNodeValue()
            									.equalsIgnoreCase("short")) {
            								messType = ZedMessage.SHORT;
            							} else {
            								messType = ZedMessage.DETAIL;
            							}
            							onFalseMessages.put(nFalseList.item(k).getAttributes().getNamedItem("class").getNodeValue(),
            									new ZedMessage(messType,nFalseList.item(k).getFirstChild().getNodeValue(),"en"));
            						}
            					}
            				}
            			}
            		}

                    // Initialize each test as zedtestobject and add to testlist
                    // key = testmapid
                    zTest = new ZedTest(testId, testDescription, testType,
                            onFalseMessages, testSpecificationReference);
                    testList.put(testId, zTest);
                }//while

                // We have initialized all tests and now add them to the context
                // object
                setTests(testList);
            }
        } // end Main if
        // Test map or package file not initialized
        else {
            throw new ZedContextException(
                    "Cannot load tests:  map file and/or package file are not initialized");
        }

        return this.getTests();
    }

    /**
     * Instantiates all applicable test processors based on test list and processor map
     * <p>
     * <em>Note:</em> setTests() or loadTests() must be called before this method
     * </p>
     * 
     * @return LinkedHashMap of ZedTestProcessors created
     */
	public Map<String,ZedTestProcessor> loadTestProcessors() throws ZedContextException {
        if (this.getTests() != null && this.getProcessorMap() != null) {
        	Map<String,ZedTest> curTests = new LinkedHashMap<String,ZedTest>();				// LinkedHashMap to fill with tests for each test processor
        	Map<String,ZedFile> curFiles = new LinkedHashMap<String,ZedFile>();				// LinkedHashMap to fill with files that the current test processor applies to
            Map<String,ZedTestProcessor> loadedTestProcessors = new LinkedHashMap<String,ZedTestProcessor>();
            ZedTestProcessor curTestProcessor = null;
            String testProcessorLabel = ""; 							// Variable to hold the test processor label


            NodeList testProcessorNodes = XPathUtils.selectNodes(this.getProcessorMap().getMapDocument(), "//testProcessor");

            // Iterate through all found testprocessors in the procmap
            for (int j = 0; j < testProcessorNodes.getLength(); j++) {
            	Node testProcessorNode = testProcessorNodes.item(j);
            	//if manual test
            	if (testProcessorNode.getAttributes().getNamedItem("type").getNodeValue().equals("MANUAL") && this.manualTests == false) {
            		continue;
            	}

            	curTests = null;
            	curFiles = null;

            	// We iterate through the testrefs
            	NodeList testProcessorNodeChildNodes = testProcessorNode.getChildNodes();  //TODO change this to a method that includes groupRef, returns a new node? Or normalize that at procMap parse...
            	if (testProcessorNodeChildNodes != null) {
            		String appliesToFile = null;
            		String appliesToGrammarVersion = null;

            		for (int i = 0; i < testProcessorNodeChildNodes.getLength(); i++) {
            			if (testProcessorNodeChildNodes.item(i).getNodeName().equals("label")) {
            				testProcessorLabel = testProcessorNodeChildNodes.item(i).getFirstChild() .getNodeValue();
            			}    
            			if (testProcessorNodeChildNodes.item(i).getNodeName().equals("testRef")) {
            				// Is test in testlist?
            				if (this.getTests().containsKey(testProcessorNodeChildNodes.item(i).getAttributes().getNamedItem("href").getNodeValue().trim())) {
            					// Add test to array
            					if (curTests == null) curTests = new LinkedHashMap<String,ZedTest>();

            					curTests.put(testProcessorNodeChildNodes.item(i).getAttributes().getNamedItem("href").getNodeValue(), 
            							this.getTests().get(testProcessorNodeChildNodes.item(i).getAttributes().getNamedItem("href").getNodeValue()));

            					// Initiate variables to keep the lines shorter
            					appliesToFile = testProcessorNode.getAttributes().getNamedItem("appliesToFile").getNodeValue();
            					//mg 20061126 appliesToGrammarVersion, optionally present
            					Node test = testProcessorNode.getAttributes().getNamedItem("appliesToGrammarVersion");
            					if(test!=null) appliesToGrammarVersion = test.getNodeValue();

            					// Add files to be tested to array.
            					// All tests in each testprocessor have the same value for appliesToFile so this only has to be done once each iteration.
            					try {
            						if (curFiles == null) {
            							curFiles = new LinkedHashMap<String,ZedFile>();
            							if (appliesToFile.equals("opf")) {
            								curFiles.put(this.getPackageFile().getCanonicalPath(),this.getPackageFile());
            							} else if (appliesToFile.equals("ncx")) {
            								curFiles.put(this.getPackageFile().getNcx().getCanonicalPath(),this.getPackageFile().getNcx());
            							} else if (appliesToFile.equals("audio")) {
            								curFiles.putAll(this.getPackageFile().getAudioFiles());
            							} else if (appliesToFile.equals("aac")) {
            								curFiles.putAll(this.getPackageFile().getAacFiles());
            							} else if (appliesToFile.equals("mp3")) {
            								curFiles.putAll(this.getPackageFile().getMp3Files());
            							} else if (appliesToFile.equals("wav")) {
            								curFiles.putAll(this.getPackageFile().getWavFiles());
            							} else if (appliesToFile.equals("image")) {
            								curFiles.putAll(this.getPackageFile().getImageFiles());
            							} else if (appliesToFile.equals("jpeg")) {
            								curFiles.putAll(this.getPackageFile().getJpegFiles());
            							} else if (appliesToFile.equals("png")) {
            								curFiles.putAll(this.getPackageFile().getPngFiles());
            							} else if (appliesToFile.equals("svg")) {
            								curFiles.putAll(this.getPackageFile().getSvgFiles());                                            
            							} else if (appliesToFile.equals("resource")) {
            								curFiles.put(this.getPackageFile().getResource().getCanonicalPath(),this.getPackageFile().getResource());
            							} else if (appliesToFile.equals("smil")) {
            								//curFiles = this.getPackageFile().getSmilFiles(); //pre minor version support, overridden by below
            								//mg 20061126: this grammar has minor versions, handle that by only add those files that match testprocessor version
            								if(appliesToGrammarVersion==null){
            									//this testprocessor is not minor version specific
            									curFiles.putAll(this.getPackageFile().getSmilFiles());
            								}else{
            									//this testprocessor is minor version specific
            									for (SmilFile s : this.getPackageFile().getSmilFiles().values()) {
            										String smilFileVersion = getDocTypeVersion(s);
            										try{
            											if(smilFileVersion.equals(appliesToGrammarVersion)){
            												curFiles.put(s.getCanonicalPath(), s);
            											}
            										}catch (NullPointerException npe) {
            											//could not determine smilfile version, fatal
            											throw new ZedContextException(
            													"Cannot determine version of " + s.getName() + "; missing doctype");
            										}
            									}                                            		
            								}
            							} else if (appliesToFile.equals("dtbook")) {
            								//curFiles = this.getPackageFile().getTextFiles(); //pre minor version support, overridden by below
            								//mg 20061126: this grammar has minor versions, handle that by only add those files that match testprocessor version
            								if(appliesToGrammarVersion==null){
            									//this testprocessor is not minor version specific
            									curFiles.putAll(getPackageFile().getTextFiles());
            								}else{
            									//this testprocessor is minor version specific
            									for (TextFile t : this.getPackageFile().getTextFiles().values()) {
            										String textFileVersion = getDocTypeVersion(t);
            										try{
            											if(textFileVersion.equals(appliesToGrammarVersion)){
            												curFiles.put(t.getCanonicalPath(), t);
            											}

            										}catch (NullPointerException npe) {
            											//could not determine smilfile version, fatal
            											throw new ZedContextException(
            													"Cannot determine version of " + t.getName() + "; missing doctype");
            										}
            									}                                            		
            								}
            							}
            						}
            					} catch (IOException e) {
            						throw new ZedContextException("IOException while getting canonical path: " + e.getMessage());
            					}
            				} else {
            					//System.err.println("!!no testref found: " + testProcessorNodeChildNodes.item(i).getAttributes().getNamedItem("href").getNodeValue());
            				}//if (this.getTests().containsKey(testProcessorNodeChildNodes.item(i)
            			} //if (testProcessorNodeChildNodes.item(i).getNodeName().equals("testRef"))
            		} //for (int i = 0; i < testProcessorNodeChildNodes.getLength(); i++)
            	} //if (testProcessorNodeChildNodes != null)

            	// We now have the information we need to create the testprocessor
            	if (curFiles != null && !curFiles.isEmpty()) { //mg added isEmpty 20061123
            		if (testProcessorNode.getAttributes().getNamedItem("type").getNodeValue().equals("RNG")) {
            			boolean useRng = true;
            			boolean useSchematron = false;
            			curTestProcessor = new RngSchematronTestProcessor(testProcessorNode.getAttributes().getNamedItem("id").getNodeValue(), 
            					testProcessorLabel, curTests,curFiles, this, testProcessorNode.getAttributes().getNamedItem("uses").getNodeValue(), useRng, useSchematron);
            		} else if (testProcessorNode.getAttributes().getNamedItem("type").getNodeValue().equals("SCH")) {
            			boolean useRng = false;
            			boolean useSchematron = true;
            			curTestProcessor = new RngSchematronTestProcessor(testProcessorNode.getAttributes().getNamedItem("id").getNodeValue(), 
            					testProcessorLabel, curTests,curFiles, this, testProcessorNode.getAttributes().getNamedItem("uses").getNodeValue(), useRng,useSchematron);
            		} else if (testProcessorNode.getAttributes().getNamedItem("type").getNodeValue().equals("DTD")) {
            			curTestProcessor = new DtdTestProcessor(testProcessorNode.getAttributes().getNamedItem("id").getNodeValue(), 
            					testProcessorLabel, curTests,curFiles, this, testProcessorNode.getAttributes().getNamedItem("uses").getNodeValue());
            		} else if (testProcessorNode.getAttributes().getNamedItem("type").getNodeValue().equals("CUSTOM")) {
            			curTestProcessor = new CustomTestProcessor(testProcessorNode.getAttributes().getNamedItem("id").getNodeValue(), 
            					testProcessorLabel, curTests,curFiles, this, testProcessorNode.getAttributes().getNamedItem("uses").getNodeValue());
            		} else if (testProcessorNode.getAttributes().getNamedItem("type").getNodeValue().equals("XSL")) {
            			curTestProcessor = new XslTestProcessor(testProcessorNode.getAttributes().getNamedItem("id").getNodeValue(), 
            					testProcessorLabel, curTests,curFiles, this, testProcessorNode.getAttributes().getNamedItem("uses").getNodeValue());
            		} else if (testProcessorNode.getAttributes().getNamedItem("type").getNodeValue().equals("MANUAL")) {
            			curTestProcessor = new ManualTestProcessor(testProcessorNode.getAttributes().getNamedItem("id").getNodeValue(), 
            					testProcessorLabel, curTests,curFiles, this, new File(this.manualReportPath+ "/"+ testProcessorNode.getAttributes().getNamedItem("uses").getNodeValue()));
            		}

            		// Add the testprocessor to the TestProcessorlist
            		loadedTestProcessors.put(testProcessorNode.getAttributes().getNamedItem("id").getNodeValue(),curTestProcessor);
            	} else {
            		// System.err.println("curFiles null " + n.getAttributes().getNamedItem("type").getNodeValue());
            	}//if (curFiles != null) 
            	//System.out.println(curTestProcessor.toString());
            } //while ((testProcessorNode = testProcessorNodes.nextNode()) != null)

            // We are done and set the list with testprocessors for this run
            setTestProcessors(loadedTestProcessors);

        }
        return this.getTestProcessors();
    }

    private String getDocTypeVersion(TextFile tf) {    	
		
    	String pid = tf.getDoctypePublicId();
		if(pid!=null) {
			if(pid.equals(ZedConstants.PUBLIC_ID_DTBOOK_Z2005_1)) {
				return "2005-1";
			}
			if(pid.equals(ZedConstants.PUBLIC_ID_DTBOOK_Z2005_2)) {
				return "2005-2";
			}
			if(pid.equals(ZedConstants.PUBLIC_ID_DTBOOK_Z2005_3)) {
				return "2005-3";
			}
		}
    	
		String sid = tf.getDoctypeSystemId();
		if(sid!=null) {
			if(sid.contains("2005-1")) {
				return "2005-1";
			}
			if(sid.contains("2005-2")) {
				return "2005-2";
			}
			if(sid.contains("2005-3")) {
				return "2005-3";
			}
		}
    	
		//TODO could peek and look at @version
    	
		return null;
	}

    private String getDocTypeVersion(SmilFile sf) {    	
		
    	String pid = sf.getDoctypePublicId();
		if(pid!=null) {
			if(pid.equals(ZedConstants.PUBLIC_ID_SMIL_Z2005_1)) {
				return "2005-1";
			}
			if(pid.equals(ZedConstants.PUBLIC_ID_SMIL_Z2005_2)) {
				return "2005-2";
			}
		}
    	
		String sid = sf.getDoctypeSystemId();
		if(sid!=null) {
			if(sid.contains("2005-1")) {
				return "2005-1";
			}
			if(sid.contains("2005-2")) {
				return "2005-2";
			}
		}
    	    	
		return null;
	}
    
	/**
     * A method to check congruence between the loaded testMap and processorMap.
     * Reports missing test implementations, dupes, etc. Need not be called for
     * every run of ZedVal, intended primarily for map development phases
     */

    void printMapsValidityInfo() {
    	if(this.getTestMap()==null){
    		System.err.println("cant validate testmap - its null ");
    		return;
    	}

    	if(this.getProcessorMap()==null){
    		System.err.println("cant validate procmap - its null ");
    		return;
    	}
    	
        try {
            //long start = System.nanoTime();//PK: only in java 1.5
            long start = System.currentTimeMillis();
            NodeList testMapTestList = this.getTestMap().getMapDocument()
                    .getElementsByTagName("test");
            NodeList procMapTestRefList = this.getProcessorMap().getMapDocument()
                    .getElementsByTagName("testRef");

            System.err.println("-- begin map validation -- ");
            System.err.println(testMapTestList.getLength() + " tests in "
                    + this.getTestMap().getName());
            System.err.println(procMapTestRefList.getLength() + " tests in "
                    + this.getProcessorMap().getName());

            // check that each procMap testRef maps to an id in testMap
            boolean procMapHrefsValid = true;
            for (int i = 0; i < procMapTestRefList.getLength(); i++) {
                Node ref = procMapTestRefList.item(i);
                Node testMapTest = this.getTestMap().getMapDocument()
                        .getElementById(
                                ref.getAttributes().getNamedItem("href")
                                        .getNodeValue());
                if (testMapTest == null) {
                    procMapHrefsValid = false;
                    System.err.println("procMap href "
                            + ref.getAttributes().getNamedItem("href")
                                    .getNodeValue()
                            + " does not match a testMap id");
                }
            }

            if (procMapHrefsValid)
                System.err.println("All procMap hrefs match ids in testMap. Congrats.");

            // check that each testMap test has an implementation in procMap
            boolean testMapTestMissingInProcMap = false;
            for (int i = 0; i < testMapTestList.getLength(); i++) {
                Node test = testMapTestList.item(i);
                String testId = test.getAttributes().getNamedItem("id")
                        .getNodeValue();
                    
                Node impl = XPathUtils.selectSingleNode(this
                        .getProcessorMap().getMapDocument()
                        .getDocumentElement(), "//testRef[@href='" + testId
                        + "']");
                    
                if (impl == null) {
                    testMapTestMissingInProcMap = true;
                    System.err.println("the testmap test with id " + testId
                            + " is missing in procMap");
                }
            }
            if (!testMapTestMissingInProcMap)
                System.err.println("All testMap ids match hrefs in procMap. Congrats.");


            boolean dupe = false;
//          // check for dupe value procMap hrefs
            //            for (int i = 0; i < procMapTestRefList.getLength(); i++) {
//                String href = procMapTestRefList.item(i).getAttributes()
//                        .getNamedItem("href").getNodeValue();
//                
//                NodeList dupes = XPathAPI.selectNodeList(this
//                        .getProcessorMap().getMapDocument()
//                        .getDocumentElement(), "//testRef[@href='" + href
//                        + "']");
//                    
//                if (dupes.getLength() != 1) {
//                    dupe = true;
//                    System.err
//                            .println("the procmap testRef with href value "
//                                    + href
//                                    + " occurs more than once in procMap");
//                }
//            }
//            if (!dupe) {
//                System.err.println("No testRef href dupes found in in procMap. Congrats.");
//            }

            // check for dupe testMap ids(although this should be caught by dtd)
            dupe = false;
            for (int i = 0; i < testMapTestList.getLength(); i++) {
                String id = testMapTestList.item(i).getAttributes()
                        .getNamedItem("id").getNodeValue();
                NodeList dupes = XPathUtils.selectNodes(this.getTestMap()
                        .getMapDocument().getDocumentElement(),
                        "//test[@id='" + id + "']");
                    
                if (dupes.getLength() != 1) {
                    dupe = true;
                    System.err.println("the testMap test with id value "
                            + id + " occurs more than once in testMap");
                }
            }
            if (!dupe) {
                System.err.println("No test id dupes found in in testMap. Congrats.");
            }

            //long time = System.nanoTime() - start;//PK: only in java 1.5
            //System.err.println("mapVal took " + time / 1000000000 + " seconds.");
            long time = System.currentTimeMillis() - start;
            System.err.println("mapVal took " + time / 1000000 + " seconds.");
            System.err.println("-- end map information -- ");
        } catch (Exception e) {
            System.err.println("exception in DefaultContext.printMapsInfo");

        }
    }

    public void setVerbosity(String vrbs) throws NumberFormatException {
        int v = java.lang.Integer.parseInt(vrbs);
        if (v < 1 || v > 3) {
            throw new NumberFormatException("value must be 1,2 or 3");
        }
        this.verbosity = v;
    }

    public void setVerbosity(int v) throws NumberFormatException {
        if (v < 1 || v > 3) {
            throw new NumberFormatException("value must be 1,2 or 3");
        }
        this.verbosity = v;
    }

    /**
     * @return The verbosity level
     */
    public int getVerbosity() {
        return this.verbosity;
    }

    public void setTimeTolerance(String tol) throws NumberFormatException {
        SmilClock.setTolerance(Long.parseLong(tol));
    }

    public void setTimeTolerance(long tol) {
        SmilClock.setTolerance(tol);
    }

    public void setStylesheetName(String xsltName) {
        this.stylesheetName = xsltName;
    }

    /**
     * Returns the name of the stylesheet to use in the output report
     * 
     * @return String value of stylesheet name
     */
    public String getStylesheetName() {
        return this.stylesheetName;
    }

    public void setManualReportPath(String manualReportPath) {
        this.manualReportPath = manualReportPath;
        this.manualTests = true;
    }

    public String getManualReportPath() {
        return this.manualReportPath;
    }

    public void setOutputList(String testListName) throws IOException {
        this.testListName = testListName;
        this.outputTestList = true;
        File f = new File(this.testListName);
        try {
            if (!f.createNewFile() || !f.canWrite()) {
                throw new IOException(
                        "could not create or write to outputlist file "
                                + f.getAbsolutePath());
            }
        } catch (Exception e) {
            throw new IOException("exception when creating outputlist file "
                    + f.getAbsolutePath());
        }
    }

    public String[] getCommandLineArguments() {
        return this.commandLineArguments;
    }

    public void setCommandLineArguments(String[] args) {
        this.commandLineArguments = args;
    }

    /**
     * Is the outputList flag set?
     * 
     * @return The outputList flag value
     */
    public boolean getOutputListFlag() {
        return this.outputTestList;
    }

    /**
     * @return The name of the test list file for output
     */
    public String getTestListName() {
        return this.testListName;
    }

    /**
     * @return Returns the debugState.
     */
    boolean getDebugState() {
        return debugState;
    }

    /**
     * @param debugState
     *            The debugState to set.
     */
    void setDebugState(boolean debugState) {
        this.debugState = debugState;
    }

    /**
     * Returns the start time of the application
     * 
     * @return GregorianCalendar that represents start time
     */
    public GregorianCalendar getStartTime() {
        return this.startTime;
    }

}