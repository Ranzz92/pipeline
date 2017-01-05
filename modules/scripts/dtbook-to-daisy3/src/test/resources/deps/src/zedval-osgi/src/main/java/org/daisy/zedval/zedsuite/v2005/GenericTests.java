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


package org.daisy.zedval.zedsuite.v2005;

import java.io.File;
import java.net.URI;

import org.daisy.zedval.engine.PackageFile;

/**
 * Exposes logic for customtests whose execution does 
 * not differ between filetypes
 * 
 * @author Markus Gylling
 */
public final class GenericTests {

    /**
     * Returns true if the filename complies to the ascii subset defined by
     * z3986-2005.html#Allowed-Char
     */
    static boolean hasValidName(File file) {
        return file.getName().matches("[A-Za-z0-9_.-]+");
    }

    /**
     * Returns true if the file path (relative to opf) complies to the
     * foldername ascii subset defined by z3986-2005.html#Allowed-Char Note:
     * this test is done on resolved pathspecs
     */
    static boolean hasValidRelativePath(File file, PackageFile pkg) throws Exception {

    	//mg20081215 rewritten for #2430796
    	URI relative = pkg.getParentFile().toURI()
    		.relativize(file.getParentFile().toURI());
    	
    	String path = relative.toString().replace("/", "");
    	if(path.length()>0) {
    		 return path.matches("[A-Za-z0-9_-]+");
    	} //else file is in same dir as pkg        	
        	
        return true;
    }
}
