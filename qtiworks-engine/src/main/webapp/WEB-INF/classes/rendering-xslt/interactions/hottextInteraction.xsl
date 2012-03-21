<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:jqti="http://jqti.qtitools.org"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti jqti xs">

  <xsl:template match="qti:hottextInteraction">
    <input name="jqtipresented_{@responseIdentifier}" type="hidden" value="1"/>
    <div class="{local-name()}">
      <xsl:if test="qti:prompt">
        <div class="prompt">
          <xsl:apply-templates select="qti:prompt"/>
        </div>
      </xsl:if>
      <xsl:if test="jqti:is-invalid-response(@responseIdentifier)">
        <div class="badResponse">
          You must select
          <xsl:if test="@minChoices &gt; 0">
            at least <xsl:value-of select="@minChoices"/>
            <xsl:if test="@maxChoices &gt; 0"> and </xsl:if>
          </xsl:if>
          <xsl:if test="@maxChoices &gt; 0">
            at most <xsl:value-of select="@maxChoices"/>
          </xsl:if>
          options.
        </div>
      </xsl:if>
      <xsl:apply-templates/>
    </div>
  </xsl:template>

  <xsl:template match="qti:hottext">
    <xsl:if test="jqti:is-visible(.)">
      <xsl:variable name="hottextInteraction" select="ancestor::qti:hottextInteraction" as="element(qti:hottextInteraction)"/>
      <xsl:variable name="responseIdentifier" select="$hottextInteraction/@responseIdentifier" as="xs:string"/>
      <span class="hottext">
        <input type="{if ($hottextInteraction/@maxChoices=1) then 'radio' else 'checkbox'}"
             name="jqtiresponse_{$responseIdentifier}"
             value="{@identifier}">
           <xsl:if test="jqti:value-contains(jqti:get-response-value($responseIdentifier), @identifier)">
             <xsl:attribute name="checked" select="'checked'"/>
          </xsl:if>
        </input>
        <xsl:apply-templates/>
      </span>
    </xsl:if>
  </xsl:template>
</xsl:stylesheet>
