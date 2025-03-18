package se.inera.intyg.intygstjanst.web.service.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import se.inera.intyg.intygstjanst.web.csintegration.dto.ExportCertificateInternalResponseDTO;

class CertificateExportPageDTOTest {

    @Test
    void shallUpdateTotal() {
        final var exportPageDTO = CertificateExportPageDTO.of("careProvider", 0, 0, 0, Collections.emptyList());
        exportPageDTO.updateTotal(10);
        assertEquals(10, exportPageDTO.getTotal());
    }

    @Test
    void shallUpdateRevoked() {
        final var exportPageDTO = CertificateExportPageDTO.of("careProvider", 0, 0, 0, Collections.emptyList());
        exportPageDTO.updateRevoked(10);
        assertEquals(10, exportPageDTO.getTotalRevoked());
    }

    @Test
    void shallUpdateCertificateXmls() {
        final var expectedCertificateXmlDTO = CertificateXmlDTO.of(
            "id",
            false,
            new String(Base64.getDecoder().decode("xml"), StandardCharsets.UTF_8)
        );

        final var exportCertificateInternalResponseDTO = ExportCertificateInternalResponseDTO.builder()
            .certificateId("id")
            .revoked(false)
            .xml("xml")
            .build();

        final var exportPageDTO = CertificateExportPageDTO.of("careProvider", 0, 0, 0, Collections.emptyList());

        exportPageDTO.updateCertificateXmls(Collections.singletonList(exportCertificateInternalResponseDTO));

        assertEquals(1, exportPageDTO.getCount());
        assertEquals(1, exportPageDTO.getCertificateXmls().size());
        assertEquals(expectedCertificateXmlDTO, exportPageDTO.getCertificateXmls().getFirst());
    }

    @Test
    void shallUpdateCount() {
        final var exportPageDTO = CertificateExportPageDTO.of("careProvider", 0, 0, 0, Collections.emptyList());

        exportPageDTO.updateCertificateXmls(
            Collections.singletonList(
                ExportCertificateInternalResponseDTO.builder()
                    .certificateId("id")
                    .revoked(false)
                    .xml("xml")
                    .build()
            )
        );
        
        assertEquals(1, exportPageDTO.getCount());
    }
}