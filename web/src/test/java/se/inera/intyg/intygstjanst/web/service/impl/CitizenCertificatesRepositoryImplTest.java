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
import se.inera.intyg.common.support.common.enumerations.RelationKod;
import se.inera.intyg.intygstjanst.persistence.model.dao.*;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificate;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificateConverter;
import se.inera.intyg.intygstjanst.web.service.repo.CitizenCertificatesRepositoryImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CitizenCertificatesRepositoryImplTest {
    @Mock
    RelationDao relationDao;

    @Mock
    CertificateRepository certificateRepository;

    @Mock
    CitizenCertificateConverter citizenCertificateConverter;

    @InjectMocks
    CitizenCertificatesRepositoryImpl citizenCertificatesRepository;

    private static final String CERTIFICATE_ID_1 = "id1";
    private static final String CERTIFICATE_ID_2 = "id2";
    private static final String REVOKED_CERTIFICATE_ID = "revokedCertificateId";
    private static final Certificate REVOKED_CERTIFICATE = new Certificate(REVOKED_CERTIFICATE_ID);
    private static final Certificate CERTIFICATE_1 = new Certificate(CERTIFICATE_ID_1);
    private static final Certificate CERTIFICATE_2 = new Certificate(CERTIFICATE_ID_2);
    private static final CitizenCertificate CONVERTED_CERTIFICATE = CitizenCertificate.builder().build();
    private static final List<Certificate> CERTIFICATES = List.of(CERTIFICATE_1, CERTIFICATE_2, REVOKED_CERTIFICATE);
    private static final List<Certificate> REVOKED_CERTIFICATES = List.of(REVOKED_CERTIFICATE);
    private static final String PATIENT_ID = "191212121212";

    @Nested
    class NoCertificates {
        @BeforeEach
        void setup() {
            Mockito.when(certificateRepository.findCertificatesForPatient(anyString()))
                    .thenReturn(Collections.emptyList());
        }

        @Test
        void shouldReturnEmptyList() {
            final var response = citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);

            assertEquals(0, response.size());
        }

        @Test
        void shouldNotCallCRelationDao() {
            final var response = citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);

            verify(relationDao, never()).getRelations(anyList(), anyList());
        }
    }

    @Nested
    class HasCertificates {

        @BeforeEach
        void setup() {
            CERTIFICATE_1.setCertificateMetaData(new CertificateMetaData());
            CERTIFICATE_2.setCertificateMetaData(new CertificateMetaData());

            final var metaData = new CertificateMetaData();
            metaData.setRevoked(true);
            REVOKED_CERTIFICATE.setCertificateMetaData(metaData);

            Mockito.when(certificateRepository.findCertificatesForPatient(anyString())).thenReturn(CERTIFICATES);
        }

        @Test
        void shouldFilterRevokedCertificate() {
            final var response = citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);

            assertEquals(2, response.size());
            assertFalse(response.contains(REVOKED_CERTIFICATE));
        }

        @Nested
        class Converter {
            @BeforeEach
            void setup() {
                Mockito.when(citizenCertificateConverter.get(any(Certificate.class), anyList()))
                        .thenReturn(CONVERTED_CERTIFICATE);
            }

            @Test
            void shouldSendNonRevokedRelations() {
                final var relation = new Relation(CERTIFICATE_ID_1, CERTIFICATE_ID_2, "code", LocalDateTime.now());
                final var revokedRelation = new Relation(REVOKED_CERTIFICATE_ID, CERTIFICATE_ID_1, "code", LocalDateTime.now());
                Mockito.when(relationDao.getRelations(anyList(), anyList())).thenReturn(List.of(relation, revokedRelation));
                final var captor = ArgumentCaptor.forClass(List.class);

                citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);
                verify(citizenCertificateConverter, times(2)).get(any(Certificate.class), captor.capture());

                assertEquals(1, captor.getValue().size());
                assertEquals(relation, captor.getValue().get(0));
            }

            @Test
            void shouldReturnConvertedCertificate() {
                final var response = citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);

                assertEquals(CONVERTED_CERTIFICATE, response.get(0));
                assertEquals(CONVERTED_CERTIFICATE, response.get(1));
            }
        }

        @Nested
        class RelationDao {
            @Test
            void shouldMakeCallWithCertificateIds() {
                citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);

                final var captor = ArgumentCaptor.forClass(List.class);

                verify(relationDao).getRelations(captor.capture(), anyList());

                assertEquals(2, captor.getValue().size());
                assertEquals(CERTIFICATES.get(0).getId(), captor.getValue().get(0));
                assertEquals(CERTIFICATES.get(1).getId(), captor.getValue().get(1));
            }

            @Test
            void shouldMakeCallWithRelationKodErsatt() {
                citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);

                final var captor = ArgumentCaptor.forClass(List.class);

                verify(relationDao).getRelations(anyList(), captor.capture());

                assertEquals(1, captor.getValue().size());
                assertEquals(RelationKod.ERSATT.toString(), captor.getValue().get(0));
            }
        }
    }

}