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
 */

public class ZedFileInitializationException extends Exception {
    
	private final List<String> errors = new LinkedList<String>();

    public ZedFileInitializationException() {
        super();
    }

    public ZedFileInitializationException(String errMsg) {
        super(errMsg);
        this.getErrors().add(errMsg);
    }

    
  /**
  * Concatenates all error messages to a single string
  * @see java.lang.Throwable#getMessage()
  */
 public String getMessage() {
     String msg = "";
     if (errors.size() > 0) {
    	 for (String error : errors) {
    		 msg = msg + error + "\n";
		}
     }
     return msg;
 }
    
    /**
     * @return Returns the errors.
     */
    public List<String> getErrors() {
        return errors;
    }
    
    /**
     * @param error The error to set.
     */
    public void addError(String error) {
        this.getErrors().add(error);
    }

    private static final long serialVersionUID = -2692451879197373215L;
}
