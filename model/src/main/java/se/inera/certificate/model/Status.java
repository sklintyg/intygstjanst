package se.inera.certificate.model;

import org.joda.time.LocalDateTime;

/**
 * @author andreaskaltenbach
 */
public class Status {

    private CertificateState type;

    private String target;

    private LocalDateTime timestamp;

    public CertificateState getType() {
        return type;
    }

    public void setType(CertificateState type) {
        this.type = type;
    }

    public final String getTarget() {
        return target;
    }

    public final void setTarget(String target) {
        this.target = target;
    }

    public final LocalDateTime getTimestamp() {
        return timestamp;
    }

    public final void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
