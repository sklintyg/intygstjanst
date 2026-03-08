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
