package se.inera.intyg.intygstjanst.integration.intygproxyservice.hsa.configuration;

import static se.inera.intyg.intygstjanst.integration.intygproxyservice.hsa.constants.HsaIntygProxyServiceConstants.EMPLOYEE_CACHE_NAME;
import static se.inera.intyg.intygstjanst.integration.intygproxyservice.hsa.constants.HsaIntygProxyServiceConstants.HEALTH_CARE_PROVIDER_CACHE_NAME;
import static se.inera.intyg.intygstjanst.integration.intygproxyservice.hsa.constants.HsaIntygProxyServiceConstants.HEALTH_CARE_UNIT_CACHE_NAME;
import static se.inera.intyg.intygstjanst.integration.intygproxyservice.hsa.constants.HsaIntygProxyServiceConstants.HEALTH_CARE_UNIT_MEMBERS_CACHE_NAME;
import static se.inera.intyg.intygstjanst.integration.intygproxyservice.hsa.constants.HsaIntygProxyServiceConstants.UNIT_CACHE_NAME;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import se.inera.intyg.intygstjanst.integration.intygproxyservice.configuration.IntygProxyServiceProperties;

@Configuration
@RequiredArgsConstructor
public class RedisCacheConfig {

    private final IntygProxyServiceProperties properties;

    @Bean
    public RedisCacheManagerBuilderCustomizer hsaCacheManagerBuilderCustomizer() {
        final var cache = properties.cache();
        return builder -> builder
            .withCacheConfiguration(EMPLOYEE_CACHE_NAME,
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(cache.employeeTtlSeconds())))
            .withCacheConfiguration(HEALTH_CARE_UNIT_CACHE_NAME,
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(cache.healthcareUnitTtlSeconds())))
            .withCacheConfiguration(HEALTH_CARE_UNIT_MEMBERS_CACHE_NAME,
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(cache.healthcareUnitMembersTtlSeconds())))
            .withCacheConfiguration(UNIT_CACHE_NAME,
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(cache.unitTtlSeconds())))
            .withCacheConfiguration(HEALTH_CARE_PROVIDER_CACHE_NAME,
                RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(cache.healthcareProviderTtlSeconds())));
    }
}
