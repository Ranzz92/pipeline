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

// SmilRelations.java
/* Versions:
 0.2.0 (10/04/2003)
 0.3.0 (28/05/2003)
 - Added implementations of smil_srcDtbook, smil_srcAudio, smil_srcImg, smil_hrefSMIL
 - Fixed test ID for totalElapsedTime check
 1.0.0 (20/07/2003)
 - Fixed bug that caused run-time error if package spine contained non-SMIL files (bug #771619)
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

import org.daisy.util.xml.SmilClock;
import org.daisy.zedval.engine.AudioFile;
import org.daisy.zedval.engine.ImageFile;
import org.daisy.zedval.engine.ManifestFile;
import org.daisy.zedval.engine.PackageFile;
import org.daisy.zedval.engine.SmilFile;
import org.daisy.zedval.engine.TextFile;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;

/**
 * Performs SMIL relations checks:
 * <dl>
 * <dt>smil_metaDtbUidValue</dt>
 * <dd>dtb:uid meta element content attribute matches value of package
 * unique-identifier</dd>
 * <dt>smil_metaAudioDtbTotElaAccurate</dt>
 * <dd>dtb:totalElapsedTime meta element content attribute value represents the
 * total time elapsed up to the beginning of this SMIL file</dd>
 * <dt>smil_manifestRefs</dt>
 * <dd>SMIL only references files that are items in manifest</dd>
 * <dt>smil_srcDtbook</dt>
 * <dd>src attribute on text element references dtbook file</dd>
 * <dt>smil_srcAudio</dt>
 * <dd>src attribute on audio element references audio file</dd>
 * <dt>smil_srcImg</dt>
 * <dd>src attribute on img element references image file</dd>
 * <dt>smil_hrefSMIL</dt>
 * <dd>href attribute on the a element points to a SMIL file</dd>
 * </dl>
 * 
 * @author James Pritchett
 */

public class SmilRelations extends ZedCustomTest {

	/**
	 * Performs the relational tests on a SMIL file
	 * 
	 * @param f
	 *            A SmilFile instance
	 */
	public void performTest(ZedFile f) throws ZedCustomTestException {
		SmilFile s;
		PackageFile p;
		long totDur;
		SmilClock expectedElapsedTime;

		this.resultDoc = null;				// Clear the results
		
		// If f isn't a SmilFile, this is a fatal error
		try {
			s = (SmilFile) f; // Make our life simpler by removing need for casts
        } catch (ClassCastException castEx) {
            throw new ZedCustomTestException(castEx.getMessage());
        }

		p = s.getPackage();

		// TEST: dtb:uid value matches package unique-identifier
		// (smil_metaDtbUidValue)
		if (s.getUid() != null && s.getUid().equals(p.getIdentifier()) == false) {
			super.addTestFailure("smil_metaDtbUidValue","dtb:uid value in SMIL file "
					+ s.getName()
					+ " ('"
					+ s.getUid()
					+ "') does not match package file unique identifier ('"
					+ p.getIdentifier() + "')",null,null);
		}

		// TEST: SMIL only references files that are items in manifest
		// (smil_manifestRefs)
		for (File aFileRef : s.getFileRefs().values()) {
			try {
				if (p.getManifest().get(aFileRef.getCanonicalPath()) == null) {
					super.addTestFailure("smil_manifestRefs","SMIL file "
							+ s.getName() + " references non-manifest file "
							+ aFileRef.getName(),aFileRef.getName(),null);
				}
			} catch (IOException e) {
				throw new ZedCustomTestException("IOException: " + e.getMessage());
			}
		}

		// TEST: dtb:totalElapsedTime meta element content attribute value
		// represents the total
		//        time elapsed up to the beginning of this SMIL file
		// (smil_metaDtbTotElaCalc)
		//   This test is done by adding the ACTUAL durations of all SMIL files
		// that occur before
		//   the current file in the spine.

		totDur = 0;
		for (SmilFile spineFile : p.getSpine().values()) {
			try {
				if (spineFile.getName().equals(s.getName())) {
					break; // We're done now
				} 
				totDur += spineFile.expectedDuration().millisecondsValue();				
			} catch (ClassCastException e) {
				// This exception happens if spine contains a non-SMIL file;
				// just ignore
			} catch(NullPointerException npe) {
				//if a smilfile is malformed we end up here
			}
		}
		expectedElapsedTime = new org.daisy.util.xml.SmilClock(totDur);
		if (expectedElapsedTime.equals(s.getTotalElapsedTime()) == false) {
			super.addTestFailure("smil_metaAudioDtbTotElaAccurate","Declared total elapsed time ("
					+ s.getTotalElapsedTime()
					+ ") does not match actual elapsed time ("
					+ expectedElapsedTime + ")",null,null);
		}

		// TEST: src attribute on text element references dtbook file
		if (s.getTextFileRefs() != null) {
			for (File aFileRef : s.getTextFileRefs().values()) {
				try {
					ManifestFile aManifestItem = p.getManifest().get(aFileRef.getCanonicalPath());
					if (aManifestItem != null
							&& !(aManifestItem instanceof TextFile)) {
						super.addTestFailure("smil_srcDtbook","SMIL text element references non-dtbook file "
								+ aFileRef.getName(),aFileRef.getName(),null);
					}
				} catch (IOException e) {
					throw new ZedCustomTestException("IOException: " + e.getMessage());
				}
			}
		}

		// TEST: src attribute on audio element references audio file
		if (s.getAudioFileRefs() != null) {
			for (File aFileRef : s.getAudioFileRefs().values()) {
				try {
					ManifestFile aManifestItem = p.getManifest().get(aFileRef.getCanonicalPath());
					if (aManifestItem != null
							&& !(aManifestItem instanceof AudioFile)) {
						super.addTestFailure("smil_srcAudio","SMIL audio element references non-audio file "
								+ aFileRef.getName(),aFileRef.getName(),null);
					}
				} catch (IOException e) {
					throw new ZedCustomTestException("IOException: " + e.getMessage());
				}
			}
		}

		// TEST: src attribute on img element references image file
		if (s.getImageFileRefs() != null) {
			for (File aFileRef : s.getImageFileRefs().values()) {
				try {
					ManifestFile aManifestItem = p.getManifest().get(aFileRef.getCanonicalPath());
					if (aManifestItem != null
							&& !(aManifestItem instanceof ImageFile)) {
						super.addTestFailure("smil_srcImg","SMIL img element references non-image file "
								+ aFileRef.getName(),aFileRef.getName(),null);
					}
				} catch (IOException e) {
					throw new ZedCustomTestException("IOException: " + e.getMessage());
				}
			}
		}

		// TEST: href attribute on the a element points to a SMIL file
		if (s.getLinkFileRefs() != null) {
			for (File aFileRef : s.getLinkFileRefs().values()) {
				try {
					ManifestFile aManifestItem = p.getManifest().get(aFileRef.getCanonicalPath());
					if (aManifestItem != null
							&& !(aManifestItem instanceof SmilFile)) {
						super.addTestFailure("smil_hrefSMIL","SMIL a element references non-SMIL file "
								+ aFileRef.getName(),aFileRef.getName(),null);
					}
				} catch (IOException e) {
					throw new ZedCustomTestException("IOException: " + e.getMessage());
				}
			}
		}
	}
}
