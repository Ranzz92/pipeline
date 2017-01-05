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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.daisy.util.text.URIUtils;
import org.daisy.util.xml.SmilClock;
import org.daisy.zedval.engine.AudioFile;
import org.daisy.zedval.engine.ImageFile;
import org.daisy.zedval.engine.ManifestFile;
import org.daisy.zedval.engine.NcxFile;
import org.daisy.zedval.engine.SmilFile;
import org.daisy.zedval.engine.XmlFileElement;
import org.daisy.zedval.engine.ZedConstants;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;

/**
 * Performs customtests on ncx
 * @author mgylling
 * @author jpritchett
 */

public class NcxTests extends ZedCustomTest {
	/**
	 * Performs customtests on ncx
	 * @param f
	 *            An NcxFile instance
	 */
	public void performTest(ZedFile f)
			throws ZedCustomTestException {
		NcxFile n;
		ManifestFile aManifestItem;

		this.resultDoc = null; // Clear out the results document

		// If n isn't an NCXFile, this is a fatal error
		try {
			n = (NcxFile) f; // Make our life simpler by removing need for
			// casts
		} catch (ClassCastException castEx) {
			throw new ZedCustomTestException(castEx.getMessage());
		}

		// TEST: NCX file has .ncx extension (ncx_fileExtn)
		if (n.getName().matches(".*\\.[n][c][x]") == false) {
			super.addTestFailure("ncx_fileExtn", "NCX file "
					+ n.getName()
					+ " does not use .ncx extension", null, null);
		}

		// TEST: filename restriction (ncx_fileName)
		if (!GenericTests.hasValidName(n)) {
			super.addTestFailure("ncx_fileName", "NCX file "
					+ n.getName()
					+ " uses disallowed characters in its name", null, null);
		}

		//TEST: path restriction (ncx_relPath)
		try{
			if (!GenericTests.hasValidRelativePath(n, n.getPackage())) {
				super.addTestFailure("ncx_relPath", "NCX file "
					+ n.getName()
					+ " uses disallowed characters in its path, relative to packagefile", null, null);
			}
		} catch (Exception e) {
	        throw new ZedCustomTestException("Exception: " + e.getMessage());
	    }		

		// TEST: Resource file public identifier (ncx_prologPubId)
		if (n.getDoctypePublicId() == null) {
			super.addTestFailure("ncx_prologPubId", "NCX file "
					+ n.getName()
					+ " has no DTD public identifier", null, null);
		} else if (!n.getDoctypePublicId().equals(ZedConstants.PUBLIC_ID_NCX_Z2005)) {
			super.addTestFailure("ncx_prologPubId", "NCX file "
					+ n.getName()
					+ " uses incorrect DTD public identifier: "
					+ n.getDoctypePublicId(), null, null);
		}

		// TEST: NCX file only references files that are items in manifest
		// (ncx_manifestRefs)
		if (null != n.getFileRefs()) {
			for (File aFileRef : n.getFileRefs().values()) {
				try {
					if (n.getPackage().getManifest().get(aFileRef.getCanonicalPath()) == null) {
						super.addTestFailure("ncx_manifestRefs", "NCX file references non-manifest file "
								+ aFileRef.getName(), aFileRef.getName(), null);
					}
				} catch (IOException e) {
					throw new ZedCustomTestException("IOException: "
							+ e.getMessage());
				}
			}
		} else {
			// filerefs was null: ncx didnt reference any files
		}

		// TEST: ncx_UidOpfUid
		try {
			if (!n.getUid().equals(n.getPackage().getIdentifier())) {
				super.addTestFailure("ncx_UidOpfUid", "Ncx file "
						+ n.getName()
						+ " dtb:uid does not match package identifier", null, null);
			}
        } catch (Exception e) {
            throw new ZedCustomTestException(
                    e.getClass().getName()
                            + " when performing test ncx_UidOpfUid on file "
                            + f.getName() + ": " + e.getMessage());
        }

		// TEST: dtb:depth content attribute value indicates depth of structure
		// of the DTB as exposed by the NCX (ncx_metaDtbDepthValueCorrelated)
        try{
			if (n.getDepth() != null
					&& n.expectedDepth() != null
					&& n.getDepth().compareTo(n.expectedDepth()) != 0) {
				super.addTestFailure("ncx_metaDtbDepthValueCorrelated", "dtb:depth="
						+ n.getDepth()
						+ ", actual depth of NCX="
						+ n.expectedDepth(), null, null);
			}
        } catch (Exception e) {
            throw new ZedCustomTestException(
                    e.getClass().getName()
                            + " when performing test ncx_metaDtbDepthValueCorrelated on file "
                            + f.getName() + ": " + e.getMessage());
        }

		//TEST: ncx_metaDtbmaxPageNumValueCorrelated
        //TEST: ncx_allPageTargetsHaveValueAttribute
		//dtb:maxPageNumber content attribute value indicates 
		//highest page value
        if(n.getAllPageNormalTargetsHaveValueAttribute()) {
			if (n.getMaxPageNumber() != n.expectedMaxPageNumber()) {
				super.addTestFailure("ncx_metaDtbmaxPageNumValueCorrelated", "expected number: "
						+ n.expectedMaxPageNumber(), null, null);
			}
        }else{
        	//we could not perform the ncx_metaDtbmaxPageNumValueCorrelated test
        	//we issue
        	super.addTestFailure("ncx_allPageTargetsHaveValueAttribute", "The test ncx_metaDtbmaxPageNumValueCorrelated could not be performed", null, null);
        }


		// TEST: src attribute on content element references SMIL file
        try{
			if (n.getContentFileRefs() != null) {
				for (File aFileRef : n.getContentFileRefs().values()) {
					aManifestItem = (ManifestFile) (n.getPackage().getManifest().get(aFileRef.getCanonicalPath()));
					if (aManifestItem != null
							&& (aManifestItem instanceof SmilFile) == false) {
						super.addTestFailure("ncx_srcSMIL", "NCX content element references non-SMIL file "
								+ aFileRef.getName(), aFileRef.getName(), null);
					}
				}
			}
        } catch (Exception e) {
            throw new ZedCustomTestException(
                    e.getClass().getName()
                            + " when performing test ncx_srcSMIL on file "
                            + f.getName() + ": " + e.getMessage());
        }

		// TEST: src attribute on audio element references audio file
        try{
			if (n.getAudioFileRefs() != null) {
				for (File aFileRef : n.getAudioFileRefs().values()) {
					aManifestItem = (ManifestFile) (n.getPackage().getManifest().get(aFileRef.getCanonicalPath()));
					if (aManifestItem != null
							&& (aManifestItem instanceof AudioFile) == false) {
						super.addTestFailure("ncx_srcAudio", "NCX audio element references non-audio file "
								+ aFileRef.getName(), aFileRef.getName(), null);
					}
				}
			}
        } catch (Exception e) {
            throw new ZedCustomTestException(
                    e.getClass().getName()
                            + " when performing test ncx_srcAudio on file "
                            + f.getName() + ": " + e.getMessage());
        }

		// TEST: src attribute on img element references image file
        try{
			if (n.getImageFileRefs() != null) {
				for (File aFileRef : n.getImageFileRefs().values()) {
					aManifestItem = (ManifestFile) (n.getPackage().getManifest().get(aFileRef.getCanonicalPath()));
					if (aManifestItem != null
							&& (aManifestItem instanceof ImageFile) == false) {
						super.addTestFailure("ncx_srcImg", "NCX img element references non-image file "
								+ aFileRef.getName(), aFileRef.getName(), null);
					}
				}
			}
        } catch (Exception e) {
            throw new ZedCustomTestException(
                    e.getClass().getName()
                            + " when performing test ncx_srcImg on file "
                            + f.getName() + ": " + e.getMessage());
        }

        
		// TEST: ncx_clipEndBeforeEOF
		// 		audio element clipEnd attribute value is less than end of audio file
		// TEST: ncx_clipEndAfterclipBegin
		// 		audio element clipEnd attribute value is greater than clipBegin
        // TEST: ncx_contentSrcFragmentResolves
        //		src attribute URI on content element contains a fragment identifier that resolves
     	// TEST: ncx_contentSrcValue
        //		is a valid URI
     	// TEST: ncx_audioSrcValue
        //		is a valid URI
        // TEST: ncx_imgSrcValue
        //		is a valid URI        
		try{
			SmilFile s = null;
			for (XmlFileElement ncxElement : n.getXmlFileElements()) {
    			URI srcUri = null;
    			File file = null;
    			
				if (ncxElement.getLocalName().equals("audio")) {
					String src = ncxElement.getAttributeValueL("src");
					String clipEnd = ncxElement.getAttributeValueL("clipEnd");
					String clipBegin = ncxElement.getAttributeValueL("clipBegin");
					
					if (clipEnd != null && src != null) {
						try {
							try{
								srcUri = new URI(src);
							}catch (Exception e) {
								super.addTestFailure("ncx_audioSrcValue", "NCX file "+ n.getName()+ "contains an audio src value "+ src + " which is invalid: " + e.getMessage(),null,null);
							}
			                file = new File(URIUtils.resolve(n.toURI(), srcUri).getPath());
							AudioFile af = (AudioFile) n.getPackage().getManifest().get(file.getCanonicalPath());
							SmilClock endDur = new SmilClock(clipEnd);
							if (endDur.millisecondsValue() > af.getDuration().millisecondsValue()) {
								super.addTestFailure("ncx_clipEndBeforeEOF", "NCX file "+ n.getName()+ " contains a reference to the time " + clipEnd + " in the audiofile " + src + " but the duration of the " + "audiofile is shorter than that, namely " + af.getDuration().millisecondsValue() + " ms.", null, null);
							}
						} catch (Exception e) {
							continue;
						}
					} else { 
						//reported by other test
					}					
					if (clipEnd != null && clipBegin != null) {
						SmilClock begin = new SmilClock(clipBegin);
						SmilClock end = new SmilClock(clipEnd);
						if (begin.millisecondsValue() >= end.millisecondsValue()) {
							super.addTestFailure("ncx_clipEndAfterclipBegin", begin.toString(SmilClock.FULL) + " is equal to or higher than " + end.toString(SmilClock.FULL) + " on audio element with id " + ncxElement.getAttributeValueL("id"), null, null);
						}
					}else{ 
						//reported by other test
					} 					
				}// if (ncxElement.getLocalName().equals("audio")) 
				else if (ncxElement.getLocalName().equals("img")) {
					String srcValue = ncxElement.getAttributeValueL("src");
					if(srcValue!=null){		        		
						try{
		        			srcUri = URIUtils.resolve(n.toURI(), new URI(srcValue));
						}catch (Exception e) {
							super.addTestFailure("ncx_imgSrcValue", "NCX file "+ n.getName()+ "contains a img src value "+ srcValue + " which is invalid: " + e.getMessage(),null,null);						
		        			continue;
		        		}
					}	
				}//if (ncxElement.getLocalName().equals("img")) {
				else if (ncxElement.getLocalName().equals("content")) {
					String srcValue = ncxElement.getAttributeValueL("src");
					if(srcValue!=null){		        		
		        		try{
		        			srcUri = URIUtils.resolve(n.toURI(), new URI(srcValue));
						}catch (Exception e) {
							super.addTestFailure("ncx_contentSrcAttrValue", "NCX file "+ n.getName()+ "contains a content src value "+ srcValue + " which is invalid: " + e.getMessage(),null,null);						
		        			continue;
		        		}	
		                file = new File(srcUri.getPath());
		                //get a new SmilFile from package only if it isnt already here
		                
		                if(s==null || !s.getCanonicalPath().contentEquals(new StringBuffer(file.getCanonicalPath()))) {//PK: java 1.4 does not have contentEquals(CharSequence cs)
		                	 s = (SmilFile) n.getPackage().getSmilFiles().get(file.getCanonicalPath());
		                }
		                if (s != null) {
		                	//we have the SmilFile we need
		                	XmlFileElement smilElement = s.getXmlFileElementById(srcUri.getFragment());
		                	if(smilElement==null) {
		                		super.addTestFailure("ncx_contentSrcFragmentResolves","NCX file contains the content src attribute value "+ srcValue+ " which points to a nonexisting fragment",null, null);
		                	}
		                }													
					}//if(srcValue!=null)					
				}								
			}
        } catch (Exception e) {
            throw new ZedCustomTestException(e.getClass().getName() + " when performing test ncx_clipEndBeforeEOF on file " + f.getName() + ": " + e.getMessage());
        }

        
		// TEST: ncx_smilCustomTestMatch
		// 		each smilCustomTest element in NCX has its id, override,
		// 		and defaultState attrs duplicated in one or more customTest
		// 		elements in one or more SMIL files
		// TEST: ncx_bookStructReq
		// 		smilCustomTests for linenum, pagenum, note, noteref,
		// 		annotation, optional prodnote and optional sidebar
		// 		have bookStruct attributes
		// TEST: ncx_bookStructValue
		// 		smilCustomTests for linenum, pagenum, note, noteref,
		// 		annotation, optional prodnote and optional sidebar
		// 		have corresponding bookStruct values

		try {
//			ArrayList customTestInfoList = new ArrayList();
			for (XmlFileElement ncxCustomTest : n.getSmilCustomTestElements()) {
				boolean foundNcxToSmilCustomTestMatch = false;
				Map<String, SmilFile> smilFiles = n.getPackage().getSmilFiles();
				for (SmilFile s : smilFiles.values()) {
										
					for (XmlFileElement smilCustomTest : s.getCustomTestElements()) {
						// find out if this smil test matches the current ncx test
						try{
							if (ncxCustomTest.getAttributeValueL("id").equals(smilCustomTest.getAttributeValueL("id"))
									&& ncxCustomTest.getAttributeValueL("defaultState").equals(smilCustomTest.getAttributeValueL("defaultState"))
									&& ncxCustomTest.getAttributeValueL("override").equals(smilCustomTest.getAttributeValueL("override"))) {
								// the current smiltest matches the current ncx test
								foundNcxToSmilCustomTestMatch = true;
								//carry on and check bookstruct stuff -- only when dtbook available
								if(n.getPackage().getTextFiles() != null){
									List<XmlFileElement> referers = SmilTests.getCustomTestReferers(smilCustomTest.getAttributeValueL("id"),s);
									//referers are those body descendants that IDREFer to our current smil:head customTest
									//and therefore also implicitly to our current ncx customTest
									for (XmlFileElement smilBodyElem : referers) {
										//todo only get if diff in idref
										XmlFileElement dtbookElem = SmilTests.getReferencingTextFileElement(smilBodyElem, s);
										if(dtbookElem!=null){
											//this dtbkelem references a smil tc 
											//with the customtest that we started out in ncx with
											if(requiresBookstruct(dtbookElem)) {
												if (ncxCustomTest.getAttributeValueL("bookStruct") == null) {
													// this ncx:smilCustomTest didnt have a bookstruct attr at all
													super.addTestFailure("ncx_bookStructReq", "no bookStruct atttribute on ncx:SmilCustomTest", null, null);
												} else {
													if(!dtbkElemMatchesNcxBookstruct(dtbookElem.getLocalName(),ncxCustomTest.getAttributeValueL("bookStruct"))) {
														//no match between dtbook element and ncx bookStruct
														super.addTestFailure("ncx_bookStructValue", "smilCustomTest with bookStruct "+ ncxCustomTest.getAttributeValueL("bookStruct") + "corresponds to a customTest in "+ s.getName()+ " with id "+ smilCustomTest.getAttributeValueL("id")+ " which applies to a "+ dtbookElem.getLocalName()+ " element in dtbook.", null, null);
													}
												}
//												//collect defaultstate info, to be tested below												
//												String defaultState = smilCustomTest.getAttributeValueL("defaultState");
//												if (defaultState != null) {
//													//add to collection
//													customTestInfoList.add(new customTestInfo(smilBodyElem , defaultState, dtbookElem, s));
//												}
											}											
										}//if(dtbookElem!=null)
										//all items in this collection should be the same element,
										//only need to test the first one
										break;
									}//while(refererIterator.hasNext())
								}//if(n.getPackage().getTextFiles() != null)
								break;
							} //if (ncxCustomTest.getAttributeValueL("id").equals
	                    } catch (Exception e) {
                        	//a missing property, reported by other test
                        }
					}// while (smilCustomTestsIterator.hasNext())
					//if (foundNcxToSmilCustomTestMatch) break;
				}// while(smilFileIterator.hasNext())
				if (!foundNcxToSmilCustomTestMatch) {
					super.addTestFailure("ncx_smilCustomTestMatch", "NCX smilCustomTest with id " + ncxCustomTest.getAttributeValueL("id") + " does not match any customTest in smil", null, null);
				}
			}// for(Iterator ncxCustomTestIterator = ncxCustomTests.iterator();

			// TEST: ncx_defaultStateSame
			//		For a given customTest, defaultState must 
			//		have the same value in all SMIL files
			//		Since we verify general ncx+intersmil integrity above, all this test needs to do
			//      is to check that all smil ctests with id value x has the same value for defaultState
			Map<String,SmilFile> smilFiles = n.getPackage().getSmilFiles();
			List<XmlFileElement> customTests = new ArrayList<XmlFileElement>();
			for (SmilFile s : smilFiles.values()) {
				//if (s.getCustomTestElements()!=null) customTests.addAll(s.getCustomTestElements());									
				customTests.addAll(s.getCustomTestElements());
			} 
			
			//we now have all smil customtests in the arraylist
			for (XmlFileElement outer : customTests) {
				String outerId = outer.getAttributeValueL("id");
				String outerDefaultState = outer.getAttributeValueL("defaultState");
				if(outerId != null && outerDefaultState != null) {
					for (XmlFileElement inner : customTests) {
						String innerId = inner.getAttributeValueL("id");
						if(innerId!= null && innerId.equals(outerId)) {
							String innerDefaultState = inner.getAttributeValueL("defaultState");
							if(innerDefaultState == null || ! innerDefaultState.equals(outerDefaultState)) {
								super.addTestFailure("ncx_defaultStateSame", "Tests with id values " + outerId +" and " + innerId, null, null);
							}
						}						
					}					
				}				
			}
			
			
			
//			//old version of ncx_defaultStateSame
//			if (n.getPackage().getDtbMultimediaTypeAsDeclared().matches("audioFullText|textOnly|textPartAudio")) {
//				//now we have collected all customTest in all smilfiles
//				//now make sure all dtbook elems have same defaultstate
//				ArrayList alreadyTestDtbookElemNames = new ArrayList();
//				for (Iterator it = customTestInfoList.iterator(); it.hasNext();) {
//					customTestInfo cInfo = (customTestInfo) it.next();
//					if (alreadyTestDtbookElemNames.indexOf(cInfo.dtbookRefererElem.getLocalName())<0) {
//						for (Iterator it2 = customTestInfoList.iterator(); it2.hasNext();) {
//							customTestInfo cInfo2 = (customTestInfo) it2.next();																					
//							if (cInfo.dtbookRefererElem.getLocalName().equals(cInfo2.dtbookRefererElem.getLocalName())) {
//								if (!cInfo.defaultStateValue.equals(cInfo2.defaultStateValue)) {
//									//same dtbook elem but different defaultState 
//									super.addTestFailure("ncx_defaultStateSame", "mismatch between element with id "
//											+ cInfo.smilElement.getAttributeValueL("id")
//											+ " in file "
//											+ cInfo.s.getName()
//											+ " and element with id "
//											+ cInfo2.smilElement.getAttributeValueL("id")
//											+ " in file "
//											+ cInfo2.s.getName()
//											+ ". Given defaultstate values are "
//											+ cInfo.defaultStateValue
//											+ " and "
//											+ cInfo2.defaultStateValue
//											+ ". Dtbook element names are "
//											+ cInfo.dtbookRefererElem.getLocalName()
//											+ " and "
//											+ cInfo2.dtbookRefererElem.getLocalName()
//											+ " (and the latter should be identical).", null, null);
//								}
//							}
//						}
//					}
//					alreadyTestDtbookElemNames.add(cInfo.dtbookRefererElem.getLocalName());
//				}//for (Iterator it = customTestInfoList.iterator()
//			}//if (n.getPackage().getDtbMultimediaTypeAsDeclared()
		
		} catch (Exception e) {
			throw new ZedCustomTestException(e.getClass().getName() + " when performing test ncx_smilCustomTestMatch on file " + f.getName() + ": " + e.getMessage());
		}
	}//performTest
	
	/**
	 * @return true if the inparam dtbook element is one that the zed spec requires to have a bookStruct attribute 
	 * on its ncx customTest
	 */
	private boolean requiresBookstruct(XmlFileElement dtbkelem) {
		if (dtbkelem.getLocalName().matches("linenum|pagenum|note|noteref|annotation")
				|| (dtbkelem.getLocalName().matches("prodnote|sidebar") 
						&& (dtbkelem.getAttributeValueL("render") != null 
						&& dtbkelem.getAttributeValueL("render") .equals("optional")))) {
			return true;
		}
		return false;
	}
	
	/**
	 * @return true if the inparam dtbook element name and the bookstruct value map
	 */
	private boolean dtbkElemMatchesNcxBookstruct(String dtbookElemLocalName, String bookStructValue) {
		dtbookElemLocalName = dtbookElemLocalName.intern();
		bookStructValue = bookStructValue.intern();
		
		if (bookStructValue=="LINE_NUMBER"
				&& dtbookElemLocalName=="linenum") {
			return true;
		} else if (bookStructValue=="PAGE_NUMBER"
				&& dtbookElemLocalName=="pagenum") {
			return true;
		} else if (bookStructValue=="NOTE"
				&& dtbookElemLocalName=="note") {
			return true;
		} else if (bookStructValue=="NOTE_REFERENCE"
				&& dtbookElemLocalName=="noteref") {
			return true;
		} else if (bookStructValue=="ANNOTATION"
				&& dtbookElemLocalName=="annotation") {
			return true;
		} else if (bookStructValue=="OPTIONAL_SIDEBAR"
				&& dtbookElemLocalName=="sidebar") {
			return true;
		} else if (bookStructValue=="OPTIONAL_PRODUCER_NOTE"
				&& dtbookElemLocalName=="prodnote") {
			return true;
		}
		return false;
	}
	
//	class customTestInfo {
//		XmlFileElement smilElement;
//		String defaultStateValue;
//		XmlFileElement dtbookRefererElem;
//		SmilFile s;
//
//		customTestInfo(
//				XmlFileElement smelem,
//				String defaultStateValue,
//				XmlFileElement dtbookRefererElem,
//				SmilFile s) {
//			this.smilElement = smelem;
//			this.defaultStateValue = defaultStateValue;
//			this.dtbookRefererElem = dtbookRefererElem;
//			this.s = s;
//		}
//	}
	
	
}