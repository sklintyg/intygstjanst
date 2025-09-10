package se.inera.intyg.intygstjanst.web.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.persistence.model.dao.SjukfallCertificateDao;
import se.inera.intyg.intygstjanst.web.csintegration.CSIntegrationService;
import se.inera.intyg.intygstjanst.web.csintegration.dto.GetCertificateXmlResponse;

@Service
@RequiredArgsConstructor
public class HandleSickLeaveService {

    private final SjukfallCertificateDao sjukfallCertificateDao;
    private final CSIntegrationService csIntegrationService;

    public void created(GetCertificateXmlResponse response) {

        final var sickLeaveResponse = csIntegrationService.getSickLeaveCertificate(response.getCertificateId());

        if (sickLeaveResponse.isAvailable()) {
            sjukfallCertificateDao.store(sickLeaveResponse.getSickLeaveCertificate());
        }
    }

    public void revoked(GetCertificateXmlResponse response) {

        final var sickLeaveResponse = csIntegrationService.getSickLeaveCertificate(response.getCertificateId());

        if (sickLeaveResponse.isAvailable()) {
          sjukfallCertificateDao.revoke(sickLeaveResponse.getSickLeaveCertificate().getId());
        }
    }
}