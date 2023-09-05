package se.inera.intyg.intygstjanst.web.service.impl;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CitizenCertificateRecipientConverterImplTest {

    private static final String ID_FK = "FKASSA";
    private static final String ID_TRANSP = "TRANSP";
    private static final String NAME_FK = "Försäkringskassan";
    private static final String NAME_TRANSP = "Transportstyrelsen";
    private static final LocalDateTime SENT_TIMESTAMP = LocalDateTime.now();
    private static final LocalDateTime RECEIEVED_TIMESTAMP = LocalDateTime.now().plusDays(1);

    @InjectMocks
    CitizenCertificateRecipientConverterImpl citizenCertificateRecipientConverter;

    @Test
    void shouldReturnNullIfStatesIsEmpty() {
        final var response = citizenCertificateRecipientConverter.get(Collections.emptyList());

        assertNull(response);
    }

    @Nested
    class Fk {

        Collection<CertificateStateHistoryEntry> states = List.of(
                new CertificateStateHistoryEntry(ID_FK, CertificateState.SENT, SENT_TIMESTAMP),
                new CertificateStateHistoryEntry(ID_FK, CertificateState.RECEIVED, RECEIEVED_TIMESTAMP)
        );

        @Test
        void shouldSetId() {
            final var response = citizenCertificateRecipientConverter.get(states);

            assertEquals(ID_FK, response.getId());
        }

        @Test
        void shouldSetName() {
            final var response = citizenCertificateRecipientConverter.get(states);

            assertEquals(NAME_FK, response.getName());
        }

        @Test
        void shouldSetTimestamp() {
            final var response = citizenCertificateRecipientConverter.get(states);

            assertEquals(SENT_TIMESTAMP.toString(), response.getSent());
        }
    }

    @Nested
    class Transp {

        Collection<CertificateStateHistoryEntry> states = List.of(
                new CertificateStateHistoryEntry(ID_TRANSP, CertificateState.SENT, SENT_TIMESTAMP),
                new CertificateStateHistoryEntry(ID_TRANSP, CertificateState.RECEIVED, RECEIEVED_TIMESTAMP)
        );

        @Test
        void shouldSetId() {
            final var response = citizenCertificateRecipientConverter.get(states);

            assertEquals(ID_TRANSP, response.getId());
        }

        @Test
        void shouldSetName() {
            final var response = citizenCertificateRecipientConverter.get(states);

            assertEquals(NAME_TRANSP, response.getName());
        }

        @Test
        void shouldSetTimestamp() {
            final var response = citizenCertificateRecipientConverter.get(states);

            assertEquals(SENT_TIMESTAMP.toString(), response.getSent());
        }
    }
}