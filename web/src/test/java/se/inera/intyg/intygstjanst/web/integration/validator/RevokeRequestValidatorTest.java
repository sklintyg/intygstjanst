/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.integration.validator;

import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;
import se.inera.intyg.common.support.validate.CertificateValidationException;

@RunWith(MockitoJUnitRunner.class)
public class RevokeRequestValidatorTest {

    @Test
    public void testValidateAndCorrect() throws Exception {
        new RevokeRequestValidator(createRequest()).validateAndCorrect();
    }

    @Test(expected = CertificateValidationException.class)
    public void testValidateAndCorrectVardReferensIdMissing() throws Exception {
        RevokeType revokeRequest = createRequest();
        revokeRequest.setVardReferensId(null);
        new RevokeRequestValidator(revokeRequest).validateAndCorrect();
    }

    @Test(expected = CertificateValidationException.class)
    public void testValidateAndCorrectAvsantTidpunktMissing() throws Exception {
        RevokeType revokeRequest = createRequest();
        revokeRequest.setAvsantTidpunkt(null);
        new RevokeRequestValidator(revokeRequest).validateAndCorrect();
    }

    @Test(expected = CertificateValidationException.class)
    public void testValidateAndCorrectLakarutlatandeError() throws Exception {
        RevokeType revokeRequest = createRequest();
        revokeRequest.getLakarutlatande().setPatient(null);
        new RevokeRequestValidator(revokeRequest).validateAndCorrect();
    }

    @Test(expected = CertificateValidationException.class)
    public void testValidateAndCorrectAdressVardError() throws Exception {
        RevokeType revokeRequest = createRequest();
        revokeRequest.getAdressVard().setHosPersonal(null);
        new RevokeRequestValidator(revokeRequest).validateAndCorrect();
    }

    protected RevokeType createRequest() throws Exception {
        // read request from file
        JAXBContext jaxbContext = JAXBContext.newInstance(RevokeMedicalCertificateRequestType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        JAXBElement<RevokeMedicalCertificateRequestType> request = unmarshaller.unmarshal(
                new StreamSource(new ClassPathResource("revoke-medical-certificate/revoke-medical-certificate-request.xml").getInputStream()),
                RevokeMedicalCertificateRequestType.class);
        return request.getValue().getRevoke();
    }

}
