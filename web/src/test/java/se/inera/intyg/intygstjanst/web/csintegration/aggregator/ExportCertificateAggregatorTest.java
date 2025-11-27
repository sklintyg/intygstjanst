package se.inera.intyg.intygstjanst.web.csintegration.aggregator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import se.inera.intyg.intygstjanst.web.csintegration.ExportCertificateFromCS;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateExportPageDTO;

@ExtendWith(MockitoExtension.class)
class ExportCertificateAggregatorTest {

    @Mock
    ExportCertificateFromCS exportCertificateFromCS;
    @Mock
    private CertificateRepository certificateRepository;
    @InjectMocks
    private ExportCertificateAggregator exportCertificateAggregator;


    private static final String CARE_PROVIDER_ID = "CARE_PROVIDER_ID";
    private static final int COLLECTED = 0;
    private static final int EXPORT_SIZE = 2;
    private static final String CERTIFICATE_START_TAG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    private static final Pageable EXPORT_PAGEABLE = PageRequest.of(COLLECTED, EXPORT_SIZE, Sort.by(Direction.ASC, "signedDate", "id"));


    @Test
    void shallIncludeCertificatesFromCS() {
        when(certificateRepository.findTotalRevokedForCareProvider(any(String.class))).thenReturn(1L);
        when(certificateRepository.findCertificatesForCareProvider(eq(CARE_PROVIDER_ID), any(Pageable.class)))
            .thenReturn(getCertificatePage());

        final var expectedResult = CertificateExportPageDTO.of(
            CARE_PROVIDER_ID,
            EXPORT_SIZE,
            3,
            1,
            Collections.emptyList()
        );

        when(exportCertificateFromCS.addCertificatesFromCS(any(CertificateExportPageDTO.class), eq(CARE_PROVIDER_ID), eq(COLLECTED),
            eq(EXPORT_SIZE))).thenReturn(expectedResult);

        final var result = exportCertificateAggregator.exportPage(CARE_PROVIDER_ID, COLLECTED, EXPORT_SIZE);
        assertEquals(expectedResult, result);
    }

    @Test
    void shallNotIncludeCertificateXmlsIfAllCertificatesFromITIsCollected() {
        final var argumentCaptor = ArgumentCaptor.forClass(CertificateExportPageDTO.class);
        final var certificatePage = getCertificatePage();

        when(certificateRepository.findTotalRevokedForCareProvider(any(String.class))).thenReturn(1L);
        when(certificateRepository.findCertificatesForCareProvider(eq(CARE_PROVIDER_ID), any(Pageable.class)))
            .thenReturn(certificatePage);
        when(exportCertificateFromCS.addCertificatesFromCS(argumentCaptor.capture(), eq(CARE_PROVIDER_ID),
            eq(certificatePage.getNumberOfElements()), eq(EXPORT_SIZE))).thenReturn(
            CertificateExportPageDTO.of(CARE_PROVIDER_ID, 0, 3, 1, Collections.emptyList()));

        exportCertificateAggregator.exportPage(CARE_PROVIDER_ID, certificatePage.getNumberOfElements(), EXPORT_SIZE);

        assertTrue(argumentCaptor.getValue().getCertificateXmls().isEmpty());
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