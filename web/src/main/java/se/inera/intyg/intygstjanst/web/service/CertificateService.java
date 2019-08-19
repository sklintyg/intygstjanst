/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateTypeInfo;
import se.inera.intyg.schemas.contract.Personnummer;

/**
 * @author andreaskaltenbach
 */
public interface CertificateService {

    enum SendStatus {
        ALREADY_SENT,
        OK
    }

    /**
     * Returns the list of certificates for the patient and filter criteria.
     *
     * @param civicRegistrationNumber the patient's civic registration number
     * @param certificateTypes optional certificate type filter. If empty or null, all certificate types will be returned
     * @param fromDate optional from date filter
     * @param toDate optional to date filter
     * @return list of matching certificates or empty list if no such certificates can be found
     */
    List<Certificate> listCertificatesForCitizen(Personnummer civicRegistrationNumber, List<String> certificateTypes, LocalDate fromDate,
        LocalDate toDate);

    /**
     * Returns a list of certificates for one or many care units.
     *
     * @param civicRegistrationNumber the patient's civic registration number
     * @param careUnits a list of care units for which the certificates must belong.
     * @return list of matching certificates or empty list if no such certificates can be found
     */
    List<Certificate> listCertificatesForCare(Personnummer civicRegistrationNumber, List<String> careUnits);

    /**
     * Returns the certificate for the given patient and certificate ID.
     * Implementation should not return revoked certificates - but rather throw an {@link CertificateRevokedException}
     *
     * @param civicRegistrationNumber the patient's civic registration number that must match same info on certificate
     * @param certificateId the certificate ID
     * @return the certificate information or null if the requested certificate does not exist
     * @throws InvalidCertificateException if the certificate does not exist or the certificate id and civicRegistrationNumber didn't match
     * @throws CertificateRevokedException if the certificate has been revoked
     */
    Certificate getCertificateForCitizen(Personnummer civicRegistrationNumber, String certificateId) throws InvalidCertificateException,
        CertificateRevokedException;

    /**
     * Returns the certificate for the given certificate ID.
     * Implementation should also return revoked certificates - but with resultCode REVOKED
     *
     * @param certificateId the certificate ID
     * @return the certificate information or null if the requested certificate does not exist
     * @throws InvalidCertificateException if the certificate does not exist or the certificate id and civicRegistrationNumber didn't match
     */
    Certificate getCertificateForCare(String certificateId) throws InvalidCertificateException;

    /**
     * Returns the certificate's type for the given certificate ID.
     *
     * @param certificateId the certificate ID
     * @return the certificate's type or null if the requested certificate does not exist
     */
    CertificateTypeInfo getCertificateTypeInfo(String certificateId);

    /**
     * Sends a certificate to a destined recipient.
     *
     * @param civicRegistrationNumber The identifier of a patient.
     * @param certificateId The identifier of the certificate.
     * @param recipientId The identifier of the recipient.
     * @throws se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException if the certificate does not exist or
     * the certificate id and civicRegistrationNumber didn't match
     * @throws se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException if the certificate has been revoked
     * @returns SendStatus further subclassifying the outcome of a successful send
     */
    SendStatus sendCertificate(Personnummer civicRegistrationNumber, String certificateId, String recipientId)
        throws InvalidCertificateException, CertificateRevokedException, RecipientUnknownException;

    void setCertificateState(Personnummer civicRegistrationNumber, String certificateId, String target, CertificateState state,
        LocalDateTime timestamp)
        throws InvalidCertificateException;

    void setCertificateState(String certificateId, String target, CertificateState state, LocalDateTime timestamp)
        throws InvalidCertificateException;

    /**
     * Revokes the certificate.
     *
     * @param civicRegistrationNumber the patient's civic registration number.
     * @param certificateId the certificate ID
     * @return the revoked certificate.
     * @throws InvalidCertificateException if the certificate does not exist or the certificate id and civicRegistrationNumber didn't match
     * @throws CertificateRevokedException if the certificate has been revoked
     */
    Certificate revokeCertificate(Personnummer civicRegistrationNumber, String certificateId)
        throws InvalidCertificateException, CertificateRevokedException;

    void revokeCertificateForStatistics(Certificate certificate);

}
