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

import org.xml.sax.Attributes;

/**
 * Base class for an object that
 * holds local information on an element in a ZedFile;
 * typically localname, nsuri and attribute values 
 * @see SmilFileElement
 * @see PackageFileElement
 * @see ResourceFileElement
 * @see NcxFileElement 
 * @see TextFileElement 
 * @author mgylling
 */
class ZedFileElement {
    String id = null;
    String localName = null;
    String nsURI = null;
    
  ZedFileElement(String localName, String nsUri, Attributes attrs) {
      this.id = attrs.getValue("id");
      this.localName = localName;
      this.nsURI = nsUri;
  }
  
  /**
   * @return Returns the value of the id attribute, null if not present.
   */
  public String getIdAttrValue() {
      return id;
  }
  
  /**
   * @return Returns the localName of the element.
   */
  public String getLocalName() {
      return localName;
  }

  /**
   * @return Returns the nsURI of the element.
   */
  public String getNsURI() {
      return nsURI;
  }
}
