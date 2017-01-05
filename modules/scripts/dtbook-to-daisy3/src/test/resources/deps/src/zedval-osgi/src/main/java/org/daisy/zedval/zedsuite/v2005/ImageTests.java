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

import org.daisy.zedval.engine.ImageFile;
import org.daisy.zedval.engine.JpegFile;
import org.daisy.zedval.engine.PngFile;
import org.daisy.zedval.engine.SvgFile;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;

/**
 * Performs custom tests on image files
 * @author mgylling
 * @author jpritchett
 */
public class ImageTests extends ZedCustomTest {
	
	/**
	 * Performs custom tests on image files
	 * @param f An ImageFile instance
	 */
    public void performTest(ZedFile f) throws ZedCustomTestException {
        ImageFile i = null;

        this.resultDoc = null; // Clear out the results document
        // If f isn't an ImageFile, this is a fatal error
        try {
            i = (ImageFile) f; // Make our life simpler by removing need for
                                // casts
        } catch (ClassCastException castEx) {
            throw new ZedCustomTestException(castEx.getMessage());
        }

        // TEST: image file has legal file extension (img_fileExtn)
        boolean match = true;
        if (i instanceof JpegFile) {
            if (!(i.getName().matches(".*\\.[j][p][g]"))) {
                match = false;
            }
        } else if (i instanceof PngFile) {
            if (!(i.getName().matches(".*\\.[p][n][g]"))) {
                match = false;
            }
        } else if (i instanceof SvgFile) {
            if (!(i.getName().matches(".*\\.[s][v][g]"))) {
                match = false;
            }
        }
        if (!match) {
            super.addTestFailure("img_fileExtn",
                    "Incorrect extension for image file " + i.getName(), null,
                    null);
        }

        // TEST: image file has legal name (img_fileName)
        if (!GenericTests.hasValidName(i)) {
            super.addTestFailure("img_fileName", "Image file " + i.getName()
                    + " uses disallowed characters in its name", null, null);
        }

        //TEST: path restriction (img_relPath)
	    try{    
	        if (!GenericTests.hasValidRelativePath(i,i.getPackage())) {
	            super.addTestFailure("img_relPath", "Image file " + i.getName()
	                    + " uses disallowed characters in its path, relative to packagefile", null, null);            
	        }
	    } catch (Exception e) {
	        throw new ZedCustomTestException("Exception: " + e.getMessage());
	    }

        
        // TEST: image file format matches extension (img_format)
        // if (i.expectedMimeType() != null && i.doesMatchFormat() == false) {
        if (i.doesMatchFormat() == false) {
            if (i instanceof JpegFile) {
                super.addTestFailure("img_format", "Not a valid JPEG file",
                        null, null);
            } else if (i instanceof PngFile) {
                super.addTestFailure("img_format", "Not a valid PNG file",
                        null, null);
            } else if (i instanceof SvgFile) {
                super.addTestFailure("img_format", "Not a valid SVG file",
                        null, null);
            }
        }

    }
}
