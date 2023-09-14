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

package se.inera.intyg.intygstjanst.web.service.repo.model;

import lombok.Builder;
import lombok.Data;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationDTO;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CitizenCertificate {
    private String id;
    private String type;
    private String typeVersion;
    private String unitName;
    private String unitId;
    private String issuerName;
    private String additionalInfo;
    private LocalDateTime issued;
    private LocalDateTime sentDate;
    private List<CitizenCertificateRelationDTO> relations;
}
