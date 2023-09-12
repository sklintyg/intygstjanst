/*
 * Copyright (C) 2023 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.inera.intyg.intygstjanst.web.service.impl;

import org.springframework.stereotype.Service;
import se.inera.intyg.intygstjanst.web.service.CitizenCertificateRecipientConverter;
import se.inera.intyg.intygstjanst.web.service.bean.CertificateRecipientType;
import se.inera.intyg.intygstjanst.web.service.dto.citizen.CitizenCertificateRecipientDTO;
import se.inera.intyg.intygstjanst.web.service.repo.RecipientRepo;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CitizenCertificateRecipientConverterImpl implements CitizenCertificateRecipientConverter {

    private final RecipientRepo recipientRepo;

    public CitizenCertificateRecipientConverterImpl(RecipientRepo recipientRepo) {
        this.recipientRepo = recipientRepo;
    }

    @Override
    public Optional<CitizenCertificateRecipientDTO> convert(String certificateType, LocalDateTime sent) {
        return recipientRepo
                .listRecipients()
                .stream()
                .filter(
                        (recipient) -> recipient.getCertificateTypes().contains(certificateType)
                                && recipient.getRecipientType() == CertificateRecipientType.HUVUDMOTTAGARE
                )
                .findFirst()
                .map(recipient ->
                        CitizenCertificateRecipientDTO
                                .builder()
                                .id(recipient.getId())
                                .name(recipient.getName())
                                .sent(sent)
                                .build()
                );
    }
}
