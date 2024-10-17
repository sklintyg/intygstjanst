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

package se.inera.intyg.intygstjanst.web.csintegration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.inera.intyg.intygstjanst.logging.MdcHelper.LOG_SESSION_ID_HEADER;
import static se.inera.intyg.intygstjanst.logging.MdcHelper.LOG_TRACE_ID_HEADER;
import static se.inera.intyg.intygstjanst.logging.MdcLogConstants.SESSION_ID_KEY;
import static se.inera.intyg.intygstjanst.logging.MdcLogConstants.TRACE_ID_KEY;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestBodyUriSpec;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;
import org.springframework.web.client.RestClient.ResponseSpec;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.intygstjanst.web.csintegration.dto.CertificateExistsResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCertificateMetadataResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCertificateXmlResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCitizenCertificatesRequest;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCitizenCertificatesResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetMessageXmlResponse;
import se.inera.intyg.intygstjanst.web.csintegration.dto.SendCitizenCertificateRequestDTO;
import se.inera.intyg.intygstjanst.web.csintegration.dto.SendCitizenCertificateResponseDTO;

@ExtendWith(MockitoExtension.class)
class CSIntegrationServiceTest {

    private static final List<Certificate> CITIZEN_CERTIFICATES = List.of(new Certificate());
    private static final Certificate CITIZEN_CERTIFICATE = new Certificate();
    private static final GetCitizenCertificatesRequest GET_CITIZEN_CERTIFICATES_REQUEST = GetCitizenCertificatesRequest.builder().build();
    private static final String CERTIFICATE_ID = "certificateId";
    private static final SendCitizenCertificateRequestDTO SEND_CITIZEN_CERTIFICATE_REQUEST = SendCitizenCertificateRequestDTO.builder()
        .build();

    @Mock
    private RestTemplate restTemplate;
    @Mock
    private RestClient restClient;
    @InjectMocks
    private CSIntegrationService csIntegrationService;

    @Nested
    class GetCitizenCertificatesTest {

        private RequestBodyUriSpec requestBodyUriSpec;
        private ResponseSpec responseSpec;

        @BeforeEach
        void setUp() {
            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            MDC.put(TRACE_ID_KEY, "traceId");
            MDC.put(SESSION_ID_KEY, "sessionId");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri(eq("/api/citizen/certificate"))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(LOG_TRACE_ID_HEADER, "traceId")).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(LOG_SESSION_ID_HEADER, "sessionId")).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(GetCitizenCertificatesRequest.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shallReturnGetCitizenCertificatesResponse() {
            final var expectedResponse = GetCitizenCertificatesResponse.builder()
                .citizenCertificates(CITIZEN_CERTIFICATES)
                .build();

            doReturn(expectedResponse).when(responseSpec).body(GetCitizenCertificatesResponse.class);

            final var actualResponse = csIntegrationService.getCitizenCertificates(GET_CITIZEN_CERTIFICATES_REQUEST);

            assertEquals(expectedResponse.getCitizenCertificates(), actualResponse);
        }

        @Test
        void shallThrowIfResponseIsNull() {
            doReturn(null).when(responseSpec).body(GetCitizenCertificatesResponse.class);

            assertThrows(IllegalStateException.class, () -> csIntegrationService.getCitizenCertificates(GET_CITIZEN_CERTIFICATES_REQUEST));
        }
    }

    @Nested
    class GetMessageXmlResponseTests {

        private RequestBodyUriSpec requestBodyUriSpec;
        private ResponseSpec responseSpec;

        @BeforeEach
        void setUp() {
            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            MDC.put(TRACE_ID_KEY, "traceId");
            MDC.put(SESSION_ID_KEY, "sessionId");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri("/internalapi/message/{messageId}/xml", "messageId")).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(LOG_TRACE_ID_HEADER, "traceId")).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(LOG_SESSION_ID_HEADER, "sessionId")).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shallReturnGetCitizenCertificatesResponse() {
            final var expectedResponse = GetMessageXmlResponse.builder()
                .xml("xmlFromCertificateService")
                .build();

            doReturn(expectedResponse).when(responseSpec).body(GetMessageXmlResponse.class);

            final var actualResponse = csIntegrationService.getMessageXmlResponse("messageId");

            assertEquals(expectedResponse, actualResponse);
        }
    }

    @Nested
    class SendCitizenCertificateRequestDTOTests {

        private RequestBodyUriSpec requestBodyUriSpec;
        private ResponseSpec responseSpec;

        @BeforeEach
        void setUp() {
            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            MDC.put(TRACE_ID_KEY, "traceId");
            MDC.put(SESSION_ID_KEY, "sessionId");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri("/api/citizen/certificate/{certificateId}/send", CERTIFICATE_ID)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(LOG_TRACE_ID_HEADER, "traceId")).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(LOG_SESSION_ID_HEADER, "sessionId")).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.body(any(SendCitizenCertificateRequestDTO.class))).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shallReturnSendCitizenCertificateResponse() {
            final var expectedResponse = SendCitizenCertificateResponseDTO.builder()
                .citizenCertificate(CITIZEN_CERTIFICATE)
                .build();

            doReturn(expectedResponse).when(responseSpec).body(SendCitizenCertificateResponseDTO.class);

            final var actualResponse = csIntegrationService.sendCitizenCertificates(SEND_CITIZEN_CERTIFICATE_REQUEST, CERTIFICATE_ID);

            assertEquals(expectedResponse.getCitizenCertificate(), actualResponse);
        }
    }

    @Nested
    class GetCertificateXmlResponseTests {

        private RequestBodyUriSpec requestBodyUriSpec;
        private ResponseSpec responseSpec;

        @BeforeEach
        void setUp() {
            requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            MDC.put(TRACE_ID_KEY, "traceId");
            MDC.put(SESSION_ID_KEY, "sessionId");

            when(restClient.post()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri("/internalapi/certificate/{certificateId}/xml", "certificateId")).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(LOG_TRACE_ID_HEADER, "traceId")).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(LOG_SESSION_ID_HEADER, "sessionId")).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shallReturnGetCitizenCertificatesResponse() {
            final var expectedResponse = GetCertificateXmlResponse.builder()
                .xml("xmlFromCertificateService")
                .build();

            doReturn(expectedResponse).when(responseSpec).body(GetCertificateXmlResponse.class);

            final var actualResponse = csIntegrationService.getCertificateXmlResponse("certificateId");

            assertEquals(expectedResponse, actualResponse);
        }
    }

    @Nested
    class GetCertificateExistsResponseTests {

        private RequestHeadersUriSpec requestBodyUriSpec;
        private ResponseSpec responseSpec;

        @BeforeEach
        void setUp() {
            requestBodyUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            MDC.put(TRACE_ID_KEY, "traceId");
            MDC.put(SESSION_ID_KEY, "sessionId");

            when(restClient.get()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri("/internalapi/certificate/{certificateId}/exists", "certificateId")).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(LOG_TRACE_ID_HEADER, "traceId")).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(LOG_SESSION_ID_HEADER, "sessionId")).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shallReturnGetCitizenCertificatesResponse() {
            final var expectedResponse = CertificateExistsResponse.builder()
                .exists(true)
                .build();

            doReturn(expectedResponse).when(responseSpec).body(CertificateExistsResponse.class);

            final var actualResponse = csIntegrationService.certificateExists("certificateId");

            assertEquals(expectedResponse.isExists(), actualResponse);
        }

        @Test
        void shallThrowIfResponseIsNull() {
            doReturn(null).when(responseSpec).body(CertificateExistsResponse.class);

            assertThrows(IllegalStateException.class, () -> csIntegrationService.certificateExists("certificateId"));
        }
    }

    @Nested
    class GetCertificateMetadataResponseTests {

        private RequestHeadersUriSpec requestBodyUriSpec;
        private ResponseSpec responseSpec;

        @BeforeEach
        void setUp() {
            requestBodyUriSpec = mock(RestClient.RequestHeadersUriSpec.class);
            responseSpec = mock(RestClient.ResponseSpec.class);

            MDC.put(TRACE_ID_KEY, "traceId");
            MDC.put(SESSION_ID_KEY, "sessionId");

            when(restClient.get()).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.uri("/internalapi/certificate/{certificateId}/metadata", "certificateId")).thenReturn(
                requestBodyUriSpec);
            when(requestBodyUriSpec.header(LOG_TRACE_ID_HEADER, "traceId")).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.header(LOG_SESSION_ID_HEADER, "sessionId")).thenReturn(requestBodyUriSpec);
            when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
        }

        @Test
        void shallReturnGetCitizenCertificatesResponse() {
            final var expectedResponse = GetCertificateMetadataResponse.builder()
                .certificateMetadata(
                    CertificateMetadata.builder().build()
                )
                .build();

            doReturn(expectedResponse).when(responseSpec).body(GetCertificateMetadataResponse.class);

            final var actualResponse = csIntegrationService.getCertificateMetadata("certificateId");

            assertEquals(expectedResponse.getCertificateMetadata(), actualResponse);
        }

        @Test
        void shallThrowIfResponseIsNull() {
            doReturn(null).when(responseSpec).body(GetCertificateMetadataResponse.class);

            assertThrows(IllegalStateException.class, () -> csIntegrationService.getCertificateMetadata("certificateId"));
        }
    }
}