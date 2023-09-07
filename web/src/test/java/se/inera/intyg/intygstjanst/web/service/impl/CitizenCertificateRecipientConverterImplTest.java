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
            final var response = citizenCertificateRecipientConverter.get(CERTIFICATE_TYPE, null);

            assertEquals(RECIPIENT_ID, response.getId());
        }

        @Test
        void shouldReturnRecipientNameOfHuvudmottagare() {
            final var response = citizenCertificateRecipientConverter.get(CERTIFICATE_TYPE, null);

            assertEquals(RECIPIENT_NAME, response.getName());
        }

        @Test
        void shouldReturnNullAsSent() {
            final var response = citizenCertificateRecipientConverter.get(CERTIFICATE_TYPE, null);

            assertNull(response.getSent());
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
            final var response = citizenCertificateRecipientConverter.get(CERTIFICATE_TYPE, SENT_TIMESTAMP);

            assertEquals(RECIPIENT_ID, response.getId());
        }

        @Test
        void shouldReturnRecipientNameOfHuvudmottagare() {
            final var response = citizenCertificateRecipientConverter.get(CERTIFICATE_TYPE, SENT_TIMESTAMP);

            assertEquals(RECIPIENT_NAME, response.getName());
        }

        @Test
        void shouldReturnTimestampAsSent() {
            final var response = citizenCertificateRecipientConverter.get(CERTIFICATE_TYPE, SENT_TIMESTAMP);

            assertEquals(SENT_TIMESTAMP.toString(), response.getSent());
        }
    }


    @Test
    void shouldReturnNullIfWrongType() {
        final var response = citizenCertificateRecipientConverter.get("wrongType", SENT_TIMESTAMP);

        assertNull(response);
    }
}
