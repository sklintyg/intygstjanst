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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKapitel;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.web.service.CalculatePatientAgeService;
import se.inera.intyg.intygstjanst.web.service.DiagnosisChapterService;
import se.inera.intyg.intygstjanst.web.service.FilterSickLeaves;
import se.inera.intyg.intygstjanst.web.service.dto.SickLeaveLengthInterval;

@Service
public class FilterSickLeavesImpl implements FilterSickLeaves {

    private final DiagnosisChapterService diagnosisChapterService;
    private final CalculatePatientAgeService calculatePatientAgeService;

    public FilterSickLeavesImpl(DiagnosisChapterService diagnosisChapterService, CalculatePatientAgeService calculatePatientAgeService) {
        this.diagnosisChapterService = diagnosisChapterService;
        this.calculatePatientAgeService = calculatePatientAgeService;
    }

    @Override
    public List<SjukfallEnhet> filter(List<SjukfallEnhet> sickLeaveList, List<SickLeaveLengthInterval> sickLeaveLengthIntervals,
        List<DiagnosKapitel> diagnosisChapters, Integer fromPatientAge, Integer toPatientAge, LocalDate fromSickLeaveEndDate,
        LocalDate toSickLeaveEndDate, List<String> doctorsIds, List<String> rekoStatuses) {
        return sickLeaveList.stream()
            .filter(sickLeave -> filterOnSickLeaveLengthIntervals(sickLeave, sickLeaveLengthIntervals))
            .filter(sickLeave -> filterOnDiagnosisChapters(sickLeave, diagnosisChapters))
            .filter(sickLeave -> filterOnPatientAge(sickLeave, fromPatientAge, toPatientAge))
            .filter(sickLeave -> filterOnSickLeaveEndDate(sickLeave, fromSickLeaveEndDate, toSickLeaveEndDate))
            .filter(sickLeave -> filterOnDoctorIds(sickLeave, doctorsIds))
            .filter(sickLeave -> filterOnRekoStatuses(sickLeave, rekoStatuses))
            .collect(Collectors.toList());
    }

    private boolean filterOnRekoStatuses(SjukfallEnhet sickLeave, List<String> rekoStatuses) {
        if (rekoStatuses == null || rekoStatuses.size() == 0) {
            return true;
        }

        return rekoStatuses.stream().anyMatch(
                (rekoStatus) -> sickLeave.getRekoStatus().getStatus().getId().equals(rekoStatus)
        );
    }

    private boolean filterOnDoctorIds(SjukfallEnhet sickLeave, List<String> doctorIds) {
        if (doctorIds == null || doctorIds.size() == 0) {
            return true;
        }

        return doctorIds.stream().anyMatch((doctorId) -> sickLeave.getLakare().getId().equals(doctorId));
    }

    private boolean filterOnSickLeaveLengthIntervals(SjukfallEnhet sickLeave, List<SickLeaveLengthInterval> sickLeaveLengthIntervals) {
        if (sickLeaveLengthIntervals == null || sickLeaveLengthIntervals.isEmpty()) {
            return true;
        }
        return sickLeaveLengthIntervals.stream()
            .anyMatch(interval ->
                (interval.getFrom() == null || interval.getFrom() <= sickLeave.getDagar())
                    && (interval.getTo() == null || interval.getTo() >= sickLeave.getDagar()));
    }

    private boolean filterOnDiagnosisChapters(SjukfallEnhet sickLeave, List<DiagnosKapitel> diagnosisChapters) {
        if (diagnosisChapters == null || diagnosisChapters.isEmpty()) {
            return true;
        }
        return diagnosisChapters.contains(diagnosisChapterService.getDiagnosisChaptersFromSickLeave(sickLeave));
    }

    private boolean filterOnPatientAge(SjukfallEnhet sickLeave, Integer patientAgeFrom, Integer patientAgeTo) {
        if (patientAgeFrom == null || patientAgeTo == null) {
            return true;
        }
        final var patientAge = calculatePatientAgeService.get(sickLeave.getPatient().getId());
        return patientAgeFrom <= patientAge && patientAgeTo >= patientAge;
    }

    private boolean filterOnSickLeaveEndDate(SjukfallEnhet sickLeave, LocalDate from, LocalDate to) {
        return (from == null || from.isBefore(sickLeave.getSlut())) && (to == null || to.isAfter(sickLeave.getSlut()));
    }
}
