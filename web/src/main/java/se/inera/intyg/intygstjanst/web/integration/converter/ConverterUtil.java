package se.inera.intyg.intygstjanst.web.integration.converter;

import java.util.ArrayList;
import java.util.List;

import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.common.support.modules.support.api.CertificateHolder;
import se.inera.intyg.common.support.modules.support.api.CertificateStateHolder;

public final class ConverterUtil {

    private ConverterUtil() {
    }

    public static Certificate toCertificate(CertificateHolder certificateHolder) {
        Certificate certificate = new Certificate(certificateHolder.getId(), certificateHolder.getDocument());

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
        certificate.setWireTapped(certificateHolder.isWireTapped());
        certificate.setAdditionalInfo(certificateHolder.getAdditionalInfo());
        if (certificateHolder.getCertificateStates() != null) {
            List<CertificateStateHistoryEntry> certificateStates = new ArrayList<CertificateStateHistoryEntry>(certificateHolder.getCertificateStates().size());
            for (CertificateStateHolder certificateStateHolder : certificateHolder.getCertificateStates()) {
                CertificateStateHistoryEntry certificateState = new CertificateStateHistoryEntry();
                certificateState.setTarget(certificateStateHolder.getTarget());
                certificateState.setState(certificateStateHolder.getState());
                certificateState.setTimestamp(certificateStateHolder.getTimestamp());
                certificateStates.add(certificateState);
            }
            certificate.setStates(certificateStates);
        }
        return certificate;
    }

    public static CertificateHolder toCertificateHolder(Certificate certificate) {
        CertificateHolder certificateHolder = new CertificateHolder();
        certificateHolder.setId(certificate.getId());
        certificateHolder.setType(certificate.getType());
        certificateHolder.setCareUnitId(certificate.getCareUnitId());
        certificateHolder.setCareUnitName(certificate.getCareUnitName());
        certificateHolder.setCareGiverId(certificate.getCareGiverId());
        certificateHolder.setSigningDoctorName(certificate.getSigningDoctorName());
        certificateHolder.setSignedDate(certificate.getSignedDate());
        certificateHolder.setCivicRegistrationNumber(certificate.getCivicRegistrationNumber());
        certificateHolder.setAdditionalInfo(certificate.getAdditionalInfo());
        certificateHolder.setDocument(certificate.getDocument());
        certificateHolder.setDeleted(certificate.getDeleted());
        certificateHolder.setValidFromDate(certificate.getValidFromDate());
        certificateHolder.setValidToDate(certificate.getValidToDate());
        certificateHolder.setDeletedByCareGiver(certificate.isDeletedByCareGiver());
        certificateHolder.setWireTapped(certificate.isWireTapped());
        List<CertificateStateHolder> certificateStates = new ArrayList<CertificateStateHolder>(certificate.getStates().size());
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
