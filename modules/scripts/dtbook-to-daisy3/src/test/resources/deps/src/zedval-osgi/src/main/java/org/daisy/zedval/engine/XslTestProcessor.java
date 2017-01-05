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

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.FeatureKeys;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * An <code>XslTestProcessor</code> object executes tests via an XSLT
 * stylesheet
 * 
 * @author James Pritchett
 * @author Piotr Kiernicki
 * @author Markus Gylling
 */

public class XslTestProcessor extends ZedTestProcessor implements
        ErrorListener, URIResolver {

    private static String originalXTFProp;
    private static boolean systemXTFPropModified = false;
    private final static String XTF_SYSTEM_PROP_NAME = "javax.xml.transform.TransformerFactory";
    private static final String SAXON_TRANSFORMER_FACTORY = "net.sf.saxon.TransformerFactoryImpl";
    /**
     * @param id
     *            Id for this ZedTestProcessor (from processor map)
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
     * @param sheetName
     *            Full name/path of XSLT stylesheet to execute
     */
	public XslTestProcessor(String id, String l, Map<String,ZedTest> tests,
			Map<String,ZedFile> files, ZedContext c, String sheetName) {
        super(id, l, tests, files, c);
        
        if (c != null && c.getProcessorMap() != null) {
        	URL xslurl = null;
        	
            xslurl = this.getClass().getResource(
                    "/org/daisy/zedval/zedsuite/v" + c.getSpecYear() + "/xslt/" + sheetName);
        
            //if url is null, test user.dir
            //this is for those adventurous people who add their
            //own testProcessors - 
            //may the lord be with them.
            if (xslurl == null) {            	
            	try {
            		xslurl = new URL(System.getProperty("user.dir")+File.separator+sheetName);
                    //File test = new File(xslurl.toURI());// URL#toURI only in jre_1.5; same as new URI (this.toString())
					File test = new File(new URI(xslurl.toString()));
					if(!test.exists()) {
						xslurl=null;
					}
				} catch (URISyntaxException e) {
					xslurl=null;
				} catch (MalformedURLException e) {
					xslurl=null;
				}
            }
            
            //additionally, a full pathname may have been provided by
            //the adventurer.                
            if (xslurl == null) {            	
            	try {
            		xslurl = new URL(sheetName);
					File test = new File(new URI(xslurl.toString()));
					if(!test.exists()) {
						xslurl=null;
					}
				} catch (URISyntaxException e) {
					xslurl=null;
				} catch (MalformedURLException e) {
					xslurl=null;					
				}
            }
            
            //now we tried all reasonable sources to find the resource             
            if (xslurl != null) {
                this.sheetUrl = xslurl;
            } else {
                try {
                    this.getContext().getReporter().addMessage(
                            new TestProcessorErrMsg(
                                    "unable to create URL for xslt sheet or file does not exist"
                                            + sheetName, "en", false, this));
                } catch (ZedReporterException zre) {
                    System.err.println("Exception while writing to reporter: "
                            + zre.getMessage());
                }
            }//if(schemaurl!=null)                                   
        }
                       
        this.transformer = null;
        this.xReader = null;
        this.xReader2 = null;

        // Check stylesheet to be sure it exists and is readable
        if (this.sheetUrl == null) {
            try {
                c.getReporter().addMessage(
                        new TestProcessorErrMsg("Stylesheet " + sheetName
                                + " does not exist", "en", true, this));
            } catch (ZedReporterException zre) {
                System.err.println("Exception while writing to reporter: "
                        + zre.getMessage());
            }
        }else {
            // Create & set up the TransformerFactory, transformer, and XMLReaders
            // we'll need later
        	if(!XslTestProcessor.systemXTFPropModified){
        	    XslTestProcessor.setSystemXTFProperty();
            }
            TransformerFactory tFactory = TransformerFactoryImpl.newInstance();        	

        	if (tFactory instanceof TransformerFactoryImpl) {
                //Needed to get line/col numbers
                tFactory.setAttribute(FeatureKeys.LINE_NUMBERING,
                        Boolean.TRUE); 
            }
            if (tFactory.getFeature(SAXSource.FEATURE)) { // Have to be sure
                // that it will accept
                // a SAXSource ...
                SAXParserFactory pfactory = SAXParserFactory.newInstance();
                pfactory.setNamespaceAware(true); // Very important!
                pfactory.setValidating(false); // Turn off validation.

                // Create/set up the XMLReaders
                try {
                    this.xReader = pfactory.newSAXParser().getXMLReader();
                    this.xReader2 = pfactory.newSAXParser().getXMLReader();
                } catch (Exception e) {
                    try {
                        c.getReporter().addMessage(
                                new TestProcessorErrMsg(
                                        "Exception instantiating XMLReader: "
                                                + e.getMessage(), "en", true,
                                        this));
                    } catch (ZedReporterException zre) {
                        System.err
                                .println("Exception while writing to reporter: "
                                        + zre.getMessage());
                    }
                    this.transformer = null;
                    this.xReader = null;
                    this.xReader2 = null;
                    return;
                }

                // Create the transformer
                try {
                    this.transformer = tFactory.newTransformer(
                            new StreamSource(new InputStreamReader(
                                    this.sheetUrl.openConnection().getInputStream())));
                    this.transformer.setURIResolver(this);
                    
                } catch (Exception e) {
                    try {
                        this.getContext().getReporter().addMessage(
                                new TestProcessorErrMsg(
                                        "Error while creating XSL transformer: "
                                                + e.getMessage(), "en", false,
                                        this));
                    } catch (ZedReporterException zre) {
                        System.err
                                .println("Exception while writing to reporter: "
                                        + zre.getMessage());
                    }
                    this.transformer = null;
                    this.xReader = null;
                    return;
                }

                this.transformer.setErrorListener(this);
            } else {
                try {
                    c
                            .getReporter()
                            .addMessage(
                                    new TestProcessorErrMsg(
                                            "TransformerFactory doesn't support SAXSource (!)",
                                            "en", true, this));
                } catch (ZedReporterException zre) {
                    System.err.println("Exception while writing to reporter: "
                            + zre.getMessage());
                }
                this.transformer = null;
                this.xReader = null;
            }
        }
    }

    private static void setSystemXTFProperty() {
        if(!XslTestProcessor.systemXTFPropModified ){
            //save the original TransformerFactory property 
            //to be able to put it back when the tests are done
            XslTestProcessor.originalXTFProp = System.getProperty(XslTestProcessor.XTF_SYSTEM_PROP_NAME);
            System.setProperty(XslTestProcessor.XTF_SYSTEM_PROP_NAME,XslTestProcessor.SAXON_TRANSFORMER_FACTORY);
            XslTestProcessor.systemXTFPropModified = true;
        }
    }
    
    private static void resetSystemXTFProperty() {
        if(XslTestProcessor.systemXTFPropModified){
            System.setProperty(XslTestProcessor.XTF_SYSTEM_PROP_NAME,XslTestProcessor.originalXTFProp);
            XslTestProcessor.systemXTFPropModified = false;
        }
    }
    

    /**
     * Runs all files through the stylesheet
     */
	public boolean performTests() {
        boolean retVal = false; // Return value
        Iterator<ZedFile> i;
        SAXSource source;
        DOMResult domResults;
        Document results;
        ZedReporter r;
        PackageFile p;
        NodeList nl;
        int j;
        ZedTest failedTest;
        Element failNode;
        Node detailTextNode;
        long line = -1;
        long column = -1;

        if(!XslTestProcessor.systemXTFPropModified){
            XslTestProcessor.setSystemXTFProperty();
        }
        /*
         * If we have no Transformer, no XMLReader, no files, or no tests, then
         * bail now
         */
        if (this.transformer == null || this.xReader == null
                || this.getFilesTested() == null
                || this.getTestsImplemented() == null){
            XslTestProcessor.resetSystemXTFProperty();
            return retVal;
        }

        /*
         * Iterate over the files collection and evaluate the test(s) for each
         */
        for (i = this.getFilesTested().values().iterator(); i.hasNext();) {
            this.curFile = i.next(); // Save this in case tester
                                                // calls
            // an error handler below

            // First, check to be sure we can actually run the test on this file
            r = this.getContext().getReporter();
            if (this.curFile.exists() == false) {
                try {
                    r.addMessage(new TestProcessorErrMsg(
                            "Cannot run testProcessor [" + this.getLabel()
                                    + "] on file " + this.curFile.getName()
                                    + ": File does not exist", "en", false,
                            this.curFile, this));
                } catch (ZedReporterException zre) {
                    System.err.println("Exception while writing to reporter: "
                            + zre.getMessage());
                }
            } else if (this.curFile.canRead() == false) {
                try {
                    r.addMessage(new TestProcessorErrMsg(
                            "Cannot run testProcessor [" + this.getLabel()
                                    + "] on file " + this.curFile.getName()
                                    + ": File is not readable", "en", false,
                            this.curFile, this));
                } catch (ZedReporterException zre) {
                    System.err.println("Exception while writing to reporter: "
                            + zre.getMessage());
                }
            } else if ((this.curFile instanceof XmlFile) == false) { // Must
                                                                        // be
                // an
                // XmlFile!
                try {
                    r.addMessage(new TestProcessorErrMsg(
                            "Cannot run testProcessor [" + this.getLabel()
                                    + "] on file " + this.curFile.getName()
                                    + ": File is not an XML file", "en", false,
                            this.curFile, this));
                } catch (ZedReporterException zre) {
                    System.err.println("Exception while writing to reporter: "
                            + zre.getMessage());
                }
            } else if (((XmlFile) (this.curFile)).isWellFormed() == false) {
                try {
                    r.addMessage(new TestProcessorErrMsg(
                            "Cannot run testProcessor [" + this.getLabel()
                                    + "] on XML file " + this.curFile.getName()
                                    + ": File is not well-formed", "en", false,
                            this.curFile, this));
                } catch (ZedReporterException zre) {
                    System.err.println("Exception while writing to reporter: "
                            + zre.getMessage());
                }
            }

            // If file is testable, run the stylesheet over it and get the
            // results
            else {
                // The tested file can act as entity resolver/error handler for
                // the XMLReaders
                this.xReader.setEntityResolver((XmlFile) curFile); // This one
                // for the
                // source doc
                this.xReader.setErrorHandler((XmlFile) curFile);
                this.xReader2.setEntityResolver((XmlFile) curFile); // This one
                // for
                // document()
                // calls
                this.xReader2.setErrorHandler((XmlFile) curFile);

                // Specify a SAXSource that parses our input file using our
                // XMLReader
                try {
                    source = new SAXSource(this.xReader, new InputSource(
                            "file:///" + this.curFile.getCanonicalPath()));
                } catch (IOException e) {
                    System.err
                            .println("IOException while specifying a SAX source");
                    XslTestProcessor.resetSystemXTFProperty();
                    return retVal;
                }

                // Create an empty DOMResult object for the output.
                domResults = new DOMResult();

                // Pass parameters to stylesheet
                p = this.getContext().getPackageFile();
                try {
                    transformer.setParameter("opfPath", p.getParentFile()
                            .getCanonicalPath()
                            + File.separator);
                } catch (Exception e) {
                    try {
                        r.addMessage(new TestProcessorErrMsg(
                                "Exception while trying to pass package file path to transform: "
                                        + e.getMessage(), "en", false,
                                this.curFile, this));
                    } catch (ZedReporterException zre) {
                        System.err
                                .println("Exception while writing to reporter: "
                                        + zre.getMessage());
                    }
                }
                transformer.setParameter("inputName", this.curFile.getName());
                transformer.setParameter("opfName", p.getName());
                if (p.getNcx() != null) {
                    transformer.setParameter("ncxName", p.getNcx().getName());
                }

                // Now you can do the transform
                try {
                    transformer.transform(source, domResults);
                } catch (Exception e) {
                    try {
                        r.addMessage(new TestProcessorErrMsg(
                                "Exception while running transform: "
                                        + e.getMessage(), "en", false,
                                this.curFile, this));
                    } catch (ZedReporterException zre) {
                        System.err
                                .println("Exception while writing to reporter: "
                                        + zre.getMessage());
                    }
                    continue; // Do the next file
                }
                retVal = true;
                results = (Document) domResults.getNode();

                // Query the resulting DOM document and format messages for any
                // testFailure nodes found
                // QUESTION: What do we do with @badRef on <detail>?
                nl = results.getElementsByTagName("testFailure");
                for (j = 0; j < nl.getLength(); j++) {
                    failNode = (Element) nl.item(j); // <testFailure
                    // testRef="testId">
                    failedTest = (ZedTest) this.getTestsImplemented().get(
                            failNode.getAttribute("testRef"));
                    if (failedTest != null) {
                        failNode.normalize(); // Make sure detail text is all
                                                // in
                        // one text node
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
                            r.addMessage(new FailureMessage(detailTextNode
                                    .getNodeValue().trim(), "en", line, column,
                                    failedTest, this.curFile, this));
                        } catch (ZedReporterException zre) {
                            System.err
                                    .println("Exception while writing to reporter: "
                                            + zre.getMessage());
                        }
                    }
                }
            }
        }
        //reset the system property
        XslTestProcessor.resetSystemXTFProperty();
        return retVal;
    }

//    /**
//     * Returns the XSL stylesheet used by this processor
//     * 
//     * @return File objecgt for XSL stylesheet
//     */
//    public File getSheetFile() {
//        return this.sheetFile;
//    }

    /**
     * Returns the XSL stylesheet used by this processor
     * 
     * @return File objecgt for XSL stylesheet
     */
    public URL getSheetUrl() {
        return this.sheetUrl;
    }
    
    /**
     * Receives notification of a recoverable (probably validation) error
     * 
     * @param e
     *            The exception that represents the error
     */
    public void error(TransformerException e) {
        try {
            this.getContext().getReporter().addMessage(
                    new TestProcessorErrMsg("Error while running transform: "
                            + e.getMessage(), "en", false, this.curFile, this));
        } catch (ZedReporterException zre) {
            System.err.println("Exception while writing to reporter: "
                    + zre.getMessage());
        }
    }

    /**
     * Receives notification of a non-recoverable (probably well-formedness)
     * error
     * 
     * @param e
     *            The exception that represents the error
     */
    public void fatalError(TransformerException e) {
        try {
            this.getContext().getReporter().addMessage(
                    new TestProcessorErrMsg("Unable to run transform: "
                            + e.getMessage(), "en", false, this.curFile, this));
        } catch (ZedReporterException zre) {
            System.err.println("Exception while writing to reporter: "
                    + zre.getMessage());
        }
    }

    /**
     * Receives notification of a warning
     * 
     * @param e
     *            The exception that represents the warning
     */
    public void warning(TransformerException e) {
        // Do we do anything here?
    }

    /**
     * Creates a SAXSource for any document pulled in through a document() call.
     * This version will use our standard entity resolver mechanism to use local
     * copies of all known DTDs.
     * 
     * @param href
     *            The URI to resolve (relative or absolute)
     * @param base
     *            The base URI in effect at the time href was encountered
     * @return A SAXSource built from our local XMLReader that uses an XmlFile
     *         as an EntityResolver
     */

	public Source resolve(String href, String base) throws TransformerException {
        SAXSource _s = null;
        /*
         * (01/03/2005, PK) bug #1154318 Avoid creating a new SAXSource object
         * for the same href. Otherwise when document() is called repeatedly
         * with same input parameter (href), it returns a result document only
         * at the first call. (That was the actual cause of bug #958191.)
         * Collect the SAXSource objects instead
         */
        if (docSAXSources.containsKey(href)) {
            _s = (SAXSource) docSAXSources.get(href);

        } else {
            _s = new SAXSource(this.xReader2, new InputSource(href));
            docSAXSources.put(href, _s);
        }
        return _s;
    }

    public String toString() {
        if (this.sheetUrl != null) {
            return super.toString() + "\t[uses=" + this.sheetUrl
                    + "]\n";
        } 
            return super.toString() + "\t[uses=null]\n";        
    }

    //private File sheetFile;
    
    private URL sheetUrl;

    private XMLReader xReader; // This one is for the source document

    private XMLReader xReader2; // This one is for any document() calls within

    // the stylesheet
	private Map<String,SAXSource> docSAXSources = new HashMap<String, SAXSource>(); // collection of SAXSource

    // objects created when
    // document() is called
    private Transformer transformer;

    private ZedFile curFile;
}
