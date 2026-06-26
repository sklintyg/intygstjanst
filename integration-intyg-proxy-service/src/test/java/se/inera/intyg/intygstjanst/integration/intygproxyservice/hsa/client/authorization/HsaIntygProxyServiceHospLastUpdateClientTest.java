/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.integration.intygproxyservice.hsa.client.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.inera.intyg.intygstjanst.integration.intygproxyservice.hsa.configuration.HsaRestClientConfig.LOG_SESSION_ID_HEADER;
import static se.inera.intyg.intygstjanst.integration.intygproxyservice.hsa.configuration.HsaRestClientConfig.LOG_TRACE_ID_HEADER;
import static se.inera.intyg.intygstjanst.integration.intygproxyservice.hsa.configuration.HsaRestClientConfig.SESSION_ID_KEY;
import static se.inera.intyg.intygstjanst.integration.intygproxyservice.hsa.configuration.HsaRestClientConfig.TRACE_ID_KEY;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.RequestHeadersUriSpec;
import org.springframework.web.client.RestClient.ResponseSpec;
import se.inera.intyg.intygstjanst.integration.intygproxyservice.configuration.IntygProxyServicePropertiesTestFixtures;
import se.inera.intyg.intygstjanst.integration.intygproxyservice.hsa.dto.authorization.GetHospLastUpdateResponseDTO;

@ExtendWith(MockitoExtension.class)
class HsaIntygProxyServiceHospLastUpdateClientTest {

  private static final String URI = "/api/from/configuration";

  @Mock private RestClient ipsRestClient;

  private HsaIntygProxyServiceHospLastUpdateClient hospLastUpdateClient;

  private RequestHeadersUriSpec requestBodyUriSpec;
  private ResponseSpec responseSpec;

  @BeforeEach
  void setUp() {
    hospLastUpdateClient =
        new HsaIntygProxyServiceHospLastUpdateClient(
            ipsRestClient, IntygProxyServicePropertiesTestFixtures.withEndpoints(URI));

    requestBodyUriSpec = mock(RequestHeadersUriSpec.class);
    responseSpec = mock(ResponseSpec.class);

    MDC.put(TRACE_ID_KEY, "traceId");
    MDC.put(SESSION_ID_KEY, "sessionId");

    when(ipsRestClient.get()).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.uri(URI)).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.header(LOG_TRACE_ID_HEADER, "traceId")).thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.header(LOG_SESSION_ID_HEADER, "sessionId"))
        .thenReturn(requestBodyUriSpec);
    when(requestBodyUriSpec.retrieve()).thenReturn(responseSpec);
  }

  @Test
  void shallReturnGetCitizenCertificatesResponse() {
    final var expectedResponse =
        GetHospLastUpdateResponseDTO.builder().lastUpdate(LocalDateTime.now()).build();

    doReturn(expectedResponse).when(responseSpec).body(GetHospLastUpdateResponseDTO.class);

    final var actualResponse = hospLastUpdateClient.get();

    assertEquals(expectedResponse, actualResponse);
  }
}
