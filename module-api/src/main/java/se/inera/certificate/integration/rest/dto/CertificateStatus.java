package se.inera.certificate.integration.rest.dto;

import java.io.Serializable;

import org.joda.time.LocalDateTime;

public class CertificateStatus implements Serializable {

    private String type;

    private String target;

    private LocalDateTime timestamp;

    public CertificateStatus() {
    }

    public CertificateStatus(String type, String target, LocalDateTime timestamp) {
        this.type = type;
        this.target = target;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
