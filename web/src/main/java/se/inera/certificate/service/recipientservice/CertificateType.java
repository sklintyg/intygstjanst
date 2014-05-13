package se.inera.certificate.service.recipientservice;

import static org.springframework.util.Assert.hasText;

public class CertificateType {

    private final String id;

    public CertificateType(String id) {
        hasText(id, "Id must not be empty");

        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
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
        return id.equals(other.id);
    }
}
