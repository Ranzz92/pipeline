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

<!-- Stylesheet to implement Zed NCX file tests
     James Pritchett
     Version 0.2.0 (08/04/2003)
     Version 0.3.0 (29/05/2003)
        - Added implementations of ncx_multiNavLabel, ncx_mapRef
     Version 1.0.0 (25/06/2003)
        - Fixed bug in testNoValue template that caused whitespace to pass through (bug #753316)
        - Fixed bug that kept audio tests from being performed inside navLabels (bug #757885)
     Version 1.0.2
     	(02/14/2005, Piotr Kiernicki)
     	- Added existence tests for dtb:pageSpecial, dtb:pageFront, dtb:maxPageNormal, dtb:pageNormal
     	(02/28/2005, Piotr Kiernicki)
     	- Added ncx_srcSMILFragment test (bug #1062602)
     04/05/2005, James Pritchett
     	- Added ncx_mapRefToNavPoint and ncx_navPointToPage tests (bug #837440)
-->

<!--
    This sheet implements the following NCX tests:
		ncx_metaDtbUid - dtb:uid meta element exists
		ncx_metaDbtDepthPresence - dtb:depth meta element exists
		ncx_metaDtbGeneratorPresence - dtb:generator meta element exists
		ncx_metaDtbPageSpecialPresence - dtb:pageSpecial meta element exists
		ncx_metaDtbPageFrontPresence - dtb:pageFront meta element exists
		ncx_metaDtbMaxPageNormalPresence - dtb:maxPageNormal meta element exists
		ncx_metaDtbPageNormalPresence - dtb:pageNormal meta element exists
		
		ncx_clipEndAfterclipBegin - audio element clipEnd attribute value is greater than clipBegin
		ncx_clipEndBeforeEOF - audio element clipEnd attribute value is less than end of file
        ncx_multiNavLabel - If multiple navLabels exist within an NCX node, lang attribute is not repeated
        ncx_mapRef - mapRef attribute on navTarget points to id of innermost navPoint whose pageRef points to that navTarget
        ncx_srcSMILFragment - src attribute on content element references SMIL file fragment
        ncx_mapRefToNavPoint - mapRef points to a navPoint
        ncx_navPointToPage - pageRef attribute on navPoint points to a navTarget in the pagenum navList
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

<xsl:param name="opfPath" select="'.'" />

<!-- INITIALIZATION
    This section collects document data in variables that we'll then use
    in doing the tests
-->
<!-- Tests for metadata node existence:  get counts of the ones we care about -->
<xsl:variable name="uidNodeCount" select="count(//meta[@name='dtb:uid'])" />
<xsl:variable name="depthNodeCount" select="count(//meta[@name='dtb:depth'])" />
<xsl:variable name="generatorNodeCount" select="count(//meta[@name='dtb:generator'])" />
<xsl:variable name="pageSpecialNodeCount" select="count(//meta[@name='dtb:pageSpecial'])" />
<xsl:variable name="pageFrontNodeCount" select="count(//meta[@name='dtb:pageFront'])" />
<xsl:variable name="maxPageNormalNodeCount" select="count(//meta[@name='dtb:maxPageNormal'])" />
<xsl:variable name="pageNormalNodeCount" select="count(//meta[@name='dtb:pageNormal'])" />
		
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
    Test for one or more of the following:  dtb:uid, dtb:depth, dtb:generator
-->
        <xsl:call-template name="testExistence">
            <xsl:with-param name="testID" select="'ncx_metaDtbUid'" />
            <xsl:with-param name="testNodeCount" select="$uidNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:uid'" />
        </xsl:call-template>
        <xsl:call-template name="testExistence">
            <xsl:with-param name="testID" select="'ncx_metaDbtDepthPresence'" />
            <xsl:with-param name="testNodeCount" select="$depthNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:depth'" />
        </xsl:call-template>
        <xsl:call-template name="testExistence">
            <xsl:with-param name="testID" select="'ncx_metaDtbGeneratorPresence'" />
            <xsl:with-param name="testNodeCount" select="$generatorNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:generator'" />
        </xsl:call-template>
		
		<xsl:call-template name="testExistence">
            <xsl:with-param name="testID" select="'ncx_metaDtbPageFrontPresence'" />
            <xsl:with-param name="testNodeCount" select="$pageFrontNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:pageFront'" />
        </xsl:call-template>
        <xsl:call-template name="testExistence">
            <xsl:with-param name="testID" select="'ncx_metaDtbPageSpecialPresence'" />
            <xsl:with-param name="testNodeCount" select="$pageSpecialNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:pageSpecial'" />
        </xsl:call-template>
        <xsl:call-template name="testExistence">
            <xsl:with-param name="testID" select="'ncx_metaDtbMaxPageNormalPresence'" />
            <xsl:with-param name="testNodeCount" select="$maxPageNormalNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:maxPageNormal'" />
        </xsl:call-template>
        <xsl:call-template name="testExistence">
            <xsl:with-param name="testID" select="'ncx_metaDtbPageNormalPresence'" />
            <xsl:with-param name="testNodeCount" select="$pageNormalNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:pageNormal'" />
        </xsl:call-template>
        <xsl:apply-templates />
</xsl:template>

<!-- Audio file tests:
    Test for audio clip begin < end, audio clip end < end of file
-->
<xsl:template match="audio">
    <xsl:variable name="audioName" select="concat(concat($opfPath,'/'),@src)" />

    <!-- Convert clip SMIL clock values to seconds and get audio file duration -->
    <xsl:variable name="beginSec" select="zedSuite:smilToSec(@clipBegin)" />
    <xsl:variable name="endSec" select="zedSuite:smilToSec(@clipEnd)" />
    <xsl:variable name="fileDur" select="zedSuite:fileDur($audioName)" />

    <!-- Note:  Don't do this test if either clip attribute is malformed -->
    <xsl:if test="$beginSec &gt;= 0 and $endSec &gt;= 0 and $beginSec &gt; $endSec">
        <xsl:element name="testFailure">
            <xsl:attribute name="testRef">ncx_clipEndAfterclipBegin</xsl:attribute>
            <xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
            <xsl:element name="detail">
                clipBegin=<xsl:value-of select="@clipBegin"/>, clipEnd=<xsl:value-of select="@clipEnd"/>
            </xsl:element>
        </xsl:element>
    </xsl:if>

    <!-- Note:  Don't do this test if either clip attribute or file duration is unknown -->
    <xsl:if test="$beginSec &gt;= 0 and $endSec &gt;= 0 and zedSuite:compareSmil(@clipEnd,$fileDur) = 1">
        <xsl:element name="testFailure">
            <xsl:attribute name="testRef">ncx_clipEndBeforeEOF</xsl:attribute>
            <xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
            <xsl:element name="detail">
                Total duration of audio file <xsl:value-of select="@src"/> is <xsl:value-of select="zedSuite:smilToSec($fileDur)"/> seconds, clipEnd=<xsl:value-of select="@clipEnd"/>
            </xsl:element>
        </xsl:element>
    </xsl:if>
</xsl:template>

<!-- navPoint test:  Be sure that any pageRef points to a navTarget in the "pagenum" navList -->
<xsl:template match="navPoint">
    <xsl:variable name="myPageRef" select="@pageRef" />
    <xsl:variable name="myID" select="@id" />
    <xsl:if test="$myPageRef != ''">
	    <!-- This selects the page navTarget that this point references; if it isn't there, that's an error -->
	    <xsl:variable name="myNavTarget" select="/ncx/navList[@class='pagenum']/navTarget[@id=$myPageRef]" />
	    <xsl:if test="count($myNavTarget) = 0">
	    	<xsl:element name="testFailure">
	    		<xsl:attribute name="testRef">ncx_navPointToPage</xsl:attribute>
	    		<xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
	    		<xsl:element name="detail">
	    			navPoint with id="<xsl:value-of select="$myID" />" has pageRef="<xsl:value-of select="$myPageRef" />" that does not point to a navTarget in the pagenum navList
	    		</xsl:element>
	    	</xsl:element>
	    </xsl:if>
	</xsl:if>
	<xsl:apply-templates />
</xsl:template>

<!-- navTarget tests:  Be sure that mapRef points to a navPoint and that it is the innermost navPoint (for pages)
-->
<xsl:template match="navTarget">
    <xsl:variable name="myMapRef" select="@mapRef" />
    <xsl:variable name="myID" select="@id" />
    <xsl:variable name="myClass" select="@class" />
    <!-- This selects the navPoint that this target references; if it isn't there, that's an error -->
    <xsl:variable name="myNavPoint" select="../../navMap/descendant::navPoint[@id=$myMapRef]" />
    <xsl:if test="count($myNavPoint) = 0">
    	<xsl:element name="testFailure">
    		<xsl:attribute name="testRef">ncx_mapRefToNavPoint</xsl:attribute>
    		<xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
    		<xsl:element name="detail">
    			navTarget with id="<xsl:value-of select="$myID" />" has mapRef="<xsl:value-of select="$myMapRef" />" that does not point to a navPoint
    		</xsl:element>
    	</xsl:element>
    </xsl:if>

	<xsl:if test="$myClass = 'pagenum'">
	    <!-- This selects any descendant of the navPoint this page references via mapRef that has a pageRef pointing here -->
	    <xsl:variable name="innerPoint" select="../../navMap/navPoint[@id=$myMapRef]/descendant::navPoint[@pageRef=$myID]" />
	    <xsl:if test="count($innerPoint) > 0">
	        <xsl:element name="testFailure">
	            <xsl:attribute name="testRef">ncx_mapRef</xsl:attribute>
	            <xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
	            <xsl:element name="detail">
	                There is a descendant of navPoint/@id="<xsl:value-of select="@mapRef" />" with pageRef="<xsl:value-of select="@id" />"
	            </xsl:element>
	        </xsl:element>
	    </xsl:if>
	</xsl:if>

</xsl:template>

<!-- navLabel tests:  Be sure that there's exactly one navLabel for a given language
-->
<xsl:template match="navLabel">
    <xsl:variable name="myLang" select="@lang" />
    <xsl:variable name="otherLabels" select="preceding-sibling::navLabel[string(@lang) = string($myLang)]" />
    <xsl:if test="count($otherLabels) > 0">
        <xsl:element name="testFailure">
            <xsl:attribute name="testRef">ncx_multiNavLabel</xsl:attribute>
            <xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
            <xsl:element name="detail">
                There is already a navLabel with lang="<xsl:value-of select="$myLang" />"
            </xsl:element>
        </xsl:element>
    </xsl:if>
    <xsl:apply-templates />
</xsl:template>

<!-- ncx_srcSMILFragment test
-->
<xsl:template match="content">
	<xsl:variable name="smilName" select="substring-before(@src,'#')"/>
	<xsl:variable name="smilFragmentID" select="substring-after(@src,'#')"/>

	<xsl:variable name="smilDoc" select="document(concat($opfPath,$smilName))"/>
	<xsl:variable name="smilFragmentCount" select="count($smilDoc//child::node()[@id=$smilFragmentID])"/>
		
    <xsl:if test="$smilFragmentCount != 1">
        <xsl:element name="testFailure">
            <xsl:attribute name="testRef"><xsl:value-of select="'ncx_srcSMILFragment'" /></xsl:attribute>
            <xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
            <xsl:element name="detail">
                <xsl:text>There are </xsl:text>
                <xsl:value-of select="$smilFragmentCount" /> 
                <xsl:text> instances of </xsl:text>
                <xsl:value-of select="concat(concat('[@id=',$smilFragmentID),']')" /> 
                <xsl:text> in </xsl:text>
                <xsl:value-of select="$smilName" />
			</xsl:element>
        </xsl:element>
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
