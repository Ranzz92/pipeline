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

// JpegFile.java
/* Versions:
 0.2.0 (11/04/2003)
 */

package org.daisy.zedval.engine;

import java.io.FileInputStream;

/**
 * A <code>JpegFile</code> object represents a single DTB JPEG image file
 * 
 * @author James Pritchett
 */
public class JpegFile extends ImageFile {

    /**
     * @param fullPath
     *            File path
     * @param id
     *            id from package file manifest
     * @param mimeType
     *            File MIME type as given in package manifest
     */
    public JpegFile(String fullPath, String id, String mimeType, String manifestURI) {
        super(fullPath, id, mimeType, manifestURI);
    }

    /**
     * Does all basic integrity tests and sets properties
     * 
     * @throws ZedFileInitializationException
     */
    public void initialize() throws ZedFileInitializationException {
        FileInputStream fis = null;
        byte[] header = new byte[10]; // Where the header will go for testing
        int readResult;

        // If the file exists and is readable, grab the first 10 bytes and check
        // them for JPEG signature
        if (this.exists() && this.canRead()) {
            try {
                fis = new FileInputStream(this);
                readResult = fis.read(header, 0, 10);
            } catch (Exception e) {
                this.matchesFormat = false;
                throw new ZedFileInitializationException(
                        "WARNING! Exception when trying to read header from JPEG file "
                                + this.getName());
            } finally {
                try {
                    fis.close();
                } catch (Exception e) {
                } // Ignore any exceptions here
            }

            // If we reached EOF on the file, just exit now and leave well
            // enough alone
            if (readResult == -1) {
                this.matchesFormat = false;
                return;
            }

            // JPEG signature: First 10 bytes are: 0xff 0xd8 0xff 0xe0 0x?? 0x??
            // 0x4a 0x46 0x49 0x46
            if (header[0] == (byte) 0xff && header[1] == (byte) 0xd8
                    && header[2] == (byte) 0xff && header[3] == (byte) 0xe0
                    && header[6] == (byte) 0x4a && header[7] == (byte) 0x46
                    && header[8] == (byte) 0x49 && header[9] == (byte) 0x46) {
                this.matchesFormat = true;
            } else {
                this.matchesFormat = false;
            }
        }
    }
    
    private static final long serialVersionUID = -5716005546675686533L;
    
//    /**
//     * Returns expected MIME type for this file
//     * 
//     * @return String expressing expected MIME type
//     */
//    public String expectedMimeType() {
//        return ManifestFile.MIME_JPEG;
//    }
}
