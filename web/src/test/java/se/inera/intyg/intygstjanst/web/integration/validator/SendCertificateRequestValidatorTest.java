/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

import iso.v21090.dt.v1.II;
import java.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.VardAdresseringsType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.EnhetType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.HosPersonalType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.VardgivareType;
import se.inera.intyg.common.support.validate.CertificateValidationException;

@RunWith(MockitoJUnitRunner.class)
public class SendCertificateRequestValidatorTest {

    @Test
    public void testValidateAndCorrect() throws Exception {
        new SendCertificateRequestValidator(createRequest()).validateAndCorrect();
    }

    @Test(expected = CertificateValidationException.class)
    public void testValidateAndCorrectVardReferensIdMissing() throws Exception {
        SendType sendRequest = createRequest();
        sendRequest.setVardReferensId(null);
        new SendCertificateRequestValidator(sendRequest).validateAndCorrect();
    }

    @Test(expected = CertificateValidationException.class)
    public void testValidateAndCorrectAvsantTidpunktMissing() throws Exception {
        SendType sendRequest = createRequest();
        sendRequest.setAvsantTidpunkt(null);
        new SendCertificateRequestValidator(sendRequest).validateAndCorrect();
    }

    @Test(expected = CertificateValidationException.class)
    public void testValidateAndCorrectLakarutlatandeError() throws Exception {
        SendType sendRequest = createRequest();
        sendRequest.getLakarutlatande().setPatient(null);
        new SendCertificateRequestValidator(sendRequest).validateAndCorrect();
    }

    @Test(expected = CertificateValidationException.class)
    public void testValidateAndCorrectAdressVardError() throws Exception {
        SendType sendRequest = createRequest();
        sendRequest.getAdressVard().setHosPersonal(null);
        new SendCertificateRequestValidator(sendRequest).validateAndCorrect();
    }

    private SendType createRequest() {
        final String hsaIdRoot = "1.2.752.129.2.1.4.1";
        SendType sendType = new SendType();
        VardAdresseringsType vardAdresseringsType = new VardAdresseringsType();
        HosPersonalType hosPersonal = new HosPersonalType();
        EnhetType enhet = new EnhetType();
        enhet.setEnhetsnamn("enhetsnamn");
        II enhetsId = new II();
        enhetsId.setRoot(hsaIdRoot);
        enhetsId.setExtension("enhetsid");
        enhet.setEnhetsId(enhetsId);
        VardgivareType vardGivare = new VardgivareType();
        II vardGivarId = new II();
        vardGivarId.setRoot(hsaIdRoot);
        vardGivarId.setExtension("vardgivarid");
        vardGivare.setVardgivareId(vardGivarId);
        vardGivare.setVardgivarnamn("MI");
        enhet.setVardgivare(vardGivare);
        hosPersonal.setEnhet(enhet);
        hosPersonal.setFullstandigtNamn("MI");
        II personalId = new II();
        personalId.setRoot(hsaIdRoot);
        personalId.setExtension("MI");
        hosPersonal.setPersonalId(personalId);
        hosPersonal.setFullstandigtNamn("hospersonal namn");
        vardAdresseringsType.setHosPersonal(hosPersonal);
        sendType.setAdressVard(vardAdresseringsType);
        sendType.setAvsantTidpunkt(LocalDateTime.now());
        sendType.setVardReferensId("MI");
        LakarutlatandeEnkelType lakarutlatande = new LakarutlatandeEnkelType();
        lakarutlatande.setLakarutlatandeId("certificateId");
        lakarutlatande.setSigneringsTidpunkt(LocalDateTime.now());
        PatientType patient = new PatientType();
        II patientIdHolder = new II();
        patientIdHolder.setRoot("1.2.752.129.2.1.3.1");
        patientIdHolder.setExtension("19121212-1212");
        patient.setPersonId(patientIdHolder);
        patient.setFullstandigtNamn("patientnamn");
        lakarutlatande.setPatient(patient);

        sendType.setLakarutlatande(lakarutlatande);
        return sendType;
    }
}
