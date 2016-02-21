/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
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

import static org.junit.Assert.assertEquals;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import se.inera.intyg.intygstjanst.persistence.model.dao.SendMessageToCare;
import se.inera.intyg.intygstjanst.persistence.model.dao.SendMessageToCareRepository;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/persistence-config-unittest.xml" })
@ActiveProfiles({ "dev", "unit-testing" })
@Transactional
public class SendMessageToCareRepositoryTest {
    
    private static String ENHET_1_ID = "ENHET_1_ID";
    private static String ENHET_2_ID = "ENHET_2_ID";
    private static String ENHET_3_ID = "ENHET_3_ID";
    private static String ENHET_4_ID = "ENHET_4_ID";
    
    @PersistenceContext
    private EntityManager em;
    
    @Autowired
    private SendMessageToCareRepository sendMessageToCareRepository;

    @Test
    public void testFindOne() {
        SendMessageToCare saved = buildFragaSvarFraga(ENHET_1_ID);
        sendMessageToCareRepository.save(saved);
        SendMessageToCare read = sendMessageToCareRepository.findOne(saved.getInternReferens());
        assertEquals(read.getInternReferens(), saved.getInternReferens());
    }
    
    @Test
    public void testFindByMeddelandeId() {
        SendMessageToCare saved = buildFragaSvarFraga(ENHET_1_ID);
        sendMessageToCareRepository.save(saved);
        SendMessageToCare read = sendMessageToCareRepository.findByMeddelandeId(saved.getMeddelandeId());
        assertEquals(read.getInternReferens(), saved.getInternReferens());
    }
    
    @Test
    public void testFindAll() {
        sendMessageToCareRepository.save(buildFragaSvarFraga(ENHET_1_ID));
        sendMessageToCareRepository.save(buildFragaSvarFraga(ENHET_2_ID));
        sendMessageToCareRepository.save(buildFragaSvarFraga(ENHET_3_ID));
        sendMessageToCareRepository.save(buildFragaSvarFraga(ENHET_3_ID));
        sendMessageToCareRepository.save(buildFragaSvarFraga(ENHET_4_ID));
        List<SendMessageToCare> read = sendMessageToCareRepository.findAll();
        assertEquals(read.size(), 5);
    }

    private SendMessageToCare buildFragaSvarFraga(String logiskMottagare) {
        SendMessageToCare sendMessageToCare = new SendMessageToCare();
        sendMessageToCare.setIntygsId("intygsID");
        sendMessageToCare.setMeddelandeId("meddelandeId");
        sendMessageToCare.setReferens(ENHET_2_ID);
        sendMessageToCare.setTimeStamp(LocalDateTime.now());
        sendMessageToCare.setMeddelande("Meddelande");
        sendMessageToCare.setLogiskAdressmottagare(ENHET_3_ID);
        sendMessageToCare.setAmne("OVRIGT");
        return sendMessageToCare;
    }
    
}
