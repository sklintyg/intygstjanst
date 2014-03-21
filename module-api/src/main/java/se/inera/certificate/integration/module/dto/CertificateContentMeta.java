package se.inera.certificate.integration.module.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;

/**
 * Wrapper class that holds meta information about a certificate, such as the list of statuses
 * @author marced
 */
@SuppressWarnings("serial")
public class CertificateContentMeta implements Serializable {

    private String id;
    private String type;
    private String patientId;
    private LocalDate fromDate;
    private LocalDate tomDate;
    private List<CertificateStatus> statuses = new ArrayList<>();

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getTomDate() {
        return tomDate;
    }

    public void setTomDate(LocalDate tomDate) {
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
