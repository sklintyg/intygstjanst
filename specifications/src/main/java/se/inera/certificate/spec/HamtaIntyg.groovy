package se.inera.certificate.spec

import org.skyscreamer.jsonassert.JSONAssert
import se.inera.certificate.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.getcertificate.rivtab20.v1.GetCertificateResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.getcertificateresponder.v1.CertificateType
import se.inera.ifv.insuranceprocess.healthreporting.getcertificateresponder.v1.GetCertificateRequestType
import se.inera.ifv.insuranceprocess.healthreporting.getcertificateresponder.v1.GetCertificateResponseType
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum

import javax.xml.bind.JAXBContext

/**
 *
 * @author andreaskaltenbach
 */
public class HamtaIntyg extends WsClientFixture {

    private GetCertificateResponderInterface getCertificateResponder

	static String serviceUrl = System.getProperty("service.getCertificateUrl")

    public HamtaIntyg() {
		String url = serviceUrl ? serviceUrl : baseUrl + "get-certificate/v1.0"
		getCertificateResponder = createClient(GetCertificateResponderInterface.class, url)
    }

    String personnummer
    String intyg
	String förväntatSvar
	private String faktisktSvar
	private String resultat
    private def status

    private GetCertificateResponseType response

	public void reset() {
		response = null
		resultat = null
		förväntatSvar = null
		faktisktSvar = null
        status = null
	}

    public void execute() {
        GetCertificateRequestType request = new GetCertificateRequestType()
        request.setNationalIdentityNumber(personnummer)
        request.setCertificateId(intyg)

        response = getCertificateResponder.getCertificate(logicalAddress, request)
        switch (response.result.resultCode) {
            case ResultCodeEnum.OK:
	            CertificateType certificate = response.certificate
				JAXBContext payloadContext = JAXBContext.newInstance(RegisterMedicalCertificateType.class);
				org.w3c.dom.Node node = (org.w3c.dom.Node) certificate.any[0]
				faktisktSvar = asJson(payloadContext.createUnmarshaller().unmarshal(node).value)
                // Collect status type and sort alphabetically
                status = response.meta.status.collect{it.type.toString()}.sort()
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
