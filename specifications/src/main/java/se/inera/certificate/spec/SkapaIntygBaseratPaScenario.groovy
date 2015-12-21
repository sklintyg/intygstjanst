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
import se.inera.intyg.common.specifications.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificate.rivtabp20.v3.RegisterMedicalCertificateResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateResponseType
import se.inera.ifv.insuranceprocess.healthreporting.registermedicalcertificateresponder.v3.RegisterMedicalCertificateType

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource


public class SkapaIntygBaseratPaScenario extends WsClientFixture {

	static String serviceUrl = System.getProperty("service.registerMedicalCertificateUrl")
	static JAXBContext jaxbContext = JAXBContext.newInstance(RegisterMedicalCertificateType.class)
	static Unmarshaller unmarshaller = jaxbContext.createUnmarshaller()
	UUID uid = UUID.fromString("8d796df0-0bad-40bd-88ab-0d0e75952c7e")

	String mall
	String antalIntyg
	File personFile

	def personnummer = []
	def responses = []

	// Used to iterate through list of personnummer
	int index = 0

	RegisterMedicalCertificateResponseType response
	RegisterMedicalCertificateResponderInterface registerMedicalCertificateResponder

	public SkapaIntygBaseratPaScenario() {
		super()
	}

	public SkapaIntygBaseratPaScenario(String logiskAddress) {
		super(logiskAddress)
    }

    @Override
    public void init() {
		String url = serviceUrl ? serviceUrl : baseUrl + "register-certificate/v3.0"
		registerMedicalCertificateResponder = createClient(RegisterMedicalCertificateResponderInterface.class, url)
		personFile = new ClassPathResource(System.getProperty("persondata")).getFile()
		personFile.eachLine { line -> personnummer << line }
	}

	/**
	 * Go through the list of personnummer,
	 * when the end is reached start over from the beginning.
	 *
	 * @return a personnummer as a String
	 */
	private String getNextPersonnummer() {
		String ret
		if (personnummer[index] != null) {
			ret = personnummer[index]
			index++
		}
		else {
			index = 0
			ret = personnummer[index]
		}
		return ret
	}

	public void execute() {

		for (int i = 0; i < Integer.parseInt(antalIntyg); i++) {
			RegisterMedicalCertificateType request = unmarshaller.unmarshal(new StreamSource(new ClassPathResource("grundladda/templates/" + mall).getInputStream()), RegisterMedicalCertificateType.class).getValue()

			request.lakarutlatande.patient.personId.extension = getNextPersonnummer()
			request.lakarutlatande.lakarutlatandeId = uid.randomUUID()
			response = registerMedicalCertificateResponder.registerMedicalCertificate(logicalAddress, request)

			// Put the resultAsString in a list so we can check it all went okay
			responses << resultAsString(response)
		}
	}

	public String resultat() {
		return (responses.count("OK") != 0) ? "OK" : "FAILED"
	}
}
