package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.persistence.model.dao.CertificateStateHistoryEntry;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRecipientConverter;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRecipientDTO;

import java.util.Collection;

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
        return CitizenCertificateRecipientDTO
                .builder()
                .id("")
                .name("")
                .sent("")
                .build();
    }
}
