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

// ResIntegrity.java
/* Versions:
 0.3.0 (11/05/2003)
 
 30 Jun 2005 (JP)
 - Changed from implementation to extension of ZedCustomTest
 - performTest no longer returns value
 - Removed ErrorListener
 - Discovers incorrect parameter type through CastClass exception
 - Removed all DOM calls (now handled in ZedCustomTest)
 */
package org.daisy.zedval.zedsuite.v2002;

import org.daisy.zedval.engine.ResourceFile;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;

/**
 * Performs resource file integrity checks:
 * <dl>
 * <dt>res_fileExtn</dt>
 * <dd>Resource file has .res extension</dd>
 * </dl>
 * 
 * @author James Pritchett
 */

public class ResIntegrity extends ZedCustomTest {

	/**
	 * Performs the integrity tests on the resource file
	 * 
	 * @param f
	 *            A ResouceFile instance
	 */
	public void performTest(ZedFile f) throws ZedCustomTestException {
		ResourceFile r;

		this.resultDoc = null;		// Clear out the results document

		// If f isn't a ResourceFile, this is a fatal error
		try {
			r = (ResourceFile) f; // Make our life simpler by removing need for casts
        } catch (ClassCastException castEx) {
            throw new ZedCustomTestException(castEx.getMessage());
        }

		// TEST: Resource file has .res extension (res_fileExtn)
		if (r.getName().matches(".*\\.[rR][eE][sS]") == false) {
			super.addTestFailure("res_fileExtn","Resource file "
					+ r.getName() + " does not use .res extension",null,null);
		}
	}
}
