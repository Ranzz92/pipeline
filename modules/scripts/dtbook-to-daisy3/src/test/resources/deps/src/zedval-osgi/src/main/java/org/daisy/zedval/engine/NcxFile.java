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
 * An <code>NcxFile</code> object represents a DTB navigation control file
 * @author James Pritchett
 * @author Markus Gylling
 */

public class NcxFile extends XmlFile {
    private int maxPageNormal;
    private int maxPageNormalActual;
    private int maxPageNumber;
    private int maxPageNumberActual = 0;
    private boolean allPageNormalTargetsHaveValueAttribute = true;
    private int pageFront;
    private int pageFrontActual;
    private int pageNormal;
    private int pageNormalActual;
    private int pageSpecial;
    private int pageSpecialActual;
    private Integer depth;
    private Integer depthActual;
    private String myUid;    
	private Map<String,File> fileRefs = new HashMap<String, File>();
    private Map<String,File> contentFileRefs = new HashMap<String, File>();
    private Map<String,File> audioFileRefs = new HashMap<String, File>();
    private Map<String,File> imageFileRefs = new HashMap<String, File>();
    private int curDepth;
    private List<XmlFileElement> customTestElements = new LinkedList<XmlFileElement>(); 

    /**
     * @param id
     *            id from package file manifest
     * @param fullPath
     *            File path
     * @param mimeType
     *            File MIME type as given in package manifest
     */
    public NcxFile(String fullPath, String id, String mimeType, String manifestURI) {
        super(fullPath, id, mimeType, manifestURI);
    }

    /**
     * Returns the declared maximum page-normal value (value of
     * dtb:maxPageNormal metadata item)
     * 
     * @return the declared maximum page-normal value (value of
     *         dtb:maxPageNormal metadata item)
     */
    public int getMaxPageNormal() {
        return this.maxPageNormal;
    }

    /**
     * Returns the actual maximum page-normal value
     * @return the actual maximum page-normal value
     */
    public int expectedMaxPageNormal() {
        return this.maxPageNormalActual;
    }

    /**
     * Returns the declared number of front pages (value of dtb:pageFront
     * metadata item)
     * 
     * @return the declared number of front pages (value of dtb:pageFront
     *         metadata item)
     */
    public int getPageFront() {
        return this.pageFront;
    }

    /**
     * Returns the actual number of front pages
     * 
     * @return the actual number of front pages
     */
    public int expectedPageFront() {
        return this.pageFrontActual;
    }

    /**
     * Is the declared PageFront value correct?
     * 
     * @return <code>true</code> if so
     */
    public boolean isValidPageFront() {
        return (this.pageFront == this.pageFrontActual);
    }

    /**
     * Returns the declared number of normal pages (value of dtb:pageNormal
     * metadata item)
     * 
     * @return the declared number of normal pages (value of dtb:pageNormal
     *         metadata item)
     */
    public int getPageNormal() {
        return this.pageNormal;
    }

    /**
     * Returns the actual number of normal pages
     * 
     * @return the actual number of normal pages
     */
    public int expectedPageNormal() {
        return this.pageNormalActual;
    }

    /**
     * Returns the declared maxPageNumber (see z2005 meta dtb:maxPageNumber)
     * @return the declared maxPageNumber value
     */
    public int getMaxPageNumber() {
        return this.maxPageNumber;
    }

    /**
     * Returns the actual maxPageNumber (see z2005 meta dtb:maxPageNumber)
     * @return the actual maxPageNumber value
     * @throws IllegalStateException when NCX contains pageTargets without value attribute
     */
    public int expectedMaxPageNumber() {
    	if(allPageNormalTargetsHaveValueAttribute)
    		return this.maxPageNumberActual;
    	throw new IllegalStateException();
    }

    /**
     * Is the declared PageNormal value correct?
     * 
     * @return <code>true</code> if so
     */
    public boolean isValidPageNormal() {
        return (this.pageNormal == this.pageNormalActual);
    }

    /**
     * Returns the declared number of special pages (value of dtb:pageSpecial
     * metadata item)
     * 
     * @return the declared number of special pages (value of dtb:pageSpecial
     *         metadata item)
     */
    public int getPageSpecial() {
        return this.pageSpecial;
    }

    /**
     * Returns the actual number of special pages
     * 
     * @return the actual number of special pages
     */
    public int expectedPageSpecial() {
        return this.pageSpecialActual;
    }

    /**
     * Is the declared PageSpecial value correct?
     * 
     * @return <code>true</code> if so
     */
    public boolean isValidPageSpecial() {
        return (this.pageSpecial == this.pageSpecialActual);
    }

    /**
     * Returns the declared depth of heading nesting for this NCX (value of
     * dtb:depth metadata item)
     * 
     * @return The declared depth of heading nesting for this NCX (value of
     *         dtb:depth metadata item)
     */
    public Integer getDepth() {
        return this.depth;
    }

    /**
     * Returns the actual depth of heading nesting for this NCX
     * 
     * @return The actual depth of heading nesting for this NCX
     */
    public Integer expectedDepth() {
        return this.depthActual;
    }

    /**
     * Is the declared Depth value correct?
     * 
     * @return <code>true</code> if so
     */
    public boolean isValidDepth() {
        return (this.depth == this.depthActual);
    }

    /**
     * Returns the declared unique identifier value for this NCX (value of
     * "dtb:uid" metadata item)
     * 
     * @return The declared unique identifier value for this NCX (value of
     *         "dtb:uid" metadata item)
     */
    public String getUid() {
        return this.myUid;
    }

    /**
     * Returns a HashMap of all files referenced by this file
     * 
     * @return A HashMap of File objects (key = absolute path)
     */
    public Map<String,File> getFileRefs() {
    	if(this.fileRefs.isEmpty()) return null;
        return this.fileRefs;
    }

    /**
     * Returns a HashMap of all content files referenced by this file
     * 
     * @return A HashMap of File objects (key = absolute path)
     */
    public Map<String,File> getContentFileRefs() {
    	if(this.contentFileRefs.isEmpty()) return null;
        return this.contentFileRefs;
    }

    /**
     * Returns a HashMap of all audio files referenced by this file
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

        //do generic collects
        super.startElement(namespaceURI, localName, qName, atts);
                
        //add to the element collector
        XmlFileElement xe = this.addXmlFileElement(namespaceURI,localName,qName,atts);
        if(xe.getLocalName()=="smilCustomTest") {
        	this.customTestElements.add(xe);
        }
        
        //misc special properties
        // <meta> - Grab the important numbers/properties
        if (localName=="meta") {
            metaName = atts.getValue("name").intern();
            if (metaName=="dtb:uid") {
                this.myUid = atts.getValue("content");
            } else if (metaName=="dtb:maxPageNormal") {
                try {
                    this.maxPageNormal = Integer.parseInt(atts
                            .getValue("content"));
                } catch (NumberFormatException e) {
                    this.maxPageNormal = 0;
                }
            } else if (metaName=="dtb:pageFront") {
                try {
                    this.pageFront = Integer.parseInt(atts.getValue("content"));
                } catch (NumberFormatException e) {
                    this.pageFront = 0;
                }
            } else if (metaName=="dtb:pageNormal") {
                try {
                    this.pageNormal = Integer
                            .parseInt(atts.getValue("content"));
                } catch (NumberFormatException e) {
                    this.pageNormal = 0;
                }
            } else if (metaName=="dtb:pageSpecial") {
                try {
                    this.pageSpecial = Integer.parseInt(atts
                            .getValue("content"));
                } catch (NumberFormatException e) {
                    this.pageSpecial = 0;
                }
            } else if (metaName=="dtb:depth") {
                try {
                    this.depth = new Integer(atts.getValue("content"));
                } catch (NumberFormatException e) {
                    this.depth = null;
                }
            } else if (metaName=="dtb:maxPageNumber") {
                try {
                    this.maxPageNumber = Integer.parseInt(atts
                            .getValue("content"));
                } catch (NumberFormatException e) {
                    this.maxPageNumber = 0;
                }
            }
        }//if (localName.equals("meta"))

        // If this is starting a <navPoint>, bump the current depth. If this is
        // a new high, save it in depthActual
        else if (localName=="navPoint") {
            this.curDepth++;
            if (this.depthActual == null
                    || this.curDepth > this.depthActual.intValue()) {
                this.depthActual = new Integer(this.curDepth);
            }
        }

        //check if its a pageTarget with a value; if so see if its the highest    
        else if (localName=="pageTarget") {
        	if(atts.getValue("value")!=null) {
	            try {            	
		             int current = Integer.parseInt(atts.getValue("value"));
		             if (current > this.maxPageNumberActual) {
		                  this.maxPageNumberActual = current;
		             }            	   
	            } catch (NumberFormatException e) {
	            	super.getZedFileInitEx().addError("NumberFormatException in file " + this.getName() + ": " + e.getMessage());
	            }
        	}else{
        		//pageTarget@value is strongly recommended in section 8.4.5
        		//mg20080218: only expect value attr when type equals normal
        		if(atts.getValue("type")!=null && atts.getValue("type").equals("normal") ) {
        			allPageNormalTargetsHaveValueAttribute = false;
        		}
        	}
        }

        //collect URI values and add to filerefs
        try{
	        if (localName=="content") {
	        	File fl = this.addToFileRefs(this.fileRefs,atts.getValue("src"));
	        	if(fl!=null) { //this was a new kid on the block
	        		this.contentFileRefs.put(fl.getCanonicalPath(), fl);
	        	}
	        } else if (localName=="audio") {
	        	File fl = this.addToFileRefs(this.fileRefs,atts.getValue("src"));
	        	if(fl!=null) { //this was a new kid on the block
	        		this.audioFileRefs.put(fl.getCanonicalPath(), fl);
	        	}
	        } else if (localName=="img") {
	        	File fl = this.addToFileRefs(this.fileRefs,atts.getValue("src"));
	        	if(fl!=null) { //this was a new kid on the block
	        		this.imageFileRefs.put(fl.getCanonicalPath(), fl);
	        	}
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
     * Looks for end of all <navPoint> elements to keep track of actual NCX
     * depth
     * 
     * @param namespaceURI
     *            The namespace URI of the element
     * @param localName
     *            The non-prefixed name of the element
     * @param qName
     *            The qualified name of the element
     */
    public void endElement(String namespaceURI, String localName, String qName) {
        if (localName.equals("navPoint")) {
            this.curDepth--;
        }
    }

    /**
     * @return a List&lt;XmlFileElement&gt; of all NCX elements with local name "smilCustomTest"
     */
    public List<XmlFileElement> getSmilCustomTestElements() {    	
    	return this.customTestElements;
    }

    // toString() for debugging
    public String toString() {
        String s;

        s = super.toString() + "\n\t[uid=" + this.myUid + "]\n";
        // Print declared/actual for all number properties
        s = s + "\t[maxPageNormal=" + this.maxPageNormal + " (actual="
                + this.maxPageNormalActual + ")]\n";
        s = s + "\t[pageFront=" + this.pageFront + " (actual="
                + this.pageFrontActual + ")]\n";
        s = s + "\t[pageNormal=" + this.pageNormal + " (actual="
                + this.pageNormalActual + ")]\n";
        s = s + "\t[pageSpecial=" + this.pageSpecial + " (actual="
                + this.pageSpecialActual + ")]\n";
        s = s + "\t[depth=" + this.depth + " (actual=" + this.depthActual
                + ")]\n";

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
        if (this.contentFileRefs != null) {
            s = s + "\t[contentFileRefs=\n";
            for (File f : contentFileRefs.values()) {
                try {
                    s = s + "\t\titem=" + f.getCanonicalPath() + "\n";
                } catch (IOException e) {
                    s = s + "\t\titem=" + f.getName() + "\n";
                }
            }
            s = s + "]\n";
        } else {
            s = s + "\t[contentFileRefs=null]\n";
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

        return s;
    }
    
    private static final long serialVersionUID = 1367310895349860726L;

	public boolean getAllPageNormalTargetsHaveValueAttribute() {
		return allPageNormalTargetsHaveValueAttribute;
	}
}



//// Look for file references on the appropriate elements
//else if (localName.equals("content")) {
//  try {
//      srcUri = new URI(atts.getValue("src"));
//      srcFilename = srcUri.getPath();
//      if (this.getParent() != null)
//          srcFilename = this.getParentFile().getCanonicalPath()
//                  + File.separator + srcFilename;
//  } catch (URISyntaxException e) {
//      // just ignore exceptions
//      return;
//  } catch (IOException e) {
//      // just ignore exceptions
//      return;
//  }
//
//  f = new File(srcFilename);
//
//  if (this.fileRefs == null)
//      this.fileRefs = new HashMap();
//  if (this.contentFileRefs == null)
//      this.contentFileRefs = new HashMap();
//
//  try {
//      if (this.fileRefs.containsValue(f.getCanonicalPath()) == false) {
//          this.fileRefs.put(f.getCanonicalPath(), f);
//      }
//      if (this.contentFileRefs.containsValue(f.getCanonicalPath()) == false) {
//          this.contentFileRefs.put(f.getCanonicalPath(), f);
//      }
//  } catch (IOException e) {
//      super.getZedFileInitEx().addError(
//              "IOException while accessing file " + srcFilename);
//      return;
//  }
//
//} else if (localName.equals("audio")) {
//  try {
//      srcUri = new URI(atts.getValue("src"));
//      srcFilename = srcUri.getPath();
//      if (this.getParent() != null)
//          srcFilename = this.getParentFile().getCanonicalPath()
//                  + File.separator + srcFilename;
//  } catch (URISyntaxException e) {
//      // just ignore exceptions
//      return;
//  } catch (IOException e) {
//      // just ignore exceptions
//      return;
//  }
//
//  f = new File(srcFilename);
//
//  if (this.fileRefs == null)
//      this.fileRefs = new HashMap();
//  if (this.audioFileRefs == null)
//      this.audioFileRefs = new HashMap();
//
//  try {
//      if (this.fileRefs.containsValue(f.getCanonicalPath()) == false) {
//          this.fileRefs.put(f.getCanonicalPath(), f);
//      }
//      if (this.audioFileRefs.containsValue(f.getCanonicalPath()) == false) {
//          this.audioFileRefs.put(f.getCanonicalPath(), f);
//      }
//  } catch (IOException e) {
//      super.getZedFileInitEx().addError(
//              "IOException while accessing file " + srcFilename);
//      return;
//  }
//} else if (localName.equals("img")) {
//  try {
//      srcUri = new URI(atts.getValue("src"));
//      srcFilename = srcUri.getPath();
//      if (this.getParent() != null)
//          srcFilename = this.getParentFile().getCanonicalPath()
//                  + File.separator + srcFilename;
//  } catch (URISyntaxException e) {
//      // just ignore exceptions
//      return;
//  } catch (IOException e) {
//      // just ignore exceptions
//      return;
//  }
//
//  f = new File(srcFilename);
//
//  if (this.fileRefs == null)
//      this.fileRefs = new HashMap();
//  if (this.imageFileRefs == null)
//      this.imageFileRefs = new HashMap();
//
//  try {
//      if (this.fileRefs.containsValue(f.getCanonicalPath()) == false) {
//          this.fileRefs.put(f.getCanonicalPath(), f);
//      }
//      if (this.imageFileRefs.containsValue(f.getCanonicalPath()) == false) {
//          this.imageFileRefs.put(f.getCanonicalPath(), f);
//      }
//  } catch (IOException e) {
//      super.getZedFileInitEx().addError(
//              "IOException while accessing file " + srcFilename);
//      return;
//  }
//
//}


//// TODO remove - this is redone
//// add the element to the elements hashset
//// if it has an element we want to do a customTest on
//// note: extend the if clause below to
//// add other elements   
//
//if (localName.matches("smilCustomTest|audio")) {
//  try {
//      elements.add(new NcxFileElement(atts, localName, namespaceURI));
//  } catch (Exception e) {
//      super.getZedFileInitEx().addError(
//              "Exception while building dtbook elementlist; id value: "
//                      + atts.getValue("id") + e.getMessage());
//      return;
//  }
//}        

