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

package se.inera.intyg.intygstjanst.persistence.model.dao.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.intygstjanst.persistence.config.JpaConstants;
import se.inera.intyg.intygstjanst.persistence.model.dao.Reko;
import se.inera.intyg.intygstjanst.persistence.model.dao.RekoRepository;

class RekoRepositoryTest extends TestSupport {

    private static final String PATIENT_ID_1 = "patientId1";
    private static final String PATIENT_ID_2 = "patientId2";
    private static final String STATUS = "status";
    private static final String STAFF_ID = "staffId";
    private static final String STAFF_NAME = "staffName";
    private static final String CARE_PROVIDER_ID = "careProviderId";
    private static final String CARE_UNIT_ID_1 = "careUnitId1";
    private static final String CARE_UNIT_ID_2 = "careUnitId2";
    @PersistenceContext(unitName = JpaConstants.PERSISTANCE_UNIT_NAME)
    private EntityManager entityManager;

    @Autowired
    private RekoRepository rekoRepository;

    @Test
    void shouldFindByPatientId() {
        final var expectedResult = getReko(PATIENT_ID_1, CARE_UNIT_ID_1, LocalDateTime.now());
        final var rekoWithWrongPatientId = getReko(PATIENT_ID_2, CARE_UNIT_ID_1, LocalDateTime.now());
        rekoRepository.save(expectedResult);
        rekoRepository.save(rekoWithWrongPatientId);

        final var result = rekoRepository.findByPatientId(expectedResult.getPatientId());

        assertEquals(1, result.size());
        assertEquals(expectedResult, result.getFirst());
    }

    @Test
    void shouldFindByPatientIds() {
        final var expectedResult = List.of(
            getReko(PATIENT_ID_1, CARE_UNIT_ID_1, LocalDateTime.now()),
            getReko(PATIENT_ID_1, CARE_UNIT_ID_1, LocalDateTime.now()),
            getReko(PATIENT_ID_2, CARE_UNIT_ID_1, LocalDateTime.now())
        );
        rekoRepository.save(expectedResult.get(0));
        rekoRepository.save(expectedResult.get(1));
        rekoRepository.save(expectedResult.get(2));

        final var actualResult = rekoRepository.findByPatientIdIn(List.of(PATIENT_ID_1));

        assertEquals(expectedResult.get(0), actualResult.get(0));
        assertEquals(expectedResult.get(1), actualResult.get(1));
        assertEquals(2, actualResult.size());
    }

    @Test
    void shouldFindByPatientIdsAndCareUnitId() {
        final var expectedResult =
            List.of(
                getReko(PATIENT_ID_1, CARE_UNIT_ID_1, LocalDateTime.now()),
                getReko(PATIENT_ID_1, CARE_UNIT_ID_2, LocalDateTime.now()),
                getReko(PATIENT_ID_2, CARE_UNIT_ID_1, LocalDateTime.now()
                )
            );

        rekoRepository.save(expectedResult.get(0));
        rekoRepository.save(expectedResult.get(1));
        rekoRepository.save(expectedResult.get(2));
        final var actualResult = rekoRepository.findByPatientIdInAndCareUnitId(
            List.of(
                PATIENT_ID_1
            ),
            CARE_UNIT_ID_1
        );
        assertEquals(expectedResult.get(0), actualResult.getFirst());
        assertEquals(1, actualResult.size());
    }

    @Test
    void shouldFindByPatientIdAndCareUnitId() {
        final var rekoStatuses =
            List.of(
                getReko(PATIENT_ID_1, CARE_UNIT_ID_1, LocalDateTime.now()),
                getReko(PATIENT_ID_1, CARE_UNIT_ID_2, LocalDateTime.now()),
                getReko(PATIENT_ID_2, CARE_UNIT_ID_1, LocalDateTime.now()
                )
            );

        rekoRepository.save(rekoStatuses.get(0));
        rekoRepository.save(rekoStatuses.get(1));
        rekoRepository.save(rekoStatuses.get(2));
        final var response = rekoRepository.findByPatientIdAndCareUnitId(PATIENT_ID_1, CARE_UNIT_ID_1);
        assertEquals(rekoStatuses.get(0), response.getFirst());
        assertEquals(1, response.size());
    }

    private static Reko getReko(String patientId, String careUnitId, LocalDateTime sickLeaveTimestamp) {
        final var reko = new Reko();
        reko.setPatientId(patientId);
        reko.setStatus(STATUS);
        reko.setStaffId(STAFF_ID);
        reko.setStaffName(STAFF_NAME);
        reko.setCareProviderId(CARE_PROVIDER_ID);
        reko.setCareUnitId(careUnitId);
        reko.setRegistrationTimestamp(sickLeaveTimestamp);
        reko.setSickLeaveTimestamp(sickLeaveTimestamp);
        return reko;
    }
}
