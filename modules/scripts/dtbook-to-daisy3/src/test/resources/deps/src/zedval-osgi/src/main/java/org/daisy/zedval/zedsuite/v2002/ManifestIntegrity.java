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

// ManifestIntegrity.java
/* Versions:
 0.1.0 (12/02/2003)
 0.2.0 (28/03/2003)
 - Added to org.daisy.zedval.zedsuite package
 - Added more tests
 */
package org.daisy.zedval.zedsuite.v2002;

import org.daisy.zedval.engine.ManifestFile;
import org.daisy.zedval.engine.PackageFile;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;

/**
 * Performs package file manifest integrity checks:
 * <dl>
 * <dt>opf_mnfIntegrityExists</dt>
 * <dd>All items referenced in manifest exist</dd>
 * <dt>opf_mnfIntegrityReadable</dt>
 * <dd>All items referenced in manifest are readable</dd>
 * <dt>opf_mnfOneSmil</dt>
 * <dd>manifest references at least one SMIL file</dd>
 * <dt>opf_mnfNcxMediaType</dt>
 * <dd>item element for ncx in the manifest must have a media-type attribute of
 * "text/xml"</dd>
 * <dt>opf_mnfOpfMediaType</dt>
 * <dd>item element for package file in the manifest must have a media-type
 * attribute of "text/xml"</dd>
 * <dt>opf_mnfResMediaType</dt>
 * <dd>item element for resource file must, if listed in manifest, have a
 * media-type attribute of "text/xml"</dd>
 * </dl>
 * 
 * @author James Pritchett
 */

public class ManifestIntegrity extends ZedCustomTest {

	/**
	 * Performs the manifest integrity tests on the package file
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
		
		// Iterate through the manifest and evaluate the tests. Note that
		// failure of existence test
		// causes readability test to be skipped (since failure is a foregone
		// conclusion)
		for (ManifestFile mf : p.getManifest().values()) {

			// TEST: Manifest file exists (opf_mnfIntegrityExists)
			if ((mf.exists()&&!mf.matchesCase())||!mf.exists()) {
			    super.addTestFailure("opf_mnfIntegrityExists","Manifest references nonexistent file "+ mf.getName(), mf.getName(), null);
			}
			// TEST: Manifest file is readable (opf_mnfIntegrityReadable)
			else if (mf.canRead() == false) {
			    super.addTestFailure("opf_mnfIntegrityReadable","Manifest references unreadable file "+ mf.getName(), mf.getName(), null);
			}
		}

		// TEST: Manifest references at least one SMIL file
		if (p.getSmilFiles() == null) {
		    super.addTestFailure("opf_mnfOneSmil","Could not detect any SMIL files in manifest", null, null);
		}

		// TEST: NCX has a media-type attribute of "text/xml"
		if (p.getNcx() != null
				&& p.getNcx().getMimeType().equals("text/xml") == false) {
		    super.addTestFailure("opf_mnfNcxMediaType","Invalid MIME type for NCX:  "+ p.getNcx().getMimeType(), null, null);
		}

		// TEST: Package file has a media-type attribute of "text/xml"
		if (p.getMimeType().equals("text/xml") == false) {
		    super.addTestFailure("opf_mnfOpfMediaType","Invalid MIME type for package file:  "+ p.getMimeType(), null, null);
		}

		// TEST: Resource file has a media-type attribute of "text/xml"
		if (p.getResource() != null
				&& p.getResource().getMimeType().equals("text/xml") == false) {
		    super.addTestFailure("opf_mnfResMediaType","Invalid MIME type for resource file:  "+ p.getResource().getMimeType(), null, null);
		}

	}

}
