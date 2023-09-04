package se.inera.intyg.intygstjanst.persistence.model.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Entity
@Data
@Table(name="CERTIFICATE")
public class CitizenCertificate {
    @Id
    @Column(name = "ID", nullable = false)
    private String id;
    @Column(name = "CERTIFICATE_TYPE", nullable = false)
    private String type;
    @Column(name = "CERTIFICATE_TYPE_VERSION", nullable = false)
    private String typeVersion;
    @Column(name = "CIVIC_REGISTRATION_NUMBER", nullable = false)
    private String patientId;
    @Column(name = "CARE_UNIT_NAME", nullable = false)
    private String unitName;
    @Column(name = "CARE_UNIT_ID", nullable = false)
    private String unitId;
    @Column(name = "SIGNING_DOCTOR_NAME", nullable = false)
    private String doctorName;
    @Column(name = "ADDITIONAL_INFO", nullable = false)
    private String additionalInfo;
    @Column(name = "SIGNED_DATE", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private String signedDate;
    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "certificate", cascade = CascadeType.REMOVE)
    private CertificateMetaData certificateMetaData;
}
