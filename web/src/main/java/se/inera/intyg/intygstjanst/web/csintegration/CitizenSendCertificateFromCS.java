/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.csintegration;

import static se.inera.intyg.intygstjanst.web.csintegration.util.PersonIdTypeEvaluator.getType;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.intygstjanst.web.csintegration.dto.SendCitizenCertificateRequestDTO;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.TestCertificateException;
import se.inera.intyg.intygstjanst.web.service.CertificateService.SendStatus;
import se.inera.intyg.intygstjanst.web.service.InternalNotificationService;
import se.inera.intyg.intygstjanst.web.service.SendCertificateService;
import se.inera.intyg.intygstjanst.web.service.dto.PersonIdDTO;
import se.inera.intyg.intygstjanst.web.service.dto.SendCertificateRequestDTO;

@Service
@RequiredArgsConstructor
public class CitizenSendCertificateFromCS implements SendCertificateService {

  private final CSIntegrationService csIntegrationService;
  private final InternalNotificationService internalNotificationService;

  @Override
  public SendStatus send(SendCertificateRequestDTO request)
      throws InvalidCertificateException, TestCertificateException, CertificateRevokedException, RecipientUnknownException {
    if (Boolean.FALSE.equals(csIntegrationService.certificateExists(request.getCertificateId()))) {
      return null;
    }

    final var certificate = csIntegrationService.sendCitizenCertificates(
        getSendCitizenCertificateRequest(request),
        request.getCertificateId()
    );

    internalNotificationService.notifyCareIfSentByCitizen(
        certificate,
        request.getPatientId().getOriginalPnr(),
        request.getHsaId()
    );

    return SendStatus.OK;
  }

  private static SendCitizenCertificateRequestDTO getSendCitizenCertificateRequest(
      SendCertificateRequestDTO request) {
    return SendCitizenCertificateRequestDTO.builder()
        .personId(
            PersonIdDTO.builder()
                .id(request.getPatientId().getOriginalPnr())
                .type(getType(request.getPatientId()))
                .build()
        )
        .build();
  }
}