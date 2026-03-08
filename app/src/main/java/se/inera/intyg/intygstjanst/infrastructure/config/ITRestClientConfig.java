package se.inera.intyg.intygstjanst.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import se.inera.intyg.intygstjanst.infrastructure.config.properties.AppProperties;

@Configuration
@RequiredArgsConstructor
public class ITRestClientConfig {

    private final AppProperties appProperties;

    @Bean
    public RestClient csRestClient() {
        return RestClient.create(appProperties.integration().certificateService().baseUrl());
    }
}
