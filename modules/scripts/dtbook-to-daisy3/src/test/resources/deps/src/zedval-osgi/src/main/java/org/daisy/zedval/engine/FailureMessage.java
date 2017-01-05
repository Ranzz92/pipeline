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
 * A <code>FailureMessage</code> object represents a ZedVal message reporting
 * details of a validation failure
 * 
 * @author James Pritchett
 */
public class FailureMessage extends ZedMessage {

    /**
     * @param details
     *            Extended details about the message
     * @param lang
     *            The language of the message
     * @param line
     *            Line number of input file where error occurred
     * @param column
     *            Column number of input file where error occurred
     * @param t
     *            The ZedTest that failed
     * @param f
     *            The ZedFile that generated the error
     * @param ztp
     *            The ZedTestProcessor that generated the error
     */
    public FailureMessage(String details, String lang, long line, long column,
            ZedTest t, ZedFile f, ZedTestProcessor ztp) {
        super(ZedMessage.DETAIL, details, lang);
        this.line = line;
        this.column = column;
        this.test = t;
        this.myFile = f;
        this.processor = ztp;
    }

    /**
     * @param details
     *            Any extended details about the message
     * @param lang
     *            The language of the message
     * @param line
     *            Line number of input file where error occurred
     * @param column
     *            Column number of input file where error occurred
     * @param f
     *            The ZedFile that generated the error
     * @param ztp
     *            The ZedTestProcessor that generated the error
     */
    public FailureMessage(String details, String lang, long line, long column,
            ZedFile f, ZedTestProcessor ztp) {
        super(ZedMessage.DETAIL, details, lang);
        this.line = line;
        this.column = column;
        this.myFile = f;
        this.processor = ztp;
    }
    
    public FailureMessage(String details, String lang, ZedTest t, long line, long column,
            ZedFile f, ZedTestProcessor ztp) {
        super(ZedMessage.DETAIL, details, lang);
        this.test = t;
        this.line = line;
        this.column = column;
        this.myFile = f;
        this.processor = ztp;
    }
    
    /**
     * @param details
     *            Any extended details about the message
     * @param lang
     *            The language of the message
     * @param t
     *            The ZedTest that failed
     * @param f
     *            The ZedFile that generated the error
     * @param ztp
     *            The ZedTestProcessor that generated the error
     */
    public FailureMessage(String details, String lang, ZedTest t, ZedFile f,
            ZedTestProcessor ztp) {
        super(ZedMessage.DETAIL, details, lang);
        this.test = t;
        this.myFile = f;
        this.processor = ztp;
        this.line = -1;
        this.column = -1;
    }

    /**
     * @param details
     *            Any extended details about the message
     * @param lang
     *            The language of the message
     * @param f
     *            The ZedFile that generated the error
     * @param ztp
     *            The ZedTestProcessor that generated the error
     */
    public FailureMessage(String details, String lang, ZedFile f,
            ZedTestProcessor ztp) {
        super(ZedMessage.DETAIL, details, lang);
        this.myFile = f;
        this.processor = ztp;
        this.line = -1;
        this.column = -1;
    }

    /**
     * Returns line number of source that generated the error
     * 
     * @return line number of source that generated the error
     */
    public long getLine() {
        return this.line;
    }

    /**
     * Returns column number of source that generated the error
     * 
     * @return column number of source that generated the error
     */
    public long getColumn() {
        return this.column;
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
     * Returns ZedTest that failed
     * 
     * @return ZedTest that failed
     */
    public ZedTest getTest() {
        return this.test;
    }

    /**
     * Returns test processor that generated the error
     * 
     * @return test processor that generated the error
     */
    public ZedTestProcessor getProcessor() {
        return this.processor;
    }

    public String toString() {
        String testName = null;
        String fileName = null;
        String procName = null;

        if (this.test != null) {
            testName = this.test.getId();
        }
        if (this.myFile != null) {
            fileName = this.myFile.getName();
        }
        if (this.processor != null) {
            procName = this.processor.getLabel();
        }

        return super.toString() + " [test=" + testName + " file=" + fileName
                + " processor='" + procName + "'"
                + ((line >= 0) ? " line=" + this.line : "")
                + ((column >= 0) ? " column=" + this.column : "") + "]";
    }

    private long line;

    private long column;

    private ZedTest test;

    private ZedFile myFile;

    private ZedTestProcessor processor;
}
