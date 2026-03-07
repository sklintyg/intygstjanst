package se.inera.intyg.intygstjanst.web.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.persistence.model.dao.ApprovedReceiverDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.ArendeRepository;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateRepository;
import se.inera.intyg.intygstjanst.persistence.model.dao.RelationDao;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;

@Slf4j
@Service
@RequiredArgsConstructor
public class EraseCertificatesFromIT {

    @Value("${erase.certificates.page.size:1000}")
    private int eraseCertificatesPageSize;
    
    private final CertificateRepository certificateRepository;
    private final ArendeRepository arendeRepository;
    private final ApprovedReceiverDao approvedReceiverDao;
    private final RelationDao relationDao;
    private final SjukfallCertificateDao sjukfallCertificateDao;
    private final CertificateDao certificateDao;

    public void eraseCertificates(String careProviderId) {
        final var erasePageable = PageRequest.of(0, eraseCertificatesPageSize, Sort.by(Direction.ASC, "signedDate", "id"));
        Page<String> certificateIdPage = Page.empty();
        int erasedMessagesTotal = 0;
        int erasedSjukfallTotal = 0;
        int erasedCertificatesTotal = 0;
        int erasedCertificates = 0;

        try {
            do {
                erasedCertificates = 0;
                certificateIdPage = certificateRepository.findCertificateIdsForCareProvider(careProviderId, erasePageable);
                final var certificateIds = certificateIdPage.getContent();

                if (certificateIds.isEmpty()) {
                    break;
                }

                log.info("Starting batch erasure of {} certificates for care provider {}.", certificateIds.size(), careProviderId);

                approvedReceiverDao.eraseApprovedReceivers(certificateIds, careProviderId);
                relationDao.eraseCertificateRelations(certificateIds, careProviderId);
                erasedMessagesTotal += eraseMessages(certificateIds, careProviderId);
                erasedSjukfallTotal += sjukfallCertificateDao.eraseCertificates(certificateIds, careProviderId);
                erasedCertificates = certificateDao.eraseCertificates(certificateIds, careProviderId);
                erasedCertificatesTotal += erasedCertificates;

                log.info("Completed batch erasure of {} certificates for care provider {}. Certificates remaining: {}.",
                    certificateIds.size(), careProviderId, certificateIdPage.getTotalElements() - erasedCertificates);

            } while (certificateIdPage.hasNext());

            log.info("Successfully completed erasure of certificates for care provider {}. Total number of erased certificates: {}, "
                    + "sjukfallCertificates: {}, messages: {}.", careProviderId, erasedCertificatesTotal, erasedSjukfallTotal,
                erasedMessagesTotal);

        } catch (Exception e) {
            log.error("Error erasing certificates for care provider {}. Number of erased certificates: {}, sjukfallCertificates: {}, "
                    + "messages: {}. Certificates remaining: {}.", careProviderId, erasedCertificatesTotal, erasedSjukfallTotal,
                erasedMessagesTotal, certificateIdPage.getTotalElements() - erasedCertificates, e);
            throw e;
        }
    }

    private int eraseMessages(List<String> certificateIds, String careProviderId) {
        final var erasedArendeCount = arendeRepository.eraseArendenByCertificateIds(certificateIds);
        log.debug("Erased {} Arenden for care provider {}.", erasedArendeCount, careProviderId);
        return erasedArendeCount;
    }
}