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

//zedXalanExt.java
/* Versions:
 0.2.0 (10/04/2003)
 */
package org.daisy.zedval.zedsuite.v2002;

import java.io.File;

import org.daisy.util.xml.SmilClock;
import org.daisy.zedval.engine.AacFile;
import org.daisy.zedval.engine.AudioFile;
import org.daisy.zedval.engine.Mp3File;
import org.daisy.zedval.engine.WavFile;
import org.daisy.zedval.engine.ZedFileInitializationException;

/**
 * Provides various handy functions to use within XSLT stylesheets
 * 
 * @author James Pritchett
 */
public class zedXalanExt {

	/**
	 * Converts a SMIL clock value into the equivalent number of seconds
	 * 
	 * @param s
	 *            String giving SMIL clock value
	 * @return Double representing the duration in seconds, or -1 if s is not a
	 *         valid SMIL clock value
	 */
	public static Double smilToSec(String s) {
		// Just create a SmilClock instance and get the dur from that
		SmilClock sc;

		try {
			sc = new SmilClock(s);
		} catch (NumberFormatException e) {
			return new Double(-1);
		}
		return new Double(sc.secondsValue());
	}

	/**
	 * Compares two SMIL clock values
	 * 
	 * @param s1
	 *            String giving SMIL clock value
	 * @param s2
	 *            String giving SMIL clock value
	 * @return -1 if s1 < s2; 0 if s1 = s2; 1 if s1 > s2; null on errors
	 */
	public static Integer compareSmil(String s1, String s2) {
		SmilClock sc1, sc2;

		try {
			sc1 = new SmilClock(s1);
			sc2 = new SmilClock(s2);
		} catch (Exception e) {
			return null;
		}
		return new Integer(sc1.compareTo(sc2));
	}

	/**
	 * Tests for file existence
	 * 
	 * @param f
	 *            Name/path of file
	 * @return <code>true</code> if file exists, <code>false</code>
	 *         otherwise
	 */
	public static Boolean fileExists(String f) {
		// Just create a File and test the exists() property
		File myFile;

		myFile = new File(f);
		return new Boolean(myFile.exists());
	}

	/**
	 * Returns duration of an audio file
	 * 
	 * @param f
	 *            Name/path of file
	 * @return String representing total duration of file as a SMIL clock value,
	 *         or null on any error
	 */
	public static String fileDur(String f) {
		AudioFile af;

		if (f.matches(".*\\.[mM][pP]3")) {
			af = new Mp3File(f, "foo", "audio/mpeg", null);
		} else if (f.matches(".*\\.[aA][aA][cC]")) {
			af = new AacFile(f, "foo", "audio/MP4A-LATM", null);
		} else if (f.matches(".*\\.[wW][aA][vV]")) {
			af = new WavFile(f, "foo", "audio/x-wav",null);
		} else {
			return null;
		}
		try {
            af.initialize();
        } catch (ZedFileInitializationException e) {
            return null;
        }
		if (af.exists() && af.getDuration() != null) {
			return af.getDuration().toString();
		} 
		return null;		
	}

}
