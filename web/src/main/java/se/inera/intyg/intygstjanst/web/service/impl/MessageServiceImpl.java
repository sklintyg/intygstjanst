/*
 * Copyright (C) 2022 Inera AB (http://www.inera.se)
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

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.inera.intyg.infra.message.dto.MessageFromIT;
import se.inera.intyg.intygstjanst.persistence.model.dao.Arende;
import se.inera.intyg.intygstjanst.persistence.model.dao.ArendeRepository;
import se.inera.intyg.intygstjanst.web.service.MessageService;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private ArendeRepository messageRepository;

    @Override
    public List<MessageFromIT> findMessagesByCertificateId(String certificateId) {
        final var messages = messageRepository.findByIntygsId(certificateId);
        return messages.stream()
            .map(message -> convert(message))
            .collect(Collectors.toList());
    }

    private MessageFromIT convert(Arende message) {
        return MessageFromIT.create(message.getIntygsId(), message.getMeddelandeId(), message.getMeddelande(), message.getAmne(),
            message.getLogiskAdressmottagare(), message.getTimestamp());
    }
}
