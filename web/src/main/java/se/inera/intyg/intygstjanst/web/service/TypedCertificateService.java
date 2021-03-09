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
package se.inera.intyg.intygstjanst.web.service;

import java.time.LocalDate;
import java.util.List;
import se.inera.intyg.infra.certificate.dto.DiagnosedCertificate;
import se.inera.intyg.infra.certificate.dto.SickLeaveCertificate;
import se.inera.intyg.schemas.contract.Personnummer;

/**
 * Service to provide certificate data with more specialized information than included in base information.
 * Used for places were the whole information set of a certificate is not wanted and/or when there is a need for a more generalized type of
 * certificate than the common base information.
 */
public interface TypedCertificateService {

    /**
     * List certificates on unit(s) with diagnosis information
     *
     * @param units List of units the certificates are bound to
     * @param certificateTypeList The specific type of certificates to get
     * @param fromDate First signing date of selection
     * @param toDate Last signing date of selection
     * @return List of certificates with diagnosis information
     */
    List<DiagnosedCertificate> listDiagnosedCertificatesForCareUnits(List<String> units, List<String> certificateTypeList,
        LocalDate fromDate, LocalDate toDate, List<String> doctorIds);

    /**
     * List certificates for person with diagnosis information
     *
     * @param personId Id of the person to get certificates for
     * @param certificateTypeList The specific type of certificates to get
     * @param fromDate First signing date of selection
     * @param toDate Last signing date of selection
     * @param units List of units the certificates are bound to
     * @return List of certificates with diagnosis information
     */
    List<DiagnosedCertificate> listDiagnosedCertificatesForPerson(Personnummer personId, List<String> certificateTypeList,
        LocalDate fromDate, LocalDate toDate, List<String> units);

    /**
     * List certificates for person with sickleave information
     *
     * @param personId Id of the person to get certificates for
     * @param certificateTypeList The specific type of certificates to get
     * @param fromDate First signing date of selection
     * @param toDate Last signing date of selection
     * @param units List of units the certificates are bound to
     * @return List of certificates with sickleave information
     */
    List<SickLeaveCertificate> listSickLeaveCertificatesForPerson(Personnummer personId, List<String> certificateTypeList,
        LocalDate fromDate, LocalDate toDate, List<String> units);

    /**
     * List doctors that have signed certificates on unit(s)
     *
     * @param units List of units the certificates are bound to
     * @param certificateTypes The specific type of certificates to get
     * @param fromDate First signing date of selection
     * @param toDate Last signing date of selection
     * @return List of certificates with diagnosis information
     */
    List<String> listDoctorsForCareUnits(List<String> units, List<String> certificateTypes,
        LocalDate fromDate, LocalDate toDate);
}
