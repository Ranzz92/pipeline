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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.daisy.util.xml.XPathUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Represents a Test Map or Processor Map.
 * This class replaces the MapFile object used in ZedVal2002
 * ZedMap does not inherit from the ZedFile hierarchy since
 * maps because of jarness need to be instantiated from InputStream,
 * and since jarness and java.io.File compatibility problems.

 * @author Markus Gylling
 */
public class ZedMap implements ErrorHandler {
    private Document mapDocument;
    private boolean valid = true;
    private String specVersion = null;
    private URL url = null;

    public ZedMap(String path) throws ZedFileInitializationException {
        try {
            initialize(new File(path).toURI().toURL());
        } catch (ZedFileInitializationException e) {
            throw e;
        } catch (MalformedURLException e) {
            throw new ZedFileInitializationException(e.getMessage());
        }
    }
        
    public ZedMap(URL url) throws ZedFileInitializationException {
        initialize(url);
    }
    
    /**
     * Does all basic integrity tests and loads into DOM Document (mapDocument) 
     * @throws ZedFileInitializationException
     */
    private void initialize(URL url) throws ZedFileInitializationException {
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);
            DocumentBuilder parser;
            
            try {
                parser = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                this.valid = false;
                throw new ZedFileInitializationException(
                        "ERROR: Can't set up parser for map file "
                                + this.getName() + ": " + e.getMessage());
            }
            
            parser.setErrorHandler(this);
            
            try {
                //send the string url in to .parse() second param systemId to get entities resolved correctly
                this.mapDocument = parser.parse(url.openConnection().getInputStream(),url.toString());                                                                                                
                this.specVersion = this.mapDocument.getDocumentElement().getAttribute("specVersion");
                if(this.mapDocument.getDocumentElement().getNodeName().equals("zedProcessorMap")){
                	normalizeProcMap();
                }
                                
                
            } catch (SAXException e) {
                this.valid = false;
                String line = "";
                if (e instanceof SAXParseException) {
                    // get the linenumber
                    SAXParseException spe = (SAXParseException) e;
                    try {
                        line = "at line: " + spe.getLineNumber();
                    } catch (Exception ex) {

                    }
                }
                throw new ZedFileInitializationException(
                        "ERROR: Can't parse map file " + this.getName() + ": "
                                + e.getMessage() + " " + line);
            } catch (IOException e) {
                this.valid = false;
                throw new ZedFileInitializationException(
                        "ERROR: Can't parse map file " + this.getName() + ": "
                                + e.getMessage());
            }
            this.url = url;
    }

    /**
     * normalizes the testRef children of a testProcessor element in the procMap.
     * This is done since 2.0RC2, when symlinks were added through the testGroup and groupRef elements.
     * The normalization result is that all symlinked tests are lifted into the testProcessor.
     */
    private void normalizeProcMap() throws ZedFileInitializationException {
    	/*
    	 * for each //testProcessor
    	 *   find out if it has a groupRef element
    	 *   if so, add into the testProcessor all testRefs that the group links to.
    	 */
		try{
            NodeList testProcessorNodes = XPathUtils.selectNodes(this.getMapDocument(), "//testProcessor");
            // Iterate through all found testprocessors in the procmap
            for (int j = 0; j < testProcessorNodes.getLength(); j++) {
            	Node testProcessorNode = testProcessorNodes.item(j);		
            	NodeList testProcessorNodeGroupRefChildNodes = XPathUtils.selectNodes(testProcessorNode, "groupRef");
            	for (int i = 0; i < testProcessorNodeGroupRefChildNodes.getLength(); i++) {
            		//System.err.println("groupRef was found");
					Element groupRef = (Element)testProcessorNodeGroupRefChildNodes.item(i);
					String idref = groupRef.getAttribute("idref");
					Element testGroup = this.getMapDocument().getElementById(idref);
					//insert all children of this testGroup into the current testProcessor: clone, dont move
					//since many processors may refer to this group
					if(testGroup!=null){
						NodeList groupChildren = XPathUtils.selectNodes(testGroup, "testRef");
						for (int i2 = 0; i2 < groupChildren.getLength(); i2++) {
							Node testRef = groupChildren.item(i2);
							testProcessorNode.appendChild(testRef.cloneNode(true));
						}						
					}else{
						System.err.println("a testGroup was null");
					}
				}            	            	            	
            }//while ((testProcessorNode = testProcessorNodes.nextNode()) != null)		
            
//            //clean the testGroup and groupRef elements out
//            NodeIterator toClean = XPathAPI.selectNodeIterator(this.getMapDocument(), "//groupRef|//testGroup");
//            Node node;
//            while ((node = toClean.nextNode()) != null) {
//            	Node deleted = node.getParentNode().removeChild(node);
//            }
            
		}catch (Exception e) {
			throw new ZedFileInitializationException("ZedMap.normalizeProcMap failure:" + e.getMessage());
		}
		
	}

	/** 
     * @return <code>true</code> if document is valid
     */
    public boolean isValid() {
        return this.valid;
    }

    /** 
     * @return the name of the map file
     */
    public String getName() {
        if (this.mapDocument!=null){
            //return this.mapDocument.getDocumentURI();// does not exist in java 1.4
            return this.url.toString();
        }
        return "nullmap";        
    }
    
    /** 
     * @return the name of the map file
     */
    public boolean exists() {
        return this.mapDocument!=null;        
    }
    
    /**
     * @return DOM Document representing contents of map
     */
    public Document getMapDocument() {
        return this.mapDocument;
    }

    /**
     * @return String of spec version, as should be declared in dc:Format
     */
    public String getSpecVersion() {
        return this.specVersion;
    }

    public void warning(SAXParseException e) throws SAXException {
        throw e; 
    }

    public void error(SAXParseException e) throws SAXException {
        throw e; 
    }

    public void fatalError(SAXParseException e) throws SAXException {
        throw e; 
    }

}
