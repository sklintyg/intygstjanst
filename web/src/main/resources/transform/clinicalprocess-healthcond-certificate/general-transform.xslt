<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                xmlns:c="urn:riv:clinicalprocess:healthcond:certificate:1">

  <!-- Copy all XML nodes, if no more specific template matches. -->
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Transform <soap:Fault> element into a proper WS response. -->
  <xsl:template match="soap:Fault">
    <!--
      Here we call the 'abstract' template named response. Has to be provided by
      all XSLTs which include this one.
      -->
    <xsl:call-template name="response"/>
  </xsl:template>

  <!-- Transform <faultcode> and <faultstring> elements to <resultCode> and <resultText> -->
  <xsl:template name="result">
    <c:resultCode>ERROR</c:resultCode>
    <c:resultText>
      <xsl:value-of select="faultstring/text()"/>
    </c:resultText>
  </xsl:template>

</xsl:stylesheet>