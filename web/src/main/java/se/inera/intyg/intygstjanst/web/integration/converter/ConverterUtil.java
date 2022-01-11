/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.CertificateStateHolder;
import se.inera.intyg.common.support.modules.support.api.dto.AdditionalMetaData;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateMetaData;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;

public final class ConverterUtil {

    private ConverterUtil() {
    }

    public static Certificate toCertificate(CertificateHolder certificateHolder) {
        Certificate certificate = new Certificate(certificateHolder.getId());

        certificate.setType(certificateHolder.getType());
        certificate.setTypeVersion(certificateHolder.getTypeVersion());
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
                certificateStates
                    .add(new CertificateStateHistoryEntry(certificateStateHolder.getTarget(), certificateStateHolder.getState(),
                        certificateStateHolder.getTimestamp()));
            }
            certificate.setStates(certificateStates);
        }

        final var diagnoses = getDiagnoses(certificateHolder.getAdditionalMetaData());
        certificate.setCertificateMetaData(new CertificateMetaData(certificate, certificateHolder.getSigningDoctorId(),
            certificateHolder.getSigningDoctorName(), certificateHolder.isRevoked(), diagnoses));

        return certificate;
    }

    public static String getDiagnoses(AdditionalMetaData additionalMetaData) {
        if (additionalMetaData == null || additionalMetaData.getDiagnoses() == null || additionalMetaData.getDiagnoses().isEmpty()) {
            return null;
        }
        return StringUtils.join(additionalMetaData.getDiagnoses());
    }

    public static CertificateHolder toCertificateHolder(Certificate certificate) {
        CertificateHolder certificateHolder = new CertificateHolder();
        certificateHolder.setId(certificate.getId());
        certificateHolder.setType(certificate.getType());
        certificateHolder.setTypeVersion(certificate.getTypeVersion());
        certificateHolder.setOriginalCertificate(
            certificate.getOriginalCertificate() == null ? null : certificate.getOriginalCertificate().getDocument());
        certificateHolder.setCareUnitId(certificate.getCareUnitId());
        certificateHolder.setCareUnitName(certificate.getCareUnitName());
        certificateHolder.setCareGiverId(certificate.getCareGiverId());
        certificateHolder
            .setSigningDoctorId(certificate.getCertificateMetaData() == null ? null : certificate.getCertificateMetaData().getDoctorId());
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
            certificateStates.add(new CertificateStateHolder(certificateStateEntry.getTarget(), certificateStateEntry.getState(),
                certificateStateEntry.getTimestamp()));
        }
        certificateHolder.setCertificateStates(certificateStates);
        certificateHolder.setRevoked(certificate.isRevoked());
        certificateHolder.setAdditionalMetaData(certificate.getCertificateMetaData() != null ? getAdditionalMetaData(
            certificate.getCertificateMetaData()) : null);
        return certificateHolder;
    }

    private static AdditionalMetaData getAdditionalMetaData(CertificateMetaData certificateMetaData) {
        final var addtionalMetaData = new AdditionalMetaData();

        final var diagnoses = getDiagnoses(certificateMetaData.getDiagnoses());

        addtionalMetaData.setDiagnoses(diagnoses);

        return addtionalMetaData;
    }

    private static List<String> getDiagnoses(String diagnosesAsString) {
        if (diagnosesAsString == null || diagnosesAsString.trim().length() == 0) {
            return Collections.emptyList();
        }

        return Arrays.asList(
            diagnosesAsString
                .replaceAll("\\[|\\]", "")
                .split("\\s*,\\s*")
        );
    }
}
