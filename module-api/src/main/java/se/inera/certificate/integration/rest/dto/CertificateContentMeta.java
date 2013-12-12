package se.inera.certificate.integration.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDateTime;

/**
 * Wrapper class that holds meta information about a certificate, such as the list of statuses
 * @author marced
 */
@SuppressWarnings("serial")
public class CertificateContentMeta implements Serializable {

    private String id;
    private String type;
    private String patientId;
    private LocalDateTime fromDate;
    private LocalDateTime tomDate;
    private List<CertificateStatus> statuses = new ArrayList<>();

    public LocalDateTime getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDateTime fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDateTime getTomDate() {
        return tomDate;
    }

    public void setTomDate(LocalDateTime tomDate) {
        this.tomDate = tomDate;
    }

    public List<CertificateStatus> getStatuses() {
        return statuses;
    }

    public void setStatuses(List<CertificateStatus> statuses) {
        this.statuses = statuses;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
}
