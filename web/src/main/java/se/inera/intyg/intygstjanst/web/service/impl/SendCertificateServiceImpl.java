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

package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.TestCertificateException;
import se.inera.intyg.intygstjanst.web.service.CertificateService;
import se.inera.intyg.intygstjanst.web.service.CertificateService.SendStatus;
import se.inera.intyg.intygstjanst.web.service.InternalNotificationService;
import se.inera.intyg.intygstjanst.web.service.SendCertificateService;
import se.inera.intyg.intygstjanst.web.service.StatisticsService;
import se.inera.intyg.intygstjanst.web.service.dto.SendCertificateRequestDTO;

@Service
public class SendCertificateServiceImpl implements SendCertificateService {

    private final CertificateService certificateService;
    private final StatisticsService statisticsService;
    private final InternalNotificationService internalNotificationService;

    public SendCertificateServiceImpl(
        CertificateService certificateService, StatisticsService statisticsService,
        InternalNotificationService internalNotificationService) {
        this.certificateService = certificateService;
        this.statisticsService = statisticsService;
        this.internalNotificationService = internalNotificationService;
    }

    @Override
    public SendStatus send(SendCertificateRequestDTO request)
        throws InvalidCertificateException, TestCertificateException, CertificateRevokedException, RecipientUnknownException {

        final var certificate = certificateService.getCertificateForCare(request.getCertificateId());
        final var sendStatus = certificateService.sendCertificate(
            request.getPatientId(),
            request.getCertificateId(),
            request.getRecipientId()
        );

        if (sendStatus != CertificateService.SendStatus.ALREADY_SENT) {
            statisticsService.sent(
                certificate.getId(),
                certificate.getType(),
                certificate.getCareUnitId(),
                request.getRecipientId()
            );

            internalNotificationService.notifyCareIfSentByCitizen(
                certificate,
                request.getPatientId().getOriginalPnr(),
                request.getHsaId()
            );
        }
        return sendStatus;
    }
}
