<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:qti="http://www.imsglobal.org/xsd/imsqti_v2p1"
  xmlns:jqti="http://jqti.qtitools.org"
  xmlns="http://www.w3.org/1999/xhtml"
  exclude-result-prefixes="qti jqti xs">

  <xsl:template match="qti:orderInteraction">
    <input name="jqtipresented_{@responseIdentifier}" type="hidden" value="1"/>
    <div class="{local-name()}">
      <xsl:if test="qti:prompt">
        <div class="prompt">
          <xsl:apply-templates select="qti:prompt"/>
        </div>
      </xsl:if>
      <xsl:if test="jqti:is-invalid-response(@responseIdentifier)">
        <xsl:call-template name="jqti:generic-bad-response-message"/>
      </xsl:if>

      <div id="jqtiresponse_{@responseIdentifier}">
        <!-- Create holder for hidden form fields that will contain the actual data to pass back -->
        <div class="hiddenInputContainer"></div>

        <!-- Filter out the choice identifiers that are visible and split into those which haven't
        been selected and those which have -->
        <xsl:variable name="thisInteraction" select="." as="element(qti:orderInteraction)"/>
        <xsl:variable name="visibleOrderedChoices" as="element(qti:simpleChoice)*" select="jqti:get-visible-ordered-choices(., qti:simpleChoice)"/>
        <xsl:variable name="respondedChoiceIdentifiers" select="jqti:extract-iterable-elements(jqti:get-response-value(@responseIdentifier))" as="xs:string*"/>
        <xsl:variable name="unselectedVisibleChoices" select="$visibleOrderedChoices[not(@identifier = $respondedChoiceIdentifiers)]" as="element(qti:simpleChoice)*"/>
        <xsl:variable name="respondedVisibleChoices" as="element(qti:simpleChoice)*">
          <xsl:for-each select="$respondedChoiceIdentifiers">
            <xsl:sequence select="$thisInteraction/qti:simpleChoice[@identifier=current() and jqti:is-visible(.)]"/>
          </xsl:for-each>
        </xsl:variable>

        <!-- Now generate selection widget -->
        <xsl:variable name="orientation" select="if (@orientation) then @orientation else 'horizontal'" as="xs:string"/>
        <div class="source box {$orientation}">
          <span class="info">Drag unused items from here...</span>
          <ul class="{$orientation}">
            <xsl:apply-templates select="$unselectedVisibleChoices"/>
          </ul>
          <br/>
        </div>
        <div class="target box {$orientation}">
          <span class="info">Drop and order your selected items here...</span>
          <ul class="{$orientation}">
            <xsl:apply-templates select="$respondedVisibleChoices"/>
          </ul>
          <br/>
        </div>
        <br/>

        <!-- Register with JavaScript -->
        <script type="text/javascript">
          $(document).ready(function() {
            JQTIItemRendering.registerOrderInteraction('<xsl:value-of
              select="@responseIdentifier"/>', [<xsl:value-of
              select="jqti:to-javascript-arguments(for $s in $unselectedVisibleChoices return $s/@identifier)"/>], [<xsl:value-of
              select="jqti:to-javascript-arguments(for $s in $respondedVisibleChoices return $s/@identifier)"/>], <xsl:value-of
              select="if (@minChoices) then @minChoices else 'null'"/>, <xsl:value-of
              select="if (@maxChoices) then @maxChoices else 'null'"/>);
          });
        </script>
      </div>
    </div>
  </xsl:template>

  <xsl:template match="qti:orderInteraction/qti:simpleChoice">
    <li id="jqtiresponse_{@identifier}" class="ui-state-default">
      <span class="ui-icon ui-icon-arrowthick2-n-s"></span>
      <xsl:apply-templates/>
    </li>
  </xsl:template>

</xsl:stylesheet>
