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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import se.inera.intyg.intygstjanst.web.service.dto.GetCertificateXmlResponse;
import se.inera.intyg.intygstjanst.web.service.dto.RecipientDTO;

@ExtendWith(MockitoExtension.class)
class GetCertificateXmlServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GetCertificateXmlServiceImpl getCertificateXmlService;

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
            .unitId(UNIT_ID)
            .recipient(RecipientDTO.builder()
                .id(RECIPIENT_ID)
                .build())
            .xml(ENCODED_XML)
            .build();
        when(restTemplate.postForObject(anyString(), eq(HttpEntity.EMPTY), eq(GetCertificateXmlResponse.class), eq(CERTIFICATE_ID)))
            .thenReturn(expectedResponse);

        final var actualResponse = getCertificateXmlService.get(CERTIFICATE_ID);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void shouldThrowRestClientExceptioWhenRequestFails() {
        when(restTemplate.postForObject(anyString(), eq(HttpEntity.EMPTY), eq(GetCertificateXmlResponse.class), eq(CERTIFICATE_ID)))
            .thenThrow(RestClientException.class);
        assertThrows(RestClientException.class, () -> getCertificateXmlService.get(CERTIFICATE_ID));
    }
}
