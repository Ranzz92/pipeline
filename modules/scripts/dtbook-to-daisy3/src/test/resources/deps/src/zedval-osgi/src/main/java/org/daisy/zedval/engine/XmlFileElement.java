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

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;

/**
 * An XML Element descriptor,
 * similar to the javax.xml.stream.StartElement, 
 * but with less footprint.
 * @author Markus Gylling
 */
public class XmlFileElement {
	private String qn = null;
	private String lcl = null;
	private String pfx = null;
	private String ns = null;
	
	private Map<String,XmlFileAttribute> attributes = new HashMap<String, XmlFileAttribute>(); //<localName,XmlFileAttribute>
		
	/**
	 * 
     * @param qName a possibly prefixed version of the XML element name, cannot be null
	 * @param localName local name of the element, cannot be null
	 * @param nsURI , can be null
	 * @param attrs an org.xml.sax.Attributes object
	 */
	public XmlFileElement(String qName, String localName, String nsURI, Attributes attrs) {
		this.qn = qName;
		this.lcl = localName;
		this.ns = nsURI;
		for (int i = 0; i < attrs.getLength(); i++) {
			attributes.put(attrs.getLocalName(i),new XmlFileAttribute(
			attrs.getLocalName(i),
			attrs.getQName(i),
			attrs.getURI(i),
			attrs.getValue(i),
			attrs.getType(i)));
		}				
	}
	
	/**
	 * @return the qname (=possibly prefixed) version of the XML element name
	 */
	public String getQName(){
		return this.qn;
	}

	/**
	 * @return the prefix part of the of the XML element qname 
	 */
	public String getPrefix(){
		if(this.pfx==null) {
			this.pfx = getPfx(this.qn);
		}
		return this.pfx;
	}
	
	/**
	 * @return the local part of the of the XML element qname 
	 */
	public String getLocalName(){
		return this.lcl;
	}
	
	/**
	 * @return the namespace URI of the of the XML element; null if namespace context is null 
	 */
	public String getNsURI(){
		return this.ns;
	}
	
	/**
	 * @param qName a possibly prefixed attribute name
	 * @return the attribute value if attribute qname exists, null otherwise 
	 */
	public String getAttributeValueQ(String qName) {
		for (XmlFileAttribute a : attributes.values()) {
			if(a.getQName().equals(qName)) {
				return a.getValue();
			}			
		}
		return null;
	}

	/**
	 * @param localName the local part of an attribute qname
	 * @return the attribute value if attribute localname exists, null otherwise 
	 */
	public String getAttributeValueL(String localName) {
		XmlFileAttribute a = (XmlFileAttribute)attributes.get(localName);
		if (a!=null) return a.getValue(); 
		return null; 
	}

	/**
	 * @param nsURI the namespace URI of an attribute
	 * @param localName the local part of an attribute qname
	 * @return the attribute value if attribute localname exists, null otherwise 
	 */
	
	public String getAttributeValueNS(String nsURI, String localName) {
		XmlFileAttribute a = (XmlFileAttribute) attributes.get(localName);
		if (a!=null && a.getNsURI().equals("nsURI")) return a.getValue();
		return null;		
	}
	
    /**
     * @return the prefix part of a xml name string, null if no prefix part
     */
    private static StringBuffer sb = new StringBuffer();
    private String getPfx(String str){    	
    	sb.delete(0,sb.length());    	
    	for (int i = 0; i < str.length(); i++) {
    		char ch = str.charAt(i);
    		if(ch==':') return sb.toString();
    		sb.append(ch);				
		}
    	return null;    		    	    	
    }
 
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append('{');
    	sb.append(this.ns);
    	sb.append('}');
    	sb.append(this.lcl);
    	sb.append(' ');
    	for(Object o : attributes.values()) {
    		XmlFileAttribute a = (XmlFileAttribute) o;
    		sb.append('@');
    		sb.append(a.getQName());
    		sb.append("='");
    		sb.append(a.getValue());
    		sb.append('\'');
    		sb.append(' ');
    	}
    	return sb.toString();
    }
}
