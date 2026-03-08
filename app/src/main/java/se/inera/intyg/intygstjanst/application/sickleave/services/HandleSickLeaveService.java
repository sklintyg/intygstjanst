/*
 * Copyright (C) 2026 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.application.sickleave.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.application.sickleave.converter.SickLeaveCertificateToSjukfallCertificateConverter;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.CSIntegrationService;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.dto.GetCertificateXmlResponse;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.SjukfallCertificateDao;

@Service
@RequiredArgsConstructor
public class HandleSickLeaveService {

  private final SjukfallCertificateDao sjukfallCertificateDao;
  private final CSIntegrationService csIntegrationService;
  private final SickLeaveCertificateToSjukfallCertificateConverter converter;

  public void created(GetCertificateXmlResponse response) {

    final var sickLeaveResponse =
        csIntegrationService.getSickLeaveCertificate(response.getCertificateId());

    if (sickLeaveResponse.isAvailable()) {
      sjukfallCertificateDao.store(converter.convert(sickLeaveResponse.getSickLeaveCertificate()));
    }
  }

  public void revoked(GetCertificateXmlResponse response) {

    final var sickLeaveResponse =
        csIntegrationService.getSickLeaveCertificate(response.getCertificateId());

    if (sickLeaveResponse.isAvailable()) {
      sjukfallCertificateDao.revoke(sickLeaveResponse.getSickLeaveCertificate().getId());
    }
  }
}
