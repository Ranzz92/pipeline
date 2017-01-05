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

// TextInternal.java
/* Versions:
 0.2.0 (10/04/2003)
 1.0.0 (25/06/2003)
 - Changed detail message when null public ID detected (bug #755770)
 30 Jun 2005 (JP)
 - Changed from implementation to extension of ZedCustomTest
 - performTest no longer returns value
 - Removed ErrorListener
 - Discovers incorrect parameter type through CastClass exception
 - Removed all DOM calls (now handled in ZedCustomTest)
 */
package org.daisy.zedval.zedsuite.v2002;

import java.io.FileInputStream;

import javax.xml.parsers.SAXParserFactory;

import org.daisy.zedval.engine.TextFile;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.XMLReader;

/**
 * Performs text file internal checks:
 * <dl>
 * <dt>dtbook_prologPubId</dt>
 * <dd>dtbook source file public identifier equals "-//NISO//DTD dtbook
 * v1.1.0//EN"</dd>
 * <dt>dtbook_Version</dt>
 * <dd>Version attribute on dtbook element has value 1.1.0</dd>
 * </dl>
 * 
 * @author James Pritchett
 */

public class TextInternal extends ZedCustomTest implements ContentHandler {

	/**
	 * Performs the internal tests on a text file
	 * 
	 * @param f
	 *            A TextFile instance
	 */
	public void performTest(ZedFile f) throws ZedCustomTestException {
		TextFile t;
		XMLReader parser = null;
		FileInputStream fis = null;
		InputSource is = null;

		this.resultDoc = null;		// Clear out the results document

		// If f isn't a TextFile, this is a fatal error
		try {
			t = (TextFile) f; // Make our life simpler by removing need for casts
        } catch (ClassCastException castEx) {
            throw new ZedCustomTestException(castEx.getMessage());
        }

		// Check out the public identifier that was picked up at parse time.

		// TEST: dtbook source file public identifier equals "-//NISO//DTD
		// dtbook v1.1.0//EN" (dtbook_prologPubId)
		if (t.getDoctypePublicId() == null) {
			super.addTestFailure("dtbook_prologPubId","Text document "
					+ t.getName() + " has no DTD public identifier",null,null);
		} else if (t.getDoctypePublicId().equals("-//NISO//DTD dtbook v1.1.0//EN") == false) {
			super.addTestFailure("dtbook_prologPubId","Text document "
					+ t.getName() + "uses incorrect DTD public identifier: "
					+ t.getDoctypePublicId(),null,null);
		}

		// TEST: Version attribute on dtbook element has value 1.1.0
		// (dtbook_Version)
		// This test requires that the version attribute actually be present in
		// the document, not just
		//      defaulted from the DTD. To test this, we reparse the document without
		// loading the
		//      DTD and look for the version attribute.

		// Create the parser
		try {
			//TODO move to proper JAXP?			
			//parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
            parser = SAXParserFactory.newInstance().newSAXParser().getXMLReader();		
			if(parser.getClass().getName().indexOf("xerces")<0) {
				System.err.println("Warning: SAXParser is not Xerces: " + parser.getClass().getName());	
			}			
			parser.setFeature("http://xml.org/sax/features/validation", false);
			parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (Exception e) {
			throw new ZedCustomTestException("Could not create XMLReader: "
							+ e.getMessage());
		}

		// Set us up as the content handler
		parser.setContentHandler(this);

		// Parse the file; all errors will be handled by error(), fatalError(),
		// and warning() below
		try {
			fis = new FileInputStream(t);
			is = new InputSource(fis);
			parser.parse(is);
		} catch (Exception e) {
			// Eat up all exceptions
		}finally{
			try {
				fis.close();
			} catch (Exception e) {
			} // Ignore any exceptions here
		}

		// The version number (if present) is stashed in an instance variable
		// after parse
		if (this.version == null || this.version.equals("1.1.0") == false) {
			if (this.version == null) {
				super.addTestFailure("dtbook_Version","Missing version attribute in document",null,null);
			} else {
				super.addTestFailure("dtbook_Version","Bad version attribute value: "
						+ this.version,null,null);
			}
		}
	}

	// ContentHandler implementation; startElement is the only one we care about
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) {
// jpritchett@rfbd.org, 23 Aug 2006:  Added qName to test
		if (localName.equals("dtbook") || qName.equals("dtbook")) {
			this.version = atts.getValue("version");
		}
	}

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

	private String version;
}
