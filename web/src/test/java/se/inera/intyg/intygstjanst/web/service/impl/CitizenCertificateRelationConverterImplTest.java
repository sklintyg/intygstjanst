package se.inera.intyg.intygstjanst.web.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationType;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificateRelationConverterImpl;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CitizenCertificateRelationConverterImplTest {

    private static final String ID = "ID";
    private static final String CODE = RelationKod.ERSATT.toString();
    private static final String OTHER_ID = "OTHER_ID";
    private static final LocalDateTime TIMESTAMP = LocalDateTime.now();

    @InjectMocks
    CitizenCertificateRelationConverterImpl citizenCertificateRelationConverter;

    @Test
    void shouldSetCertificateIdAsFromIdIfIdIsTo() {
        final var response = citizenCertificateRelationConverter.get(ID, ID, OTHER_ID, TIMESTAMP, CODE);

        assertEquals(OTHER_ID, response.getCertificateId());
    }

    @Test
    void shouldSetCertificateIdAsToIdIfIdIsFrom() {
        final var response = citizenCertificateRelationConverter.get(ID, OTHER_ID, ID, TIMESTAMP, CODE);

        assertEquals(ID, response.getCertificateId());
    }

    @Test
    void shouldSetTypeRenewed() {
        final var response = citizenCertificateRelationConverter.get(ID, ID, OTHER_ID, TIMESTAMP, CODE);

        assertEquals(CitizenCertificateRelationType.RENEWED, response.getType());
    }

    @Test
    void shouldSetTypeRenews() {
        final var response = citizenCertificateRelationConverter.get(ID, OTHER_ID, ID, TIMESTAMP, CODE);

        assertEquals(CitizenCertificateRelationType.RENEWS, response.getType());
    }

    @Test
    void shouldSetTimestamp() {
        final var response = citizenCertificateRelationConverter.get(ID, ID, OTHER_ID, TIMESTAMP, CODE);

        assertEquals(TIMESTAMP.toString(), response.getTimestamp());
    }

    @Test
    void shouldReturnNullIfIdDoesntMatch() {
        final var response = citizenCertificateRelationConverter.get("NON_MATCHING_ID", ID, OTHER_ID, TIMESTAMP, CODE);

        assertNull(response);
    }
}