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
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;

/**
 * A <code>ResourceFile</code> object represents a DTB resource file
 * 
 * @author James Pritchett
 * @author Markus Gylling
 */
public class ResourceFile extends XmlFile {	
	private List<XmlFileElement> scopeElements = new LinkedList<XmlFileElement>();
	private Map<String,File> fileRefs = new HashMap<String, File>();
	
    /**
     * @param id
     *            id from package file manifest
     * @param fullPath
     *            File path
     * @param mimeType
     *            File MIME type as given in package manifest
     */
    public ResourceFile(String fullPath, String id, String mimeType,String manifestURI) {
        super(fullPath, id, mimeType,manifestURI);
    }

    /**
     * Returns a HashMap of all files referenced by this file
     * 
     * @return A HashMap of File objects (key = abolute path)
     */
    public Map<String,File> getFileRefs() {
    	if(this.fileRefs.isEmpty()) return null;
        return this.fileRefs;
    }

    // SAX ContentHandler implementations:
    /**
     * Grabs all incoming elements and looks for properties to set
     * 
     * @param namespaceURI
     *            The namespace URI of the element
     * @param localName
     *            The non-prefixed name of the element
     * @param qName
     *            The qualified name of the element
     * @param atts
     *            List of all element attributes
     */
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        
        //do generic collects
        super.startElement(namespaceURI, localName, qName, atts);
                
        //add to the element collector
        XmlFileElement xfe = this.addXmlFileElement(namespaceURI,localName,qName,atts);
        if(xfe.getLocalName()=="scope"){
        	scopeElements.add(xfe);
        }	

        //collect URI values and add to filerefs
        try{
	        if (localName=="audio" || localName=="img") {
	        	this.addToFileRefs(this.fileRefs,atts.getValue("src"));
	        }
        } catch (URISyntaxException e) {
        	super.getZedFileInitEx().addError("URISyntaxException in file " + this.getName() + ": " + e.getMessage());
            return;
        } catch (IOException e) {
            super.getZedFileInitEx().addError("IOException while accessing files pointed out by " + this.getName()+ ": " + e.getMessage());
            return;
        }
        
        
       
    }

    
    
    /**
     * @return true if this file has a scope element with an nsuri attr valued as the input param, false otherwise
     * note- for z2002 DTBs, this test will always return false
     */
    public boolean hasNsUri(String nsURI) {
    	for (XmlFileElement xf : scopeElements) {
			if(xf.getAttributeValueL("nsuri")!=null && xf.getAttributeValueL("nsuri").equals(nsURI)){
				return true;
			}
    	}
    	return false;
    }
    
    private static final long serialVersionUID = 2135233863482203033L;
}


