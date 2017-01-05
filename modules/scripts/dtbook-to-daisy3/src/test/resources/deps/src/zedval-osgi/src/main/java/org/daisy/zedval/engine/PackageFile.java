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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.daisy.util.text.URIUtils;
import org.daisy.util.xml.SmilClock;
import org.daisy.util.xml.peek.PeekResult;
import org.daisy.util.xml.peek.Peeker;
import org.daisy.util.xml.peek.PeekerPool;
import org.daisy.util.xml.pool.PoolException;
import org.xml.sax.Attributes;

/**
 * A <code>PackageFile</code> object represents a DTB package file
 * 
 * @author James Pritchett
 * @author Piotr Kiernicki
 * @author mgylling
 */

public class PackageFile extends XmlFile {

	/**
	 * @param fullPath
	 *            Full file path (including name)
	 */
	public PackageFile(String fullPath) {
		super(fullPath, "", "", "");
	}

	/**
	 * @param id
	 *            id from package file manifest
	 * @param fullPath
	 *            File path
	 * @param mimeType
	 *            File MIME type as given in package manifest
	 */
	public PackageFile(String fullPath,
			String id, String mimeType) {
		super(fullPath, id, mimeType, "");
	}

	/**
	 * Returns the book title
	 * 
	 * @return the book title
	 */
	public String getTitle() {
		return this.title.toString().trim();
	}

	/**
	 * Returns array of book creator names
	 * 
	 * @return ArrayList of Strings
	 */
	public List<String> getCreators() {
		return this.creators;
	}

	/**
	 * Returns version of Zed specification this book follows
	 * 
	 * @return Value of dc:Format metadata item
	 */
	public String getSpecVersion() {
		return this.specVersion.toString().trim();
	}

	/**
	 * Returns ZedFiles named in package manifest
	 * 
	 * @return LinkedHashMap of ZedFiles named in package manifest (key =
	 *         absolute file path)
	 */
	public Map<String,ManifestFile> getManifest() {
		return this.manifest;
	}

	/**
	 * Returns the NCX file for this package
	 * 
	 * @return The NcxFile for this package
	 */
	public NcxFile getNcx() {
		return this.ncx;
	}

	/**
	 * Returns the resource file for this package
	 * 
	 * @return The ResourceFile for this package
	 */
	public ResourceFile getResource() {
		return this.resource;
	}

	/**
	 * Returns the text files for this package
	 * 
	 * @return LinkedHashMap of the TextFile objects (key = absolute file path)
	 */
	public Map<String,TextFile> getTextFiles() {
		return this.textFiles;
	}

	/**
	 * Returns the XSL files for this package
	 * 
	 * @return LinkedHashMap of the XsltFile objects (key = absolute file path)
	 */
	public Map<String,XsltFile> getXsltFiles() {
		return this.xsltFiles;
	}
	
	/**
	 * Returns the audio files for this package
	 * 
	 * @return LinkedHashMap of the AudioFile objects (key = absolute file path)
	 */
	public Map<String,AudioFile> getAudioFiles() {
		return this.audioFiles;
	}

	/**
	 * Returns the AAC audio files for this package
	 * 
	 * @return LinkedHashMap of the AacFile objects (key = absolute file path)
	 */
	public Map<String, AacFile> getAacFiles() {
		return this.aacFiles;
	}

	/**
	 * Returns the MP3 audio files for this package
	 * 
	 * @return LinkedHashMap of the Mp3File objects (key = absolute file path)
	 */
	public Map<String, Mp3File> getMp3Files() {
		return this.mp3Files;
	}

	/**
	 * Returns the WAV audio files for this package
	 * 
	 * @return LinkedHashMap of the WavFile objects (key = absolute file path)
	 */
	public Map<String, WavFile> getWavFiles() {
		return this.wavFiles;
	}

	/**
	 * Returns the image files for this package
	 * 
	 * @return LinkedHashMap of the ImageFile objects (key = absolute file path)
	 */
	public Map<String, ImageFile> getImageFiles() {
		return this.imageFiles;
	}

	/**
	 * Returns the JPEG image files for this package
	 * 
	 * @return LinkedHashMap of the JpegFile objects (key = absolute file path)
	 */
	public Map<String, JpegFile> getJpegFiles() {
		return this.jpegFiles;
	}

	/**
	 * Returns the PNG image files for this package
	 * 
	 * @return LinkedHashMap of the PngFile objects (key = absolute file path)
	 */
	public Map<String, PngFile> getPngFiles() {
		return this.pngFiles;
	}

	/**
	 * Returns the SVG image files for this package
	 * 
	 * @return LinkedHashMap of the SvgFile objects (key = absolute file path)
	 */
	public Map<String,SvgFile> getSvgFiles() {
		return this.svgFiles;
	}

	/**
	 * Returns the SMIL files for this package
	 * 
	 * @return LinkedHashMap of the SmilFile objects (key = absolute file path)
	 */
	public Map<String,SmilFile> getSmilFiles() {
		return this.smilFiles;
	}

	/**
	 * Returns the CSS files for this package
	 * 
	 * @return LinkedHashMap of the CssFile objects (key = absolute file path)
	 */
	public Map<String,CssFile> getCssFiles() {
		return this.cssFiles;
	}

	/**
	 * Returns the DTD files for this package (DTDs, Ents and Mods)
	 * 
	 * @return LinkedHashMap of the CssFile objects (key = absolute file path)
	 */
	public Map<String,DtdFile> getDtdFiles() {
		return this.dtdFiles;
	}

	/**
	 * Returns list of ZedFiles named in package spine
	 * 
	 * @return LinkedHashMap of ZedFiles
	 */
	public Map<String,SmilFile> getSpine() {
		return this.spine;
	}

	/**
	 * Returns the unique identifier for this package
	 * 
	 * @return the unique identifier for this package
	 */
	public String getIdentifier() {
		return this.identifier.toString().trim();
	}

	/**
	 * Returns the declared DTB multimedia type (value of dtb:multimediaType
	 * metadata item)
	 * 
	 * @return the declared DTB multimedia type (value of dtb:multimediaType
	 *         metadata item)
	 */
	public String getDtbMultimediaTypeAsDeclared() {
		//orig values are: ENUM ('audioOnly' | 'audioNCX' | 'audioPartText' | 'audioFullText' | 'textPartAudio' | 'textNCX')
		return this.metaDtbMultimediaType;
	}

	/**
	 * Returns the computed DTB multimedia type (computed by checking fileset member types)
	 * Will only return the significant trio subset (audioOnly|audioFullText|textNCX) as the
	 * others are programatically undeterminable
	 * @return the computed DTB multimedia type (computed by checking fileset member types)
	 */
	public String getDtbMultimediaTypeAsComputed() {
		//orig values are: ENUM ('audioOnly' | 'audioNCX' | 'audioPartText' | 'audioFullText' | 'textPartAudio' | 'textNCX')
		//only returns text or audio presence, all others are basically programmatically indeterminable
		if (this.textFiles == null) {
			return "audioNCX";
		} else if (this.audioFiles == null) {
			return "textNCX";
		} else if (this.audioFiles != null
				&& this.textFiles != null) {
			return "audioFullText";
		}
		//ah well
		return null;
	}

	/**
	 * Returns the declared DTB multimedia content (value of
	 * dtb:multimediaContent metadata item)
	 * 
	 * @return the declared DTB multimedia content (value of
	 *         dtb:multimediaContent metadata item)
	 */
	public String getMetaDtbMultimediaContent() {
		return this.metaDtbMultimediaContent;
	}

	/**
	 * Returns the declared total playback time of the book (value of
	 * dtb:totalTime metadata item)
	 * 
	 * @return SmilClock object expressing total declared playback time
	 */
	public SmilClock getTotalTime() {
		return this.metaDtbTotalTime;
	}

	/**
	 * Returns the list of audio formats for this package
	 * 
	 * @return The content of the dtb:audioFormat metadata item (space-delimited
	 *         list of formats)
	 */
	public String audioFormat() {
		return this.metaDtbAudioFormat;
	}

	private <I extends ManifestFile> Map<String,I> addToMap(
			Map<String,I> anyMap, I f)
			throws IOException {
		if (null == anyMap) {
			Map<String,I> newMap = new LinkedHashMap<String,I>();
			newMap.put(f.getCanonicalPath(), f);
			return newMap;
		}
		anyMap.put(f.getCanonicalPath(), f);
		return anyMap;

	}

	// SAX ContentHandler implementations:

	/**
	 * Grabs all incoming elements and looks for properties to set
	 * 
	 * @param namespaceURI
	 *            The namespace URI of the element
	 * @param localName
	 *            The non-prefixed name of the element
	 * @param qName
	 *            The qualified name of the element
	 * @param atts
	 *            List of all element attributes
	 */
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) {
		String metaName;
		String itemMimeType;
		String itemId;
		String peekedRootElemLocalName = "";
		URI hrefUri;
		ManifestFile f;

        //do generic collects
    	super.startElement(namespaceURI, localName, qName, atts);

        //add to the element collector    	    	
        super.addXmlFileElement(namespaceURI,localName,qName,atts);
    	
		// Handle all the different elements that we might care about here
		// <meta> - get the content for dtbType and totalTime (ignore others)
		if (localName=="meta") {
			metaName = atts.getValue("name").intern();
			if (metaName=="dtb:multimediaType") {
				this.metaDtbMultimediaType = atts.getValue("content");
			} else if (metaName=="dtb:multimediaContent"){
				this.metaDtbMultimediaContent = atts.getValue("content");
			} else if (metaName=="dtb:totalTime") {
				try {
					this.metaDtbTotalTime = new SmilClock(atts.getValue("content"));
				} catch (NumberFormatException e) {
					this.metaDtbTotalTime = null;
				}
			} else if (metaName=="dtb:audioFormat") {
				this.metaDtbAudioFormat = atts.getValue("content");
			}
		}

		// <item> - Instantiate/initialize the appropriate kind of ZedFile and
		// add to manifest
		else if (localName=="item") {
			if (this.manifest == null)
				this.manifest = new LinkedHashMap<String,ManifestFile>();

			/*
			 * Instantiate the appropriate kind of ManifestFile and add to the
			 * appropriate type-specific array. We identify package file by
			 * name; NCX and resource file by id; all others by MIME type.
			 * 
			 * mg20060212: The above holds true for z2002, but for z2005
			 * remember that mimetypes were changed and improved, so the
			 * detection algo is sometimes different between specs.
			 * 
			 * Remember: the job of this function is to heuristically find out
			 * what a file really is even if mimetypes, extensions, ids etc
			 * happen to be wrong in manifest, so we need to use whatever means
			 * possible. For this reason, added Peeker, that collects instance
			 * prolog/root properties. In this verion of PackageFile, peeking
			 * takes precedence over any ids, mimes etc in manifest. After that,
			 * mime+extension+ids are treated as equal weight. This approach used
			 * for both z2002 and z2005
			 */
			itemId = atts.getValue("id");
			itemMimeType = atts.getValue("media-type");

			try {
				hrefUri = URIUtils.resolve(this.toURI(), new URI(atts.getValue("href")));
			} catch (URISyntaxException e) {
				super.getZedFileInitEx().addError("ERROR:  Bad URI for manifest item href "
						+ atts.getValue("href")
						+ " (file will not be tested)");
				return;
			}

			// get the root elem local name and other stuff through the sax
			// peeker
			Peeker peeker = null;
			try {
				peeker = PeekerPool.getInstance().acquire();
				PeekResult result = peeker.peek(hrefUri);
				peekedRootElemLocalName = result.getRootElementLocalName().toLowerCase().intern();
			} catch (Exception e) {
				// this was not an xml file or something else went wrong
			}finally{
				try {
					PeekerPool.getInstance().release(peeker);
				} catch (PoolException e) {

				}
			}

			try {
				// Rootcheck PackageFile
				if (peekedRootElemLocalName.equals("package")) {
					this.setID(itemId);
					this.setMimeType(itemMimeType);
					this.setManifestURI(atts.getValue("href"));
					f = this;
				}

				// Rootcheck NcxFile
				else if (peekedRootElemLocalName.equals("ncx")) {
					this.ncx = new NcxFile(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
					f = this.ncx;
				}

				// Rootcheck ResourceFile
				else if (peekedRootElemLocalName.equals("resources")) {
					this.resource = new ResourceFile(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
					f = this.resource;
				}

				// Rootcheck SmilFile
				else if (peekedRootElemLocalName.equals("smil")) {
					f = new SmilFile(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
					this.smilFiles = addToMap(this.smilFiles, (SmilFile) f);
				}

				// Rootcheck TextFile
				else if (peekedRootElemLocalName.equals("dtbook")) {
					f = new TextFile(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
					this.textFiles = addToMap(this.textFiles, (TextFile) f);
				}
				
				// Rootcheck XsltFile
				else if (peekedRootElemLocalName.equals("stylesheet")) {
						f = new XsltFile(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
						this.xsltFiles = addToMap(this.xsltFiles, (XsltFile) f);
						
				// it was not an xmlfile where we could match via peeker.getRootElemLocalName	
				} else { 				 
					// AacFile
					// else if (itemMimeType.equals(ManifestFile.MIME_AAC)) {
					// //= 2002 version
					if (itemMimeType.equals(ZedConstants.MIMETYPE_AUDIO_MP4AAC_Z2002)
							|| itemMimeType.equals(ZedConstants.MIMETYPE_AUDIO_MP4AAC_Z2005)
							|| hrefUri.getPath().matches(".*\\.[mM][pP]4$")) {
						f = new AacFile(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
						this.audioFiles = addToMap(this.audioFiles, (AudioFile) f);
						this.aacFiles = addToMap(this.aacFiles, (AacFile) f);
					}

					// Mp3File
					// else if (itemMimeType.equals(ManifestFile.MIME_MP3)) {
					// //= 2002 version
					else if (itemMimeType.equals(ZedConstants.MIMETYPE_AUDIO_MP3)
							|| hrefUri.getPath().matches(".*\\.[mM][pP]3$")) {
						f = new Mp3File(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
						this.audioFiles = addToMap(this.audioFiles, (AudioFile) f);
						this.mp3Files = addToMap(this.mp3Files, (Mp3File) f);
					}

					// WavFile
					// else if (itemMimeType.equals(ManifestFile.MIME_WAV) //=
					// 2002 version
					else if (itemMimeType.equals(ZedConstants.MIMETYPE_AUDIO_PCMWAV)
							|| hrefUri.getPath().matches(".*\\.[wW][aA][vV]$")) {
						f = new WavFile(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
						this.audioFiles = addToMap(this.audioFiles, (AudioFile) f);
						this.wavFiles = addToMap(this.wavFiles, (WavFile) f);
					}
					// JpegFile
					else if (itemMimeType.equals(ZedConstants.MIMETYPE_IMAGE_JPEG)
							|| hrefUri.getPath().matches(".*\\.[jJ][pP][gG]$")
							|| hrefUri.getPath().matches(".*\\.[jJ][pP][eE][gG]$")) {
						f = new JpegFile(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
						this.imageFiles = addToMap(this.imageFiles, (ImageFile) f);
						this.jpegFiles = addToMap(this.jpegFiles, (JpegFile) f);
					}
					// PngFile
					else if (itemMimeType.equals(ZedConstants.MIMETYPE_IMAGE_PNG)
							|| hrefUri.getPath().matches(".*\\.[pP][nN][gG]$")) {
						f = new PngFile(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
						this.imageFiles = addToMap(this.imageFiles, (ImageFile) f);
						this.pngFiles = addToMap(this.pngFiles, (PngFile) f);
					}
					// SvgFile
					else if (peekedRootElemLocalName.equals("svg")
							|| itemMimeType.equals(ZedConstants.MIMETYPE_IMAGE_SVG)
							|| hrefUri.getPath().matches(".*\\.[sS][vV][gG]$")) {
						f = new SvgFile(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
						this.imageFiles = addToMap(this.imageFiles, (ImageFile) f);
						this.svgFiles = addToMap(this.svgFiles, (SvgFile) f);
					}
					// CssFile
					else if (itemMimeType.equals(ZedConstants.MIMETYPE_CSS)
							|| hrefUri.getPath().matches(".*\\.[cC][sS][sS]$")) {
						f = new CssFile(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
						this.cssFiles = addToMap(this.cssFiles, (CssFile) f);
					}

					// Othercheck package
					else if (hrefUri.getPath().endsWith(this.getName())) {
						this.setID(itemId);
						this.setMimeType(itemMimeType);
						f = this;
					}
					// Othercheck ncx
					else if (itemMimeType.equals(ZedConstants.MIMETYPE_NCX_Z2005)
							|| itemId.equals("ncx")
							|| hrefUri.getPath().matches(".*\\.[Nn][Cc][Xx]$")) {
						this.ncx = new NcxFile(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
						f = this.ncx;
					}
					// Othercheck ResourceFile
					else if (itemMimeType.equals(ZedConstants.MIMETYPE_RESOURCE_Z2005)
							|| itemId.equals("resource")
							|| hrefUri.getPath().matches(".*\\.[rR][eE][sS]$")) {
						this.resource = new ResourceFile(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
						f = this.resource;
					}

					// Othercheck SmilFile
					else if (itemMimeType.equals(ZedConstants.MIMETYPE_SMIL)
							|| hrefUri.getPath().matches(".*\\.[sS][mM][iI][lL]$")) {
						f = new SmilFile(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
						this.smilFiles = addToMap(this.smilFiles, (SmilFile) f);
					}
					// Othercheck TextFile
					else if (itemMimeType.equals(ZedConstants.MIMETYPE_DTBOOK_Z2005)
							|| (itemMimeType.equals(ZedConstants.MIMETYPE_DTBOOK_Z2002) && hrefUri.getPath().matches(".*\\.xml$"))) {
						f = new TextFile(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
						this.textFiles = addToMap(this.textFiles, (TextFile) f);
					}
					// Othercheck DTDFile
					else if (itemMimeType.equals(ZedConstants.MIMETYPE_DTD_Z2005)
							|| (itemMimeType.equals(ZedConstants.MIMETYPE_DTD_Z2002) && hrefUri.getPath().matches(".*\\.[Dd][Tt][Dd]$|.*\\.[Ee][Nn][Tt]$|.*\\.[Mm][Oo][Dd]$"))) {
						f = new DtdFile(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
						this.dtdFiles = addToMap(this.dtdFiles, (DtdFile) f);
					} else {
						// Check out the MIME types for audio & image files that
						// we just don't recognize
						if (itemMimeType.matches("audio/.*")) {
							f = new AudioFile(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
							this.audioFiles = addToMap(this.audioFiles, (AudioFile) f);
						} else if (itemMimeType.matches("image/.*")) {
							f = new ImageFile(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
							this.imageFiles = addToMap(this.imageFiles, (ImageFile) f);
						}
						// Anything else is just "other" (not known to spec)
						else {
							f = new OtherFile(hrefUri.getPath(), itemId, itemMimeType, atts.getValue("href"));
							//                            super.getZedFileInitEx().addError(
							//                                    "Warning: unrecognized filetype encountered: "
							//                                            + hrefUri.getPath());
						}
					}
				}// it was not an xmlfile where we could match via
				// peeker.getRootElemLocalName

			} catch (IOException e) {
				super.getZedFileInitEx().addError("IOException while accessing file "
						+ hrefUri.getPath());
				return;
			}

			/*
			 * Now initialize the file and add it to the manifest
			 */
			f.setPackage(this);
			try {
				this.manifest.put(f.getCanonicalPath(), f);
			} catch (IOException e) {
				super.getZedFileInitEx().addError("IOException while accessing file");
				return;
			}

			if (f != this) {
				try {
					f.initialize();
				} catch (ZedFileInitializationException initEx) {
					super.getZedFileInitEx().getErrors().addAll(initEx.getErrors());
				}
			}
		}

		// <package> - Save the unique-identifier attribute value for use later
		else if (localName=="package") {
			this.uniqueIdRef = atts.getValue("unique-identifier");
		}

		// <dc:Identifier> - Save the id attribute for use later
		else if (localName=="Identifier") {
			this.curId = atts.getValue("id");
		}

		// <itemRef> - Find the referenced file in the manifest and add it to
		// the spine, too
		else if (localName=="itemref"
				&& this.smilFiles != null) {
			if (this.spine == null)
				this.spine = new LinkedHashMap<String,SmilFile>();

			// Since we can't be sure that all items referenced by the spine
			// are, in fact,
			// SMIL files, we have to search the whole manifest for them.
			try {
				for (ManifestFile file : this.manifest.values()) {
					//mg20080224 added null test, note this can cause other probs
					if (file.getId()!=null&&file.getId().equals(atts.getValue("idref"))) {
						this.spine.put(file.getCanonicalPath(), (SmilFile) file);
						break;
					}
				}
			} catch (IOException e) {
				super.getZedFileInitEx().addError("IOException while accessing file");
				return;

			}
		}

		this.curElement = localName; // Always do this

//		// add the element to the elements arraylist
//		// if it has an element we want to do a customTest on
//		// note: extend the if clause below to
//		// add other elements        
//		if (atts.getValue("href") != null) {
//			try {
//				elements.add(new PackageFileElement(atts, localName, namespaceURI));
//			} catch (Exception e) {
//				super.getZedFileInitEx().addError("Exception while building package elementlist; id value: "
//						+ atts.getValue("id")
//						+ e.getMessage());
//				return;
//			}
//		}

	}

	/**
	 * Grabs all text of current element and sets properties as needed. <br/>If
	 * current element's text node contains entities, characters() will be
	 * called more than once for the element.
	 * 
	 * @param ch
	 *            Array of characters in document
	 * @param start
	 *            Starting index of text in this element
	 * @param length
	 *            Length of text in this element
	 */
	public void characters(char[] ch, int start,
			int length) {
		String s = new String(ch, start, length);

		/*
		 * PK 2005-04-06 bug #787453 Setting the parser feature
		 * http://xml.org/sax/features/external-general-entities to false does
		 * not seem to prevent the parser from resolving the entities That is
		 * why we make sure here to have the &amp;, &lt; and &gt; entities
		 * preserved. (&gt; needs to be handled for the sake of the following
		 * string ']]>', that might occur in a text node)
		 */
		if (s.equals("&")) {
			s = "&amp;";
		} else if (s.equals("<")) {
			s = "&lt;";
		} else if (s.equals(">")) {
			s = "&gt;";
		}

		/*
		 * If this text isn't empty, then check it for something useful NOTE: We
		 * use only local names here, so all dc:* elements are tested minus the
		 * prefix NOTE: If current element's text node contains entities
		 * characters() will be called more than once for this element and that
		 * is why we use concatenation to a buffer below.
		 */
		if (s.trim().length() > 0) {
			// dc:Title - concatenate it
			if (curElement.equals("Title")) {
				this.title.append(s);
			}

			// dc:Creator - concatenate it (complete creators are collected in
			// endElement)
			else if (curElement.equals("Creator")) {
				this.creator.append(s);
			}

			// dc:Identifer - set identifier property if this is the one
			// referenced by unique-identifier
			else if (curElement.equals("Identifier")
					&& this.curId != null
					&& this.curId.equals(this.uniqueIdRef)) {
				this.identifier.append(s);
			}

			// dc:Format = concatenate it
			else if (curElement.equals("Format")) {
				this.specVersion.append(s);
			}
		}
	}

	public void endElement(String namespaceURI,			
			String localName, String qName) {
		if (curElement.equals("Creator")) {
			if (this.creators == null) {
				this.creators = new ArrayList<String>();
			}
			this.creators.add(this.creator.toString().trim());
			this.creator.setLength(0);
		}

	}

	// toString() for debugging
	public String toString() {
		String s;

		// First, spit out some basic stuff
		s = super.toString() + "\n"
				+ "\t[identifier="
				+ this.getIdentifier() + "]\n"
				+ "\t[title=" + this.getTitle()
				+ "]\n" + "\t[specVersion="
				+ this.getSpecVersion() + "]\n"
				+ "\t[dtbType="
				+ this.metaDtbMultimediaType
				+ "]\n" + "\t[audioFormat="
				+ this.metaDtbAudioFormat + "]\n"
				+ "\t[totalTime="
				+ metaDtbTotalTime + "]\n";
		if (this.creators != null) {
			s = s + "\t[creators:\n";
			for (String creator : this.creators) {
				s = s + "\t\titem=" + creator
						+ "\n";
			}
			s = s + "\t]\n";
		} else {
			s = s + "\t[creators=null]\n";
		}

		// Now the manifest
		if (this.manifest != null) {
			s = s + "\t[manifest:\n";
			for (ManifestFile f : this.manifest.values()) {
				try {
					s = s
							+ "\t\titem="
							+ f.getCanonicalPath()
							+ "\n";
				} catch (IOException e) {
					s = s + "\t\titem="
							+ f.getName() + "\n";
				}
			}
			s = s + "\t]\n";
		} else {
			s = s + "\t[manifest=null]\n";
		}

		// And the spine ...
		if (this.spine != null) {
			s = s + "\t[spine:\n";
			for (ManifestFile f : this.spine.values()) {
				try {
					s = s
							+ "\t\titem="
							+ f.getCanonicalPath()
							+ "\n";
				} catch (IOException e) {
					s = s + "\t\titem="
							+ f.getName() + "\n";
				}
			}
			s = s + "\t]\n";
		} else {
			s = s + "\t[spine=null]\n";
		}

		// "... and the rest ... "
		s = s + "\t[ncx=" + this.ncx + "]\n";
		s = s + "\t[resource=" + this.resource
				+ "]\n";
		if (this.smilFiles != null) {
			s = s + "\t[smilFiles:\n";
			for (ManifestFile f : this.smilFiles.values()) {
				s = s + "\t\titem=" + f
						+ "\n";
			}
			s = s + "\t]\n";
		} else {
			s = s + "\t[smilFiles=null]\n";
		}
		if (this.textFiles != null) {
			s = s + "\t[textFiles:\n";
			for (ManifestFile f : this.textFiles.values()) {
				s = s + "\t\titem=" + f
						+ "\n";
			}
			s = s + "\t]\n";
		} else {
			s = s + "\t[textFiles=null]\n";
		}
		if (this.audioFiles != null) {
			s = s + "\t[audioFiles:\n";
			for (ManifestFile f : this.audioFiles.values()) {
				s = s + "\t\titem=" + f
						+ "\n";
			}
			s = s + "\t]\n";
			if (this.aacFiles != null) {
				s = s + "\t[aacFiles:\n";
				for (ManifestFile f : this.aacFiles.values()) {
					s = s + "\t\titem="
							+ f + "\n";
				}
				s = s + "\t]\n";
			} else {
				s = s + "\t[aacFiles=null]\n";
			}
			if (this.mp3Files != null) {
				s = s + "\t[mp3Files:\n";
				for (ManifestFile f : this.mp3Files.values()) {
					s = s + "\t\titem="
							+ f + "\n";
				}
				s = s + "\t]\n";
			} else {
				s = s + "\t[mp3Files=null]\n";
			}
			if (this.wavFiles != null) {
				s = s + "\t[wavFiles:\n";
				for (ManifestFile f : this.wavFiles.values()) {
					s = s + "\t\titem="
							+ f + "\n";
				}
				s = s + "\t]\n";
			} else {
				s = s + "\t[wavFiles=null]\n";
			}
		} else {
			s = s + "\t[audioFiles=null]\n";
		}
		if (this.imageFiles != null) {
			s = s + "\t[imageFiles:\n";
			for (ManifestFile f : this.imageFiles.values()) {
				s = s + "\t\titem=" + f
						+ "\n";
			}
			s = s + "\t]\n";
			if (this.jpegFiles != null) {
				s = s + "\t[jpegFiles:\n";
				for (ManifestFile f : this.jpegFiles.values()) {
					s = s + "\t\titem="
							+ f + "\n";
				}
				s = s + "\t]\n";
			} else {
				s = s + "\t[jpegFiles=null]\n";
			}
			if (this.pngFiles != null) {
				s = s + "\t[pngFiles:\n";
				for (ManifestFile f : this.pngFiles.values()) {
					s = s + "\t\titem="
							+ f + "\n";
				}
				s = s + "\t]\n";
			} else {
				s = s + "\t[pngFiles=null]\n";
			}
			if (this.svgFiles != null) {
				s = s + "\t[svgFiles:\n";
				for (ManifestFile f : this.svgFiles.values()) {
					s = s + "\t\titem="
							+ f + "\n";
				}
				s = s + "\t]\n";
			} else {
				s = s + "\t[svgFiles=null]\n";
			}
		} else {
			s = s + "\t[imageFiles=null]\n";
		}

		return s;
	}

	private StringBuffer title = new StringBuffer();

	private List<String> creators;

	private StringBuffer creator = new StringBuffer();

	private StringBuffer specVersion = new StringBuffer();

	private Map<String,ManifestFile> manifest; // contains all files in manifest

	private NcxFile ncx;

	private ResourceFile resource;

	private Map<String,TextFile> textFiles; // is a subset of manifest, etc for the
	// below

	private Map<String,AudioFile> audioFiles;

	private Map<String,AacFile> aacFiles; // is a subset of audiofile

	private Map<String,Mp3File> mp3Files;

	private Map<String,WavFile> wavFiles;

	private Map<String,ImageFile> imageFiles;

	private Map<String,JpegFile> jpegFiles;

	private Map<String,PngFile> pngFiles;

	private Map<String,SvgFile> svgFiles;

	private Map<String,SmilFile> smilFiles;

	private Map<String,CssFile> cssFiles;

	private Map<String,DtdFile> dtdFiles;
	
	private Map<String,XsltFile> xsltFiles;

	private Map<String,SmilFile> spine; // contains all smil files in spine

	private StringBuffer identifier = new StringBuffer();

	private String metaDtbMultimediaType;

	private String metaDtbMultimediaContent;

	private SmilClock metaDtbTotalTime;

	private String metaDtbAudioFormat;

	private String uniqueIdRef; // The unique-identifier idref from the

	// <package> element
	private String curId; // The id of the element currently being parsed (if

	// needed)
	private String curElement; // The name of the element currently being
	// parsed

	public Map<String,? extends File> getFileRefs() {
		return this.getManifest();
	}

	private static final long serialVersionUID = -2800261762864528767L;
}
