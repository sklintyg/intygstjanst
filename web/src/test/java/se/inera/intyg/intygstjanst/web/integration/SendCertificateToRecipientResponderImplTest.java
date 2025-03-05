/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType.ERROR;
import static se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType.INFO;
import static se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType.OK;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.intygstjanst.logging.HashUtility;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.ServerException;
import se.inera.intyg.intygstjanst.web.exception.TestCertificateException;
import se.inera.intyg.intygstjanst.web.service.CertificateService.SendStatus;
import se.inera.intyg.intygstjanst.web.service.SendCertificateService;
import se.inera.intyg.intygstjanst.web.service.dto.SendCertificateRequestDTO;
import se.inera.intyg.schemas.contract.Personnummer;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientResponseType;
import se.riv.clinicalprocess.healthcond.certificate.sendCertificateToRecipient.v2.SendCertificateToRecipientType;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.HsaId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.IntygId;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.Part;
import se.riv.clinicalprocess.healthcond.certificate.types.v3.PersonId;
import se.riv.clinicalprocess.healthcond.certificate.v3.ErrorIdType;
import se.riv.clinicalprocess.healthcond.certificate.v3.HosPersonal;

@ExtendWith(MockitoExtension.class)
class SendCertificateToRecipientResponderImplTest {

    private static final Personnummer PERSONNUMMER = Personnummer.createPersonnummer("19121212-1212").orElseThrow();
    private static final String CERTIFICATE_ID = "Intygs-id-1234567890";
    private static final String RECIPIENT_ID = "TRANSP";

    private static final String LOGICAL_ADDRESS = "Intygstjänsten";
    private SendCertificateToRecipientType request;
    @Mock
    private SendCertificateService sendCertificateService;
    @Mock
    private HashUtility hashUtility;
    @InjectMocks
    private SendCertificateToRecipientResponderImpl responder;

    @Test
    void testSendCertificateToRecipient() throws Exception {

        SendCertificateToRecipientType sendCertificateToRecipient = createRequest();
        sendCertificateToRecipient.getMottagare().setCode("FKASSA");
        when(sendCertificateService.send(any())).thenReturn(SendStatus.OK);

        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, sendCertificateToRecipient);

        assertEquals(OK, response.getResult().getResultCode());
    }

    @Test
    void testSendCertificateToRecipientAlreadySent() throws Exception {

        when(sendCertificateService.send(any())).thenReturn(SendStatus.ALREADY_SENT);

        SendCertificateToRecipientType sendCertificateToRecipient = createRequest();
        sendCertificateToRecipient.getMottagare().setCode("FKASSA");
        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, sendCertificateToRecipient);

        assertEquals(INFO, response.getResult().getResultCode());
        assertEquals("Certificate 'Intygs-id-1234567890' already sent to 'FKASSA'.", response.getResult().getResultText());
    }

    @Test
    void testSendCertificateToRecipientInvalidCertificate() throws Exception {
        final var pnr = hashUtility.hash(PERSONNUMMER.getPersonnummer());
        when(sendCertificateService.send(any()))
            .thenThrow(new InvalidCertificateException(CERTIFICATE_ID, pnr));

        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, createRequest());

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdType.APPLICATION_ERROR, response.getResult().getErrorId());
        assertEquals("Unknown certificate ID: Intygs-id-1234567890", response.getResult().getResultText());
    }

    @Test
    void testSendCertificateToRecipientCertificateRevoked() throws Exception {
        when(sendCertificateService.send(any()))
            .thenThrow(new CertificateRevokedException(CERTIFICATE_ID));

        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, createRequest());

        assertEquals(INFO, response.getResult().getResultCode());
        assertEquals("Certificate 'Intygs-id-1234567890' has been revoked.", response.getResult().getResultText());
    }

    @Test
    void testSendCertificateToRecipientRecipientUnknown() throws Exception {
        when(sendCertificateService.send(any()))
            .thenThrow(new RecipientUnknownException(""));

        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, createRequest());

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdType.APPLICATION_ERROR, response.getResult().getErrorId());
        assertEquals("Unknown recipient ID: TRANSP", response.getResult().getResultText());
    }

    @Test
    void testSendCertificateToRecipientServerException() throws Exception {
        when(sendCertificateService.send(any()))
            .thenThrow(new ServerException());

        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, createRequest());

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdType.TECHNICAL_ERROR, response.getResult().getErrorId());
        assertEquals("Certificate 'Intygs-id-1234567890' couldn't be sent to recipient", response.getResult().getResultText());
    }

    @Test
    void testSendTestCertificateToRecipientTestCertificateException() throws Exception {
        when(sendCertificateService.send(any()))
            .thenThrow(new TestCertificateException(CERTIFICATE_ID));

        SendCertificateToRecipientResponseType response = responder.sendCertificateToRecipient(LOGICAL_ADDRESS, createRequest());

        assertEquals(ERROR, response.getResult().getResultCode());
        assertEquals(ErrorIdType.VALIDATION_ERROR, response.getResult().getErrorId());
        assertEquals("Certificate 'Intygs-id-1234567890' couldn't be sent to recipient because it is a test certificate",
            response.getResult().getResultText());
    }

    void setup() {
        request = createRequest();
        request.getMottagare().setCode("FKASSA");
    }

    @SneakyThrows
    @Test
    void shouldSendPatientId() {
        setup();
        final var captor = ArgumentCaptor.forClass(SendCertificateRequestDTO.class);
        responder.sendCertificateToRecipient(LOGICAL_ADDRESS, request);

        verify(sendCertificateService).send(captor.capture());

        assertEquals(request.getPatientPersonId().getExtension(),
            captor.getValue().getPatientId().getOriginalPnr());
    }

    @SneakyThrows
    @Test
    void shouldSendCertificateId() {
        setup();
        final var captor = ArgumentCaptor.forClass(SendCertificateRequestDTO.class);
        responder.sendCertificateToRecipient(LOGICAL_ADDRESS, request);

        verify(sendCertificateService).send(captor.capture());

        assertEquals(request.getIntygsId().getExtension(), captor.getValue().getCertificateId());
    }

    @SneakyThrows
    @Test
    void shouldSendRecipientId() {
        setup();
        final var captor = ArgumentCaptor.forClass(SendCertificateRequestDTO.class);
        responder.sendCertificateToRecipient(LOGICAL_ADDRESS, request);

        verify(sendCertificateService).send(captor.capture());

        assertEquals(request.getMottagare().getCode(), captor.getValue().getRecipientId());
    }

    @SneakyThrows
    @Test
    void shouldSendHsaId() {
        setup();
        final var captor = ArgumentCaptor.forClass(SendCertificateRequestDTO.class);
        responder.sendCertificateToRecipient(LOGICAL_ADDRESS, request);

        verify(sendCertificateService).send(captor.capture());

        assertEquals(request.getSkickatAv().getHosPersonal().getPersonalId().getExtension(),
            captor.getValue().getHsaId());
    }

    private SendCertificateToRecipientType createRequest() {

        SendCertificateToRecipientType sendCertificateToRecipient = new SendCertificateToRecipientType();
        sendCertificateToRecipient.setPatientPersonId(new PersonId());
        sendCertificateToRecipient.getPatientPersonId().setExtension(PERSONNUMMER.getPersonnummer());
        sendCertificateToRecipient.setMottagare(new Part());
        sendCertificateToRecipient.getMottagare().setCode(RECIPIENT_ID);
        sendCertificateToRecipient.setIntygsId(new IntygId());
        sendCertificateToRecipient.getIntygsId().setExtension(CERTIFICATE_ID);
        SendCertificateToRecipientType.SkickatAv skickatAv = new SendCertificateToRecipientType.SkickatAv();
        skickatAv.setPersonId(new PersonId());
        skickatAv.setHosPersonal(new HosPersonal());
        skickatAv.getHosPersonal().setPersonalId(new HsaId());
        skickatAv.getHosPersonal().getPersonalId().setExtension("EXTENSION");
        sendCertificateToRecipient.setSkickatAv(skickatAv);
        return sendCertificateToRecipient;
    }
}
