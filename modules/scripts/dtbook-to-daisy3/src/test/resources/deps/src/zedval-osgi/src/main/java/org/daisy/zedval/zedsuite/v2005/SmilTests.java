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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.daisy.util.text.URIUtils;
import org.daisy.util.xml.Namespaces;
import org.daisy.util.xml.SmilClock;
import org.daisy.util.xml.validation.RelaxngSchematronValidator;
import org.daisy.util.xml.validation.SchematronMessage;
import org.daisy.util.xml.validation.ValidationException;
import org.daisy.zedval.engine.AudioFile;
import org.daisy.zedval.engine.ImageFile;
import org.daisy.zedval.engine.ManifestFile;
import org.daisy.zedval.engine.NcxFile;
import org.daisy.zedval.engine.PackageFile;
import org.daisy.zedval.engine.SmilFile;
import org.daisy.zedval.engine.TextFile;
import org.daisy.zedval.engine.XmlFileElement;
import org.daisy.zedval.engine.ZedConstants;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
/**
 * Performs customtests on a smil file
 * @author jpritchett
 * @author mgylling
 */
public class SmilTests extends ZedCustomTest implements ErrorHandler {
    /**
     * Performs customtests on a smil file
     * @param f
     *            A SmilFile instance
     */
	public void performTest(ZedFile f) throws ZedCustomTestException {
        SmilFile s;
        PackageFile p;
        long totDur;
        SmilClock expectedElapsedTime;
        ManifestFile aManifestItem;

        this.resultDoc = null; // Clear out the results document

        // If f isn't a SmilFile, this is a fatal error
        try {
            s = (SmilFile) f; // Make our life simpler by removing need for
            // casts
        } catch (ClassCastException castEx) {
            throw new ZedCustomTestException(castEx.getMessage());
        }

        p = s.getPackage();

        // TEST: extension (smil_fileExtn)
        if (s.getName().matches(".*\\.[s][m][i][l]") == false) {
            super.addTestFailure("smil_fileExtn", "Smil file " + s.getName()
                    + " does not use .smil extension", null, null);
        }

        // TEST: filename restriction (smil_fileName)
        if (!GenericTests.hasValidName(s)) {
            super.addTestFailure("smil_fileName", "Smil file " + s.getName()
                    + " uses disallowed characters in its name", null, null);
        }

        //TEST: path restriction (smil_relPath)
        try{
	        if (!GenericTests.hasValidRelativePath(s,s.getPackage())) {
	            super.addTestFailure("smil_relPath", "Smil file " + s.getName()
	                    + " uses disallowed characters in its path, relative to packagefile", null, null);            
	        }
        } catch (Exception e) {
	        throw new ZedCustomTestException("Exception: " + e.getMessage());
	    }        
        
        // TEST: dtbook source file public identifier (smil_prologPubId)
        if (s.getDoctypePublicId() == null) {
            super
                    .addTestFailure("smil_prologPubId", "Smil document "
                            + s.getName() + " has no DTD public identifier",
                            null, null);
        } else {
        	//run both tests, filtered later
        	if (!s.getDoctypePublicId().equals(ZedConstants.PUBLIC_ID_SMIL_Z2005_1)) {
        		super.addTestFailure("smil_prologPubId_2005_1", "Smil document "
                    + s.getName() + "uses incorrect DTD public identifier: "
                    + s.getDoctypePublicId(), null, null);
        	}	
        	if (!s.getDoctypePublicId().equals(ZedConstants.PUBLIC_ID_SMIL_Z2005_2)) {
        		super.addTestFailure("smil_prologPubId_2005_2", "Smil document "
                    + s.getName() + "uses incorrect DTD public identifier: "
                    + s.getDoctypePublicId(), null, null);
        	}	
        }

        // TEST: smil_metaDtbUidValue
        try {
        	if(s.getUid()!=null){
	            if (!s.getUid().equals(p.getIdentifier())) {
	                super.addTestFailure("smil_metaDtbUidValue", "Smil file "
	                        + s.getName()
	                        + " dtb:uid does not match package identifier", null,
	                        null);
	            }
        	}else{
        		//no uid in this smil, reported by other test
        	}
        } catch (Exception e) {
            throw new ZedCustomTestException("Exception: " + e.getMessage());
        }

        // TEST: src attribute on text element references dtbook file
        // (smil_srcDtbook)
        if (s.getTextFileRefs() != null) {
        	for (File aFileRef : s.getTextFileRefs().values()) {
                try {
                    aManifestItem = (ManifestFile) (p.getManifest().get(aFileRef.getCanonicalPath()));
                    if (aManifestItem != null
                            && (aManifestItem instanceof TextFile) == false) {
                        super.addTestFailure("smil_srcDtbook",
                                "SMIL text element references non-dtbook file "
                                        + aFileRef.getName(), aFileRef
                                        .getName(), null);
                    }
                } catch (IOException e) {
                    throw new ZedCustomTestException("IOException: "
                            + e.getMessage());
                }
            }
        }

        // TEST: src attribute on audio element references audio file
        // (smil_srcAudio)
        if (s.getAudioFileRefs() != null) {
        	for (File aFileRef : s.getAudioFileRefs().values()) {
                try {
                    aManifestItem = (ManifestFile) (p.getManifest()
                            .get(aFileRef.getCanonicalPath()));
                    if (aManifestItem != null
                            && (aManifestItem instanceof AudioFile) == false) {
                        super.addTestFailure("smil_srcAudio",
                                "SMIL audio element references non-audio file "
                                        + aFileRef.getName(), aFileRef
                                        .getName(), null);
                    }
                } catch (IOException e) {
                    throw new ZedCustomTestException("IOException: "
                            + e.getMessage());
                }
            }
        }

        // TEST: src attribute on img element references image file
        // (smil_srcImg)
        if (s.getImageFileRefs() != null) {
        	for (File aFileRef : s.getImageFileRefs().values()) {
                try {
                    aManifestItem = (ManifestFile) (p.getManifest()
                            .get(aFileRef.getCanonicalPath()));
                    if (aManifestItem != null
                            && (aManifestItem instanceof ImageFile) == false) {
                        super.addTestFailure("smil_srcImg",
                                "SMIL img element references non-image file "
                                        + aFileRef.getName(), aFileRef
                                        .getName(), null);
                    }
                } catch (IOException e) {
                    throw new ZedCustomTestException("IOException: "
                            + e.getMessage());
                }
            }
        }

        // TEST: smil_textSrcResolves
        //
        // TEST: smil_hrefValue
        //   href attribute on the a element is a valid URI
        // TEST: smil_hrefSMIL 
        //		href attribute on anchor elements with attribute external='false' points to a SMIL file
        // TEST: smil_anchorManifestExternalAttributeFalse
        // 		All files referenced from SMIL are represented in package file manifest,
        // 		except those files referenced
        // 		from anchor elements where the external attribute is valued 'true'
        // TEST: smil_anchorManifestExternalAttributeTrue
        // 		All files referenced from SMIL anchor elements where the external
        // 		attribute is valued 'true' are not represented in package file manifest
        // TEST: smil_clipEndBeforeEOF
        // 		audio element clipEnd attribute value is less than end of audio file
        // TEST: smil_clipEndAfterclipBegin
        // 		audio element clipEnd attribute value is greater than clipBegin
        // TEST: smil_ncxCustomTestMatch
        // 		each customTest element in SMIL head has its id, override, and defaultState attrs
        // 		duplicated in one smilCustomTest element in NCX
        try {
        	//TextFile dtbk = new TextFile("null","null",null);
        	TextFile dtbk = null;
        	
        	for (XmlFileElement smilElement : s.getXmlFileElements()) {
    			URI srcUri = null;
    			File file = null;
        		if (smilElement.getLocalName().equals("text")) {
        			try{
	        			srcUri = new URI(smilElement.getAttributeValueL("src"));
	        			file = new File(URIUtils.resolve(s.toURI(), srcUri).getPath());
	        		} catch (Exception e) {
	        			//mg: canonically done in RNG by anyURI, but report anyway; anyURI is no good. 	        			
	        			super.addTestFailure("smil_textSrcValue","Smilfile "+ s.getName()+ " contains a text src value "+ smilElement.getAttributeValueL("src") + " which is invalid: " + e.getMessage(),null,null);
	        			continue;
	        		}	
                    dtbk = (TextFile) p.getManifest().get(file.getCanonicalPath());	                
	                if(dtbk!=null){
	                	XmlFileElement dtbookElement = dtbk.getXmlFileElementById(srcUri.getFragment());
	                	if(dtbookElement==null){
	                		super.addTestFailure("smil_textSrcResolves","Smil file "+ s.getName()+ " contains the src attr value "+ smilElement.getAttributeValueL("src")+ " which points to a nonexisting fragment",null, null);	
	                	}	                		                	
	                }else{//if(dtbk!=null)
                        super.addTestFailure("smil_textSrcResolves","Smil file "+ s.getName()+ " contains the src attr value "+ smilElement.getAttributeValueL("src")+ " which points to a nonexisting textfile",null, null);            	
	                }//if(dtbk!=null)
        			
        		}//if (smilElement.getLocalName().equals("text")
        		else if (smilElement.getLocalName().equals("a")) {
        			try{
	        			srcUri = new URI(smilElement.getAttributeValueL("href"));
	        			file = new File(URIUtils.resolve(s.toURI(), srcUri).getPath());
	        		} catch (Exception e) {
	        			//error in uri
	        			//mg: canonically done in RNG by anyURI, but report anyway; anyURI is no good.
	        			super.addTestFailure("smil_hrefValue","Smilfile "+ s.getName()+ " contains an anchor href value "+ smilElement.getAttributeValueL("href") + " which is invalid: " + e.getMessage(),null,null);
	        			continue;
	        		}		
	        		if(file.exists()){
		        		ZedFile manifestFile = (ZedFile) p.getManifest().get(file.getCanonicalPath());        			
		        		if(smilElement.getAttributeValueL("external").equals("false")) {
	        				//it must be a smilfile, represented in manifest
		        			if(manifestFile==null) {
	                          super.addTestFailure("smil_anchorManifestExternalAttributeFalse","Smilfile "+ s.getName()+ " contains an anchor href value "+ srcUri.toString() + " but the file does not occur in manifest",null,null);
		        			}else if(!(manifestFile instanceof SmilFile)) {
		                        super.addTestFailure("smil_hrefSMIL", "Smilfile " + s.getName() + " contains an anchor href value " + srcUri.toString() + " which doesnt point to a smilfile",null,null);	        				
		        			}        				
	        			}else if(smilElement.getAttributeValueL("external").equals("true")){
	        				//it must not be represented in manifest
	        				if(manifestFile!=null) {
	        					super.addTestFailure("smil_anchorManifestExternalAttributeTrue","Smilfile "+ s.getName()+ " contains an external anchor href value "+ srcUri.toString() + " but the file occurs in manifest",null,null);
	        				}        				
	        			}else{
	        				//no external attribute on this anchor
	        			}
	        		}
        		}//else if (smilElement.getLocalName().equals("a"))
        		else if (smilElement.getLocalName().equals("img")) {
        			try{
	        			srcUri = new URI(smilElement.getAttributeValueL("src"));	        			
	        		} catch (Exception e) {
	        			//mg: canonically done in RNG by anyURI, but report anyway; anyURI is no good. 	        			
	        			super.addTestFailure("smil_imgSrcValue","Smilfile "+ s.getName()+ " contains a img src value "+ smilElement.getAttributeValueL("src") + " which is invalid: " + e.getMessage(),null,null);
	        			continue;
	        		}	        			
        		}//else if (smilElement.getLocalName().equals("img")) {
        		else if (smilElement.getLocalName().equals("audio")) {
        			try{
	        			srcUri = new URI(smilElement.getAttributeValueL("src"));
	        			file = new File(URIUtils.resolve(s.toURI(), srcUri).getPath());
	        		} catch (Exception e) {
	        			//mg: canonically done in RNG by anyURI, but report anyway; anyURI is no good. 	        			
	        			super.addTestFailure("smil_audioSrcValue","Smilfile "+ s.getName()+ " contains a audio src value "+ smilElement.getAttributeValueL("src") + " which is invalid: " + e.getMessage(),null,null);
	        			continue;
	        		}	
	        		if(file.exists()){
	                    AudioFile af = (AudioFile) p.getManifest().get(file.getCanonicalPath());
	                    String clipEnd = smilElement.getAttributeValueL("clipEnd");
	                    String clipBegin = smilElement.getAttributeValueL("clipBegin");
	                    
	                    if(af!=null && clipEnd != null){
		                    SmilClock endDur = new SmilClock(smilElement.getAttributeValueL("clipEnd"));
		                    if (endDur.millisecondsValue() > af.getDuration().millisecondsValue()) {
		                        super.addTestFailure("smil_clipEndBeforeEOF","Smilfile "+ s.getName()+ " contains a reference to the time "+ smilElement.getAttributeValueL("clipEnd")+ " in the audiofile "+ smilElement.getAttributeValueL("src")+ " but the duration of the "+ "audiofile is shorter than that, namely "+ af.getDuration().millisecondsValue()+ " ms.", null, null);
		                    }
	                    }
	                                        
	                    if (clipEnd != null && clipBegin != null) {
	                        SmilClock begin = new SmilClock(clipBegin);
	                        SmilClock end = new SmilClock(clipEnd);
	                        if (begin.millisecondsValue() >= end.millisecondsValue()) {
	                            super.addTestFailure("smil_clipEndAfterclipBegin",begin.toString(SmilClock.FULL)+ " is equal to or higher than "+ end.toString(SmilClock.FULL)+ " on audio element with id "+ smilElement.getAttributeValueL("id"),null, null);
	                        }
	                    }  
	        		}
        		}//else if (smilElement.getLocalName().equals("audio"))
        		
        		else if (smilElement.getLocalName().equals("customTest")) {        			
        			NcxFile n = s.getPackage().getNcx();
        			if(n!=null){
	                    boolean match = false;
	                    for (XmlFileElement ncxCustomTest : n.getSmilCustomTestElements()) {
	                        // find out if this ncx test matches the current smil test
	                        try{
		                        if (ncxCustomTest.getAttributeValueL("id").equals(smilElement.getAttributeValueL("id"))
		                                && ncxCustomTest.getAttributeValueL("defaultState").equals(smilElement.getAttributeValueL("defaultState"))
		                                && ncxCustomTest.getAttributeValueL("override").equals(smilElement.getAttributeValueL("override"))) {
		                            match = true;
		                            break;
		                        }
	                        } catch (Exception e) {
	                        	//a missing property, reported by other test
	                        }
	                    }// for(Iterator ncxSmilCustomTestIterator
	                    if (!match) {
	                        super.addTestFailure("smil_ncxCustomTestMatch","SMIL smilCustomTest with id "+ smilElement.getAttributeValueL("id")+ " in file " + s.getName()+ " does not match a customTest in ncx",null, null);
	                    }
        			}else{
        				//ncx is null, reported by other test
        			}
        		}//else if (smilElement.getLocalName().equals("customTest"))
        		
        		
        	}//while(smilElementIterator.hasNext()        	        	        	
        } catch (Exception e) {
            throw new ZedCustomTestException(e.getClass().getName()
                    + " when performing test smil_textSrcResolves on file"
                    + f.getName() + ": " + e.getMessage());
        }
        

        
        
        // TEST: smil_metaAudioDtbTotElaAccurate
        // TEST: smil_metaTextDtbTotElaAccurate
        // represents the total time elapsed up to the beginning of this SMIL
        // file
        // This test is done by adding the ACTUAL durations of all SMIL files
        // that occur before the current file in the spine.
        try {
            if (s.getPackage().getDtbMultimediaTypeAsDeclared().equals("textNCX")) {
                // it is a text dtb (= has no duration)
                if (s.getTotalElapsedTime().millisecondsValue() != 0) {
                    super.addTestFailure("smil_metaTextDtbTotElaAccurate", "",
                            null, null);
                }
            } else {
                // it is an audio dtb (= has duration)
                totDur = 0;
                for (SmilFile spineFile : p.getSpine().values()) {
                    try {
                        if (spineFile.getName().equals(s.getName())) {
                            break; // We're done now
                        }
                        totDur += spineFile.expectedDuration().millisecondsValue();
                    } catch (NullPointerException npe) {
                        // if a smilfile is malformed we end up here
                    }
                }
                expectedElapsedTime = new SmilClock(totDur);
                if (expectedElapsedTime.equals(s.getTotalElapsedTime()) == false) {
                    super.addTestFailure("smil_metaAudioDtbTotElaAccurate",
                            "Declared total elapsed time ("
                                    + s.getTotalElapsedTime()
                                    + ") does not match actual elapsed time ("
                                    + expectedElapsedTime + ")", null, null);
                }
            }
        } catch (Exception e) {
            throw new ZedCustomTestException(
                    e.getClass().getName()
                            + " when performing test smil_metaAudioDtbTotElaAccurate|smil_metaTextDtbTotElaAccurate on file"
                            + f.getName() + ": " + e.getMessage());
        }


        // TEST: smil_noterefLink
        // TEST: smil_annorefLink
        // all smil constructs representing dtbook
        // annorefs and noterefs have links
        // logic:
        // for each element in smil
        // find out if this element is refd by annoref or noteref
        // if so, find out if anchor descendant or ancestor
        //TODO this may be optimized by having dtbook collect a annoref+noteref hashset

        try {
            Map<String,String> smilIds = new HashMap<String,String>();// <SmilId,testId>
            for (XmlFileElement smilElement : s.getXmlFileElements()) {
                if (smilElement.getLocalName().matches("par|seq|audio|text")) {
                    XmlFileElement dtbookElement = getReferencingTextFileElement(smilElement, s);
                    if (dtbookElement != null) {
                        if (dtbookElement.getLocalName().matches("noteref|annoref")) {
                            // collect the id of this smil container
                            if (dtbookElement.getLocalName().equals("noteref")) {
                                smilIds.put(smilElement.getAttributeValueL("id"),"smil_noterefLink");
                            } else if (dtbookElement.getLocalName().equals("annoref")) {
                                smilIds.put(smilElement.getAttributeValueL("id"),"smil_annorefLink");
                            }
                        }// if(dtbkelem.getLocalName().matches("noteref|annoref"))
                    }// if(dtbkelem!=null)
                }// if(smelem.getLocalName().matches("par|seq"))
            }// for(Iterator it = s.getElements().iterator(); it.hasNext();)

            // we now have the ids of all smil elements that are referenced by
            // annoref, noteref
            // build and execute a schematron rule
            if (!smilIds.isEmpty()) {
                StringBuffer sch = new StringBuffer();
                StringBuffer pattern = new StringBuffer();

                sch.append(
                        "<schema xmlns='http://www.ascc.net/xml/schematron'>\n");
                sch.append(
                        "<ns prefix='smil' uri='http://www.w3.org/2001/SMIL20/'/>\n");

                int count = 0;
                for (String smilId : smilIds.keySet()) {
                    count++;
                    String testId = smilIds.get(smilId);
                    pattern.append("<pattern name='test_" + count
                            + "' id='test_" + count + "'>\n");
                    pattern.append("<rule context=\"//*[@id='" + smilId
                            + "']\">\n");
                    pattern
                            .append("<assert test='ancestor::smil:a or descendant::smil:a'>\n");
                    if (testId.equals("smil_noterefLink")) {
                        pattern.append("[sch][zedid::smil_noterefLink]");
                    } else {
                        pattern.append("[sch][zedid::smil_annorefLink]");
                    }
                    pattern.append("\n</assert>\n</rule>\n</pattern>\n");
                    sch.append(pattern);
                    pattern.delete(0, pattern.length());
                }// for (Iterator it = SmilIds.keySet()
                sch.append("</schema>");
                // run the test
                RelaxngSchematronValidator schval = new RelaxngSchematronValidator(this, sch.toString());
                if (!schval.isValid(s)) {
                    // there were errors, reported to this.sax.errorhandler
                }
            }// if (!SmilIds.isEmpty())

        } catch (Exception e) {
            throw new ZedCustomTestException(
                    e.getClass().getName()
                            + " when performing test smil_noterefLink|smil_annorefLink on file"
                            + f.getName() + ": " + e.getMessage());
        }

        //TEST: math_textDestination
        // Any SMIL text URI that refers to Dtbook content within the MathML namespace 
        // refers only to the math element
        try {
        	//loop trough smil file, find text elems with @type=math
        	//for each found, check that text src destinaton URI is on a math element
        	for(Object o : s.getXmlFileElements()) {
        		XmlFileElement xfe = (XmlFileElement)o;
        		if(xfe.getLocalName().equals("text") 
        				&& xfe.getAttributeValueL("type")!=null 
        					&& xfe.getAttributeValueL("type").equals(Namespaces.MATHML_NS_URI) ) {
        			
        			//get the dtbook element thats referenced from this smil elements src
        			URI srcUri;
        			File file;
        			if(xfe.getAttributeValueL("src")!=null) {
	        			try{
		        			srcUri = new URI(xfe.getAttributeValueL("src"));
		                    file = new File(URIUtils.resolve(s.toURI(), srcUri).getPath());
		        		} catch (Exception e) {
		        			//tested above, be silent
		        			continue;
		        		}	
	                    TextFile dtbk = (TextFile) p.getManifest().get(file.getCanonicalPath());	                
		                if(dtbk!=null){
		                	XmlFileElement dtbookElement = dtbk.getXmlFileElementById(srcUri.getFragment());
		                	if(dtbookElement==null){
		                		//tested above, be silent
		                		continue;
		                	}
		                	if(!dtbookElement.getLocalName().equals("math")) {
		            			super.addTestFailure("math_textDestination","URI is: "+ xfe.getAttributeValueL("src") + " in: " + s.getName(),xfe.getAttributeValueL("src"),null);
		            		}
		                	
		                }else{//if(dtbk!=null)
		                	//tested above, be silent            	
		                }//if(dtbk!=null)    
        			}
        		}
        	}
        } catch (Exception e) {
            throw new ZedCustomTestException(
                    e.getClass().getName()
                            + " when performing test math_textDestination on file"
                            + f.getName() + ": " + e.getMessage());
        }
        
        //TEST: math_SMILTextTypeAttribute
		//In a SMIL par referring to MathML, the text element must carry a type attribute with the 
        //value of the MathML Namespace URI.
        try {
        	//for efficiencys sake, first query dtbook on whether math is present at all
        	boolean mathPresent = false;
        	if(p.getTextFiles()!=null) {
	        	for(Object o : p.getTextFiles().values()) {
	        		TextFile tf = (TextFile) o;
	        		if(tf.getNamespaceURIs().contains(Namespaces.MATHML_NS_URI)) {
	        			mathPresent = true;
	        		}
	        	}
        	}
        	
        	if(mathPresent) {
	        	for(Object o : s.getXmlFileElements()) {
	        		XmlFileElement xfe = (XmlFileElement)o;
	        		if(xfe.getLocalName().equals("text")) {
	        			//get the dtbook element referred to
	        			URI srcUri;
	        			File file;
	        			try{
		        			srcUri = new URI(xfe.getAttributeValueL("src"));
		                    file = new File(URIUtils.resolve(s.toURI(), srcUri).getPath());
		        		} catch (Exception e) {
		        			//tested above, be silent
		        			continue;
		        		}	
	                    TextFile dtbk = (TextFile) p.getManifest().get(file.getCanonicalPath());	                
		                if(dtbk!=null){	        			        			
		        			XmlFileElement dtbookDestination = dtbk.getXmlFileElementById(srcUri.getFragment());
		        			//if a math element is referred to, we need type="math"
		        			if(dtbookDestination!=null && dtbookDestination.getNsURI().equals(Namespaces.MATHML_NS_URI)) {
		        				if(xfe.getAttributeValueL("type")==null 
			    						|| !xfe.getAttributeValueL("type").equals(Namespaces.MATHML_NS_URI) ) {
			    					super.addTestFailure("math_SMILTextTypeAttribute",null,xfe.getAttributeValueL("src"),null);
			    				}	
		        			}
		                }
	        		}	
	        	}
        	}
        } catch (Exception e) {
            throw new ZedCustomTestException(
                    e.getClass().getName()
                            + " when performing test math_textDestination on file"
                            + f.getName() + ": " + e.getMessage());
        }
    }

    /**
     * A helper method.
     * @param smilElement
     *            an XmlFileElement in a SMIL file that may be referenced by a
     *            XmlFileElement in dtbook via a URI-ID link
     * @param s The SmilFile in which smilElement lives           
     * @return the first found dtbook XmlFileElement that references smilElement, or null
     *         if no reference to this smilElement was found
     */
	static XmlFileElement getReferencingTextFileElement(XmlFileElement smilElement, SmilFile s) throws Exception {
    	String idValue = smilElement.getAttributeValueL("id");
    	if(idValue==null) return null;
        if (s.getPackage().getTextFiles() != null) {
        	for (TextFile dtbookFile : s.getPackage().getTextFiles().values()) {
                //create the URI that the dtbook smilref is expected to have
                //TODO if dtbook and smil is not in same folder, this will break:
                String expectedURI = s.getName() + "#" + idValue;
                XmlFileElement dtbookElement = dtbookFile.getXmlFileElementBySmilrefValue(expectedURI);
                if(dtbookElement!=null)return dtbookElement;
            }// for (Iterator iter =
        }// if(s.getPackage().getTextFiles()!=null)
        return null;
    }
    
    /**
     * A helper method.
     * @param smelem
     *            a SmilFileElement that may reference a
     *            customTestElement in the head of s
     * @param s The smilfile in which smelem lives           
     * @return the XmlFileElement head/customAttributes/customTest, or null
     *         if no reference to a customTest element was found
     */  
    static XmlFileElement getCustomTestElement(XmlFileElement smelem, SmilFile s) {    	 
    	String idref = smelem.getAttributeValueL("customTest");
    	if(idref!=null) {
    		return s.getXmlFileElementById(smelem.getAttributeValueL("customTest"));
    	}
    	return null;
    }
    
    /**
     * A helper method.
     * @param idValue a value that may be the value of a customTest attribute on a smil:body descendant
     * @param s The smilfile in which the value may occur           
     * @return a List&lt;XmlFileElement&gt; of smil:body descendants where 
     * inparam idValue=attribute customTest value, or null if no match was found
     */  
	static List<XmlFileElement> getCustomTestReferers(String idValue, SmilFile s) {    	
    	List<XmlFileElement> returnSet = new LinkedList<XmlFileElement>();
    	for (XmlFileElement smilElement : s.getXmlFileElements()) {
    		String ctestAttr = smilElement.getAttributeValueL("customTest");
    		if(ctestAttr!=null && ctestAttr.equals(idValue)) {
    			returnSet.add(smilElement);
    		}
    	}
    	return returnSet;    
    }
    
    public void warning(SAXParseException exception) {
        try {
        	SchematronMessage sm = new SchematronMessage(exception.getMessage());        	
            super.addTestFailure(sm.getMessage("zedid"), "", null, Integer.toString(exception.getLineNumber()));
        } catch (ZedCustomTestException e) {
        	System.err.println("ZedCustomTestException at SmilTests.errh.warning");
        } catch (ValidationException e) {
        	System.err.println("ValidationException at SmilTests.errh.warning");;
		}

    }

    public void error(SAXParseException exception) {
        try {
        	SchematronMessage sm = new SchematronMessage(exception.getMessage());        	
            super.addTestFailure(sm.getMessage("zedid"), "", null, Integer.toString(exception.getLineNumber()));
        } catch (ZedCustomTestException e) {
        	System.err.println("ZedCustomTestException at SmilTests.errh.error");
        } catch (ValidationException e) {
        	System.err.println("ValidationException at SmilTests.errh.error");;
		}

    }

    public void fatalError(SAXParseException exception) {
        try {
        	SchematronMessage sm = new SchematronMessage(exception.getMessage());        	
            super.addTestFailure(sm.getMessage("zedid"), "", null, Integer.toString(exception.getLineNumber()));
        } catch (ZedCustomTestException e) {
        	System.err.println("ZedCustomTestException at SmilTests.errh.fatalError");
        } catch (ValidationException e) {
        	System.err.println("ValidationException at SmilTests.errh.fatalError");;
		}
    }
}