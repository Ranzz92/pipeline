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

// NcxIntegrity.java
/* Versions:
 0.2.0 (08/04/2003)
 */
package org.daisy.zedval.zedsuite.v2002;

import org.daisy.zedval.engine.NcxFile;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;

/**
 * Performs NCX integrity checks:
 * <dl>
 * <dt>ncx_fileExtn</dt>
 * <dd>NCX file has .ncx extension</dd>
 * </dl>
 * 
 * @author James Pritchett
 */

public class NcxIntegrity extends ZedCustomTest {

	/**
	 * Performs the integrity tests on the NCX
	 * 
	 * @param f An NcxFile instance
	 * @throws ZedCustomTestException
	 */
	public void performTest(ZedFile f) throws ZedCustomTestException {
		NcxFile n;

		this.resultDoc = null;		// Clear out the results document

		// If f isn't an NcxFile, this is a fatal error
		try {
            n = (NcxFile) f; // Make our life simpler by removing need for casts
        } catch (ClassCastException castEx) {
            throw new ZedCustomTestException(castEx.getMessage());
        }
		// Test the properties on the PackageFile to evaluate the tests.

		// TEST: NCX file has .ncx extension (ncx_fileExtn)
		if (n.getName().matches(".*\\.[nN][cC][xX]") == false) {
		    super.addTestFailure("ncx_fileExtn","NCX file " + n.getName()+ " does not use .ncx extension", null, null);
		}

	}

}
