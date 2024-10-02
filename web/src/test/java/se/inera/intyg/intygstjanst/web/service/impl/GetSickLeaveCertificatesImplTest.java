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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
import se.inera.intyg.intygstjanst.web.service.PuFilterService;

@ExtendWith(MockitoExtension.class)
class GetSickLeaveCertificatesImplTest {

    @Mock
    private SjukfallCertificateDao sjukfallCertificateDao;

    @Mock
    private IntygsDataConverter intygDataConverter;

    @Mock
    private SjukfallEngineService sjukfallEngineService;

    @Mock
    private PuFilterService puFilterService;

    @InjectMocks
    private GetSickLeaveCertificatesImpl getSickLeaveCertificates;

    private static final String CARE_PROVIDER_ID = "CareProviderId";
    private static final List<String> UNIT_IDS = List.of("UnitId1", "UnitId2");
    private static final List<String> PATIENT_IDS = List.of("PatientId1", "PatientId2");
    private static final Integer MAX_CERTIFICATE_GAP = 5;
    private static final Integer MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED = 3;
    private static final String PROTECTED_PERSON_FILTER_ID = "filterId";

    @Nested
    class CareProviderIdTest {

        @Test
        void shallIncludeCareProviderIdWhenQueryActiveSjukfallCertificate() {
            final var careProviderIdCaptor = ArgumentCaptor.forClass(String.class);
            getSickLeaveCertificates.get(CARE_PROVIDER_ID,
                UNIT_IDS,
                PATIENT_IDS,
                MAX_CERTIFICATE_GAP,
                MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED,
                PROTECTED_PERSON_FILTER_ID
            );
            verify(sjukfallCertificateDao).findAllSjukfallCertificate(careProviderIdCaptor.capture(), anyList(), anyList());
            assertEquals(CARE_PROVIDER_ID, careProviderIdCaptor.getValue());
        }

        @Test
        void shallThrowExceptionIfCareProviderIdIsNull() {
            assertThrows(IllegalArgumentException.class,
                () -> getSickLeaveCertificates.get(null,
                    UNIT_IDS,
                    PATIENT_IDS,
                    MAX_CERTIFICATE_GAP,
                    MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED,
                    PROTECTED_PERSON_FILTER_ID
                )

            );
        }

        @Test
        void shallThrowExceptionIfCareProviderIdIsEmpty() {
            assertThrows(IllegalArgumentException.class,
                () -> getSickLeaveCertificates.get(
                    " ",
                    UNIT_IDS,
                    PATIENT_IDS,
                    MAX_CERTIFICATE_GAP,
                    MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED,
                    PROTECTED_PERSON_FILTER_ID
                )
            );
        }
    }

    @Nested
    class UnitIdsTest {

        @Test
        void shallIncludeUnitIdsWhenQueryActiveSjukfallCertificate() {
            final var unitIdsCaptor = ArgumentCaptor.forClass(List.class);
            getSickLeaveCertificates.get(
                CARE_PROVIDER_ID,
                UNIT_IDS,
                PATIENT_IDS,
                MAX_CERTIFICATE_GAP,
                MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED,
                PROTECTED_PERSON_FILTER_ID);
            verify(sjukfallCertificateDao).findAllSjukfallCertificate(anyString(), unitIdsCaptor.capture(), anyList());
            assertEquals(UNIT_IDS, unitIdsCaptor.getValue());
        }

        @Test
        void shallThrowExceptionIfUnitIdsIsNull() {
            assertThrows(IllegalArgumentException.class,
                () -> getSickLeaveCertificates.get(
                    CARE_PROVIDER_ID,
                    null,
                    PATIENT_IDS,
                    MAX_CERTIFICATE_GAP,
                    MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED,
                    PROTECTED_PERSON_FILTER_ID
                )
            );
        }

        @Test
        void shallThrowExceptionIfUnitIdsIsEmpty() {
            assertThrows(IllegalArgumentException.class,
                () -> getSickLeaveCertificates.get(CARE_PROVIDER_ID, Collections.emptyList(), PATIENT_IDS, MAX_CERTIFICATE_GAP,
                    MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, PROTECTED_PERSON_FILTER_ID)
            );
        }

        @Test
        void shallThrowExceptionIfUnitIdsEmptyValues() {
            assertThrows(IllegalArgumentException.class,
                () -> getSickLeaveCertificates.get(CARE_PROVIDER_ID, List.of("UnitId1", " "), PATIENT_IDS, MAX_CERTIFICATE_GAP,
                    MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, PROTECTED_PERSON_FILTER_ID)
            );
        }

        @Test
        void shallThrowExceptionIfUnitIdsNullValues() {
            assertThrows(IllegalArgumentException.class,
                () -> getSickLeaveCertificates.get(CARE_PROVIDER_ID, Arrays.asList("UnitId1", null), PATIENT_IDS, MAX_CERTIFICATE_GAP,
                    MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, PROTECTED_PERSON_FILTER_ID)
            );
        }
    }

    @Nested
    class PatientIdsTest {

        @Test
        void shallIncludeUnitIdsWhenQueryActiveSjukfallCertificate() {
            final var patientIdsCaptor = ArgumentCaptor.forClass(List.class);
            getSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, PATIENT_IDS, MAX_CERTIFICATE_GAP,
                MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, PROTECTED_PERSON_FILTER_ID);
            verify(sjukfallCertificateDao).findAllSjukfallCertificate(anyString(), anyList(), patientIdsCaptor.capture());
            assertEquals(PATIENT_IDS, patientIdsCaptor.getValue());
        }

        @Test
        void shallThrowExceptionIfUnitIdsIsNull() {
            assertThrows(IllegalArgumentException.class,
                () -> getSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, null, MAX_CERTIFICATE_GAP,
                    MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, PROTECTED_PERSON_FILTER_ID)
            );
        }

        @Test
        void shallThrowExceptionIfUnitIdsIsEmpty() {
            assertThrows(IllegalArgumentException.class,
                () -> getSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, Collections.emptyList(), MAX_CERTIFICATE_GAP,
                    MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, PROTECTED_PERSON_FILTER_ID)
            );
        }

        @Test
        void shallThrowExceptionIfUnitIdsEmptyValues() {
            assertThrows(IllegalArgumentException.class,
                () -> getSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, List.of("PatientId1", " "), MAX_CERTIFICATE_GAP,
                    MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, PROTECTED_PERSON_FILTER_ID)
            );
        }

        @Test
        void shallThrowExceptionIfUnitIdsNullValues() {
            assertThrows(IllegalArgumentException.class,
                () -> getSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, Arrays.asList("PatientId1", null), MAX_CERTIFICATE_GAP,
                    MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, PROTECTED_PERSON_FILTER_ID)
            );
        }
    }

    @Nested
    class CalculateSickLeaves {

        @Test
        void shallIncludeMaxCertificateGapWhenCalculatingSickLeaves() {
            final var intygParametrarCapture = ArgumentCaptor.forClass(IntygParametrar.class);
            getSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, PATIENT_IDS, MAX_CERTIFICATE_GAP,
                MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, PROTECTED_PERSON_FILTER_ID);
            verify(sjukfallEngineService).beraknaSjukfallForEnhet(anyList(), intygParametrarCapture.capture());
            assertEquals(MAX_CERTIFICATE_GAP, intygParametrarCapture.getValue().getMaxIntygsGlapp());
        }

        @Test
        void shallIncludeMaxDaysSinceSickLeaveCompletedWhenCalculatingSickLeaves() {
            final var intygParametrarCapture = ArgumentCaptor.forClass(IntygParametrar.class);
            getSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, PATIENT_IDS, MAX_CERTIFICATE_GAP,
                MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, PROTECTED_PERSON_FILTER_ID);
            verify(sjukfallEngineService).beraknaSjukfallForEnhet(anyList(), intygParametrarCapture.capture());
            assertEquals(MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, intygParametrarCapture.getValue().getMaxAntalDagarSedanSjukfallAvslut());
        }

        @Test
        void shallIncludeTodayDateWhenCalculatingSickLeaves() {
            final var expectedTodayDate = LocalDate.now();
            final var intygParametrarCapture = ArgumentCaptor.forClass(IntygParametrar.class);
            getSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, PATIENT_IDS, MAX_CERTIFICATE_GAP,
                MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, PROTECTED_PERSON_FILTER_ID);
            verify(sjukfallEngineService).beraknaSjukfallForEnhet(anyList(), intygParametrarCapture.capture());
            assertEquals(expectedTodayDate, intygParametrarCapture.getValue().getAktivtDatum());
        }
    }

    @Nested
    class PuFilterTest {

        @Test
        void shallCallPuFilterService() {
            getSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, PATIENT_IDS, MAX_CERTIFICATE_GAP,
                MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, PROTECTED_PERSON_FILTER_ID);
            verify(puFilterService).enrichWithPatientNameAndFilter(anyList(), anyString());
        }

        @Test
        void shallCallPuFilterServiceWithListOfIntygData() {
            final var intygDataList = Collections.singletonList(new IntygData());
            when(intygDataConverter.convert(any())).thenReturn(intygDataList);
            final var captor = ArgumentCaptor.forClass(List.class);
            getSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, PATIENT_IDS, MAX_CERTIFICATE_GAP,
                MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, PROTECTED_PERSON_FILTER_ID);
            verify(puFilterService).enrichWithPatientNameAndFilter(captor.capture(), anyString());
            assertEquals(intygDataList, captor.getValue());
        }

        @Test
        void shallCallPuFilterServiceWithFilterOnProtectedPerson() {
            final var captor = ArgumentCaptor.forClass(String.class);
            getSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, PATIENT_IDS, MAX_CERTIFICATE_GAP,
                MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, PROTECTED_PERSON_FILTER_ID);
            verify(puFilterService).enrichWithPatientNameAndFilter(anyList(), captor.capture());
            assertEquals(PROTECTED_PERSON_FILTER_ID, captor.getValue());
        }
    }

    @Test
    void shallReturnSickLeaves() {
        final var expectedSjukfallEnhet = new SjukfallEnhet();
        expectedSjukfallEnhet.setAktivIntygsId("EXPECTED_ID");
        final var expectedSjukfallEnhetList = List.of(expectedSjukfallEnhet);

        final var sjukfallCertificateList = List.of(
            new SjukfallCertificate("EXPECTED_ID"),
            new SjukfallCertificate("INTYG_DATA_ONE"),
            new SjukfallCertificate("INTYG_DATA_TWO")
        );
        doReturn(sjukfallCertificateList)
            .when(sjukfallCertificateDao)
            .findAllSjukfallCertificate(anyString(), anyList(), anyList());

        final var intygDataOne = new IntygData();
        intygDataOne.setIntygId("INTYG_DATA_ONE");
        final var intygDataTwo = new IntygData();
        intygDataOne.setIntygId("INTYG_DATA_TWO");
        final var intygDataThree = new IntygData();
        intygDataThree.setIntygId("EXPECTED_ID");
        final var intygDataList = List.of(intygDataOne, intygDataTwo, intygDataThree);
        doReturn(intygDataList)
            .when(intygDataConverter)
            .convert(sjukfallCertificateList);

        doReturn(expectedSjukfallEnhetList)
            .when(sjukfallEngineService)
            .beraknaSjukfallForEnhet(eq(intygDataList), any(IntygParametrar.class));

        final var actualSjukfallEnhetList = getSickLeaveCertificates.get(CARE_PROVIDER_ID, UNIT_IDS, PATIENT_IDS, MAX_CERTIFICATE_GAP,
            MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED, PROTECTED_PERSON_FILTER_ID);

        assertEquals(expectedSjukfallEnhetList, actualSjukfallEnhetList);
    }

}