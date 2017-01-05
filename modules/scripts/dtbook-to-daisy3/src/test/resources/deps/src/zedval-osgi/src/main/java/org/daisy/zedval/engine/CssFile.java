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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.batik.css.parser.Parser;
import org.daisy.util.text.URIUtils;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.DocumentHandler;
import org.w3c.css.sac.ErrorHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.css.sac.SACMediaList;
import org.w3c.css.sac.SelectorList;
/**
 * A <code>CssFile</code> object represents a single DTB css file
 * @author Markus Gylling
 */
public class CssFile extends ManifestFile implements Referring,
        DocumentHandler, ErrorHandler {
    
    private Parser parser;
    
	private Map<String,File> fileRefs;

    public CssFile(String fullPath, String id, String mimeType, String manifestURI) {
        super(fullPath, id, mimeType, manifestURI);
    }

    void initialize() throws ZedFileInitializationException {
        parser = new Parser();
        parser.setLocale(Locale.getDefault());
        parser.setDocumentHandler(this);
        parser.setErrorHandler(this);
        try {
            parser.parseStyleSheet(new InputSource(this.toURI().toString()));
        } catch (CSSException e) {
            throw new ZedFileInitializationException(e.getMessage());
        } catch (IOException e) {
            throw new ZedFileInitializationException(e.getMessage());
        }
    }

//    String expectedMimeType() {
//        return null;
//    }

    public Map<String,File> getFileRefs() {
        return this.fileRefs;
    }
    
    public void property(String name, LexicalUnit value, boolean important)
            throws CSSException {
        try {
            // collect all properties that contain url() statements
            if (name
                    .matches("(^background$)|(^background-image$)|(^list-style$)")) {
                // System.err.println(name);
                try {
                    String str = value.getStringValue();
                    if (str
                            .matches("(.+\\.[Jj][Pp][Gg]$)|(.+\\.[Jj][Pp][Ee][Gg]$)|(.+\\.[Pn][Ng][Gg]$)|(.+\\.[Gg][Ii][Ff]$)|(.+\\.[Bb][Mm][Pp]$)")) {
                        try {
                            URI uri = new URI(str);
                            uri = URIUtils.resolve(this.toURI(), uri);
                            File f = new File(uri);
                            if (this.fileRefs == null)
                                this.fileRefs = new HashMap<String, File>();
                            this.fileRefs.put(f.getCanonicalPath(), f);
                        } catch (Exception e) {
                            System.err.println("importStyle error in css");
                        }
                    }
                } catch (Exception e) {
                    // System.err.println("value.getStringValue failed in css");
                    // //TODO
                }
            }
        } catch (Exception e) {
            System.err.println("css error: " + e.getMessage());
        }
    }

    public void importStyle(String inuri, SACMediaList media, String defaultNamespaceURI) {
        try {
            URI uri = new URI(inuri);
            uri = URIUtils.resolve(this.toURI(), uri);
            File f = new File(uri);
            if (this.fileRefs == null)
                this.fileRefs = new HashMap<String, File>();
            this.fileRefs.put(f.getCanonicalPath(), f);
        } catch (Exception e) {
            System.err.println("importStyle error in css"); 
        }
    }

    public void error(CSSParseException e) throws CSSException {
        System.err.println("css error: " + e.getMessage()); 
    }

    public void fatalError(CSSParseException e) throws CSSException {
        System.err.println("css fatalerror: " + e.getMessage()); 
    }

    public void warning(CSSParseException e) throws CSSException {
        System.err.println("css warning: " + e.getMessage()); 
    }

    public void startDocument(InputSource source) throws CSSException {
    }

    public void endDocument(InputSource source) throws CSSException {
    }

    public void startSelector(SelectorList selectors) throws CSSException {
    }

    public void endSelector(SelectorList selectors) throws CSSException {
    }

    public void comment(String text) throws CSSException {
    }

    public void startPage(String name, String pseudo_page) throws CSSException {
    }

    public void endPage(String name, String pseudo_page) throws CSSException {
    }

    public void ignorableAtRule(String atRule) throws CSSException {
    }

    public void namespaceDeclaration(String prefix, String uri)
            throws CSSException {
    }

    public void startFontFace() throws CSSException {
    }

    public void endFontFace() throws CSSException {
    }

    public void startMedia(SACMediaList media) throws CSSException {
    }

    public void endMedia(SACMediaList media) throws CSSException {
    }
    
    private static final long serialVersionUID = -4029016054367952753L;
}
