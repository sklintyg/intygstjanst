/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.infrastructure.config;

import org.apache.cxf.transport.servlet.CXFServlet;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.DispatcherServletPath;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.DispatcherServlet;
import se.inera.intyg.intygstjanst.infrastructure.security.filter.InternalApiFilter;
import se.inera.intyg.intygstjanst.infrastructure.logging.MdcServletFilter;

/**
 * Replicates the servlet and filter layout from {@code web.xml} as Spring Boot
 * {@link ServletRegistrationBean}s and {@link FilterRegistrationBean}s.
 *
 * <p>URL routing is identical to the current WAR setup:</p>
 * <ul>
 *   <li>DispatcherServlet handles {@code /internalapi/*}, {@code /api/*}, {@code /resources/*}</li>
 *   <li>CXFServlet handles {@code /*} (catches all SOAP paths not matched above)</li>
 * </ul>
 *
 * <p>Under Gretty (WAR), {@code web.xml} still controls servlet/filter setup and these beans
 * are ignored. They only take effect when running under Spring Boot embedded Tomcat.</p>
 */
@Configuration
public class ServletConfig {

    /**
     * Provides the {@link DispatcherServletPath} bean required by {@code ErrorMvcAutoConfiguration}.
     * Since we register DispatcherServlet with explicit sub-paths (not at "/"), we must supply
     * this bean manually.
     */
    @Bean
    @Primary
    public DispatcherServletPath dispatcherServletPath() {
        return () -> "/";
    }

    /**
     * Overrides Spring Boot's default DispatcherServlet registration (which maps to {@code /}).
     * Instead, we map to the exact same sub-paths as the current {@code web.xml}, preserving
     * all existing REST endpoint URLs.
     */
    @Bean(name = DispatcherServletAutoConfiguration.DEFAULT_DISPATCHER_SERVLET_REGISTRATION_BEAN_NAME)
    public ServletRegistrationBean<DispatcherServlet> dispatcherServletRegistration(DispatcherServlet dispatcherServlet) {
        final var registration = new ServletRegistrationBean<>(dispatcherServlet,
            "/internalapi/*", "/api/*", "/resources/*");
        registration.setName("dispatcherServlet");
        registration.setLoadOnStartup(2);
        return registration;
    }

    /**
     * Registers the CXF servlet at {@code /*} — exactly as in {@code web.xml}.
     * Because {@code /internalapi/*}, {@code /api/*}, and {@code /resources/*} are more specific
     * path mappings, the servlet container routes those to DispatcherServlet first.
     * Everything else (SOAP paths) goes to CXF.
     */
    @Bean
    public ServletRegistrationBean<CXFServlet> cxfServletRegistration() {
        final var registration = new ServletRegistrationBean<>(new CXFServlet(), "/*");
        registration.setName("cxf");
        registration.setLoadOnStartup(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<InternalApiFilter> internalApiFilterRegistration(InternalApiFilter internalApiFilter) {
        final var registration = new FilterRegistrationBean<>(internalApiFilter);
        registration.addUrlPatterns("/internalapi/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registration.setName("internalApiFilter");
        return registration;
    }

    /**
     * Registers {@link MdcServletFilter} at {@code /*}, matching {@code web.xml}.
     * Populates MDC with session/trace/span IDs for every request.
     */
    @Bean
    public FilterRegistrationBean<MdcServletFilter> mdcServletFilterRegistration(MdcServletFilter mdcServletFilter) {
        final var registration = new FilterRegistrationBean<>(mdcServletFilter);
        registration.addUrlPatterns("/*");
        registration.setOrder(2);
        return registration;
    }
}