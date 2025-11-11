package se.inera.intyg.intygstjanst.web.csintegration.aggregator;

import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.csintegration.CSIntegrationService;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetValidSickLeaveCertificateIdsInternalRequest;

@Service
@RequiredArgsConstructor
public class ValidSickLeaveAggregator {

    private final SjukfallCertificateDao sjukfallCertificateDao;
    private final CSIntegrationService csIntegrationService;

    public List<SjukfallCertificate> get(List<SjukfallCertificate> sjukfallCertificate) {
        final var sickLeaveIds = sjukfallCertificate.stream()
            .map(SjukfallCertificate::getId)
            .toList();

        final var sickLeavesStoredInCS = sjukfallCertificateDao.findSickLeavesStoredInCS(sickLeaveIds);

        if (sickLeavesStoredInCS.isEmpty()) {
            return sjukfallCertificate.stream()
                .filter(sickLeave -> !sickLeave.isTestCertificate())
                .toList();
        }

        final var validSickLeaveIdsFromCS = csIntegrationService.getValidSickLeaveIds(
            GetValidSickLeaveCertificateIdsInternalRequest.builder()
                .certificateIds(sickLeavesStoredInCS)
                .build()
        );

        final var validSickLeaveIdsFromIT = sickLeaveIds.stream()
            .filter(id -> !sickLeavesStoredInCS.contains(id))
            .toList();

        final var validSickLeaveIds = Stream.concat(
                validSickLeaveIdsFromCS.stream(),
                validSickLeaveIdsFromIT.stream()
            )
            .toList();

        return sjukfallCertificate.stream()
            .filter(sickLeave -> validSickLeaveIds.contains(sickLeave.getId()))
            .filter(sickLeave -> !sickLeave.isTestCertificate())
            .toList();
    }
}