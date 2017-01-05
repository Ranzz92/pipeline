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
 * An object implementing the <code>ZedReporter</code> interface collects,
 * formats, and outputs validation test results
 * 
 * @author James Pritchett
 */
public interface ZedReporter {

    /**
     * Associates a ZedContext with this report
     * 
     * @param c
     *            The ZedContext object for this ZedVal run
     */
    public void setContext(ZedContext c);

    /**
     * Initializes the report
     */
    public void initialize() throws ZedReporterException;

    /**
     * Adds a message for the report output
     */
    public void addMessage(ZedMessage m) throws ZedReporterException;

    /**
     * Closes the report
     */
    public void close() throws ZedReporterException;
}
