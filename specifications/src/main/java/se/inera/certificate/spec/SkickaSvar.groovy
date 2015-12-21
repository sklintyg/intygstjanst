/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.common.specifications.spec

import org.joda.time.LocalDateTime
import org.springframework.core.io.ClassPathResource
import se.inera.intyg.common.specifications.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.Amnetyp
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateanswer.rivtabp20.v1.SendMedicalCertificateAnswerResponderInterface
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
