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

package se.inera.intyg.intygstjanst.infrastructure.csintegration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.Staff;
import se.inera.intyg.common.support.facade.model.metadata.CertificateMetadata;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRecipient;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelations;
import se.inera.intyg.common.support.facade.model.metadata.CertificateSummary;
import se.inera.intyg.common.support.facade.model.metadata.Unit;
import se.inera.intyg.common.support.modules.registry.IntygModuleRegistry;
import se.inera.intyg.common.support.modules.support.ModuleEntryPoint;
import se.inera.intyg.intygstjanst.application.citizen.dto.CitizenCertificateIssuerDTO;
import se.inera.intyg.intygstjanst.application.citizen.dto.CitizenCertificateRelationType;
import se.inera.intyg.intygstjanst.application.citizen.dto.CitizenCertificateSummaryDTO;
import se.inera.intyg.intygstjanst.application.citizen.dto.CitizenCertificateTypeDTO;
import se.inera.intyg.intygstjanst.application.citizen.dto.CitizenCertificateUnitDTO;

@ExtendWith(MockitoExtension.class)
class CitizenCertificateConverterFromCSTest {

  private static final String ID = "certificateId";
  private static final String TYPE = "type";
  private static final String MAPPED_TYPE = "mappedType";
  private static final String NAME = "certificateName";
  private static final String TYPE_VERSION = "typeVersion";
  private static final String UNIT_ID = "unitId";
  private static final String UNIT_NAME = "unitName";
  private static final String FULL_NAME = "fullName";
  private static final String SUMMARY_LABEL = "summaryLabel";
  private static final String SUMMARY_VALUE = "summaryValue";
  private static final String RECIPIENT_ID = "recipientId";
  private static final String RECIPIENT_NAME = "recipientName";
  private Certificate certificate;
  private CertificateMetadata.CertificateMetadataBuilder certificateMetadataBuilder;

  @Mock private IntygModuleRegistry intygModuleRegistry;
  @InjectMocks private CitizenCertificateConverterFromCS citizenCertificateConverterFromCS;

  @BeforeEach
  void setUp() {
    certificate = new Certificate();

    certificateMetadataBuilder =
        CertificateMetadata.builder()
            .id(ID)
            .name(NAME)
            .type(TYPE)
            .typeVersion(TYPE_VERSION)
            .signed(LocalDateTime.now())
            .modified(LocalDateTime.now().plusDays(5))
            .unit(Unit.builder().unitId(UNIT_ID).unitName(UNIT_NAME).build())
            .issuedBy(Staff.builder().fullName(FULL_NAME).build())
            .summary(CertificateSummary.builder().label(SUMMARY_LABEL).value(SUMMARY_VALUE).build())
            .recipient(
                CertificateRecipient.builder()
                    .id(RECIPIENT_ID)
                    .name(RECIPIENT_NAME)
                    .sent(LocalDateTime.now())
                    .build());

    certificate.setMetadata(certificateMetadataBuilder.build());

    when(intygModuleRegistry.getModuleEntryPoints()).thenReturn(List.of());
  }

  @Test
  void shallIncludeId() {
    assertEquals(ID, citizenCertificateConverterFromCS.convert(certificate).getId());
  }

  @Test
  void shallIncludeType() {
    final var expectedType =
        CitizenCertificateTypeDTO.builder().id(TYPE).name(NAME).version(TYPE_VERSION).build();

    assertEquals(expectedType, citizenCertificateConverterFromCS.convert(certificate).getType());
  }

  @Test
  void shallIncludeMappedType() {
    final var expectedType =
        CitizenCertificateTypeDTO.builder()
            .id(MAPPED_TYPE)
            .name(NAME)
            .version(TYPE_VERSION)
            .build();

    final var mockedEntryPoint = mock(ModuleEntryPoint.class);
    when(mockedEntryPoint.getModuleId()).thenReturn(MAPPED_TYPE);
    when(mockedEntryPoint.certificateServiceTypeId()).thenReturn(TYPE);
    when(intygModuleRegistry.getModuleEntryPoints()).thenReturn(List.of(mockedEntryPoint));

    assertEquals(expectedType, citizenCertificateConverterFromCS.convert(certificate).getType());
  }

  @Test
  void shallIncludeUnit() {
    final var expectedUnit =
        CitizenCertificateUnitDTO.builder().id(UNIT_ID).name(UNIT_NAME).build();
    assertEquals(expectedUnit, citizenCertificateConverterFromCS.convert(certificate).getUnit());
  }

  @Test
  void shallIncludeIssued() {
    assertNotNull(citizenCertificateConverterFromCS.convert(certificate).getIssued());
  }

  @Test
  void shallIncludeIssuer() {
    final var expectedIssuer = CitizenCertificateIssuerDTO.builder().name(FULL_NAME).build();
    assertEquals(
        expectedIssuer, citizenCertificateConverterFromCS.convert(certificate).getIssuer());
  }

  @Test
  void shallIncludeSummary() {
    final var expectedSummary =
        CitizenCertificateSummaryDTO.builder().label(SUMMARY_LABEL).value(SUMMARY_VALUE).build();
    assertEquals(
        expectedSummary, citizenCertificateConverterFromCS.convert(certificate).getSummary());
  }

  @Test
  void shallIncludeRecipientId() {
    assertEquals(
        RECIPIENT_ID,
        citizenCertificateConverterFromCS.convert(certificate).getRecipient().getId());
  }

  @Test
  void shallNotIncludeRecipientIfNoRecipient() {
    certificateMetadataBuilder.recipient(null);
    certificate.setMetadata(certificateMetadataBuilder.build());
    assertNull(citizenCertificateConverterFromCS.convert(certificate).getRecipient());
  }

  @Test
  void shallIncludeRecipientName() {
    assertEquals(
        RECIPIENT_NAME,
        citizenCertificateConverterFromCS.convert(certificate).getRecipient().getName());
  }

  @Test
  void shallIncludeRecipientSent() {
    assertNotNull(citizenCertificateConverterFromCS.convert(certificate).getRecipient().getSent());
  }

  @Nested
  class RelationsTests {

    private static final String REPLACED_CERTIFICATE_ID = "replacedCertificateId";

    @Test
    void shallReturnEmptyRelationsIfMissing() {
      assertTrue(citizenCertificateConverterFromCS.convert(certificate).getRelations().isEmpty());
    }

    @Test
    void shallIncludeRelationReplaced() {
      certificateMetadataBuilder.relations(
          CertificateRelations.builder()
              .children(
                  new CertificateRelation[] {
                    CertificateRelation.builder()
                        .certificateId(REPLACED_CERTIFICATE_ID)
                        .created(LocalDateTime.now())
                        .status(CertificateStatus.SIGNED)
                        .type(CertificateRelationType.REPLACED)
                        .build()
                  })
              .build());
      certificate.setMetadata(certificateMetadataBuilder.build());
      assertEquals(
          CitizenCertificateRelationType.REPLACED,
          citizenCertificateConverterFromCS
              .convert(certificate)
              .getRelations()
              .getFirst()
              .getType());
    }

    @Test
    void shallFilterChildRelationIfNotSigned() {
      certificateMetadataBuilder.relations(
          CertificateRelations.builder()
              .children(
                  new CertificateRelation[] {
                    CertificateRelation.builder()
                        .certificateId(REPLACED_CERTIFICATE_ID)
                        .created(LocalDateTime.now())
                        .status(CertificateStatus.REVOKED)
                        .type(CertificateRelationType.REPLACED)
                        .build()
                  })
              .build());
      certificate.setMetadata(certificateMetadataBuilder.build());
      assertTrue(citizenCertificateConverterFromCS.convert(certificate).getRelations().isEmpty());
    }

    @Test
    void shallFilterChildRelationIfNotReplacedOrComplemented() {
      certificateMetadataBuilder.relations(
          CertificateRelations.builder()
              .children(
                  new CertificateRelation[] {
                    CertificateRelation.builder()
                        .certificateId(REPLACED_CERTIFICATE_ID)
                        .created(LocalDateTime.now())
                        .status(CertificateStatus.SIGNED)
                        .type(CertificateRelationType.COPIED)
                        .build()
                  })
              .build());
      certificate.setMetadata(certificateMetadataBuilder.build());
      assertTrue(citizenCertificateConverterFromCS.convert(certificate).getRelations().isEmpty());
    }

    @Test
    void shallIncludeRelationReplaces() {
      certificateMetadataBuilder.relations(
          CertificateRelations.builder()
              .parent(
                  CertificateRelation.builder()
                      .certificateId(REPLACED_CERTIFICATE_ID)
                      .created(LocalDateTime.now())
                      .status(CertificateStatus.SIGNED)
                      .type(CertificateRelationType.REPLACED)
                      .build())
              .build());
      certificate.setMetadata(certificateMetadataBuilder.build());
      assertEquals(
          CitizenCertificateRelationType.REPLACES,
          citizenCertificateConverterFromCS
              .convert(certificate)
              .getRelations()
              .getFirst()
              .getType());
    }

    @Test
    void shallFilterParentRelationIfNotSigned() {
      certificateMetadataBuilder.relations(
          CertificateRelations.builder()
              .parent(
                  CertificateRelation.builder()
                      .certificateId(REPLACED_CERTIFICATE_ID)
                      .created(LocalDateTime.now())
                      .status(CertificateStatus.REVOKED)
                      .type(CertificateRelationType.REPLACED)
                      .build())
              .build());
      certificate.setMetadata(certificateMetadataBuilder.build());
      assertTrue(citizenCertificateConverterFromCS.convert(certificate).getRelations().isEmpty());
    }

    @Test
    void shallFilterParentRelationIfNotReplacedOrComplemented() {
      certificateMetadataBuilder.relations(
          CertificateRelations.builder()
              .parent(
                  CertificateRelation.builder()
                      .certificateId(REPLACED_CERTIFICATE_ID)
                      .created(LocalDateTime.now())
                      .status(CertificateStatus.SIGNED)
                      .type(CertificateRelationType.COPIED)
                      .build())
              .build());
      certificate.setMetadata(certificateMetadataBuilder.build());
      assertTrue(citizenCertificateConverterFromCS.convert(certificate).getRelations().isEmpty());
    }

    @Test
    void shallIncludeRelationsReplacesAndReplaced() {
      certificateMetadataBuilder.relations(
          CertificateRelations.builder()
              .parent(
                  CertificateRelation.builder()
                      .certificateId(REPLACED_CERTIFICATE_ID)
                      .created(LocalDateTime.now())
                      .status(CertificateStatus.SIGNED)
                      .type(CertificateRelationType.REPLACED)
                      .build())
              .children(
                  new CertificateRelation[] {
                    CertificateRelation.builder()
                        .certificateId(REPLACED_CERTIFICATE_ID)
                        .created(LocalDateTime.now())
                        .status(CertificateStatus.SIGNED)
                        .type(CertificateRelationType.REPLACED)
                        .build()
                  })
              .build());
      certificate.setMetadata(certificateMetadataBuilder.build());
      assertEquals(
          CitizenCertificateRelationType.REPLACES,
          citizenCertificateConverterFromCS.convert(certificate).getRelations().get(0).getType());
      assertEquals(
          CitizenCertificateRelationType.REPLACED,
          citizenCertificateConverterFromCS.convert(certificate).getRelations().get(1).getType());
    }
  }
}
