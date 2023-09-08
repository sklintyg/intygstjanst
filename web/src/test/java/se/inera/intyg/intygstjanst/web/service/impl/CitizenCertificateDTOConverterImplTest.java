package se.inera.intyg.intygstjanst.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.modules.registry.ModuleNotFoundException;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRecipientConverter;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateIssuerDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRecipientDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateSummaryDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateUnitDTO;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificate;

@ExtendWith(MockitoExtension.class)
class CitizenCertificateDTOConverterImplTest {

    @Mock
    private CitizenCertificateRecipientConverter citizenCertificateRecipientConverter;

    @InjectMocks
    private CitizenCertificateDTOConverterImpl citizenCertificateDTOConverter;

    private static final String CERTIFICATE_ID = "Id";
    private static final String TYPE_NAME = "Type";
    private static final String CERTIFICATE_TYPE = "Certificate type";
    private static final String TYPE_VERSION = "Type version";
    private static final String SUMMARY_LABEL = "Summary label";
    private static final String SUMMARY_VALUE = "Summary value";
    private static final String ISSUER_NAME = "Issuer name";
    private static final String UNIT_ID = "Unit id";
    private static final String UNIT_NAME = "Unit name";
    private static final LocalDateTime ISSUED_DATE = LocalDateTime.now();
    private static final String RECIPIENT_NAME = "Recipient name";
    private static final String RECIPIENT_SENT = "Recipient sent";
    private static final LocalDateTime SENT_DATE = LocalDateTime.now();
    private static final List<CitizenCertificateRelationDTO> RELATIONS = Collections.emptyList();

    private CitizenCertificate getCitizenCertificate() {
        return CitizenCertificate.builder()
            .id(CERTIFICATE_ID)
            .issued(ISSUED_DATE)
            .type(CERTIFICATE_TYPE)
            .typeVersion(TYPE_VERSION)
            .additionalInfo(SUMMARY_VALUE)
            .issuerName(ISSUER_NAME)
            .unitId(UNIT_ID)
            .unitName(UNIT_NAME)
            .relations(RELATIONS)
            .sentDate(SENT_DATE)
            .build();
    }

    @Test
    void shouldConvertToCorrectFormatForId() throws ModuleNotFoundException {
        final var citizenCertificate = getCitizenCertificate();
        final var actualResult = citizenCertificateDTOConverter.get(citizenCertificate, TYPE_NAME, SUMMARY_LABEL);

        assertEquals(CERTIFICATE_ID, actualResult.getId());
    }

    @Test
    void shouldConvertToCorrectFormatForType() throws ModuleNotFoundException {
        final var expectedType = CitizenCertificateTypeDTO.builder()
            .id(CERTIFICATE_TYPE)
            .name(TYPE_NAME)
            .version(TYPE_VERSION)
            .build();

        final var citizenCertificate = getCitizenCertificate();

        final var actualResult = citizenCertificateDTOConverter.get(citizenCertificate, TYPE_NAME, SUMMARY_LABEL);
        assertEquals(expectedType, actualResult.getType());
    }

    @Test
    void shouldConvertToCorrectFormatForSummary() throws ModuleNotFoundException {
        final var expectedType = CitizenCertificateSummaryDTO.builder()
            .value(SUMMARY_VALUE)
            .label(SUMMARY_LABEL)
            .build();

        final var citizenCertificate = getCitizenCertificate();

        final var actualResult = citizenCertificateDTOConverter.get(citizenCertificate, TYPE_NAME, SUMMARY_LABEL);
        assertEquals(expectedType, actualResult.getSummary());
    }

    @Test
    void shouldConvertToCorrectFormatForIssuer() throws ModuleNotFoundException {
        final var expectedType = CitizenCertificateIssuerDTO.builder()
            .name(ISSUER_NAME)
            .build();

        final var citizenCertificate = getCitizenCertificate();

        final var actualResult = citizenCertificateDTOConverter.get(citizenCertificate, TYPE_NAME, SUMMARY_LABEL);
        assertEquals(expectedType, actualResult.getIssuer());
    }

    @Test
    void shouldConvertToCorrectFormatForUnit() throws ModuleNotFoundException {
        final var expectedType = CitizenCertificateUnitDTO.builder()
            .id(UNIT_ID)
            .name(UNIT_NAME)
            .build();

        final var citizenCertificate = getCitizenCertificate();

        final var actualResult = citizenCertificateDTOConverter.get(citizenCertificate, TYPE_NAME, SUMMARY_LABEL);
        assertEquals(expectedType, actualResult.getUnit());
    }

    @Test
    void shouldConvertToCorrectFormatForRecipient() throws ModuleNotFoundException {
        final var expectedResult = CitizenCertificateRecipientDTO.builder()
            .id(CERTIFICATE_ID)
            .name(RECIPIENT_NAME)
            .sent(RECIPIENT_SENT)
            .build();

        final var citizenCertificate = getCitizenCertificate();

        when(citizenCertificateRecipientConverter.get(citizenCertificate.getType(), citizenCertificate.getSentDate())).thenReturn(
            expectedResult);

        final var actualResult = citizenCertificateDTOConverter.get(citizenCertificate, TYPE_NAME, SUMMARY_LABEL);
        assertEquals(expectedResult, actualResult.getRecipient());
    }

    @Test
    void shouldConvertToCorrectFormatForIssued() throws ModuleNotFoundException {
        final var citizenCertificate = getCitizenCertificate();
        final var actualResult = citizenCertificateDTOConverter.get(citizenCertificate, TYPE_NAME, SUMMARY_LABEL);

        assertEquals(ISSUED_DATE.toString(), actualResult.getIssued());
    }

    @Test
    void shouldConvertToCorrectFormatForRelations() throws ModuleNotFoundException {
        final var citizenCertificate = getCitizenCertificate();
        final var actualResult = citizenCertificateDTOConverter.get(citizenCertificate, TYPE_NAME, SUMMARY_LABEL);

        assertEquals(RELATIONS, actualResult.getRelations());
    }
}
