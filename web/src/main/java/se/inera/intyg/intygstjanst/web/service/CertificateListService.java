/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import se.inera.intyg.infra.certificate.dto.CertificateListRequest;
import se.inera.intyg.infra.certificate.dto.CertificateListResponse;

/**
* Service that collects certificates into a list.
 */

public interface CertificateListService {

    /**
     * Collects certificates for a specific doctor on the logged in unit.
     * @param request Request with filter query including filters that user has chosen or default filters.
     * @return Response including all signed certificates and the total amount of certificates.
     */
    CertificateListResponse listCertificatesForDoctor(CertificateListRequest request);
}
