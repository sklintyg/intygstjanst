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

package se.inera.intyg.intygstjanst.web.csintegration.aggregator;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.integration.module.exception.CertificateRevokedException;
import se.inera.intyg.common.support.integration.module.exception.InvalidCertificateException;
import se.inera.intyg.intygstjanst.web.csintegration.util.CertificateServiceProfile;
import se.inera.intyg.intygstjanst.web.exception.RecipientUnknownException;
import se.inera.intyg.intygstjanst.web.exception.TestCertificateException;
import se.inera.intyg.intygstjanst.web.service.CertificateService.SendStatus;
import se.inera.intyg.intygstjanst.web.service.SendCertificateService;
import se.inera.intyg.intygstjanst.web.service.dto.SendCertificateRequestDTO;

@Service
@RequiredArgsConstructor
public class CitizenSendCertificateAggregator implements SendCertificateService {

    private final CertificateServiceProfile certificateServiceProfile;
    private final SendCertificateService sendCertificateServiceImpl;
    private final SendCertificateService citizenSendCertificateFromCS;

    @Override
    public SendStatus send(SendCertificateRequestDTO request)
        throws InvalidCertificateException, TestCertificateException, CertificateRevokedException, RecipientUnknownException {
        if (!certificateServiceProfile.active()) {
            return sendCertificateServiceImpl.send(request);
        }

        final var responseFromCS = citizenSendCertificateFromCS.send(request);

        return responseFromCS != null ? responseFromCS : sendCertificateServiceImpl.send(request);
    }
}