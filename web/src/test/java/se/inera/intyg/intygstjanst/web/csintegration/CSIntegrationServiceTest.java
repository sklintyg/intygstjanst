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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCitizenCertificatesRequest;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCitizenCertificatesResponse;
import se.inera.intyg.intygstjanst.web.service.dto.GetCertificateXmlResponse;
import se.inera.intyg.intygstjanst.web.service.dto.GetMessageXmlResponse;
import se.inera.intyg.intygstjanst.web.service.dto.RecipientDTO;
import se.inera.intyg.intygstjanst.web.service.dto.UnitDTO;

@ExtendWith(MockitoExtension.class)
class CSIntegrationServiceTest {

    private static final List<Certificate> CITIZEN_CERTIFICATES = List.of(new Certificate());
    private static final GetCitizenCertificatesResponse GET_CITIZEN_CERTIFICATES_RESPONSE = GetCitizenCertificatesResponse.builder()
        .citizenCertificates(CITIZEN_CERTIFICATES)
        .build();
    private static final GetCitizenCertificatesRequest GET_CITIZEN_CERTIFICATES_REQUEST = GetCitizenCertificatesRequest.builder().build();
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private CSIntegrationService csIntegrationService;

    @Nested
    class GetCitizenCertificatesTest {

        @Test
        void shouldPreformPostUsingRequest() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CITIZEN_CERTIFICATES_RESPONSE);
            final var captor = ArgumentCaptor.forClass(GetCitizenCertificatesRequest.class);

            csIntegrationService.getCitizenCertificates(GET_CITIZEN_CERTIFICATES_REQUEST);
            verify(restTemplate).postForObject(anyString(), captor.capture(), any());

            assertEquals(GET_CITIZEN_CERTIFICATES_REQUEST, captor.getValue());
        }

        @Test
        void shouldReturnListOfCitizenCertificate() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CITIZEN_CERTIFICATES_RESPONSE);
            final var response = csIntegrationService.getCitizenCertificates(GET_CITIZEN_CERTIFICATES_REQUEST);

            assertEquals(CITIZEN_CERTIFICATES, response);
        }

        @Test
        void shallThrowIfResponseIsNull() {
            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(null);

            assertThrows(IllegalStateException.class,
                () -> csIntegrationService.getCitizenCertificates(GET_CITIZEN_CERTIFICATES_REQUEST));
        }

        @Test
        void shouldSetUrlCorrect() {
            ReflectionTestUtils.setField(csIntegrationService, "baseUrl", "baseUrl");
            final var captor = ArgumentCaptor.forClass(String.class);

            when(restTemplate.postForObject(anyString(), any(), any()))
                .thenReturn(GET_CITIZEN_CERTIFICATES_RESPONSE);

            csIntegrationService.getCitizenCertificates(GET_CITIZEN_CERTIFICATES_REQUEST);
            verify(restTemplate).postForObject(captor.capture(), any(), any());

            assertEquals("baseUrl/api/citizen/certificate", captor.getValue());
        }
    }

    @Nested
    class GetMessageXmlResponseTests {

        private static final String MESSAGE_ID = "messageId";
        private static final String TOPIC = "topic";
        private static final String ENCODED_XML = "xmlFromCertificateService";

        @Test
        void shouldReturnFetchedObjectOnSuccessfulRequest() {
            final var expectedResponse = GetMessageXmlResponse.builder()
                .messageId(MESSAGE_ID)
                .topic(TOPIC)
                .xml(ENCODED_XML)
                .build();
            when(restTemplate.postForObject(anyString(), eq(HttpEntity.EMPTY), eq(GetMessageXmlResponse.class), eq(MESSAGE_ID)))
                .thenReturn(expectedResponse);

            final var actualResponse = csIntegrationService.getMessageXmlResponse(MESSAGE_ID);

            assertEquals(expectedResponse, actualResponse);
        }

        @Test
        void shouldThrowRestClientExceptioWhenRequestFails() {
            when(restTemplate.postForObject(anyString(), eq(HttpEntity.EMPTY), eq(GetMessageXmlResponse.class), eq(MESSAGE_ID)))
                .thenThrow(RestClientException.class);
            assertThrows(RestClientException.class, () -> csIntegrationService.getMessageXmlResponse(MESSAGE_ID));
        }
    }

    @Nested
    class GetCertificateXmlResponseTests {

        private static final String CERTIFICATE_ID = "certificateId";
        private static final String CERTIFICATE_TYPE = "fk7211";
        private static final String UNIT_ID = "unitId";
        private static final String RECIPIENT_ID = "recipientId";
        private static final String ENCODED_XML = "xmlFromCertificateService";

        @Test
        void shouldReturnFetchedObjectOnSuccessfulRequest() {
            final var expectedResponse = GetCertificateXmlResponse.builder()
                .certificateId(CERTIFICATE_ID)
                .certificateType(CERTIFICATE_TYPE)
                .unit(
                    UnitDTO.builder()
                        .unitId(UNIT_ID)
                        .build()
                )
                .recipient(RecipientDTO.builder()
                    .id(RECIPIENT_ID)
                    .build())
                .xml(ENCODED_XML)
                .build();
            when(restTemplate.postForObject(anyString(), eq(HttpEntity.EMPTY), eq(GetCertificateXmlResponse.class), eq(CERTIFICATE_ID)))
                .thenReturn(expectedResponse);

            final var actualResponse = csIntegrationService.getCertificateXmlResponse(CERTIFICATE_ID);

            assertEquals(expectedResponse, actualResponse);
        }

        @Test
        void shouldThrowRestClientExceptioWhenRequestFails() {
            when(restTemplate.postForObject(anyString(), eq(HttpEntity.EMPTY), eq(GetCertificateXmlResponse.class), eq(CERTIFICATE_ID)))
                .thenThrow(RestClientException.class);
            assertThrows(RestClientException.class, () -> csIntegrationService.getCertificateXmlResponse(CERTIFICATE_ID));
        }
    }
}
