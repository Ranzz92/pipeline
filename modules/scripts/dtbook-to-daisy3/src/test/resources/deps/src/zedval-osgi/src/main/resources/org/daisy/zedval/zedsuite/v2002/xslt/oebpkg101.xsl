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

<!-- Stylesheet to implement Zed package file tests
     James Pritchett
     Version 0.1.0 (10/02/2003)
     Version 0.1.1 (20/02/2003)
        - Changed declaration of namespace for nodeinfo so that it can use standard (buggy)
          Xalan 2.4.1 distribution
     Version 0.2.0 (28/03/2003)
        - Added dc-metadata "value present" tests
        - Added dc-metadata existence tests
        - Added x-metadata count tests
        - Added manifest tests
        - Modularized entire stylesheet
     Version 0.3.0 (11/05/2003)
        - Added test for presence of package file in manifest
        - Fixed bug that caused dtb:multimediaType test to be skipped if x-metadata was missing
     Version 1.0.0 (20/08/2003)
        - Fixed bug in testNoValue template that caused whitespace to pass through (bug #753316)
        - Fixed bug in dtb:revisionDescription test (bug #760055)
        - Fixed bug in opf_mnfDistInfo (wrong capitalization used in file name) (bug #761269)
        - Fixed bug in opf_mnfDistInfo that caused program crash (bug #791538)
        - Fixed bug in opf_opfInManifest (pathnames in item@href caused failure) (bug #791857)
-->

<!--
    This sheet implements the following package file tests:
        opf_DcIdUniqIdref - dc:Identifier id attribute matches the value of
            unique-identifier attribute
        opf_DcIdentifierValue - dc:Identifier contains a value
        opf_DcTitleValue - dc:Title contains a value
        opf_DcPubValue - dc:Publisher contains a value
        opf_DcPubPresent - dc:Publisher is present at least once
        opf_DcDatePresent - dc:Date is present at least once
        opf_DcFormatPresent - dc:Format is present at least once
        opf_DcLangPresent - dc:Language is present at least once
        opf_xMetaMmTypePresence - x-metadata contains exactly one occurrence of
            meta element whose name attribute equals dtb:multimediaType
        opf_xMetaDtbSourceDatePresence - dtb:sourceDate occurs zero or once
        opf_xMetaDtbSourceEditionPresence - dtb:sourceEdition occurs zero or once
        opf_xMetaDtbSourcePublisherPresence - dtb:sourcePublisher occurs zero or once
        opf_xMetaDtbSourceRightsPresence - dtb:sourceRights occurs zero or once
        opf_xMetaDtbSourceTitlePresence - dtb:sourceTitle occurs zero or once
        opf_xMetaTotTimePresence - dtb:totalTime occurs exactly once
        opf_xMetaDtbProducedDatePresence - dtb:producedDate occurs zero or once
        opf_xMetaDtbRevisionPresence - dtb:revision occurs zero or once
        opf_xMetaDtbRevisionDatePresence - dtb:revisionDate occurs zero or once
        opf_xMetaDtbRevisionDescriptionPresence - dtb:revisionDescription occurs zero or once
        opf_mnfNcxId - manifest contains one item whose id value equals "ncx"
        opf_mnfDistInfo - manifest does not reference a distInfo file
        opf_mnfHrefUnique - item element href attribute value is unique among items
        opf_mnfHrefFrag - item element href attribute value does not contain a fragment identifier
        opf_opfInManifest - package file is listed in manifest
-->

<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:exslt="http://exslt.org/common"
	xmlns:opf="http://openebook.org/namespaces/oeb-package/1.0/"
	xmlns:dc="http://purl.org/dc/elements/1.0/"
	xmlns:saxon="http://saxon.sf.net/"
	exclude-result-prefixes="exslt opf dc saxon">
<xsl:output method="xml" indent="yes" encoding="ISO-8859-1"
	    omit-xml-declaration="yes"
/>

<xsl:param name="opfName" select="''" />

<!-- INITIALIZATION
    This section collects document data in variables that we'll then use
    in doing the tests
-->
<!-- Test opf_DcIdUniqIdref:  Select and count all dc:Identifier nodes
     with id=package unique-identifier -->
<xsl:variable name="uidref" select="//@unique-identifier" />
<xsl:variable name="uidNode" select="//opf:metadata/opf:dc-metadata/dc:Identifier[@id=$uidref]" />
<xsl:variable name="uidCount" select="count($uidNode)" />

<!-- Tests for dc: metadata node existence:  get counts of the ones we care about -->
<xsl:variable name="dcPubNodeCount" select="count(//dc:Publisher)" />
<xsl:variable name="dcDateNodeCount" select="count(//dc:Date)" />
<xsl:variable name="dcFormatNodeCount" select="count(//dc:Format)" />
<xsl:variable name="dcLangNodeCount" select="count(//dc:Language)" />

<!-- Tests for dtb: metadata node counts:  get counts of the ones we care about -->
<xsl:variable name="xMetadataCount" select="count(//opf:x-metadata)" />
<xsl:variable name="xMMediaNodeCount" select="count(//opf:meta[@name='dtb:multimediaType'])" />
<xsl:variable name="xSrcDateNodeCount" select="count(//opf:meta[@name='dtb:sourceDate'])" />
<xsl:variable name="xSrcEditionNodeCount" select="count(//opf:meta[@name='dtb:sourceEdition'])" />
<xsl:variable name="xSrcPubNodeCount" select="count(//opf:meta[@name='dtb:sourcePublisher'])" />
<xsl:variable name="xSrcRightsNodeCount" select="count(//opf:meta[@name='dtb:sourceRights'])" />
<xsl:variable name="xSrcTitleNodeCount" select="count(//opf:meta[@name='dtb:sourceTitle'])" />
<xsl:variable name="xTotTimeNodeCount" select="count(//opf:meta[@name='dtb:totalTime'])" />
<xsl:variable name="xProdDateNodeCount" select="count(//opf:meta[@name='dtb:producedDate'])" />
<xsl:variable name="xRevNodeCount" select="count(//opf:meta[@name='dtb:revision'])" />
<xsl:variable name="xRevDateNodeCount" select="count(//opf:meta[@name='dtb:revisionDate'])" />
<xsl:variable name="xRevDescNodeCount" select="count(//opf:meta[@name='dtb:revisionDescription'])" />

<!-- Manifest tests: -->
<!-- Count of package, NCX, and distinfo items -->
<xsl:variable name="mOpfNodeCount" select="count(//opf:item[contains(@href,$opfName)])" />
<xsl:variable name="mNcxNodeCount" select="count(//opf:item[@id='ncx'])" />
<xsl:variable name="mDistinfoNodeCount" select="count(//opf:item[contains(@href,'distInfo.dinf')])" />

<!-- Uniqueness of item hrefs:  collect a sorted list of all hrefs to test later -->
<xsl:variable name="sortedManifestItems.tf">
    <xsl:for-each select="//opf:item" >
        <xsl:sort select="@href" />
        <!-- Create a new element with the href and line number -->
        <xsl:element name="item">
            <xsl:attribute name="href"><xsl:value-of select="@href" /></xsl:attribute>
            <xsl:attribute name="line"><xsl:value-of select="saxon:line-number(.)" /></xsl:attribute>
        </xsl:element>
    </xsl:for-each>
</xsl:variable>
<xsl:variable name="sortedManifestItems" select="exslt:nodeSet($sortedManifestItems.tf)" />

<!-- OUTPUT
    Here we evaluate the data collected above
-->
<xsl:template match="@*|text()" />		<!-- Eat up everything by default -->

<xsl:template match="/" >                   <!-- Root element just wraps everything in <testResults> -->
    <xsl:element name="testResults">
        <xsl:apply-templates />
    </xsl:element>
</xsl:template>

<xsl:template match="opf:package">          <!-- Evaluate the tests on the package element -->
<!-- Test opf_DcIdUniqIdref
    Test link between <package @unique-identifier> idref and a <dc:Identifier> element
-->
        <xsl:if test="$uidCount != 1">
            <xsl:element name="testFailure">
                <xsl:attribute name="line"><xsl:value-of select="saxon:line-number()" /></xsl:attribute>
                <xsl:attribute name="testRef">opf_DcIdUniqIdref</xsl:attribute>
                <xsl:element name="detail">
                    There are <xsl:value-of select="$uidCount"/> dc:Identifier elements referenced by the package unique-identifier value '<xsl:value-of select="$uidref" />'
                </xsl:element>
            </xsl:element>
        </xsl:if>

        <xsl:apply-templates />
</xsl:template>

<!-- We need to do the dtb:multimediaType test on the metadata node, if the
     x-metadata element doesn't exist at all
-->
<xsl:template match="opf:metadata">
    <xsl:if test="$xMetadataCount = 0">
        <xsl:call-template name="testExactlyOne">
            <xsl:with-param name="testID" select="'opf_xMetaMmTypePresence'" />
            <xsl:with-param name="testNodeCount" select="$xMMediaNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:multimediaType'" />
        </xsl:call-template>
    </xsl:if>
    <xsl:apply-templates />
</xsl:template>

<xsl:template match="opf:dc-metadata">
<!-- dc: metadata existence tests:
    Test for one or more of the following:  dc:Publisher, dc:Date, dc:Format, dc:Langauge
-->
        <xsl:call-template name="testExistence">
            <xsl:with-param name="testID" select="'opf_DcPubPresent'" />
            <xsl:with-param name="testNodeCount" select="$dcPubNodeCount" />
            <xsl:with-param name="elemName" select="'dc:Publisher'" />
        </xsl:call-template>
        <xsl:call-template name="testExistence">
            <xsl:with-param name="testID" select="'opf_DcDatePresent'" />
            <xsl:with-param name="testNodeCount" select="$dcDateNodeCount" />
            <xsl:with-param name="elemName" select="'dc:Date'" />
        </xsl:call-template>
        <xsl:call-template name="testExistence">
            <xsl:with-param name="testID" select="'opf_DcFormatPresent'" />
            <xsl:with-param name="testNodeCount" select="$dcFormatNodeCount" />
            <xsl:with-param name="elemName" select="'dc:Format'" />
        </xsl:call-template>
        <xsl:call-template name="testExistence">
            <xsl:with-param name="testID" select="'opf_DcLangPresent'" />
            <xsl:with-param name="testNodeCount" select="$dcLangNodeCount" />
            <xsl:with-param name="elemName" select="'dc:Language'" />
        </xsl:call-template>

        <xsl:apply-templates />
</xsl:template>

<!-- Test that all dc:Identifier, dc:Title, and dc:Publisher elements have actual values
-->
<xsl:template match="dc:Identifier">
    <xsl:call-template name="testNoValue">
        <xsl:with-param name="testID" select="'opf_DcIdentifierValue'" />
        <xsl:with-param name="testNode" select="." />
        <xsl:with-param name="elemName" select="'dc:Identifier'" />
    </xsl:call-template>
</xsl:template>

<xsl:template match="dc:Title">
    <xsl:call-template name="testNoValue">
        <xsl:with-param name="testID" select="'opf_DcTitleValue'" />
        <xsl:with-param name="testNode" select="." />
        <xsl:with-param name="elemName" select="'dc:Title'" />
    </xsl:call-template>
</xsl:template>

<xsl:template match="dc:Publisher">
    <xsl:call-template name="testNoValue">
        <xsl:with-param name="testID" select="'opf_DcPubValue'" />
        <xsl:with-param name="testNode" select="." />
        <xsl:with-param name="elemName" select="'dc:Publisher'" />
    </xsl:call-template>
</xsl:template>

<xsl:template match="opf:x-metadata">
<!-- x-metadata count tests
    Test for proper number of various x-metadata dtb: items
-->
        <xsl:call-template name="testExactlyOne">
            <xsl:with-param name="testID" select="'opf_xMetaMmTypePresence'" />
            <xsl:with-param name="testNodeCount" select="$xMMediaNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:multimediaType'" />
        </xsl:call-template>
        <xsl:call-template name="testZeroOrOne">
            <xsl:with-param name="testID" select="'opf_xMetaDtbSourceDatePresence'" />
            <xsl:with-param name="testNodeCount" select="$xSrcDateNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:sourceDate'" />
        </xsl:call-template>
        <xsl:call-template name="testZeroOrOne">
            <xsl:with-param name="testID" select="'opf_xMetaDtbSourceEditionPresence'" />
            <xsl:with-param name="testNodeCount" select="$xSrcEditionNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:sourceEdition'" />
        </xsl:call-template>
        <xsl:call-template name="testZeroOrOne">
            <xsl:with-param name="testID" select="'opf_xMetaDtbSourcePublisherPresence'" />
            <xsl:with-param name="testNodeCount" select="$xSrcPubNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:sourcePublisher'" />
        </xsl:call-template>
        <xsl:call-template name="testZeroOrOne">
            <xsl:with-param name="testID" select="'opf_xMetaDtbSourceRightsPresence'" />
            <xsl:with-param name="testNodeCount" select="$xSrcRightsNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:sourceRights'" />
        </xsl:call-template>
        <xsl:call-template name="testZeroOrOne">
            <xsl:with-param name="testID" select="'opf_xMetaDtbSourceTitlePresence'" />
            <xsl:with-param name="testNodeCount" select="$xSrcTitleNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:sourceTitle'" />
        </xsl:call-template>
        <xsl:call-template name="testExactlyOne">
            <xsl:with-param name="testID" select="'opf_xMetaTotTimePresence'" />
            <xsl:with-param name="testNodeCount" select="$xTotTimeNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:totalTime'" />
        </xsl:call-template>
        <xsl:call-template name="testZeroOrOne">
            <xsl:with-param name="testID" select="'opf_xMetaDtbProducedDatePresence'" />
            <xsl:with-param name="testNodeCount" select="$xProdDateNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:producedDate'" />
        </xsl:call-template>
        <xsl:call-template name="testZeroOrOne">
            <xsl:with-param name="testID" select="'opf_xMetaDtbRevisionPresence'" />
            <xsl:with-param name="testNodeCount" select="$xRevNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:revision'" />
        </xsl:call-template>
        <xsl:call-template name="testZeroOrOne">
            <xsl:with-param name="testID" select="'opf_xMetaDtbRevisionDatePresence'" />
            <xsl:with-param name="testNodeCount" select="$xRevDateNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:revisionDate'" />
        </xsl:call-template>
        <xsl:call-template name="testZeroOrOne">
            <xsl:with-param name="testID" select="'opf_xMetaDtbRevisionDescriptionPresence'" />
            <xsl:with-param name="testNodeCount" select="$xRevDescNodeCount" />
            <xsl:with-param name="elemName" select="'dtb:revisionDescription'" />
        </xsl:call-template>

    <xsl:apply-templates />
</xsl:template>

<!-- Manifest tests -->
<xsl:template match="opf:manifest">
<!-- Look for existence of package file -->
    <xsl:call-template name="testExistence">
        <xsl:with-param name="testID" select="'opf_opfInManifest'" />
        <xsl:with-param name="testNodeCount" select="$mOpfNodeCount" />
        <xsl:with-param name="elemName" select="concat('manifest items with name=',$opfName)" />
    </xsl:call-template>

<!-- Look for existence of NCX -->
    <xsl:call-template name="testExistence">
        <xsl:with-param name="testID" select="'opf_mnfNcxId'" />
        <xsl:with-param name="testNodeCount" select="$mNcxNodeCount" />
        <xsl:with-param name="elemName" select="'manifest items with id=ncx'" />
    </xsl:call-template>

<!-- Look for lack of distinfo -->
    <xsl:call-template name="testZero">
        <xsl:with-param name="testID" select="'opf_mnfDistInfo'" />
        <xsl:with-param name="testNodeCount" select="$mDistinfoNodeCount" />
        <xsl:with-param name="elemName" select="'distinfo manifest item(s)'" />
    </xsl:call-template>

<!-- Test uniqueness of item hrefs and check for fragment identifiers -->
    <xsl:for-each select="$sortedManifestItems/*">
        <xsl:variable name="pos" select="position()" />
        <xsl:variable name="currentItem" select="." />
        <!-- If current is same as previous, it's a dupe! -->
        <xsl:if test="$currentItem/@href = $sortedManifestItems/*[$pos - 1]/@href">
            <xsl:element name="testFailure">
                <xsl:attribute name="testRef">opf_mnfHrefUnique</xsl:attribute>
                <xsl:attribute name="line"><xsl:value-of select="$currentItem/@line" /></xsl:attribute>
                <xsl:element name="detail">
                     Duplicate item href: <xsl:value-of select="$currentItem/@href" />
                </xsl:element>
            </xsl:element>
        </xsl:if>
        <xsl:if test="contains($currentItem/@href,'#')">
            <xsl:element name="testFailure">
                <xsl:attribute name="testRef">opf_mnfHrefFrag</xsl:attribute>
                <xsl:attribute name="line"><xsl:value-of select="$currentItem/@line" /></xsl:attribute>
                <xsl:element name="detail">
                     item href contains fragment identifier:  <xsl:value-of select="$currentItem/@href" />
                </xsl:element>
            </xsl:element>
        </xsl:if>
    </xsl:for-each>
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
            <xsl:element name="detail">
                <xsl:choose>
                    <xsl:when test="$testNodeCount = 1">
                        There is 1 instance of <xsl:value-of select="$elemName" />
                    </xsl:when>
                    <xsl:otherwise>
                        There are <xsl:value-of select="$testNodeCount" /> instances of <xsl:value-of select="$elemName" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
        </xsl:element>
    </xsl:if>
</xsl:template>

</xsl:stylesheet>
