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

import org.daisy.util.fileset.util.URIStringParser;

/**
 * A <code>ManifestFile</code> object represents a single DTB file that can
 * appear in a package manifest
 * @author James Pritchett
 */

public abstract class ManifestFile extends ZedFile {

	private static final long serialVersionUID = 8825059209475384593L;

	private String manifestURI;
	
    /**
     * @param fullPath
     *            File path (full, including name)
     * @param id
     *            id from package file manifest
     * @param mimeType
     *            File MIME type as given in package manifest
     */
    public ManifestFile(String fullPath, String id, String mimeType, String manifestURI) {
        super(fullPath);
        this.id = id;
        this.mimeType = mimeType;
        this.manifestURI = manifestURI;
    }

    /**
     * Returns the URI of this ZedFile as it appears in manifest.
     */
    public String getManifestURI() {
    	return manifestURI;
    }
    
    void setManifestURI(String manifestURI) {
    	this.manifestURI = manifestURI;
    }
    
    /**
     * Does integrity tests, etc. Defined by subclasses
     */
    abstract void initialize() throws ZedFileInitializationException;

    /**
     * Sets id of file
     * 
     * @param id
     *            ID of file from package manifest
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * Sets MIME type of file
     * 
     * @param mimeType
     *            MIME type value from package manifest
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Sets the parent PackageFile
     * 
     * @param p
     *            PackageFile object whose manifest contains this file
     */
    public void setPackage(PackageFile p) {
        this.myPackage = p;
    }

    /**
     * Returns the PackageFile object whose manifest contains this file
     * 
     * @return PackageFile object
     */
    public PackageFile getPackage() {
        return this.myPackage;
    }

    /**
     * Returns id of this file (from package manifest)
     * 
     * @return Returns id of this file (from package manifest)
     */
    public String getId() {
        return this.id;
    }

    /**
     * Returns MIME type of this file (as declared in package)
     * 
     * @return MIME type of this file (as declared in package)
     */
    public String getMimeType() {
        return this.mimeType;
    }
    
    /**
     * Tests whether the Manifest URI used to reference this file
     * matches the real files case. 
     */
	public boolean matchesCase() {
		String uriFileName = URIStringParser.stripFragment(URIStringParser.stripPath(manifestURI));		
		if(this.getName().equals(uriFileName))
			return true;		
		return false;
	}

    public String toString() {
        return super.toString() + "\n" + "\t[id=" + this.id + "]\n"
                + "\t[mimeType=" + this.mimeType + "]";
    }

    // Mime media type constants
    //note(mg) move to use ZedConstants.java instead of these
    public static final String MIME_PACKAGE = "text/xml";

    public static final String MIME_NCX = "text/xml";

    public static final String MIME_RESOURCE = "text/xml";

    public static final String MIME_SMIL = "application/smil";

    public static final String MIME_TEXT = "text/xml";

    public static final String MIME_AAC = "audio/MP4A-LATM";

    public static final String MIME_MP3 = "audio/mpeg";

    public static final String MIME_WAV = "audio/x-wav";

    public static final String MIME_PNG = "image/png";

    public static final String MIME_JPEG = "image/jpeg";

    public static final String MIME_SVG = "image/svg+xml";

    private String id; // From manifest (?)

    private String mimeType; // From manifest

    private PackageFile myPackage;

}
