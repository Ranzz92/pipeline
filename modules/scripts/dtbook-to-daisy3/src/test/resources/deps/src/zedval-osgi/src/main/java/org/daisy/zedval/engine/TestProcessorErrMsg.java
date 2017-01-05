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

/**
 * A <code>TestProcessorErrMsg</code> object represents an error message from
 * a ZedTestProcessor
 * 
 * @author James Pritchett
 */
public class TestProcessorErrMsg extends ErrorMessage {
    /**
     * @param details
     *            Any extended details about the message
     * @param lang
     *            The language of the message
     * @param fatal
     *            Is this a fatal error?
     * @param f
     *            The ZedFile that generated the error
     * @param ztp
     *            The ZedTestProcessor that generated the error
     */
    public TestProcessorErrMsg(String details, String lang, boolean fatal,
            ZedFile f, ZedTestProcessor ztp) {
        super(details, lang, fatal);
        this.myFile = f;
        this.processor = ztp;
    }

    /**
     * @param details
     *            Any extended details about the message
     * @param lang
     *            The language of the message
     * @param fatal
     *            Is this a fatal error?
     * @param ztp
     *            The ZedTestProcessor that generated the error
     */
    public TestProcessorErrMsg(String details, String lang, boolean fatal,
            ZedTestProcessor ztp) {
        super(details, lang, fatal);
        this.processor = ztp;
    }

    /**
     * Returns ZedFile that generated the error
     * 
     * @return ZedFile that generated the error
     */
    public ZedFile getFile() {
        return this.myFile;
    }

    /**
     * Returns ZedTestProcessor that generated the error
     * 
     * @return ZedTestProcessor that generated the error
     */
    public ZedTestProcessor getProcessor() {
        return this.processor;
    }

    public String toString() {
        String fileName = null;
        String procName = null;

        if (this.myFile != null) {
            fileName = this.myFile.getName();
        }
        if (this.processor != null) {
            procName = this.processor.getLabel();
        }

        return super.toString() + " [file=" + fileName + " processor='"
                + procName + "'" + "]";
    }

    private ZedFile myFile;

    private ZedTestProcessor processor;

}
