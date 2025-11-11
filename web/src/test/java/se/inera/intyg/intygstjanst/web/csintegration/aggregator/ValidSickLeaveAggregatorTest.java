package se.inera.intyg.intygstjanst.web.csintegration.aggregator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.csintegration.CSIntegrationService;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetValidSickLeaveCertificateIdsInternalRequest;

@ExtendWith(MockitoExtension.class)
class ValidSickLeaveAggregatorTest {

    @Mock
    private SjukfallCertificateDao sjukfallCertificateDao;

    @Mock
    private CSIntegrationService csIntegrationService;

    @InjectMocks
    private ValidSickLeaveAggregator validSickLeaveAggregator;

    @Test
    void shouldReturnCertificatesWhenAllStoredInIT() {
        final var certA = new SjukfallCertificate("a");
        certA.setTestCertificate(false);
        final var certB = new SjukfallCertificate("b");
        certB.setTestCertificate(false);

        final var input = List.of(certA, certB);
        final var ids = List.of("a", "b");

        when(sjukfallCertificateDao.findSickLeavesStoredInCS(ids)).thenReturn(List.of());

        final var result = validSickLeaveAggregator.get(input);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> "a".equals(c.getId())));
        assertTrue(result.stream().anyMatch(c -> "b".equals(c.getId())));
    }

    @Test
    void shouldReturnOnlyValidCertificatesFromCSAndIT() {
        final var certA = new SjukfallCertificate("a");
        certA.setTestCertificate(false);
        final var certB = new SjukfallCertificate("b");
        certB.setTestCertificate(false);
        final var certC = new SjukfallCertificate("c");
        certC.setTestCertificate(false);

        final var input = List.of(certA, certB, certC);
        final var ids = List.of("a", "b", "c");

        when(sjukfallCertificateDao.findSickLeavesStoredInCS(ids)).thenReturn(List.of("b", "c"));
        when(csIntegrationService.getValidSickLeaveIds(any(GetValidSickLeaveCertificateIdsInternalRequest.class)))
            .thenReturn(List.of("c"));

        final var result = validSickLeaveAggregator.get(input);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(c -> "a".equals(c.getId())));
        assertTrue(result.stream().anyMatch(c -> "c".equals(c.getId())));
    }

    @Test
    void shouldExcludeTestCertificates() {
        final var certA = new SjukfallCertificate("a");
        certA.setTestCertificate(true);
        final var certB = new SjukfallCertificate("b");
        certB.setTestCertificate(false);
        final var certC = new SjukfallCertificate("c");
        certC.setTestCertificate(false);

        final var input = List.of(certA, certB, certC);
        final var ids = List.of("a", "b", "c");

        when(sjukfallCertificateDao.findSickLeavesStoredInCS(ids)).thenReturn(List.of("b", "c"));
        when(csIntegrationService.getValidSickLeaveIds(any(GetValidSickLeaveCertificateIdsInternalRequest.class)))
            .thenReturn(List.of("c"));

        final var result = validSickLeaveAggregator.get(input);

        assertEquals(1, result.size());
        assertTrue(result.stream().anyMatch(c -> "c".equals(c.getId())));
    }
}