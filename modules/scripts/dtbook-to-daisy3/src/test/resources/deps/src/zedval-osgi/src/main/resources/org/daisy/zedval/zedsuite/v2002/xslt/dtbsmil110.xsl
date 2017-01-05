<?xml version="1.0" encoding="ASCII" ?>

<!--
 ZedVal - ANSI/NISO Z39.86-2002 DTB Validator
 Copyright (C) 2003 Daisy Consortium

 This library is free software; you can
 redistribute it and/or modify it under the
 terms of the GNU Lesser General Public License
 as published by the Free Software Foundation;
 either version 2.1 of the License,
 or (at your option) any later version.

 This library is distributed in the hope that it
 will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.
 See the GNU Lesser General Public License for
 more details.

 You should have received a copy of the
 GNU Lesser General Public License along with
 this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330,
 Boston, MA  02111-1307  USA

 For information about the Daisy Consortium,
 visit www.daisy.org or contact info@mail.daisy.org.
 For development issues, contact
 markus.gylling@tpb.se.
-->

<!-- Stylesheet to implement Zed SMIL file tests
     James Pritchett
     Version 0.2.0 (10/04/2003)
     Version 0.3.0 (30/05/2003)
        - Disabled text node template (now handled by custom SMIL-dtbook test)
        - Added smil_metaTextDtbTotElaAccurate, smil_validRegion
     Version 1.0.0 (09/07/2003)
        - Fixed bug in testNoValue template that caused whitespace to pass through (bug #753316) 
        - Added string() conversion for attributes in customTest test (to catch missing attributes)      
        - Fixed bug in ncxSmilCustomTest that caused bogus error message if NCX didn't exist (bug #768668)
     Version 1.0.2 (2005-02-15, Piotr Kiernicki)
     	- fixed bug #958191; added a global variable $ncxDoc. 
-->

<!--
    This sheet implements the following SMIL tests:
		smil_metaDtbUidPresence - dtb:uid meta element exists
		smil_metaDtbGenPresence - dtb:generator meta element exists
		smil_metaDtbTotElaPresence - dtb:totalElapsedTime meta element exists
		smil_clipEndAfterclipBegin - audio element clipEnd attribute value is greater than clipBegin
		smil_clipEndBeforeEOF - audio element clipEnd attribute value is less than end of file
		smil_metaTextDtbTotElaAccurate - dtb:totalElapsedTime contains value of 0 [for text-only books]
        smil_validRegion - region attribute on media object points to region defined in layout

    It also implements the following NCX test, since it is best handled via analysis
    of the SMIL files:
        ncx_smilCustomTest - for each SMIL file listed in manifest, attributes of all customTest
                    elements are duplicated once in smilCustomTest element in NCX head
-->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:exslt="http://exslt.org/common"
	xmlns:zedSuite="xalan://org.daisy.zedval.zedsuite.v2002.zedXalanExt"
	xmlns:saxon="http://saxon.sf.net/"
	exclude-result-prefixes="exslt saxon zedSuite"
>
<xsl:output method="xml" indent="yes" encoding="ISO-8859-1"
	    omit-xml-declaration="yes"
/>

<xsl:param name="opfPath" />
<xsl:param name="ncxName" />
<xsl:variable name="ncxDoc" select="document(concat($opfPath,$ncxName))" />
<xsl:param name="inputName" />

<!-- INITIALIZATION
    This section collects document data in variables that we'll then use
    in doing the tests
-->
<!-- Tests for metadata node existence:  get counts of the ones we care about -->
<xsl:variable name="uidNodeCount" select="count(//meta[@name='dtb:uid'])" />
<xsl:variable name="elapsedNodeCount" select="count(//meta[@name='dtb:totalElapsedTime'])" />
<xsl:variable name="generatorNodeCount" select="count(//meta[@name='dtb:generator'])" />
<xsl:key name="regions" match="layout/region" use="@id" />

<!-- OUTPUT
    Here we evaluate the data collected above
-->
<xsl:template match="@*|text()" />		<!-- Eat up everything by default -->

<xsl:template match="/" >                   <!-- Root element just wraps everything in <testResults> -->
    <xsl:element name="testResults">
        <xsl:apply-templates />
    </xsl:element>
</xsl:template>

<xsl:template match="head">
<!-- metadata existence tests:
    Test for one or more of the following:  dtb:uid, dtb:totalElapsedTime, dtb:generator
    Also tests for totalElapsedTime = 0.  This stylesheet will test this for all books, but
    ZedVal will only report it as an error for text-only books.
-->
        <xsl:call-template name="testExistence">
            <xsl:with-param name="testID" select="'smil_metaDtbUidPresence'" />
            <xsl:with-param name="testNodeCount" select="$uidNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:uid'" />
        </xsl:call-template>
        <xsl:call-template name="testExistence">
            <xsl:with-param name="testID" select="'smil_metaDtbTotElaPresence'" />
            <xsl:with-param name="testNodeCount" select="$elapsedNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:totalElapsedTime'" />
        </xsl:call-template>
        <xsl:call-template name="testExistence">
            <xsl:with-param name="testID" select="'smil_metaDtbGenPresence'" />
            <xsl:with-param name="testNodeCount" select="$generatorNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:generator'" />
        </xsl:call-template>
        <xsl:apply-templates />
</xsl:template>

<!-- Test for totalElapsedTime = 0.
     This stylesheet will return any value != 0 as a failure, but ZedVal will
     only report the failure for text-only books
-->
<xsl:template match="meta[@name='dtb:totalElapsedTime']">
    <xsl:variable name="elapsedValue" select="zedSuite:smilToSec(@content)" />
    <xsl:if test="$elapsedValue > 0">
        <xsl:element name="testFailure">
            <xsl:attribute name="testRef">smil_metaTextDtbTotElaAccurate</xsl:attribute>
            <xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
            <xsl:element name="detail">
                dtb:totalElapsedTime value = <xsl:value-of select="@content" />
            </xsl:element>
        </xsl:element>
    </xsl:if>
</xsl:template>


<!-- Audio file tests:
    Test for audio clip begin < end, audio clip end < end of file
    Test any region attribute value for validity
-->
<xsl:template match="audio">
    <xsl:variable name="audioName" select="concat($opfPath,@src)" />

    <!-- Convert clip SMIL clock values to seconds and get audio file duration -->
    <xsl:variable name="beginSec" select="zedSuite:smilToSec(@clipBegin)" />
    <xsl:variable name="endSec" select="zedSuite:smilToSec(@clipEnd)" />
    <xsl:variable name="fileDur" select="zedSuite:fileDur($audioName)" />

    <!-- Note:  Don't do this test if either clip attribute is malformed -->
    <xsl:if test="$beginSec &gt;= 0 and $endSec &gt;= 0 and $beginSec > $endSec">
        <xsl:element name="testFailure">
            <xsl:attribute name="testRef">smil_clipEndAfterclipBegin</xsl:attribute>
            <xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
            <xsl:element name="detail">
                clipBegin=<xsl:value-of select="@clipBegin"/>, clipEnd=<xsl:value-of select="@clipEnd"/>
            </xsl:element>
        </xsl:element>
    </xsl:if>

    <!-- Note:  Don't do this test if either clip attribute or file duration is unknown -->
    <xsl:if test="$beginSec &gt;= 0 and $endSec &gt;= 0 and zedSuite:compareSmil(@clipEnd,$fileDur) = 1">
        <xsl:element name="testFailure">
            <xsl:attribute name="testRef">smil_clipEndBeforeEOF</xsl:attribute>
            <xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
            <xsl:element name="detail">
                Total duration of audio file <xsl:value-of select="@src"/> is <xsl:value-of select="zedSuite:smilToSec($fileDur)"/> seconds, clipEnd=<xsl:value-of select="@clipEnd"/>
            </xsl:element>
        </xsl:element>
    </xsl:if>

    <!-- Test region attribute to be sure that it points to a region element -->
    <xsl:if test="@region != ''">
        <xsl:if test="count(key('regions',@region)) = 0" >
            <xsl:element name="testFailure">
                <xsl:attribute name="testRef">smil_validRegion</xsl:attribute>
                <xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
                <xsl:element name="detail">
                    region attribute value = <xsl:value-of select="@region" />
                </xsl:element>
            </xsl:element>        
        </xsl:if>
    </xsl:if>
    
</xsl:template>

<xsl:template match="img">
    <!-- Test region attribute to be sure that it points to a region element -->
    <xsl:if test="@region != ''">
        <xsl:if test="count(key('regions',@region)) = 0" >
            <xsl:element name="testFailure">
                <xsl:attribute name="testRef">smil_validRegion</xsl:attribute>
                <xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
                <xsl:element name="detail">
                    region attribute value = <xsl:value-of select="@region" />
                </xsl:element>
            </xsl:element>        
        </xsl:if>
    </xsl:if>
</xsl:template>

<xsl:template match="text">
    <!-- Test region attribute to be sure that it points to a region element -->
    <xsl:if test="@region != ''">
        <xsl:if test="count(key('regions',@region)) = 0" >
            <xsl:element name="testFailure">
                <xsl:attribute name="testRef">smil_validRegion</xsl:attribute>
                <xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
                <xsl:element name="detail">
                    region attribute value = <xsl:value-of select="@region" />
                </xsl:element>
            </xsl:element>        
        </xsl:if>
    </xsl:if>
</xsl:template>

<!-- customTest tests: be sure they're duplicated in NCX
-->
<xsl:template match="customTest">
    <!-- Try to select the matching smilCustomTest in the NCX -->
    <xsl:variable name="myId" select="@id" />
<!--    <xsl:variable name="ncxSCTnode" select="document(concat($opfPath,$ncxName))//smilCustomTest[@id=$myId]" />-->
<!--
	Bug #958191, 
	2005-02-15, PK 
	If you call document() more than once with the same URI parameter
	you get a result only at the first call.
	It is also more effective to save the result doc as a global variable
	and that is what we use below ($ncxDoc). 

	2005-03-01, PK
	The problem with repeated calls of document with the same input parameter 
	was actually caused by another bug: #1154318.
-->
	<xsl:variable name="ncxSCTnode" select="$ncxDoc//smilCustomTest[@id=$myId]" />


    <!-- If the NCX file doesn't exist, skip this whole thing (that's a different problem) -->
    <xsl:if test="zedSuite:fileExists(concat($opfPath,$ncxName))">
        <!-- First, test to see if there's a matching node ... -->
        <xsl:choose>
            <xsl:when test="count($ncxSCTnode) = 0">
                <xsl:element name="testFailure">
                    <xsl:attribute name="testRef">ncx_smilCustomTest</xsl:attribute>
                    <xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
                    <xsl:element name="detail">
                        customTest <xsl:value-of select="$myId" /> has no matching smilCustomTest in the NCX
	                </xsl:element>
                </xsl:element>
            </xsl:when>
        <!-- If it's there, test the attributes, too -->
            <xsl:otherwise>
                <xsl:if test="string(@defaultState) != string($ncxSCTnode/@defaultState) or 
                              string(@override) != string($ncxSCTnode/@override)"  >
                    <xsl:element name="testFailure">
                        <xsl:attribute name="testRef">ncx_smilCustomTest</xsl:attribute>
                        <xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
                        <xsl:element name="detail">
                            Attributes for customTest id='<xsl:value-of select="$myId" />' do not match those on
                            corresponding smilCustomTest in NCX
                        </xsl:element>
                    </xsl:element>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:if>
</xsl:template>


<!-- ================================================================================
     Called templates that do various kinds of tests
     ================================================================================ -->

<!-- testNoValue:  Tests node for non-null content -->
<xsl:template name="testNoValue">
    <xsl:param name="testID" />
    <xsl:param name="testNode" />
    <xsl:param name="elemName" />

    <xsl:if test="normalize-space($testNode) = ''">
        <xsl:element name="testFailure">
            <xsl:attribute name="testRef"><xsl:value-of select="$testID" /></xsl:attribute>
            <xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
            <xsl:element name="detail">
                 <xsl:value-of select="$elemName" /> is empty
            </xsl:element>
        </xsl:element>
    </xsl:if>
</xsl:template>

<!-- testExistence:  Tests node count for at least one -->
<xsl:template name="testExistence">
    <xsl:param name="testID" />
    <xsl:param name="testNodeCount" />
    <xsl:param name="elemName" />

    <xsl:if test="$testNodeCount &lt; 1">
        <xsl:element name="testFailure">
            <xsl:attribute name="testRef"><xsl:value-of select="$testID" /></xsl:attribute>
            <xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
            <xsl:element name="detail">
                There are <xsl:value-of select="$testNodeCount" /> instances of <xsl:value-of select="$elemName" />
            </xsl:element>
        </xsl:element>
    </xsl:if>
</xsl:template>

<!-- testZeroOrOne:  Tests node count for zero or one -->
<xsl:template name="testZeroOrOne">
    <xsl:param name="testID" />
    <xsl:param name="testNodeCount" />
    <xsl:param name="elemName" />

    <xsl:if test="$testNodeCount &gt; 1">
        <xsl:element name="testFailure">
            <xsl:attribute name="testRef"><xsl:value-of select="$testID" /></xsl:attribute>
            <xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
            <xsl:element name="detail">
                There are <xsl:value-of select="$testNodeCount" /> instances of <xsl:value-of select="$elemName" />
            </xsl:element>
        </xsl:element>
    </xsl:if>
</xsl:template>

<!-- testExactlyOne:  Tests node count for equivalence to one -->
<xsl:template name="testExactlyOne">
    <xsl:param name="testID" />
    <xsl:param name="testNodeCount" />
    <xsl:param name="elemName" />

    <xsl:if test="$testNodeCount != 1">
        <xsl:element name="testFailure">
            <xsl:attribute name="testRef"><xsl:value-of select="$testID" /></xsl:attribute>
            <xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
            <xsl:element name="detail">
                There are <xsl:value-of select="$testNodeCount" /> instances of <xsl:value-of select="$elemName" />
            </xsl:element>
        </xsl:element>
    </xsl:if>
</xsl:template>

<!-- testZero:  Tests node count for equivalence to zero -->
<xsl:template name="testZero">
    <xsl:param name="testID" />
    <xsl:param name="testNodeCount" />
    <xsl:param name="elemName" />

    <xsl:if test="$testNodeCount != 0">
        <xsl:element name="testFailure">
            <xsl:attribute name="testRef"><xsl:value-of select="$testID" /></xsl:attribute>
            <xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
            <xsl:if test="$testNodeCount &gt; 1">
                <xsl:element name="detail">
                    There are <xsl:value-of select="$testNodeCount" /> instances of <xsl:value-of select="$elemName" />
                </xsl:element>
            </xsl:if>
        </xsl:element>
    </xsl:if>
</xsl:template>

</xsl:stylesheet>
