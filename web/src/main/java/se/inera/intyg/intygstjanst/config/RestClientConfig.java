package se.inera.intyg.intygstjanst.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${certificateservice.base.url}")
    private String baseUrl;

    @Bean
    public RestClient csRestClient() {
        return RestClient.create(baseUrl);
    }
}
