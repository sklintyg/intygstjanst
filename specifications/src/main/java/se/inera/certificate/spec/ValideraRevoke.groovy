



/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import se.inera.intyg.common.specifications.spec.util.FitnesseHelper
import se.inera.intyg.common.specifications.spec.util.WsClientFixture
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificate.rivtabp20.v1.RevokeMedicalCertificateResponderInterface
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateResponseType

import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.transform.stream.StreamSource


/**
 *
 * @author andreaskaltenbach
 */
class ValideraRevoke extends WsClientFixture {

    private RevokeMedicalCertificateResponderInterface revokeResponder

    static String serviceUrl = System.getProperty("service.revokeCertificateUrl")

    String filnamn
    RevokeMedicalCertificateResponseType response

    public ValideraRevoke() {
        super()
    }

    public ValideraRevoke(String logiskAddress) {
        super(logiskAddress)
    }

    @Override
    public void init() {
        String url = serviceUrl ? serviceUrl : baseUrl + "revoke-certificate/v1.0"
        revokeResponder = createClient(RevokeMedicalCertificateResponderInterface.class, url)
    }

    public void execute() {
        // read request template from file
        JAXBContext jaxbContext = JAXBContext.newInstance(RevokeMedicalCertificateRequestType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        RevokeMedicalCertificateRequestType request = unmarshaller.unmarshal(new StreamSource(new FileInputStream (FitnesseHelper.getFile(filnamn))),
                                                                        RevokeMedicalCertificateRequestType.class).getValue()

        response = revokeResponder.revokeMedicalCertificate(logicalAddress, request);
    }

    public String resultat() {
        resultAsString(response)
    }
}
