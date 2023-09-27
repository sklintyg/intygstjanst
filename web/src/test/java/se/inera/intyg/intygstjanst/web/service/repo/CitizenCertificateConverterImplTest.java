/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.service.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationType;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificateRelationConverter;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificateConverterImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitizenCertificateConverterImplTest {

    @Mock
    CitizenCertificateRelationConverter citizenCertificateRelationConverter;

    @InjectMocks
    CitizenCertificateConverterImpl citizenCertificateConverter;

    private static final String CERTIFICATE_ID = "Id";
    private static final String TYPE = "Type";
    private static final String TYPE_VERSION = "Type version";
    private static final String ISSUER = "Doctor name";
    private static final String SUMMARY = "Summary";
    private static final LocalDateTime DATE = LocalDateTime.now();
    private static final List<Relation> relations = Collections.emptyList();
    private static Certificate certificate;

    @BeforeEach
    void setup() {
        certificate = mock(Certificate.class);
        Mockito.when(certificate.getId()).thenReturn(CERTIFICATE_ID);
        Mockito.when(certificate.getType()).thenReturn(TYPE);
        Mockito.when(certificate.getTypeVersion()).thenReturn(TYPE_VERSION);
        Mockito.when(certificate.getSigningDoctorName()).thenReturn(ISSUER);
        Mockito.when(certificate.getSignedDate()).thenReturn(DATE);
        Mockito.when(certificate.getAdditionalInfo()).thenReturn(SUMMARY);
    }
    @Test
    void shouldConvertId() {
        final var response = citizenCertificateConverter.convert(certificate, relations);

        assertEquals(CERTIFICATE_ID, response.getId());
    }

    @Test
    void shouldConvertType() {
        final var response = citizenCertificateConverter.convert(certificate, relations);

        assertEquals(TYPE, response.getType());
    }

    @Test
    void shouldConvertTypeVersion() {
        final var response = citizenCertificateConverter.convert(certificate, relations);

        assertEquals(TYPE_VERSION, response.getTypeVersion());
    }

    @Test
    void shouldConvertSentDateWhenNull() {
        certificate.setStates(null);
        final var response = citizenCertificateConverter.convert(certificate, relations);

        assertNull(response.getSentDate());
    }

    @Test
    void shouldConvertSentDateWhenSent() {
        final var timeStamp = LocalDateTime.now();
        when(certificate.getStates()).thenReturn(List.of(
                new CertificateStateHistoryEntry("Id", CertificateState.SENT, timeStamp))
        );
        final var response = citizenCertificateConverter.convert(certificate, relations);

        assertEquals(timeStamp, response.getSentDate());
    }

    @Test
    void shouldConvertSentDateWhenNotSent() {
        final var timeStamp = LocalDateTime.now();
        when(certificate.getStates()).thenReturn(List.of(
                new CertificateStateHistoryEntry("Id", CertificateState.CANCELLED, timeStamp))
        );
        final var response = citizenCertificateConverter.convert(certificate, relations);

        assertNull(response.getSentDate());
    }


    @Test
    void shouldConvertIssuer() {
        final var response = citizenCertificateConverter.convert(certificate, relations);

        assertEquals(ISSUER, response.getIssuerName());
    }

    @Test
    void shouldConvertIssuedDate() {
        final var response = citizenCertificateConverter.convert(certificate, relations);

        assertEquals(DATE, response.getIssued());
    }

    @Test
    void shouldConvertSummary() {
        final var response = citizenCertificateConverter.convert(certificate, relations);

        assertEquals(SUMMARY, response.getAdditionalInfo());
    }

    @Test
    void shouldFilterNullValuesOfRelation() {
        final var relation = new Relation("wrongId", "wrongId", "code", LocalDateTime.now());
        final var response = citizenCertificateConverter.convert(certificate, List.of(relation));
        assertTrue(response.getRelations().isEmpty());
    }

    @Nested
    class ConversionOfRelationValues {
        CitizenCertificateRelationDTO expectedRelation;

        Relation relation = new Relation("fromId", "toId", "NotErsatt", LocalDateTime.now());
        Relation complementedRelation = new Relation("fromId", "toId", "KOMPLT", LocalDateTime.now());
        Relation replacedRelation = new Relation("fromId", "toId", "ERSATT", LocalDateTime.now());

        @BeforeEach
        void setup() {

            expectedRelation = CitizenCertificateRelationDTO.builder()
                .certificateId(CERTIFICATE_ID)
                .type(CitizenCertificateRelationType.REPLACED)
                .timestamp(LocalDateTime.now())
                .build();

            when(citizenCertificateRelationConverter.convert(anyString(), any(Relation.class)))
                    .thenReturn(Optional.of(expectedRelation));
        }

        @Test
        void shouldConvertRelation() {
            final var response = citizenCertificateConverter.convert(certificate, List.of(relation,
                replacedRelation));
            assertEquals(expectedRelation, response.getRelations().get(0));
        }

        @Test
        void shouldConvertComplementedRelation() {
            final var response = citizenCertificateConverter.convert(
                certificate, List.of(relation, complementedRelation)
            );
            assertEquals(expectedRelation, response.getRelations().get(0));
        }

        @Test
        void shouldSendIdToConverter() {
            citizenCertificateConverter.convert(certificate, List.of(relation, replacedRelation));
            final var captor = ArgumentCaptor.forClass(String.class);

            verify(citizenCertificateRelationConverter, times(1)).convert(
                    captor.capture(), any()
            );
            assertEquals(certificate.getId(), captor.getValue());
        }

        @Test
        void shouldSendRelationToConverter() {
            citizenCertificateConverter.convert(certificate, List.of(relation, replacedRelation));
            final var captor = ArgumentCaptor.forClass(Relation.class);

            verify(citizenCertificateRelationConverter, times(1)).convert(
                    anyString(), captor.capture()
            );
            assertEquals(replacedRelation, captor.getValue());
        }
    }
}