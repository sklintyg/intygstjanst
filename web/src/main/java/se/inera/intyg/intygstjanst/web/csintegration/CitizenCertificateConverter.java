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

package se.inera.intyg.intygstjanst.web.csintegration;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.facade.model.Certificate;
import se.inera.intyg.common.support.facade.model.CertificateRelationType;
import se.inera.intyg.common.support.facade.model.CertificateStatus;
import se.inera.intyg.common.support.facade.model.metadata.CertificateRelation;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateIssuerDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRecipientDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationType;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateSummaryDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateUnitDTO;

@Service
public class CitizenCertificateConverter {

    public CitizenCertificateDTO convert(Certificate certificate) {
        return CitizenCertificateDTO.builder()
            .id(certificate.getMetadata().getId())
            .type(
                CitizenCertificateTypeDTO.builder()
                    .id(certificate.getMetadata().getType())
                    .name(certificate.getMetadata().getName())
                    .version(certificate.getMetadata().getTypeVersion())
                    .build()
            )
            .unit(
                CitizenCertificateUnitDTO.builder()
                    .id(certificate.getMetadata().getUnit().getUnitId())
                    .name(certificate.getMetadata().getUnit().getUnitName())
                    .build()
            )
            .issued(certificate.getMetadata().getSigned())
            .issuer(
                CitizenCertificateIssuerDTO.builder()
                    .name(certificate.getMetadata().getIssuedBy().getFullName())
                    .build()
            )
            .summary(
                CitizenCertificateSummaryDTO.builder()
                    .label(certificate.getMetadata().getSummary().getLabel())
                    .value(certificate.getMetadata().getSummary().getValue())
                    .build()
            )
            .recipient(
                CitizenCertificateRecipientDTO.builder()
                    .id(certificate.getMetadata().getRecipient().getId())
                    .name(certificate.getMetadata().getRecipient().getName())
                    .sent(certificate.getMetadata().getRecipient().getSent())
                    .build()
            )
            .relations(
                Stream.concat(
                        parentRelation(certificate),
                        childRelations(certificate)
                    )
                    .collect(Collectors.toList())
            )
            .build();
    }

    private static Stream<CitizenCertificateRelationDTO> childRelations(Certificate certificate) {
        if (certificate.getMetadata().getRelations() == null || certificate.getMetadata().getRelations().getChildren() == null) {
            return Stream.empty();
        }

        return Arrays.stream(certificate.getMetadata().getRelations().getChildren())
            .filter(CitizenCertificateConverter::isSigned)
            .filter(CitizenCertificateConverter::isReplacedOrComplemented)
            .map(certificateRelation ->
                CitizenCertificateRelationDTO.builder()
                    .certificateId(certificateRelation.getCertificateId())
                    .timestamp(certificateRelation.getCreated())
                    .type(CitizenCertificateRelationType.REPLACED)
                    .build());
    }

    private static Stream<CitizenCertificateRelationDTO> parentRelation(Certificate certificate) {
        if (certificate.getMetadata().getRelations() == null || certificate.getMetadata().getRelations().getParent() == null) {
            return Stream.empty();
        }

        if (!isSigned(certificate.getMetadata().getRelations().getParent()) || !isReplacedOrComplemented(
            certificate.getMetadata().getRelations().getParent())) {
            return Stream.empty();
        }

        return Stream.of(
            CitizenCertificateRelationDTO.builder()
                .certificateId(certificate.getMetadata().getRelations().getParent().getCertificateId())
                .timestamp(certificate.getMetadata().getRelations().getParent().getCreated())
                .type(CitizenCertificateRelationType.REPLACES)
                .build()
        );
    }

    private static boolean isSigned(CertificateRelation certificateRelation) {
        return certificateRelation.getStatus().equals(CertificateStatus.SIGNED);
    }

    private static boolean isReplacedOrComplemented(CertificateRelation certificateRelation) {
        return certificateRelation.getType().equals(CertificateRelationType.REPLACED)
            || certificateRelation.getType().equals(CertificateRelationType.COMPLEMENTED);
    }
}
