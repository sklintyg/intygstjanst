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

package se.inera.intyg.intygstjanst.web.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.CertificateEventValidator;

@Service
@Slf4j
public class CertificateEventValidatorImpl implements CertificateEventValidator {

    private static final String MESSAGE_SENT = "message-sent";

    @Override
    public boolean validate(String eventType, String certificateId, String messageId) {
        if (eventType == null || eventType.isBlank()) {
            log.warn("Missing required parameter 'eventType'.");
            return false;
        }
        if (certificateId == null || certificateId.isBlank()) {
            log.warn("Missing required parameter 'certificateId'.");
            return false;
        }
        if (eventType.equals(MESSAGE_SENT) && (messageId == null || messageId.isBlank())) {
            log.warn("Missing required parameter 'messageId'.");
            return false;
        }

        return true;
    }
}
