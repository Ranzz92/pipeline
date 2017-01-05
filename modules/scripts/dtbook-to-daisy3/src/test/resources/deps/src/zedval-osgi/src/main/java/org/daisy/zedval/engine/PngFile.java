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

package org.daisy.zedval.engine;

import java.io.FileInputStream;

/**
 * An <code>PngFile</code> object represents a single DTB PNG image file
 * 
 * @author James Pritchett
 */
public class PngFile extends ImageFile {

    /**
     * @param fullPath
     *            File path
     * @param id
     *            id from package file manifest
     * @param mimeType
     *            File MIME type as given in package manifest
     */
    public PngFile(String fullPath, String id, String mimeType, String manifestURI) {
        super(fullPath, id, mimeType, manifestURI);
    }

    /**
     * Does all basic integrity tests (existence, readability, format) and sets
     * properties
     * 
     * @throws ZedFileInitializationException
     */
    public void initialize() throws ZedFileInitializationException {
        FileInputStream fis = null;
        byte[] header = new byte[8]; // Where the header will go for testing
        int readResult = 0;

        // If the file exists and is readable, grab the first 10 bytes and check
        // them for JPEG signature
        if (this.exists() && this.canRead()) {
            try {
                fis = new FileInputStream(this);
                readResult = fis.read(header, 0, 8);
            } catch (Exception e) {
                this.matchesFormat = false;
                throw new ZedFileInitializationException(
                        "WARNING! Exception when trying to read header from PNG file "
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

            /*
             * PNG signature: First 8 bytes are: 0x89 0x50 0x4e 0x47 0x0d 0x0a
             * 0x1a 0x0a
             */
            if (header[0] == (byte) 0x89 && header[1] == (byte) 0x50
                    && header[2] == (byte) 0x4e && header[3] == (byte) 0x47
                    && header[4] == (byte) 0x0d && header[5] == (byte) 0x0a
                    && header[6] == (byte) 0x1a && header[7] == (byte) 0x0a) {
                this.matchesFormat = true;
            } else {
                this.matchesFormat = false;
            }
        }
    }

    private static final long serialVersionUID = -7634499868710138066L;
}
