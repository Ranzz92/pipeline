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

// OpfIntegrity.java
/* Versions:
 0.1.0 (12/02/2003)
 0.2.0 (26/03/2003)
 - Added to org.daisy.zedval.zedsuite package
 - Added file extension test
 */
package org.daisy.zedval.zedsuite.v2002;

import org.daisy.zedval.engine.PackageFile;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;

/**
 * Performs package file integrity checks:
 * <dl>
 * <dt>opf_exists</dt>
 * <dd>Package file exists</dd>
 * <dt>opf_isReadable</dt>
 * <dd>Package file is readable</dd>
 * <dt>opf_fileExtn</dt>
 * <dd>Package file has .opf extension</dd>
 * </dl>
 * <p>
 * <em>Note:</em> In default ZedVal application (and probably in others),
 * package file existence test will probably never be executed here. Instead, it
 * will throw an application error earlier on in the process. This test is
 * included here for completeness of the test processor suite.
 * </p>
 * 
 * @author James Pritchett
 */

public class OpfIntegrity extends ZedCustomTest {

	/**
	 * Performs the integrity tests on the package file
	 * 
	 * @param f A PackageFile instance
	 * @throws ZedCustomTestException
	 */
	public void performTest(ZedFile f) throws ZedCustomTestException {
		PackageFile p;

		this.resultDoc = null;		// Clear out the results document

		// If f isn't a PackageFile, this is a fatal error
		try {
            p = (PackageFile) f; // Make our life simpler by removing need for casts
        } catch (ClassCastException castEx) {
            throw new ZedCustomTestException(castEx.getMessage());
        }

		// Test the properties on the PackageFile to evaluate the tests. Note
		// that failure of existence test
		// causes readability test to be skipped (since failure is a foregone
		// conclusion)

		// TEST: Package file exists (opf_exists)
		if (p.exists() == false) {
		    super.addTestFailure("opf_exists","Package file "+ p.getName() + " does not exist", null, null);
		}
		// TEST: Package file is readable (opf_isReadable)
		else if (p.canRead() == false) {
		    super.addTestFailure("opf_isReadable","Package file "+ p.getName() + " is not readable", null, null);
		}
		// TEST: Package file has .opf extension (opf_fileExtn)
		else if (p.getName().matches(".*\\.[oO][pP][fF]") == false) {
		    super.addTestFailure("opf_fileExtn","Package file "+ p.getName() + " does not use .opf extension", null, null);
		}

	}

}
