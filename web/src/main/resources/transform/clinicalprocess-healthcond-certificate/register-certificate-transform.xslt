<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rmc="urn:riv:clinicalprocess:healthcond:certificate:RegisterCertificateResponder:1">

  <xsl:include href="transform/clinicalprocess-healthcond-certificate/general-transform.xslt"/>

  <xsl:template name="response">
     <rmc:RegisterCertificateResponse>
       <rmc:result>
         <xsl:call-template name="result"/>
       </rmc:result>
     </rmc:RegisterCertificateResponse>
   </xsl:template>

</xsl:stylesheet>