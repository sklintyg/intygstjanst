/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.service.converter;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import se.inera.intyg.infra.certificate.builder.DiagnosedCertificateBuilder;
import se.inera.intyg.infra.certificate.dto.DiagnosedCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;

@Component
public class CertificateToDiagnosedCertificateConverter {

    public DiagnosedCertificate convertLuaefs(Certificate certificate,
        List<String> diagnosisList) {
        return getBuild(certificate, diagnosisList);
    }

    public DiagnosedCertificate convertLuaena(Certificate certificate,
        List<String> diagnosisList) {
        return getBuild(certificate, diagnosisList);
    }

    public DiagnosedCertificate convertLuse(Certificate certificate,
        List<String> diagnosisList) {
        return getBuild(certificate, diagnosisList);
    }

    private static DiagnosedCertificate getBuild(Certificate certificate, List<String> diagnoses) {

        return new DiagnosedCertificateBuilder(certificate.getId())
            .certificateType(certificate.getType())
            .personId(certificate.getCivicRegistrationNumber().getPersonnummerWithDash())
            .personalHsaId(certificate.getCertificateMetaData().getDoctorId())
            .signingDoctorName(certificate.getSigningDoctorName())
            .careProviderId(certificate.getCareGiverId())
            .careUnitId(certificate.getCareUnitId())
            .careUnitName(certificate.getCareUnitName())
            .signingDateTime(certificate.getSignedDate())
            .deleted(certificate.isRevoked())
            .testCertificate(certificate.isTestCertificate())
            .diagnoseCode(diagnoses != null ? diagnoses.get(0) : null)
            .secondaryDiagnoseCodes(buildSecondaryDiagnoseCodes(diagnoses))
            .build();
    }

    private static List<String> buildSecondaryDiagnoseCodes(List<String> diagnoseList) {
        if (diagnoseList == null || diagnoseList.size() <= 1) {
            return null;
        }

        return diagnoseList.stream().skip(1).collect(Collectors.toList());
    }
}
