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
package se.inera.intyg.intygstjanst.integration.webcert.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import se.inera.intyg.intygstjanst.integration.webcert.configuration.WebcertProperties;
import se.inera.intyg.intygstjanst.integration.webcert.configuration.WebcertRestClientConfig;

class GetBinaryCertificateWebcertClientTest {

  private static final String BASE_URL = "https://webcert.example.se";
  private static final String ENDPOINT = "/internalapi/certificate/%s/binary";
  private static final String CERT_ID = "abc-123";
  private static final String TRACE_ID = "trace-id";
  private static final String SESSION_ID = "session-id";
  private static final String EXPECTED_URL = BASE_URL + "/internalapi/certificate/abc-123/binary";

  private MockRestServiceServer mockServer;
  private GetBinaryCertificateWebcertClient client;

  @BeforeEach
  void setUp() {
    MDC.put(WebcertRestClientConfig.TRACE_ID_KEY, TRACE_ID);
    MDC.put(WebcertRestClientConfig.SESSION_ID_KEY, SESSION_ID);

    final var restClientBuilder = RestClient.builder();
    mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
    final var restClient = restClientBuilder.baseUrl(BASE_URL).build();
    client =
        new GetBinaryCertificateWebcertClient(
            restClient, new WebcertProperties(BASE_URL, ENDPOINT));
  }

  @Test
  void shouldGetBinaryCertificateAndSetHeaders() {
    mockServer
        .expect(requestTo(EXPECTED_URL))
        .andExpect(method(HttpMethod.GET))
        .andExpect(header(WebcertRestClientConfig.LOG_TRACE_ID_HEADER, TRACE_ID))
        .andExpect(header(WebcertRestClientConfig.LOG_SESSION_ID_HEADER, SESSION_ID))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    final var response = client.get(CERT_ID);

    assertNotNull(response);
    mockServer.verify();
  }

  @Test
  void shouldThrowWebcertClientExceptionWhenServerReturnsErrorStatus() {
    mockServer
        .expect(requestTo(EXPECTED_URL))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR).body("server error"));

    final var exception = assertThrows(WebcertClientException.class, () -> client.get(CERT_ID));

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    assertTrue(exception.getMessage().contains(EXPECTED_URL));
    assertTrue(exception.getMessage().contains("500"));
    mockServer.verify();
  }
}
