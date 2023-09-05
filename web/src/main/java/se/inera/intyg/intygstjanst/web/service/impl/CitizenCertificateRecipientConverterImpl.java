package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.common.support.model.CertificateState;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRecipientConverter;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRecipientDTO;

import java.util.Collection;
import java.util.Optional;

@Service
public class CitizenCertificateRecipientConverterImpl implements CitizenCertificateRecipientConverter {

    @Override
    public CitizenCertificateRecipientDTO get(String id, String name, String sent) {
        return CitizenCertificateRecipientDTO
                .builder()
                .id(id)
                .name(name)
                .sent(sent)
                .build();
    }

    @Override
    public CitizenCertificateRecipientDTO get(Collection<CertificateStateHistoryEntry> states) {

        final var sentState = getSentState(states);

        return sentState.map(certificateStateHistoryEntry -> CitizenCertificateRecipientDTO
                .builder()
                .id(certificateStateHistoryEntry.getTarget())
                .name(getRecipientName(certificateStateHistoryEntry.getTarget()))
                .sent(certificateStateHistoryEntry.getTimestamp().toString()) //TODO: How should we format it to ISO?
                .build()).orElse(null);
    }

    private String getRecipientName(String id) {
        if (id.equals("TRANSP")) {
            return "Transportstyrelsen";
        }

        if (id.equals("FKASSA")) {
            return "Försäkringskassan"; //TODO: Can the names be gotten from some other place?
        }

        return null;
    }

    private Optional<CertificateStateHistoryEntry> getSentState(Collection<CertificateStateHistoryEntry> states) {
        return states.stream().filter((state) -> state.getState() == CertificateState.SENT).findFirst();
    }
}
