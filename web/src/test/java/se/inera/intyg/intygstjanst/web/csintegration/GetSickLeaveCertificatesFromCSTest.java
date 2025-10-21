package se.inera.intyg.intygstjanst.web.csintegration;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.infra.certificate.dto.SickLeaveCertificate;
import se.inera.intyg.intygstjanst.web.csintegration.dto.SickLeaveCertificatesRequestDTO;
import se.inera.intyg.intygstjanst.web.service.dto.PersonIdTypeDTO;
import se.inera.intyg.schemas.contract.Personnummer;

@ExtendWith(MockitoExtension.class)
class GetSickLeaveCertificatesFromCSTest {

    @Mock
    private CSIntegrationService csIntegrationService;
    @InjectMocks
    private GetSickLeaveCertificatesFromCS getSickLeaveCertificatesFromCS;

    @Test
    void shallIncludePersonIdentityNumberWhenProvided() {
        final var expected = "19121212-1212";

        final var captor = ArgumentCaptor.forClass(SickLeaveCertificatesRequestDTO.class);

        getSickLeaveCertificatesFromCS.get(Personnummer.createPersonnummer(expected).orElseThrow(), null, null, null, null);

        verify(csIntegrationService).getSickLeaveCertificates(captor.capture());

        assertAll(
            () -> assertEquals(expected, captor.getValue().getPersonId().getId()),
            () -> assertEquals(PersonIdTypeDTO.PERSONAL_IDENTITY_NUMBER, captor.getValue().getPersonId().getType())
        );
    }

    @Test
    void shallIncludeCoordinationNumberWhenProvided() {
        final var expected = "19121272-1212";

        final var captor = ArgumentCaptor.forClass(SickLeaveCertificatesRequestDTO.class);

        getSickLeaveCertificatesFromCS.get(Personnummer.createPersonnummer(expected).orElseThrow(), null, null, null, null);

        verify(csIntegrationService).getSickLeaveCertificates(captor.capture());

        assertAll(
            () -> assertEquals(expected, captor.getValue().getPersonId().getId()),
            () -> assertEquals(PersonIdTypeDTO.COORDINATION_NUMBER, captor.getValue().getPersonId().getType())
        );
    }

    @Test
    void shallContainCertificateTypeListWhenProvided() {
        final var expected = List.of("A", "B", "C");

        final var captor = ArgumentCaptor.forClass(SickLeaveCertificatesRequestDTO.class);

        getSickLeaveCertificatesFromCS.get(null, expected, null, null, null);

        verify(csIntegrationService).getSickLeaveCertificates(captor.capture());

        assertEquals(expected, captor.getValue().getCertificateTypes());
    }

    @Test
    void shallContainSignedFromDateWhenProvided() {
        final var expected = LocalDate.now();

        final var captor = ArgumentCaptor.forClass(SickLeaveCertificatesRequestDTO.class);

        getSickLeaveCertificatesFromCS.get(null, null, expected, null, null);

        verify(csIntegrationService).getSickLeaveCertificates(captor.capture());

        assertEquals(expected, captor.getValue().getSignedFrom());
    }

    @Test
    void shallContainSignedToDateWhenProvided() {
        final var expected = LocalDate.now();

        final var captor = ArgumentCaptor.forClass(SickLeaveCertificatesRequestDTO.class);

        getSickLeaveCertificatesFromCS.get(null, null, null, expected, null);

        verify(csIntegrationService).getSickLeaveCertificates(captor.capture());

        assertEquals(expected, captor.getValue().getSignedTo());
    }

    @Test
    void shallContainIssuedByUnitIdsListWhenProvided() {
        final var expected = List.of("A", "B", "C");

        final var captor = ArgumentCaptor.forClass(SickLeaveCertificatesRequestDTO.class);

        getSickLeaveCertificatesFromCS.get(null, null, null, null, expected);

        verify(csIntegrationService).getSickLeaveCertificates(captor.capture());

        assertEquals(expected, captor.getValue().getIssuedByUnitIds());
    }

    @Test
    void shallReturnSickLeaveCertificatesFromCS() {
        final var expected = List.of(
            new SickLeaveCertificate(),
            new SickLeaveCertificate(),
            new SickLeaveCertificate()
        );

        when(csIntegrationService.getSickLeaveCertificates(any(SickLeaveCertificatesRequestDTO.class))).thenReturn(expected);

        final var actual = getSickLeaveCertificatesFromCS.get(null, null, null, null, null);

        assertEquals(expected, actual);
    }
}