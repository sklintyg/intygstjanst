package se.inera.certificate.spec

import org.joda.time.LocalDateTime
import org.springframework.core.io.ClassPathResource
import se.inera.certificate.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestion.rivtabp20.v1.SendMedicalCertificateQuestionResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.QuestionToFkType
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionResponseType
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificatequestionresponder.v1.SendMedicalCertificateQuestionType

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource


class SkickaFraga extends WsClientFixture {

    private SendMedicalCertificateQuestionResponderInterface sendResponder

    String vårdReferens
    String ämne
    String fråga
    String frågeTidpunkt
	String signeringsTidpunkt
	String lakarutlatandeId
	String personnr
	String namn

	static String serviceUrl = System.getProperty("service.sendMedicalCertificateQuestionUrl")

	public SkickaFraga() {
		super()
	}

    public SkickaFraga(String logiskAddress) {
		super(logiskAddress)
    }

    @Override
    public void init() {
		String url = serviceUrl ? serviceUrl : baseUrl + "send-certificate-question-stub"
		sendResponder = createClient(SendMedicalCertificateQuestionResponderInterface.class, url)
    }

    public String resultat() {
        // read request template from file
        JAXBContext jaxbContext = JAXBContext.newInstance(SendMedicalCertificateQuestionType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        QuestionToFkType question = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("SendMedicalCertificateQuestion_template.xml").getInputStream()), SendMedicalCertificateQuestionType.class).getValue().getQuestion()
        question.setAvsantTidpunkt(LocalDateTime.now());
		if (ämne) question.setAmne(Amnetyp.fromValue(ämne))
        if (vårdReferens) question.setVardReferensId(vårdReferens);
        if (fråga) question.getFraga().setMeddelandeText(fråga);
        if (frågeTidpunkt) question.getFraga().setSigneringsTidpunkt(LocalDateTime.parse(frågeTidpunkt));
		if (lakarutlatandeId) question.getLakarutlatande().setLakarutlatandeId(lakarutlatandeId)
		if (signeringsTidpunkt) question.getLakarutlatande().setSigneringsTidpunkt(LocalDateTime.parse(signeringsTidpunkt))
		if (personnr) question.getLakarutlatande().getPatient().getPersonId().setExtension(personnr)
		if (namn) question.getLakarutlatande().getPatient().setFullstandigtNamn(namn)

        SendMedicalCertificateQuestionType parameters = new SendMedicalCertificateQuestionType();
        parameters.setQuestion(question);

        SendMedicalCertificateQuestionResponseType sendResponse = sendResponder.sendMedicalCertificateQuestion(logicalAddress, parameters);
        resultAsString(sendResponse)
    }
}
