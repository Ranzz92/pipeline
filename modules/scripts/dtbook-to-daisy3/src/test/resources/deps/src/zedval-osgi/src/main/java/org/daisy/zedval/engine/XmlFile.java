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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.daisy.util.xml.catalog.CatalogEntityResolver;
import org.daisy.util.xml.catalog.CatalogExceptionNotRecoverable;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;


/**
 * An <code>XmlFile</code> object represents a single XML document file
 * <p>
 * This class includes default, do-nothing methods to implement the SAX
 * <code>ContentHandler</code> interface (The appropriate ones will be
 * overridden by the subclasses).
 * </p>
 * <p>
 * This class also implements the SAX <code>EntityResolver</code> interface as
 * a way of substituting local DTDs for known, internet-based system
 * identifiers. It uses the org.daisy.util.xml.catalog.CatalogEntityResolver for
 * this purpose.
 * </p>
 * <p>
 * Each XMLfile is validated on parse; any validation errors are caught and
 * stored in local hashsets for later retrieval by testprocessors.
 * </p>
 * 
 * @author James Pritchett
 * @author Markus Gylling
 */

abstract public class XmlFile extends ManifestFile implements ContentHandler,
        ErrorHandler, EntityResolver, LexicalHandler, DTDHandler, Referring {

	private static final long serialVersionUID = -2270631004605020987L;
	
	private boolean isWellFormed = true; // default to true since SAX only reports problems
    private boolean isValid = true; // default to true since SAX only reports problems
    private boolean isValidated = false; // == if parsed with validation on
    private boolean isParsed = false;
    private String doctypeName;
    private String doctypePublicId;
    private String doctypeSystemId;
    String rootElementLocalName = null;
    String rootElementNsURI = null;
    static SAXParserFactory saxFactory;
    private ZedFileInitializationException zedFileInitEx = new ZedFileInitializationException();
	private List<SAXParseException> validationErrors = new LinkedList<SAXParseException>();
    private List<SAXParseException> validationFatalErrors = new LinkedList<SAXParseException>();
    private List<SAXParseException> validationWarnings = new LinkedList<SAXParseException>();
    private HashSet<String> nsURIs = new HashSet<String>(); //collect all nsURIs
    
    /**
     * records the sax startelement information into a list of XmlFileElement 
     */
    protected List<XmlFileElement> xmlFileElementList = new ArrayList<XmlFileElement>(ZedConstants.INITIAL_CAPACITY_COLLECTION); 
    
    /**
     * provides hashed pointers by id into to the xmlFileElementList  
     */
    protected Map<String,XmlFileElement> hashIdToXmlFileElementMap = new HashMap<String, XmlFileElement>(ZedConstants.INITIAL_CAPACITY_COLLECTION);    
    

    /**
     * @param id
     *            id from package file manifest
     * @param fullPath
     *            File path
     * @param mimeType
     *            File MIME type as given in package manifest
     */
    public XmlFile(String fullPath, String id, String mimeType,String manifestURI) {
        super(fullPath, id, mimeType,manifestURI);
    }

    /**
     * Does all basic integrity tests
     * 
     * @throws ZedFileInitializationException
     */
    public void initialize() throws ZedFileInitializationException {
        SAXParser saxParser = null;
        FileInputStream fis = null;
        InputSource is = null;

        /*
         * If the document exists and is readable, then we parse it to see if
         * it's well-formed and valid. NOTE: Validity reporting is done via
         * DtdTestProcessor, but we explicitly set the parser to be validating
         * here, and collect any SAXParseExceptions in local hashsets, for the
         * DtdTestProcessor to collect later.
         */
        if (this.exists() && this.canRead()) {
            // Create the parser
            try {
                // use standard JAXP to instantiate the parser
                if (saxFactory == null) {
                    saxFactory = SAXParserFactory.newInstance();
                    saxFactory.setValidating(true);
                    saxFactory.setNamespaceAware(true);
                    if (saxFactory.getClass().getName().indexOf(
                            "xerces")<0) {
                        throw new ZedFileInitializationException(
                                "WARNING! Apache Xerces seems not to be the SAX parser used. ZedVal report not guaranteed to be accurate.");
                    }

                }
//                long start = System.nanoTime();                
                saxParser = saxFactory.newSAXParser();
//                long end = System.nanoTime();
//                System.out.println("newSAXParser millis: "+(double)(end-start)/1000000);
                saxParser.getXMLReader().setContentHandler(this);
                saxParser.getXMLReader().setErrorHandler(this);
                saxParser.getXMLReader().setEntityResolver(this);
                saxParser.getXMLReader().setDTDHandler(this);
                saxParser.setProperty("http://xml.org/sax/properties/lexical-handler", this);
            } catch (Exception e) {
                this.isWellFormed = false;
                throw new ZedFileInitializationException(
                        "WARNING!  Unrecoverable parse error in "
                                + this.getName()
                                + ".  Could not create SAXParser: "
                                + e.getMessage());
            }

            try {
                fis = new FileInputStream(this);
                is = new InputSource(fis);
                saxParser.getXMLReader().parse(is);
            } catch (IOException e) {
                this.isWellFormed = false;
                throw new ZedFileInitializationException(
                        "Unrecoverable parse error in "
                                + this.getName()
                                + ". IOException when parsing:  "
                                + e.getMessage());
            } catch (SAXException e) {
                this.isWellFormed = false;
                throw new ZedFileInitializationException(
                        "Unrecoverable parse error in "
                                + this.getName()
                                + ". SAXException when parsing:  "
                                + e.getMessage());
            } finally {
                this.isParsed = true;
                if (saxFactory.isValidating())
                    this.isValidated = true;
                try {
                    fis.close();
                } catch (Exception e) {
                } // Ignore any exceptions here
            }
        } else {
            this.isWellFormed = false; // If it doesn't exist, it isn't well-formed
        }
        
        if (this.zedFileInitEx.getErrors().size() > 0) {
            throw this.zedFileInitEx;
        }
    }

    /**
     * Is this file well-formed XML?
     * 
     * @return <code>true</code> if wellformed, <code>false</code> if
     *         malformed OR not yet parsed
     */
    public boolean isWellFormed() {
        if (this.isParsed) {
            return this.isWellFormed;
        }
        return false;
    }

    /**
     * Is this file valid to the DTD?
     * 
     * @return <code>true</code> if valid, <code>false</code> if invalid OR
     *         not yet parsed OR malformed
     * 
     */
    public boolean isValid() {
        if (this.isParsed && this.isWellFormed) {
            return this.isValid;
        }
        return false;
    }

    /**
     * Has this file been parsed?
     * 
     * @return <code>true</code> if has been parsed, <code>false</code>
     *         otherwise
     * 
     */
    public boolean isParsed() {
        return this.isParsed;
    }

    /**
     * Has this file been parsed with validation on?
     * 
     * @return <code>true</code> if has been parsed with validation on, false
     *         otherwise
     * 
     */
    public boolean isValidated() {
        return this.isValidated;
    }

    /**
     * @return an empty or non-empty collection of SAXParseExceptions caught via
     *         the SAX Errhandler <code>error</code> method
     * @see #isValid()
     */
    public List<SAXParseException> getValidationErrors() {
        return this.validationErrors;
    }

    /**
     * @return an empty or non-empty collection of SAXParseExceptions caught via
     *         the SAX Errhandler <code>fatalError</code> method
     * @see #isWellFormed()
     */
    public List<SAXParseException> getValidationFatalErrors() {
        return this.validationFatalErrors;
    }

    /**
     * @return an empty or non-empty collection of SAXParseExceptions caught via
     *         the SAX Errhandler <code>warning</code> method
     */

    public List<SAXParseException> getValidationWarnings() {
        return this.validationWarnings;
    }

    /**
     * @return an empty or non-empty collection of SAXParseExceptions caught via
     *         the SAX Errhandler <code>error</code>, <code>fatalError</code>
     *         and <code>warning</code> methods
     */
    public List<SAXParseException> getAllValidationErrors() {
        List<SAXParseException> all = new LinkedList<SAXParseException>();
        all.addAll(this.validationErrors);
        all.addAll(this.validationFatalErrors);
        all.addAll(this.validationWarnings);
        return all;
    }

    /**
     * Returns the name given in the DOCTYPE (if any)
     * 
     * @return DOCTYPE name string
     */
    public String getDoctypeName() {
        return this.doctypeName;
    }

    /**
     * Returns the public id given in the DOCTYPE (if any)
     * 
     * @return DOCTYPE public id string
     */
    public String getDoctypePublicId() {
        return this.doctypePublicId;
    }

    /**
     * Returns the system id given in the DOCTYPE (if any)
     * 
     * @return DOCTYPE system id string
     */
    public String getDoctypeSystemId() {
        return this.doctypeSystemId;
    }

    // Methods to implement SAX ContentHandler interface; all are empty, to be
    // overridden by subclasses
    public void characters(char[] ch, int start, int length) {
    }

    public void endDocument() {
    }

    public void endElement(String namespaceURI, String localName, String qName) {
    }

    public void endPrefixMapping(String prefix) {
    }

    public void ignorableWhitespace(char[] ch, int start, int length) {
    }

    public void processingInstruction(String target, String data) {
    }

    public void setDocumentLocator(Locator locator) {
    }

    public void skippedEntity(String name) {
    }

    public void startDocument() {
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
    	//this method in this class only does generic (for all XmlFile subclasses) 
    	//collects to avoid code duplication
    	//therefore, all XmlFile subclasses must run a super

    	//check root properties
        if (this.rootElementNsURI == null && this.rootElementLocalName == null) {
            this.rootElementNsURI = namespaceURI;
        }
        if (this.rootElementLocalName == null) {
            this.rootElementLocalName = localName;
        }        
        
        nsURIs.add(namespaceURI);
        for (int i = 0; i < atts.getLength(); i++) {
        	String attNsUri = atts.getURI(i);
        	if(attNsUri!=null && attNsUri.length()>0) {
        		nsURIs.add(attNsUri);	
        	}			
		}
    }

    public void startPrefixMapping(String prefix, String uri) {
    	
    }

    // Methods to implement SAX extension LexicalHandler interface; startDTD()
    // is the only one that does anything
    public void startDTD(String name, String publicId, String systemId) {

        this.doctypeName = name;
        this.doctypePublicId = publicId;
        this.doctypeSystemId = systemId;

        // As a courtesy, we handle references to the OeB 1.0 package DTD.
        // However, we have to issue a warning here about this, too.
        if ((publicId != null && publicId
                .equals("+//ISBN 0-9673008-1-9//DTD OEB 1.0 Package//EN"))
                || (systemId != null && systemId
                        .equals("http://openebook.org/dtds/oeb-1.0/oebpkg1.dtd"))) {
            this
                    .getZedFileInitEx()
                    .addError(
                            "WARNING!  Package file "
                                    + this.getName()
                                    + " uses the obsolete OeB 1.0 DTD.\nWill process using 1.0.1 DTD, but parse errors may occur.");
        }
    }

    public void comment(char[] ch, int start, int length) {
    }

    public void endCDATA() {
    }

    public void endDTD() {
    }

    public void endEntity(java.lang.String name) {
    }

    public void startCDATA() {
    }

    public void startEntity(java.lang.String name) {
    }

    public void notationDecl(String name, String publicId, String systemId)
            throws SAXException {
    }

    public void unparsedEntityDecl(String name, String publicId,
            String systemId, String notationName) throws SAXException {
    }

    /**
     * Handles errors thrown by SAX parser (meaning document is not valid
     * internally)
     * 
     * @param e
     *            The SAX exception thrown
     */
    public void error(SAXParseException e) {
        this.validationErrors.add(e);
        this.isValid = false;
    }

    /**
     * Handles warnings thrown by SAX parser
     * 
     * @param e
     *            The SAX exception thrown
     */
    public void warning(SAXParseException e) {
        this.validationWarnings.add(e);
    }

    /**
     * Handles fatal errors thrown by SAX parser (meaning document is not
     * well-formed)
     * 
     * @param e
     *            The SAX exception thrown
     */
    public void fatalError(SAXParseException e) {
        this.validationFatalErrors.add(e);
        this.isValid = false;
        this.isWellFormed = false;
    }

    public InputSource resolveEntity(String publicId, String systemId) {
        // be a little roundtripping to allow verbosity if entity not supported
        // in catalog
        InputSource retSource = null;
        try {
            retSource = CatalogEntityResolver.getInstance().resolveEntity(
                    publicId, systemId);
            if (retSource == null) {
                // the entity was not supported
                this.getZedFileInitEx().addError(
                        "WARNING: CatalogEntityResolver does not recognize entity: "
                                + publicId + ", " + systemId + " (file="
                                + this.getName() + "). Expect repercussions.");
            }
        } catch (CatalogExceptionNotRecoverable e) {
            this.getZedFileInitEx().addError(
                    "CatalogEntityResolver nonrecoverable error: (file="
                            + this.getName() + ") " + e.getMessage());
        } catch (IOException e) {
            this.getZedFileInitEx().addError(
                    "CatalogEntityResolver IO error: (file=" + this.getName()
                            + ") " + e.getMessage());
        }
        // return even if its null (this will cause the parser to try to resolve
        // IDs given in instance)
        return retSource;
    }

    // toString() here for debugging use
    public String toString() {
        return super.toString() + "\n" + "\t[wellFormed=" + this.isWellFormed
                + "]";
    }

    /**
     * @return Returns the errors.
     */
    protected ZedFileInitializationException getZedFileInitEx() {
        return this.zedFileInitEx;
    }

    /**
     * @return Returns the root element localname, null if not set
     */
    public String getRootElementLocalName() {
        return rootElementLocalName;
    }

    /**
     * @return Returns the root element namespace uri, null if not set
     */
    public String getRootElementNsURI() {
        return rootElementNsURI;
    }
    
    /**
     * Creates a XmlFileElement object
     * based on the data in a SAX.startelement call.
     * Adds it to the XmlFile.xmlFileElementList ArrayList.
     * If an an id attribute is present
     * adds the object to a HashSet which is used for performance
     * purposes only. Subclasses may implement the same performance increasing method
     * by capturing the return value of this method.
     */
    protected XmlFileElement addXmlFileElement(String namespaceURI, String localName, String qName, Attributes atts) {
    	    	
    	XmlFileElement xfe = new XmlFileElement(qName,localName,namespaceURI,atts);
    	xmlFileElementList.add(xfe);
    	for (int i = 0; i < atts.getLength(); i++) {
    		if(atts.getLocalName(i)=="id") {
    			hashIdToXmlFileElementMap.put(atts.getValue(i),xfe);   
    		}	
    	}    	
    	return xfe;   	    	
    }
    
    /**
     * @param idValue a string that may or may not be the value of an attribute named id in this XmlFile
     * @return true if this XmlFile has the value on an attribute named id, false otherwise
     */
    public boolean hasIDValue(String idValue){
    	return this.hashIdToXmlFileElementMap.containsKey(idValue);
    }
    
    /**
     * @param idValue a string that may or may not be the value of an attribute named id in this XmlFile
     * @return an XmlFileElement that matches the inparam id value, null if no match
     */
    public XmlFileElement getXmlFileElementById(String idValue){    	
    	return (XmlFileElement)this.hashIdToXmlFileElementMap.get(idValue);
    }
    
    /**
     * @return a Collection&lt;XmlFileElement&gt; representing a serialized startelement stream of this XmlFile
     */
    public List<XmlFileElement> getXmlFileElements(){    	
    	return this.xmlFileElementList;
    }
    
    /**
     * @return a set of used Namespace URIs in this XmlFile. Namespace URIs that are declared but not used or not included. 
     */
    public Set<String> getNamespaceURIs() {
    	return nsURIs;
    }
    
}
