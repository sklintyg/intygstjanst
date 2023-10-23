/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
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

package se.inera.intyg.intygstjanst.web.service.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateMetaData;
import se.inera.intyg.intygstjanst.persistence.model.dao.Relation;
import se.inera.intyg.intygstjanst.persistence.model.dao.RelationDao;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificate;
import se.inera.intyg.intygstjanst.web.service.repo.model.CitizenCertificateConverter;

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

    private static final String CERTIFICATE_ID_1 = "id1";
    private static final String CERTIFICATE_ID_2 = "id2";
    private static final String REVOKED_CERTIFICATE_ID = "revokedCertificateId";
    private static final Certificate REVOKED_CERTIFICATE = new Certificate(REVOKED_CERTIFICATE_ID);
    private static final Certificate CERTIFICATE_1 = new Certificate(CERTIFICATE_ID_1);
    private static final Certificate CERTIFICATE_2 = new Certificate(CERTIFICATE_ID_2);
    private static final Certificate CERTIFICATE_DB = new Certificate("db");
    private static final Certificate CERTIFICATE_DOI = new Certificate("doi");
    private static final CitizenCertificate CONVERTED_CERTIFICATE = CitizenCertificate.builder().build();
    private static final List<Certificate> CERTIFICATES = List.of(
        CERTIFICATE_1,
        CERTIFICATE_2,
        REVOKED_CERTIFICATE,
        CERTIFICATE_DB,
        CERTIFICATE_DOI
    );
    private static final String PATIENT_ID = "191212121212";

    @Nested
    class NoCertificates {

        @BeforeEach
        void setup() {
            Mockito.when(certificateDao.findCertificatesForPatient(anyString()))
                .thenReturn(Collections.emptyList());
        }

        @Test
        void shouldReturnEmptyList() {
            final var response = citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);

            assertEquals(0, response.size());
        }

        @Test
        void shouldNotCallCRelationDao() {
            citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);

            verify(relationDao, never()).getRelations(anyList(), anyList());
        }
    }

    @Nested
    class HasCertificates {

        @BeforeEach
        void setup() {
            CERTIFICATE_1.setCertificateMetaData(new CertificateMetaData());
            CERTIFICATE_2.setCertificateMetaData(new CertificateMetaData());
            CERTIFICATE_DB.setCertificateMetaData(new CertificateMetaData());
            CERTIFICATE_DOI.setCertificateMetaData(new CertificateMetaData());
            CERTIFICATE_1.setType("type1");
            CERTIFICATE_2.setType("type2");
            CERTIFICATE_DB.setType("db");
            CERTIFICATE_DOI.setType("doi");

            final var metaData = new CertificateMetaData();
            metaData.setRevoked(true);
            REVOKED_CERTIFICATE.setCertificateMetaData(metaData);
            REVOKED_CERTIFICATE.setType("type1");

            Mockito.when(certificateDao.findCertificatesForPatient(anyString())).thenReturn(CERTIFICATES);
        }

        @Test
        void shouldFilterRevokedCertificate() {
            final var response = citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);

            assertEquals(2, response.size());
        }

        @Test
        void shouldFilterDbCertificate() {
            final var response = citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);

            assertEquals(2, response.size());
        }

        @Test
        void shouldFilterDoiCertificate() {
            final var response = citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);

            assertEquals(2, response.size());
        }

        @Test
        void shouldCallDatabaseOnceWithPatientId() {
            citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);

            final var captor = ArgumentCaptor.forClass(String.class);

            verify(certificateDao, times(1)).findCertificatesForPatient(captor.capture());

            assertEquals(PATIENT_ID, captor.getValue());
        }

        @Nested
        class Converter {

            final Relation relation = new Relation(CERTIFICATE_ID_1, CERTIFICATE_ID_2, "ERSATT", LocalDateTime.now());
            final Relation otherRelation = new Relation("OTHER_ID", "OTHER_ID", "ERSATT", LocalDateTime.now());

            @BeforeEach
            void setup() {
                Mockito.when(relationDao.getRelations(anyList(), anyList()))
                    .thenReturn(
                        Map.of(
                            CERTIFICATE_ID_1, List.of(relation),
                            REVOKED_CERTIFICATE_ID, List.of(otherRelation),
                            CERTIFICATE_ID_2, List.of(relation)
                        )
                    );

                Mockito.when(citizenCertificateConverter.convert(any(Certificate.class), anyList()))
                    .thenReturn(CONVERTED_CERTIFICATE);
            }

            @Test
            void shouldSendRelationsForCertificate() {
                final var captor = ArgumentCaptor.forClass(List.class);

                citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);
                verify(citizenCertificateConverter, times(2)).convert(any(Certificate.class), captor.capture());

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

                assertEquals(4, captor.getValue().size());
                assertEquals(CERTIFICATES.get(0).getId(), captor.getValue().get(0));
                assertEquals(CERTIFICATES.get(1).getId(), captor.getValue().get(1));
                assertEquals(CERTIFICATES.get(3).getId(), captor.getValue().get(2));
                assertEquals(CERTIFICATES.get(4).getId(), captor.getValue().get(3));
            }

            @Test
            void shouldMakeCallWithRevokedCertificateIds() {
                citizenCertificatesRepository.getCertificatesForPatient(PATIENT_ID);

                final var captor = ArgumentCaptor.forClass(List.class);

                verify(relationDao).getRelations(anyList(), captor.capture());

                assertEquals(1, captor.getValue().size());
                assertEquals(REVOKED_CERTIFICATE_ID, captor.getValue().get(0));
            }
        }
    }

}