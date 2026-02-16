package se.inera.intyg.intygstjanst.web.csintegration.aggregator;

import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificate;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.csintegration.CSIntegrationService;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetValidSickLeaveCertificateIdsInternalRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidSickLeaveAggregator {

    private final SjukfallCertificateDao sjukfallCertificateDao;
    private final CSIntegrationService csIntegrationService;

    public List<SjukfallCertificate> get(List<SjukfallCertificate> sjukfallCertificate) {
        final var sickLeaveIds = sjukfallCertificate.stream()
            .map(SjukfallCertificate::getId)
            .toList();

        log.info("Checking validity of sick leave certificates with ids: {}", sickLeaveIds);
        final var sickLeavesStoredInCS = sjukfallCertificateDao.findSickLeavesStoredInCS(sickLeaveIds);
        log.info("Sick leave certificates stored in CS: {}", sickLeavesStoredInCS);

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
        log.info("Valid sick leave certificate ids from CS: {}", validSickLeaveIdsFromCS);

        final var validSickLeaveIdsFromIT = sickLeaveIds.stream()
            .filter(id -> !sickLeavesStoredInCS.contains(id))
            .toList();
        log.info("Valid sick leave certificate ids from IT (not stored in CS): {}", validSickLeaveIdsFromIT);

        final var validSickLeaveIds = Stream.concat(
                validSickLeaveIdsFromCS.stream(),
                validSickLeaveIdsFromIT.stream()
            )
            .toList();

        log.info("All valid sick leave certificate ids: {}", validSickLeaveIds);

        return sjukfallCertificate.stream()
            .filter(sickLeave -> validSickLeaveIds.contains(sickLeave.getId()))
            .filter(sickLeave -> !sickLeave.isTestCertificate())
            .toList();
    }
}