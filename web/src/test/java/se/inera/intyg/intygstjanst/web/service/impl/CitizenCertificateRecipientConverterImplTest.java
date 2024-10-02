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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateRecipientType;
import se.inera.intyg.intygstjanst.web.service.bean.Recipient;
import se.inera.intyg.intygstjanst.web.service.repo.RecipientRepo;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CitizenCertificateRecipientConverterImplTest {

    private static final LocalDateTime SENT_TIMESTAMP = LocalDateTime.now();
    private static final String CERTIFICATE_TYPE = "type";
    private static final String RECIPIENT_ID = "id";
    private static final String RECIPIENT_NAME = "name";

    @Mock
    RecipientRepo recipientRepo;

    @InjectMocks
    CitizenCertificateRecipientConverterImpl citizenCertificateRecipientConverter;

    List<Recipient> recipients;

    @BeforeEach
    void setup() {
        recipients = List.of(
            new Recipient(
                "address",
                "other name",
                "other id",
                CertificateRecipientType.MOTTAGARE.toString(),
                List.of(CERTIFICATE_TYPE),
                true,
                true
            ),
            new Recipient(
                "address",
                RECIPIENT_NAME,
                RECIPIENT_ID,
                CertificateRecipientType.HUVUDMOTTAGARE.toString(),
                List.of(CERTIFICATE_TYPE),
                true,
                true
            )
        );

        Mockito.when(recipientRepo.listRecipients()).thenReturn(recipients);
    }

    @Nested
    class NotSent {

        @Test
        void shouldReturnRecipientIdOfHuvudmottagare() {
            final var response = citizenCertificateRecipientConverter.convert(CERTIFICATE_TYPE, null);

            assertEquals(RECIPIENT_ID, response.get().getId());
        }

        @Test
        void shouldReturnRecipientNameOfHuvudmottagare() {
            final var response = citizenCertificateRecipientConverter.convert(CERTIFICATE_TYPE, null);

            assertEquals(RECIPIENT_NAME, response.get().getName());
        }

        @Test
        void shouldReturnNullAsSent() {
            final var response = citizenCertificateRecipientConverter.convert(CERTIFICATE_TYPE, null);

            assertNull(response.get().getSent());
        }
    }

    @Nested
    class Sent {

        @BeforeEach
        void setup() {
            Mockito.when(recipientRepo.listRecipients()).thenReturn(recipients);
        }

        @Test
        void shouldReturnRecipientIdOfHuvudmottagare() {
            final var response = citizenCertificateRecipientConverter.convert(CERTIFICATE_TYPE, SENT_TIMESTAMP);

            assertEquals(RECIPIENT_ID, response.get().getId());
        }

        @Test
        void shouldReturnRecipientNameOfHuvudmottagare() {
            final var response = citizenCertificateRecipientConverter.convert(CERTIFICATE_TYPE, SENT_TIMESTAMP);

            assertEquals(RECIPIENT_NAME, response.get().getName());
        }

        @Test
        void shouldReturnTimestampAsSent() {
            final var response = citizenCertificateRecipientConverter.convert(CERTIFICATE_TYPE, SENT_TIMESTAMP);

            assertEquals(SENT_TIMESTAMP, response.get().getSent());
        }
    }


    @Test
    void shouldReturnNullIfWrongType() {
        final var response = citizenCertificateRecipientConverter.convert("wrongType", SENT_TIMESTAMP);

        assertTrue(response.isEmpty());
    }
}
