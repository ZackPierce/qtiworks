<?xml version="1.0" encoding="UTF-8"?>
<!--

This stylesheet tidies up the XHTML generated by our rendering
process to add in any required extra stuff for the selected
serialization method (e.g. add in MathPlayer gubbins). It also
does a bit of tidying of the results to make it look nicer and
hence slightly easier to debug.

-->
<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:m="http://www.w3.org/1998/Math/MathML"
  xmlns:xhtml="http://www.w3.org/1999/xhtml"
  xmlns:jqti="http://jqti.qtitools.org"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="xs xhtml m jqti">

  <xsl:param name="serializationMethod" as="xs:string"/>
  <xsl:param name="contentType" as="xs:string"/>
  <xsl:param name="outputMethod" as="xs:string"/>
  <xsl:param name="engineBasePath" as="xs:string"/>

  <!--
  <xsl:param name="mathJaxUrl" select="'http://www2.ph.ed.ac.uk/lib/mathjax/1.1-latest/MathJax.js?config=MML_HTMLorMML-full'" as="xs:string"/>
  -->
  <xsl:param name="mathJaxUrl" select="'http://cdn.mathjax.org/mathjax/1.1-latest/MathJax.js?config=MML_HTMLorMML-full'" as="xs:string"/>
  <xsl:param name="mathJaxConfig" as="xs:string?"/>

  <!-- ************************************************************ -->

  <xsl:template match="xhtml:html" as="element()" mode="serialize">
    <xsl:variable name="containsMathML" select="exists(xhtml:body//m:*)" as="xs:boolean"/>
    <!-- Generate XHTML tree in usual namespace -->
    <xsl:variable name="html" as="element(xhtml:html)">
      <html>
        <xsl:copy-of select="@*"/>
        <xsl:if test="$serializationMethod='IE_MATHPLAYER' and $containsMathML">
          <xsl:namespace name="m" select="'http://www.w3.org/1998/Math/MathML'"/>
        </xsl:if>
        <head>
          <xsl:choose>
            <xsl:when test="$serializationMethod='HTML5_MATHJAX'">
              <!-- Add in HTML5 Content Type meta -->
              <meta charset="UTF-8"/>
            </xsl:when>
            <xsl:otherwise>
              <!-- Add traditional HTML Content Type meta -->
              <meta http-equiv="Content-Type" content="{$contentType}; charset=UTF-8"/>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:if test="$serializationMethod='IE_MATHPLAYER' and $containsMathML">
            <object id="MathPlayer" classid="clsid:32F66A20-7614-11D4-BD11-00104BD3F987"/>
            <xsl:processing-instruction name="import">
              <xsl:text>namespace="m" implementation="#MathPlayer"</xsl:text>
            </xsl:processing-instruction>
          </xsl:if>
          <!-- Pull in <head/> stuff added by other stylesheets -->
          <xsl:for-each select="xhtml:head/*">
            <xsl:copy-of select="."/>
            <xsl:text>&#x0a;</xsl:text>
          </xsl:for-each>
          <!-- Finally pull in MathJax if required -->
          <xsl:if test="$containsMathML and $serializationMethod=('XHTML_MATHJAX','HTML5_MATHJAX')">
            <xsl:if test="string($mathJaxConfig)">
              <script type="text/x-mathjax-config">
                <xsl:value-of select="$mathJaxConfig"/>
              </script>
            </xsl:if>
            <xsl:text>&#x0a;</xsl:text>
            <script type="text/javascript" src="{$mathJaxUrl}"/>
            <xsl:text>&#x0a;</xsl:text>
          </xsl:if>
        </head>
        <xsl:apply-templates select="xhtml:body" mode="serialize"/>
      </html>
    </xsl:variable>
    <!--
    For strict HTML outputs, move HTML elements into no namespace,
    otherwise output tree as-is.
    -->
    <xsl:choose>
      <xsl:when test="$outputMethod='html'">
        <xsl:apply-templates select="$html" mode="no-namespace"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="$html"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="xhtml:*" mode="serialize">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="serialize"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="m:*" mode="serialize">
    <xsl:choose>
      <xsl:when test="$serializationMethod='IE_MATHPLAYER'">
        <xsl:element name="m:{local-name()}">
          <xsl:copy-of select="@*"/>
          <xsl:apply-templates mode="serialize"/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:copy-of select="@*"/>
          <xsl:apply-templates mode="serialize"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*" mode="serialize">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="serialize"/>
    </xsl:copy>
  </xsl:template>

  <xsl:function name="jqti:is-xhtml-block-element" as="xs:boolean">
    <xsl:param name="element" as="node()?"/>
    <xsl:sequence select="boolean($element[self::xhtml:* and local-name()=('p','table','div','tbody','tr','td','form','ul','li')])"/>
  </xsl:function>

  <xsl:template match="m:*/text()" mode="serialize">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="text()" mode="serialize">
    <xsl:variable name="trimmed" as="xs:string">
      <xsl:choose>
        <xsl:when test="normalize-space(.)='' and (jqti:is-xhtml-block-element(following-sibling::node()[1]) or jqti:is-xhtml-block-element(preceding-sibling::node()[1]))">
          <!-- Whitespace Nodes before/after block elements are ignorable -->
          <xsl:sequence select="''"/>
        </xsl:when>
        <xsl:when test="not(preceding-sibling::node()[1])">
          <!-- Strip leading whitespace on first child node, collapse trailing whitespace down -->
          <xsl:sequence select="replace(replace(., '^\s+', ''), '\s+$', ' ')"/>
        </xsl:when>
        <xsl:when test="not(following-sibling::node()[1])">
          <!-- Strip trailing whitespace on last child node, collapse leading whitespace down -->
          <xsl:sequence select="replace(replace(., '\s+$', ''), '^\s+', ' ')"/>
        </xsl:when>
        <xsl:otherwise>
          <!-- Collapse leading and trailing whitespace down -->
          <xsl:sequence select="replace(replace(., '^\s+', ' '), '\s+$', ' ')"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!-- Replace whitespace before and after inline MathML with a non-breaking space, as IE
    sometimes ignores the whitespace otherwise -->
    <xsl:variable name="temp" as="xs:string"
      select="if ($trimmed and preceding-sibling::node()[1][self::m:math and not(@display='block')])
      then concat('&#xa0;', substring($trimmed, 2)) else $trimmed"/>
    <xsl:value-of select="if ($trimmed and following-sibling::node()[1][self::m:math and not(@display='block')])
      then concat(substring($temp, 1, string-length($temp)-1), '&#xa0;') else $temp"/>
  </xsl:template>

  <!-- ************************************************************ -->

  <xsl:template match="xhtml:html" mode="no-namespace">
    <xsl:element name="html" namespace="">
      <xsl:copy-of select="@*"/>
      <!-- (Need to re-copy any explicit xmlns:m added previously) -->
      <xsl:if test="$serializationMethod='IE_MATHPLAYER' and in-scope-prefixes(.)='m'">
        <xsl:namespace name="m" select="'http://www.w3.org/1998/Math/MathML'"/>
      </xsl:if>
      <xsl:apply-templates mode="no-namespace"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="xhtml:*" mode="no-namespace">
    <xsl:element name="{local-name()}" namespace="">
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="no-namespace"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="*" mode="no-namespace">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates mode="no-namespace"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="text()|comment()|processing-instruction()" mode="no-namespace">
    <xsl:copy-of select="."/>
  </xsl:template>

  <!-- ************************************************************ -->

</xsl:stylesheet>
