package se.inera.intyg.intygstjanst.persistence.model.dao;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by eriklupander on 2016-02-02.
 */
@Entity
@Table(name = "SJUKFALL_CERT")
public class SjukfallCertificate {

    private SjukfallCertificate() {

    }

    public SjukfallCertificate(String id) {
        this.id = id;
    }

    /**
     * Id of the certificate.
     */
    @Id
    @Column(name = "ID")
    private String id;

    /**
     * Type of the certificate.
     */
    @Column(name = "CERTIFICATE_TYPE", nullable = false)
    private String type;

    /**
     * Name of the doctor that signed the certificate.
     */
    @Column(name = "SIGNING_DOCTOR_ID", nullable = false)
    private String signingDoctorId;

    /**
     * Name of the doctor that signed the certificate.
     */
    @Column(name = "SIGNING_DOCTOR_NAME", nullable = false)
    private String signingDoctorName;

    /**
     * Id of care unit.
     */
    @Column(name = "CARE_UNIT_ID", nullable = false)
    private String careUnitId;

    /**
     * Name of care unit.
     */
    @Column(name = "CARE_UNIT_NAME", nullable = false)
    private String careUnitName;

    /**
     * Id of care giver.
     */
    @Column(name = "CARE_GIVER_ID", nullable = false)
    private String careGiverId;

    /**
     * Civic registration number for patient.
     */
    @Column(name = "CIVIC_REGISTRATION_NUMBER", nullable = false)
    private String civicRegistrationNumber;


    /**
     * Patient first name
     */
    @Column(name = "PATIENT_FIRST_NAME", nullable = true)
    private String patientFirstName;

    /**
     * Patient last name
     */
    @Column(name = "PATIENT_LAST_NAME", nullable = true)
    private String patientLastName;

    /**
     * Main diagnose code
     */
    @Column(name = "DIAGNOSE_CODE", nullable = false)
    private String diagnoseCode;

    /**
     * If this certificate is deleted or not.
     */
    @Column(name = "DELETED", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean deleted = Boolean.FALSE;

    /**
     * Arbetsförmåga på intyget.
     */
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "CERTIFICATE_ID")
    private List<SjukfallCertificateWorkCapacity> sjukfallCertificateWorkCapacity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSigningDoctorId() {
        return signingDoctorId;
    }

    public void setSigningDoctorId(String signingDoctorId) {
        this.signingDoctorId = signingDoctorId;
    }

    public String getSigningDoctorName() {
        return signingDoctorName;
    }

    public void setSigningDoctorName(String signingDoctorName) {
        this.signingDoctorName = signingDoctorName;
    }

    public String getCareUnitId() {
        return careUnitId;
    }

    public void setCareUnitId(String careUnitId) {
        this.careUnitId = careUnitId;
    }

    public String getCareUnitName() {
        return careUnitName;
    }

    public void setCareUnitName(String careUnitName) {
        this.careUnitName = careUnitName;
    }

    public String getCareGiverId() {
        return careGiverId;
    }

    public void setCareGiverId(String careGiverId) {
        this.careGiverId = careGiverId;
    }

    public String getCivicRegistrationNumber() {
        return civicRegistrationNumber;
    }

    public void setCivicRegistrationNumber(String civicRegistrationNumber) {
        this.civicRegistrationNumber = civicRegistrationNumber;
    }

    public String getPatientFirstName() {
        return patientFirstName;
    }

    public void setPatientFirstName(String patientFirstName) {
        this.patientFirstName = patientFirstName;
    }

    public String getPatientLastName() {
        return patientLastName;
    }

    public void setPatientLastName(String patientLastName) {
        this.patientLastName = patientLastName;
    }

    public String getDiagnoseCode() {
        return diagnoseCode;
    }

    public void setDiagnoseCode(String diagnoseCode) {
        this.diagnoseCode = diagnoseCode;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public List<SjukfallCertificateWorkCapacity> getSjukfallCertificateWorkCapacity() {
        if (sjukfallCertificateWorkCapacity == null) {
            sjukfallCertificateWorkCapacity = new ArrayList<>();
        }
        return sjukfallCertificateWorkCapacity;
    }

    public void setSjukfallCertificateWorkCapacity(List<SjukfallCertificateWorkCapacity> sjukfallCertificateWorkCapacity) {
        this.sjukfallCertificateWorkCapacity = sjukfallCertificateWorkCapacity;
    }
}
