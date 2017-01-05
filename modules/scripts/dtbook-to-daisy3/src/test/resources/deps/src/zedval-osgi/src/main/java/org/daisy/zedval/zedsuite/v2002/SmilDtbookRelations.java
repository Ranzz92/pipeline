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

// SmilDtbookRelations.java
/* Versions:
 0.3.0 (02/06/2003)
 1.0.0 (20/07/2003)
 - Tests are skipped if package file shows no dtbook files present (bug #768670)
 30 Jun 2005 (JP)
 - Changed from implementation to extension of ZedCustomTest
 - performTest no longer returns value
 - Removed ErrorListener
 - Discovers incorrect parameter type through CastClass exception
 - Removed all DOM calls (now handled in ZedCustomTest)
 */
package org.daisy.zedval.zedsuite.v2002;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.SAXParserFactory;

import org.daisy.zedval.engine.PackageFile;
import org.daisy.zedval.engine.SmilFile;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Performs SMIL-dtbook relations checks:
 * <dl>
 * <dt>smil_textSrcResolves</dt>
 * <dd>text element src attribute URI resolves</dd>
 * <dt>dtbook_SmilRef</dt>
 * <dd>Each dtbook element referenced from smil contains a smilRef attribute
 * pointing to the smil element that references it</dd>
 * </dl>
 * 
 * @author James Pritchett
 */

public class SmilDtbookRelations extends ZedCustomTest implements ContentHandler {

	/**
	 * Performs the relational tests on a SMIL file
	 * 
	 * @param f
	 *            A SmilFile instance
	 */
	public void performTest(ZedFile f) throws ZedCustomTestException {
		SmilFile s;
		PackageFile p;
		XMLReader parser = null;
		FileInputStream fis = null;
		InputSource is = null;
		Map<String,TextElement> textElems;

		this.resultDoc = null;		// Clear out the results document

		// If f isn't a SmilFile, this is a fatal error
		try {
			s = (SmilFile) f; // Make our life simpler by removing need for casts
        } catch (ClassCastException castEx) {
            throw new ZedCustomTestException(castEx.getMessage());
        }

		// Look at the package and check to see if there are any dtbook files to
		// test
		p = s.getPackage();
		if (p.getTextFiles() == null || p.getTextFiles().isEmpty()) {
			return; // Bail now and save us all some time
		}
		textFiles = new HashMap<String, Map<String,TextElement>>();

		// Set up the parser

		// Create the parser
		try {
			//TODO move to propoer JAXP?
			//parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
			parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			if(parser.getClass().getName().indexOf("xerces")<0) {
				System.err.println("Warning: SAXParser is not Xerces: " + parser.getClass().getName());	
			}			
			parser.setFeature("http://xml.org/sax/features/validation", false);
		} catch (Exception e) {
			throw new ZedCustomTestException("Unrecoverable parse error in SmilDtbookRelations.  Could not create XMLReader: "
					+ e.getMessage());
		}

		// Set us up as the content handler; SMIL file is entity resolver
		parser.setContentHandler(this);
		parser.setEntityResolver(s);
		parser.setErrorHandler(s);

		// Parse the SMIL file and collect the data on text references
		this.textReferences = new HashMap<String, TextRef>();
		try {
			this.fileType = "SMIL";
			this.parsedFile = s;
			fis = new FileInputStream(s);
			is = new InputSource(fis);
			parser.parse(is);
		} catch (IOException e) {
			throw new ZedCustomTestException("Unrecoverable parse error in SmilDtbookRelations. IOException when parsing:  "
					+ e.getMessage());
		} catch (SAXException e) {
			// Eat up all other SAX exceptions
		}finally{
			try {
				fis.close();
			} catch (Exception e) {
			} // Ignore any exceptions here
		}

		// Parse each referenced text file and collect element data
		this.fileType = "TEXT";
		for (Iterator<String> i = this.textFiles.keySet().iterator(); i.hasNext();) {
			try {
				this.parsedFile = new File((String) i.next());
				fis = new FileInputStream(parsedFile);
				is = new InputSource(fis);
				parser.parse(is);
			} catch (IOException e) {
				throw new ZedCustomTestException("Unrecoverable parse error in SmilDtbookRelations. IOException when parsing:  "
						+ e.getMessage());
			} catch (SAXException e) {
				// Eat up all other SAX exceptions
			}finally{
				try {
					fis.close();
				} catch (Exception e) {
				} // Ignore any exceptions here
			}
		}

		// Test each text reference for resolution and for a smilRef on the
		// other end
		for (TextRef aTextRef : this.textReferences.values()) {
			textElems = this.textFiles.get(aTextRef.filePath);
			if (textElems == null) {
				super.addTestFailure("smil_textSrcResolves","File named in URI does not exist: "
						+ aTextRef.myURI,null,aTextRef.lineNum.toString());
			} else if (textElems.get(aTextRef.myURI.getFragment()) == null) {
				super.addTestFailure("smil_textSrcResolves","Fragment identifier named in URI does not exist in document: "
						+ aTextRef.myURI,null,aTextRef.lineNum.toString());
			} else if (((TextElement) (textElems.get(aTextRef.myURI.getFragment()))).smilRef == null) {
				super.addTestFailure("dtbook_SmilRef","No smilref attribute on element "
						+ aTextRef.myURI,null,aTextRef.lineNum.toString());
			}
		}
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
		this.myLocator = locator;
	}
	public void skippedEntity(String name) {
	}
	public void startDocument() {
	}
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) {
		File myFile;
		TextRef myRef;
		TextElement myElem;

		// SMIL file text element -- work with the src attribute URI
//		 jpritchett@rfbd.org, 23 Aug 2006:  Added qName to test
		if (this.fileType.equals("SMIL") && 
				(localName.equals("text") || qName.equals("text"))) {
			myRef = new TextRef();
			myRef.id = atts.getValue("id");
			if (myRef.id == null) { // Not all SMIL text elements have ids
				myRef.id = "zvsdtb" + myLocator.getLineNumber()
						+ myLocator.getColumnNumber();
			}
			try {
				myRef.myURI = new URI(atts.getValue("src"));
				myFile = new File(parsedFile.getParentFile().getCanonicalPath()
						+ File.separator + myRef.myURI.getPath());
				myRef.filePath = myFile.getCanonicalPath();
				myRef.lineNum = new Integer(myLocator.getLineNumber());

				// If we've never seen this text file before, then make a new
				// HashMap to store
				// its element data in later (but only if the file actually
				// exists)
				if (myFile.exists()
						&& this.textFiles.containsKey(myRef.filePath) == false) {
					this.textFiles.put(myRef.filePath, new HashMap<String, TextElement>());
				}
				// Save the text ref info
				this.textReferences.put(myRef.id, myRef);
			} catch (URISyntaxException e) {
			} catch (IOException e) {
			}
		}
		// Any DTBook element -- collect ids and any smilRef data
		else if (this.fileType.equals("TEXT")) {
			if (atts.getValue("id") != null || atts.getValue("smilref") != null) {
				myElem = new TextElement();
				myElem.id = atts.getValue("id");
				myElem.lineNum = new Integer(myLocator.getLineNumber());
				myElem.smilRef = atts.getValue("smilref");
				try {
					Map<String,TextElement> hm = this.textFiles.get(this.parsedFile.getCanonicalPath());
					hm.put(myElem.id, myElem);
				} catch (IOException e) {
				}
			}
		}
	}
	public void startPrefixMapping(String prefix, String uri) {
	}

	private Map<String, Map<String,TextElement>> textFiles;
	private Map<String,TextRef> textReferences;
	private String fileType;
	private File parsedFile;
	private Locator myLocator;
}

// These are little utility classes to encapsulate data on SMIL text references
// and
//      dtbook elements

class TextRef {
	public Integer lineNum;
	public URI myURI;
	public String filePath;
	public String fileName;
	public String id;
}

class TextElement {
	public Integer lineNum;
	public String id;
	public String smilRef;
	public URI myURI;
	public String filePath;
}
