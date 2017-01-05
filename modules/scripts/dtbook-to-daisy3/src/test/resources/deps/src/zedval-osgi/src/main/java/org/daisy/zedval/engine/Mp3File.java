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
import java.io.FileNotFoundException;
import java.io.IOException;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Header;

import org.daisy.util.xml.SmilClock;

/**
 * An <code>Mp3File</code> object represents a single DTB MPEG-1/2 Layer III
 * (MP3) audio file
 * 
 * @author James Pritchett
 * @author Daniel Carlsson
 * @author Edmar Schut
 * @author Markus Gylling
 */
public class Mp3File extends org.daisy.zedval.engine.AudioFile {

	private FileInputStream fis = null;
	private Bitstream bts = null;
	private Header header = null;
	private int version;
	private int layer;
	private boolean vbr = false;

	//private float durationmillis;

	/**
	 * @param id
	 *            id from package file manifest
	 * @param fullPath
	 *            File path
	 * @param mimeType
	 *            File MIME type as given in package manifest
	 */

	public Mp3File(String fullPath, String id,
			String mimeType, String manifestURI) {
		super(fullPath, id, mimeType, manifestURI);
	}

	/**
	 * Does all basic integrity tests and sets properties
	 */
	public void initialize() throws ZedFileInitializationException {
		if (this.exists() && this.canRead()) {
			try {
				// set to -1 to make sure the value is actually set (one
				// possible return value is 0)
				try{
					this.layer = -1;
					fis = new FileInputStream(this.getCanonicalPath());
					bts = new Bitstream(fis);
					header = bts.readFrame();
					this.version = header.version();
					this.layer = header.layer();		
					this.vbr = header.vbr();
				} catch (Exception e){
					//if something went really wrong
					//AudioFile.matchesFormat() is still false
					fis.close();
					bts.close();
					return;
				}
				
				if (this.isVBR()) {
					//let JL1.0 do its best...
					this.setDuration(new SmilClock((long)(header.total_ms(fis.available()))));
				}else{
					//get value manually (JL1.0 durationmillis is always some millis too much... even if no id3)
					//TODO subtract id3.length() from this.length() 					
					this.setDuration(new SmilClock(((double)(this.length())/(header.bitrate()/8))));
				}

				//System.err.println(this.getName() + ": " + this.getDuration().toString(SmilClock.FULL));
				
				fis.close();
				bts.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (BitstreamException e) {
				e.printStackTrace();
			}
		}

		/*
		 * return from javalayer header: version property; MPEG1 1 MPEG2_LSF 0
		 * MPEG25_LSF 2 layer property: just an int
		 */

		if (this.version > -1 && this.layer == 3) {
			this.setMatchesFormat(true);			
		}
	}

	public boolean isVBR() {
		return this.vbr;
	}
	
	private static final long serialVersionUID = -7655272819997347888L;
}
