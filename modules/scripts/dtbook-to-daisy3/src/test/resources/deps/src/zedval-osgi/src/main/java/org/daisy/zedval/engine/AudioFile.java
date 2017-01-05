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

import org.daisy.util.xml.SmilClock;

/**
 * An <code>AudioFile</code> object represents a single DTB audio file
 * @author James Pritchett
 */
public class AudioFile extends ManifestFile {
    
    /**
     * @param id
     *            id from package file manifest
     * @param fullPath
     *            File path (relative to package file)
     * @param mimeType
     *            File MIME type as given in package manifest
     */
    public AudioFile(String fullPath, String id, String mimeType, String manifestURI) {
        super(fullPath, id, mimeType,manifestURI);
        matchesFormat = false; // These are set in the subclasses
    }

    /**
     * Is the file in the declared format?
     * 
     * @return <code>true</code> if so
     */
    public boolean doesMatchFormat() {
        return this.matchesFormat;
    }

    /**
     * Sets the matchesFormat flag
     * 
     * @param val
     *            Value to set
     */
    protected void setMatchesFormat(boolean val) {
        this.matchesFormat = val;
    }

    /**
     * Returns duration of the audio file
     * 
     * @return duration of the file as SmilClock
     */
    public SmilClock getDuration() {
        return this.duration;
    }

    /**
     * Sets the duration value
     * 
     * @param val
     *            SmilClock expressing duration value
     */
    protected void setDuration(SmilClock val) {
        this.duration = val;
    }

    /**
     * A do-nothing initialize for the generic AudioFile
     */
	public void initialize() throws ZedFileInitializationException {
    }

    private boolean matchesFormat;

    private SmilClock duration;
    
    private static final long serialVersionUID = 8892508571049153494L;
}
