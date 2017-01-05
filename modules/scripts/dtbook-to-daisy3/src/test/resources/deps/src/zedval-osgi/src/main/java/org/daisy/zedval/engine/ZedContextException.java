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

import java.util.LinkedList;
import java.util.List;

/**
 * @author Piotr Kiernicki
 * @author James Pritchett
 */

public class ZedContextException extends Exception {

    
	private List<String> errors = new LinkedList<String>();

    private boolean recoverable = true;

    public ZedContextException() {
        super();
    }

    public ZedContextException(String errMsg) {
        super(errMsg);
        this.getErrors().add(errMsg);
    }

    /**
     * Concatenates all error messages to a single string
     * 
     * @see java.lang.Throwable#getMessage()
     */
    public String getMessage() {
        String msg = "";

        if (this.getErrors().size() > 0) {
        	for (String error : this.getErrors()) {
        		msg = msg + error + "\n";
			}
        }
        return msg;
    }

    /**
     * Can we can recover from this exception?
     * 
     * @return Value of recoverable flag
     */
    public boolean isRecoverable() {
        return recoverable;
    }

    /**
     * Sets recoverable flag
     * 
     * @param b
     *            Value for recoverable flag
     */
    public void setRecoverable(boolean b) {
        recoverable = b;
    }

    /**
     * @return Returns the errors.
     */
    public List<String> getErrors() {
        return errors;
    }

    /**
     * @param error
     *            The error to set.
     */
    public void addError(String error) {
        this.getErrors().add(error);
    }
    
    private static final long serialVersionUID = 3036591766425846918L;
}
