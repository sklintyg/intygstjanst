/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "CERTIFICATE_METADATA")
public class CertificateMetaData {

    @Id
    @Column(name = "CERTIFICATE_ID")
    private String certificateId;

    @Column(name = "DOCTOR_ID")
    private String doctorId;

    @Column(name = "DOCTOR_NAME")
    private String doctorName;

    @Column(name = "IS_REVOKED")
    private boolean isRevoked;

    @Column(name = "DIAGNOSES")
    private String diagnoses;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CERTIFICATE_ID")
    private Certificate certificate;


    public CertificateMetaData() {
    }

    public CertificateMetaData(Certificate certificate, String doctorId, String doctorName, boolean isRevoked, String diagnoses) {
        this.certificateId = certificate.getId();
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.isRevoked = isRevoked;
        this.certificate = certificate;
        this.diagnoses = diagnoses;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public void setCertificateId(String certificateId) {
        this.certificateId = certificateId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public boolean isRevoked() {
        return isRevoked;
    }

    public void setRevoked(boolean revoked) {
        isRevoked = revoked;
    }

    public Certificate getCertificate() {
        return certificate;
    }

    public void setCertificate(Certificate certificate) {
        this.certificate = certificate;
    }

    public String getDiagnoses() {
        return diagnoses;
    }

    public void setDiagnoses(String diagnoses) {
        this.diagnoses = diagnoses;
    }
}
