package se.inera.intyg.intygstjanst.integration.intygproxyservice.pu.client;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import se.inera.intyg.intygstjanst.integration.intygproxyservice.configuration.IntygProxyServiceProperties;
import se.inera.intyg.intygstjanst.integration.intygproxyservice.pu.configuration.PURestClientConfig;
import se.inera.intyg.intygstjanst.integration.intygproxyservice.pu.dto.PersonRequestDTO;
import se.inera.intyg.intygstjanst.integration.intygproxyservice.pu.dto.PersonResponseDTO;

@Service
public class GetPersonIntygProxyServiceClient {

    private final RestClient ipsRestClient;
    private final IntygProxyServiceProperties properties;

    @Autowired
    public GetPersonIntygProxyServiceClient(
        @Qualifier("puIntygProxyServiceRestClient") RestClient ipsRestClient,
        IntygProxyServiceProperties properties) {
        this.ipsRestClient = ipsRestClient;
        this.properties = properties;
    }

    public PersonResponseDTO get(PersonRequestDTO request) {
        return ipsRestClient
            .post()
            .uri(properties.pu().personEndpoint())
            .body(request)
            .header(PURestClientConfig.LOG_TRACE_ID_HEADER, MDC.get(PURestClientConfig.TRACE_ID_KEY))
            .header(PURestClientConfig.LOG_SESSION_ID_HEADER, MDC.get(PURestClientConfig.SESSION_ID_KEY))
            .contentType(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(PersonResponseDTO.class);
    }
}