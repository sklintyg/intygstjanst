package se.inera.certificate.integration.converter;

import java.util.ArrayList;
import java.util.List;

import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.model.dao.CertificateStateHistoryEntry;
import se.inera.certificate.modules.support.api.CertificateHolder;
import se.inera.certificate.modules.support.api.CertificateStateHolder;

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
        certificate.setCivicRegistrationNumber(certificateHolder.getCivicRegistrationNumber());
        certificate.setValidFromDate(certificateHolder.getValidFromDate());
        certificate.setValidToDate(certificateHolder.getValidToDate());
        certificate.setAdditionalInfo(certificateHolder.getAdditionalInfo());
        return certificate;
    }

    public static CertificateHolder toCertificateHolder(Certificate certificate) {
        CertificateHolder certificateHolder = new CertificateHolder();
        certificateHolder.setId(certificate.getId());
        certificateHolder.setCareUnitId(certificate.getCareUnitId());
        certificateHolder.setCareUnitName(certificate.getCareUnitName());
        certificateHolder.setSigningDoctorName(certificate.getSigningDoctorName());
        certificateHolder.setSignedDate(certificate.getSignedDate());
        certificateHolder.setCivicRegistrationNumber(certificate.getCivicRegistrationNumber());
        certificateHolder.setAdditionalInfo(certificate.getAdditionalInfo());
        certificateHolder.setDocument(certificate.getDocument());
        certificateHolder.setDeleted(certificate.getDeleted());
        List<CertificateStateHolder> certificateStates = new ArrayList<CertificateStateHolder>(certificate.getStates().size());
        for (CertificateStateHistoryEntry certificateStateEntry : certificate.getStates()) {
            CertificateStateHolder certificateStateHolder = new CertificateStateHolder();
            certificateStateHolder.setTarget(certificateStateEntry.getTarget());
            certificateStateHolder.setState(certificateStateEntry.getState());
            certificateStateHolder.setTimestamp(certificateStateEntry.getTimestamp());
        }
        certificateHolder.setCertificateStates(certificateStates);
        return certificateHolder;
    }
}
