package se.inera.intyg.intygstjanst.application.sickleave.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.infrastructure.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.CSIntegrationService;
import se.inera.intyg.intygstjanst.infrastructure.csintegration.dto.GetCertificateXmlResponse;
import se.inera.intyg.intygstjanst.application.sickleave.converter.SickLeaveCertificateToSjukfallCertificateConverter;

@Service
@RequiredArgsConstructor
public class HandleSickLeaveService {

    private final SjukfallCertificateDao sjukfallCertificateDao;
    private final CSIntegrationService csIntegrationService;
    private final SickLeaveCertificateToSjukfallCertificateConverter converter;

    public void created(GetCertificateXmlResponse response) {

        final var sickLeaveResponse = csIntegrationService.getSickLeaveCertificate(response.getCertificateId());

        if (sickLeaveResponse.isAvailable()) {
            sjukfallCertificateDao.store(converter.convert(sickLeaveResponse.getSickLeaveCertificate()));
        }
    }

    public void revoked(GetCertificateXmlResponse response) {

        final var sickLeaveResponse = csIntegrationService.getSickLeaveCertificate(response.getCertificateId());

        if (sickLeaveResponse.isAvailable()) {
            sjukfallCertificateDao.revoke(sickLeaveResponse.getSickLeaveCertificate().getId());
        }
    }
}