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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationType;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificateRelationConverterImpl;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CitizenCertificateRelationConverterImplTest {

    private static final String ID = "ID";
    private static final String CODE = RelationKod.ERSATT.toString();
    private static final String OTHER_ID = "OTHER_ID";
    private static final LocalDateTime TIMESTAMP = LocalDateTime.now();

    @InjectMocks
    CitizenCertificateRelationConverterImpl citizenCertificateRelationConverter;

    @Test
    void shouldSetCertificateIdAsFromIdIfIdIsTo() {
        final var relation = new Relation(OTHER_ID, ID, CODE, TIMESTAMP);
        final var response = citizenCertificateRelationConverter.convert(ID, relation);

        assertEquals(OTHER_ID, response.get().getCertificateId());
    }

    @Test
    void shouldSetCertificateIdAsToIdIfIdIsFrom() {
        final var relation = new Relation(ID, OTHER_ID, CODE, TIMESTAMP);
        final var response = citizenCertificateRelationConverter.convert(ID, relation);

        assertEquals(OTHER_ID, response.get().getCertificateId());
    }

    @Test
    void shouldSetTypeReplacedIfIdMatchesTo() {
        final var relation = new Relation(OTHER_ID, ID, CODE, TIMESTAMP);
        final var response = citizenCertificateRelationConverter.convert(ID, relation);

        assertEquals(CitizenCertificateRelationType.REPLACED, response.get().getType());
    }

    @Test
    void shouldSetTypeReplacesIfIdMatchesFrom() {
        final var relation = new Relation(ID, OTHER_ID, CODE, TIMESTAMP);
        final var response = citizenCertificateRelationConverter.convert(ID, relation);

        assertEquals(CitizenCertificateRelationType.REPLACES, response.get().getType());
    }

    @Test
    void shouldSetTimestamp() {
        final var relation = new Relation(ID, OTHER_ID, CODE, TIMESTAMP);
        final var response = citizenCertificateRelationConverter.convert(ID, relation);

        assertEquals(TIMESTAMP, response.get().getTimestamp());
    }

    @Test
    void shouldReturnOptionalEmptyIfIdDoesntMatch() {
        final var relation = new Relation(ID, OTHER_ID, CODE, TIMESTAMP);
        final var response = citizenCertificateRelationConverter.convert("NON_MATCHING_ID", relation);

        assertTrue(response.isEmpty());
    }
}