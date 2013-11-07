package se.inera.certificate.spec
import org.skyscreamer.jsonassert.JSONAssert
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareRequestType
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponderInterface
import se.inera.certificate.clinicalprocess.healthcond.certificate.getcertificateforcare.v1.GetCertificateForCareResponseType
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.UtlatandeType
import se.inera.certificate.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum
/**
 *
 * @author andreaskaltenbach
 */
public class JournalsystemHamtarIntyg extends WsClientFixture {

    private GetCertificateForCareResponderInterface responder

	static String serviceUrl = System.getProperty("service.clinicalProcess.getCertificateUrl")

    public JournalsystemHamtarIntyg() {
		String url = serviceUrl ? serviceUrl : baseUrl + "get-certificate-for-care/v1.0"
		responder = createClient(GetCertificateForCareResponderInterface.class, url)
    }

    String intyg
	String förväntatSvar
	private String faktisktSvar
	private String resultat
    private def status
	
    private GetCertificateForCareResponseType response

	public void reset() {
		response = null
		resultat = null
		förväntatSvar = null
		faktisktSvar = null
        status = null
	}
	
    public void execute() {
        GetCertificateForCareRequestType request = new GetCertificateForCareRequestType()
        request.certificateId = intyg

        response = responder.getCertificateForCare(logicalAddress, request)
        switch (response.result.resultCode) {
            case ResultCodeEnum.OK:
                UtlatandeType utlatande = response.certificate
				faktisktSvar = asJson(utlatande)
                // Sort status by timestamp (descending), and collect the type
                status = response.meta.status.sort{a, b -> a.timestamp < b.timestamp ? 1 : -1}.collect{it.type.toString()}
				resultat = "OK"
				break
            case ResultCodeEnum.INFO:
                resultat = "[${response.result.resultCode.toString()}] - ${response.result.infoText}"
				break
            default:
				resultat = "[${response.result.resultCode.toString()}] - ${response.result.errorText}"
		} 
    }

    public String resultat() {
        if (response.result.resultCode == ResultCodeEnum.OK && förväntatSvar) {
            try {  
				JSONAssert.assertEquals(förväntatSvar, faktisktSvar, false)
				return "OK"
			} catch (AssertionError e) {
				asErrorMessage(e.message)
			}
		} else {
			resultat
		}
    }

	public String svar() {
		faktisktSvar
	}

    public String status() {
        status
    }

}
