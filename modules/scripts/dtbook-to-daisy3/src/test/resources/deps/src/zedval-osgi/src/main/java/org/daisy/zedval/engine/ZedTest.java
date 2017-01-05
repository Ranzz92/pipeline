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

import java.util.Map;

/**
 * A <code>ZedTest</code> object represents a single validation test from the
 * testMap.
 * 
 * @author James Pritchett
 */
public class ZedTest {
    /**
     * @param id
     *            id from testMap document
     * @param description
     *            description of test
     * @param type
     *            test type (REQUIREMENT or RECOMMENDATION)
     * @param onFalseMsgs
     *            ZedMessages to print on failure
     * @param specRef
     *            URL of specification reference
     */
	public ZedTest(String id, String description, int type,
            Map<String,ZedMessage> onFalseMsgs, String specRef) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.onFalseMsgs = onFalseMsgs;
        this.specRef = specRef;
    }

    /**
     * Returns id of test from testMap document
     * 
     * @return id of test from testMap document
     */
    public String getId() {
        return id;
    }

    /**
     * Returns a description of test
     * 
     * @return a description of test
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the type of test (requirement or recommendation)
     * 
     * @return the type code (REQUIREMENT or RECOMMENDATION)
     */
    public int getType() {
        return type;
    }

    /**
     * Returns a HashMap of messages to be used in case of test failure
     * 
     * @return a HashMap of ZedMessage objects
     */
	public Map<String,ZedMessage> getOnFalseMsgs() {
        return onFalseMsgs;
    }

    /**
     * Returns a reference to the applicable section of the Z39.86 specification
     * 
     * @return a reference to the applicable section of the Z39.86 specification
     */
    public String getSpecRef() {
        return specRef;
    }

    /**
     * Sets up a pointer to the ZedTestProcessor that executes this test
     */
    public void setProcessedBy(ZedTestProcessor aTP) {
        this.processedBy = aTP;
    }

    /**
     * Returns the ZedTestProcessor that executes this test
     * 
     * @return the ZedTestProcessor that executes this test
     */
    public ZedTestProcessor getProcessedBy() {
        return processedBy;
    }

	public String toString() {
        String s = getClass().getName() + " [id=" + this.id + " description="
                + this.description;
        switch (this.type) {
        case REQUIREMENT:
            s = s + " type=REQUIREMENT";
            break;
        case RECOMMENDATION:
            s = s + " type=RECOMMENDATION";
            break;
        default:
            s = s + " type=<unknown>";
            break;
        }
        s = s + " specRef=" + this.specRef + "]\n";
        if (this.onFalseMsgs != null) {
            s = s + "\t[onFalseMsgs=\n";
            for (ZedMessage msg : onFalseMsgs.values()) {
            	s = s + "\t\t" + msg + "\n";
				
			}
            s = s + "\t]\n";
        } else {
            s = s + "\t[onFalseMsgs=null]\n";
        }

        return s;
    }

    // Test type codes
    public static final int REQUIREMENT = 1;

    public static final int RECOMMENDATION = 2;

    private String id;

    private String description;

    private int type;

	private Map<String,ZedMessage> onFalseMsgs;

    private String specRef;

    private ZedTestProcessor processedBy;
}
