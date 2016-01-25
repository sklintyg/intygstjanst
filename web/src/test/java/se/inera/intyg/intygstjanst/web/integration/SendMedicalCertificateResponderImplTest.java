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

package se.inera.intyg.intygstjanst.web.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum.ERROR;
import static se.inera.ifv.insuranceprocess.healthreporting.v2.ResultCodeEnum.OK;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3.wsaddressing10.AttributedURIType;

import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.LakarutlatandeEnkelType;
import se.inera.ifv.insuranceprocess.healthreporting.medcertqa.v1.VardAdresseringsType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificate.rivtabp20.v1.SendMedicalCertificateResponderInterface;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateRequestType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendMedicalCertificateResponseType;
import se.inera.ifv.insuranceprocess.healthreporting.sendmedicalcertificateresponder.v1.SendType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.EnhetType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.HosPersonalType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.PatientType;
import se.inera.ifv.insuranceprocess.healthreporting.v2.VardgivareType;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.builder.CertificateBuilder;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import iso.v21090.dt.v1.II;


@RunWith( MockitoJUnitRunner.class )
public class SendMedicalCertificateResponderImplTest {

    private static final String LOGICAL_ADDRESS = "HSA-1234567890";

    private static final String CERTIFICATE_ID = "Intygs-id-1234567890";
    private static final String CERTIFICATE_TYPE = "fk7263";

    private static final Personnummer PERSONNUMMER = new Personnummer("19121212-1212");

    private static final String FK_RECIPIENT_ID = "FK";
    private static final String FK_RECIPIENT_NAME = "Försäkringskassan";
    private static final String FK_RECIPIENT_LOGICALADDRESS = "FKORG";
    private static final String FK_RECIPIENT_CERTIFICATETYPES = "fk7263";

    private static final String PATIENT_ID_OID = "1.2.752.129.2.1.3.1";
    private static final String HOS_PERSONAL_OID = "1.2.752.129.2.1.4.1";
    private static final String ENHET_OID = "1.2.752.129.2.1.4.1";
    private static final String ARBETSPLATS_CODE_OID = "1.2.752.29.4.71";


    @Mock
    private CertificateService certificateService = mock(CertificateService.class);

    @Mock
    private RecipientService recipientService = mock(RecipientService.class);

    @InjectMocks
    private SendMedicalCertificateResponderInterface responder = new SendMedicalCertificateResponderImpl();

    @Test
    public void testSendOk() throws Exception {
        List<Recipient> recipients = new ArrayList<Recipient>();
        recipients.add(createFkRecipient());

        when(certificateService.getCertificateForCare(CERTIFICATE_ID)).thenReturn(createCertificateBuilder().build());
        when(recipientService.listRecipients(any(CertificateType.class))).thenReturn(recipients);

        AttributedURIType uri = new AttributedURIType();
        uri.setValue(LOGICAL_ADDRESS);

        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(uri, createRequest());

        assertEquals(OK, response.getResult().getResultCode());

        verify(recipientService).listRecipients(createCertificateType());
        verify(certificateService).sendCertificate(PERSONNUMMER, CERTIFICATE_ID, FK_RECIPIENT_ID);
    }

    @Test
    public void testSendFailsWhenMultipleRecipients() throws Exception {
        String errMsg = "Multiple recipients were found for certificate of type fk7263. Unable to decide recipient. Maybe this is a missed configuration.";

        List<Recipient> recipients = new ArrayList<Recipient>();
        recipients.add(createFkRecipient());
        recipients.add(createFkRecipient());

        when(certificateService.getCertificateForCare(CERTIFICATE_ID)).thenReturn(createCertificateBuilder().build());
        when(recipientService.listRecipients(any(CertificateType.class))).thenReturn(recipients);

        AttributedURIType uri = new AttributedURIType();
        uri.setValue(LOGICAL_ADDRESS);

        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(uri, createRequest());

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(errMsg, response.getResult().getErrorText());

        verify(recipientService).listRecipients(createCertificateType());
    }

    @Test
    public void testSendFailsWhenZeroRecipients() throws Exception {
        String errMsg = "No recipient was found for certificate of type fk7263. Maybe this is a missed configuration.";

        List<Recipient> recipients = new ArrayList<Recipient>();

        when(certificateService.getCertificateForCare(CERTIFICATE_ID)).thenReturn(createCertificateBuilder().build());
        when(recipientService.listRecipients(any(CertificateType.class))).thenReturn(recipients);

        AttributedURIType uri = new AttributedURIType();
        uri.setValue(LOGICAL_ADDRESS);

        SendMedicalCertificateResponseType response = responder.sendMedicalCertificate(uri, createRequest());

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(errMsg, response.getResult().getErrorText());

        verify(recipientService).listRecipients(createCertificateType());
    }

    private CertificateBuilder createCertificateBuilder() {
        return new CertificateBuilder(CERTIFICATE_ID)
                .certificateType(CERTIFICATE_TYPE)
                .civicRegistrationNumber(PERSONNUMMER);
    }

    private CertificateType createCertificateType() {
        return new CertificateType(CERTIFICATE_TYPE);
    }

    private Recipient createFkRecipient() {
        return new Recipient(FK_RECIPIENT_LOGICALADDRESS,
                FK_RECIPIENT_NAME,
                FK_RECIPIENT_ID,
                FK_RECIPIENT_CERTIFICATETYPES);
    }

    private SendMedicalCertificateRequestType createRequest() {
        SendMedicalCertificateRequestType request = new SendMedicalCertificateRequestType();
        SendType sendType = new SendType();
        
        VardAdresseringsType vardAdresseringsType = new VardAdresseringsType();
        HosPersonalType hosPersonal = new HosPersonalType();

        // Enhet
        EnhetType enhet = new EnhetType();
        enhet.setEnhetsnamn("enhetsnamn");
        II enhetsId = new II();
        enhetsId.setRoot(ENHET_OID);
        enhetsId.setExtension("enhetsid");
        enhet.setEnhetsId(enhetsId);
        II arbetsplatsKod = new II();
        arbetsplatsKod.setRoot(ARBETSPLATS_CODE_OID);
        arbetsplatsKod.setExtension("arbetsplatskod");
        enhet.setArbetsplatskod(arbetsplatsKod);
        VardgivareType vardGivare = new VardgivareType();
        II vardGivarId = new II();
        vardGivarId.setRoot(ENHET_OID);
        vardGivarId.setExtension("vardgivarid");
        vardGivare.setVardgivareId(vardGivarId);
        vardGivare.setVardgivarnamn("MI");
        enhet.setVardgivare(vardGivare);
        hosPersonal.setEnhet(enhet);
        hosPersonal.setFullstandigtNamn("MI");
        II personalId = new II();
        personalId.setRoot(HOS_PERSONAL_OID);
        personalId.setExtension("MI");
        hosPersonal.setPersonalId(personalId);
        hosPersonal.setFullstandigtNamn("hospersonal namn");
        vardAdresseringsType.setHosPersonal(hosPersonal);

        sendType.setAdressVard(vardAdresseringsType);
        sendType.setAvsantTidpunkt(new LocalDateTime());
        sendType.setVardReferensId("MI");

        // Lakarutlatande
        LakarutlatandeEnkelType lakarutlatande = new LakarutlatandeEnkelType();
        lakarutlatande.setLakarutlatandeId(CERTIFICATE_ID);
        lakarutlatande.setSigneringsTidpunkt(new LocalDateTime());
        PatientType patient = new PatientType();
        II patientIdHolder = new II();
        patientIdHolder.setRoot(PATIENT_ID_OID);
        patientIdHolder.setExtension(PERSONNUMMER.getPersonnummer());
        patient.setPersonId(patientIdHolder);
        patient.setFullstandigtNamn("patientnamn");
        lakarutlatande.setPatient(patient);

        sendType.setLakarutlatande(lakarutlatande);        

        request.setSend(sendType);

        return request;
    }
}
