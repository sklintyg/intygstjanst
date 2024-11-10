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
package se.inera.intyg.intygstjanst.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import org.apache.cxf.Bus;
import org.apache.cxf.ext.logging.LoggingFeature;
import org.apache.cxf.ext.logging.slf4j.Slf4jVerboseEventSender;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import se.inera.intyg.infra.monitoring.MonitoringConfiguration;

@Configuration
@EnableTransactionManagement
@DependsOn("transactionManager")
@PropertySource("classpath:application.properties")
@PropertySource(ignoreResourceNotFound = true, value = "file:${dev.config.file}")
@ImportResource({"classpath:META-INF/cxf/cxf.xml"})
@Import(MonitoringConfiguration.class)
public class ApplicationConfig implements TransactionManagementConfigurer {

    @Autowired
    private Bus bus;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Value("${logging.soap.enable:false}")
    private boolean loggingSoapEnable;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @PostConstruct
    public Bus init() {
        bus.setFeatures(new ArrayList<>(Collections.singletonList(loggingFeature())));
        bus.setProperty("org.apache.cxf.logging.enable", Boolean.toString(loggingSoapEnable));
        return bus;
    }

    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("version");
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    @Bean
    public LoggingFeature loggingFeature() {
        LoggingFeature loggingFeature = new LoggingFeature();
        loggingFeature.setPrettyLogging(true);
        loggingFeature.setSender(slf4jVerboseEventSender());
        return loggingFeature;
    }

    @Bean
    public Slf4jVerboseEventSender slf4jVerboseEventSender() {
        final var slf4jVerboseEventSender = new Slf4jVerboseEventSender();
        slf4jVerboseEventSender.setLoggingLevel(Level.INFO);
        return slf4jVerboseEventSender;
    }

    @Bean
    public JacksonJsonProvider jacksonJsonProvider(ObjectMapper objectMapper) {
        return new JacksonJsonProvider(objectMapper);
    }

    @Bean
    public PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver() {
        return new PathMatchingResourcePatternResolver();
    }

    @Nonnull
    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return transactionManager;
    }
}
