package se.inera.intyg.intygstjanst.integration.intygproxyservice.pu.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.inera.intyg.intygstjanst.integration.intygproxyservice.pu.configuration.PURestClientConfig.LOG_SESSION_ID_HEADER;
import static se.inera.intyg.intygstjanst.integration.intygproxyservice.pu.configuration.PURestClientConfig.LOG_TRACE_ID_HEADER;
import static se.inera.intyg.intygstjanst.integration.intygproxyservice.pu.configuration.PURestClientConfig.SESSION_ID_KEY;
import static se.inera.intyg.intygstjanst.integration.intygproxyservice.pu.configuration.PURestClientConfig.TRACE_ID_KEY;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import se.inera.intyg.intygstjanst.integration.intygproxyservice.configuration.IntygProxyServiceProperties;
import se.inera.intyg.intygstjanst.integration.intygproxyservice.pu.dto.PersonRequestDTO;
import se.inera.intyg.intygstjanst.integration.intygproxyservice.pu.dto.PersonResponseDTO;
import se.inera.intyg.intygstjanst.integration.pu.model.Person;
import se.inera.intyg.intygstjanst.integration.pu.model.PersonSvar.Status;

@ExtendWith(MockitoExtension.class)
class GetPersonIntygProxyServiceClientTest {

    private static final String ENDPOINT = "endpoint";
    private static final String TRACE_ID = "traceId";
    private static final String SESSION_ID = "sessionId";
    private final RestClient.RequestBodyUriSpec requestBodyUriSpec = mock(RestClient.RequestBodyUriSpec.class);
    private final RestClient.RequestBodySpec requestBodySpec = mock(RestClient.RequestBodySpec.class);
    private final RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

    @Mock
    private RestClient restClient;

    @Mock
    private IntygProxyServiceProperties properties;

    @Mock
    private IntygProxyServiceProperties.Pu puProperties;

    @InjectMocks
    private GetPersonIntygProxyServiceClient getPersonIntygProxyServiceClient;

    @BeforeEach
    void setUp() {
        when(properties.pu()).thenReturn(puProperties);
        when(puProperties.personEndpoint()).thenReturn(ENDPOINT);
        MDC.put(TRACE_ID_KEY, TRACE_ID);
        MDC.put(SESSION_ID_KEY, SESSION_ID);
    }

    @Test
    void shallReturnPersonResponse() {
        final var request = PersonRequestDTO.builder()
            .personId("personId")
            .queryCache(true)
            .build();

        final var expectedResponse = PersonResponseDTO.builder()
            .status(Status.FOUND)
            .person(mock(Person.class))
            .build();

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(ENDPOINT);
        doReturn(requestBodySpec).when(requestBodySpec).body(request);
        doReturn(requestBodySpec).when(requestBodySpec).header(LOG_TRACE_ID_HEADER, TRACE_ID);
        doReturn(requestBodySpec).when(requestBodySpec).header(LOG_SESSION_ID_HEADER, SESSION_ID);
        doReturn(requestBodySpec).when(requestBodySpec).contentType(MediaType.APPLICATION_JSON);
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(expectedResponse).when(responseSpec).body(PersonResponseDTO.class);

        final var response = getPersonIntygProxyServiceClient.get(request);

        assertEquals(expectedResponse, response);
    }

    @Test
    void shallSetHeadersCorrectly() {
        final var request = PersonRequestDTO.builder()
            .personId("personId")
            .queryCache(true)
            .build();

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(ENDPOINT);
        doReturn(requestBodySpec).when(requestBodySpec).body(request);
        doReturn(requestBodySpec).when(requestBodySpec).header(LOG_TRACE_ID_HEADER, TRACE_ID);
        doReturn(requestBodySpec).when(requestBodySpec).header(LOG_SESSION_ID_HEADER, SESSION_ID);
        doReturn(requestBodySpec).when(requestBodySpec).contentType(MediaType.APPLICATION_JSON);
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(mock(PersonResponseDTO.class)).when(responseSpec).body(PersonResponseDTO.class);

        getPersonIntygProxyServiceClient.get(request);

        verify(requestBodySpec).header(LOG_TRACE_ID_HEADER, TRACE_ID);
        verify(requestBodySpec).header(LOG_SESSION_ID_HEADER, SESSION_ID);
    }

    @Test
    void shallSetContentTypeAsApplicationJson() {
        final var request = PersonRequestDTO.builder()
            .personId("personId")
            .queryCache(true)
            .build();

        doReturn(requestBodyUriSpec).when(restClient).post();
        doReturn(requestBodySpec).when(requestBodyUriSpec).uri(ENDPOINT);
        doReturn(requestBodySpec).when(requestBodySpec).body(request);
        doReturn(requestBodySpec).when(requestBodySpec).header(LOG_TRACE_ID_HEADER, TRACE_ID);
        doReturn(requestBodySpec).when(requestBodySpec).header(LOG_SESSION_ID_HEADER, SESSION_ID);
        doReturn(requestBodySpec).when(requestBodySpec).contentType(MediaType.APPLICATION_JSON);
        doReturn(responseSpec).when(requestBodySpec).retrieve();
        doReturn(mock(PersonResponseDTO.class)).when(responseSpec).body(PersonResponseDTO.class);

        getPersonIntygProxyServiceClient.get(request);

        verify(requestBodySpec).contentType(MediaType.APPLICATION_JSON);
    }
}