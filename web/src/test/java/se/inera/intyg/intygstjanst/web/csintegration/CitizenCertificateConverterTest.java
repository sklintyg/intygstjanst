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

package se.inera.intyg.intygstjanst.web.csintegration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateIssuerDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRecipientDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationType;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateSummaryDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateUnitDTO;

class CitizenCertificateConverterTest {

    private static final String ID = "certificateId";
    private static final String TYPE = "type";
    private static final String NAME = "certificateName";
    private static final String TYPE_VERSION = "typeVersion";
    private static final String UNIT_ID = "unitId";
    private static final String UNIT_NAME = "unitName";
    private static final String FULL_NAME = "fullName";
    private static final String SUMMARY_LABEL = "summaryLabel";
    private static final String SUMMARY_VALUE = "summaryValue";
    private static final String RECIPIENT_ID = "recipientId";
    private static final String RECIPIENT_NAME = "recipientName";
    private CitizenCertificateConverter citizenCertificateConverter;
    private Certificate certificate;
    private CertificateMetadata.CertificateMetadataBuilder certificateMetadataBuilder;

    @BeforeEach
    void setUp() {
        citizenCertificateConverter = new CitizenCertificateConverter();
        certificate = new Certificate();

        certificateMetadataBuilder = CertificateMetadata.builder()
            .id(ID)
            .name(NAME)
            .type(TYPE)
            .typeVersion(TYPE_VERSION)
            .signed(LocalDateTime.now())
            .modified(LocalDateTime.now().plusDays(5))
            .unit(
                Unit.builder()
                    .unitId(UNIT_ID)
                    .unitName(UNIT_NAME)
                    .build()
            )
            .issuedBy(Staff.builder()
                .fullName(FULL_NAME)
                .build()
            )
            .summary(
                CertificateSummary.builder()
                    .label(SUMMARY_LABEL)
                    .value(SUMMARY_VALUE)
                    .build()
            )
            .recipient(
                CertificateRecipient.builder()
                    .id(RECIPIENT_ID)
                    .name(RECIPIENT_NAME)
                    .sent(LocalDateTime.now())
                    .build()
            );

        certificate.setMetadata(
            certificateMetadataBuilder.build()
        );
    }

    @Test
    void shallIncludeId() {
        assertEquals(ID, citizenCertificateConverter.convert(certificate).getId());
    }

    @Test
    void shallIncludeType() {
        final var expectedType = CitizenCertificateTypeDTO.builder()
            .id(TYPE)
            .name(NAME)
            .version(TYPE_VERSION)
            .build();

        assertEquals(expectedType, citizenCertificateConverter.convert(certificate).getType());
    }

    @Test
    void shallIncludeUnit() {
        final var expectedUnit = CitizenCertificateUnitDTO.builder()
            .id(UNIT_ID)
            .name(UNIT_NAME)
            .build();
        assertEquals(expectedUnit, citizenCertificateConverter.convert(certificate).getUnit());
    }

    @Test
    void shallIncludeIssued() {
        assertNotNull(citizenCertificateConverter.convert(certificate).getIssued());
    }


    @Test
    void shallIncludeIssuer() {
        final var expectedIssuer = CitizenCertificateIssuerDTO.builder()
            .name(FULL_NAME)
            .build();
        assertEquals(expectedIssuer, citizenCertificateConverter.convert(certificate).getIssuer());
    }

    @Test
    void shallIncludeSummary() {
        final var expectedSummary = CitizenCertificateSummaryDTO.builder()
            .label(SUMMARY_LABEL)
            .value(SUMMARY_VALUE)
            .build();
        assertEquals(expectedSummary, citizenCertificateConverter.convert(certificate).getSummary());
    }

    @Test
    void shallIncludeRecipientId() {
        assertEquals(RECIPIENT_ID, citizenCertificateConverter.convert(certificate).getRecipient().getId());
    }

    @Test
    void shallNotIncludeRecipientIfNoRecipient() {
        certificateMetadataBuilder.recipient(null);
        certificate.setMetadata(certificateMetadataBuilder.build());
        assertNull(citizenCertificateConverter.convert(certificate).getRecipient());
    }

    @Test
    void shallIncludeRecipientName() {
        assertEquals(RECIPIENT_NAME, citizenCertificateConverter.convert(certificate).getRecipient().getName());
    }

    @Test
    void shallIncludeRecipientSent() {
        assertNotNull(citizenCertificateConverter.convert(certificate).getRecipient().getSent());
    }

    @Nested
    class RelationsTests {

        private static final String REPLACED_CERTIFICATE_ID = "replacedCertificateId";

        @Test
        void shallReturnEmptyRelationsIfMissing() {
            assertTrue(citizenCertificateConverter.convert(certificate).getRelations().isEmpty());
        }

        @Test
        void shallIncludeRelationReplaced() {
            certificateMetadataBuilder.relations(
                CertificateRelations.builder()
                    .children(
                        new CertificateRelation[]{
                            CertificateRelation.builder()
                                .certificateId(REPLACED_CERTIFICATE_ID)
                                .created(LocalDateTime.now())
                                .status(CertificateStatus.SIGNED)
                                .type(CertificateRelationType.REPLACED)
                                .build()
                        }
                    )
                    .build()
            );
            certificate.setMetadata(certificateMetadataBuilder.build());
            assertEquals(CitizenCertificateRelationType.REPLACED,
                citizenCertificateConverter.convert(certificate).getRelations().get(0).getType());
        }

        @Test
        void shallFilterChildRelationIfNotSigned() {
            certificateMetadataBuilder.relations(
                CertificateRelations.builder()
                    .children(
                        new CertificateRelation[]{
                            CertificateRelation.builder()
                                .certificateId(REPLACED_CERTIFICATE_ID)
                                .created(LocalDateTime.now())
                                .status(CertificateStatus.REVOKED)
                                .type(CertificateRelationType.REPLACED)
                                .build()
                        }
                    )
                    .build()
            );
            certificate.setMetadata(certificateMetadataBuilder.build());
            assertTrue(citizenCertificateConverter.convert(certificate).getRelations().isEmpty());
        }

        @Test
        void shallFilterChildRelationIfNotReplacedOrComplemented() {
            certificateMetadataBuilder.relations(
                CertificateRelations.builder()
                    .children(
                        new CertificateRelation[]{
                            CertificateRelation.builder()
                                .certificateId(REPLACED_CERTIFICATE_ID)
                                .created(LocalDateTime.now())
                                .status(CertificateStatus.SIGNED)
                                .type(CertificateRelationType.COPIED)
                                .build()
                        }
                    )
                    .build()
            );
            certificate.setMetadata(certificateMetadataBuilder.build());
            assertTrue(citizenCertificateConverter.convert(certificate).getRelations().isEmpty());
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
                            .build()
                    )
                    .build()
            );
            certificate.setMetadata(certificateMetadataBuilder.build());
            assertEquals(CitizenCertificateRelationType.REPLACES,
                citizenCertificateConverter.convert(certificate).getRelations().get(0).getType());
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
                            .build()
                    )
                    .build()
            );
            certificate.setMetadata(certificateMetadataBuilder.build());
            assertTrue(citizenCertificateConverter.convert(certificate).getRelations().isEmpty());
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
                            .build()
                    )
                    .build()
            );
            certificate.setMetadata(certificateMetadataBuilder.build());
            assertTrue(citizenCertificateConverter.convert(certificate).getRelations().isEmpty());
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
                            .build()
                    )
                    .children(
                        new CertificateRelation[]{
                            CertificateRelation.builder()
                                .certificateId(REPLACED_CERTIFICATE_ID)
                                .created(LocalDateTime.now())
                                .status(CertificateStatus.SIGNED)
                                .type(CertificateRelationType.REPLACED)
                                .build()
                        }
                    )
                    .build()
            );
            certificate.setMetadata(certificateMetadataBuilder.build());
            assertEquals(CitizenCertificateRelationType.REPLACES,
                citizenCertificateConverter.convert(certificate).getRelations().get(0).getType());
            assertEquals(CitizenCertificateRelationType.REPLACED,
                citizenCertificateConverter.convert(certificate).getRelations().get(1).getType());
        }
    }
}
