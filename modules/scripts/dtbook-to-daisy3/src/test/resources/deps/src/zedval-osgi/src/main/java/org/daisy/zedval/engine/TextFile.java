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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.daisy.util.text.URIUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;

/**
 * A <code>TextFile</code> object represents a DTB textual content file
 * 
 * @author James Pritchett
 * @author Piotr Kiernicki
 * @author mgylling
 */
public class TextFile extends XmlFile {    
	private Map<String, File> fileRefs = new HashMap<String,File>();
    private Map<String, XmlFileElement> hashSmilRefToXmlFileElementMap = new HashMap<String,XmlFileElement>(ZedConstants.INITIAL_CAPACITY_COLLECTION);
    private String uid;

    /**
     * @param id
     *            id from package file manifest
     * @param fullPath
     *            File path
     * @param mimeType
     *            File MIME type as given in package manifest
     */
    public TextFile(String fullPath, String id, String mimeType, String manifestURI) {
        super(fullPath, id, mimeType,manifestURI);
    }

    /**
     * Returns a HashMap of all files referenced by this file
     * @return A HashMap of File objects (key = absolute full path)
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
        XmlFileElement xe = this.addXmlFileElement(namespaceURI,localName,qName,atts);        
        
        //misc special properties
        if (localName=="meta") {
            try {
                if (atts.getValue("name").equals("dtb:uid")) {
                    this.uid = (atts.getValue("content"));
                }
            } catch (Exception e) {
                // do nothing
            }
        }
        
        //collect URI values and add to filerefs 
        try{
	        if ((localName=="link" || localName=="a") && atts.getValue("href") != null) {
	        	//mg20081116, dont add when @external=true
	        	String external = atts.getValue("external");
	        	if(external==null||external.equals("false"))
	        		this.addToFileRefs(this.fileRefs,atts.getValue("href"));
	        } else if (localName=="img") {
	            if (atts.getValue("src") != null) {
	            	this.addToFileRefs(this.fileRefs,atts.getValue("src"));
	            } 
	            if (atts.getValue("longdesc") != null) {
	            	this.addToFileRefs(this.fileRefs,atts.getValue("longdesc"));
	            }        	
	        } else if (localName=="math") {
	            if (atts.getValue("altimg") != null) {
	            	this.addToFileRefs(this.fileRefs,atts.getValue("altimg"));
	            } 
	        }    
	        
	        String sr = atts.getValue("smilref");
	        if (sr != null) {	
	        	this.addToFileRefs(this.fileRefs,sr);
	        	//add to the performance hashmap using xe from above
	        	this.hashSmilRefToXmlFileElementMap.put(sr,xe);
	        }
	        //mg20080617: for @smilref when on m:math
	        sr = atts.getValue("dtbook:smilref");
	        if (sr != null) {	
	        	this.addToFileRefs(this.fileRefs,sr);
	        	//add to the performance hashmap using xe from above
	        	this.hashSmilRefToXmlFileElementMap.put(sr,xe);
	        }
        } catch (URISyntaxException e) {
        	super.getZedFileInitEx().addError("URISyntaxException in file " + this.getName() + ": " + e.getMessage());
            return;
        } catch (IOException e) {
            super.getZedFileInitEx().addError("IOException while accessing files pointed out by " + this.getName()+ ": " + e.getMessage());
            return;
        }            
    }

    // public void processingInstruction(String target, String data) throws
    // SAXException {
    public void processingInstruction(String target, String data) {
        if (target.equals("xml-stylesheet")) { // see:
                                                // http://www.w3.org/TR/xml-stylesheet/
            String content[] = data.split(" ");
            for (int i = 0; i < content.length; i++) {
                if (content[i].startsWith("href")) {
                    char doublequote = '"';
                    char singlequote = '\'';
                    String value = content[i].replace(singlequote, doublequote);
                    try {
                        value = value.substring(value.indexOf(doublequote) + 1,
                                value.lastIndexOf(doublequote));
                    } catch (Exception e) {
                        String message = "could not grok processing instruction"
                                + target + " " + data + " in " + this.getName();
                        this.error(new SAXParseException(message, null));
                    }
                    //
                    try {
                        URI uri = new URI(value);
                        uri = URIUtils.resolve(this.toURI(), uri);
                        File f = new File(uri);
                        if (this.fileRefs == null)this.fileRefs = new HashMap<String,File>();
                        this.fileRefs.put(f.getCanonicalPath(), f);
                    } catch (Exception e) {
                        System.err
                                .println("processinginstruction error on TextFile");                         
                    }

                }
            }
        } else {
            String message = "did not recognize processing instruction"
                    + target + " " + data + " in " + this.getName();
            this.error(new SAXParseException(message, null));
        }
    }

    public String getUid() {
        return this.uid;
    }
    
    /** 
     * @param smilrefValue a string that may or may not be the value of an attribute named smilref in this TextFile
     * @return an XmlFileElement (=element of this textfile) that matches the inparam smilref value, null if no match
     */
    public XmlFileElement getXmlFileElementBySmilrefValue(String smilrefValue){    	
    	return (XmlFileElement)this.hashSmilRefToXmlFileElementMap.get(smilrefValue);
    }
    
    /**
     * @return a Collection&lt;XmlFileElement&gt; of those elements in in this TextFile that carry a smilref attribute
     */
    public Collection<XmlFileElement> getXmlFileElementsWithSmilrefAttrs(){    	
    	return this.hashSmilRefToXmlFileElementMap.values();
    }
    
    private static final long serialVersionUID = 579148419422953227L;
}
