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

package se.inera.intyg.intygstjanst.infrastructure.config;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.inera.intyg.common.ts_parent.integration.RegisterCertificateV1Client;
import se.inera.intyg.common.ts_parent.integration.RegisterCertificateV3Client;
import se.inera.intyg.common.ts_parent.integration.SendTSClient;
import se.inera.intyg.common.ts_parent.integration.SendTSClientFactory;
import se.inera.intyg.intygstjanst.infrastructure.config.properties.AppProperties;

@Configuration
@RequiredArgsConstructor
public class TsBasItCxfConfig {

  private final AppProperties appProperties;

  @Bean("tsBasRegisterCertificateVersion")
  public String tsBasRegisterCertificateVersion() {
    return appProperties.ntjp().tsBasRegisterCertificateVersion();
  }

  @Bean("tsBasRegisterCertificateV1Client")
  public RegisterCertificateV1Client tsBasRegisterCertificateV1Client() {
    return new RegisterCertificateV1Client(
        appProperties.ntjp().endpoints().registerCertificateV1());
  }

  @Bean("tsBasRegisterCertificateV3Client")
  public RegisterCertificateV3Client tsBasRegisterCertificateV3Client() {
    return new RegisterCertificateV3Client(
        appProperties.ntjp().endpoints().registerCertificateV3());
  }

  @Bean
  public SendTSClientFactory sendTSClientFactory(
      @Qualifier("tsBasRegisterCertificateV1Client") RegisterCertificateV1Client v1Client,
      @Qualifier("tsBasRegisterCertificateV3Client") RegisterCertificateV3Client v3Client) {
    final Map<String, SendTSClient> clientMap = new HashMap<>();
    clientMap.put("v1", v1Client);
    clientMap.put("v3", v3Client);
    return new SendTSClientFactory(clientMap);
  }
}
