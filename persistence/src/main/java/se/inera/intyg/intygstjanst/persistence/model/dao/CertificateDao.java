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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.intygstjanst.persistence.exception.PersistenceException;
import se.inera.intyg.schemas.contract.Personnummer;

/**
 * Data Access Object for handling {@link Certificate}.
 *
 * @author parwenaker
 */
public interface CertificateDao {

    /**
     * Retrieves a list of {@link Certificate} filtered by parameters.
     *
     * @param civicRegistrationNumber Civic registration number of patient
     * @param units The unit ids of the care unit and/or sub units that the certificate was issued from
     * @param fromDate From date when the certificate was signed
     * @param toDate To date when the certificate was signed
     * @param orderBy Field that list should be sorted according to
     * @param orderAscending If list should be sorted ascending or not
     * @return filtered list of certificates
     */
    List<Certificate> findCertificates(Personnummer civicRegistrationNumber, String[] units, LocalDateTime fromDate,
        LocalDateTime toDate, String orderBy, boolean orderAscending, Set<String> types, String doctorId);

    /**
     * Retrieves a list of {@link Certificate} filtered by parameters.
     *
     * @param civicRegistrationNumber Civic registration number
     * @param types Type of certificate
     * @param fromDate From date when the certificate is valid
     * @param toDate To data when the certificate is valid
     * @return filtered list
     */
    List<Certificate> findCertificate(Personnummer civicRegistrationNumber, List<String> types, LocalDate fromDate, LocalDate toDate,
        List<String> careUnits);

    /**
     * Retrieves a list of {@link Certificate} filtered by parameters.
     *
     * @param careUnits List of care units
     * @param types Type of certificate
     * @param fromDate From date when the certificate is valid
     * @param toDate To data when the certificate is valid
     * @return filtered list
     */
    List<Certificate> findCertificate(List<String> careUnits, List<String> types, LocalDate fromDate, LocalDate toDate);

    /**
     * This method will replace findCertificate(careUnits, types, fromDate, toDate). For backward compatibility both methods will
     * coexist until the new MetaData table is in full use. This method can be renamed and the old method removed from 2021-2 and after.
     */
    List<Certificate> findCertificatesUsingMetaDataTable(List<String> careUnits, List<String> types, LocalDate fromDate, LocalDate toDate,
        List<String> doctorIds);

    /**
     * Retrieves a list of issuing doctors hsa-ids.
     *
     * @param careUnits List of units (care units or sub units)
     * @param types Type of certificates
     * @param fromDate From date when the certificate is valid
     * @param toDate To data when the certificate is valid
     * @return List of hsa-id of doctors that have issued certificates matching the parameters.
     */
    List<String> findDoctorIds(List<String> careUnits, List<String> types, LocalDate fromDate, LocalDate toDate);

    /**
     * Gets one {@link Certificate}.
     *
     * @param civicRegistrationNumber the user's civic registration number or null, if no check for civic registration number is desired
     * @param certificateId Id of the Certificate
     * @return the matching certificate or {@code null} if there is no certificate for the given certificate ID and
     * civic registration number
     * @throws se.inera.intyg.intygstjanst.persistence.exception.PersistenceException if the given civic registration number does not match
     * with
     * the certificate's patient civic registration number
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
     * @return The id that the entity got when persisted.
     */
    long storeOriginalCertificate(OriginalCertificate originalCertificate);

    /**
     * Updates the certificate's status.
     *
     * @param certificateId the certificate's ID
     * @param civicRegistrationNumber the civic registration number of the patient associated to the certificate
     * @param state the state of the certificate
     * @param target the target associated with the status update (e.g. Försäkringskassan)
     * @param timestamp the timestamp of the status update
     * @throws se.inera.intyg.intygstjanst.persistence.exception.PersistenceException if the combination of certificate ID and civic
     * registration number
     * does not match
     */
    void updateStatus(String certificateId, Personnummer civicRegistrationNumber, CertificateState state, String target,
        LocalDateTime timestamp)
        throws PersistenceException;

    /**
     * Updates the certificate's status.
     *
     * @param certificateId the certificate's ID
     * @param state the state of the certificate
     * @param target the target associated with the status update (e.g. Försäkringskassan)
     * @param timestamp the timestamp of the status update
     * @throws se.inera.intyg.intygstjanst.persistence.exception.PersistenceException if the certificate does not exist
     */
    void updateStatus(String certificateId, CertificateState state, String target, LocalDateTime timestamp)
        throws PersistenceException;

    /**
     * Removes all {@link Certificate}s belonging to specified citizen which have the flag
     * {@link Certificate#isDeletedByCareGiver()} set to <code>true</code>.
     * <p>
     * NOTE: This method should only be called for citizens WHITOUT a given consent.
     *
     * @param civicRegistrationNumber The citizen for which to remove certificates.
     */
    void removeCertificatesDeletedByCareGiver(Personnummer civicRegistrationNumber);

    /**
     * Find test certificates with signed dates within passed date/time interval.
     *
     * @param from From datetime. Can be null.
     * @param to To datetime. Can be null.
     * @return List of matching test certificates.
     */
    List<Certificate> findTestCertificates(LocalDateTime from, LocalDateTime to);

    /**
     * Erase any data related to test certificates passed as ids.
     *
     * @param ids Certificate ids.
     */
    void eraseTestCertificates(List<String> ids);

    int eraseCertificates(List<String> certificateIds, String careProviderId);

    void storeCertificateMetadata(CertificateMetaData metadata);

    /**
     * Method to be used when populating Certificate with metadata.
     *
     * @param maxNumber Maxiumum number of certificate ids to return
     * @return List of certificate Ids wothout metadata.
     */
    List<String> findCertificatesWithoutMetadata(int maxNumber);

    /**
     * @return List of all certificate types
     */
    List<CertificateType> getCertificateTypes();

    List<Certificate> findCertificatesForPatient(String patientId);

    Certificate findCertificate(String certificateId);


}
