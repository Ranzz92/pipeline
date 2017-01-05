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

package org.daisy.zedval.zedsuite.v2005;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.daisy.util.xml.validation.RelaxngSchematronValidator;
import org.daisy.util.xml.validation.SchematronMessage;
import org.daisy.util.xml.validation.ValidationException;
import org.daisy.zedval.engine.NcxFile;
import org.daisy.zedval.engine.ResourceFile;
import org.daisy.zedval.engine.SmilFile;
import org.daisy.zedval.engine.XmlFileElement;
import org.daisy.zedval.engine.ZedConstants;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Performs custom tests on resource file
 * 
 * @author mgylling
 * @author jpritchett
 */

public class ResourceTests extends ZedCustomTest implements ErrorHandler {
	/**
	 * Performs custom tests on resource file
	 * @param f
	 * 		A ResourceFile instance
	 */
	public void performTest(ZedFile f)
			throws ZedCustomTestException {
		ResourceFile r;

		this.resultDoc = null; // Clear out the results document

		// If r isn't a ResourceFile, this is a fatal error
		try {
			r = (ResourceFile) f; // Make our life simpler by removing need
			// for casts
		} catch (ClassCastException castEx) {
			throw new ZedCustomTestException(castEx.getMessage());
		}

		// TEST: Resource file has .res extension (res_fileExtn)
		if (r.getName().matches(".*\\.[r][e][s]") == false) {
			super.addTestFailure("resource_fileExtn", "Resource file "
					+ r.getName()
					+ " does not use .res extension", null, null);
		}

		// TEST: filename restriction (resource_fileName)
		if (!GenericTests.hasValidName(r)) {
			super.addTestFailure("resource_fileName", "Resource file "
					+ r.getName()
					+ " uses disallowed characters in its name", null, null);
		}

		//TEST: path restriction (resource_relPath)
		try{
			if (!GenericTests.hasValidRelativePath(r, r.getPackage())) {
				super.addTestFailure("resource_relPath", "Resource file "
						+ r.getName()
						+ " uses disallowed characters in its path, relative to packagefile", null, null);
			}
		} catch (Exception e) {
	        throw new ZedCustomTestException("Exception: " + e.getMessage());
	    }		

		// TEST: Resource file public identifier (resource_prologPubId)
		if (r.getDoctypePublicId() == null) {
			super.addTestFailure("resource_prologPubId", "Resource file "
					+ r.getName()
					+ " has no DTD public identifier", null, null);
		} else if (!r.getDoctypePublicId().equals(ZedConstants.PUBLIC_ID_RESOURCE_Z2005)) {
			super.addTestFailure("resource_prologPubId", "Resource file "
					+ r.getName()
					+ " uses incorrect DTD public identifier: "
					+ r.getDoctypePublicId(), null, null);
		}

		// TEST: Resource file only references files that are items in manifest
		// (resource_manifestRefs)
		if (null != r.getFileRefs()) {
			for (File aFileRef : r.getFileRefs().values()) {
				try {
					if (r.getPackage().getManifest().get(aFileRef.getCanonicalPath()) == null) {
						super.addTestFailure("resource_manifestRefs", "Resource file references non-manifest file "
								+ aFileRef.getName(), aFileRef.getName(), null);
					}
				} catch (IOException e) {
					throw new ZedCustomTestException("IOException: "
							+ e.getMessage());
				}
			}
		} else {
			// filerefs was null: resource file didnt reference any files
			// (perhaps only using text resources today?)
		}

		// TEST: resource_skipStructResEntry
		// 		Every skippable structure must have a resource file entry        
		// 		This test currently assumes that all skippable structures are
		// 		correctly noted in the NCX. 
		// 		Note that the normative 7.4.3 requires skip resources to be done through NCX,
		// 		so no need to check the SMIL scope

		try {
			NcxFile n = r.getPackage().getNcx();
			if(n!=null){
				List<XmlFileElement> smilCustomTestElements = n.getSmilCustomTestElements();		
				if(!smilCustomTestElements.isEmpty()) {
					if (r.hasNsUri("http://www.daisy.org/z3986/2005/ncx/")) {
						//there is an res scope element with the ncx nsuri
						//build the individual tests for each skippable element
						StringBuffer sch = new StringBuffer();
						StringBuffer pattern = new StringBuffer();
						sch.append("<schema xmlns='http://www.ascc.net/xml/schematron'>\n");
						sch.append("<ns prefix='res' uri='http://www.daisy.org/z3986/2005/resource/'/>\n");
						int count = 0;
		
						for (XmlFileElement sct : smilCustomTestElements) {
							count++;
							if (sct.getLocalName().equals("smilCustomTest")) { //not really necessary
								//create the test for each customTest
								pattern.append("<pattern name='test_"+ count + "' id='test_" + count + "'>\n");
								pattern.append("<rule context=\"res:resources/res:scope[@nsuri='http://www.daisy.org/z3986/2005/ncx/']\">\n");
		
								//test the nodeSet@select value against ncx bookStruct AND/OR id value	                        
								String test = "";
								String expecting = "";
								if (sct.getAttributeValueL("bookStruct") != null
										&& sct.getAttributeValueL("id") != null) {	                        	
									test = "res:nodeSet[(starts-with(@select, '//smilCustomTest[@bookStruct=') and contains(@select,'"
											+ sct.getAttributeValueL("bookStruct")
											+ "')) or (starts-with(@select, '//smilCustomTest[@id=') and contains(@select,'"
											+ sct.getAttributeValueL("id")
											+ "'))]";
									expecting = "smilCustomTest with bookStruct attr valued "+sct.getAttributeValueL("bookStruct")+"and/or id attr valued "+sct.getAttributeValueL("id")+".";
								} else if (sct.getAttributeValueL("bookStruct") != null) {
									test = "res:nodeSet[starts-with(@select, '//smilCustomTest[@bookStruct=') and contains(@select,'"
											+ sct.getAttributeValueL("bookStruct")
											+ "')]";
									expecting = "smilCustomTest with bookStruct attr valued "+sct.getAttributeValueL("bookStruct");
								} else if (sct.getAttributeValueL("id") != null) {
									test = "res:nodeSet[starts-with(@select, '//smilCustomTest[@id=') and contains(@select,'"
											+ sct.getAttributeValueL("id")
											+ "')]";
									expecting = "smilCustomTest with id attr valued "+sct.getAttributeValueL("id");
								}
								pattern.append("<assert test=\"" + test + "\">\n");
								pattern.append("[sch][zedid::resource_skipStructResEntry][detail::expecting in ncx scope: "+ expecting + "]");
								pattern.append("\n</assert>\n</rule>\n</pattern>\n");
								sch.append(pattern);
								pattern.delete(0, pattern.length());
							}//if(sct.getLocalName().equals("smilCustomTest")	        			
						}//while(ncti.hasNext())
		
						sch.append("</schema>");
						RelaxngSchematronValidator schval = new RelaxngSchematronValidator(this, sch.toString());
						if (!schval.isValid(r)) {
							// there were errors, reported to this.sax.errorhandler
						}
					} else { //if(r.hasNsUri("ncx"))
						//there was no ncx scope element        		
						if(!smilCustomTestElements.isEmpty()) {
							super.addTestFailure("resource_skipStructResEntry", "No scope element matching NCX", null, null);
						}	
					}
				} else { //if(!smilCustomTestElements.isEmpty())
					//there were zero SmilCustomTest elements in NCX, reported by other test
				}
			}//if (n!=null)
			else{
				//ncx is null, reported by other test
			}
		} catch (Exception e) {
			throw new ZedCustomTestException(e.getClass().getName()
					+ " when performing test resource_skipStructResEntry on file "
					+ f.getName()
					+ ": "
					+ e.getMessage());
		}

		
		//TEST: res_escStructResEntry
		//Every escapable structure must have an associated resource file entry
		//logic:
		//collect all escapable structures in smil body
		//check if there is a resource that maps to each one of these:        
		//-via smil class attr (which the normative 7.4.1 refers to as the 'typical' approach
		//-via dtbook, which is the other way in addition to the above
		try {						
			StringBuffer sch = new StringBuffer();
			StringBuffer pattern = new StringBuffer();
			Set<String> patterns = new HashSet<String>(); //collect all tests in order to not repeat
			sch.append("<schema xmlns='http://www.ascc.net/xml/schematron'>\n");
			sch.append("<ns prefix='res' uri='http://www.daisy.org/z3986/2005/resource/'/>\n");

			int count = 0;
			for (SmilFile s : r.getPackage().getSmilFiles().values()) {
				if (s.hasEscapableStructures()) {					
					for (XmlFileElement smilFileElement : s.getEscapableElements()) {
						count++;
						pattern.append("<pattern name='test_"+ count + "' id='test_" + count + "'>\n");
						pattern.append("<rule context=\"res:resources\">\n");
						XmlFileElement dtbookElement = SmilTests.getReferencingTextFileElement(smilFileElement,s);
						String test ="";
						String expecting = "";
						if(smilFileElement.getAttributeValueL("class")!=null) {
							expecting = "a resource for class attr with value " + smilFileElement.getAttributeValueL("class") + " in smil scope";
							test = "res:scope[@nsuri='http://www.w3.org/2001/SMIL20/']/res:nodeSet" +
									"[(starts-with(@select, '//par[@class=') " +
									"or starts-with(@select, '//seq[@class=') " +
									"or starts-with(@select, '//*[@class=')) and contains(@select,'"
										+ smilFileElement.getAttributeValueL("class")										
										+ "')]";
							
						}
						if(dtbookElement!=null) {
							if(smilFileElement.getAttributeValueL("class")!=null){
								test = test + " or ";
								expecting = expecting + " or ";
							}
							expecting = expecting + "a resource for dtbook element " + dtbookElement.getLocalName();
							test = test + "res:scope[@nsuri='http://www.daisy.org/z3986/2005/dtbook/']/res:nodeSet[contains(@select,'"
										+ dtbookElement.getLocalName()										
										+ "')]";	
						}
																							   					   												
						pattern.append("<assert test=\"" + test + "\">\n");
						pattern.append("[sch][zedid::resource_escStructResEntry][detail::expecting: " + expecting +"]");
						pattern.append("\n</assert>\n</rule>\n</pattern>\n");
						
						//check if this test is already added to sch
						if (!patterns.contains(test)) {							
							sch.append(pattern);
							patterns.add(test);		
						}														
						pattern.delete(0, pattern.length());
					}																				
				}//if(s.hasEscapableStructures())        	        	
			}//for(Iterator i = r.getPackage().getSmilFiles().keySet().iterator(); i.hasNext();
			
			sch.append("</schema>");
			if(count>0) {
				RelaxngSchematronValidator schval = new RelaxngSchematronValidator(this, sch.toString());
				if (!schval.isValid(r)) {
				// there were errors, reported to this.sax.errorhandler
				}			
			}
		} catch (Exception e) {
			throw new ZedCustomTestException(e.getClass().getName()
					+ " when performing test resource_escStructResEntry on file "
					+ f.getName()
					+ ": "
					+ e.getMessage());
		}

		//TEST: resource_resourceSrcValue
        try {        	
        	for (XmlFileElement resourceElement : r.getXmlFileElements()) {
        		String uriValue = resourceElement.getAttributeValueL("src");
        		if(uriValue!=null){
	        		try{
	        			new URI(uriValue);
	        		} catch (Exception e) {
	        			super.addTestFailure("resource_resourceSrcValue", "Dtbook file "+ r.getName()+ "contains a src value "+ uriValue + " which is invalid: " + e.getMessage(),null,null);
	        		}	        			
        		}
        	}	
        }catch (Exception e) {
            throw new ZedCustomTestException(
                    e.getClass().getName()
                            + " when performing test resource_resourceSrcValue on file "
                            + f.getName() + ": " + e.getMessage());
		}
	}

	public void warning(SAXParseException exception) {
		try {
			SchematronMessage sm = new SchematronMessage(exception.getMessage());
			super.addTestFailure(sm.getMessage("zedid"), sm.getMessage("detail"), null, Integer.toString(exception.getLineNumber()));
		} catch (ZedCustomTestException e) {
			System.err.println("ZedCustomTestException at ResourceTests.errh.warning");
		} catch (ValidationException e) {
			System.err.println("ValidationException at ResourceTests.errh.warning");
		}

	}

	public void error(SAXParseException exception) {
		try {
			SchematronMessage sm = new SchematronMessage(exception.getMessage());
			super.addTestFailure(sm.getMessage("zedid"), sm.getMessage("detail"), null, Integer.toString(exception.getLineNumber()));
		} catch (ZedCustomTestException e) {
			System.err.println("ZedCustomTestException at ResourceTests.errh.error");
		} catch (ValidationException e) {
			System.err.println("ValidationException at ResourceTests.errh.error");
		}
	}

	public void fatalError(SAXParseException exception) {
		try {
			SchematronMessage sm = new SchematronMessage(exception.getMessage());
			super.addTestFailure(sm.getMessage("zedid"), sm.getMessage("detail"), null, Integer.toString(exception.getLineNumber()));
		} catch (ZedCustomTestException e) {
			System.err.println("ZedCustomTestException at ResourceTests.errh.fatalError");
		} catch (ValidationException e) {
			System.err.println("ValidationException at ResourceTests.errh.fatalError");
		}
	}

}
