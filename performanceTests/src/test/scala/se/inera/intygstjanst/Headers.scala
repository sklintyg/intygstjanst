package se.inera.intygstjanst

object Headers {

  val acceptJson = """application/json, text/plain, */*"""
  val acceptHtml = """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"""

  val default = Map(
    "Accept" -> acceptHtml)

  val form_urlencoded = Map(
    "Accept" -> acceptHtml,
    "Content-Type" -> """application/x-www-form-urlencoded""",
    "Pragma" -> """no-cache""")

  val json = Map(
    "Accept" -> acceptJson,
    "Content-Type" -> """application/json;charset=UTF-8""")

  val list_certificates = soap( """urn:riv:insuranceprocess:healthreporting:ListCertificatesResponder:1:ListCertificates""")

  val list_active_sick_leaves = soap( """urn:riv:clinicalprocess:healthcond:rehabilitation:ListActiveSickLeavesForCareUnitResponder:1:ListActiveSickLeavesForCareUnit""")

  val set_consent = soap( """urn:riv:insuranceprocess:healthreporting:SetConsentResponder:1:SetConsent""")

  val store_certificate = soap( """urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:1""")

  val get_certificate = soap( """urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1:GetCertificate""")

  val send_medical_certificate = soap( """urn:riv:insuranceprocess:healthreporting:SendMedicalCertificateResponder:1:SendMedicalCertificate""")

  val register_certificate = soap( """urn:riv:clinicalprocess:healthcond:certificate:RegisterCertificateResponder:3:RegisterCertificate"""")

  val list_sick_leaves = soap( """urn:riv:clinicalprocess:healthcond:certificate:ListSickLeavesForCareResponder:1:ListSickLeavesForCare""")

  def soap(action: String): Map[String, String] = {
    return Map (
    "Accept" -> acceptJson,
    "Content-Type" -> """text/xml;charset=UTF-8""",
    "SOAPAction" -> action)
  }

}
