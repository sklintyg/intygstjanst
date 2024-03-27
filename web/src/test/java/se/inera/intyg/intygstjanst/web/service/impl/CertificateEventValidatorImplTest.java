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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CertificateEventValidatorImplTest {

    private static final String CERTIFICATE_ID = "certificateId";
    private static final String MESSAGE_ID = "messageId";
    private static final String EVENT_TYPE = "eventType";
    private static final String EVENT_SIGNED = "certificate-signed";
    private static final String EVENT_MESSAGE_SENT = "message-sent";

    private CertificateEventValidatorImpl certificateStateMassageValidator;

    @BeforeEach
    void setup() {
        certificateStateMassageValidator = new CertificateEventValidatorImpl();
    }

    @Test
    void shouldReturnTrueIfRequiredPropertiesForEventMessageSent() {
        assertTrue(certificateStateMassageValidator.validate(EVENT_MESSAGE_SENT, CERTIFICATE_ID, MESSAGE_ID));
    }

    @Test
    void shouldReturnTrueIfRequiredPropertiesAndNotEventMessageSent() {
        assertTrue(certificateStateMassageValidator.validate(EVENT_SIGNED, CERTIFICATE_ID, null));
    }

    @Test
    void shouldReturnFalseIfMissingMessageIdForEventMessageSent() {
        assertFalse(certificateStateMassageValidator.validate(EVENT_MESSAGE_SENT, CERTIFICATE_ID, null));
    }

    @Test
    void shouldReturnFalseIfMissingEventType() {
        assertFalse(certificateStateMassageValidator.validate(null, CERTIFICATE_ID, MESSAGE_ID));
    }

    @Test
    void shouldReturnFalseIfMissingCertificateId() {
        assertFalse(certificateStateMassageValidator.validate(EVENT_SIGNED, null, MESSAGE_ID));
    }
}
