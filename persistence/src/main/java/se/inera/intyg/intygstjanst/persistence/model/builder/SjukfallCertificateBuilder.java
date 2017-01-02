/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.persistence.model.builder;

import java.time.LocalDateTime;
import java.util.List;

import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateWorkCapacity;

/**
 * Created by eriklupander on 2016-02-04.
 */
public class SjukfallCertificateBuilder {

    private SjukfallCertificate certificate;

    public SjukfallCertificateBuilder(String certificateId) {
        this.certificate = new SjukfallCertificate(certificateId);
    }

    public SjukfallCertificateBuilder certificateType(String certificateType) {
        certificate.setType(certificateType);
        return this;
    }

    public SjukfallCertificateBuilder civicRegistrationNumber(String civicRegistrationNumber) {
        certificate.setCivicRegistrationNumber(civicRegistrationNumber);
        return this;
    }

    public SjukfallCertificateBuilder patientName(String patientName) {
        certificate.setPatientName(patientName);
        return this;
    }

    public SjukfallCertificateBuilder careGiverId(String careGiverId) {
        certificate.setCareGiverId(careGiverId);
        return this;
    }

    public SjukfallCertificateBuilder careUnitId(String careUnitId) {
        certificate.setCareUnitId(careUnitId);
        return this;
    }

    public SjukfallCertificateBuilder careUnitName(String careUnitName) {
        certificate.setCareUnitName(careUnitName);
        return this;
    }

    public SjukfallCertificateBuilder signingDoctorId(String signingDoctorId) {
        certificate.setSigningDoctorId(signingDoctorId);
        return this;
    }

    public SjukfallCertificateBuilder signingDoctorName(String signingDoctorName) {
        certificate.setSigningDoctorName(signingDoctorName);
        return this;
    }

    public SjukfallCertificateBuilder signingDateTime(LocalDateTime signingDateTime) {
        certificate.setSigningDateTime(signingDateTime);
        return this;
    }

    public SjukfallCertificateBuilder diagnoseCode(String diagnoseCode) {
        certificate.setDiagnoseCode(diagnoseCode);
        return this;
    }

    public SjukfallCertificateBuilder deleted(boolean deleted) {
        certificate.setDeleted(deleted);
        return this;
    }

    public SjukfallCertificateBuilder workCapacities(List<SjukfallCertificateWorkCapacity> workCapacities) {
        certificate.setSjukfallCertificateWorkCapacity(workCapacities);
        return this;
    }

    public SjukfallCertificate build() {
        return certificate;
    }


}
