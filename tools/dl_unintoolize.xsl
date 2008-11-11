<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0">
  <xsl:output  method="xml"
    indent="yes"
    omit-xml-declaration="no"/>

  <xsl:template  match="*">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

<xsl:template match="comment()|text()">
  <xsl:copy/>
</xsl:template>

  <xsl:template  match="*[substring(name(),1,1)='_']">
    <xsl:element  name="{substring(name(),2)}">
<!-- We could add its translate rules here...
      <xsl:attribute name="its:translate">yes</xsl:attribute>
-->

      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet> 