package se.inera.certificate.spec
import org.skyscreamer.jsonassert.JSONAssert

import se.inera.certificate.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.getcertificate.v1.rivtabp20.GetCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.getcertificatecontentresponder.v1.GetCertificateContentRequest
import se.inera.ifv.insuranceprocess.healthreporting.getcertificatecontentresponder.v1.GetCertificateContentResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.getcertificatecontentresponder.v1.GetCertificateContentResponderService
import se.inera.ifv.insuranceprocess.healthreporting.getcertificatecontentresponder.v1.GetCertificateContentResponse
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum
import fitnesse.slim.SlimError

/**
 *
 * @author andreaskaltenbach
 */
public class HamtaIntygsinnehall extends WsClientFixture {

    private GetCertificateContentResponderInterface getCertificateContentResponder

	static String serviceUrl = System.getProperty("service.getCertificateContentUrl")
	
    public HamtaIntygsinnehall() {
		String url = serviceUrl ? serviceUrl : baseUrl + "get-certificate-content/v1.0"
		getCertificateContentResponder = createClient(GetCertificateContentResponderInterface.class, url)
    }

    String personnummer
    String intyg
	String förväntatSvar
	private String faktisktSvar
	private String resultat

    GetCertificateContentResponse response

    public void execute() {
		faktisktSvar = null
        GetCertificateContentRequest request = new GetCertificateContentRequest()
        request.setNationalIdentityNumber(personnummer)
        request.setCertificateId(intyg)

        response = getCertificateContentResponder.getCertificateContent(logicalAddress, request)
		switch (response.result.resultCode) {
			case ResultCodeEnum.OK:
				faktisktSvar = asJson(response.certificate)
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
