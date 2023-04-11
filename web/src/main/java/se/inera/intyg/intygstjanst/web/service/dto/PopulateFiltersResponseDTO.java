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

package se.inera.intyg.intygstjanst.web.service.dto;

import java.util.List;
import java.util.Objects;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKapitel;
import se.inera.intyg.infra.sjukfall.dto.Lakare;

public class PopulateFiltersResponseDTO {

    private List<Lakare> activeDoctors;
    private List<DiagnosKapitel> diagnosisChapters;


    public static PopulateFiltersResponseDTO create(List<Lakare> doctorsRequestDTO, List<DiagnosKapitel> diagnosisCodes) {
        final var populateFiltersResponseDTO = new PopulateFiltersResponseDTO();
        populateFiltersResponseDTO.activeDoctors = doctorsRequestDTO;
        populateFiltersResponseDTO.diagnosisChapters = diagnosisCodes;
        return populateFiltersResponseDTO;
    }

    public void setDiagnosisChapters(List<DiagnosKapitel> diagnosisChapters) {
        this.diagnosisChapters = diagnosisChapters;
    }

    public List<DiagnosKapitel> getDiagnosisChapters() {
        return diagnosisChapters;
    }

    public List<Lakare> getActiveDoctors() {
        return activeDoctors;
    }

    public void setActiveDoctors(List<Lakare> activeDoctors) {
        this.activeDoctors = activeDoctors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PopulateFiltersResponseDTO that = (PopulateFiltersResponseDTO) o;
        return Objects.equals(activeDoctors, that.activeDoctors) && Objects.equals(diagnosisChapters,
            that.diagnosisChapters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activeDoctors, diagnosisChapters);
    }

    @Override
    public String toString() {
        return "PopulateFiltersResponseDTO{"
            + "activeDoctors=" + activeDoctors
            + ", diagnosisChapters=" + diagnosisChapters
            + '}';
    }
}
