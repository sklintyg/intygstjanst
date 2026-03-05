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
package se.inera.intyg.intygstjanst.config;


import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistryImpl;
import se.inera.intyg.common.support.modules.support.ApplicationOrigin;
import se.inera.intyg.infra.security.filter.InternalApiFilter;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineService;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineServiceImpl;

@Configuration
@EnableCaching
@EnableAspectJAutoProxy
public class ApplicationConfig {

    @Bean(name = Bus.DEFAULT_BUS_ID)
    public SpringBus springBus() {
        return new SpringBus();
    }

    @Bean
    public PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver() {
        return new PathMatchingResourcePatternResolver();
    }

    @Bean
    public SjukfallEngineService sjukfallEngineService() {
        return new SjukfallEngineServiceImpl();
    }

    @Bean
    public InternalApiFilter internalApiFilter() {
        return new InternalApiFilter();
    }

    @Bean
    public IntygModuleRegistryImpl moduleRegistry() {
        final var registry = new IntygModuleRegistryImpl();
        registry.setOrigin(ApplicationOrigin.INTYGSTJANST);
        return registry;
    }
}
