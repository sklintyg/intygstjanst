/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
import java.util.List;

/**
 * Created by eriklupander on 2016-02-02.
 */
public interface SjukfallCertificateDao {

    List<SjukfallCertificate> findActiveSjukfallCertificate(String careGiverId, List<String> unitIds, List<String> doctorIds,
        LocalDate activeDate, LocalDate recentlyClosed);

    List<SjukfallCertificate> findAllSjukfallCertificate(String careGiverId, List<String> unitIds, List<String> patientIds);

    List<SjukfallCertificate> findActiveSjukfallCertificateForCareUnits(
        String careGiverHsaId,
        List<String> careUnitHsaIds,
        int maxDagarSedanAvslut);

    List<SjukfallCertificate> findActiveSjukfallCertificateForPersonOnCareUnits(
        String careGiverHsaId,
        List<String> careUnitHsaIds,
        String personnummer,
        int maxDagarSedanAvslut);

    List<SjukfallCertificate> findSjukfallCertificateForPerson(
        String personnummer);

    void store(SjukfallCertificate sjukfallCert);

    void revoke(String id);

    /**
     * Erase any data related to test certificates passed as ids.
     *
     * @param ids Certificate ids.
     */
    void eraseTestCertificates(List<String> ids);

    int eraseCertificates(List<String> careProviderIds, String careProviderId);
}
