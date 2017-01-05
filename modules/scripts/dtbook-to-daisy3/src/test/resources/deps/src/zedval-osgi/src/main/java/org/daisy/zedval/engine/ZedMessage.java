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
 * A <code>ZedMessage</code> object represents a ZedVal output message
 * 
 * @author James Pritchett
 */
public class ZedMessage {

    /**
     * @param type
     *            The type of message (uses constants LONG, SHORT, DETAIL)
     * @param text
     *            The text of the message
     * @param lang
     *            The language of the message
     */
    public ZedMessage(int type, String text, String lang) {
        this.type = type;
        this.text = text;
        this.language = lang;
    }

    /**
     * Returns text of the message
     * 
     * @return String representing text of message
     */
    public String getText() {
        return this.text;
    }

    /**
     * Returns language of message
     * 
     * @return String representing language of message using ISO codes
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Returns type of message
     * 
     * @return LONG, SHORT, or DETAIL code
     */
    public int getType() {
        return this.type;
    }

    public String toString() {
        String s;

        s = getClass().getName() + " [";
        switch (this.type) {
        case LONG:
            s = s + "type=LONG";
            break;
        case SHORT:
            s = s + "type=SHORT";
            break;
        case DETAIL:
            s = s + "type=DETAIL";
            break;
        default:
            s = s + "type=<unknown>";
            break;
        }

        return s + " language=" + this.language + " text='" + this.text + "'"
                + "]";
    }

    public static final int LONG = 1;

    public static final int SHORT = 2;

    public static final int DETAIL = 3;

    private int type;

    private String text;

    private String language;
}
