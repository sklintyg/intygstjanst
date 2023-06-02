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

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKod;
import se.inera.intyg.infra.sjukfall.dto.SjukfallEnhet;
import se.inera.intyg.intygstjanst.web.service.CalculatePatientAgeService;
import se.inera.intyg.intygstjanst.web.service.ResolvePatientGenderService;
import se.inera.intyg.intygstjanst.web.service.TextSearchFilterService;

@Service
public class TextSearchFilterServiceImpl implements TextSearchFilterService {

    private final CalculatePatientAgeService calculatePatientAgeService;
    private final ResolvePatientGenderService resolvePatientGenderService;

    public TextSearchFilterServiceImpl(CalculatePatientAgeService calculatePatientAgeService,
        ResolvePatientGenderService resolvePatientGenderService) {
        this.calculatePatientAgeService = calculatePatientAgeService;
        this.resolvePatientGenderService = resolvePatientGenderService;
    }

    @Override
    public List<SjukfallEnhet> filterList(List<SjukfallEnhet> sickLeaves, String textSearch) {
        if (textSearch == null || textSearch.isEmpty()) {
            return sickLeaves;
        }
        return filterOnTextSearch(sickLeaves, textSearch);
    }

    @Override
    public boolean filter(SjukfallEnhet sickLeave, String textSearch) {
        if (textSearch == null || textSearch.isEmpty()) {
            return true;
        }
        return textSearchMatchesAnyField(sickLeave, textSearch);
    }

    private List<SjukfallEnhet> filterOnTextSearch(List<SjukfallEnhet> sickLeaves, String textSearch) {
        return sickLeaves.stream()
            .filter(sickLeave -> textSearchMatchesAnyField(sickLeave, textSearch))
            .collect(Collectors.toList());
    }

    private boolean textSearchMatchesAnyField(SjukfallEnhet sickLeave, String textSearch) {
        return searchMatchesAge(sickLeave, textSearch)
            || searchMatchesPatientName(sickLeave, textSearch)
            || searchMatchesPatientGender(sickLeave, textSearch)
            || searchMatchesDiagnosisCode(sickLeave, textSearch)
            || searchMatchesSickLeavePeriod(sickLeave, textSearch)
            || searchMatchesLength(sickLeave, textSearch)
            || searchMatchesNumberOfCertificates(sickLeave, textSearch)
            || searchMatchesActiveDegree(sickLeave, textSearch)
            || searchMatchesDoctorName(sickLeave, textSearch)
            || searchMatchesPatientId(sickLeave, textSearch);
    }

    private boolean searchMatchesPatientId(SjukfallEnhet sickLeave, String textSearch) {
        return matches(sickLeave.getPatient().getId(), textSearch);
    }

    private boolean searchMatchesDoctorName(SjukfallEnhet sickLeave, String textSearch) {
        return matches(sickLeave.getLakare().getNamn(), textSearch);
    }

    private boolean searchMatchesActiveDegree(SjukfallEnhet sickLeave, String textSearch) {
        return matches(sickLeave.getAktivGrad() + "%", textSearch);
    }

    private boolean searchMatchesNumberOfCertificates(SjukfallEnhet sickLeave, String textSearch) {
        return matches(String.valueOf(sickLeave.getIntygLista().size()), textSearch);
    }

    private boolean searchMatchesLength(SjukfallEnhet sickLeave, String textSearch) {
        return matches(String.valueOf(sickLeave.getDagar()), textSearch);
    }

    private boolean searchMatchesSickLeavePeriod(SjukfallEnhet sickLeave, String textSearch) {
        return matches(sickLeave.getStart().toString(), textSearch) || matches(sickLeave.getSlut().toString(), textSearch);
    }

    private boolean searchMatchesDiagnosisCode(SjukfallEnhet sickLeave, String textSearch) {
        return matches(getDiagnosis(sickLeave), textSearch);
    }

    private static String getDiagnosis(SjukfallEnhet sickLeave) {
        final var diagnosis = new StringBuilder(sickLeave.getDiagnosKod().getCleanedCode() + sickLeave.getDiagnosKod().getName());
        sickLeave.getBiDiagnoser().forEach(
            biDiagnosis -> diagnosis
                .append(biDiagnosis.getCleanedCode())
                .append(isLastBiDiagnosis(sickLeave, biDiagnosis) ? "" : ","));
        return diagnosis.toString();
    }

    private static boolean isLastBiDiagnosis(SjukfallEnhet sickLeave, DiagnosKod biDiagnosis) {
        return sickLeave.getBiDiagnoser().indexOf(biDiagnosis) == sickLeave.getBiDiagnoser().size() - 1;
    }

    private boolean searchMatchesPatientGender(SjukfallEnhet sickLeave, String textSearch) {
        final var patientGender = resolvePatientGenderService.get(sickLeave.getPatient().getId());
        return matches(patientGender, textSearch);
    }

    private boolean searchMatchesAge(SjukfallEnhet sickLeave, String textSearch) {
        final var patientAge = calculatePatientAgeService.get(sickLeave.getPatient().getId());
        return matches(patientAge.toString(), textSearch);
    }

    private boolean searchMatchesPatientName(SjukfallEnhet sickLeave, String textSearch) {
        return matches(sickLeave.getPatient().getNamn(), textSearch);
    }

    private boolean matches(String text, String textSearch) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return text.toUpperCase().contains(textSearch.toUpperCase());
    }
}
