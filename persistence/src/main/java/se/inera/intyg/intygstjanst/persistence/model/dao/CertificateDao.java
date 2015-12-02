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
package se.inera.certificate.model.dao;

import java.util.List;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import se.inera.certificate.exception.PersistenceException;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.common.support.modules.support.api.dto.Personnummer;

/**
 * Data Access Object for handling {@link Certificate}.
 *
 * @author parwenaker
 *
 */
public interface CertificateDao {

    /**
     * Retrieves a list of {@link Certificate} filtered by parameters.
     *
     * @param civicRegistrationNumber Civic registration number
     * @param types Type of certificate
     * @param fromDate From date when the certificate is valid
     * @param toDate To data when the certificate is valid
     * @return filtered list
     */
    List<Certificate> findCertificate(Personnummer civicRegistrationNumber, List<String> types, LocalDate fromDate, LocalDate toDate, List<String> careUnits);

    /**
     * Gets one {@link Certificate}.
     * @param civicRegistrationNumber the user's civic registration number or null, if no check for civic registration number is desired
     * @param certificateId Id of the Certificate
     *
     * @return the matching certificate or {@code null} if there is no certificate for the given certificate ID and
     * civic registration number
     *
     * @throws se.inera.certificate.exception.PersistenceException if the given civic registration number does not match with
     *  the certificate's patient civic registration number
     */
    Certificate getCertificate(Personnummer civicRegistrationNumber, String certificateId) throws PersistenceException;

    /**
     * Stores a {@link Certificate}.
     *
     * @param certificate certificate
     */
    void store(Certificate certificate);

    /**
     * Stores the original JAXB serialization of a received certificate {@link OriginalCertificate}.
     *
     * @param originalCertificate certificate
     *
     * @return The id that the entity got when persisted.
     */
    long storeOriginalCertificate(OriginalCertificate originalCertificate);

    /**
     * Updates the certificate's status.
     * @param certificateId the certificate's ID
     * @param civicRegistrationNumber the civic registration number of the patient associated to the certificate
     * @param state the state of the certificate
     * @param target the target associated with the status update (e.g. Försäkringskassan)
     * @param timestamp the timestamp of the status update
     * @throws se.inera.certificate.exception.PersistenceException if the combination of certificate ID and civic registration number
     *  does not match
     */
    void updateStatus(String certificateId, Personnummer civicRegistrationNumber, CertificateState state, String target, LocalDateTime timestamp)
            throws PersistenceException;

    /**
     * Removes all {@link Certificate}s belonging to specified citizen which have the flag
     * {@link Certificate#isDeletedByCareGiver()} set to <code>true</code>.
     * <p>
     * NOTE: This method should only be called for citizens WHITOUT a given consent.
     *
     * @param civicRegistrationNumber
     *            The citizen for which to remove certificates.
     */
    void removeCertificatesDeletedByCareGiver(Personnummer civicRegistrationNumber);

    /**
     * Set the field 'Deleted', (actually corresponding to whether or not the Intyg is archived by the user or not).
     */
    void setArchived(String certificateId, Personnummer civicRegistrationNumber, String archivedState) throws PersistenceException;
}
