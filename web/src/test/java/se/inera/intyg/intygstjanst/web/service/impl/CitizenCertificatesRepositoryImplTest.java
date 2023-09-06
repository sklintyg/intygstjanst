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
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateMetaData;
import se.inera.intyg.intygstjanst.persistence.model.dao.RelationDao;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificate;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificateConverter;
import se.inera.intyg.intygstjanst.web.service.repo.CitizenCertificatesRepositoryImpl;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CitizenCertificatesRepositoryImplTest {
    @Mock
    RelationDao relationDao;

    @Mock
    CertificateDao certificateDao;

    @Mock
    CitizenCertificateConverter citizenCertificateConverter;

    @InjectMocks
    CitizenCertificatesRepositoryImpl citizenCertificatesRepository;

    private final static String CERTIFICATE_ID_1 = "id1";
    private final static String CERTIFICATE_ID_2 = "id2";
    private final static String REVOKED_CERTIFICATE_ID = "revokedCertificateId";
    private final static Certificate REVOKED_CERTIFICATE = new Certificate(REVOKED_CERTIFICATE_ID);
    private final static Certificate CERTIFICATE_1 = new Certificate(CERTIFICATE_ID_1);
    private final static Certificate CERTIFICATE_2 = new Certificate(CERTIFICATE_ID_2);
    private final static CitizenCertificate CONVERTED_CERTIFICATE = CitizenCertificate.builder().build();
    private final static List<Certificate> CERTIFICATES = List.of(CERTIFICATE_1, CERTIFICATE_2);
    private final static List<Certificate> REVOKED_CERTIFICATES = List.of(REVOKED_CERTIFICATE);
    private final static String PATIENT_ID = "191212121212";

    @Nested
    class NoCertificates {
        @BeforeEach
        void setup() {
            Mockito.when(certificateDao.findCertificates(any(), any(), any(), any(), any(), anyBoolean(), any(), any()))
                    .thenReturn(Collections.emptyList());
        }

        @Test
        void shouldReturnEmptyListIfNoCertificates() {
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
    class InvalidPatientId {
        @Test
        void shouldReturnEmptyList() {
            final var response = citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);

            assertEquals(0, response.size());
        }

        @Test
        void shouldNotCallCertificateDao() {
            final var response = citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);

            verify(certificateDao, never())
                    .findCertificates(any(), any(), any(), any(), any(), anyBoolean(), any(), any());
        }
    }

    @Nested
    class RevokedCertificate {
        @BeforeEach
        void setup() {
            Mockito.when(certificateDao.findCertificates(any(), any(), any(), any(), any(), anyBoolean(), any(), any()))
                    .thenReturn(REVOKED_CERTIFICATES);

            final var metaData = new CertificateMetaData();
            metaData.setRevoked(true);
            REVOKED_CERTIFICATE.setCertificateMetaData(metaData);
        }

        @Test
        void shouldFilterOutRevokedCertificates() {
            final var response = citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);

            assertEquals(0, response.size());
        }
    }

    @Nested
    class HasCertificates {

        @BeforeEach
        void setup() {
            CERTIFICATE_1.setCertificateMetaData(new CertificateMetaData());
            CERTIFICATE_2.setCertificateMetaData(new CertificateMetaData());

            Mockito.when(certificateDao.findCertificates(any(), any(), any(), any(), any(), anyBoolean(), any(), any()))
                    .thenReturn(CERTIFICATES);
        }

        @Nested
        class Converter {
            @BeforeEach
            void setup() {
                Mockito.when(citizenCertificateConverter.get(any(Certificate.class), anyList()))
                        .thenReturn(CONVERTED_CERTIFICATE);
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
                final var response = citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);

                final var captor = ArgumentCaptor.forClass(List.class);

                verify(relationDao).getRelations(captor.capture(), anyList());

                assertEquals(CERTIFICATES.size(), captor.getValue().size());
                assertEquals(CERTIFICATES.get(0).getId(), captor.getValue().get(0));
                assertEquals(CERTIFICATES.get(1).getId(), captor.getValue().get(1));
            }

            @Test
            void shouldMakeCallWithRelationKodErsatt() {
                final var response = citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);

                final var captor = ArgumentCaptor.forClass(List.class);

                verify(relationDao).getRelations(anyList(), captor.capture());

                assertEquals(1, captor.getValue().size());
                assertEquals(RelationKod.ERSATT.toString(), captor.getValue().get(0));
            }
        }
    }

}