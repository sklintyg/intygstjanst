/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.xml.ws.soap.SOAPFaultException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCertificateXmlResponse;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.MonitoringLogService;
import se.inera.intyg.intygstjanst.web.service.RecipientService;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateRecipientType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.intygstjanst.web.service.dto.RecipientDTO;
import se.inera.intyg.intygstjanst.web.service.dto.UnitDTO;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateResponderInterface;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateResponseType;
import se.riv.clinicalprocess.healthcond.certificate.registerCertificate.v3.RegisterCertificateType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultCodeType;
import se.riv.clinicalprocess.healthcond.certificate.v3.ResultType;

@ExtendWith(MockitoExtension.class)
class CertificateEventSendServiceImplTest {

    @Mock
    private RegisterCertificateResponderInterface registerCertificateResponderInterface;
    @Mock
    private RecipientService recipientService;
    @Mock
    private MonitoringLogService monitoringLogService;

    @InjectMocks
    private CertificateEventSendServiceImpl certificateEventSendService;

    private static final String ERROR_MESSAGE = "errorMessage";
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String CERTIFICATE_TYPE = "fk7211";
    private static final String UNIT_ID = "unitId";
    private static final String RECIPIENT_ID = "recipientId";
    private static final String RECIPIENT_NAME = "recipientName";
    private static final String LOGICAL_ADDRESS = "logicalAddress";
    private static final Recipient RECIPIENT = new Recipient(LOGICAL_ADDRESS, RECIPIENT_NAME, RECIPIENT_ID,
        CertificateRecipientType.HUVUDMOTTAGARE.name(), CERTIFICATE_TYPE, true, true);
    private static final String ENCODED_XML = Base64.getEncoder().encodeToString("xmlFromCertificateService"
        .getBytes(StandardCharsets.UTF_8));
    private static final String EXAMPLE_CERTIFICATE_FILE_PATH = "GetCertificateResponderImplTest/valid-minimal-registercertificate-v3.xml";

    private String decodedXml;
    private GetCertificateXmlResponse xmlResponse;

    @BeforeEach
    void setUp() throws IOException {
        decodedXml = Resources.toString(Resources.getResource(EXAMPLE_CERTIFICATE_FILE_PATH), Charsets.UTF_8);

        xmlResponse = GetCertificateXmlResponse.builder()
            .certificateId(CERTIFICATE_ID)
            .certificateType(CERTIFICATE_TYPE)
            .unit(
                UnitDTO.builder()
                    .unitId(UNIT_ID)
                    .build()
            )
            .xml(ENCODED_XML)
            .recipient(RecipientDTO.builder()
                .id(RECIPIENT_ID)
                .build())
            .build();
    }

    @Nested
    class ResultOk {

        @Test
        void shouldNotThrowIfResultCodeOk() throws RecipientUnknownException {
            when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(RECIPIENT);
            when(registerCertificateResponderInterface.registerCertificate(eq(LOGICAL_ADDRESS), any(RegisterCertificateType.class)))
                .thenReturn(createResponse(ResultCodeType.OK, ""));

            assertDoesNotThrow(() -> certificateEventSendService.send(xmlResponse, decodedXml));
        }

        @Test
        void shouldMonitorLogIfResultCodeOk() throws RecipientUnknownException {
            when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(RECIPIENT);
            when(registerCertificateResponderInterface.registerCertificate(eq(LOGICAL_ADDRESS), any(RegisterCertificateType.class)))
                .thenReturn(createResponse(ResultCodeType.OK, ""));

            certificateEventSendService.send(xmlResponse, decodedXml);

            verify(monitoringLogService).logCertificateSent(xmlResponse.getCertificateId(), xmlResponse.getCertificateType(),
                xmlResponse.getUnit().getUnitId(), xmlResponse.getRecipient().getId());
        }
    }

    @Nested
    class ResultInfo {

        @Test
        void shouldNotThrowIfResultCodeInfo() throws RecipientUnknownException {
            when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(RECIPIENT);
            when(registerCertificateResponderInterface.registerCertificate(eq(LOGICAL_ADDRESS), any(RegisterCertificateType.class)))
                .thenReturn(createResponse(ResultCodeType.INFO, "infoText"));

            assertDoesNotThrow(() -> certificateEventSendService.send(xmlResponse, decodedXml));
        }

        @Test
        void shouldMonitorLogIfResultCodeInfo() throws RecipientUnknownException {
            when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(RECIPIENT);
            when(registerCertificateResponderInterface.registerCertificate(eq(LOGICAL_ADDRESS), any(RegisterCertificateType.class)))
                .thenReturn(createResponse(ResultCodeType.INFO, "infoText"));

            certificateEventSendService.send(xmlResponse, decodedXml);

            verify(monitoringLogService).logCertificateSent(xmlResponse.getCertificateId(), xmlResponse.getCertificateType(),
                xmlResponse.getUnit().getUnitId(), xmlResponse.getRecipient().getId());
        }
    }

    @Nested
    class ResultError {

        @Test
        void shouldThrowIllegalStateExceptionIfResultCodeError() throws RecipientUnknownException {
            when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(RECIPIENT);
            when(registerCertificateResponderInterface.registerCertificate(eq(LOGICAL_ADDRESS), any(RegisterCertificateType.class)))
                .thenReturn(createResponse(ResultCodeType.ERROR, "errorText"));

            assertThrows(IllegalStateException.class, () -> certificateEventSendService.send(xmlResponse, decodedXml));
        }

        @Test
        void shouldIncludeSendCallParametersInExceptionMessageWhenResultIsError() throws RecipientUnknownException {
            when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(RECIPIENT);
            when(registerCertificateResponderInterface.registerCertificate(eq(LOGICAL_ADDRESS), any(RegisterCertificateType.class)))
                .thenReturn(createResponse(ResultCodeType.ERROR, ERROR_MESSAGE));

            final var e = assertThrows(IllegalStateException.class, () -> certificateEventSendService.send(xmlResponse, decodedXml));
            assertAll(
                () -> assertTrue(e.getMessage().contains(CERTIFICATE_ID)),
                () -> assertTrue(e.getMessage().contains(CERTIFICATE_TYPE)),
                () -> assertTrue(e.getMessage().contains(RECIPIENT_ID)),
                () -> assertTrue(e.getMessage().contains(ERROR_MESSAGE))
            );
        }
    }

    @Nested
    class ResultNull {

        @Test
        void shouldIncludeSendCallParametersInExceptionMessageWhenResponseIsNull() throws RecipientUnknownException {
            when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(RECIPIENT);
            when(registerCertificateResponderInterface.registerCertificate(eq(LOGICAL_ADDRESS), any(RegisterCertificateType.class)))
                .thenReturn(createResponse(null));

            final var e = assertThrows(IllegalStateException.class, () -> certificateEventSendService.send(xmlResponse, decodedXml));
            assertAll(
                () -> assertTrue(e.getMessage().contains(CERTIFICATE_ID)),
                () -> assertTrue(e.getMessage().contains(CERTIFICATE_TYPE)),
                () -> assertTrue(e.getMessage().contains(RECIPIENT_ID))
            );
        }

        @Test
        void shouldIncludeSendCallParametersInExceptionMessageWhenResultIsNull() throws RecipientUnknownException {
            when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(RECIPIENT);
            when(registerCertificateResponderInterface.registerCertificate(eq(LOGICAL_ADDRESS), any(RegisterCertificateType.class)))
                .thenReturn(createResponse(null));

            final var e = assertThrows(IllegalStateException.class, () -> certificateEventSendService.send(xmlResponse, decodedXml));
            assertAll(
                () -> assertTrue(e.getMessage().contains(CERTIFICATE_ID)),
                () -> assertTrue(e.getMessage().contains(CERTIFICATE_TYPE)),
                () -> assertTrue(e.getMessage().contains(RECIPIENT_ID))
            );
        }
    }

    @Test
    void shouldThrowIllegalStateExceptionIfRecipientUnknownException() throws RecipientUnknownException {
        when(recipientService.getRecipient(RECIPIENT_ID)).thenThrow(RecipientUnknownException.class);
        assertThrows(IllegalStateException.class, () -> certificateEventSendService.send(xmlResponse, decodedXml));
    }

    @Test
    void shouldThrowIllegalStateExceptionIfSoapFault() throws RecipientUnknownException {
        when(recipientService.getRecipient(RECIPIENT_ID)).thenReturn(RECIPIENT);
        when(registerCertificateResponderInterface.registerCertificate(eq(LOGICAL_ADDRESS), any(RegisterCertificateType.class)))
            .thenThrow(SOAPFaultException.class);
        assertThrows(IllegalStateException.class, () -> certificateEventSendService.send(xmlResponse, decodedXml));
    }

    private RegisterCertificateResponseType createResponse(ResultCodeType resultCode, String resultText) {
        final var resultType = new ResultType();
        resultType.setResultCode(resultCode);
        resultType.setResultText(resultText);
        return createResponse(resultType);
    }

    private RegisterCertificateResponseType createResponse(ResultType resultType) {
        final var responseType = new RegisterCertificateResponseType();
        responseType.setResult(resultType);
        return responseType;
    }

}
