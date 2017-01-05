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
import java.util.Map;

import org.daisy.util.text.URIUtils;

/**
 * A <code>ZedFile</code> object represents a single DTB or ZedVal file
 * @author James Pritchett
 * @author Markus Gylling
 */

public abstract class ZedFile extends File {

	private static final long serialVersionUID = -5415529284185237091L;

	/**
     * @param fullPath
     *            File path (full, including name)
     */
    public ZedFile(String fullPath) {
        super(fullPath);        
    }
    
    /**
     * Does integrity tests, etc. Defined by subclasses
     */
    abstract void initialize() throws ZedFileInitializationException;

    public String toString() {
        return getClass().getName() + " name=" + this.getName() + " [exists="
                + this.exists() + " canRead=" + this.canRead() + "]";
    }

    /**
     * Returns the name of the file object This overrides java.io.File.getName()
     * since that method (on windows) will not represent case
     * inconsistencies between constructor string and physical name
     * 
     * @throws IOException
     */
    public String getName() {
        File file;
        try {
            file = new File(this.getCanonicalPath());
        } catch (IOException e) {
            return null;
        }
        return file.getName();
    }
        
    /**
     * a convenience method for those subclasses that carry the fileRefs property
     * @return a File object if the file represented by the URI was added to the map, null otherwise (because already existing)
     * @throws URISyntaxException
     * @throws IOException
     */
	protected File addToFileRefs(Map<String,File> fileRefs, String unresolvedURI) throws URISyntaxException, IOException, IllegalArgumentException{
    	//unresolvedURI = URIStringParser.stripFragment(unresolvedURI);//jre_1.5 specific
        unresolvedURI = this.stripFragment(unresolvedURI);
    	if(unresolvedURI.equals("")) return null; //this was a fragment only URI
    	//mg20081215 when contains uri-disallowed chars, cannot use single arg constructor
    	//URI uri = new URI(unresolvedURI);
    	URI uri = new URI(null,null,null,-1,unresolvedURI,null,null);
    	if (uri.getScheme() == null || uri.getScheme().equals("file") && uri.getPath().equals("") == false) {
    		File f = new File(URIUtils.resolve(this.toURI(), uri).getPath());
            if (!fileRefs.containsValue(f.getCanonicalPath())) {
                fileRefs.put(f.getCanonicalPath(), f);
                return f;
            }
        }    	    	    	    	
    	return null;
    }
    
    protected String stripFragment(String uri) {                
        StringBuffer sb = new StringBuffer();
        int length = uri.length();
        char hash = '#';
        for (int i = 0; i < length; i++) {
            if (uri.charAt(i)==hash) {
                return sb.toString();
            }
            sb.append(uri.charAt(i));           
        }
        return sb.toString();                               
    }
    
}
