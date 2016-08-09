/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.integration.converter;

import java.util.ArrayList;
import java.util.List;

import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.CertificateStateHolder;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;

public final class ConverterUtil {

    private ConverterUtil() {
    }

    public static Certificate toCertificate(CertificateHolder certificateHolder) {
        Certificate certificate = new Certificate(certificateHolder.getId());

        certificate.setType(certificateHolder.getType());
        certificate.setSigningDoctorName(certificateHolder.getSigningDoctorName());
        certificate.setSignedDate(certificateHolder.getSignedDate());

        certificate.setCareUnitId(certificateHolder.getCareUnitId());
        certificate.setCareUnitName(certificateHolder.getCareUnitName());
        certificate.setCareGiverId(certificateHolder.getCareGiverId());
        certificate.setCivicRegistrationNumber(certificateHolder.getCivicRegistrationNumber());
        certificate.setValidFromDate(certificateHolder.getValidFromDate());
        certificate.setValidToDate(certificateHolder.getValidToDate());
        certificate.setDeletedByCareGiver(certificateHolder.isDeletedByCareGiver());
        certificate.setAdditionalInfo(certificateHolder.getAdditionalInfo());
        if (certificateHolder.getCertificateStates() != null) {
            List<CertificateStateHistoryEntry> certificateStates = new ArrayList<>(
                    certificateHolder.getCertificateStates().size());
            for (CertificateStateHolder certificateStateHolder : certificateHolder.getCertificateStates()) {
                certificateStates.add(new CertificateStateHistoryEntry(certificateStateHolder.getTarget(), certificateStateHolder.getState(),
                        certificateStateHolder.getTimestamp()));
            }
            certificate.setStates(certificateStates);
        }
        return certificate;
    }

    public static CertificateHolder toCertificateHolder(Certificate certificate) {
        CertificateHolder certificateHolder = new CertificateHolder();
        certificateHolder.setId(certificate.getId());
        certificateHolder.setType(certificate.getType());
        certificateHolder.setOriginalCertificate(certificate.getOriginalCertificate() == null ? null
                : certificate.getOriginalCertificate().getDocument());
        certificateHolder.setCareUnitId(certificate.getCareUnitId());
        certificateHolder.setCareUnitName(certificate.getCareUnitName());
        certificateHolder.setCareGiverId(certificate.getCareGiverId());
        certificateHolder.setSigningDoctorName(certificate.getSigningDoctorName());
        certificateHolder.setSignedDate(certificate.getSignedDate());
        certificateHolder.setCivicRegistrationNumber(certificate.getCivicRegistrationNumber());
        certificateHolder.setAdditionalInfo(certificate.getAdditionalInfo());
        certificateHolder.setDeleted(certificate.isDeleted());
        certificateHolder.setValidFromDate(certificate.getValidFromDate());
        certificateHolder.setValidToDate(certificate.getValidToDate());
        certificateHolder.setDeletedByCareGiver(certificate.isDeletedByCareGiver());
        List<CertificateStateHolder> certificateStates = new ArrayList<>(certificate.getStates().size());
        for (CertificateStateHistoryEntry certificateStateEntry : certificate.getStates()) {
            CertificateStateHolder certificateStateHolder = new CertificateStateHolder();
            certificateStateHolder.setTarget(certificateStateEntry.getTarget());
            certificateStateHolder.setState(certificateStateEntry.getState());
            certificateStateHolder.setTimestamp(certificateStateEntry.getTimestamp());
            certificateStates.add(certificateStateHolder);
        }
        certificateHolder.setCertificateStates(certificateStates);
        certificateHolder.setRevoked(certificate.isRevoked());
        return certificateHolder;
    }
}
