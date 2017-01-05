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

// AudioTests.java
/* Versions:
 0.2.0 (10/04/2003)
 */
package org.daisy.zedval.zedsuite.v2002;

import org.daisy.zedval.engine.AacFile;
import org.daisy.zedval.engine.AudioFile;
import org.daisy.zedval.engine.Mp3File;
import org.daisy.zedval.engine.WavFile;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;

/**
 * Performs all audio file tests
 * <dl>
 * <dt>audio_fileExtn</dt>
 * <dd>audio file has legal file extension</dd>
 * <dt>audio_format</dt>
 * <dd>audio file format matches extension</dd>
 * </dl>
 * 
 * @author James Pritchett
 */

public class AudioTests extends ZedCustomTest {

	/**
	 * Performs the integrity tests on an audio file
	 * 
	 * @param f An AudioFile instance
	 * @throws ZedCustomTestException
	 */
	public void performTest(ZedFile f) throws ZedCustomTestException {
		AudioFile af = null;
		
		this.resultDoc = null;		// Clear out the results document

        // If f isn't an AudioFile, this is a fatal error
		try {
            af = (AudioFile) f; // Make our life simpler by removing need for casts
        } catch (ClassCastException castEx) {
            throw new ZedCustomTestException(castEx.getMessage());
        }

		// TEST: audio file has legal file extension (audio_fileExtn)
		if (af.getName().matches(".*\\.[aA][aA][cC]") == false
				&& af.getName().matches(".*\\.[mM][pP]3") == false
				&& af.getName().matches(".*\\.[wW][aA][vV]") == false) {
		    super.addTestFailure("audio_fileExtn","Unknown extension for audio file "+ af.getName(), null, null);
		}

		// TEST: audio file format matches extension (audio_format)
		//if (af.expectedMimeType() != null && af.doesMatchFormat() == false) {
        if (af.doesMatchFormat() == false) {			
			if (af instanceof AacFile) {
			    super.addTestFailure("audio_format","Not a valid AAC file", null, null);
			} else if (af instanceof Mp3File) {
			    super.addTestFailure("audio_format","Not a valid MP3 file", null, null);
			}else if (af instanceof WavFile) {
			    super.addTestFailure("audio_format","Not a valid WAV file", null, null);
            }    
//			}else{
//                super.addTestFailure("audio_format","Not a recognize audio type", null, null);
//            }
		}

	}

}
