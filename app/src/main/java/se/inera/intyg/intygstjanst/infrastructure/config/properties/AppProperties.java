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
package se.inera.intyg.intygstjanst.infrastructure.config.properties;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Immutable configuration record for all custom application properties under the {@code app.*}
 * prefix.
 *
 * <p>Binding is performed by Spring Boot's relaxed binding (kebab-case YAML → camelCase record
 * components). Validation runs at application-context startup; a misconfigured property causes a
 * fast, descriptive failure.
 *
 * <p>Legacy flat property names (e.g. {@code internal.api.port}, {@code ntjp.ws.certificate.file})
 * are kept as aliases in {@code application.yml} pointing to the canonical {@code app.*} values, so
 * all existing {@code @Value} injection sites continue to work until they are migrated to inject
 * {@link AppProperties} directly (Phases 4–5).
 */
@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
    @Valid Server server,
    @Valid Ntjp ntjp,
    @Valid Jms jms,
    @Valid Integration integration,
    @Valid Diagnosis diagnosis,
    @Valid Recipients recipients,
    @Valid Security security,
    @Valid Erase erase) {

  public record Server(@Positive int internalPort) {}

  public record Ntjp(
      @NotBlank String baseUrl,
      @Valid Tls tls,
      @Valid Endpoints endpoints,
      @NotBlank String tsBasRegisterCertificateVersion) {

    public record Tls(
        @NotBlank String certificateFile,
        @NotBlank String certificatePassword,
        @NotBlank String certificateType,
        @NotBlank String keyManagerPassword,
        @NotBlank String truststoreFile,
        @NotBlank String truststorePassword,
        @NotBlank String truststoreType) {}

    public record Endpoints(
        @NotBlank String registerCertificateV1,
        @NotBlank String registerCertificateV3,
        @NotBlank String registerMedicalCertificateV3,
        @NotBlank String revokeCertificateV2,
        @NotBlank String revokeMedicalCertificateV1,
        @NotBlank String sendMedicalCertificateQuestionV1,
        @NotBlank String sendMessageToCareV2,
        @NotBlank String sendMessageToRecipientV2) {}
  }

  public record Jms(
      @NotBlank String statisticsQueue,
      @NotBlank String internalNotificationQueue,
      @NotBlank String certificateEventQueue,
      boolean statisticsEnabled) {}

  public record Integration(
      @Valid CertificateService certificateService, @Valid IntygProxyService intygProxyService) {

    public record CertificateService(@NotBlank String baseUrl) {}

    public record IntygProxyService(
        @NotBlank String baseUrl, @Valid Hsa hsa, @Valid Pu pu, @Valid Cache cache) {

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
  }

  public record Diagnosis(
      @NotBlank String chaptersFile, @NotBlank String icd10seFile, @NotBlank String ksh97pFile) {}

  public record Recipients(@NotBlank String file, @NotBlank String updateCron) {}

  public record Security(@NotBlank String hashSalt) {}

  public record Erase(@Positive int pageSize) {}
}
