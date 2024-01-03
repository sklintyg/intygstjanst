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

package se.inera.intyg.intygstjanst.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRecipientConverter;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateIssuerDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRecipientDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateSummaryDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateUnitDTO;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificate;

@ExtendWith(MockitoExtension.class)
class CitizenCertificateDTOConverterImplTest {

    @Mock
    private CitizenCertificateRecipientConverter citizenCertificateRecipientConverter;

    @InjectMocks
    private CitizenCertificateDTOConverterImpl citizenCertificateDTOConverter;

    private static final String CERTIFICATE_ID = "Id";
    private static final String TYPE_NAME = "Type";
    private static final String CERTIFICATE_TYPE = "Certificate type";
    private static final String TYPE_VERSION = "Type version";
    private static final String SUMMARY_LABEL = "Summary label";
    private static final String SUMMARY_VALUE = "Summary value";
    private static final String ISSUER_NAME = "Issuer name";
    private static final String UNIT_ID = "Unit id";
    private static final String UNIT_NAME = "Unit name";
    private static final LocalDateTime ISSUED_DATE = LocalDateTime.now();
    private static final String RECIPIENT_NAME = "Recipient name";
    private static final LocalDateTime SENT_DATE = LocalDateTime.now();
    private static final List<CitizenCertificateRelationDTO> RELATIONS = Collections.emptyList();

    private CitizenCertificate getCitizenCertificate() {
        return CitizenCertificate.builder()
            .id(CERTIFICATE_ID)
            .issued(ISSUED_DATE)
            .type(CERTIFICATE_TYPE)
            .typeVersion(TYPE_VERSION)
            .additionalInfo(SUMMARY_VALUE)
            .issuerName(ISSUER_NAME)
            .unitId(UNIT_ID)
            .unitName(UNIT_NAME)
            .relations(RELATIONS)
            .sentDate(SENT_DATE)
            .build();
    }

    @Test
    void shouldConvertToCorrectFormatForId() {
        final var citizenCertificate = getCitizenCertificate();
        final var actualResult = citizenCertificateDTOConverter.convert(citizenCertificate, TYPE_NAME, SUMMARY_LABEL);

        assertEquals(CERTIFICATE_ID, actualResult.getId());
    }

    @Test
    void shouldConvertToCorrectFormatForType() {
        final var expectedType = CitizenCertificateTypeDTO.builder()
            .id(CERTIFICATE_TYPE)
            .name(TYPE_NAME)
            .version(TYPE_VERSION)
            .build();

        final var citizenCertificate = getCitizenCertificate();

        final var actualResult = citizenCertificateDTOConverter.convert(citizenCertificate, TYPE_NAME, SUMMARY_LABEL);
        assertEquals(expectedType, actualResult.getType());
    }

    @Test
    void shouldConvertToCorrectFormatForSummary() {
        final var expectedType = CitizenCertificateSummaryDTO.builder()
            .value(SUMMARY_VALUE)
            .label(SUMMARY_LABEL)
            .build();

        final var citizenCertificate = getCitizenCertificate();

        final var actualResult = citizenCertificateDTOConverter.convert(citizenCertificate, TYPE_NAME, SUMMARY_LABEL);
        assertEquals(expectedType, actualResult.getSummary());
    }

    @Test
    void shouldConvertToCorrectFormatForIssuer() {
        final var expectedType = CitizenCertificateIssuerDTO.builder()
            .name(ISSUER_NAME)
            .build();

        final var citizenCertificate = getCitizenCertificate();

        final var actualResult = citizenCertificateDTOConverter.convert(citizenCertificate, TYPE_NAME, SUMMARY_LABEL);
        assertEquals(expectedType, actualResult.getIssuer());
    }

    @Test
    void shouldConvertToCorrectFormatForUnit() {
        final var expectedType = CitizenCertificateUnitDTO.builder()
            .id(UNIT_ID)
            .name(UNIT_NAME)
            .build();

        final var citizenCertificate = getCitizenCertificate();

        final var actualResult = citizenCertificateDTOConverter.convert(citizenCertificate, TYPE_NAME, SUMMARY_LABEL);
        assertEquals(expectedType, actualResult.getUnit());
    }

    @Test
    void shouldConvertToCorrectFormatForRecipient() {
        final var expectedResult = CitizenCertificateRecipientDTO.builder()
            .id(CERTIFICATE_ID)
            .name(RECIPIENT_NAME)
            .sent(SENT_DATE)
            .build();

        final var citizenCertificate = getCitizenCertificate();

        when(citizenCertificateRecipientConverter.convert(citizenCertificate.getType(), citizenCertificate.getSentDate()))
            .thenReturn(Optional.of(expectedResult));

        final var actualResult = citizenCertificateDTOConverter.convert(citizenCertificate, TYPE_NAME, SUMMARY_LABEL);
        assertEquals(expectedResult, actualResult.getRecipient());
    }

    @Test
    void shouldConvertToCorrectFormatForIssued() {
        final var citizenCertificate = getCitizenCertificate();
        final var actualResult = citizenCertificateDTOConverter.convert(citizenCertificate, TYPE_NAME, SUMMARY_LABEL);

        assertEquals(ISSUED_DATE, actualResult.getIssued());
    }

    @Test
    void shouldConvertToCorrectFormatForRelations() {
        final var citizenCertificate = getCitizenCertificate();
        final var actualResult = citizenCertificateDTOConverter.convert(citizenCertificate, TYPE_NAME, SUMMARY_LABEL);

        assertEquals(RELATIONS, actualResult.getRelations());
    }
}
