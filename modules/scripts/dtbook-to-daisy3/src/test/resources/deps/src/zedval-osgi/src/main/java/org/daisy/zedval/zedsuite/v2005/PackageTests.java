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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.daisy.util.text.URIUtils;
import org.daisy.util.xml.Namespaces;
import org.daisy.util.xml.SmilClock;
import org.daisy.util.xml.peek.PeekResult;
import org.daisy.util.xml.peek.Peeker;
import org.daisy.util.xml.peek.PeekerPool;
import org.daisy.zedval.engine.AacFile;
import org.daisy.zedval.engine.AudioFile;
import org.daisy.zedval.engine.CssFile;
import org.daisy.zedval.engine.DtdFile;
import org.daisy.zedval.engine.ImageFile;
import org.daisy.zedval.engine.JpegFile;
import org.daisy.zedval.engine.ManifestFile;
import org.daisy.zedval.engine.Mp3File;
import org.daisy.zedval.engine.NcxFile;
import org.daisy.zedval.engine.PackageFile;
import org.daisy.zedval.engine.PngFile;
import org.daisy.zedval.engine.Referring;
import org.daisy.zedval.engine.ResourceFile;
import org.daisy.zedval.engine.SmilFile;
import org.daisy.zedval.engine.SvgFile;
import org.daisy.zedval.engine.TextFile;
import org.daisy.zedval.engine.WavFile;
import org.daisy.zedval.engine.XmlFile;
import org.daisy.zedval.engine.XmlFileElement;
import org.daisy.zedval.engine.XsltFile;
import org.daisy.zedval.engine.ZedConstants;
import org.daisy.zedval.engine.ZedCustomTest;
import org.daisy.zedval.engine.ZedCustomTestException;
import org.daisy.zedval.engine.ZedFile;

/**
 * Performs custom tests on package file
 *<p>
 *<em>Note:</em> In default ZedVal application (and probably in
 *others), package file existance test will probably never be executed
 *here. Instead, it will throw an application error earlier on in the
 *process. This test is included here for completeness of the test
 *processor suite.
 *</p>
 * @author mgylling
 * @author jpritchett
 */
public class PackageTests extends ZedCustomTest {
	ManifestFile mf;	
	long actualTotalTime;
	SmilClock actualTime;
	SmilFile sf;

	/**
	 * Performs custom tests on package file
	 * @param f
	 *            A PackageFile instance
	 */
	public void performTest(ZedFile f)
			throws ZedCustomTestException {
		PackageFile p;

		this.resultDoc = null; // Clear out the results document

		// If pkg isn't a PackageFile, this is a fatal error
		try {
			p = (PackageFile) f; // Make our life simpler by removing need
			// for casts
		} catch (ClassCastException castEx) {
			throw new ZedCustomTestException(castEx.getMessage());
		}

		// TEST: Package file exists (opf_exists)
		if (p.exists() == false) {
			super.addTestFailure("opf_exists", "Package file "
					+ p.getName()
					+ " does not exist", null, null);
		}

		// TEST: Package file is readable (opf_isReadable)
		else if (p.canRead() == false) {
			super.addTestFailure("opf_isReadable", "Package file "
					+ p.getName()
					+ " is not readable", null, null);
		}

		// TEST: Package file has .opf extension (opf_fileExtn)
		if (p.getName().matches(".*\\.[o][p][f]") == false) {
			super.addTestFailure("opf_fileExtn", "Package file "
					+ p.getName()
					+ " does not use .opf extension", null, null);
		}

		// TEST: filename restriction (opf_fileName)
		if (!GenericTests.hasValidName(p)) {
			super.addTestFailure("pkg_fileName", "Package file "
					+ p.getName()
					+ " uses disallowed characters in its name", null, null);
		}

		// TEST: Package file public identifier (opf_prologPubId)
		if (p.getDoctypePublicId() == null) {
			super.addTestFailure("opf_prologPubId", "Package file "
					+ p.getName()
					+ " has no DTD public identifier", null, null);
		} else if (!p.getDoctypePublicId().equals(ZedConstants.PUBLIC_ID_OPF_Z2005)) {
			super.addTestFailure("opf_prologPubId", "Package file "
					+ p.getName()
					+ " uses incorrect DTD public identifier: "
					+ p.getDoctypePublicId(), null, null);
		}

		// Iterate through the manifest and evaluate the tests. Note that
		// failure of existence test
		// causes readability test to be skipped (since failure is a foregone
		// conclusion)
				
		for (ManifestFile mf : p.getManifest().values()) {
			// TEST: Manifest file exists (opf_mnfIntegrityExists)
			if ((mf.exists() && !mf.matchesCase()) || !mf.exists()) {
				super.addTestFailure("opf_mnfIntegrityExists", "Manifest references nonexistent file "
						+ mf.getManifestURI(), mf.getManifestURI(), null);
			}
			// TEST: Manifest file is readable (opf_mnfIntegrityReadable)
			else if (mf.canRead() == false) {
				super.addTestFailure("opf_mnfIntegrityReadable", "Manifest references unreadable file "
						+ mf.getName(), mf.getName(), null);
			}
		}

		// TEST: x-metadata dtb:totalTime value is the sum of the durations of
		// all SMIL files in the spine
		// Iterate through the spine, add up the expectedDurations, and compare
		// to the totalTime metadata

		// TEST opf_spnItemRefIdrefSmil: spine itemref idref attributes points
		// to the id of a SMIL filelisted in manifest
		try {
			if (p.getSpine() != null) {
				actualTotalTime = 0;
				for (ManifestFile mf : p.getSpine().values()) {
					if (mf instanceof SmilFile) {
						sf = (SmilFile) mf;
						try {
							actualTotalTime += sf.expectedDuration().millisecondsValue();
						} catch (NullPointerException npe) {
							throw new ZedCustomTestException(npe.getMessage());
						}
					} else {
						// spine had a nonsmilfile
						super.addTestFailure("opf_spnItemRefIdrefSmil", "Spine itemref "
								+ mf.getId()
								+ " references non-SMIL file "
								+ mf.getName(), mf.getName(), null);
					}
				}
				if(p.getTotalTime()!=null) { //mg20061018: whilst testing a bookshare DTB
					if (Math.abs(actualTotalTime
							- p.getTotalTime().millisecondsValue()) > (SmilClock.getTolerance() * p.getSpine().size())) {
						actualTime = new SmilClock(actualTotalTime);
						super.addTestFailure("opf_xMetaTotTimeAccurate", "dtb:totalTime = "
								+ p.getTotalTime()
								+ " ; actual total time = "
								+ actualTime, null, null);
					}
				}
			}
		} catch (Exception e) {
			throw new ZedCustomTestException(e.getClass().getName()
					+ " when performing test opf_spnItemRefIdrefSmil|opf_xMetaTotTimeAccurate on file "
					+ f.getName()
					+ ": "
					+ e.getMessage());
		}

		// TEST: opf_mnfHrefUriUni item element href attribute value, when
		// 		resolved, is unique among item elements
		// TEST: opf_mnfHrefUriFrag item element href attribute URI does not
		// 		include a fragment identifier
		// TEST: opf_mnfHrefUriVal; this is done by rng datatype anyURI but
		// 		report it here too, since anyURI sucks

		// build a list of all URIs
		try {
			List<URI> list = new ArrayList<URI>();
			for (XmlFileElement packageFileElement : p.getXmlFileElements()) {
				if (packageFileElement.getLocalName().equals("item")) {
					try {
						URI relativeURI = new URI(packageFileElement.getAttributeValueL("href"));
						if (relativeURI.getFragment() != null) {
							super.addTestFailure("opf_mnfHrefUriFrag", "item element href "
									+ packageFileElement.getAttributeValueL("href")
									+ " contains a fragment identifier", null, null);
						}
						// add a resolved version to list
						list.add(URIUtils.resolve(p.toURI(), relativeURI));
					} catch (URISyntaxException e) {
						super.addTestFailure("opf_mnfHrefUriVal", "item element href "
								+ packageFileElement.getAttributeValueL("href")
								+ " is not a valid URI", null, null);
					}
				}// if(pfe.getLocalName().
			} // for

			// now check the uniqueness
			// note if non-unique files exist, they will be reported more than
			// once...
			// if its a dupe, twice; if its a trupe, thrice etc.
			// stupid code for stupid books.
			for (int j = 0; j < list.size(); j++) {
				URI uriToCheck = (URI) list.get(j);
				for (int k = 0; k < list.size(); k++) {
					URI uriToCompare = (URI) list.get(k);
					Integer ij = new Integer(j);
					Integer ik = new Integer(k);
					if ((ij.compareTo(ik) != 0)
							&& uriToCheck.equals(uriToCompare)) {
						super.addTestFailure("opf_mnfHrefUriUni", "URI "
								+ uriToCheck.toString()
								+ " is not unique in manifest", null, null);
					}
				}
			}
		} catch (Exception e) {
			throw new ZedCustomTestException(e.getClass().getName()
					+ " when performing test opf_mnfHrefUriVal|opf_mnfHrefUriUni on file "
					+ f.getName()
					+ ": "
					+ e.getMessage());
		}

		// TEST: opf_mnfDTBmembers: manifest lists all, and only those files
		// that are part of the DTB
		// check: a) that manifest doesnt list a file which isnt part of the DTB
		// check: b) that manifest doesnt miss listing a file which is part of
		// the DTB
		// do this by comparing the manifest map keyset to an
		// all-added-mondo-getFileRefs map

		// first build the collection that contains the actual dtb fileset,
		// regardless of what manifest claims it to be
		try {
			Map<String,File> actualDtbFileset = new HashMap<String, File>(); // <canonicalpath>,<file>
			for (ZedFile member : p.getManifest().values()) {
				// first add files that arent referenced by others (res,ncx)
				// or that could possibly live life on their own (=smil)
				if (member instanceof SmilFile
						|| member instanceof ResourceFile
						|| member instanceof XsltFile //mg20080616 when adding support for mathml
						|| member instanceof NcxFile) {
					try {
						actualDtbFileset.put(member.getCanonicalPath(), member);
					} catch (IOException ioe) {
						throw new ZedCustomTestException(ioe.getMessage());
					}
				}

				// ...then go ahead and collect the fileref maps of each member
				if (member instanceof Referring) {
					Referring referer = (Referring) member;
					if (!(referer instanceof PackageFile)) {
						try {
							actualDtbFileset.putAll(referer.getFileRefs());
						} catch (NullPointerException npe) {
							// this member returned null from getFileRefs()
							// although it is a referer
						}
					}
				} else {
					//this member cannot refer to other members so ignore it
				}
			}// for (i=pkg.getManifest().keySet().iterator(); i.hasNext();) {

			// now we have we have a hashmap which contains the actual dtb fileset
			// does manifest list each member of the actual dtb fileset?
			for (String key : actualDtbFileset.keySet()) {
				if (!p.getManifest().containsKey(key)) {
					super.addTestFailure("opf_mnfDTBmembers", "File "
							+ key
							+ " is part of DTB but not listed in manifest", null, null);
				}
			}
			// TODO: local DTDs and entities supported?

			// does manifest list a file which isnt a member of the actual dtb
			// fileset?
			for (String key :p.getManifest().keySet()) {
				if (!actualDtbFileset.containsKey(key)) {
					// make sure it isnt opf
					try {
						if (!key.equals(p.getCanonicalPath())) {
							super.addTestFailure("opf_mnfDTBmembers", "File "
									+ key
									+ " is listed in manifest but not part of DTB", null, null);
						}
					} catch (IOException ioe) {
						throw new ZedCustomTestException(ioe.getMessage());
					} catch (ZedCustomTestException zce) {
						throw new ZedCustomTestException(zce.getMessage());
					}

				}
			}
		} catch (Exception e) {
			throw new ZedCustomTestException(e.getClass().getName()
					+ " when performing test opf_mnfDTBmembers on file "
					+ f.getName()
					+ ": "
					+ e.getMessage());
		}

		// TEST: opf_mnfMimeMatch each manifest media-type mime declaration
		// match actual file types
		try {
			for (ManifestFile member : p.getManifest().values()) {
				if (member.exists()) {
					if (member instanceof XmlFile) {
						XmlFile xmem = (XmlFile) member;						
						if (member.getMimeType().equals(ZedConstants.MIMETYPE_OPF)) {
							if (!(xmem.getRootElementLocalName().equals("package") && member instanceof PackageFile)) {
								super.addTestFailure("opf_mnfMimeMatch", "mime-filetype mismatch on member with mimetype "
										+ member.getMimeType(), null, null);
							}
						} else if (member.getMimeType().equals(ZedConstants.MIMETYPE_SMIL)) {
							if (!(xmem.getRootElementLocalName().equals("smil") && member instanceof SmilFile)) {
								super.addTestFailure("opf_mnfMimeMatch", "mime-filetype mismatch on member with mimetype "
										+ member.getMimeType(), null, null);
							}
						} else if (member.getMimeType().equals(ZedConstants.MIMETYPE_DTBOOK_Z2005)) {
							if (!(xmem.getRootElementLocalName().equals("dtbook") && member instanceof TextFile)) {
								super.addTestFailure("opf_mnfMimeMatch", "mime-filetype mismatch on member with mimetype "
										+ member.getMimeType(), null, null);
							}
						} else if (member.getMimeType().equals(ZedConstants.MIMETYPE_NCX_Z2005)) {
							if (!(xmem.getRootElementLocalName().equals("ncx") && member instanceof NcxFile)) {
								super.addTestFailure("opf_mnfMimeMatch", "mime-filetype mismatch on member with mimetype "
										+ member.getMimeType(), null, null);
							}
						} else if (member.getMimeType().equals(ZedConstants.MIMETYPE_RESOURCE_Z2005)) {
							if (!(xmem.getRootElementLocalName().equals("resources") && member instanceof ResourceFile)) {
								super.addTestFailure("opf_mnfMimeMatch", "mime-filetype mismatch on member with mimetype "
										+ member.getMimeType(), null, null);
							}
						//mg20071209: adding support for XSLT, still undecided which mime type to use.	
						} else if (member.getMimeType().equals(ZedConstants.MIMETYPE_XSLT_1)|| member.getMimeType().equals(ZedConstants.MIMETYPE_XSLT_2)) {
							if (!(xmem.getRootElementLocalName().equals("stylesheet") && member instanceof XsltFile)) {
								super.addTestFailure("opf_mnfMimeMatch", "mime-filetype mismatch on member with mimetype "
										+ member.getMimeType(), null, null);
							}	
						} else {
							// unknown mimetype, which cant happen in z2005
							super.addTestFailure("opf_mnfMimeMatch", "unknown mime type on XML member with mimetype "
									+ member.getMimeType(), null, null);
						}
					} else {
						if (member.getMimeType().equals(ZedConstants.MIMETYPE_AUDIO_MP3)) {
							if (!(member instanceof Mp3File)) {
								super.addTestFailure("opf_mnfMimeMatch", "mime-filetype mismatch on member with mimetype "
										+ member.getMimeType(), null, null);
							}
						} else if (member.getMimeType().equals(ZedConstants.MIMETYPE_AUDIO_MP4AAC_Z2005)) {
							if (!(member instanceof AacFile)) {
								super.addTestFailure("opf_mnfMimeMatch", "mime-filetype mismatch on member with mimetype "
										+ member.getMimeType(), null, null);
							}
						} else if (member.getMimeType().equals(ZedConstants.MIMETYPE_AUDIO_PCMWAV)) {
							if (!(member instanceof WavFile)) {
								super.addTestFailure("opf_mnfMimeMatch", "mime-filetype mismatch on member with mimetype "
										+ member.getMimeType(), null, null);
							}
						} else if (member.getMimeType().equals(ZedConstants.MIMETYPE_CSS)) {
							if (!(member instanceof CssFile)) {
								super.addTestFailure("opf_mnfMimeMatch", "mime-filetype mismatch on member with mimetype "
										+ member.getMimeType(), null, null);
							}
						} else if (member.getMimeType().equals(ZedConstants.MIMETYPE_IMAGE_JPEG)) {
							if (!(member instanceof JpegFile)) {
								super.addTestFailure("opf_mnfMimeMatch", "mime-filetype mismatch on member with mimetype "
										+ member.getMimeType(), null, null);
							}
						} else if (member.getMimeType().equals(ZedConstants.MIMETYPE_IMAGE_PNG)) {
							if (!(member instanceof PngFile)) {
								super.addTestFailure("opf_mnfMimeMatch", "mime-filetype mismatch on member with mimetype "
										+ member.getMimeType(), null, null);
							}
						} else if (member.getMimeType().equals(ZedConstants.MIMETYPE_DTD_Z2005)) {
							if (!(member instanceof DtdFile)) {
								super.addTestFailure("opf_mnfMimeMatch", "mime-filetype mismatch on member with mimetype "
										+ member.getMimeType(), null, null);
							}
						} else if (member.getMimeType().equals(ZedConstants.MIMETYPE_IMAGE_SVG)) {
							Peeker peeker = PeekerPool.getInstance().acquire();
							
							try {
								PeekResult result = peeker.peek(member.toURI());
								String peekedRootElemLocalName = result.getRootElementLocalName();
								if (!(peekedRootElemLocalName.equals("svg") && member instanceof SvgFile)) {
									super.addTestFailure("opf_mnfMimeMatch", "mime-filetype mismatch on member with mimetype "
											+ member.getMimeType(), null, null);
								}
							} catch (Exception e) {
								throw new ZedCustomTestException(e.getMessage());
							}
						} else {
							// unknown mimetype, which cant happen in z2005
							super.addTestFailure("opf_mnfMimeMatch", "unknown mime type on member with mimetype "
									+ member.getMimeType(), null, null);
						}
					}
				}//member.exists()
			}
		} catch (Exception e) {
			throw new ZedCustomTestException(e.getClass().getName()
					+ " when performing test opf_mnfMimeMatch on file "
					+ f.getName()
					+ ": "
					+ e.getMessage());
		}

		// TEST opf_mnfNcxCount: Exactly one file of type NCX is listed in
		// manifest
		// TEST opf_mnfOneSmil: manifest references at least one SMIL file
		// TEST opf_opfInManifest: exactly one package file is listed in
		// manifest
		// TEST opf_mnfResCount: Zero or one file of type Resource is listed in
		// manifest
		// TEST opf_mnfResCountWhenSkippable: when the DTB contains skippable or
		// escapable structures, exactly one resource file is listed in manifest
		// TEST opf_xMetaMmContentValueManifestText: when dtb:multimediaContent
		// includes the top-level media type "text", there is at least one
		// dtbook file in manifest
		// TEST opf_xMetaMmContentValueManifestImage: when dtb:multimediaContent
		// includes the top-level media type "image", there is at least one
		// image file in manifest
		// TEST opf_xMetaMmContentValueManifestAudio:when dtb:multimediaContent
		// includes the top-level media type "audio", there is at least one
		// audio file in manifest
		// TEST opf_mnfNcxId
		// TEST opf_mnfResId

		long ncxCount = 0;
		long resCount = 0;
		long smilCount = 0;
		long opfCount = 0;
		long textCount = 0;
		long imageCount = 0;
		long audioCount = 0;
		boolean smilHasSkippableOrEscapableStructures = false;

		try {
			for (ZedFile member : p.getManifest().values()) {
				if (member instanceof NcxFile) {
					ncxCount++;
					ManifestFile ncx = (ManifestFile) member;
					if (ncx.getId()==null||!ncx.getId().equals("ncx")) {
						super.addTestFailure("opf_mnfNcxId", "given id is "
								+ ncx.getId(), null, null);
					}
				} else if (member instanceof ResourceFile) {
					resCount++;
					ManifestFile res = (ManifestFile) member;
					if (!res.getId().equals("resource")) {
						super.addTestFailure("opf_mnfResId", "given id is "
								+ res.getId(), null, null);
					}

				} else if (member instanceof SmilFile) {
					smilCount++;
					SmilFile smil = (SmilFile) member;
					if (smil.hasSkippableStructures()
							|| smil.hasEscapableStructures()) {
						smilHasSkippableOrEscapableStructures = true;
					}
				} else if (member instanceof PackageFile) {
					opfCount++;
				} else if (member instanceof AudioFile) {
					audioCount++;
				} else if (member instanceof ImageFile) {
					imageCount++;
				} else if (member instanceof TextFile) {
					textCount++;
				}

			}// for (i = pkg.getManifest().keySet().iterator(); i.hasNext();) {

			if (ncxCount != 1) {
				super.addTestFailure("opf_mnfNcxCount", "", null, null);
			}

			if (smilCount < 1) {
				super.addTestFailure("opf_mnfOneSmil", "", null, null);
			} else {

			}

			if (opfCount != 1) {
				super.addTestFailure("opf_opfInManifest", "", null, null);
			}
		
			if (resCount > 1) {
				super.addTestFailure("opf_mnfResCount", "", null, null);
			}
					
			if (smilCount > 0) {
				if (smilHasSkippableOrEscapableStructures) {
					if (resCount != 1) {
						super.addTestFailure("opf_mnfResCountWhenSkippable", "", null, null);
					}
				}	
			}

			String mmc = p.getMetaDtbMultimediaContent();

			if (mmc.indexOf("audio") >= 0
					&& audioCount < 1) {
				super.addTestFailure("opf_xMetaMmContentValueManifestAudio", "", null, null);
			}

			if (mmc.indexOf("text") >= 0
					&& textCount < 1) {
				super.addTestFailure("opf_xMetaMmContentValueManifestText", "", null, null);
			}

			if (mmc.indexOf("image") >= 0
					&& imageCount < 1) {
				super.addTestFailure("opf_xMetaMmContentValueManifestImage", "", null, null);
			}
		} catch (Exception e) {
			throw new ZedCustomTestException(e.getClass().getName()
					+ " when performing test opf_mnfNcxCount|opf_mnfOneSmil|opf_opfInManifest|opf_mnfResCount|opf_mnfResCountWhenSkippable|opf_xMetaMmContentValueManifestText|opf_xMetaMmContentValueManifestText|opf_xMetaMmContentValueManifestImage|opf_xMetaMmContentValueManifestAudio|opf_mnfNcxId|opf_mnfResId on file "
					+ f.getName()
					+ ": "
					+ e.getMessage());
		}

		// TEST opf_SpineRefAllSmilInMnf: each smil file listed in manifest is
		// referenced by spine
		// Iterate through the SMIL file collection and test to see if it is
		// also in the spine
		try {
			if (p.getSmilFiles() != null) {
				for (SmilFile sf : p.getSmilFiles().values()) {
					if (p.getSpine() != null
							&& p.getSpine().containsKey(sf.getCanonicalPath()) == false) {
						super.addTestFailure("opf_SpineRefAllSmilInMnf", "SMIL file "
								+ sf.getName()
								+ " not listed in spine", sf.getName(), null);
					}
				}
			}
		} catch (Exception e) {
			throw new ZedCustomTestException(e.getClass().getName()
					+ " when performing test opf_SpineRefAllSmilInMnf on file "
					+ f.getName()
					+ ": "
					+ e.getMessage());
		}

		// TEST: opf_mnfZeroAudio Manifest does not reference audio files
		// this doesnt apply to 'all' so check getDtbMultimediaTypeAsDeclared()
		try {
			if (p.getDtbMultimediaTypeAsDeclared().equals("textNCX")) {
				if (p.getAudioFiles() != null) {
					if (p.getAudioFiles().size() == 1) {
						super.addTestFailure("opf_mnfZeroAudio", "1 audio file is listed in manifest; check your meta dtb:multimediaType value", null, null);
					} else {
						super.addTestFailure("opf_mnfZeroAudio", p.getAudioFiles().size()
								+ " audio files are listed in manifest; check your meta dtb:multimediaType value", null, null);
					}
				}
			}
		} catch (Exception e) {
			throw new ZedCustomTestException(e.getClass().getName()
					+ " when performing test opf_mnfZeroAudio on file "
					+ f.getName()
					+ ": "
					+ e.getMessage());
		}

		//TEST opf_mnfOneAudio manifest references at least one audio file
		// this doesnt apply to 'all' so check getDtbMultimediaTypeAsDeclared()
		try {
			if (!p.getDtbMultimediaTypeAsDeclared().equals("textNCX")) {
				if (p.getAudioFiles() == null) {
					super.addTestFailure("opf_mnfOneAudio", "no audio files are listed in manifest; check your meta dtb:multimediaType value", null, null);
				}
			}
		} catch (Exception e) {
			throw new ZedCustomTestException(e.getClass().getName()
					+ " when performing test opf_mnfOneAudio on file "
					+ f.getName()
					+ ": "
					+ e.getMessage());
		}

		// TEST: opf_mnfZeroDtbook Manifest does not reference dtbook files
		// this doesnt apply to 'all' so check getDtbMultimediaTypeAsDeclared()
		try {
			if (p.getDtbMultimediaTypeAsDeclared().matches("audioOnly|audioNCX")) {
				if (p.getTextFiles() != null) {
					if (p.getTextFiles().size() == 1) {
						super.addTestFailure("opf_mnfZeroDtbook", "1 text content file is listed in manifest; check your meta dtb:multimediaType value", null, null);
					} else {
						super.addTestFailure("opf_mnfZeroDtbook", p.getTextFiles().size()
								+ " text content files are listed in manifest; check your meta dtb:multimediaType value", null, null);
					}
				}
			}
		} catch (Exception e) {
			throw new ZedCustomTestException(e.getClass().getName()
					+ " when performing testopf_mnfZeroDtbook on file "
					+ f.getName()
					+ ": "
					+ e.getMessage());
		}

		// TEST: opf_mnfOneDtbook Manifest references at least one dtbook file
		// this doesnt apply to 'all' so check getDtbMultimediaTypeAsDeclared()        
		try {
			if (!p.getDtbMultimediaTypeAsDeclared().matches("audioOnly|audioNCX")) {
				if (p.getTextFiles() == null) {
					super.addTestFailure("opf_mnfOneDtbook", "Could not detect any text content files in manifest; check your meta dtb:multimediaType value", null, null);
				}
			}
		} catch (Exception e) {
			throw new ZedCustomTestException(e.getClass().getName()
					+ " when performing opf_mnfOneDtbook on file "
					+ f.getName()
					+ ": "
					+ e.getMessage());
		}

		
		//TEST: opf_mnfHrefUriVal
        try {        	
        	for (XmlFileElement resourceElement : p.getXmlFileElements()) {
        		String uriValue = resourceElement.getAttributeValueL("href");
        		if(uriValue!=null){
	        		try{
	        			new URI(uriValue);
	        		} catch (Exception e) {
	        			super.addTestFailure("opf_mnfHrefUriVal", "Package file "+ p.getName()+ "contains a URI value "+ uriValue + " which is invalid: " + e.getMessage(),null,null);
	        		}	        			
        		}
        	}	
        }catch (Exception e) {
            throw new ZedCustomTestException(
                    e.getClass().getName()
                            + " when performing test opf_mnfHrefUriVal on file "
                            + f.getName() + ": " + e.getMessage());
		}
        
        //TEST: math_opfMathExtensionVersionMeta
        // When the package file includes the MathML-in-DAISY extension-version metadata entry, 
        // DTBook must include at least one MathML island
        try {        	
        	boolean hasMathExtensionVersionMeta = false;
        	//look for the math meta
        	for(Object o : p.getXmlFileElements()) {
				XmlFileElement xfe = (XmlFileElement) o;
				if (xfe.getLocalName().equals("meta")) {						
					if(xfe.getAttributeValueL("name") != null 
							&& xfe.getAttributeValueL("scheme") != null
								&& xfe.getAttributeValueL("scheme").equals(Namespaces.MATHML_NS_URI)
									&& xfe.getAttributeValueL("name").equals("z39-86-extension-version")) {						
						hasMathExtensionVersionMeta = true;	break;							
					}
				}else if(xfe.getLocalName().equals("manifest")) {
					break;
				}
        	}
        	
        	if(hasMathExtensionVersionMeta) {
        		//check that there is math in at least one dtbook fileset member
        		boolean dtbookHasMath = false;
        		Collection<TextFile> textFiles = p.getTextFiles().values();
        		for(TextFile tf : textFiles) {
        			for(Object o : tf.getXmlFileElements()) {
        				XmlFileElement xfe = (XmlFileElement) o;
        				if(xfe.getNsURI()!=null && xfe.getNsURI().equals(Namespaces.MATHML_NS_URI)) {
        					dtbookHasMath = true;
        					break;
        				}
        			}
        		}
        		
        		if(!dtbookHasMath) {
        			super.addTestFailure("math_opfMathExtensionVersionMeta", null,null,null);
        		}
        	}
        	
        }catch (Exception e) {
            throw new ZedCustomTestException(
                    e.getClass().getName()
                            + " when performing test math_opfMathExtensionVersionMeta on file "
                            + f.getName() + ": " + e.getMessage());
		}
        
        
        //TEST: math_xsltMetadataValue
        //TEST: math_xsltInManifest
        // The MathML XSLT meta element content attribute resolves to an XSLT document
        try {        	
        	
        	//look for the xslt meta
        	for(Object o : p.getXmlFileElements()) {
				XmlFileElement xfe = (XmlFileElement) o;
				if (xfe.getLocalName().equals("meta")) {						
					if(xfe.getAttributeValueL("name") != null 
							&& xfe.getAttributeValueL("scheme") != null
								&& xfe.getAttributeValueL("scheme").equals(Namespaces.MATHML_NS_URI)
									&& xfe.getAttributeValueL("name").equals("DTBook-XSLTFallback")) {	
						
						String contentValue = xfe.getAttributeValueL("content");
						
						if(contentValue==null||contentValue.trim().length()<1) {
							super.addTestFailure("math_xsltMetadataValue", "The value is empty",contentValue,null);
						}else{
							//absolutize and resolve
							URI xslt = URIUtils.resolve(p.toURI(), contentValue);
							File fxsl = new File(xslt);
							if(fxsl==null || !fxsl.exists()) {
								super.addTestFailure("math_xsltMetadataValue", "The referenced file does not exist",contentValue,null);	
							}else{
								//... and make sure its listed in manifest
								Object m = p.getManifest().get(fxsl.getCanonicalPath());
								if(m==null) {
									super.addTestFailure("math_xsltInManifest", "",contentValue,null);
								}
							}
							
							
						}
					}
				}else if(xfe.getLocalName().equals("manifest")) {
					break;
				}
        	}        	
        }catch (Exception e) {
            throw new ZedCustomTestException(
                    e.getClass().getName()
                            + " when performing test math_xsltMetadataValue on file "
                            + f.getName() + ": " + e.getMessage());
		}
        
	}
}
