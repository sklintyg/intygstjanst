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

package se.inera.intyg.intygstjanst.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.sjukfall.dto.IntygData;
import se.inera.intyg.infra.sjukfall.dto.IntygParametrar;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.infra.sjukfall.services.SjukfallEngineService;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.integration.sickleave.converter.IntygsDataConverter;

@ExtendWith(MockitoExtension.class)
class GetActiveSickLeaveCertificatesImplTest {

    @Mock
    private SjukfallCertificateDao sjukfallCertificateDao;

    @Mock
    private IntygsDataConverter intygDataConverter;

    @Mock
    private SjukfallEngineService sjukfallEngineService;

    @InjectMocks
    private GetActiveSickLeaveCertificatesImpl getActiveSickLeaveCertificates;

    private static final String CARE_PROVIDER_ID = "CareProviderId";
    private static final List<String> UNIT_IDS = List.of("UnitId1", "UnitId2");
    private static final List<String> DOCTOR_IDS = List.of("DoctorId1", "DoctorId2");
    private static final Integer MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED = 3;

    @Nested
    class CareProviderIdTest {

        @Test
        void shallIncludeCareProviderIdWhenQueryActiveSjukfallCertificate() {
            final var careProviderIdCaptor = ArgumentCaptor.forClass(String.class);
            getActiveSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, DOCTOR_IDS, MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);
            verify(sjukfallCertificateDao).findActiveSjukfallCertificate(careProviderIdCaptor.capture(), anyList(), anyList(),
                any(LocalDate.class), any(LocalDate.class));
            assertEquals(CARE_PROVIDER_ID, careProviderIdCaptor.getValue());
        }

        @Test
        void shallThrowExceptionIfCareProviderIdIsNull() {
            assertThrows(IllegalArgumentException.class,
                () -> getActiveSickLeaveCertificates.get(null, UNIT_IDS, DOCTOR_IDS, MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED)
            );
        }

        @Test
        void shallThrowExceptionIfCareProviderIdIsEmpty() {
            assertThrows(IllegalArgumentException.class,
                () -> getActiveSickLeaveCertificates.get(" ", UNIT_IDS, DOCTOR_IDS, MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED)
            );
        }
    }

    @Nested
    class UnitIdsTest {

        @Test
        void shallIncludeUnitIdsWhenQueryActiveSjukfallCertificate() {
            final var unitIdsCaptor = ArgumentCaptor.forClass(List.class);
            getActiveSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, DOCTOR_IDS, MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);
            verify(sjukfallCertificateDao).findActiveSjukfallCertificate(anyString(), unitIdsCaptor.capture(), anyList(),
                any(LocalDate.class), any(LocalDate.class));
            assertEquals(UNIT_IDS, unitIdsCaptor.getValue());
        }

        @Test
        void shallThrowExceptionIfUnitIdsIsNull() {
            assertThrows(IllegalArgumentException.class,
                () -> getActiveSickLeaveCertificates.get(CARE_PROVIDER_ID, null, DOCTOR_IDS, MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED)
            );
        }

        @Test
        void shallThrowExceptionIfUnitIdsIsEmpty() {
            assertThrows(IllegalArgumentException.class,
                () -> getActiveSickLeaveCertificates.get(CARE_PROVIDER_ID, Collections.emptyList(), DOCTOR_IDS,
                    MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED)
            );
        }

        @Test
        void shallThrowExceptionIfUnitIdsEmptyValues() {
            assertThrows(IllegalArgumentException.class,
                () -> getActiveSickLeaveCertificates.get(CARE_PROVIDER_ID, List.of("UnitId1", " "), DOCTOR_IDS,
                    MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED)
            );
        }

        @Test
        void shallThrowExceptionIfUnitIdsNullValues() {
            assertThrows(IllegalArgumentException.class,
                () -> getActiveSickLeaveCertificates.get(CARE_PROVIDER_ID, Arrays.asList("UnitId1", null), DOCTOR_IDS,
                    MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED)
            );
        }
    }

    @Nested
    class DoctorIdsTest {

        @Test
        void shallIncludeDoctorIdsWhenQueryActiveSjukfallCertificate() {
            final var doctorIdsCaptor = ArgumentCaptor.forClass(List.class);
            getActiveSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, DOCTOR_IDS, MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);
            verify(sjukfallCertificateDao).findActiveSjukfallCertificate(anyString(), anyList(), doctorIdsCaptor.capture(),
                any(LocalDate.class), any(LocalDate.class));
            assertEquals(DOCTOR_IDS, doctorIdsCaptor.getValue());
        }

        @Test
        void shallAllowEmptyDoctorIdsWhenQueryActiveSjukfallCertificate() {
            final var doctorIdsCaptor = ArgumentCaptor.forClass(List.class);
            getActiveSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, Collections.emptyList(), MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);
            verify(sjukfallCertificateDao).findActiveSjukfallCertificate(anyString(), anyList(), doctorIdsCaptor.capture(),
                any(LocalDate.class), any(LocalDate.class));
            assertEquals(Collections.emptyList(), doctorIdsCaptor.getValue());
        }

        @Test
        void shallAllowNullDoctorIdsWhenQueryActiveSjukfallCertificate() {
            final var doctorIdsCaptor = ArgumentCaptor.forClass(List.class);
            getActiveSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, null, MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);
            verify(sjukfallCertificateDao).findActiveSjukfallCertificate(anyString(), anyList(), doctorIdsCaptor.capture(),
                any(LocalDate.class), any(LocalDate.class));
            assertEquals(null, doctorIdsCaptor.getValue());
        }

        @Test
        void shallThrowExceptionIfDoctorIdsEmptyValues() {
            assertThrows(IllegalArgumentException.class,
                () -> getActiveSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, List.of("DoctorId1", " "),
                    MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED)
            );
        }

        @Test
        void shallThrowExceptionIfDoctorIdsNullValues() {
            assertThrows(IllegalArgumentException.class,
                () -> getActiveSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, Arrays.asList("UnitId1", null),
                    MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED)
            );
        }
    }

    @Nested
    class MaxDaysSinceSickLeaveCompleted {

        @Test
        void shallIncludeMaxDaysSinceSickLeaveCompletedWhenQueryActiveSjukfallCertificate() {
            final var expectedRecentlyClosed = LocalDate.now().minusDays(MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);
            final var recentlyClosedCapture = ArgumentCaptor.forClass(LocalDate.class);
            getActiveSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, DOCTOR_IDS, MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);
            verify(sjukfallCertificateDao).findActiveSjukfallCertificate(anyString(), anyList(), anyList(),
                any(LocalDate.class), recentlyClosedCapture.capture());
            assertEquals(expectedRecentlyClosed, recentlyClosedCapture.getValue());
        }

        @Test
        void shallIgnoreMaxDaysSinceSickLeaveCompletedIfZeroWhenQueryActiveSjukfallCertificate() {
            final var recentlyClosedCapture = ArgumentCaptor.forClass(LocalDate.class);
            getActiveSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, DOCTOR_IDS, 0);
            verify(sjukfallCertificateDao).findActiveSjukfallCertificate(anyString(), anyList(), anyList(), any(LocalDate.class),
                recentlyClosedCapture.capture());
            assertNull(recentlyClosedCapture.getValue());
        }

        @Test
        void shallIgnoreMaxDaysSinceSickLeaveCompletedIfNegativeWhenQueryActiveSjukfallCertificate() {
            final var recentlyClosedCapture = ArgumentCaptor.forClass(LocalDate.class);
            getActiveSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, DOCTOR_IDS, -3);
            verify(sjukfallCertificateDao).findActiveSjukfallCertificate(anyString(), anyList(), anyList(), any(LocalDate.class),
                recentlyClosedCapture.capture());
            assertNull(recentlyClosedCapture.getValue());
        }

        @Test
        void shallIncludeMaxDaysSinceSickLeaveCompletedWhenCalculatingSickLeaves() {
            final var intygParametrarCapture = ArgumentCaptor.forClass(IntygParametrar.class);
            getActiveSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, DOCTOR_IDS, MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);
            verify(sjukfallEngineService).beraknaSjukfallForEnhet(anyList(), intygParametrarCapture.capture());
            assertEquals(MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, intygParametrarCapture.getValue().getMaxAntalDagarSedanSjukfallAvslut());
        }
    }

    @Nested
    class TodayDate {

        @Test
        void shallIncludeTodayDateWhenQueryActiveSjukfallCertificate() {
            final var expectedTodayDate = LocalDate.now();
            final var todayDateCapture = ArgumentCaptor.forClass(LocalDate.class);
            getActiveSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, DOCTOR_IDS, MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);
            verify(sjukfallCertificateDao).findActiveSjukfallCertificate(anyString(), anyList(), anyList(),
                todayDateCapture.capture(), any(LocalDate.class));
            assertEquals(expectedTodayDate, todayDateCapture.getValue());
        }

        @Test
        void shallIncludeTodayDateWhenCalculatingSickLeaves() {
            final var expectedTodayDate = LocalDate.now();
            final var intygParametrarCapture = ArgumentCaptor.forClass(IntygParametrar.class);
            getActiveSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, DOCTOR_IDS, MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);
            verify(sjukfallEngineService).beraknaSjukfallForEnhet(anyList(), intygParametrarCapture.capture());
            assertEquals(expectedTodayDate, intygParametrarCapture.getValue().getAktivtDatum());
        }
    }

    @Test
    void shallOnlyReturnIntygDataThatAreActive() {
        final var expectedIntygData = new IntygData();
        expectedIntygData.setIntygId("EXPECTED_ID");
        final var expectedIntygDataList = List.of(expectedIntygData);

        final var sjukfallCertificateList = List.of(
            new SjukfallCertificate("EXPECTED_ID"),
            new SjukfallCertificate("INTYG_DATA_ONE"),
            new SjukfallCertificate("INTYG_DATA_TWO")
        );
        doReturn(sjukfallCertificateList)
            .when(sjukfallCertificateDao)
            .findActiveSjukfallCertificate(anyString(), anyList(), anyList(), any(LocalDate.class), any(LocalDate.class));

        final var intygDataOne = new IntygData();
        intygDataOne.setIntygId("INTYG_DATA_ONE");
        final var intygDataTwo = new IntygData();
        intygDataOne.setIntygId("INTYG_DATA_TWO");
        final var intygDataList = List.of(expectedIntygData, intygDataOne, intygDataTwo);
        doReturn(intygDataList)
            .when(intygDataConverter)
            .convert(sjukfallCertificateList);

        final var sjukfallEnhet = new SjukfallEnhet();
        sjukfallEnhet.setAktivIntygsId("EXPECTED_ID");
        final var sjukfallEnhetList = List.of(sjukfallEnhet);
        doReturn(sjukfallEnhetList)
            .when(sjukfallEngineService)
            .beraknaSjukfallForEnhet(eq(intygDataList), any(IntygParametrar.class));

        final var actualIntygDataList = getActiveSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, DOCTOR_IDS,
            MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);

        assertEquals(expectedIntygDataList, actualIntygDataList);
    }
}
