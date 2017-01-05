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

// DtbookSmilRelations.java
/* Versions:
 1.0.0 (07/07/2003)
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
import java.util.Stack;

import javax.xml.parsers.SAXParserFactory;

import org.daisy.zedval.engine.PackageFile;
import org.daisy.zedval.engine.TextFile;
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
 * Performs dtbook-SMIL relations checks:
 * <dl>
 * <dt>dtbook_SmilRefResolves</dt>
 * <dd>The smilRef attribute is a URI that resolves to a SMIL time container
 * referencing the current dtbook element</dd>
 * </dl>
 * 
 * @author James Pritchett
 */

public class DtbookSmilRelations extends ZedCustomTest implements ContentHandler {

	/**
	 * Performs the relational tests on a dtbook file
	 * 
	 * @param f A TextFile instance
	 * @throws ZedCustomTestException
	 */
	public void performTest(ZedFile f) throws ZedCustomTestException {
		TextFile t;
		PackageFile p;
		XMLReader parser = null;
		FileInputStream fis = null;
		InputSource is = null;
		Map<String,TextRef> textRefs;

		this.resultDoc = null;		// Clear out the results document

		// If f isn't a TextFile, this is a fatal error
		try {
            t = (TextFile) f; // Make our life simpler by removing need for casts
        } catch (ClassCastException castEx) {
            throw new ZedCustomTestException(castEx.getMessage());
        }

        // Look at the package and check to see if there are any SMIL files to
		// test
		p = t.getPackage();
		if (p.getSmilFiles() == null || p.getSmilFiles().isEmpty()) {
		    //TODO 2005-06-30 throw an exception?
		    throw new ZedCustomTestException("Missing smil files content.");
		    //return resultDoc; // Bail now and save us all some time
		}
		smilFiles = new HashMap<String, Map<String,TextRef>>();
		// Set up the parser
		// Create the parser
		try {
			//TODO move to proper JAXP?
			//parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			if(parser.getClass().getName().indexOf("xerces")<0) {
				System.err.println("Warning: SAXParser is not Xerces: " + parser.getClass().getName());	
			}			
			parser.setFeature("http://xml.org/sax/features/validation", false);
		} catch (Exception e) {
		    throw new ZedCustomTestException("WARNING!  Unrecoverable parse error in DtbookSmilRelations.  Could not create XMLReader: "+e.getMessage());
		}

		// Set us up as the content handler; dtbook file is entity resolver
		parser.setContentHandler(this);
		parser.setEntityResolver(t);
		parser.setErrorHandler(t);

		// Parse the dtbook file and collect the data on all smilRefs
		this.textElements = new HashMap<String, TextElement>();
		try {
			this.fileType = "TEXT";
			this.parsedFile = t;
			fis = new FileInputStream(t);
			is = new InputSource(fis);
			parser.parse(is);
		} catch (IOException e) {
		    throw new ZedCustomTestException("WARNING!  Unrecoverable parse error in DtbookSmilRelations. IOException when parsing:  "
					+ e.getMessage());
		} catch (SAXException e) {
			// Eat up all other SAX exceptions
		}finally{
			try {
				fis.close();
			} catch (Exception e) {
			} // Ignore any exceptions here
		}

		// Parse each referenced SMIL file and collect text reference data
		containerIDs = new Stack<String>();
		this.fileType = "SMIL";
		for (Iterator<String> i = this.smilFiles.keySet().iterator(); i.hasNext();) {
			try {
				this.parsedFile = new File(i.next());
				fis = new FileInputStream(parsedFile);
				is = new InputSource(fis);
				parser.parse(is);
			} catch (IOException e) {
			    throw new ZedCustomTestException("WARNING!  Unrecoverable parse error in DtbookSmilRelations. IOException when parsing:  "
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

		// Test each smilRef for resolution and for a text @src on the other end
		for (TextElement aTextElem : this.textElements.values()) {
			textRefs = this.smilFiles.get(aTextElem.filePath);
			if (textRefs == null) {
			    super.addTestFailure("dtbook_SmilRefResolves","File named in smilRef does not exist: "+ aTextElem.myURI, null,aTextElem.lineNum.toString());
			} else if (textRefs.get(aTextElem.myURI.getFragment()) == null) {
			    super.addTestFailure("dtbook_SmilRefResolves","Fragment identifier named in smilRef does not exist in document or is not a par/seq: "+ aTextElem.myURI, null, aTextElem.lineNum.toString());
			}
			// Compare the text @src URI to the current text element URI (file
			// name & fragment)
			else if (((TextRef) (textRefs.get(aTextElem.myURI.getFragment()))).myURI.getFragment().equals(aTextElem.id) == false
					|| ((TextRef) (textRefs.get(aTextElem.myURI.getFragment()))).fileName.equals(t.getName()) == false) {
			    super.addTestFailure("dtbook_SmilRefResolves","smilRef attribute URI does not point to correct time container in SMIL:  "+ aTextElem.myURI, null,aTextElem.lineNum.toString());
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
		// SMIL par or seq -- pop the ID off the stack
		if (this.fileType.equals("SMIL")
				&& (localName.equals("par") || localName.equals("seq"))) {
			this.containerIDs.pop();
		}
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

		// SMIL par or seq -- push the ID on the stack
// jpritchett@rfbd.org, 23 Aug 2006:  Added qName to the test
		if (this.fileType.equals("SMIL")
				&& (localName.equals("par") || localName.equals("seq") ||
				    qName.equals("par") || qName.equals("seq"))) {
			this.containerIDs.push(atts.getValue("id"));
		}

		// SMIL file text element -- Get the basic information
		else if (this.fileType.equals("SMIL") && 
				(localName.equals("text") || qName.equals("text"))) {
			myRef = new TextRef();
			myRef.id = (String) this.containerIDs.peek();
			try {
				myRef.myURI = new URI(atts.getValue("src"));
				myFile = new File(parsedFile.getParentFile().getCanonicalPath()
						+ File.separator + myRef.myURI.getPath());
				myRef.fileName = myFile.getName();
				myRef.lineNum = new Integer(myLocator.getLineNumber());

				// Save the text ref info
				try {
					Map<String,TextRef> hm = this.smilFiles.get(this.parsedFile.getCanonicalPath());
					hm.put(myRef.id, myRef);
				} catch (IOException e) {
				}
			} catch (URISyntaxException e) {
			} catch (IOException e) {
			}
		}
		// Any DTBook element -- collect any smilRef data
		else if (this.fileType.equals("TEXT")) {
			if (atts.getValue("smilref") != null) {
				myElem = new TextElement();
				myElem.id = atts.getValue("id");
				myElem.lineNum = new Integer(myLocator.getLineNumber());
				myElem.smilRef = atts.getValue("smilref");
				try {
					myElem.myURI = new URI(myElem.smilRef);
					myFile = new File(parsedFile.getParentFile().getCanonicalPath()
							+ File.separator + myElem.myURI.getPath());
					myElem.filePath = myFile.getCanonicalPath();

					// If we've never seen this SMIL file before, then make a
					// new HashMap to store
					// its text reference data in later (but only if the file
					// actually exists)
					if (myFile.exists()
							&& this.smilFiles.containsKey(myElem.filePath) == false) {
						this.smilFiles.put(myElem.filePath, new HashMap<String, TextRef>());
					}
					this.textElements.put(myElem.id, myElem);
				} catch (URISyntaxException e) {
				} catch (IOException e) {
				}
			}
		}
	}
	public void startPrefixMapping(String prefix, String uri) {
	}

	private Map<String,Map<String,TextRef>> smilFiles;
	private Map<String,TextElement> textElements;
	private String fileType;
	private File parsedFile;
	private Locator myLocator;
	private Stack<String> containerIDs;
}
