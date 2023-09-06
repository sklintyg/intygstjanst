package se.inera.intyg.intygstjanst.web.service.impl;

import se.inera.intyg.intygstjanst.web.integration.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateFilterService;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;

import java.util.List;

public class CitizenCertificateFilterServiceImpl implements CitizenCertificateFilterService {
    @Override
    public boolean filterOnYears(CitizenCertificateDTO certificate, List<String> includedYears) {
        final var signedYear = certificate.getIssued().substring(0, 3);

        return includedYears
                .stream()
                .anyMatch((year) -> year.equals(signedYear));
    }

    @Override
    public boolean filterOnSentStatus(CitizenCertificateDTO certificate, List<CitizenCertificateStatusTypeDTO> statuses) {
        final var includeSent = statuses.stream().anyMatch((status) -> status == CitizenCertificateStatusTypeDTO.SENT);
        final var includeNotSent = statuses.stream().anyMatch((status) -> status == CitizenCertificateStatusTypeDTO.NOT_SENT);

        if (includeSent) {
            return certificate.getRecipient() != null && certificate.getRecipient().getSent() != null;
        }

        if (includeNotSent) {
            return certificate.getRecipient() != null && certificate.getRecipient().getSent() == null;
        }

        return true;
    }

    @Override
    public boolean filterOnUnits(CitizenCertificateDTO certificate, List<String> unitIds) {
        return unitIds.contains(certificate.getUnit().getId());
    }

    @Override
    public boolean filterOnCertificateTypes(CitizenCertificateDTO certificate, List<String> certificateTypes) {
        return certificateTypes.contains(certificate.getType().getId());
    }
}
