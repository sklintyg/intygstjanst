package se.inera.intyg.intygstjanst.web.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
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
        final var relation = new Relation(OTHER_ID, ID, CODE, TIMESTAMP);
        final var response = citizenCertificateRelationConverter.convert(ID, relation);

        assertEquals(OTHER_ID, response.get().getCertificateId());
    }

    @Test
    void shouldSetCertificateIdAsToIdIfIdIsFrom() {
        final var relation = new Relation(ID, OTHER_ID, CODE, TIMESTAMP);
        final var response = citizenCertificateRelationConverter.convert(ID, relation);

        assertEquals(OTHER_ID, response.get().getCertificateId());
    }

    @Test
    void shouldSetTypeRenewedIfIdMatchesTo() {
        final var relation = new Relation(OTHER_ID, ID, CODE, TIMESTAMP);
        final var response = citizenCertificateRelationConverter.convert(ID, relation);

        assertEquals(CitizenCertificateRelationType.RENEWED, response.get().getType());
    }

    @Test
    void shouldSetTypeRenewsIfIdMatchesFrom() {
        final var relation = new Relation(ID, OTHER_ID, CODE, TIMESTAMP);
        final var response = citizenCertificateRelationConverter.convert(ID, relation);

        assertEquals(CitizenCertificateRelationType.RENEWS, response.get().getType());
    }

    @Test
    void shouldSetTimestamp() {
        final var relation = new Relation(ID, OTHER_ID, CODE, TIMESTAMP);
        final var response = citizenCertificateRelationConverter.convert(ID, relation);

        assertEquals(TIMESTAMP.toString(), response.get().getTimestamp());
    }

    @Test
    void shouldReturnOptionalEmptyIfIdDoesntMatch() {
        final var relation = new Relation(ID, OTHER_ID, CODE, TIMESTAMP);
        final var response = citizenCertificateRelationConverter.convert("NON_MATCHING_ID", relation);

        assertTrue(response.isEmpty());
    }
}