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

package se.inera.intyg.intygstjanst.web.integration.testability.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.inera.intyg.common.support.common.enumerations.RelationKod;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestabilityConfigProvider {

    private String careProviderId;
    private String certificateId;
    private String patientId;
    private int fromDays;
    private int toDays;
    private String careUnitId;
    private String doctorId;
    private String doctorName;
    private String relationsId;
    private RelationKod relationKod;
    private List<String> diagnosisCode;
    private String occupation;
    private List<String> workCapacity;
    private boolean send;
    private boolean revoked;
    private LocalDateTime signTimestamp;
}
