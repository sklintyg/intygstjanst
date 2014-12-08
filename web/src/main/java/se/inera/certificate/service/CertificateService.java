/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Certificate (http://code.google.com/p/inera-certificate).
 *
 * Inera Certificate is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Certificate is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package se.inera.certificate.service;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import se.inera.certificate.exception.CertificateAlreadyExistsException;
import se.inera.certificate.exception.CertificateRevokedException;
import se.inera.certificate.exception.CertificateValidationException;
import se.inera.certificate.exception.InvalidCertificateException;
import se.inera.certificate.exception.MissingConsentException;
import se.inera.certificate.model.CertificateState;
import se.inera.certificate.model.dao.Certificate;
import se.inera.ifv.insuranceprocess.healthreporting.revokemedicalcertificateresponder.v1.RevokeType;

import java.util.List;

/**
 * @author andreaskaltenbach
 */
public interface CertificateService {

    public enum SendStatus {
        OK, ALREADY_SENT
    }

    /**
     * Returns the list of certificates for the patient and filter criteria.
     *
     * @param civicRegistrationNumber
     *            the patient's civic registration number
     * @param certificateTypes
     *            optional certificate type filter. If empty or null, all certificate types will be returned
     * @param fromDate
     *            optional from date filter
     * @param toDate
     *            optional to date filter
     * @return list of matching certificates or empty list if no such certificates can be found
     * @throws MissingConsentException
     *             if the patient has not given consent for accessing her certificates
     */
    List<Certificate> listCertificatesForCitizen(String civicRegistrationNumber, List<String> certificateTypes, LocalDate fromDate, LocalDate toDate)
            throws MissingConsentException;

    /**
     * Returns a list of certificates for one or many care units.
     *
     * @param civicRegistrationNumber
     *            the patient's civic registration number
     * @param careUnits
     *            a list of care units for which the certificates must belong.
     * @return list of matching certificates or empty list if no such certificates can be found
     */
    List<Certificate> listCertificatesForCare(String civicRegistrationNumber, List<String> careUnits);

    /**
     * Returns the certificate for the given patient and certificate ID.
     * Implementation should not return revoked certificates - but rather throw an {@link CertificateRevokedException}
     *
     * @param civicRegistrationNumber
     *            the patient's civic registration number that must match same info on certificate
     * @param certificateId
     *            the certificate ID
     * @return the certificate information or null if the requested certificate does not exist
     * @throws MissingConsentException
     *             if the patient has not given consent for accessing her certificates
     * @throws InvalidCertificateException
     *             if the certificate does not exist or the certificate id and civicRegistrationNumber didn't match
     * @throws CertificateRevokedException
     *             if the certificate has been revoked
     */
    Certificate getCertificateForCitizen(String civicRegistrationNumber, String certificateId) throws MissingConsentException,
            InvalidCertificateException,
            CertificateRevokedException;

    /**
     * Returns the certificate for the given certificate ID.
     * Implementation should also return revoked certificates - but with resultCode REVOKED
     *
     * @param certificateId
     *            the certificate ID
     * @return the certificate information or null if the requested certificate does not exist
     * @throws InvalidCertificateException
     *             if the certificate does not exist or the certificate id and civicRegistrationNumber didn't match
     */
    Certificate getCertificateForCare(String certificateId) throws InvalidCertificateException;

    /**
     * Stores the given certificate.
     *
     * @param xml
     *            the string representation of the incoming XML
     * @param type
     *            the certificate type
     * @return the created certificate
     * @throws CertificateAlreadyExistsException
     *             when a certificate with the same identifier already exists
     * @throws InvalidCertificateException
     *             if the certificate does not exist or the certificate id and civicRegistrationNumber didn't match
     * @throws CertificateValidationException
     *             if the certificate was not valid
     */
    Certificate storeCertificate(String xml, String type) throws CertificateAlreadyExistsException,
            InvalidCertificateException, CertificateValidationException;

    void setCertificateState(String civicRegistrationNumber, String certificateId, String target, CertificateState state, LocalDateTime timestamp)
            throws InvalidCertificateException;

    /**
     * Sends the certificate to the destined target.
     *
     * @returns SendStatus further subclassifying the outcome of a successful send
     * @throws InvalidCertificateException
     *             if the certificate does not exist or the certificate id and civicRegistrationNumber didn't match
     * @throws CertificateRevokedException
     *             if the certificate has been revoked
     */
    SendStatus sendCertificate(String civicRegistrationNumber, String certificateId, String target) throws InvalidCertificateException,
            CertificateRevokedException;

    /**
     * Revokes the certificate.
     *
     * @param civicRegistrationNumber
     *            the patient's civic registration number.
     * @param certificateId
     *            the certificate ID
     * @param revokeData
     *            Data of who requested the revoke, when etc. If <code>null</code>, no revocation should be sent to
     *            earlier recipients of the intyg
     * @return the revoked certificate.
     * @throws InvalidCertificateException
     *             if the certificate does not exist or the certificate id and civicRegistrationNumber didn't match
     * @throws CertificateRevokedException
     *             if the certificate has been revoked
     */
    Certificate revokeCertificate(String civicRegistrationNumber, String certificateId, RevokeType revokeData) throws InvalidCertificateException,
            CertificateRevokedException;
}
