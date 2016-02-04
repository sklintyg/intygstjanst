package se.inera.intyg.intygstjanst.persistence.model.builder;

import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateWorkCapacity;

import java.util.List;

/**
 * Created by eriklupander on 2016-02-04.
 */
public class SjukfallCertificateBuilder {

    private SjukfallCertificate certificate;

    public SjukfallCertificateBuilder(String certificateId) {
        this.certificate = new SjukfallCertificate(certificateId);
    }

    public SjukfallCertificateBuilder certificateType(String certificateType) {
        certificate.setType(certificateType);
        return this;
    }

    public SjukfallCertificateBuilder civicRegistrationNumber(String civicRegistrationNumber) {
        certificate.setCivicRegistrationNumber(civicRegistrationNumber);
        return this;
    }

    public SjukfallCertificateBuilder patientFirstName(String patientFirstName) {
        certificate.setPatientFirstName(patientFirstName);
        return this;
    }

    public SjukfallCertificateBuilder patientLastName(String patientLastName) {
        certificate.setPatientLastName(patientLastName);
        return this;
    }

    public SjukfallCertificateBuilder careGiverId(String careGiverId) {
        certificate.setCareGiverId(careGiverId);
        return this;
    }

    public SjukfallCertificateBuilder careUnitId(String careUnitId) {
        certificate.setCareUnitId(careUnitId);
        return this;
    }

    public SjukfallCertificateBuilder careUnitName(String careUnitName) {
        certificate.setCareUnitName(careUnitName);
        return this;
    }

    public SjukfallCertificateBuilder signingDoctorId(String signingDoctorId) {
        certificate.setSigningDoctorId(signingDoctorId);
        return this;
    }

    public SjukfallCertificateBuilder signingDoctorName(String signingDoctorName) {
        certificate.setSigningDoctorName(signingDoctorName);
        return this;
    }

    public SjukfallCertificateBuilder diagnoseCode(String diagnoseCode) {
        certificate.setDiagnoseCode(diagnoseCode);
        return this;
    }

    public SjukfallCertificateBuilder deleted(boolean deleted) {
        certificate.setDeleted(deleted);
        return this;
    }

    public SjukfallCertificateBuilder workCapacities(List<SjukfallCertificateWorkCapacity> workCapacities) {
        certificate.setSjukfallCertificateWorkCapacity(workCapacities);
        return this;
    }

    public SjukfallCertificate build() {
        return certificate;
    }


}
