package se.inera.intyg.intygstjanst.web.service.bean;

import static org.springframework.util.Assert.hasText;

public class RecipientCertificateType extends CertificateType {

    private final String recipientId;

    public RecipientCertificateType(String recipientId, String certificateTypeId) {
        super(certificateTypeId);
        hasText(recipientId, "recipientId must not be empty");
        this.recipientId = recipientId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + getCertificateTypeId().hashCode();
        result = prime * result + getRecipientId().hashCode();
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
        if (!getCertificateTypeId().equals(other.getCertificateTypeId())) {
            return false;
        } else {
            return getRecipientId().equals(other.getRecipientId());
        }
    }
}
