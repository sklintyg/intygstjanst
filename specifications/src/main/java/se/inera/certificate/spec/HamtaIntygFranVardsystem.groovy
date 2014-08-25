package se.inera.certificate.spec
import org.skyscreamer.jsonassert.JSONAssert

import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareRequestType
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponderInterface
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ErrorIdType
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.ResultCodeType
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType
import se.inera.certificate.spec.util.WsClientFixtureNyaKontraktet
/**
 *
 * @author andreaskaltenbach
 */
public class HamtaIntygFranVardsystem extends WsClientFixtureNyaKontraktet {

    private GetCertificateForCareResponderInterface getCertificateForCareResponder

	static String serviceUrl = System.getProperty("service.getCertificateForCareUrl")
	
    public HamtaIntygFranVardsystem() {
		String url = serviceUrl ? serviceUrl : baseUrl + "get-certificate-for-care/v1.0"
		getCertificateForCareResponder = createClient(GetCertificateForCareResponderInterface.class, url)
    }

    String intyg
	String förväntatSvar
	private String faktisktSvar
	private String resultat

    GetCertificateForCareResponseType response

    public void execute() {
		faktisktSvar = null
        GetCertificateForCareRequestType request = new GetCertificateForCareRequestType()
        request.setCertificateId(intyg)

        response = getCertificateForCareResponder.getCertificateForCare("", request)
		switch (response.result.resultCode) {
			case ResultCodeType.OK:
				// faktisktSvar = response.certificate
                UtlatandeType utlatande = response.certificate
                faktisktSvar = asJson(utlatande)
				resultat = "OK"
				break
			case ResultCodeType.INFO:
				resultat = "[${response.result.resultCode.toString()}] - ${response.result.resultText}"
				break
			default:
				resultat = "[${response.result.errorId.toString()}] - ${response.result.resultText}"
                if (response.result.errorId == ErrorIdType.REVOKED) {
                    UtlatandeType utlatande = response.certificate
                    faktisktSvar = asJson(utlatande)
                }
		}

    }

    public String resultat() {
        if (response.result.resultCode == ResultCodeType.OK && förväntatSvar) {
            try {
				JSONAssert.assertEquals(förväntatSvar, faktisktSvar, false)
				return "OK"
			} catch (AssertionError e) {
				asErrorMessage(e.message)
			}
		}
		else resultat
    }

	public String svar() {
		faktisktSvar
	}

    public String status() {
        response.meta.status.collect{it.type.toString()}
    }

}
