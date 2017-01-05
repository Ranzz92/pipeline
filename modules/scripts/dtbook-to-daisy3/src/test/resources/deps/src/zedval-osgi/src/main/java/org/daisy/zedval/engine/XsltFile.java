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
import java.util.Map;

import org.xml.sax.Attributes;

/**
 * A <code>XsltFile</code> object represents a DTB XSL file
 * @author Markus Gylling
 */

public class XsltFile extends XmlFile {
	
	private Map<String,File> fileRefs = new HashMap<String,File>();
	
    /**
     * @param id
     *            id from package file manifest
     * @param fullPath
     *            File path
     * @param mimeType
     *            File MIME type as given in package manifest
     */
    public XsltFile(String fullPath, String id, String mimeType, String manifestURI) {
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

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
        
        //do generic collects
        super.startElement(namespaceURI, localName, qName, atts);
                
        //add to the element collector
        this.addXmlFileElement(namespaceURI,localName,qName,atts);
        
        //collect URI values and add to filerefs
        try{
	        if (localName=="include") {
	        	this.addToFileRefs(this.fileRefs,atts.getValue("href"));
	        }
        } catch (URISyntaxException e) {
        	super.getZedFileInitEx().addError("URISyntaxException in file " + this.getName() + ": " + e.getMessage());
            return;
        } catch (IOException e) {
            super.getZedFileInitEx().addError("IOException while accessing files pointed out by " + this.getName()+ ": " + e.getMessage());
            return;
        }
               
    }

    
    
	private static final long serialVersionUID = -8322567785814016695L;
}


