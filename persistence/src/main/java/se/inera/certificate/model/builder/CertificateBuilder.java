package se.inera.certificate.model.builder;

import org.joda.time.LocalDateTime;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.dao.Certificate;
import se.inera.certificate.model.dao.CertificateStateHistoryEntry;

/**
 * @author andreaskaltenbach
 */
public class CertificateBuilder {

    private Certificate certificate;

    public CertificateBuilder(String certificateId) {
        this(certificateId, "");
    }

    public CertificateBuilder(String certificateId, String document) {
        this.certificate = new Certificate(certificateId, document);
    }

    public CertificateBuilder certificateType(String certificateType) {
        certificate.setType(certificateType);
        return this;
    }

    public CertificateBuilder civicRegistrationNumber(String civicRegistrationNumber) {
        certificate.setCivicRegistrationNumber(civicRegistrationNumber);
        return this;
    }

    public CertificateBuilder validity(String fromDate, String toDate) {
        certificate.setValidFromDate(fromDate);
        certificate.setValidToDate(toDate);
        return this;
    }

    public CertificateBuilder careUnitId(String careUnitId) {
        certificate.setCareUnitId(careUnitId);
        return this;
    }

    public CertificateBuilder careUnitName(String careUnitName) {
            certificate.setCareUnitName(careUnitName);
            return this;
        }

    public CertificateBuilder signingDoctorName(String signingDoctorName) {
        certificate.setSigningDoctorName(signingDoctorName);
        return this;
    }

    public CertificateBuilder signedDate(LocalDateTime signedDate) {
        certificate.setSignedDate(signedDate);
        return this;
    }

    public CertificateBuilder deleted(boolean deleted) {
        certificate.setDeleted(deleted);
        return this;
    }

    public CertificateBuilder state(CertificateState state, String target) {
        return state(state, target, null);
    }

    public CertificateBuilder state(CertificateState state, String target, LocalDateTime timestamp) {
        certificate.addState(new CertificateStateHistoryEntry(target, state, timestamp));
        return this;
    }

    public Certificate build() {
        return certificate;
    }
}