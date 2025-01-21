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

package se.inera.intyg.intygstjanst.web.service.repo.model;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationType;

import java.util.Optional;

@Service
public class CitizenCertificateRelationConverterImpl implements CitizenCertificateRelationConverter {

    @Override
    public Optional<CitizenCertificateRelationDTO> convert(String certificateId, Relation relation) {
        if (!certificateId.equals(relation.getToIntygsId()) && !certificateId.equals(relation.getFromIntygsId())) {
            return Optional.empty();
        }

        if (!isRelationCodeIncluded(relation.getRelationKod())) {
            return Optional.empty();
        }

        return Optional.of(
            CitizenCertificateRelationDTO
                .builder()
                .certificateId(getRelatedId(certificateId, relation.getToIntygsId(), relation.getFromIntygsId()))
                .timestamp(relation.getCreated())
                .type(getType(certificateId, relation.getToIntygsId()))
                .build()
        );
    }

    private String getRelatedId(String id, String toId, String fromId) {
        return id.equals(toId) ? fromId : toId;
    }

    private boolean isRelationCodeIncluded(String code) {
        return code.equals(RelationKod.ERSATT.toString())
            || code.equals(RelationKod.KOMPLT.toString());
    }

    private CitizenCertificateRelationType getType(String certificateId, String toCertificateId) {
        return certificateId.equals(toCertificateId)
            ? CitizenCertificateRelationType.REPLACED
            : CitizenCertificateRelationType.REPLACES;
    }
}
