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

package se.inera.intyg.intygstjanst.web.service.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import se.inera.intyg.infra.sjukfall.dto.DiagnosKapitel;

@Value
@Builder
public class GetSickLeaveServiceRequest {

    String unitId;
    String careUnitId;
    List<String> doctorIds;
    int maxCertificateGap;
    int maxDaysSinceSickLeaveCompleted;
    List<SickLeaveLengthInterval> sickLeaveLengthIntervals;
    List<DiagnosKapitel> diagnosisChapters;
    Integer fromPatientAge;
    Integer toPatientAge;
    String protectedPersonFilterId;
    LocalDate fromSickLeaveEndDate;
    LocalDate toSickLeaveEndDate;
    List<String> rekoStatusTypeIds;
    List<String> occupationTypeIds;
    String textSearch;
}
