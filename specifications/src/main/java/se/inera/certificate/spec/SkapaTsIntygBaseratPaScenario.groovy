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

import org.springframework.core.io.ClassPathResource
import se.inera.intyg.common.specifications.spec.util.WsClientFixtureNyaKontraktet
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponderInterface
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateResponseType
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v1.RegisterCertificateType

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource


public class SkapaTsIntygBaseratPaScenario extends WsClientFixtureNyaKontraktet {

	static String serviceUrl = System.getProperty("service.clinicalProcess.registerCertificateUrl")
	static JAXBContext jaxbContext = JAXBContext.newInstance(RegisterCertificateType.class)
	static Unmarshaller unmarshaller = jaxbContext.createUnmarshaller()
	UUID uid = UUID.fromString("8d796df0-0bad-40bd-88ab-0d0e75952c7e")

	String mall
    String typ
    String personnummer
	String antalIntyg

	def responses = []

	// Used to iterate through list of personnummer
	int index = 0

    RegisterCertificateResponseType response
    RegisterCertificateResponderInterface registerCertificateResponder

	public SkapaTsIntygBaseratPaScenario() {
		super()
	}

	public SkapaTsIntygBaseratPaScenario(String logiskAddress) {
		super(logiskAddress)
    }

    @Override
    public void init() {
		String url = serviceUrl ? serviceUrl : baseUrl + "register-certificate/v1.0"
        registerCertificateResponder = createClient(RegisterCertificateResponderInterface.class, url)
	}

	public void execute() {

		for (int i = 0; i < Integer.parseInt(antalIntyg); i++) {
			RegisterCertificateType request = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("grundladda/" + typ + "/" + mall).getInputStream()), RegisterCertificateType.class).getValue()

			request.utlatande.patient.personId.extension = personnummer
			request.utlatande.utlatandeId.extension = uid.randomUUID()
			response = registerCertificateResponder.registerCertificate(logicalAddress.toString(), request)

			// Put the resultAsString in a list so we can check it all went okay
			responses << resultAsString(response)
		}
	}

	public String resultat() {
		return (responses.count("OK") > 0) ? "OK" : "FAILED"
	}
}
