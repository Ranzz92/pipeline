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

// NcxInternal.java
/* Versions:
 0.2.0 (08/04/2003)
 0.3.0 (28/05/2003)
 - Changed handling of dtb:depth validation to better recognize nulls
 1.0.0 (25/06/2003)
 - Changed detail message when null public ID detected (bug #755770)
 */
package org.daisy.zedval.zedsuite.v2002;

import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.SAXParserFactory;

import org.daisy.zedval.engine.NcxFile;
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
 * Performs NCX internal checks:
 * <dl>
 * <dt>NCX_PrologPubId</dt>
 * <dd>NCX public identifier is "-//NISO//DTD ncx v1.1.0//EN"</dd>
 * <dt>NCX_Version</dt>
 * <dd>Version attribute on NCX element has value 1.1.0</dd>
 * <dt>ncx_metaDtbDepthValueCorrelated</dt>
 * <dd>dtb:depth content attribute value indicates depth of structure of the
 * DTB as exposed by the NCX</dd>
 * </dl>
 * 
 * @author James Pritchett
 */

public class NcxInternal extends ZedCustomTest implements ContentHandler {

	/**
	 * Performs the internal tests on the NCX
	 * 
	 * @param f
	 *            An NcxFile instance
	 * @throws ZedCustomTestException
	 */
	public void performTest(ZedFile f) throws ZedCustomTestException {
		NcxFile n;
		XMLReader parser = null;
		FileInputStream fis = null;
		InputSource is = null;

		this.resultDoc = null;		// Clear out the results document

		// If f isn't an NcxFile, this is a fatal error
		try {
            n = (NcxFile) f; // Make our life simpler by removing need for casts
        } catch (ClassCastException castEx) {
            throw new ZedCustomTestException(castEx.getMessage());
        }
        
		// Check out the public identifier that was picked up at parse time.

		// TEST: NCX public identifier is "-//NISO//DTD ncx v1.1.0//EN"
		// (NCX_PrologPubId)
		if (n.getDoctypePublicId() == null) {
		    super.addTestFailure("NCX_PrologPubId","NCX file " + n.getName()+ " has no DTD public identifier", null, null);
		} else if (n.getDoctypePublicId().equals("-//NISO//DTD ncx v1.1.0//EN") == false) {
		    super.addTestFailure("NCX_PrologPubId","NCX file " + n.getName()+ " uses incorrect DTD public identifier: "+ n.getDoctypePublicId(), null, null);
		}

		// TEST: dtb:depth content attribute value indicates depth of structure
		// of the DTB as exposed by the NCX (ncx_metaDtbDepthValueCorrelated)
		if (n.getDepth() != null && n.expectedDepth() != null
				&& n.getDepth().compareTo(n.expectedDepth()) != 0) {
		    super.addTestFailure("ncx_metaDtbDepthValueCorrelated","dtb:depth="+ n.getDepth() + ", actual depth of NCX="+ n.expectedDepth(), null, null);
		}

		// TEST: Version attribute on NCX element has value 1.1.0 (NCX_Version)
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
		    throw new ZedCustomTestException("Could not create XMLReader: "+ e.getMessage());
		}
		// Set us up as the content handler
		parser.setContentHandler(this);

		// Parse the file; all errors will be handled by error(), fatalError(),
		// and warning() below
			try {
                fis = new FileInputStream(n);
                is = new InputSource(fis);
                parser.parse(is);
            } catch (IOException ioEx) {
                throw new ZedCustomTestException(ioEx.getMessage());
            } catch (SAXException e) {
                // Eat up all SAXExceptions
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
			    super.addTestFailure("NCX_Version","Missing version attribute in document", null, null);
			} else {
			    super.addTestFailure("NCX_Version","Bad version attribute value: "+ this.version, null, null);
			}
		}

	}

	// ContentHandler implementation; startElement is the only one we care about
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) {
// jpritchett@rfbd.org, 23 Aug 2006:  changed test to include qName
		if (localName.equals("ncx") || qName.equals("ncx")) {
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
