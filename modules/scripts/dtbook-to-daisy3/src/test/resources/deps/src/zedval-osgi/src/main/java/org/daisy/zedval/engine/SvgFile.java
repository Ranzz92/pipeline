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
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.daisy.util.xml.catalog.CatalogEntityResolver;
import org.daisy.util.xml.catalog.CatalogExceptionNotRecoverable;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * An <code>SvgFile</code> object represents a single DTB SVG image file
 * 
 * @author James Pritchett
 * @author Markus Gylling
 */

public class SvgFile extends ImageFile implements ContentHandler, ErrorHandler, EntityResolver {

    private boolean inDocument;
    private boolean isWellFormed = true; // default to true since SAX only reports problems
    private boolean isValid = true; // default to true since SAX only reports problems
    private boolean isValidated = false; // == if parsed with validation on
    private boolean isParsed = false;    
	private List<SAXParseException> validationErrors = new LinkedList<SAXParseException>();
    private List<SAXParseException> validationFatalErrors = new LinkedList<SAXParseException>(); 
    private List<SAXParseException> validationWarnings = new LinkedList<SAXParseException>();
    private ZedFileInitializationException zedFileInitEx = new ZedFileInitializationException();

    /**
     * @param fullPath
     *            File path
     * @param id
     *            id from package file manifest
     * @param mimeType
     *            File MIME type as given in package manifest
     */
    public SvgFile(String fullPath, String id, String mimeType, String manifestURI) {
        super(fullPath, id, mimeType, manifestURI);
    }

    /**
     * Does all basic integrity tests (existence, readability, format) and sets
     * properties
     * 
     * @throws ZedFileInitializationException
     */
    public void initialize() throws ZedFileInitializationException {
        SAXParserFactory factory;
        SAXParser saxParser;
        FileInputStream fis = null;
        InputSource is = null;

        this.matchesFormat = false; // Guilty until proven innocent
        if (this.exists() && this.canRead()) {
            try {
                factory = SAXParserFactory.newInstance();
                factory.setValidating(true);
                factory.setNamespaceAware(true);
                saxParser = factory.newSAXParser();
                saxParser.getXMLReader().setContentHandler(this);
                saxParser.getXMLReader().setErrorHandler(this);
                saxParser.getXMLReader().setEntityResolver(this);

                this.inDocument = false; // Signal to startElement ...

                try {
                    fis = new FileInputStream(this);
                    is = new InputSource(fis);
                    saxParser.getXMLReader().parse(is);
                } catch (IOException ioe) {
                    this.isWellFormed = false;
                    throw new ZedFileInitializationException(
                            "WARNING!  Unrecoverable parse error in "
                                    + this.getName()
                                    + ". IOException when parsing:  "
                                    + ioe.getMessage());
                } catch (SAXException se) {
                    this.isWellFormed = false;
                    throw new ZedFileInitializationException(
                            "WARNING!  Unrecoverable parse error in "
                                    + this.getName()
                                    + ". SAXException when parsing:  "
                                    + se.getMessage());
                } finally {
                    this.isParsed = true;
                    if (factory.isValidating())
                        this.isValidated = true;
                    try {
                        fis.close();
                    } catch (Exception e) {

                    } // Ignore any exceptions here
                }

            } catch (ParserConfigurationException e) {
                this.isWellFormed = false;
                throw new ZedFileInitializationException(
                        "WARNING!  Unrecoverable parse error in "
                                + this.getName()
                                + ".  Could not create XMLReader: "
                                + e.getMessage());
            } catch (SAXException e) {
                this.isWellFormed = false;
                throw new ZedFileInitializationException(
                        "WARNING!  Unrecoverable parse error in "
                                + this.getName()
                                + ".  Could not create XMLReader: "
                                + e.getMessage());
            } catch (Exception e) {
                this.isWellFormed = false;
                throw new ZedFileInitializationException(
                        "WARNING!  Unrecoverable error in " + this.getName()
                                + ".  Could not initialize properly: "
                                + e.getMessage());
            }
        } else {
            this.isWellFormed = false;
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

    public void startElement(String namespaceURI, String localName, String qName,
    		Attributes atts) {
        if (this.inDocument == false && localName.equals("svg")) {
            this.matchesFormat = true;
        }
        this.inDocument = true;
    }

    // Methods to implement SAX ErrorHandler interface
    public void error(SAXParseException e) {
        this.validationErrors.add(e);
        this.isValid = false;
    }

    public void warning(SAXParseException e) {
        this.validationWarnings.add(e);
    }

    public void fatalError(SAXParseException e) {
        this.matchesFormat = false;
        this.validationFatalErrors.add(e);
        this.isValid = false;
        this.isWellFormed = false;
    }

	public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
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
     * @return Returns the errors.
     */
    protected ZedFileInitializationException getZedFileInitEx() {
        return this.zedFileInitEx;
    }

    // unused methods
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

    public void startPrefixMapping(String prefix, String uri) {
    }
    
    private static final long serialVersionUID = -3298547905661078723L;
}
