package se.inera.intyg.common.specifications.spec
import se.inera.intyg.common.specifications.spec.util.WsClientFixtureNyaKontraktet
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateResponderInterface
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateResponseType
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.GetRecipientsForCertificateType
import se.inera.intyg.clinicalprocess.healthcond.certificate.getrecipientsforcertificate.v1.RecipientType
import se.riv.clinicalprocess.healthcond.certificate.v1.ResultCodeType

public class HamtaMottagare extends WsClientFixtureNyaKontraktet {

    private GetRecipientsForCertificateResponderInterface getRecipientsForCertificateResponder

	static String serviceUrl = System.getProperty("service.getRecipientsForCertificateUrl")

    public HamtaMottagare() {
		String url = serviceUrl ? serviceUrl : baseUrl + "get-recipients-for-certificate/v1.0"
		getRecipientsForCertificateResponder = createClient(GetRecipientsForCertificateResponderInterface.class, url)
    }

    String intygsTyp
    
    private String resultat
    private List<String> mottagare
    private GetRecipientsForCertificateResponseType response

	public void reset() {
		response = null
		resultat = null
		mottagare = null
	}

    public void execute() {
        GetRecipientsForCertificateType request = new GetRecipientsForCertificateType()
        request.certificateType = intygsTyp

        response = getRecipientsForCertificateResponder.getRecipientsForCertificate(logicalAddress.value, request)
            switch (response.result.resultCode) {
                case ResultCodeType.OK:
	            List<RecipientType> recipients = response.recipient
                mottagare = recipients.collect() { recipient -> "${recipient.id}-${recipient.name}" }
				resultat = resultAsString(response)
				break
            default:
				resultat = resultAsString(response)
		}
    }

    public List<String> mottagare() {
        return mottagare
    }

    public String resultat() {
        return resultat
    }

}
