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

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CitizenCertificateConverterImpl implements CitizenCertificateConverter {
    private final CitizenCertificateRelationConverter citizenCertificateRelationConverter;

    public CitizenCertificateConverterImpl(CitizenCertificateRelationConverter citizenCertificateRelationConverter) {
        this.citizenCertificateRelationConverter = citizenCertificateRelationConverter;
    }

    @Override
    public CitizenCertificate convert(Certificate certificate, List<Relation> relations) {
        final var sentState = getSentState(certificate.getStates());

        return CitizenCertificate
                .builder()
                .id(certificate.getId())
                .type(certificate.getType())
                .typeVersion(certificate.getTypeVersion())
                .additionalInfo(certificate.getAdditionalInfo())
                .issuerName(certificate.getSigningDoctorName())
                .unitId(certificate.getCareUnitId())
                .unitName(certificate.getCareUnitName())
                .issued(certificate.getSignedDate())
                .sentDate(sentState.map(CertificateStateHistoryEntry::getTimestamp).orElse(null))
                .relations(getRelations(certificate.getId(), relations))
                .build();
    }

    private List<CitizenCertificateRelationDTO> getRelations(String certificateId, List<Relation> relations) {
        return relations
                .stream()
                .filter(this::isRelationIncluded)
                .map((relation) -> citizenCertificateRelationConverter.convert(certificateId, relation))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private boolean isRelationIncluded(Relation relation) {
        return relation.getRelationKod().equals(RelationKod.ERSATT.toString())
            || relation.getRelationKod().equals(RelationKod.KOMPLT.toString());
    }

    private Optional<CertificateStateHistoryEntry> getSentState(Collection<CertificateStateHistoryEntry> states) {
        return states.stream().filter((state) -> state.getState() == CertificateState.SENT).findFirst();
    }
}
