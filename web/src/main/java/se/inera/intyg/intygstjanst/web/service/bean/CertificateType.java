package se.inera.certificate.service.bean;

import static org.springframework.util.Assert.hasText;

public class CertificateType {

    private final String certificateTypeId;

    public CertificateType(String certificateTypeId) {
        hasText(certificateTypeId, "certificateTypeId must not be empty");
        this.certificateTypeId = certificateTypeId;
    }

    public String getCertificateTypeId() {
        return certificateTypeId;
    }

    @Override
    public String toString() {
        return certificateTypeId;
    }

    @Override
    public int hashCode() {
        return certificateTypeId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() != obj.getClass()) {
            return false;
        }
        CertificateType other = (CertificateType) obj;
        return certificateTypeId.equals(other.certificateTypeId);
    }
}
