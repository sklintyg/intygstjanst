package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateFilterService;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificatesRequestDTO;

import java.util.List;

@Service
public class CitizenCertificateFilterServiceImpl implements CitizenCertificateFilterService {

    @Override
    public boolean filter(CitizenCertificateDTO certificate, CitizenCertificatesRequestDTO request) {
        return filterOnYears(certificate, request.getYears())
                && filterOnUnits(certificate, request.getUnits())
                && filterOnCertificateTypes(certificate, request.getCertificateTypes())
                && filterOnSentStatus(certificate, request.getStatuses());
    }

    private boolean filterOnYears(CitizenCertificateDTO certificate, List<String> includedYears) {
        if (includedYears == null || includedYears.isEmpty()) {
            return true;
        }

        final var signedYear = certificate.getIssued().getYear();

        return includedYears
                .stream()
                .anyMatch((year) -> Integer.parseInt(year) == signedYear);
    }

    private boolean filterOnSentStatus(CitizenCertificateDTO certificate, List<CitizenCertificateStatusTypeDTO> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return true;
        }

        final var includeSent = statuses.stream().anyMatch((status) -> status == CitizenCertificateStatusTypeDTO.SENT);
        final var includeNotSent = statuses.stream().anyMatch((status) -> status == CitizenCertificateStatusTypeDTO.NOT_SENT);

        if (includeSent && includeNotSent) {
            return filterOnSent(certificate) || filterOnNotSent(certificate);
        }

        if (includeSent) {
            return filterOnSent(certificate);
        }

        if (includeNotSent) {
            return filterOnNotSent(certificate);
        }

        return true;
    }

    private boolean filterOnSent(CitizenCertificateDTO certificate) {
        return certificate.getRecipient() != null && certificate.getRecipient().getSent() != null;
    }

    private boolean filterOnNotSent(CitizenCertificateDTO certificate) {
        return certificate.getRecipient() != null && certificate.getRecipient().getSent() == null;
    }

    private boolean filterOnUnits(CitizenCertificateDTO certificate, List<String> unitIds) {
        if (unitIds == null || unitIds.isEmpty()) {
            return true;
        }

        return unitIds.contains(certificate.getUnit().getId());
    }

    private boolean filterOnCertificateTypes(CitizenCertificateDTO certificate, List<String> certificateTypes) {
        if (certificateTypes == null || certificateTypes.isEmpty()) {
            return true;
        }

        return certificateTypes.contains(certificate.getType().getId());
    }
}
