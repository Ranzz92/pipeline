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

/**
 * Carries static specwide strings
 * 
 * @author mgylling
 */
public final class ZedConstants {

    private ZedConstants() {
    }

    public static final String PUBLIC_ID_RESOURCE_Z2005 = "-//NISO//DTD resource 2005-1//EN";

    public static final String PUBLIC_ID_RESOURCE_Z2002 = "-//NISO//DTD resource v1.1.0//EN";

    public static final String NAMESPACEURI_RESOURCE_Z2005 = "http://www.daisy.org/z3986/2005/resource/";

    public static final String MIMETYPE_RESOURCE_Z2005 = "application/x-dtbresource+xml";

    public static final String MIMETYPE_RESOURCE_Z2002 = "text/xml";

    public static final String PUBLIC_ID_DTBOOK_Z2005_1 = "-//NISO//DTD dtbook 2005-1//EN";
    
    public static final String PUBLIC_ID_DTBOOK_Z2005_2 = "-//NISO//DTD dtbook 2005-2//EN";
    
    public static final String PUBLIC_ID_DTBOOK_Z2005_3 = "-//NISO//DTD dtbook 2005-3//EN";

    public static final String PUBLIC_ID_DTBOOK_Z2002 = "-//NISO//DTD dtbook v1.1.0//EN";

    public static final String NAMESPACEURI_DTBOOK_Z2005 = "http://www.daisy.org/z3986/2005/dtbook/";

    public static final String MIMETYPE_DTBOOK_Z2005 = "application/x-dtbook+xml";

    public static final String MIMETYPE_DTBOOK_Z2002 = "text/xml";

    public static final String PUBLIC_ID_SMIL_Z2005_1 = "-//NISO//DTD dtbsmil 2005-1//EN";
    
    public static final String PUBLIC_ID_SMIL_Z2005_2 = "-//NISO//DTD dtbsmil 2005-2//EN";

    public static final String PUBLIC_ID_SMIL_Z2002 = "-//NISO//DTD dtbsmil v1.1.0//EN";

    public static final String NAMESPACEURI_SMIL_Z2005 = "http://www.w3.org/2001/SMIL20/";

    public static final String MIMETYPE_SMIL = "application/smil";

    public static final String PUBLIC_ID_NCX_Z2005 = "-//NISO//DTD ncx 2005-1//EN";

    public static final String PUBLIC_ID_NCX_Z2002 = "-//NISO//DTD ncx v1.1.0//EN";

    public static final String NAMESPACEURI_NCX_Z2005 = "http://www.daisy.org/z3986/2005/ncx/";

    public static final String MIMETYPE_NCX_Z2005 = "application/x-dtbncx+xml";

    public static final String MIMETYPE_NCX_Z2002 = "text/xml";

    public static final String PUBLIC_ID_OPF_Z2005 = "+//ISBN 0-9673008-1-9//DTD OEB 1.2 Package//EN";

    public static final String PUBLIC_ID_OPF_Z2002 = "+//ISBN 0-9673008-1-9//DTD OEB 1.0.1 Package//EN";

    public static final String NAMESPACEURI_OPF_Z2005 = "http://openebook.org/namespaces/oeb-package/1.0/";

    public static final String NAMESPACEURI_OPF_Z2002 = "http://openebook.org/namespaces/oeb-package/1.0/";

    public static final String MIMETYPE_OPF = "text/xml";
    
    public static final String MIMETYPE_XSLT_1 = "application/xml";
    
    public static final String MIMETYPE_XSLT_2 = "application/xslt+xml";

    public static final String MIMETYPE_AUDIO_MP4AAC_Z2005 = "audio/mpeg4-generic";

    public static final String MIMETYPE_AUDIO_MP4AAC_Z2002 = "audio/MP4A-LATM";

    public static final String MIMETYPE_AUDIO_MP3 = "audio/mpeg";

    public static final String MIMETYPE_AUDIO_PCMWAV = "audio/x-wav";

    public static final String MIMETYPE_IMAGE_JPEG = "image/jpeg";

    public static final String MIMETYPE_IMAGE_PNG = "image/png";

    public static final String MIMETYPE_IMAGE_SVG = "image/svg+xml";

    public static final String MIMETYPE_CSS = "text/css";
    
    public static final String MIMETYPE_DTD_Z2002 = "text/xml";
            
    public static final String MIMETYPE_DTD_Z2005 = "application/xml-dtd";
    
    
   
    public static final String Z3986_VERSION_2002 = "ANSI/NISO Z39.86-2002";

    public static final String Z3986_VERSION_2005 = "ANSI/NISO Z39.86-2005";
    
    
    
    public static final int INITIAL_CAPACITY_COLLECTION = 400; //for collections instantatiators, they are way too low at default
    
    public static final int INITIAL_CAPACITY_STRINGBUFFER = 50; //for stringbuffer/stringbuilder instantatiators

}
