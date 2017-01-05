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

// NcxRelations.java
/* Versions:
 0.2.0 (08/04/2003)
 0.3.0 (23/05/2003)
 - Added implementations of ncx_srcSMIL, ncx_srcAudio, ncx_srcImg
 */
package org.daisy.zedval.zedsuite.v2002;

import java.io.File;
import java.io.IOException;

import org.daisy.zedval.engine.AudioFile;
import org.daisy.zedval.engine.ImageFile;
import org.daisy.zedval.engine.ManifestFile;
import org.daisy.zedval.engine.NcxFile;
import org.daisy.zedval.engine.PackageFile;
import org.daisy.zedval.engine.SmilFile;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;

/**
 * Performs NCX relations checks:
 * <dl>
 * <dt>ncx_UidOpfUid</dt>
 * <dd>dtb:uid value matches package unique-identifier</dd>
 * <dt>ncx_manifestRefs</dt>
 * <dd>NCX only references files that are items in manifest</dd>
 * <dt>ncx_srcSMIL</dt>
 * <dd>src attribute on content element references SMIL file</dd>
 * <dt>ncx_srcAudio</dt>
 * <dd>src attribute on audio element references audio file</dd>
 * <dt>ncx_srcImg</dt>
 * <dd>src attribute on img element references image file</dd>
 * </dl>
 * 
 * @author James Pritchett
 */

public class NcxRelations extends ZedCustomTest {

	/**
	 * Performs the relational tests on the NCX
	 * 
	 * @param f An NcxFile instance
	 * @throws ZedCustomTestException
	 */
	public void performTest(ZedFile f) throws ZedCustomTestException {
		NcxFile n;
		PackageFile p;

		this.resultDoc = null;		// Clear out the results document

		// If f isn't an NcxFile, this is a fatal error
		try {
            n = (NcxFile) f; // Make our life simpler by removing need for casts
        } catch (ClassCastException castEx) {
            throw new ZedCustomTestException(castEx.getMessage());
        }
		p = n.getPackage();

		// TEST: dtb:uid value matches package unique-identifier (ncx_UidOpfUid)
		if (n.getUid() != null && n.getUid().equals(p.getIdentifier()) == false) {
		    super.addTestFailure("ncx_UidOpfUid","dtb:uid value in NCX ('"+ n.getUid()+ "') does not match package file unique identifier ('"+ p.getIdentifier() + "')", null, null);
		}

		// TEST: NCX only references files that are items in manifest
		// (ncx_manifestRefs)
		if (n.getFileRefs() != null) {
			for (File aFileRef : n.getFileRefs().values()) {
				try {
					if (p.getManifest().get(aFileRef.getCanonicalPath()) == null) {
					    super.addTestFailure("ncx_manifestRefs","NCX references non-manifest file "+ aFileRef.getName(),aFileRef.getName(), null);
					}
				} catch (IOException e) {
					throw new ZedCustomTestException(e.getMessage());
				}
			}
		}

		// TEST: src attribute on content element references SMIL file
		if (n.getContentFileRefs() != null) {
			for (File aFileRef : n.getContentFileRefs().values()) {
				try {
					ManifestFile aManifestItem = p.getManifest().get(aFileRef.getCanonicalPath());
					if (aManifestItem != null
							&& !(aManifestItem instanceof SmilFile)) {
					    super.addTestFailure("ncx_srcSMIL","NCX content element references non-SMIL file "+ aFileRef.getName(),aFileRef.getName(), null);
					}
				} catch (IOException e) {
				    throw new ZedCustomTestException(e.getMessage());
				}
			}
		}

		// TEST: src attribute on audio element references audio file
		if (n.getAudioFileRefs() != null) {
			for (File aFileRef : n.getAudioFileRefs().values()) {
				try {
					ManifestFile aManifestItem = p.getManifest().get(aFileRef.getCanonicalPath());
					if (aManifestItem != null
							&& (aManifestItem instanceof AudioFile) == false) {
					    super.addTestFailure("ncx_srcAudio","NCX audio element references non-audio file "+ aFileRef.getName(),aFileRef.getName(), null);
					}
				} catch (IOException e) {
				    throw new ZedCustomTestException(e.getMessage());
				}
			}
		}

		// TEST: src attribute on img element references image file
		if (n.getImageFileRefs() != null) {
			for (File aFileRef : n.getImageFileRefs().values()) {
				try {
					ManifestFile aManifestItem = p.getManifest().get(aFileRef.getCanonicalPath());
					if (aManifestItem != null
							&& (aManifestItem instanceof ImageFile) == false) {
					    super.addTestFailure("ncx_srcImg","NCX img element references non-image file "+ aFileRef.getName(),aFileRef.getName(), null);
					}
				} catch (IOException e) {
				    throw new ZedCustomTestException(e.getMessage());
				}
			}
		}

	}

}
