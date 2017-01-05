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


package org.daisy.zedval.zedsuite.v2005;

import org.daisy.zedval.engine.AacFile;
import org.daisy.zedval.engine.AudioFile;
import org.daisy.zedval.engine.Mp3File;
import org.daisy.zedval.engine.WavFile;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;

/**
 * Performs custom tests on audio files.
 * @author mgylling
 * @author jpritchett
 */
public class AudioTests extends ZedCustomTest {
	
	/**
	 * Performs custom tests on audio files
	 * @param f
	 *            An AudioFile instance
	 */
    public void performTest(ZedFile f) throws ZedCustomTestException {
        AudioFile a = null;

        this.resultDoc = null; // Clear out the results document
        // If f isn't an AudioFile, this is a fatal error
        try {
            a = (AudioFile) f; // Make our life simpler by removing need for
                                // casts
        } catch (ClassCastException castEx) {
            throw new ZedCustomTestException(castEx.getMessage());
        }

        // TEST: audio file has legal file extension (img_fileExtn)
        boolean match = true;
        if (a instanceof Mp3File) {
            if (!(a.getName().matches(".*\\.[m][p][3]"))) {
                match = false;
            }
        } else if (a instanceof AacFile) {
            if (!(a.getName().matches(".*\\.[m][p][4]"))) {
                match = false;
            }
        } else if (a instanceof WavFile) {
            if (!(a.getName().matches(".*\\.[w][a][v]"))) {
                match = false;
            }
        }
        
        if (!match) {
            super.addTestFailure("audio_fileExtn",
                    "Incorrect extension for audio file " + a.getName(), null,
                    null);
        }

        // TEST: image file has legal name (audio_fileName)
        if (!GenericTests.hasValidName(a)) {
            super.addTestFailure("audio_fileName", "Audio file " + a.getName()
                    + " uses disallowed characters in its name", null, null);            
        }
        
        //TEST: path restriction (audio_relPath)
	    try{    
	        if (!GenericTests.hasValidRelativePath(a,a.getPackage())) {
	            super.addTestFailure("audio_relPath", "Audio file " + a.getName()
	                    + " uses disallowed characters in its path, relative to packagefile", null, null);            
	        }
	    } catch (Exception e) {
	        throw new ZedCustomTestException("Exception: " + e.getMessage());
	    }
        
        // TEST: audio file format matches extension (audio_format)
        // if (i.expectedMimeType() != null && i.doesMatchFormat() == false) {
        if (a.doesMatchFormat() == false) {
            if (a instanceof WavFile) {
                super.addTestFailure("audio_format", "Not a valid WAV file",
                        null, null);
            } else if (a instanceof AacFile) {
                super.addTestFailure("audio_format", "Not a valid AAC file",
                        null, null);
            } else if (a instanceof Mp3File) {
                super.addTestFailure("audio_format", "Not a valid Mp3 file",
                        null, null);
            }
        }

        //TEST: mp3 doesnt use VBR (audio_mp3vbr)
        if(f instanceof Mp3File) {
        	Mp3File m = (Mp3File) f;
        	if (m.isVBR()) {
        		super.addTestFailure("audio_mp3vbr",null,null,null);
        	}
        }
        
    }
}
