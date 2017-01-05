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

// OpfInternaly.java
/* Versions:
 0.2.0 (26/03/2003)
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

import org.daisy.zedval.engine.PackageFile;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;

/**
 * Performs package file internal checks:
 * <dl>
 * <dt>opf_prologPubId</dt>
 * <dd>Package file public identifier check</dd>
 * </dl>
 * 
 * @author James Pritchett
 */

public class OpfInternal extends ZedCustomTest {

	/**
	 * Performs the internal tests on the package file
	 * 
	 * @param f
	 *            A PackageFile instance
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

		// Check out the public identifier that was picked up at parse time.

		// TEST: Package file public identifier equals "+//ISBN
		// 0-9673008-1-9//DTD OEB 1.0.1 Package//EN" (opf_prologPubId)
		if (p.getDoctypePublicId() == null) {
			super.addTestFailure("opf_prologPubId","Package file " + p.getName() + " has no DTD public identifier",null,null);
		} else if (p.getDoctypePublicId().equals("+//ISBN 0-9673008-1-9//DTD OEB 1.0.1 Package//EN") == false) {
			super.addTestFailure("opf_prologPubId","Package file "
					+ p.getName() + " uses incorrect DTD public identifier: "
					+ p.getDoctypePublicId(),null,null);
		}
	}
}