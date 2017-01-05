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

// SmilInternal.java
/* Versions:
 0.2.0 (10/04/2003)
 1.0.0 (25/06/2003)
 - Changed detail message when null public ID detected (bug #755770)
  30 Jun 2005 (JP)
 - Changed from implementation to extension of ZedCustomTest
 - performTest no longer returns value
 - Removed ErrorListener
 - Discovers incorrect parameter type through CastClass exception
 - Removed all DOM calls (now handled in ZedCustomTest)
*/
package org.daisy.zedval.zedsuite.v2002;

import org.daisy.zedval.engine.SmilFile;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;

/**
 * Performs SMIL internal checks:
 * <dl>
 * <dt>smil_prologPubId</dt>
 * <dd>smil file public identifier equals "-//NISO//DTD dtbsmil v1.1.0//EN"
 * </dd>
 * </dl>
 * 
 * @author James Pritchett
 */

public class SmilInternal extends ZedCustomTest {

	/**
	 * Performs the internal tests on a SMIL file
	 * 
	 * @param f
	 *            A SmilFile instance
	 */
	public void performTest(ZedFile f) throws ZedCustomTestException {
		SmilFile s;

		this.resultDoc = null;		// Clear out the results document

		// If f isn't a SmilFile, this is a fatal error
		try {
			s = (SmilFile) f; // Make our life simpler by removing need for casts
        } catch (ClassCastException castEx) {
            throw new ZedCustomTestException(castEx.getMessage());
        }

		// Check out the public identifier that was picked up at parse time.

		// TEST: smil file public identifier equals "-//NISO//DTD dtbsmil
		// v1.1.0//EN" (smil_prologPubId)
		if (s.getDoctypePublicId() == null) {
			super.addTestFailure("smil_prologPubId","SMIL file "
					+ s.getName() + " has no DTD public identifier",null,null);
		} else if (s.getDoctypePublicId().equals("-//NISO//DTD dtbsmil v1.1.0//EN") == false) {
			super.addTestFailure("smil_prologPubId","SMIL file "
					+ s.getName() + " uses incorrect DTD public identifier: "
					+ s.getDoctypePublicId(),null,null);
		}
	}
}