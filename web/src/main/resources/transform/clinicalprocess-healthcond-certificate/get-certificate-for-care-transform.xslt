<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:gcr="urn:riv:clinicalprocess:healthcond:certificate:GetCertificateForCareResponder:1">

  <xsl:include href="transform/clinicalprocess-healthcond-certificate/general-transform.xslt"/>

  <xsl:template name="response">
    <gcr:GetCertificateForCareResponse>
      <gcr:result>
        <xsl:call-template name="result"/>
      </gcr:result>
    </gcr:GetCertificateForCareResponse>
  </xsl:template>

</xsl:stylesheet>