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

package se.inera.intyg.intygstjanst.web.integration.sickleave;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKapitel;
import se.inera.intyg.infra.sjukfall.dto.Lakare;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.web.service.GetSickLeaveFilterService;
import se.inera.intyg.intygstjanst.web.service.GetSickLeavesService;
import se.inera.intyg.intygstjanst.web.service.dto.GetSickLeaveFilterServiceRequest;
import se.inera.intyg.intygstjanst.web.service.dto.GetSickLeaveFilterServiceResponse;
import se.inera.intyg.intygstjanst.web.service.dto.GetSickLeaveServiceRequest;
import se.inera.intyg.intygstjanst.web.service.dto.OccupationType;
import se.inera.intyg.intygstjanst.web.service.dto.OccupationTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.PopulateFiltersRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.PopulateFiltersResponseDTO;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusType;
import se.inera.intyg.intygstjanst.web.service.dto.RekoStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveLengthInterval;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveResponseDTO;

@ExtendWith(MockitoExtension.class)
class SickLeaveControllerTest {

    @Mock
    private GetSickLeavesService getSickLeavesService;
    @Mock
    private GetSickLeaveFilterService getSickLeaveFilterService;
    @InjectMocks
    private SickLeaveController sickLeaveController;

    private static final String CARE_UNIT_ID = "CareUnitId";
    private static final String UNIT_ID = "UnitId1";
    private static final String DOCTOR_ID = "DoctorId1";
    private static final List<String> DOCTOR_IDS = List.of("DoctorId1", "DoctorId2");
    private static final Integer MAX_CERTIFICATE_GAP = 5;
    private static final Integer MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED = 3;
    private static final boolean HAS_ONGOING_SICK_LEAVES = true;
    private static final List<SickLeaveLengthInterval> SICK_LEAVE_LENGTH_INTERVALS = List.of(
        new SickLeaveLengthInterval(1, 150)
    );
    private static final List<DiagnosKapitel> DIAGNOSIS_CHAPTER = List.of(
        new DiagnosKapitel("A00-B99Vissa infektionssjukdomar och parasitsjukdomar")
    );
    private static final List<Lakare> DOCTORS = List.of(
        Lakare.create(DOCTOR_ID, DOCTOR_ID)
    );
    private static final List<RekoStatusTypeDTO> REKO_STATUSES =
        List.of(new RekoStatusTypeDTO(RekoStatusType.REKO_1.toString(), RekoStatusType.REKO_1.getName()));

    private static final List<String> REKO_STATUSES_FILTER = List.of("REKO_1", "REKO_2");
    private static final List<OccupationTypeDTO> OCCUPATION_TYPE_DTO_LIST = List.of(
        new OccupationTypeDTO(OccupationType.NUVARANDE_ARBETE.toString(), OccupationType.NUVARANDE_ARBETE.getName())
    );
    private static final List<String> OCCUPATION_IDS = List.of(OccupationType.ARBETSSOKANDE.toString());
    private static final Integer PATIENT_AGE_FROM = 1;
    private static final Integer PATIENT_AGE_TO = 150;
    private static final int NUMBER_OF_SICK_LEAVES = 10;
    private static final String TEXT_SEARCH = "textSearch";


    @Nested
    class GetActiveSickLeavesForCareUnitTest {

        private SickLeaveRequestDTO sickLeaveRequestDTO;

        @BeforeEach
        void setUp() {
            sickLeaveRequestDTO = new SickLeaveRequestDTO();
            sickLeaveRequestDTO.setCareUnitId(CARE_UNIT_ID);
            sickLeaveRequestDTO.setUnitId(UNIT_ID);
            sickLeaveRequestDTO.setDoctorIds(DOCTOR_IDS);
            sickLeaveRequestDTO.setMaxCertificateGap(MAX_CERTIFICATE_GAP);
            sickLeaveRequestDTO.setMaxDaysSinceSickLeaveCompleted(MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);
            sickLeaveRequestDTO.setSickLeaveLengthIntervals(SICK_LEAVE_LENGTH_INTERVALS);
            sickLeaveRequestDTO.setDiagnosisChapters(DIAGNOSIS_CHAPTER);
            sickLeaveRequestDTO.setFromPatientAge(PATIENT_AGE_FROM);
            sickLeaveRequestDTO.setToPatientAge(PATIENT_AGE_TO);
            sickLeaveRequestDTO.setProtectedPersonFilterId(DOCTOR_ID);
            sickLeaveRequestDTO.setRekoStatusTypeIds(REKO_STATUSES_FILTER);
            sickLeaveRequestDTO.setOccupationTypeIds(OCCUPATION_IDS);
            sickLeaveRequestDTO.setTextSearch(TEXT_SEARCH);
        }

        @Test
        void shouldIncludeRequest() {
            final var expectedRequest = GetSickLeaveServiceRequest.builder()
                .careUnitId(sickLeaveRequestDTO.getCareUnitId())
                .unitId(sickLeaveRequestDTO.getUnitId())
                .maxDaysSinceSickLeaveCompleted(sickLeaveRequestDTO.getMaxDaysSinceSickLeaveCompleted())
                .doctorIds(sickLeaveRequestDTO.getDoctorIds())
                .maxCertificateGap(sickLeaveRequestDTO.getMaxCertificateGap())
                .sickLeaveLengthIntervals(sickLeaveRequestDTO.getSickLeaveLengthIntervals())
                .diagnosisChapters(sickLeaveRequestDTO.getDiagnosisChapters())
                .fromPatientAge(sickLeaveRequestDTO.getFromPatientAge())
                .toPatientAge(sickLeaveRequestDTO.getToPatientAge())
                .protectedPersonFilterId(DOCTOR_ID)
                .rekoStatusTypeIds(REKO_STATUSES_FILTER)
                .occupationTypeIds(OCCUPATION_IDS)
                .textSearch(TEXT_SEARCH)
                .build();

            final var getSickLeaveServiceRequestArgumentCaptor = ArgumentCaptor.forClass(GetSickLeaveServiceRequest.class);
            sickLeaveController.getActiveSickLeavesForCareUnit(sickLeaveRequestDTO);
            verify(getSickLeavesService).get(getSickLeaveServiceRequestArgumentCaptor.capture());
            assertEquals(expectedRequest, getSickLeaveServiceRequestArgumentCaptor.getValue());
        }

        @Test
        void shouldReturnResponse() {
            final var sickLeaveRequestDTO = new SickLeaveRequestDTO();
            final var sjukfallEnhet = new SjukfallEnhet();
            final var expectedResponse = new SickLeaveResponseDTO(List.of(sjukfallEnhet));

            doReturn(List.of(sjukfallEnhet))
                .when(getSickLeavesService)
                .get(any(GetSickLeaveServiceRequest.class));

            final var result = sickLeaveController.getActiveSickLeavesForCareUnit(sickLeaveRequestDTO);
            assertEquals(expectedResponse, result.getEntity());
        }
    }

    @Nested
    class PopulateFiltersTest {

        private PopulateFiltersRequestDTO populateFiltersRequestDTO;

        @BeforeEach
        void setUp() {
            populateFiltersRequestDTO = new PopulateFiltersRequestDTO();
            populateFiltersRequestDTO.setCareUnitId(CARE_UNIT_ID);
            populateFiltersRequestDTO.setUnitId(UNIT_ID);
            populateFiltersRequestDTO.setDoctorId(DOCTOR_ID);
            populateFiltersRequestDTO.setMaxDaysSinceSickLeaveCompleted(MAX_DAYS_SINCE_SICK_LEAVE_COMPLETED);

            doReturn(
                GetSickLeaveFilterServiceResponse.builder()
                    .activeDoctors(DOCTORS)
                    .diagnosisChapters(DIAGNOSIS_CHAPTER)
                    .nbrOfSickLeaves(NUMBER_OF_SICK_LEAVES)
                    .rekoStatusTypes(REKO_STATUSES)
                    .occupationTypes(OCCUPATION_TYPE_DTO_LIST)
                    .hasOngoingSickLeaves(HAS_ONGOING_SICK_LEAVES)
                    .build())
                .when(getSickLeaveFilterService)
                .get(any(GetSickLeaveFilterServiceRequest.class));
        }

        @Test
        void shouldIncludeRequest() {
            final var expectedRequest = GetSickLeaveFilterServiceRequest.builder()
                .careUnitId(populateFiltersRequestDTO.getCareUnitId())
                .unitId(populateFiltersRequestDTO.getUnitId())
                .maxDaysSinceSickLeaveCompleted(populateFiltersRequestDTO.getMaxDaysSinceSickLeaveCompleted())
                .doctorId(populateFiltersRequestDTO.getDoctorId())
                .build();

            final var getSickLeaveFilterServiceRequestArgumentCaptor = ArgumentCaptor.forClass(GetSickLeaveFilterServiceRequest.class);
            sickLeaveController.populateFilters(populateFiltersRequestDTO);
            verify(getSickLeaveFilterService).get(getSickLeaveFilterServiceRequestArgumentCaptor.capture());
            assertEquals(expectedRequest, getSickLeaveFilterServiceRequestArgumentCaptor.getValue());
        }

        @Test
        void shouldReturnResponse() {
            final var expectedResponse = new PopulateFiltersResponseDTO(
                DOCTORS,
                DIAGNOSIS_CHAPTER,
                NUMBER_OF_SICK_LEAVES,
                HAS_ONGOING_SICK_LEAVES,
                REKO_STATUSES,
                OCCUPATION_TYPE_DTO_LIST
            );

            final var result = sickLeaveController.populateFilters(populateFiltersRequestDTO);
            assertEquals(expectedResponse, result.getEntity());
        }
    }
}
