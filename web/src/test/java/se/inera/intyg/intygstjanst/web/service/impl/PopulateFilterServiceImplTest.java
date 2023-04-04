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

package se.inera.intyg.intygstjanst.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.service.HsaServiceProvider;
import se.inera.intyg.intygstjanst.web.service.PopulateFilterService;
import se.inera.intyg.intygstjanst.web.service.dto.PopulateFiltersRequestDTO;

@ExtendWith(MockitoExtension.class)
class PopulateFilterServiceImplTest {

    private static final String CARE_UNIT_ID = "careUnitId";
    private static final String CERTIFICATE_ID = "certificateId";
    private static final String ANOTHER_CARE_UNIT_ID = "anotherCareUnitId";
    private static final String CARE_GIVER_HSA_ID = "careGiverHsaId";
    private static final int MAX_DAYS_SINCE_SICK_LEAVE_COMPLEDTED = 5;
    private final List<String> unitAndRelatedSubUnits = List.of(CARE_UNIT_ID, ANOTHER_CARE_UNIT_ID);

    @Mock
    private SjukfallCertificateDao sjukfallCertificateDao;
    @Mock
    private HsaServiceProvider hsaServiceProvider;

    private PopulateFilterService populateFilterService;

    @BeforeEach
    void setUp() {
        populateFilterService = new PopulateFilterServiceImpl(sjukfallCertificateDao, hsaServiceProvider);
    }

    @Test
    void shouldReturnEmptyList() {
        final var populateFiltersRequestDTO = new PopulateFiltersRequestDTO();
        final var result = populateFilterService.getActiveSickLeaveCertificates(populateFiltersRequestDTO);
        assertEquals(0, result.size());
    }

    @Test
    void shouldReturnListOfActiveSickLeaveCertificates() {
        final var populateFiltersRequestDTO = new PopulateFiltersRequestDTO();
        populateFiltersRequestDTO.setCareUnitId(CARE_UNIT_ID);
        populateFiltersRequestDTO.setMaxDaysSinceSickLeaveCompleted(MAX_DAYS_SINCE_SICK_LEAVE_COMPLEDTED);

        final var expectedResult = List.of(new SjukfallCertificate(CERTIFICATE_ID), new SjukfallCertificate(CERTIFICATE_ID));

        when(hsaServiceProvider.getCareGiverHsaId(CARE_UNIT_ID)).thenReturn(CARE_GIVER_HSA_ID);
        when(hsaServiceProvider.getUnitAndRelatedSubUnits(CARE_UNIT_ID)).thenReturn(unitAndRelatedSubUnits);
        when(sjukfallCertificateDao.findDoctorsWithActiveSickLeavesForCareUnits(CARE_GIVER_HSA_ID, unitAndRelatedSubUnits,
            MAX_DAYS_SINCE_SICK_LEAVE_COMPLEDTED)).thenReturn(expectedResult);

        final var result = populateFilterService.getActiveSickLeaveCertificates(populateFiltersRequestDTO);

        assertEquals(expectedResult, result);
    }
}
