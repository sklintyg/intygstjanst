package se.inera.intyg.intygstjanst.web.csintegration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.web.csintegration.dto.ExportCertificateInternalResponseDTO;
import se.inera.intyg.intygstjanst.web.csintegration.dto.ExportCertificatesRequestDTO;
import se.inera.intyg.intygstjanst.web.csintegration.dto.ExportInternalResponseDTO;
import se.inera.intyg.intygstjanst.web.csintegration.dto.TotalExportsInternalResponseDTO;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateExportPageDTO;
import se.inera.intyg.intygstjanst.web.service.dto.CertificateXmlDTO;

@ExtendWith(MockitoExtension.class)
class ExportCertificateFromCSTest {

    private static final String CARE_PROVIDER_ID = "careProviderId";
    private static final String ENCODED_XML = "xml";
    private static final ExportInternalResponseDTO EXPORT_INTERNAL_RESPONSE_DTO = ExportInternalResponseDTO.builder()
        .exports(
            List.of(
                ExportCertificateInternalResponseDTO.builder()
                    .certificateId("id")
                    .revoked(false)
                    .xml(ENCODED_XML)
                    .build()
            )
        )
        .build();
    private static final String DECODED_XML = new String(Base64.getDecoder().decode(ENCODED_XML), StandardCharsets.UTF_8);
    @Mock
    CSIntegrationService csIntegrationService;
    @InjectMocks
    ExportCertificateFromCS exportCertificateFromCS;

    @Nested
    class CalculatePageNumber {

        @BeforeEach
        void setUp() {
            doReturn(TotalExportsInternalResponseDTO.builder().totalCertificates(10).totalRevokedCertificates(0).build())
                .when(csIntegrationService).getInternalTotalExportForCareProvider(CARE_PROVIDER_ID);
        }

        private static Stream<Arguments> providePageNumberArguments() {
            return Stream.of(
                Arguments.of(10L, 10, 5, 0),
                Arguments.of(10L, 15, 5, 1),
                Arguments.of(10L, 19, 5, 1),
                Arguments.of(10L, 22, 5, 2),
                Arguments.of(10L, 27, 5, 3),
                Arguments.of(10L, 32, 5, 4),
                Arguments.of(10L, 35, 5, 5)
            );
        }

        @ParameterizedTest
        @MethodSource("providePageNumberArguments")
        void shallCalculatePageNumber(long totalFromIT, int collected, int batchSize, int expectedPageNumber) {
            final var argumentCaptor = ArgumentCaptor.forClass(ExportCertificatesRequestDTO.class);
            final var exportPageDTO = CertificateExportPageDTO.of(CARE_PROVIDER_ID, 0, totalFromIT, 0, Collections.emptyList());

            doReturn(EXPORT_INTERNAL_RESPONSE_DTO.getExports()).when(csIntegrationService)
                .getInternalExportCertificatesForCareProvider(argumentCaptor.capture(), eq(CARE_PROVIDER_ID));

            exportCertificateFromCS.addCertificatesFromCS(exportPageDTO, CARE_PROVIDER_ID, collected, batchSize);
            assertEquals(expectedPageNumber, argumentCaptor.getValue().getPage());
        }
    }

    @Test
    void shallReturnCertificateExportPageWithUpdatedTotalAndRevoked() {
        final var totalExportsInternalResponseDTO = TotalExportsInternalResponseDTO.builder()
            .totalCertificates(10)
            .totalRevokedCertificates(5)
            .build();

        doReturn(totalExportsInternalResponseDTO).when(csIntegrationService).getInternalTotalExportForCareProvider(CARE_PROVIDER_ID);

        final var exportPageDTO = CertificateExportPageDTO.of(CARE_PROVIDER_ID, 0, 0, 0, List.of(CertificateXmlDTO.of("id", false, "xml")));
        final var result = exportCertificateFromCS.addCertificatesFromCS(exportPageDTO, CARE_PROVIDER_ID, 0, 5);

        assertEquals(10, result.getTotal());
        assertEquals(5, result.getTotalRevoked());
    }

    @Test
    void shallReturnCertificateExportPageWithUpdatedValues() {
        final var totalExportsInternalResponseDTO = TotalExportsInternalResponseDTO.builder()
            .totalCertificates(10)
            .totalRevokedCertificates(5)
            .build();

        doReturn(EXPORT_INTERNAL_RESPONSE_DTO.getExports()).when(csIntegrationService)
            .getInternalExportCertificatesForCareProvider(any(ExportCertificatesRequestDTO.class), eq(CARE_PROVIDER_ID));
        doReturn(totalExportsInternalResponseDTO).when(csIntegrationService).getInternalTotalExportForCareProvider(CARE_PROVIDER_ID);

        final var exportPageDTO = CertificateExportPageDTO.of(CARE_PROVIDER_ID, 0, 0, 0, Collections.emptyList());
        final var result = exportCertificateFromCS.addCertificatesFromCS(exportPageDTO, CARE_PROVIDER_ID, 0, 5);

        assertEquals(10, result.getTotal());
        assertEquals(5, result.getTotalRevoked());
        assertEquals(DECODED_XML, result.getCertificateXmls().getFirst().getXml());
    }
}