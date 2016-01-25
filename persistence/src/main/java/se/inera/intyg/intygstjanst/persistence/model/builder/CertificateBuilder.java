/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import org.joda.time.LocalDateTime;

import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;

/**
 * @author andreaskaltenbach
 */
public class CertificateBuilder {

    private Certificate certificate;

    public CertificateBuilder(String certificateId) {
        this(certificateId, "");
    }

    public CertificateBuilder(String certificateId, String document) {
        this.certificate = new Certificate(certificateId, document);
    }

    public CertificateBuilder certificateType(String certificateType) {
        certificate.setType(certificateType);
        return this;
    }

    public CertificateBuilder civicRegistrationNumber(Personnummer civicRegistrationNumber) {
        certificate.setCivicRegistrationNumber(civicRegistrationNumber);
        return this;
    }

    public CertificateBuilder validity(String fromDate, String toDate) {
        certificate.setValidFromDate(fromDate);
        certificate.setValidToDate(toDate);
        return this;
    }

    public CertificateBuilder careUnitId(String careUnitId) {
        certificate.setCareUnitId(careUnitId);
        return this;
    }

    public CertificateBuilder careUnitName(String careUnitName) {
            certificate.setCareUnitName(careUnitName);
            return this;
        }

    public CertificateBuilder careGiverId(String careGiverId) {
        certificate.setCareGiverId(careGiverId);
        return this;
    }

    public CertificateBuilder signingDoctorName(String signingDoctorName) {
        certificate.setSigningDoctorName(signingDoctorName);
        return this;
    }

    public CertificateBuilder signedDate(LocalDateTime signedDate) {
        certificate.setSignedDate(signedDate);
        return this;
    }

    public CertificateBuilder deleted(boolean deleted) {
        certificate.setDeleted(deleted);
        return this;
    }

    public CertificateBuilder wireTapped(boolean wireTapped) {
        certificate.setWireTapped(wireTapped);
        return this;
    }

    public CertificateBuilder state(CertificateState state, String target) {
        return state(state, target, null);
    }

    public CertificateBuilder state(CertificateState state, String target, LocalDateTime timestamp) {
        certificate.addState(new CertificateStateHistoryEntry(target, state, timestamp));
        return this;
    }

    public CertificateBuilder originalCertificate(String certificateXML) {
        OriginalCertificate originalCertificate = new OriginalCertificate();
        originalCertificate.setDocument(certificateXML);
        certificate.setOriginalCertificate(originalCertificate);
        return this;
    }

    public Certificate build() {
        return certificate;
    }
}
