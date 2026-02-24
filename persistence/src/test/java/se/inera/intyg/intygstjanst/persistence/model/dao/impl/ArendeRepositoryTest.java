/*
 * Copyright (C) 2025 Inera AB (http://www.inera.se)
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
package se.inera.intyg.intygstjanst.persistence.model.dao.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import se.inera.intyg.intygstjanst.persistence.config.JpaConstants;
import se.inera.intyg.intygstjanst.persistence.model.dao.Arende;
import se.inera.intyg.intygstjanst.persistence.model.dao.ArendeRepository;

class ArendeRepositoryTest extends TestSupport {

    private static String ENHET_1_ID = "ENHET_1_ID";
    private static String ENHET_2_ID = "ENHET_2_ID";
    private static String ENHET_3_ID = "ENHET_3_ID";
    private static String ENHET_4_ID = "ENHET_4_ID";

    @PersistenceContext(unitName = JpaConstants.PERSISTANCE_UNIT_NAME)
    private EntityManager entityManager;

    @Autowired
    private ArendeRepository sendMessageToCareRepository;

    @Test
    void testFindOne() {
        Arende saved = buildFragaSvarFraga(ENHET_1_ID);
        sendMessageToCareRepository.save(saved);
        Arende read = sendMessageToCareRepository.findById(saved.getInternReferens()).orElse(null);
        assertEquals(read.getInternReferens(), saved.getInternReferens());
    }

    @Test
    void testFindByMeddelandeId() {
        Arende saved = buildFragaSvarFraga(ENHET_1_ID);
        sendMessageToCareRepository.save(saved);
        Arende read = sendMessageToCareRepository.findByMeddelandeId(saved.getMeddelandeId());
        assertEquals(read.getInternReferens(), saved.getInternReferens());
    }

    @Test
    void testFindAll() {
        sendMessageToCareRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        sendMessageToCareRepository.save(buildFragaSvarFraga(ENHET_2_ID));
        sendMessageToCareRepository.save(buildFragaSvarFraga(ENHET_3_ID));
        sendMessageToCareRepository.save(buildFragaSvarFraga(ENHET_3_ID));
        sendMessageToCareRepository.save(buildFragaSvarFraga(ENHET_4_ID));
        List<Arende> read = sendMessageToCareRepository.findAll();
        assertEquals(5, read.size());
    }

    private Arende buildFragaSvarFraga(String logiskMottagare) {
        Arende sendMessageToCare = new Arende();
        sendMessageToCare.setIntygsId("intygsID");
        sendMessageToCare.setMeddelandeId(UUID.randomUUID().toString());
        sendMessageToCare.setReferens(ENHET_2_ID);
        sendMessageToCare.setTimeStamp(LocalDateTime.now());
        sendMessageToCare.setMeddelande("Meddelande");
        sendMessageToCare.setLogiskAdressmottagare(ENHET_3_ID);
        sendMessageToCare.setAmne("OVRIGT");
        return sendMessageToCare;
    }

}
