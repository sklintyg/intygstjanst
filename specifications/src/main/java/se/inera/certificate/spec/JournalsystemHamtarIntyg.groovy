package se.inera.intyg.common.specifications.spec

import org.skyscreamer.jsonassert.JSONAssert
import se.inera.intyg.intygstyper.fk7263.model.converter.TransportToInternal
import se.inera.intyg.common.specifications.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.mu7263.v3.LakarutlatandeType
import se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum
import se.inera.intyg.clinicalprocess.healthcond.certificate.getmedicalcertificateforcare.v1.GetMedicalCertificateForCareRequestType
import se.inera.intyg.clinicalprocess.healthcond.certificate.getmedicalcertificateforcare.v1.GetMedicalCertificateForCareResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.certificate.getmedicalcertificateforcare.v1.GetMedicalCertificateForCareResponseType
import se.riv.clinicalprocess.healthcond.certificate.v1.ErrorIdType
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType

/**
 *
 * @author andreaskaltenbach
 */
public class JournalsystemHamtarIntyg extends WsClientFixture {

    private GetMedicalCertificateForCareResponderInterface responder

	static String serviceUrl = System.getProperty("service.clinicalProcess.getMedicalCertificateForCareUrl")

    public JournalsystemHamtarIntyg() {
        super()
    }

    public JournalsystemHamtarIntyg(String address) {
        super(address)
    }

    @Override
    public void init() {
		String url = serviceUrl ? serviceUrl : baseUrl + "get-medical-certificate-for-care/v1.0"
		responder = createClient(GetMedicalCertificateForCareResponderInterface.class, url)
    }

    String intyg
    String personnummer
	String förväntatSvar
	private String faktisktSvar
	private String resultat
    private def status
	
    private GetMedicalCertificateForCareResponseType response

	public void reset() {
		response = null
		resultat = null
		förväntatSvar = null
		faktisktSvar = null
        status = null
	}
	
    public void execute() {
        GetMedicalCertificateForCareRequestType request = new GetMedicalCertificateForCareRequestType()
        request.certificateId = intyg
        request.nationalIdentityNumber = personnummer

        response = responder.getMedicalCertificateForCare(logicalAddress.value, request)
        switch (response.result.resultCode) {
            case ResultCodeType.OK:
                LakarutlatandeType utlatande = response.lakarutlatande
				faktisktSvar = asJson(TransportToInternal.convert(utlatande))
                // Sort status by timestamp (descending), and collect the type
                status = response.meta.status.sort{a, b -> a.timestamp < b.timestamp ? 1 : -1}.collect{it.type.toString()}
				resultat = "OK"
				break
            case ResultCodeType.INFO:
                resultat = "[${response.result.resultCode.toString()}] - ${response.result.resultText}"
                break
            case ResultCodeType.ERROR:
                resultat = "[${response.result.errorId.toString()}] - ${response.result.resultText}"
                if (response.result.errorId == ErrorIdType.REVOKED) {
                    LakarutlatandeType utlatande = response.lakarutlatande
                    faktisktSvar = asJson(TransportToInternal.convert(utlatande))
                }
                break
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
            if (response.result.errorId == ErrorIdType.REVOKED) {
                try {
                    JSONAssert.assertEquals(förväntatSvar, faktisktSvar, false)
                    resultat
                } catch (AssertionError e) {
                    asErrorMessage(e.message)
                }
            } else {
			    resultat
            }
		}
    }

	public String svar() {
		faktisktSvar
	}

    public String status() {
        status
    }

}
