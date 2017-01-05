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

// ImageTests.java
/* Versions:
 0.2.0 (10/04/2003)
 */
package org.daisy.zedval.zedsuite.v2002;

import org.daisy.zedval.engine.ImageFile;
import org.daisy.zedval.engine.JpegFile;
import org.daisy.zedval.engine.PngFile;
import org.daisy.zedval.engine.SvgFile;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;

/**
 * Performs all image file tests
 * <dl>
 * <dt>img_fileExtn</dt>
 * <dd>image file has legal file extension</dd>
 * <dt>img_format</dt>
 * <dd>image file format matches extension</dd>
 * </dl>
 * 
 * @author James Pritchett
 */

public class ImageTests extends ZedCustomTest {

	/**
	 * Performs the integrity tests on an image file
	 * 
	 * @param f An ImageFile instance
	 * @throws ZedCustomTestException
	 */
	public void performTest(ZedFile f) throws ZedCustomTestException {
		ImageFile i = null;

		this.resultDoc = null;		// Clear out the results document
		// If f isn't an ImageFile, this is a fatal error
		try {
            i = (ImageFile) f; // Make our life simpler by removing need for casts
        } catch (ClassCastException castEx) {
            throw new ZedCustomTestException(castEx.getMessage());
        }

		// TEST: image file has legal file extension (img_fileExtn)
		if (i.getName().matches(".*\\.[jJ][pP][gG]") == false
				&& i.getName().matches(".*\\.[pP][nN][gG]") == false
				&& i.getName().matches(".*\\.[sS][vV][gG]") == false) {
		    super.addTestFailure("img_fileExtn","Unknown extension for image file "+ i.getName(), null, null);
		}

		// TEST: image file format matches extension (img_format)
		//if (i.expectedMimeType() != null && i.doesMatchFormat() == false) {
        if (i.doesMatchFormat() == false) {
		    if (i instanceof JpegFile) {
		        super.addTestFailure("img_format","Not a valid JPEG file", null, null);
			} else if (i instanceof PngFile) {
			    super.addTestFailure("img_format","Not a valid PNG file", null, null);
			}else if (i instanceof SvgFile) {
			    super.addTestFailure("img_format","Not a valid SVG file", null, null);
			}
		}
        //TODO could add validity/wellformedness tests for SvgFile here
	}

}
