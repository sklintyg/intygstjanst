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
package se.inera.intyg.intygstjanst.integration.intygproxyservice.configuration;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Immutable configuration record for the Intyg Proxy Service integration module. Bound to the
 * {@code app.integration.intyg-proxy-service.*} prefix.
 */
@Validated
@ConfigurationProperties(prefix = "app.integration.intyg-proxy-service")
public record IntygProxyServiceProperties(
    String baseUrl, @Valid Hsa hsa, @Valid Pu pu, @Valid Cache cache) {

  public record Hsa(
      @NotBlank String employeeEndpoint,
      @NotBlank String credentialInformationEndpoint,
      @NotBlank String healthcareUnitEndpoint,
      @NotBlank String healthcareUnitMembersEndpoint,
      @NotBlank String unitEndpoint,
      @NotBlank String credentialsForPersonEndpoint,
      @NotBlank String certificationPersonEndpoint,
      @NotBlank String lastUpdateEndpoint,
      @NotBlank String healthcareProviderEndpoint) {}

  public record Pu(@NotBlank String personEndpoint, @NotBlank String personsEndpoint) {}

  public record Cache(
      @Positive long employeeTtlSeconds,
      @Positive long healthcareUnitTtlSeconds,
      @Positive long healthcareUnitMembersTtlSeconds,
      @Positive long unitTtlSeconds,
      @Positive long healthcareProviderTtlSeconds) {}
}
