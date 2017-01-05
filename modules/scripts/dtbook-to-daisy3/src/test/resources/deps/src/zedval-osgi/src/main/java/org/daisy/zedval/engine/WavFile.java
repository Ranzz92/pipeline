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

import java.io.IOException;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.daisy.util.xml.SmilClock;

/**
 * A <code>WavFile</code> object represents a single DTB linear PCM-RIFF WAVE
 * audio file
 * 
 * @author James Pritchett
 * @author Daniel Carlsson
 */
public class WavFile extends AudioFile {

    /**
     * @param id
     *            id from package file manifest
     * @param fullPath
     *            File path
     * @param mimeType
     *            File MIME type as given in package manifest
     */
    public WavFile(String fullPath, String id, String mimeType, String manifestURI) {
        super(fullPath, id, mimeType, manifestURI);
    }

    /**
     * Does all basic integrity tests and sets properties
     * 
     * @throws IOException
     * @throws ZedFileInitializationException
     * @throws ZedFileInitializationException
     */
    public void initialize() throws ZedFileInitializationException {
        /*
         * If exists and readable: Check format and set matchesFormat Calculate
         * total duration and set duration
         */
        AudioFileFormat audioFileFormat;
        AudioFormat audioFormat;

        if (this.exists() == false) {
            // Can't find file so machesFormat remains false
            // System.err.println("File not found");
        } else if (this.canRead() == false) {
            // Can't read file so machesFormat remains false
            // System.err.println("File is not readable");
        } else {

            try {
                audioFileFormat = AudioSystem.getAudioFileFormat(this);
                audioFormat = audioFileFormat.getFormat();

                if (audioFileFormat.getType().toString() == "WAVE") {

                    // Set machesFormat
                    this.setMatchesFormat(true);

                    // Duration if file matches format
                    setDuration(new SmilClock(
                             ((double) audioFileFormat.getByteLength() / (audioFormat
                                    .getSampleRate() * (audioFormat
                                    .getSampleSizeInBits() / 8 * audioFormat
                                    .getChannels())))));
                }

            } catch (UnsupportedAudioFileException ex) {
                throw new ZedFileInitializationException(
                        "Intitialization error in " + this.getAbsolutePath()
                                + ", " + ex.getMessage());
            } catch (IOException ex) {
                throw new ZedFileInitializationException(
                        "Intitialization error in " + this.getAbsolutePath()
                                + ", " + ex.getMessage());
            }

        }

    }
    
    private static final long serialVersionUID = -5008064332826719713L;
}
