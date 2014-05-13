package se.inera.certificate.service.recipientservice;

import static org.springframework.util.Assert.hasText;

public class RecipientCertificateType {
    private final String recipientId;

    private final String certificateTypeId;

    public RecipientCertificateType(String recipientId, String certificateTypeId) {
        hasText("id must not be empty", recipientId);
        hasText("certTypeId must not be empty", certificateTypeId);
        this.recipientId = recipientId;
        this.certificateTypeId = certificateTypeId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getCertificateTypeId() {
        return certificateTypeId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + certificateTypeId.hashCode();
        result = prime * result + recipientId.hashCode();
        return result;
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
        RecipientCertificateType other = (RecipientCertificateType) obj;
        if (!certificateTypeId.equals(other.certificateTypeId)) {
            return false;
        } else {
            return recipientId.equals(other.recipientId);
        }
    }
}
