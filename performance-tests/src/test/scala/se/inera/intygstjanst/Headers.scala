package se.inera.intygstjanst

object Headers {

  val default = Map(
    "Accept" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""")

  val form_urlencoded = Map(
    "Accept" -> """text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8""",
    "Content-Type" -> """application/x-www-form-urlencoded""",
    "Pragma" -> """no-cache""")

  val json = Map(
    "Accept" -> """application/json, text/plain, */*""",
    "Content-Type" -> """application/json;charset=UTF-8""")

  val list_certificates = Map(
    "Accept" -> """application/json, text/plain, */*""",
    "Content-Type" -> """text/xml;charset=UTF-8""",
    "SOAPAction" -> "urn:riv:insuranceprocess:healthreporting:ListCertificatesResponder:1:ListCertificates")

  val set_consent = Map(
    "Accept" -> """application/json, text/plain, */*""",
    "Content-Type" -> """text/xml;charset=UTF-8""",
    "SOAPAction" -> "urn:riv:insuranceprocess:healthreporting:SetConsentResponder:1:SetConsent")

  val store_certificate = Map(
    "Accept" -> """application/json, text/plain, */*""",
    "Content-Type" -> """text/xml;charset=UTF-8""",
    "SOAPAction" -> "urn:riv:insuranceprocess:healthreporting:RegisterMedicalCertificateResponder:1")

  val get_certificate = Map(
            "Accept" -> """application/json, text/plain, */*""",
            "Content-Type" -> """text/xml;charset=UTF-8""",
            "SOAPAction" -> "urn:riv:insuranceprocess:healthreporting:GetCertificateResponder:1:GetCertificate")

  val send_medical_certificate = Map(
          "Accept" -> """application/json, text/plain, */*""",
          "Content-Type" -> """text/xml;charset=UTF-8""",
          "SOAPAction" -> "urn:riv:insuranceprocess:healthreporting:SendMedicalCertificateResponder:1:SendMedicalCertificate")

}