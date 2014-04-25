package se.inera.certificate.integration.builder;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.CertificateMetaType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.CertificateStatusType;
import se.inera.certificate.clinicalprocess.healthcond.certificate.v1.StatusType;

public class ClinicalProcessCertificateMetaTypeBuilder {

    private CertificateMetaType metaType;

    public ClinicalProcessCertificateMetaTypeBuilder() {
        metaType = new CertificateMetaType();
    }

    public CertificateMetaType build() {
        return metaType;
    }

    public ClinicalProcessCertificateMetaTypeBuilder certificateId(String certificateId) {
        metaType.setCertificateId(certificateId);
        return this;
    }

    public ClinicalProcessCertificateMetaTypeBuilder certificateType(String certificateType) {
        metaType.setCertificateType(certificateType);
        return this;
    }

    public ClinicalProcessCertificateMetaTypeBuilder validity(LocalDate fromDate, LocalDate toDate) {
        metaType.setValidFrom(fromDate);
        metaType.setValidTo(toDate);
        return this;
    }

    public ClinicalProcessCertificateMetaTypeBuilder issuerName(String issuerName) {
        metaType.setIssuerName(issuerName);
        return this;
    }

    public ClinicalProcessCertificateMetaTypeBuilder facilityName(String facilityName) {
        metaType.setFacilityName(facilityName);
        return this;
    }

    public ClinicalProcessCertificateMetaTypeBuilder signDate(LocalDateTime signDate) {
        metaType.setSignDate(signDate);
        return this;
    }

    public ClinicalProcessCertificateMetaTypeBuilder available(String available) {
        metaType.setAvailable(available);
        return this;
    }

    public ClinicalProcessCertificateMetaTypeBuilder status(StatusType status, String target, LocalDateTime timestamp) {
        CertificateStatusType certificateStatusType = new CertificateStatusType();
        certificateStatusType.setTarget(target);
        certificateStatusType.setTimestamp(timestamp);
        certificateStatusType.setType(status);
        metaType.getStatus().add(certificateStatusType);
        return this;
    }

    public ClinicalProcessCertificateMetaTypeBuilder status(CertificateStatusType certificateStatusType) {
        metaType.getStatus().add(certificateStatusType);
        return this;
    }

    public ClinicalProcessCertificateMetaTypeBuilder complementaryInfo(String complemenatryInfo) {
        metaType.setComplemantaryInfo(complemenatryInfo);
        return this;
    }
}
