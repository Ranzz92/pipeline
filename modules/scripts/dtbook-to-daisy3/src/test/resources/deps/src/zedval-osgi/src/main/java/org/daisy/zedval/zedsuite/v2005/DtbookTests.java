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
import java.net.URISyntaxException;

import org.daisy.util.text.URIUtils;
import org.daisy.util.xml.Namespaces;
import org.daisy.zedval.engine.PackageFile;
import org.daisy.zedval.engine.SmilFile;
import org.daisy.zedval.engine.TextFile;
import org.daisy.zedval.engine.XmlFileElement;
import org.daisy.zedval.engine.ZedConstants;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;
/**
 * Performs customtests on a dtbook file.
 * @author mgylling
 */
public class DtbookTests extends ZedCustomTest {
	/**
	 * Performs customtests on a dtbook file
	 * @param f
	 *            A TextFile instance
	 */
	public void performTest(ZedFile f) throws ZedCustomTestException {
        TextFile t;
        
        this.resultDoc = null; // Clear out the results document

        // If f isn't a TextFile, this is a fatal error
        try {
        	//Make our life simpler by removing need for casts
            t = (TextFile) f; 
        } catch (ClassCastException castEx) {
            throw new ZedCustomTestException(castEx.getMessage());
        }

        // TEST: Dtbook file has .xml extension (dtbook_fileExtn)
        if (t.getName().matches(".*\\.[x][m][l]") == false) {
            super.addTestFailure("dtbook_fileExtn", "Dtbook file "
                    + t.getName() + " does not use .xml extension", null, null);
        }

        // TEST: filename restriction (dtbook_fileName)
        if (!GenericTests.hasValidName(t)) {
            super.addTestFailure("dtbook_fileName", "Dtbook file "
                    + t.getName() + " uses disallowed characters in its name",
                    null, null);
        }

        //TEST: path restriction (audio_relPath)
        try{
	        if (!GenericTests.hasValidRelativePath(t,t.getPackage())) {
	            super.addTestFailure("dtbook_relPath", "Dtbook file " + t.getName()
	                    + " uses disallowed characters in its path, relative to packagefile", null, null);            
	        }
	    } catch (Exception e) {
	        throw new ZedCustomTestException("Exception: " + e.getMessage());
	    }
        
        // TEST: dtbook source file public identifier
        if (t.getDoctypePublicId() == null) {
            super.addTestFailure("dtbook_prologPubId", "Text document "
                            + t.getName() + " has no DTD public identifier",
                            null, null);
        } else {
        	//run all minor version tests, filtered later
        	if (!t.getDoctypePublicId().equals(ZedConstants.PUBLIC_ID_DTBOOK_Z2005_1)) {        
        		super.addTestFailure("dtbook_prologPubId_2005_1", "Text document "
                    + t.getName() + "uses incorrect DTD public identifier: "
                    + t.getDoctypePublicId(), null, null);
        	}        	
        	if (!t.getDoctypePublicId().equals(ZedConstants.PUBLIC_ID_DTBOOK_Z2005_2)) {
        		super.addTestFailure("dtbook_prologPubId_2005_2", "Text document "
                    + t.getName() + "uses incorrect DTD public identifier: "
                    + t.getDoctypePublicId(), null, null);
        	}	
        	if (!t.getDoctypePublicId().equals(ZedConstants.PUBLIC_ID_DTBOOK_Z2005_3)) {
        		super.addTestFailure("dtbook_prologPubId_2005_3", "Text document "
                    + t.getName() + "uses incorrect DTD public identifier: "
                    + t.getDoctypePublicId(), null, null);
        	}
        }

        // TEST: Dtbook file only references files that are items in manifest
        // (dtbook_manifestRefs)
        if (null != t.getFileRefs()) {
        	for (File aFileRef : t.getFileRefs().values()) {
                try {
                    if (t.getPackage().getManifest().get(
                            aFileRef.getCanonicalPath()) == null) {
                        super.addTestFailure("dtbook_manifestRefs",
                                "Dtbook file references non-manifest file "
                                        + aFileRef.getName(), aFileRef
                                        .getName(), null);
                    }
                } catch (IOException e) {
                    throw new ZedCustomTestException("IOException: "
                            + e.getMessage());
                }
            }
        } else {
            // filerefs was null
        }

        // TEST: dtbook_UidOpfUid dtb:uid value matches package unique-identifier
        try {
            if (t.getUid()==null||!t.getUid().equals(t.getPackage().getIdentifier())) {
                super.addTestFailure("dtbook_UidOpfUid", "Dtbook file "
                        + t.getName()
                        + " dtb:uid does not match package identifier", null,
                        null);
            }
        } catch (Exception e) {
            throw new ZedCustomTestException("Exception: " + e.getMessage());
        }
                
        // TEST: dtbook_SmilRefResolves 
        // TEST: dtbook_SmilRefResolvesToTimeContainers 
        // TEST: dtbook_SmilRefResolvesToText
        // TEST: dtbook_customTestMinimally
        // TEST: dtbook_tableListClass
        // TEST: dtbook_SmilRefValue
        // TEST: dtbook_srcValue
        // TEST: dtbook_hrefValue
        // TEST: dtbook_citeValue
        // TEST: dtbook_longdescValue
        // TEST: dtbook_profileValue        
        try {        	
            // iterate over all dtbook elements

        	SmilFile s = null;
        	for (XmlFileElement dtbookElement : t.getXmlFileElements()) {
        		
        		String uriValue = dtbookElement.getAttributeValueL("src");
        		@SuppressWarnings("unused")
				URI testURI = null;
        		if(uriValue!=null){
	        		try{
	        			testURI = new URI(uriValue);
	        		} catch (Exception e) {
	        			super.addTestFailure("dtbook_srcValue", "Dtbook file "+ t.getName()
	        					+ "contains a src value "+ uriValue + " which is invalid: " 
	        					+ e.getMessage(),null,null);
	        		}	
        			
        		}
        		
        		uriValue = dtbookElement.getAttributeValueL("cite");
        		testURI = null;
        		if(uriValue!=null){
	        		try{
	        			testURI = new URI(uriValue);
	        		} catch (Exception e) {
	        			super.addTestFailure("dtbook_citeValue", "Dtbook file "+ t.getName()
	        					+ "contains a cite value "+ uriValue + " which is invalid: " 
	        					+ e.getMessage(),null,null);
	        		}	
        			
        		}
        		
        		uriValue = dtbookElement.getAttributeValueL("href");
        		testURI = null;
        		if(uriValue!=null){
	        		try{
	        			testURI = new URI(uriValue);
	        		} catch (Exception e) {
	        			super.addTestFailure("dtbook_hrefValue", "Dtbook file "+ t.getName()
	        					+ "contains a href value "+ uriValue + " which is invalid: " 
	        					+ e.getMessage(),null,null);
	        		}	
        			
        		}

        		uriValue = dtbookElement.getAttributeValueL("longdesc");
        		testURI = null;
        		if(uriValue!=null){
	        		try{
	        			testURI = new URI(uriValue);
	        		} catch (Exception e) {
	        			super.addTestFailure("dtbook_longdescValue", "Dtbook file "+ t.getName()
	        					+ "contains a longdesc value "+ uriValue + " which is invalid: " 
	        					+ e.getMessage(),null,null);
	        		}	
        		}
        		
        		uriValue = dtbookElement.getAttributeValueL("profile");
        		testURI = null;
        		if(uriValue!=null){
	        		try{
	        			testURI = new URI(uriValue);
	        		} catch (Exception e) {
	        			super.addTestFailure("dtbook_profileValue", "Dtbook file "+ t.getName()
	        					+ "contains a profile value "+ uriValue + " which is invalid: " 
	        					+ e.getMessage(),null,null);
	        		}	
        		}
        		
        		String smilrefValue = dtbookElement.getAttributeValueL("smilref");
        		
        		if(smilrefValue!=null){
	        		//create the smilfile uri and file based on smilref value   
	        		URI smilrefuri = null;
	        		try{
	        			smilrefuri = URIUtils.resolve(t.toURI(), new URI(smilrefValue));
	        		} catch (URISyntaxException e) {
	        			super.addTestFailure("dtbook_SmilRefValue", "Dtbook file "+ t.getName()+ "contains a smilref value "+ smilrefValue + " which is invalid: " + e.getMessage(),null,null);
	        			continue;
	        		}	
	                File file = new File(smilrefuri.getPath());
	                //get a new SmilFile from package only if it isnt already here
	                
	                if(s==null || !s.getCanonicalPath().contentEquals(new StringBuffer(file.getCanonicalPath()))) {//PK: java 1.4 does not have contentEquals(CharSequence cs)
	                	 s = t.getPackage().getSmilFiles().get(file.getCanonicalPath());
	                }
	                if (s != null) {
	                	//we have the SmilFile we need
	                	XmlFileElement smilElement = s.getXmlFileElementById(smilrefuri.getFragment());
	                	if(smilElement!=null) {
	                		// we found the fragment we need
	                        // check the nature of the holding element, expected name depends on dtb type
	                        if (t.getPackage().getDtbMultimediaTypeAsDeclared().equals("textNCX")) {
	                            if (!smilElement.getLocalName().equals("text")) {
	                                super.addTestFailure("dtbook_SmilRefResolvesToText","Dtbook file "+ t.getName()+ " contains the smilref value "+ smilrefValue+ " which does not point to a text element in the smilfile",null, null);
	                            }
	                        }else{
		                        if (!smilElement.getLocalName().matches("par|seq")) {
		                            super.addTestFailure("dtbook_SmilRefResolvesToTimeContainers","Dtbook file "+ t.getName()+ " contains the smilref value "+ smilrefValue+ " which does not point to a timecontainer in the smilfile",null, null);
		                        }
	                        } 
	                        
	            			if (requiresSmilRefAttr(dtbookElement)){
	            				//run the dtbook_customTestMinimally & dtbook_tableListClass tests
	                            if (!dtbookElement.getLocalName().matches("table|list")) {
	                            	//do the dtbook_customTestMinimally test
                                    if (smilElement.getAttributeValueL("customTest") == null) {
                                        super.addTestFailure("dtbook_customTestMinimally","Dtbook file "+ t.getName()+ " contains the element "+ dtbookElement.getLocalName()+ " with id "+dtbookElement.getAttributeValueL("id")+ " that points to the smil container "+ smilElement.getAttributeValueL("id")+ " in smilfile "+ s.getName()+ "; this smil element does not carry a customTest attribute",null, null);
                                    }                 
	                            } else {
	                            	//do the dtbook_tableListClass test
                                    if (dtbookElement.getLocalName().equals("table")) {
                                        if (smilElement.getAttributeValueL("class") == null|| !smilElement.getAttributeValueL("class").equals("table")) {
                                            super.addTestFailure("dtbook_tableListClass","Dtbook file "+ t.getName()+ " contains the element "+ dtbookElement.getLocalName()+ " with id "+ dtbookElement.getAttributeValueL("id")+ " that points to the smil container "+ smilElement.getAttributeValueL("id")+ " in smilfile "+ s.getName()+ "; this smil element does not carry a class='table' attribute",null, null);
                                        }
                                    } else if (dtbookElement.getLocalName().equals("list")) {
                                        if (smilElement.getAttributeValueL("class") == null|| !smilElement.getAttributeValueL("class").equals("list")) {
                                            super.addTestFailure("dtbook_tableListClass","Dtbook file "+ t.getName()+ " contains the element "+ dtbookElement.getLocalName()+ " with id "+ dtbookElement.getAttributeValueL("id")+ " that points to the smil container "+ smilElement.getAttributeValueL("id")+ " in smilfile "+ s.getName()+ "; this smil element does not carry a class='list' attribute",null, null);
                                        }
                                    }
	                            }
	            			} 
	                	}else{//if(smilElement!=null)
	                        // doesnt resolve, can find smilfile but not fragment
	                        super.addTestFailure("dtbook_SmilRefResolves","Dtbook file "+ t.getName()+ " contains the smilref value "+ smilrefValue+ " which points to a nonexisting fragment",null, null);
	                	}//if(smilElement!=null)
	                }else{ //if (s != null) 
	                    // doesnt resolve, cant find smilfile
	                    super.addTestFailure("dtbook_SmilRefResolves","Dtbook file "+ t.getName()+ " contains the smilref value "+ smilrefValue+ " which points to a nonexisting smilfile",null, null);
	                }//if (s != null) 
        		}else{ //if(smilrefValue!=null){
        			//this dtbook element didnt have a smilref attribute
        			if (requiresSmilRefAttr(dtbookElement)){
                        if (!dtbookElement.getLocalName().matches("table|list")) {
                            super.addTestFailure("dtbook_customTestMinimally","Dtbook file "+ t.getName()+ " contains the element "+ dtbookElement.getLocalName()+ " with id "+ dtbookElement.getAttributeValueL("id")+ " that doesnt have the smilref attribute which is required for this element.",null, null);                            
                        } else {
                            super.addTestFailure("dtbook_tableListClass","Dtbook file "+ t.getName()+ " contains the element "+ dtbookElement.getLocalName()+ " with id "+ dtbookElement.getAttributeValueL("id")+ " that doesnt have the smilref attribute which is required for this element.",null, null);
                        }
        			}        			
        		}
        	}//while(dtbookIterator.hasNext())
        	
        } catch (Exception e) {
            throw new ZedCustomTestException(
                    e.getClass().getName()
                            + " when performing test dtbook_SmilRefResolves | dtbook_SmilRefResolvesToTimeContainers | dtbook_SmilRefResolvesToText on file "
                            + f.getName() + ": " + e.getMessage());
        }

        
        // TEST: math_opfExtensionVersionMetadata
        // When DTBook includes at least one MathML island, the package file must contain one meta element 
        // with @name="z39-86-extension-version" and @scheme="http://www.w3.org/1998/Math/MathML"
        // TEST: math_xsltMetadataPresent
        // When DTBook includes at least one MathML island, the package file must contain one meta element with 
        // @name="DTBook-XSLTFallback" and @scheme="http://www.w3.org/1998/Math/MathML"
        try {
			//if theres a math element in this dtbook file			
			if(t.getNamespaceURIs().contains(Namespaces.MATHML_NS_URI)) {
				boolean opfMathExtensionVersionMetaSet = false;
				boolean opfMathXsltMetaSet = false;
				PackageFile opf = t.getPackage();
				for (Object o : opf.getXmlFileElements()) {
					XmlFileElement xfe = (XmlFileElement) o;
					if (xfe.getLocalName().equals("meta")) {						
						if( xfe.getAttributeValueL("name") != null && xfe.getAttributeValueL("scheme") != null) {
							if(xfe.getAttributeValueL("scheme").equals(Namespaces.MATHML_NS_URI)) {								
								if(xfe.getAttributeValueL("name").equals("z39-86-extension-version")) {
									opfMathExtensionVersionMetaSet = true;	
								}
								else if(xfe.getAttributeValueL("name").equals("DTBook-XSLTFallback")) {
									opfMathXsltMetaSet = true;								
								}		
							}
						}
					}else if(xfe.getLocalName().equals("manifest")) {
						break;
					}
				}
				
				if(!opfMathExtensionVersionMetaSet) {
					super.addTestFailure("math_opfExtensionVersionMetadata",null,null,null);
				}
				if(!opfMathXsltMetaSet) {
					super.addTestFailure("math_xsltMetadataPresent",null,null,null);
				}
			}
			
		} catch (Exception e) {
			throw new ZedCustomTestException(e.getClass().getName()
					+ " when performing test math_opfExtensionVersionMetadata "
					+ f.getName()
					+ ": "
					+ e.getMessage());
		}
        
        
		//TEST: math_smilrefPresent
		// math element must have dtbook:smilref attribute
		if(t.getNamespaceURIs().contains(Namespaces.MATHML_NS_URI)) {
			for(Object o : t.getXmlFileElements()) {
				XmlFileElement xfe = (XmlFileElement)o;
				if(xfe.getLocalName().equals("math")) {
					if(xfe.getAttributeValueL("smilref")==null){
						super.addTestFailure("math_smilrefPresent",null,null,null);
					}
				}
			}
		}
		
		
    }// performTest
    
    /**
	 * @return true if inparam dtbook element is one for which the spec
	 *         implicitly requires a smilref attribute to be set
	 */    
    private boolean requiresSmilRefAttr(XmlFileElement dtbookElement) {
      if ((ZedConstants.NAMESPACEURI_DTBOOK_Z2005.equals(dtbookElement.getNsURI()) && dtbookElement.getLocalName().matches("linenum|note|noteref|annotation|pagenum|table|list"))
    		  || (dtbookElement.getLocalName()=="sidebar"
    				  && (dtbookElement.getAttributeValueL("render") != null 
    						  && dtbookElement.getAttributeValueL("render").equals("optional")))
    		  || (dtbookElement.getLocalName()=="prodnote" 
    				  && (dtbookElement.getAttributeValueL("render") != null 
    						  && dtbookElement.getAttributeValueL("render").equals("optional")))
      	) {
    	  return true;
      }
      return false;
    }
    
}// class