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

// SpineIntegrity.java
/* Versions:
 1.0.1 (26 Oct 2004)
 - Added dtb:totalTime test (opf_xMetaTotTimeAccurate)

 0.2.0 (28/03/2003) [initial]
 30 Jun 2005 (JP)
 - Changed from implementation to extension of ZedCustomTest
 - performTest no longer returns value
 - Removed ErrorListener
 - Discovers incorrect parameter type through CastClass exception
 - Removed all DOM calls (now handled in ZedCustomTest)
 */
package org.daisy.zedval.zedsuite.v2002;

import org.daisy.util.xml.SmilClock;
import org.daisy.zedval.engine.ManifestFile;
import org.daisy.zedval.engine.PackageFile;
import org.daisy.zedval.engine.SmilFile;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;

/**
 * Performs package file spine integrity checks:
 * <dl>
 * <dt>opf_SpineRefAllSmilInMnf</dt>
 * <dd>Each smil file listed in manifest is referenced by spine</dd>
 * <dt>opf_spnItemRefIdrefSmil</dt>
 * <dd>spine itemref idref attributes points to the id of a SMIL file listed in
 * manifest</dd>
 * <dt>opf_xMetaTotTimeAccurate</dt>
 * <dd>x-metadata dtb:totalTime value is the sum of the durations of all SMIL
 * files in the spine</dd>
 * </dl>
 * 
 * @author James Pritchett
 */

public class SpineIntegrity extends ZedCustomTest {

	/**
	 * Performs the spine integrity tests on the package file
	 * 
	 * @param f
	 *            A PackageFile instance
	 */
	public void performTest(ZedFile f) throws ZedCustomTestException {
		PackageFile p;
		long actualTotalTime;
		SmilClock actualTime;

		this.resultDoc = null;		// Clear out the results document

		// If f isn't a PackageFile, this is a fatal error
		try {
			p = (PackageFile) f; // Make our life simpler by removing need for casts
        } catch (ClassCastException castEx) {
            throw new ZedCustomTestException(castEx.getMessage());
        }

		// TEST: Each smil file listed in manifest is referenced by spine
		// Iterate through the SMIL file collection and test to see if it is
		// also in the spine
		if (p.getSmilFiles() != null) {
			for (SmilFile sf : p.getSmilFiles().values()) {
				if (p.getSpine() != null
						&& p.getSpine().containsKey(sf.getAbsolutePath()) == false) {
					super.addTestFailure("opf_SpineRefAllSmilInMnf","SMIL file "
							+ sf.getName() + " not listed in spine",sf.getName(),null);
				}
			}
		}

		// TEST: Each spine itemref idref attributes points to the id of a SMIL
		// file listed in manifest
		// Iterate through the spine and test to see if each item is, in fact, a
		// SMIL file
		// TEST: x-metadata dtb:totalTime value is the sum of the durations of
		// all SMIL files in the spine
		// Iterate through the spine, add up the expectedDurations, and compare
		// to the totalTime metadata
		if (p.getSpine() != null) {
			actualTotalTime = 0;
			for (ManifestFile mf : p.getSpine().values()) {
				if (mf instanceof SmilFile) {
					SmilFile sf = (SmilFile) mf;
					try{
					actualTotalTime += sf.expectedDuration().millisecondsValue();
					}catch (NullPointerException npe) {
						//TODO if a smilfile is malformed we end up here						
					}
				} else {
					super.addTestFailure("opf_spnItemRefIdrefSmil","Spine itemref "
							+ mf.getId() + " references non-SMIL file "
							+ mf.getName(),mf.getName(),null);
				}
			}
			if(p.getTotalTime()!=null) { //mg20061018: whilst testing a bookshare DTB
				if (Math.abs(actualTotalTime - p.getTotalTime().millisecondsValue()) > (SmilClock.getTolerance() * p.getSpine().size())) {
						actualTime = new SmilClock(actualTotalTime);
						super.addTestFailure("opf_xMetaTotTimeAccurate","dtb:totalTime = "
							+ p.getTotalTime() + " ; actual total time = "
							+ actualTime,null,null);
				}
			}
		}
	}
}
