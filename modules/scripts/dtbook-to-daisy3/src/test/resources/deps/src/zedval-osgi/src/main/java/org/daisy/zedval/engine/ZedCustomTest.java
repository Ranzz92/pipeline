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

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An object implementing the <code>ZedCustomTest</code> interface executes a
 * test on a ZedFile
 * 
 * @author James Pritchett
 */

abstract public class ZedCustomTest {

    protected Document resultDoc;

    /**
     * Performs the test on a ZedFile
     * @param f
     *            The ZedFile upon which to perform the test
     */
    abstract public void performTest(ZedFile f) throws ZedCustomTestException;

    public void addTestFailure(String testRef, String detailMsg, String badRef, String line) throws ZedCustomTestException {
        if (this.resultDoc == null) {
            try {
                resultDoc = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder().getDOMImplementation()
                        .createDocument("", "testResults", null);
            } catch (ParserConfigurationException e) {
                throw new ZedCustomTestException(
                        "ParserConfigurationException: " + e.getMessage());
            }
        }
        
        Element elem = (Element) resultDoc.getDocumentElement().appendChild(
                resultDoc.createElement("testFailure"));
        elem.setAttribute("testRef", testRef);
        if (line != null) {
            elem.setAttribute("line", line);
        }
        elem = (Element) elem.appendChild(resultDoc.createElement("detail"));
        if (badRef != null) {
            elem.setAttribute("badRef", badRef);
        }
        if (detailMsg==null||detailMsg.equals("")) detailMsg = "--no details--";
        elem.appendChild(resultDoc.createTextNode(detailMsg));
    }

    /**
     * @return Returns the resultDoc.
     */
    public Document getResultDoc() {
        return resultDoc;
    }
}
