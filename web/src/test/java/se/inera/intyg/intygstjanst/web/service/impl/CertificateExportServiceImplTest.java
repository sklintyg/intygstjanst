/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import se.inera.intyg.intygstjanst.persistence.model.dao.Certificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateMetaData;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateRepository;
import se.inera.intyg.intygstjanst.persistence.model.dao.OriginalCertificate;
import se.inera.intyg.intygstjanst.web.csintegration.aggregator.EraseCertificatesAggregator;
import se.inera.intyg.intygstjanst.web.csintegration.aggregator.ExportCertificateAggregator;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateExportPageDTO;

@ExtendWith(MockitoExtension.class)
class CertificateExportServiceImplTest {

    @Mock
    private ExportCertificateAggregator exportCertificateAggregator;
    @Mock
    private EraseCertificatesAggregator eraseCertificatesAggregator;
    @Mock
    private CertificateRepository certificateRepository;
    @Mock
    private PathMatchingResourcePatternResolver resourceResolver;

    @InjectMocks
    private CertificateExportServiceImpl certificateExportService;

    private static final int PAGE = 0;
    private static final int EXPORT_SIZE = 2;

    private static final String CERTIFICATE_TYPE = "TEST_TYPE";
    private static final String CERTIFICATE_VERSION = "TEST_VERSION";
    private static final String CERTIFICATE_END_TAG = "</texter>";
    private static final String CERTIFICATE_START_TAG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String RESOURCES_LOCATION = "classpath:CertificateExportServiceImplTest/*";
    private static final String CARE_PROVIDER_ID = "CARE_PROVIDER_ID";

    private static final Pageable EXPORT_PAGEABLE = PageRequest.of(PAGE, EXPORT_SIZE, Sort.by(Direction.ASC, "signedDate", "id"));

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
    class EraseCertiticatesTests {

        @Test
        void shallCallEraseCertificatesAggregator() {
            certificateExportService.eraseCertificates(CARE_PROVIDER_ID);
            verify(eraseCertificatesAggregator).eraseCertificates(CARE_PROVIDER_ID);
        }
    }

    @Nested
    class GetCertificateExportPage {

        @Test
        void shallReturnCertificateExportPageDTO() {
            final var expectedExportPage = CertificateExportPageDTO.of(CARE_PROVIDER_ID, 1, 1, 1, new ArrayList<>());
            when(exportCertificateAggregator.exportPage(CARE_PROVIDER_ID, PAGE, EXPORT_SIZE)).thenReturn(expectedExportPage);

            final var actualExportPage = certificateExportService.getCertificateExportPage(CARE_PROVIDER_ID, PAGE, EXPORT_SIZE);
            assertEquals(expectedExportPage, actualExportPage);
        }
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