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

import org.daisy.util.xml.SmilClock;
import org.xml.sax.Attributes;

/**
 * A <code>SmilFile</code> object represents a DTB synchronization file
 * @author James Pritchett
 * @author Markus Gylling
 */
public class SmilFile extends XmlFile {
    private SmilClock totalElapsedTime;
    private SmilClock duration;
    private SmilClock durationActual;
    private String myUid;    
	private Map<String,File> fileRefs = new HashMap<String, File>();
    private Map<String,File> textFileRefs = new HashMap<String, File>();
    private Map<String,File> audioFileRefs = new HashMap<String, File>();
    private Map<String,File> imageFileRefs = new HashMap<String, File>();
    private Map<String,File> linkFileRefs = new HashMap<String, File>();    
    private List<XmlFileElement> skippableElements = new LinkedList<XmlFileElement>();
    private List<XmlFileElement> escapableElements = new LinkedList<XmlFileElement>();
    private List<XmlFileElement> customTestElements = new LinkedList<XmlFileElement>();
    private long tmpDuration;
    private boolean seenMotherSeq;
    private boolean hasEscapableStructures = false;    
    private boolean hasSkippableStructures = false;    

    /**
     * @param id
     *            id from package file manifest
     * @param fullPath
     *            File path
     * @param mimeType
     *            File MIME type as given in package manifest
     */
    public SmilFile(String fullPath, String id, String mimeType,String manifestURI) {
        super(fullPath, id, mimeType,manifestURI);
        seenMotherSeq = false;
        tmpDuration = 0;
    }

    /**
     * @return true if this SMIL file contains skippable structures (= customTest), false otherwise
     */
    public boolean hasSkippableStructures() {
        return this.hasSkippableStructures;
    }

    /**
     * @return true if this SMIL file contains escapable structures (@end contains 'DTBuserEscape;'), false otherwise
     */

    public boolean hasEscapableStructures() {
        return this.hasEscapableStructures;
    }
    
    
    /**
     * Returns declared total elapsed time prior to this SMIL file
     * (dtb:totalElapsedTime metadata item)
     * 
     * @return SmilClock object expressing declared total elapsed time
     */
    public SmilClock getTotalElapsedTime() {
        return this.totalElapsedTime;
    }

    /**
     * Returns declared total duration of this SMIL file
     * 
     * @return SmilClock object expressing declared total duration of main <seq>
     */
    public SmilClock getDuration() {
        return this.duration;
    }

    /**
     * Returns actual total duration of this SMIL file
     * 
     * @return SmilClock object expressing actual total duration of all audio
     *         clips
     */
    public SmilClock expectedDuration() {
        return this.durationActual;
    }

    /**
     * Is the declared total duration correct?
     * 
     * @return <code>true</code> if so
     */
    public boolean isValidDuration() {
        // This will need some work, I think ... too simple
        return (this.duration == this.durationActual);
    }

    /**
     * Returns the declared unique identifier value for this SMIL file (value of
     * "dtb:uid" metadata item)
     * 
     * @return The declared unique identifier value for this SMIL file (value of
     *         "dtb:uid" metadata item)
     */
    public String getUid() {
        return this.myUid;
    }

    /**
     * Returns a HashMap of all files referenced by this file
     * 
     * @return A HashMap of File objects (key = absolute full path)
     */
    public Map<String,File> getFileRefs() {
    	if(this.fileRefs.isEmpty()) return null;
        return this.fileRefs;
    }

    /**
     * Returns a HashMap of all text files referenced by this file
     * 
     * @return A HashMap of File objects (key = absolute path)
     */
    public Map<String,File> getTextFileRefs() {
    	if(this.textFileRefs.isEmpty()) return null;
        return this.textFileRefs;
    }

    /**
     * Returns a HashMap of all audio files referenced by this file
     * 
     * @return A HashMap of File objects (key = absolute path)
     */
    public Map<String,File> getAudioFileRefs() {
    	if(this.audioFileRefs.isEmpty()) return null;
        return this.audioFileRefs;
    }

    /**
     * Returns a HashMap of all image files referenced by this file
     * 
     * @return A HashMap of File objects (key = absolute path)
     */
    public Map<String,File> getImageFileRefs() {
    	if(this.imageFileRefs.isEmpty()) return null;
        return this.imageFileRefs;
    }

    /**
     * Returns a HashMap of all files referenced by links in this file
     * 
     * @return A HashMap of File objects (key = absolute path)
     */
    public Map<String,File> getLinkFileRefs() {
    	if(this.linkFileRefs.isEmpty()) return null;
        return this.linkFileRefs;
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
        String metaName;
        String clipString;
        SmilClock clipBegin = null;
        SmilClock clipEnd = null;

        //do generic collects
        super.startElement(namespaceURI, localName, qName, atts);
                
        //add to the element collector
        XmlFileElement xe = this.addXmlFileElement(namespaceURI,localName,qName,atts);

        //populate some convenience collections using xe
        if(xe.getLocalName()=="customTest") {
        	this.customTestElements.add(xe);
        }
        
    	String end = xe.getAttributeValueL("end");
    	if(end!= null && end.indexOf("DTBuserEscape")>=0 ){
    		this.escapableElements.add(xe);
    	}
    	if(xe.getAttributeValueL("customTest")!= null){
    		this.skippableElements.add(xe);
    	}
        
      
    
        // <meta> - Grab the important numbers/properties
        if (localName=="meta") {
            metaName = atts.getValue("name").intern();
            if (metaName=="dtb:uid") {
                this.myUid = atts.getValue("content");
            } else if (metaName=="dtb:totalElapsedTime") {
                try {
                    this.totalElapsedTime = new SmilClock(atts.getValue("content"));
                } catch (NumberFormatException e) {
                    this.totalElapsedTime = null;
                }
            }
        }

        // Calculate total duration from clip times
        else if(localName=="audio") {
	        clipString = atts.getValue("clipBegin"); // Get clipBegin ...
	        if (clipString != null) {
	            try {
	                clipBegin = new SmilClock(clipString);
	            } catch (NumberFormatException e) {
	            	this.getZedFileInitEx().addError(e.getMessage());
	                clipBegin = null;
	            }
	        }
	        clipString = atts.getValue("clipEnd"); // ... and get clipEnd
	        if (clipString != null) {
	            try {
	                clipEnd = new SmilClock(clipString);
	            } catch (NumberFormatException e) {
	            	this.getZedFileInitEx().addError(e.getMessage());
	                clipEnd = null;
	            }
	        }
	        if (clipBegin != null && clipEnd != null) {
	            this.tmpDuration = this.tmpDuration
	                    + clipEnd.millisecondsValue()
	                    - clipBegin.millisecondsValue();
	        }
        }

        // First seq we see is the "Mother seq"; if it has a duration, grab it
        else if (localName=="seq") {
        	if(this.seenMotherSeq == false && atts.getValue("dur") != null) {        
	            try {
	                this.duration = new SmilClock(atts.getValue("dur"));
	            } catch (NumberFormatException e) {
	                this.duration = null; // Ignore exceptions now
	            }
	            this.seenMotherSeq = true;
	        }
        	
            String endValue = atts.getValue("end");
            if(endValue!=null && endValue.indexOf("DTBuserEscape;")>=0) {
                this.hasEscapableStructures = true;
            }
        }
    
        else if(localName=="customTest") {
            this.hasSkippableStructures = true;
        }
        

            
        //collect URI values and add to filerefs
        try{        	        	        	        	
	        if (localName=="text") {
	        	File fl = this.addToFileRefs(this.fileRefs,atts.getValue("src"));
	        	if(fl!=null) { //this was a new kid on the block
	        		this.textFileRefs.put(fl.getCanonicalPath(), fl);
	        	}
	        }else if (localName=="audio") {
	        	File fl = this.addToFileRefs(this.fileRefs,atts.getValue("src"));
	        	if(fl!=null) { //this was a new kid on the block
	        		this.audioFileRefs.put(fl.getCanonicalPath(), fl);
	        	}	        		        		        		        	
	        }else if (localName=="img") {
	        	File fl = this.addToFileRefs(this.fileRefs,atts.getValue("src"));
	        	if(fl!=null) { //this was a new kid on the block
	        		this.imageFileRefs.put(fl.getCanonicalPath(), fl);
	        	}
	        }else if (localName=="a") {
	            String external = atts.getValue("external");
	            File fl = null;
	            if(external==null||external.equals("false")) {
	                fl = this.addToFileRefs(this.fileRefs,atts.getValue("href"));
	            }               
	            if(fl!=null) { //this was a new kid on the block
	                this.linkFileRefs.put(fl.getCanonicalPath(), fl);
	            }
	        }	
        } catch (URISyntaxException e) {
        	super.getZedFileInitEx().addError("URISyntaxException in file " + this.getName() + ": " + e.getMessage());
            return;
        } catch (IllegalArgumentException e) {
        	super.getZedFileInitEx().addError("IllegalArgumentException in file " + this.getName() + ": " + e.getMessage());
            return;
        } catch (IOException e) {
            super.getZedFileInitEx().addError("IOException while accessing files pointed out by " + this.getName()+ ": " + e.getMessage());
            return;
        }


    }


    /**
     * @return a List&lt;XmlFileElement&gt; of all SMIL head/customAttributes/customTest elements with local name "customTest", an empty Set
     * if no such elements exist in this SMIL file.
     */
    public List<XmlFileElement> getCustomTestElements() {
    	return this.customTestElements;    	
    }

    /**
     * @return a List&lt;XmlFileElement&gt; of all SMIL elements with an attribute "customTest", an empty Set
     * if no such elements exist in this SMIL file.
     */
    public List<XmlFileElement> getSkippableElements() {
    	return this.skippableElements;
    }

    /**
     * @return a List&lt;XmlFileElement&gt; of all SMIL elements with an attribute "end" with a value containing "DTBuserEscape", an empty Set
     * if no such elements exist in this SMIL file.
     */
    public List<XmlFileElement> getEscapableElements() {
    	return this.escapableElements;
    }

    
    /**
     * Looks for end of <body>to set total duration
     * 
     * @param namespaceURI
     *            The namespace URI of the element
     * @param localName
     *            The non-prefixed name of the element
     * @param qName
     *            The qualified name of the element
     */
    public void endElement(String namespaceURI, String localName, String qName) {
        if (localName.equals("body")) {
            this.durationActual = new SmilClock(this.tmpDuration);
        }
    }


    // toString for debugging
    public String toString() {
        String s;

        s = super.toString() + "\n\t[uid=" + this.myUid + "]\n";
        // Print declared/actual for all time properties
        s = s + "\t[totalElapsedTime=" + this.totalElapsedTime + "]\n";
        s = s + "\t[duration=" + this.duration + " (actual="
                + this.durationActual + ")]\n";

        // Print just the names of the files referenced
        if (this.fileRefs != null) {
            s = s + "\t[fileRefs=\n";
            for (File f : fileRefs.values()) {
                try {
                    s = s + "\t\titem=" + f.getCanonicalPath() + "\n";
                } catch (IOException e) {
                    s = s + "\t\titem=" + f.getName() + "\n";
                }
            }
            s = s + "]\n";
        } else {
            s = s + "\t[fileRefs=null]\n";
        }
        if (this.textFileRefs != null) {
            s = s + "\t[textFileRefs=\n";
            for (File f : textFileRefs.values()) {
                try {
                    s = s + "\t\titem=" + f.getCanonicalPath() + "\n";
                } catch (IOException e) {
                    s = s + "\t\titem=" + f.getName() + "\n";
                }
            }
            s = s + "]\n";
        } else {
            s = s + "\t[textFileRefs=null]\n";
        }
        if (this.audioFileRefs != null) {
        	s = s + "\t[audioFileRefs=\n";
            for (File f : audioFileRefs.values()) {
                try {
                    s = s + "\t\titem=" + f.getCanonicalPath() + "\n";
                } catch (IOException e) {
                    s = s + "\t\titem=" + f.getName() + "\n";
                }
            }
            s = s + "]\n";
        } else {
            s = s + "\t[audioFileRefs=null]\n";
        }
        if (this.imageFileRefs != null) {
            s = s + "\t[imageFileRefs=\n";
            for (File f : imageFileRefs.values()) {
                try {
                    s = s + "\t\titem=" + f.getCanonicalPath() + "\n";
                } catch (IOException e) {
                    s = s + "\t\titem=" + f.getName() + "\n";
                }
            }
            s = s + "]\n";
        } else {
            s = s + "\t[imageFileRefs=null]\n";
        }
        if (this.linkFileRefs != null) {
            s = s + "\t[linkFileRefs=\n";
            for (File f : linkFileRefs.values()) {
                try {
                    s = s + "\t\titem=" + f.getCanonicalPath() + "\n";
                } catch (IOException e) {
                    s = s + "\t\titem=" + f.getName() + "\n";
                }
            }
            s = s + "]\n";
        } else {
            s = s + "\t[linkFileRefs=null]\n";
        }

        return s;
    }
    
    private static final long serialVersionUID = -6551003782746696442L;
}
