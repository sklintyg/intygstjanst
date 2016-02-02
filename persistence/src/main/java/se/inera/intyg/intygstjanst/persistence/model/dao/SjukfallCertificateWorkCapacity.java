package se.inera.intyg.intygstjanst.persistence.model.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Created by eriklupander on 2016-02-02.
 */
@Entity
@Table(name = "SJUKFALL_CERT_WORK_CAPACITY")
public class SjukfallCertificateWorkCapacity {

    /**
     * Just needed for JPA compliance.
     */
    @Id
    @GeneratedValue
    @Column(name = "ID", nullable = false)
    private long id;

    @Column(name = "CAPACITY_PERCENTAGE", nullable = false)
    private Integer capacityPercentage;

    /**
     * Time from which this certificate is valid.
     */
    @Column(name = "FROM_DATE", nullable = false)
    private String fromDate;

    /**
     * Time to which this certificate is valid.
     */
    @Column(name = "TO_DATE", nullable = false)
    private String toDate;

    public long getId() {
        return id;
    }

    public Integer getCapacityPercentage() {
        return capacityPercentage;
    }

    public void setCapacityPercentage(Integer capacityPercentage) {
        this.capacityPercentage = capacityPercentage;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }
}
