/*
 * Copyright (C) 2024 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiverDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.ArendeRepository;
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateMetaData;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateRepository;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.RelationDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;

@ExtendWith(MockitoExtension.class)
class CertificateExportServiceImplTest {

    @Mock
    private CertificateRepository certificateRepository;
    @Mock
    private PathMatchingResourcePatternResolver resourceResolver;
    @Mock
    private CertificateDao certificateDao;
    @Mock
    private SjukfallCertificateDao sjukfallCertificateDao;
    @Mock
    private ArendeRepository arendeRepository;
    @Mock
    private RelationDao relationDao;
    @Mock
    private ApprovedReceiverDao approvedReceiverDao;

    @InjectMocks
    private CertificateExportServiceImpl certificateExportService;

    @Captor
    ArgumentCaptor<List<String>> certIdCaptor;

    private final List<List<String>> certIds = new ArrayList<>();

    private static final int PAGE = 0;
    private static final int EXPORT_SIZE = 2;
    private static final int ERASE_SIZE = 4;

    private static final String CERTIFICATE_TYPE = "TEST_TYPE";
    private static final String CERTIFICATE_VERSION = "TEST_VERSION";
    private static final String CERTIFICATE_END_TAG = "</texter>";
    private static final String CERTIFICATE_START_TAG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String RESOURCES_LOCATION = "classpath:CertificateExportServiceImplTest/*";
    private static final String CARE_PROVIDER_ID = "CARE_PROVIDER_ID";

    private static final Pageable EXPORT_PAGEABLE = PageRequest.of(PAGE, EXPORT_SIZE, Sort.by(Direction.ASC, "signedDate", "id"));
    private static final Pageable ERASE_PAGEABLE = PageRequest.of(PAGE, ERASE_SIZE, Sort.by(Direction.ASC, "signedDate", "id"));

    @Nested
    class GetCertificateTexts {

        @BeforeEach
        void setUp() throws IOException {
            when(resourceResolver.getResources(any(String.class))).thenReturn(getTestResources());
        }

        @Test
        public void shouldSelectActiveCertificateTextsOnly() {
            final var certificateTexts = certificateExportService.getCertificateTexts();

            assertEquals(1, certificateTexts.size());
        }

        @Test
        public void shouldSetProperAttributes() {
            final var certificateTexts = certificateExportService.getCertificateTexts();

            assertAll(
                () -> assertEquals(CERTIFICATE_TYPE, certificateTexts.get(0).getType()),
                () -> assertEquals(CERTIFICATE_VERSION, certificateTexts.get(0).getVersion())
            );
        }

        @Test
        public void shouldIncludeEntireXmlFile() {
            final var certificateTexts = certificateExportService.getCertificateTexts();

            assertAll(
                () -> assertTrue(certificateTexts.get(0).getXml().startsWith(CERTIFICATE_START_TAG)),
                () -> assertTrue(certificateTexts.get(0).getXml().endsWith(CERTIFICATE_END_TAG))
            );
        }
    }

    @Nested
    class GetCertificateExportPage {

        @BeforeEach
        void setUp() {
            when(certificateRepository.findTotalRevokedForCareProvider(any(String.class))).thenReturn(1L);
            when(certificateRepository.findCertificatesForCareProvider(eq(CARE_PROVIDER_ID), any(Pageable.class)))
                .thenReturn(getCertificatePage());
        }

        @Test
        public void shouldSetProperCounts() {
            final var certificateExportPage = certificateExportService.getCertificateExportPage(CARE_PROVIDER_ID, PAGE, EXPORT_SIZE);

            assertAll(
                () -> assertEquals(CARE_PROVIDER_ID, certificateExportPage.getCareProviderId()),
                () -> assertEquals(0, certificateExportPage.getPage()),
                () -> assertEquals(3, certificateExportPage.getCount()),
                () -> assertEquals(3, certificateExportPage.getTotal()),
                () -> assertEquals(1, certificateExportPage.getTotalRevoked())
            );
        }

        @Test
        public void shouldHaveCorrectNumberOfCertificates() {
            final var certificateExportPage = certificateExportService.getCertificateExportPage(CARE_PROVIDER_ID, PAGE, EXPORT_SIZE);
            assertEquals(3, certificateExportPage.getCertificateXmls().size());
        }

        @Test
        public void shouldSetRevokedOnRevokedCertificatesOnly() {
            final var certificateExportPage = certificateExportService.getCertificateExportPage(CARE_PROVIDER_ID, PAGE, EXPORT_SIZE);

            assertAll(
                () -> assertFalse(certificateExportPage.getCertificateXmls().get(0).isRevoked()),
                () -> assertTrue(certificateExportPage.getCertificateXmls().get(1).isRevoked()),
                () -> assertFalse(certificateExportPage.getCertificateXmls().get(2).isRevoked())
            );
        }
    }

    @Nested
    class EraseCertificates {

        @AfterEach
        public void cleanup() {
            certIds.clear();
        }

        @Test
        public void shouldCallForNewCertificateIdsForEachBatch() {
            setupPageMock(true);
            setupEraseMocks(false);

            certificateExportService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

            verify(certificateRepository, times(3)).findCertificateIdsForCareProvider(CARE_PROVIDER_ID, ERASE_PAGEABLE);
        }

        @Test
        public void shouldEraseApprovedReceiversInBatches() {
            setupPageMock(true);
            setupEraseMocks(false);

            certificateExportService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

            assertAll(
                () -> verify(approvedReceiverDao, times(3)).eraseApprovedReceivers(certIdCaptor.capture(), eq(CARE_PROVIDER_ID)),
                () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
                () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
                () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
            );
        }

        @Test
        public void shouldEraseRelationsInBatches() {
            setupPageMock(true);
            setupEraseMocks(false);

            certificateExportService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

            assertAll(
                () -> verify(relationDao, times(3)).eraseCertificateRelations(certIdCaptor.capture(), eq(CARE_PROVIDER_ID)),
                () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
                () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
                () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
            );
        }

        @Test
        public void shouldEraseArendenInBatches() {
            setupPageMock(true);
            setupEraseMocks(false);

            certificateExportService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

            assertAll(
                () -> verify(arendeRepository, times(3)).eraseArendenByCertificateIds(certIdCaptor.capture()),
                () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
                () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
                () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
            );
        }

        @Test
        public void shouldEraseSjukfallCertificatesInBatches() {
            setupPageMock(true);
            setupEraseMocks(false);

            certificateExportService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

            assertAll(
                () -> verify(sjukfallCertificateDao, times(3)).eraseCertificates(certIdCaptor.capture(), eq(CARE_PROVIDER_ID)),
                () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
                () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
                () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
            );
        }

        @Test
        public void shouldEraseCertificatesInBatches() {
            setupPageMock(true);
            setupEraseMocks(false);

            certificateExportService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

            assertAll(
                () -> verify(certificateDao, times(3)).eraseCertificates(certIdCaptor.capture(), eq(CARE_PROVIDER_ID)),
                () -> assertIterableEquals(certIds.get(0), certIdCaptor.getAllValues().get(0)),
                () -> assertIterableEquals(certIds.get(1), certIdCaptor.getAllValues().get(1)),
                () -> assertIterableEquals(certIds.get(2), certIdCaptor.getAllValues().get(2))
            );
        }

        @Test
        public void shouldRethrowAnyCaughtException() {
            setupPageMock(true);
            setupEraseMocks(true);

            assertThrows(IllegalArgumentException.class, () -> certificateExportService
                .eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize()));

            final var exception = assertThrows(IllegalArgumentException.class, () -> certificateExportService
                .eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize()));

            assertEquals("TestException", exception.getMessage());
        }
    }

    @Test
    public void shouldNotMakeEraseCallsIfCareProviderWithoutCertificates() {
        setupPageMock(false);

        certificateExportService.eraseCertificates(CARE_PROVIDER_ID, ERASE_PAGEABLE.getPageSize());

        assertAll(
            () -> verify(certificateRepository, times(1)).findCertificateIdsForCareProvider(CARE_PROVIDER_ID, ERASE_PAGEABLE),
            () -> verifyNoInteractions(approvedReceiverDao),
            () -> verifyNoInteractions(relationDao),
            () -> verifyNoInteractions(arendeRepository),
            () -> verifyNoInteractions(sjukfallCertificateDao),
            () -> verifyNoMoreInteractions(certificateDao)
        );
    }

    private void setupPageMock(boolean careProviderHasCertificates) {
        if (careProviderHasCertificates) {
            final var page1 = new PageImpl<>(getCertificateIds(4), ERASE_PAGEABLE, 10L);
            final var page2 = new PageImpl<>(getCertificateIds(4), ERASE_PAGEABLE, 6L);
            final var page3 = new PageImpl<>(getCertificateIds(2), ERASE_PAGEABLE, 2L);
            doReturn(page1, page2, page3).when(certificateRepository)
                .findCertificateIdsForCareProvider(any(String.class), any(Pageable.class));
        } else {
            final var emptyPage = new PageImpl<>(getCertificateIds(0), ERASE_PAGEABLE, 0L);
            doReturn(emptyPage).when(certificateRepository).findCertificateIdsForCareProvider(any(String.class), any(Pageable.class));
        }
    }

    private void setupEraseMocks(boolean withException) {
        doReturn(4, 4, 2).when(arendeRepository).eraseArendenByCertificateIds(any());
        doReturn(4, 4, 2).when(sjukfallCertificateDao).eraseCertificates(any(), any(String.class));

        if (!withException) {
            doReturn(4, 4, 2).when(certificateDao).eraseCertificates(any(), any(String.class));
        } else {
            when(certificateDao.eraseCertificates(any(), any(String.class))).thenReturn(4, 4)
                .thenThrow(new IllegalArgumentException("TestException"));
        }
    }

    private List<String> getCertificateIds(int count) {
        final var certificateIds = new ArrayList<String>();
        for (int i = 0; i < count; i++) {
            certificateIds.add(UUID.randomUUID().toString());
        }
        certIds.add(certificateIds);
        return certificateIds;
    }

    private Resource[] getTestResources() throws IOException {
        final var resourceResolver = new PathMatchingResourcePatternResolver();
        return resourceResolver.getResources(RESOURCES_LOCATION);
    }

    private Page<Certificate> getCertificatePage() {
        final var certificates = new ArrayList<Certificate>();
        certificates.add(getCertificate("1", false));
        certificates.add(getCertificate("2", true));
        certificates.add(getCertificate("3", false));
        return new PageImpl<>(certificates, EXPORT_PAGEABLE, 3);
    }

    private Certificate getCertificate(String id, boolean isRevoked) {
        final var certificateMetaData = new CertificateMetaData();
        certificateMetaData.setRevoked(isRevoked);

        final var originalCertificate = new OriginalCertificate();
        originalCertificate.setDocument(CERTIFICATE_START_TAG);

        final var certificate = new Certificate(id);
        certificate.setCertificateMetaData(certificateMetaData);
        certificate.setOriginalCertificate(originalCertificate);
        certificate.setCareGiverId(CARE_PROVIDER_ID);
        certificate.setSignedDate(LocalDateTime.now());
        return certificate;
    }
}
