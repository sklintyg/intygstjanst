package se.inera.intyg.intygstjanst.integration.intygproxyservice.hsa.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import se.inera.intyg.intygstjanst.integration.intygproxyservice.configuration.IntygProxyServiceProperties;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(IntygProxyServiceProperties.class)
public class HsaRestClientConfig {

    public static final String LOG_TRACE_ID_HEADER = "x-trace-id";
    public static final String LOG_SESSION_ID_HEADER = "x-session-id";

    public static final String SESSION_ID_KEY = "session.id";
    public static final String TRACE_ID_KEY = "trace.id";

    private final IntygProxyServiceProperties properties;

    @Bean(name = "hsaIntygProxyServiceRestClient")
    public RestClient ipsRestClient() {
        return RestClient.create(properties.baseUrl());
    }
}