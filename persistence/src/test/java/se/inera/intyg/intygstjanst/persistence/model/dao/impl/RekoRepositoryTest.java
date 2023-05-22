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

package se.inera.intyg.intygstjanst.persistence.model.dao.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.intygstjanst.persistence.config.JpaConstants;
import se.inera.intyg.intygstjanst.persistence.model.dao.Reko;
import se.inera.intyg.intygstjanst.persistence.model.dao.RekoRepository;

public class RekoRepositoryTest extends TestSupport {

    private static final String PATIENT_ID_1 = "patientId1";
    private static final String STATUS = "status";
    private static final String STAFF_ID = "staffId";
    private static final String STAFF_NAME = "staffName";
    private static final String CARE_PROVIDER_ID = "careProviderId";
    private static final String CARE_UNIT_ID = "careProviderId";
    @PersistenceContext(unitName = JpaConstants.PERSISTANCE_UNIT_NAME)
    private EntityManager entityManager;

    @Autowired
    private RekoRepository rekoRepository;

    @Test
    public void shouldFindByPatientId() {
        final var reko = getReko(LocalDateTime.now());
        rekoRepository.save(reko);
        final var result = rekoRepository.findByPatientId(reko.getPatientId());
        assertEquals(reko, result.get(0));
    }

    @Test
    public void shouldFindByPatientIds() {
        final var expectedResult = List.of(getReko(LocalDateTime.now()), getReko(LocalDateTime.now()), getReko(LocalDateTime.now()));
        rekoRepository.save(expectedResult.get(0));
        rekoRepository.save(expectedResult.get(1));
        rekoRepository.save(expectedResult.get(2));
        final var actualResult = rekoRepository.findByPatientIdIn(
            expectedResult.stream().map(Reko::getPatientId).collect(Collectors.toList()));
        assertEquals(expectedResult, actualResult);
    }

    @Test
    public void shouldFindByPatientIdAndCareUnitIdAndSickLeaveTimestamp() {
        final var expectedResult = getReko(LocalDateTime.now());
        rekoRepository.save(expectedResult);
        final var actualResult = rekoRepository.findByPatientIdAndCareUnitIdAndSickLeaveTimestampGreaterThanEqual(
            expectedResult.getPatientId(),
            expectedResult.getCareUnitId(),
            expectedResult.getSickLeaveTimestamp().minusDays(5));
        assertEquals(expectedResult, actualResult.get(0));
    }

    private static Reko getReko(LocalDateTime timeStamp) {
        final var reko = new Reko();
        reko.setPatientId(PATIENT_ID_1);
        reko.setStatus(STATUS);
        reko.setStaffId(STAFF_ID);
        reko.setStaffName(STAFF_NAME);
        reko.setCareProviderId(CARE_PROVIDER_ID);
        reko.setCareUnitId(CARE_UNIT_ID);
        reko.setRegistrationTimestamp(timeStamp);
        reko.setSickLeaveTimestamp(timeStamp);
        return reko;
    }
}
