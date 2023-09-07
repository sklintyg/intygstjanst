package se.inera.intyg.intygstjanst.web.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRelationType;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificateRelationConverter;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificateConverterImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitizenCertificateConverterImplTest {

    @Mock
    CitizenCertificateRelationConverter citizenCertificateRelationConverter;

    @InjectMocks
    CitizenCertificateConverterImpl citizenCertificateConverter;

    private static final String CERTIFICATE_ID = "Id";
    private static final String TYPE = "Type";
    private static final String TYPE_VERSION = "Type version";
    private static final String ISSUER = "Doctor name";
    private static final String SUMMARY = "Summary";
    private static final LocalDateTime DATE = LocalDateTime.now();
    private static List<Relation> relations = Collections.emptyList();
    private static Certificate certificate;


    @BeforeEach
    void setup() {
        certificate = mock(Certificate.class);
        Mockito.when(certificate.getId()).thenReturn(CERTIFICATE_ID);
        Mockito.when(certificate.getType()).thenReturn(TYPE);
        Mockito.when(certificate.getTypeVersion()).thenReturn(TYPE_VERSION);
        Mockito.when(certificate.getSigningDoctorName()).thenReturn(ISSUER);
        Mockito.when(certificate.getSignedDate()).thenReturn(DATE);
        Mockito.when(certificate.getAdditionalInfo()).thenReturn(SUMMARY);
    }
    @Test
    void shouldConvertId() {
        final var response = citizenCertificateConverter.get(certificate, relations);

        assertEquals(CERTIFICATE_ID, response.getId());
    }

    @Test
    void shouldConvertType(){
        final var response = citizenCertificateConverter.get(certificate, relations);

        assertEquals(TYPE, response.getType());
    }

    @Test
    void shouldConvertTypeVersion(){
        final var response = citizenCertificateConverter.get(certificate, relations);

        assertEquals(TYPE_VERSION, response.getTypeVersion());
    }

    @Test
    void shouldConvertSentDateWhenNull(){
        certificate.setStates(null);
        final var response = citizenCertificateConverter.get(certificate, relations);

        assertNull(response.getSentDate());
    }

    @Test
    void shouldConvertSentDateWhenSent(){
        final var timeStamp = LocalDateTime.now();
        when(certificate.getStates()).thenReturn(List.of(new CertificateStateHistoryEntry("Id", CertificateState.SENT, timeStamp)));
        final var response = citizenCertificateConverter.get(certificate, relations);

        assertEquals(timeStamp, response.getSentDate());
    }

    @Test
    void shouldConvertSentDateWhenNotSent(){
        final var timeStamp = LocalDateTime.now();
        when(certificate.getStates()).thenReturn(List.of(new CertificateStateHistoryEntry("Id", CertificateState.CANCELLED, timeStamp)));
        final var response = citizenCertificateConverter.get(certificate, relations);

        assertNull(response.getSentDate());
    }


    @Test
    void shouldConvertIssuer(){
        final var response = citizenCertificateConverter.get(certificate, relations);

        assertEquals(ISSUER, response.getIssuerName());
    }

    @Test
    void shouldConvertIssuedDate(){
        final var response = citizenCertificateConverter.get(certificate, relations);

        assertEquals(DATE, response.getIssued());
    }

    @Test
    void shouldConvertSummary(){
        final var response = citizenCertificateConverter.get(certificate, relations);

        assertEquals(SUMMARY, response.getAdditionalInfo());
    }

    @Test
    void shouldFilterNullValuesOfRelation(){
        Relation relation = new Relation("wrongId", "wrongId", "code", LocalDateTime.now());
        final var response = citizenCertificateConverter.get(certificate, List.of(relation));
        assertTrue(response.getRelations().isEmpty());
    }

    @Nested
    class ConversionOfRelationValues {

        CitizenCertificateRelationDTO citizenCertificateRelationDTO;
        private final String toCertificate = "toCertificate";
        private final String fromCertificate = "fromCertificate";
        private final String code = "code";
        private final LocalDateTime timeStamp = LocalDateTime.now();

        Relation relation = new Relation(fromCertificate, toCertificate, code, timeStamp);

        @BeforeEach
        void setup() {

            this.citizenCertificateRelationDTO = CitizenCertificateRelationDTO.builder()
                .certificateId("id")
                .type(CitizenCertificateRelationType.RENEWED)
                .timestamp(LocalDateTime.now().toString())
                .build();

            when(citizenCertificateRelationConverter.get(anyString(), anyString(), anyString(), any(LocalDateTime.class), anyString())).thenReturn(citizenCertificateRelationDTO);
        }

        @Test
        void shouldConvertRelation(){
            final var response = citizenCertificateConverter.get(certificate, List.of(relation));
            assertEquals(citizenCertificateRelationDTO, response.getRelations().get(0));
        }

        @Test
        void shouldSendIdToConverter(){
            final var response = citizenCertificateConverter.get(certificate, List.of(relation));
            final var captor = ArgumentCaptor.forClass(String.class);

            verify(citizenCertificateRelationConverter).get(captor.capture(), anyString(), anyString(), any(LocalDateTime.class), anyString());
            assertEquals(certificate.getId(), captor.getValue());
        }

        @Test
        void shouldSendToIntygsIdToConverter(){
            final var response = citizenCertificateConverter.get(certificate, List.of(relation));
            final var captor = ArgumentCaptor.forClass(String.class);

            verify(citizenCertificateRelationConverter).get(anyString(), captor.capture(), anyString(), any(LocalDateTime.class), anyString());
            assertEquals(relation.getToIntygsId(), captor.getValue());
        }

        @Test
        void shouldSendFromIntygsIdToConverter(){
            final var response = citizenCertificateConverter.get(certificate, List.of(relation));
            final var captor = ArgumentCaptor.forClass(String.class);

            verify(citizenCertificateRelationConverter).get(anyString(),  anyString(), captor.capture(), any(LocalDateTime.class), anyString());
            assertEquals(relation.getFromIntygsId(), captor.getValue());
        }

        @Test
        void shouldSendCreatedToConverter(){
            final var response = citizenCertificateConverter.get(certificate, List.of(relation));
            final var captor = ArgumentCaptor.forClass(LocalDateTime.class);

            verify(citizenCertificateRelationConverter).get(anyString(),  anyString(),  anyString(), captor.capture(), anyString());
            assertEquals(relation.getCreated(), captor.getValue());
        }

        @Test
        void shouldSendRelationKodToConverter(){
            final var response = citizenCertificateConverter.get(certificate, List.of(relation));
            final var captor = ArgumentCaptor.forClass(String.class);

            verify(citizenCertificateRelationConverter).get(anyString(),  anyString(), anyString(), any(LocalDateTime.class), captor.capture());
            assertEquals(relation.getRelationKod(), captor.getValue());
        }
    }
}