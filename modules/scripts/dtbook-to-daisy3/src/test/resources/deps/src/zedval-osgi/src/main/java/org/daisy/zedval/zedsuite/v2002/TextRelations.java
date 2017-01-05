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

// TextRelations.java
/* Versions:
 0.2.0 (10/04/2003)
 30 Jun 2005 (JP)
 - Changed from implementation to extension of ZedCustomTest
 - performTest no longer returns value
 - Removed ErrorListener
 - Discovers incorrect parameter type through CastClass exception
 - Removed all DOM calls (now handled in ZedCustomTest)
 */
package org.daisy.zedval.zedsuite.v2002;

import java.io.File;
import java.io.IOException;

import org.daisy.zedval.engine.PackageFile;
import org.daisy.zedval.engine.TextFile;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;

/**
 * Performs text file relations checks:
 * <dl>
 * <dt>dtbk_manifestRefs</dt>
 * <dd>dtbook only references files that are items in manifest</dd>
 * </dl>
 * 
 * @author James Pritchett
 */

public class TextRelations extends ZedCustomTest {

	/**
	 * Performs the relational tests on a text file
	 * 
	 * @param f
	 *            A TextFile instance
	 */
	public void performTest(ZedFile f) throws ZedCustomTestException {
		TextFile t;
		PackageFile p;

		this.resultDoc = null;		// Clear out the results document

		// If f isn't a TextFile, this is a fatal error
		try {
			t = (TextFile) f; // Make our life simpler by removing need for casts
        } catch (ClassCastException castEx) {
            throw new ZedCustomTestException(castEx.getMessage());
        }

		p = t.getPackage();

		// TEST: dtbook only references files that are items in manifest
		// (dtbk_manifestRefs)
		if (t.getFileRefs() != null) {
			for (File aFileRef : t.getFileRefs().values()) {
				try {
					if (p.getManifest().get(aFileRef.getCanonicalPath()) == null) {
						super.addTestFailure("dtbk_manifestRefs","Reference to non-manifest file "
								+ aFileRef.getName(),aFileRef.getName(),null);
					}
				} catch (IOException e) {
					throw new ZedCustomTestException("IOException:  " + e.getMessage());
				}
			}
		}
	}
}
