package se.inera.intyg.intygstjanst.web.service;

import se.inera.intyg.intygstjanst.web.integration.citizen.CitizenCertificateStatusTypeDTO;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateDTO;

import java.util.List;

public interface CitizenCertificateFilterService {
    boolean filterOnYears(CitizenCertificateDTO certificate, List<String> includedYears);

    boolean filterOnSentStatus(CitizenCertificateDTO certificate, List<CitizenCertificateStatusTypeDTO> statuses);

    boolean filterOnUnits(CitizenCertificateDTO certificate, List<String> unitIds);

    boolean filterOnCertificateTypes(CitizenCertificateDTO certificate, List<String> certificateTypes);

    boolean filter(CitizenCertificateDTO certificate, List<String> includedYears, List<String> unitIds,
                   List<String> certificateTypes, List<CitizenCertificateStatusTypeDTO> statuses);

}
