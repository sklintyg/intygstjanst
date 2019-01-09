/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integration.stub;

import static org.mockito.Mockito.verify;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.intyg.common.support.stub.MedicalCertificatesStore;

@RunWith(MockitoJUnitRunner.class)
public class RevokeMedicalCertificateResponderStubTest {

    private static final String UTLATANDE_ID = "intygs-id-1234567890";

    private static final String REVOKE_MESSAGE = "Meddelande";

    @Mock
    MedicalCertificatesStore store;

    @InjectMocks
    RevokeMedicalCertificateResponderStub stub = new RevokeMedicalCertificateResponderStub();

    @Test
    public void testName() throws Exception {
        // read request from file
        JAXBContext jaxbContext = JAXBContext.newInstance(RevokeMedicalCertificateRequestType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        RevokeMedicalCertificateRequestType request = unmarshaller.unmarshal(
                new StreamSource(new ClassPathResource("revoke-medical-certificate/revoke-medical-certificate-request.xml").getInputStream()),
                RevokeMedicalCertificateRequestType.class).getValue();

        stub.revokeMedicalCertificate(null, request);

        verify(store).makulera(UTLATANDE_ID, REVOKE_MESSAGE);
    }
}
