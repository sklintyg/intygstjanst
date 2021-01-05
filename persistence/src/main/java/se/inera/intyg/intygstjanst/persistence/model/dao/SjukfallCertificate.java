/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.intyg.intygstjanst.persistence.model.dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

/**
 * Created by eriklupander on 2016-02-02.
 */
@Entity
@Table(name = "SJUKFALL_CERT")
public class SjukfallCertificate {

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
     * Date and time when the certificate was signed.
     */
    @Column(name = "SIGNING_DATETIME", nullable = false)
    @Type(type = "org.jadira.usertype.dateandtime.threeten.PersistentLocalDateTime")
    private LocalDateTime signingDateTime;

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
     * Patient's full name including first, middle and last name.
     */
    @Column(name = "PATIENT_NAME", nullable = true)
    private String patientName;

    /**
     * Main diagnose code.
     */
    @Column(name = "DIAGNOSE_CODE", nullable = false)
    private String diagnoseCode;

    /**
     * Bi-diagnose code 1.
     */
    @Column(name = "BI_DIAGNOSE_CODE_1", nullable = true)
    private String biDiagnoseCode1;

    /**
     * Bi-diagnose code 2.
     */
    @Column(name = "BI_DIAGNOSE_CODE_2", nullable = true)
    private String biDiagnoseCode2;

    /**
     * Sysselsattning. If the patient has > 1 the values must be comma-separated.
     */
    @Column(name = "EMPLOYMENT", nullable = true)
    private String employment;

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

    /**
     * If this certificate was registered at a time when the patient had testIndicator-flag.
     */
    @Column(name = "TEST_CERTIFICATE", nullable = false, columnDefinition = "TINYINT(1")
    private boolean testCertificate = false;

    private SjukfallCertificate() {
    }

    public SjukfallCertificate(String id) {
        this.id = id;
    }

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

    public LocalDateTime getSigningDateTime() {
        return signingDateTime;
    }

    public void setSigningDateTime(LocalDateTime signingDateTime) {
        this.signingDateTime = signingDateTime;
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

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
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

    public String getBiDiagnoseCode1() {
        return biDiagnoseCode1;
    }

    public void setBiDiagnoseCode1(String biDiagnoseCode1) {
        this.biDiagnoseCode1 = biDiagnoseCode1;
    }

    public String getBiDiagnoseCode2() {
        return biDiagnoseCode2;
    }

    public void setBiDiagnoseCode2(String biDiagnoseCode2) {
        this.biDiagnoseCode2 = biDiagnoseCode2;
    }

    public String getEmployment() {
        return employment;
    }

    public void setEmployment(String employment) {
        this.employment = employment;
    }

    public void setTestCertificate(boolean isTestCertificate) {
        this.testCertificate = isTestCertificate;
    }

    public boolean isTestCertificate() {
        return testCertificate;
    }
}
