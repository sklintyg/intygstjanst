package se.inera.certificate.spec

import org.joda.time.LocalDateTime
import org.springframework.core.io.ClassPathResource
import se.inera.certificate.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.Amnetyp
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswer.rivtab20.v1.SendMedicalCertificateAnswerResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.AnswerToFkType
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerResponseType
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswerresponder.v1.SendMedicalCertificateAnswerType

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource


class SkickaSvar extends WsClientFixture {

    private SendMedicalCertificateAnswerResponderInterface sendResponder

    String vårdReferens
    String fkReferens
    String ämne
    String fråga
    String frågeTidpunkt
    String svar
    String svarsTidpunkt
	String signeringsTidpunkt
	String lakarutlatandeId
	String personnr
	String namn

	static String serviceUrl = System.getProperty("service.sendMedicalCertificateAnswerUrl")

    public SkickaSvar() {
		super()
	}

    public SkickaSvar(String logiskAddress) {
		super(logiskAddress)
    }

    @Override
    public void init() {
		String url = serviceUrl ? serviceUrl : baseUrl + "send-certificate-answer-stub"
		sendResponder = createClient(SendMedicalCertificateAnswerResponderInterface.class, url)
    }

    public String resultat() {
        // read request template from file
        JAXBContext jaxbContext = JAXBContext.newInstance(SendMedicalCertificateAnswerType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        AnswerToFkType answer = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("SendMedicalCertificateAnswer_template.xml").getInputStream()), SendMedicalCertificateAnswerType.class).getValue().getAnswer()
        answer.setAvsantTidpunkt(LocalDateTime.now());
		if (ämne) answer.setAmne(Amnetyp.fromValue(ämne))
        if (vårdReferens) answer.setVardReferensId(vårdReferens);
        if (fkReferens) answer.setFkReferensId(fkReferens);
		if (svar) answer.getSvar().setMeddelandeText(svar)
		if (svarsTidpunkt) answer.getSvar().setSigneringsTidpunkt(LocalDateTime.parse(svarsTidpunkt))
		if (lakarutlatandeId) answer.getLakarutlatande().setLakarutlatandeId(lakarutlatandeId)
		if (signeringsTidpunkt) answer.getLakarutlatande().setSigneringsTidpunkt(LocalDateTime.parse(signeringsTidpunkt))
		if (personnr) answer.getLakarutlatande().getPatient().getPersonId().setExtension(personnr)
		if (namn) answer.getLakarutlatande().getPatient().setFullstandigtNamn(namn)

		SendMedicalCertificateAnswerType parameters = new SendMedicalCertificateAnswerType();
		parameters.setAnswer(answer)

        SendMedicalCertificateAnswerResponseType sendResponse = sendResponder.sendMedicalCertificateAnswer(logicalAddress, parameters);
        resultAsString(sendResponse)
    }
}
